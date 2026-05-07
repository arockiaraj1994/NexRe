package com.mindshift.nexre.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

data class OgData(
    val title: String,
    val description: String,
    val imageUrl: String,
    val sourcePlatform: String,
    val bodyText: String,
)

@Singleton
class OgFetcher @Inject constructor() {

    suspend fun fetch(url: String): OgData = withContext(Dispatchers.IO) {
        val resolvedUrl = resolveUrl(url)

        // PDF files cannot be parsed as HTML — save with minimal metadata
        if (isPdfUrl(resolvedUrl)) {
            val title = resolvedUrl.substringAfterLast('/').removeSuffix(".pdf").ifBlank { "PDF Document" }
            return@withContext OgData(
                title = title,
                description = "",
                imageUrl = "",
                sourcePlatform = detectPlatform(resolvedUrl),
                bodyText = "",
            )
        }

        val doc = Jsoup.connect(resolvedUrl)
            .userAgent("Mozilla/5.0 (compatible; NexRe/1.0)")
            .timeout(10_000)
            .ignoreContentType(true)
            .get()

        val title = doc.select("meta[property=og:title]").attr("content")
            .ifBlank { doc.title() }
            .ifBlank { resolvedUrl }

        val description = doc.select("meta[property=og:description]").attr("content")
            .ifBlank { doc.select("meta[name=description]").attr("content") }

        val imageUrl = doc.select("meta[property=og:image]").attr("content")

        val bodyText = doc.select("article, main, .content, .post-content, .entry-content")
            .firstOrNull()
            ?.text()
            ?.take(3000)
            ?: doc.body().text().take(3000)

        OgData(
            title = title,
            description = description,
            imageUrl = imageUrl,
            sourcePlatform = detectPlatform(url),
            bodyText = bodyText,
        )
    }

    // arXiv: /pdf/XXXX → /abs/XXXX so we get the HTML abstract page with OG tags
    private fun resolveUrl(url: String): String {
        val lower = url.lowercase()
        if ("arxiv.org/pdf/" in lower) {
            return url
                .replace("arxiv.org/pdf/", "arxiv.org/abs/", ignoreCase = true)
                .removeSuffix(".pdf")
        }
        return url
    }

    private fun isPdfUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.endsWith(".pdf") || lower.contains("/pdf?") || lower.contains("type=pdf")
    }

    fun detectPlatform(url: String): String {
        val lower = url.lowercase()
        return when {
            "github.com" in lower -> "GITHUB"
            "linkedin.com" in lower -> "LINKEDIN"
            "twitter.com" in lower || "x.com" in lower -> "TWITTER"
            "medium.com" in lower -> "MEDIUM"
            "dev.to" in lower -> "DEV"
            "stackoverflow.com" in lower -> "STACKOVERFLOW"
            "arxiv.org" in lower -> "RESEARCH"
            else -> "WEB"
        }
    }
}
