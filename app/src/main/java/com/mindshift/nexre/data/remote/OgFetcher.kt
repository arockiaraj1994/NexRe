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
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (compatible; NexRe/1.0)")
            .timeout(10_000)
            .get()

        val title = doc.select("meta[property=og:title]").attr("content")
            .ifBlank { doc.title() }
            .ifBlank { url }

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
