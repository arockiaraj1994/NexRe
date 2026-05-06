package com.mindshift.nexre.domain.model

data class Link(
    val id: String,
    val url: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val sourcePlatform: SourcePlatform,
    val status: LinkStatus,
    val isFavourite: Boolean,
    val personalNote: String,
    val summary: String,
    val summarySource: SummarySource,
    val tags: List<String>,
    val savedAt: Long,
    val openedAt: Long,
    val readDurationSec: Int,
    val readCount: Int,
)

enum class LinkStatus { UNREAD, READ, ARCHIVED }

enum class SummarySource { NONE, OG_META, GEMINI }

enum class SourcePlatform {
    GITHUB, LINKEDIN, TWITTER, MEDIUM, DEV, STACKOVERFLOW, RESEARCH, WEB, TEXT;

    val displayName: String get() = when (this) {
        GITHUB -> "GitHub"
        LINKEDIN -> "LinkedIn"
        TWITTER -> "X / Twitter"
        MEDIUM -> "Medium"
        DEV -> "dev.to"
        STACKOVERFLOW -> "Stack Overflow"
        RESEARCH -> "arXiv"
        WEB -> "Web"
        TEXT -> "Note"
    }
}
