package com.mindshift.nexre.domain.usecase

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.mindshift.nexre.data.remote.KeywordTagger
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.LinkStatus
import com.mindshift.nexre.domain.model.SourcePlatform
import com.mindshift.nexre.domain.model.SummarySource
import com.mindshift.nexre.domain.repository.LinkRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class SaveImageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keywordTagger: KeywordTagger,
    private val linkRepository: LinkRepository,
) {
    sealed interface Result {
        data class Success(val link: Link) : Result
        data object StorageError : Result
    }

    suspend operator fun invoke(uri: Uri): Result = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val filename = resolveFilename(uri) ?: "image_$id.jpg"
        val title = filename.substringBeforeLast('.')

        val imagesDir = File(context.filesDir, "images").also { it.mkdirs() }
        val destFile = File(imagesDir, "${id}_$filename")

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output -> input.copyTo(output) }
            } ?: return@withContext Result.StorageError
        } catch (e: IOException) {
            return@withContext Result.StorageError
        }

        val tags = (keywordTagger.tag(title, "") + listOf("image")).distinct()
        val link = Link(
            id = id,
            url = "",
            title = title,
            description = "",
            thumbnailUrl = "file://${destFile.absolutePath}",
            sourcePlatform = SourcePlatform.IMAGE,
            status = LinkStatus.UNREAD,
            isFavourite = false,
            personalNote = "",
            summary = "",
            summarySource = SummarySource.NONE,
            tags = tags,
            savedAt = Instant.now().toEpochMilli(),
            openedAt = 0L,
            readDurationSec = 0,
            readCount = 0,
        )
        Result.Success(link)
    }

    suspend fun savePendingLink(link: Link) {
        linkRepository.saveLink(link, "KEYWORD")
    }

    private fun resolveFilename(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
        } catch (_: Exception) {
            null
        }
    }
}
