package com.mindshift.nexre.domain.usecase

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.repository.LinkRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExportJsonUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val linkRepository: LinkRepository,
) {
    suspend operator fun invoke(): String {
        val links = linkRepository.getAllLinks().first()
        val json = buildJson(links)
        return writeToDownloads(json)
    }

    private fun buildJson(links: List<Link>): String {
        val dateStr = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())
        val appVersion = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) { "1.0.0" }

        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"exported_at\": \"$dateStr\",\n")
        sb.append("  \"app_version\": \"$appVersion\",\n")
        sb.append("  \"total_links\": ${links.size},\n")
        sb.append("  \"links\": [\n")
        links.forEachIndexed { i, link ->
            val tagsJson = link.tags.joinToString(", ") { "\"${it.replace("\"", "\\\"")}\"" }
            sb.append("    {\n")
            sb.append("      \"id\": \"${link.id}\",\n")
            sb.append("      \"url\": \"${link.url.replace("\"", "\\\"")}\" ,\n")
            sb.append("      \"title\": \"${link.title.replace("\"", "\\\"")}\" ,\n")
            sb.append("      \"description\": \"${link.description.replace("\"", "\\\"")}\" ,\n")
            sb.append("      \"thumbnail_url\": \"${link.thumbnailUrl}\",\n")
            sb.append("      \"source_platform\": \"${link.sourcePlatform.name}\",\n")
            sb.append("      \"status\": \"${link.status.name}\",\n")
            sb.append("      \"is_favourite\": ${link.isFavourite},\n")
            sb.append("      \"personal_note\": \"${link.personalNote.replace("\"", "\\\"")}\" ,\n")
            sb.append("      \"summary\": \"${link.summary.replace("\"", "\\\"")}\" ,\n")
            sb.append("      \"summary_source\": \"${link.summarySource.name}\",\n")
            sb.append("      \"tags\": [$tagsJson],\n")
            sb.append("      \"saved_at\": ${link.savedAt},\n")
            sb.append("      \"opened_at\": ${link.openedAt},\n")
            sb.append("      \"read_duration_sec\": ${link.readDurationSec},\n")
            sb.append("      \"read_count\": ${link.readCount}\n")
            sb.append("    }${if (i < links.size - 1) "," else ""}\n")
        }
        sb.append("  ]\n}")
        return sb.toString()
    }

    private fun writeToDownloads(json: String): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fileName = "nexre_export_$date.json"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!
            context.contentResolver.openOutputStream(uri)!!.use { it.write(json.toByteArray()) }
        } else {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            file.writeText(json)
        }
        return fileName
    }
}
