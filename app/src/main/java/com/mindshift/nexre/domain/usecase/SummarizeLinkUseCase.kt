package com.mindshift.nexre.domain.usecase

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mindshift.nexre.data.remote.GeminiApiService
import com.mindshift.nexre.data.remote.KeywordTagger
import com.mindshift.nexre.data.remote.OgFetcher
import com.mindshift.nexre.data.remote.model.Content
import com.mindshift.nexre.data.remote.model.GeminiRequest
import com.mindshift.nexre.data.remote.model.GeminiSummaryResult
import com.mindshift.nexre.data.remote.model.Part
import com.mindshift.nexre.data.remote.model.SystemInstruction
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.LinkStatus
import com.mindshift.nexre.domain.model.SourcePlatform
import com.mindshift.nexre.domain.model.SummarySource
import com.mindshift.nexre.domain.repository.LinkRepository
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class SummarizeLinkUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ogFetcher: OgFetcher,
    private val keywordTagger: KeywordTagger,
    private val geminiApiService: GeminiApiService,
    private val moshi: Moshi,
    private val linkRepository: LinkRepository,
) {
    sealed interface Result {
        data class Success(val link: Link) : Result
        data object NoApiKey : Result
        data object NoInternet : Result
        data class GeminiError(val message: String) : Result
    }

    suspend fun invokeText(text: String): Result {
        val apiKey = getApiKey() ?: return Result.NoApiKey
        val title = text.lines().firstOrNull { it.isNotBlank() }?.take(120) ?: "Note"
        val prompt = buildPrompt(title, "", text.take(3000))
        val request = GeminiRequest(contents = listOf(Content(parts = listOf(Part(prompt)))), systemInstruction = jsonOnlyInstruction())
        return callGeminiAndSave(
            apiKey = apiKey,
            request = request,
            url = "",
            title = title,
            description = text,
            imageUrl = "",
            sourcePlatform = SourcePlatform.TEXT,
            fallbackText = text,
        )
    }

    suspend operator fun invoke(url: String): Result {
        val apiKey = getApiKey() ?: return Result.NoApiKey

        val og = try {
            ogFetcher.fetch(url)
        } catch (e: Exception) {
            return Result.NoInternet
        }

        val prompt = buildPrompt(og.title, og.description, og.bodyText)
        val request = GeminiRequest(contents = listOf(Content(parts = listOf(Part(prompt)))), systemInstruction = jsonOnlyInstruction())
        return callGeminiAndSave(
            apiKey = apiKey,
            request = request,
            url = url,
            title = og.title,
            description = og.description,
            imageUrl = og.imageUrl,
            sourcePlatform = runCatching { SourcePlatform.valueOf(og.sourcePlatform) }.getOrDefault(SourcePlatform.WEB),
            fallbackText = og.description,
        )
    }

    private suspend fun callGeminiAndSave(
        apiKey: String,
        request: GeminiRequest,
        url: String,
        title: String,
        description: String,
        imageUrl: String,
        sourcePlatform: SourcePlatform,
        fallbackText: String,
    ): Result {
        val modelId = getModelId()
        val endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent"
        val (summary, geminiTags, summarySource) = try {
            val response = geminiApiService.generateContent(endpointUrl, apiKey, request)
            if (response.isSuccessful) {
                val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                android.util.Log.d("NexRe_Gemini", "raw response: $rawText")
                if (rawText != null) {
                    val cleanJson = extractJson(rawText)
                    android.util.Log.d("NexRe_Gemini", "extracted json: $cleanJson")
                    val parsed = if (cleanJson != null) runCatching {
                        moshi.adapter(GeminiSummaryResult::class.java).fromJson(cleanJson)
                    }.also { r ->
                        r.exceptionOrNull()?.let { android.util.Log.e("NexRe_Gemini", "parse error: $it") }
                    }.getOrNull() else null
                    if (parsed != null) Triple(parsed.summary, parsed.tags, SummarySource.GEMINI)
                    else Triple(fallbackText, keywordTagger.tag(title, description), SummarySource.OG_META)
                } else {
                    android.util.Log.w("NexRe_Gemini", "null response text, HTTP ${response.code()}")
                    Triple(fallbackText, keywordTagger.tag(title, description), SummarySource.OG_META)
                }
            } else {
                android.util.Log.w("NexRe_Gemini", "HTTP error ${response.code()}: ${response.errorBody()?.string()}")
                Triple(fallbackText, keywordTagger.tag(title, description), SummarySource.OG_META)
            }
        } catch (e: Exception) {
            android.util.Log.e("NexRe_Gemini", "exception: $e")
            Triple(fallbackText, keywordTagger.tag(title, description), SummarySource.OG_META)
        }

        val allTags = (geminiTags + keywordTagger.tag(title, description)).distinct()
        val link = Link(
            id = UUID.randomUUID().toString(),
            url = url,
            title = title,
            description = description,
            thumbnailUrl = imageUrl,
            sourcePlatform = sourcePlatform,
            status = LinkStatus.UNREAD,
            isFavourite = false,
            personalNote = "",
            summary = summary,
            summarySource = summarySource,
            tags = allTags,
            savedAt = Instant.now().toEpochMilli(),
            openedAt = 0L,
            readDurationSec = 0,
            readCount = 0,
        )
        linkRepository.saveLink(link, if (summarySource == SummarySource.GEMINI) "GEMINI" else "KEYWORD")
        return Result.Success(link)
    }

    private fun jsonOnlyInstruction() = SystemInstruction(
        parts = listOf(Part("You are a JSON-only assistant. Output ONLY a valid JSON object. No explanations, no reasoning, no markdown, no chain of thought. Just the raw JSON.")),
    )

    private fun extractJson(text: String): String? {
        // Extract from ```json ... ``` block anywhere in the response
        val fenceRegex = Regex("```json\\s*\\n([\\s\\S]*?)\\n\\s*```", RegexOption.MULTILINE)
        fenceRegex.find(text)?.groupValues?.get(1)?.trim()?.let { return it }
        // Fallback: extract outermost { ... } object
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start != -1 && end > start) return text.substring(start, end + 1).trim()
        return null
    }

    private fun buildPrompt(title: String, description: String, body: String) = """
Title: $title
Description: $description
Content: ${body.take(3000)}

Return a JSON object with exactly two fields:

"summary": Write 4-6 bullet points. Each bullet MUST start with "• " and be on its own line separated by a newline character (\n). Do NOT merge bullets into a paragraph. Cover: what the topic is, key findings, why it matters, and notable details. Each bullet is one sentence.

"tags": Array of 2-5 lowercase topic tags (single words or short phrases).

Output format (follow this exactly):
{
  "summary": "• What it is: one sentence here\n• Key finding: one sentence here\n• Why it matters: one sentence here\n• Notable detail: one sentence here",
  "tags": ["tag1", "tag2", "tag3"]
}
    """.trimIndent()

    private fun getApiKey(): String? {
        return try {
            val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            val prefs = EncryptedSharedPreferences.create(
                context, "nexre_secure_prefs", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
            prefs.getString("gemini_api_key", null)?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    private fun getModelId(): String {
        return try {
            context.getSharedPreferences("nexre_prefs", Context.MODE_PRIVATE)
                .getString("gemini_model_id", DEFAULT_MODEL_ID)
                ?.takeIf { it.isNotBlank() }
                ?: DEFAULT_MODEL_ID
        } catch (e: Exception) {
            DEFAULT_MODEL_ID
        }
    }

    companion object {
        const val DEFAULT_MODEL_ID = "gemini-3.1-flash-lite-preview"
    }
}
