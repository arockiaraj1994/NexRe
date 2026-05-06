package com.mindshift.nexre.domain.usecase

import com.mindshift.nexre.data.remote.KeywordTagger
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.LinkStatus
import com.mindshift.nexre.domain.model.SourcePlatform
import com.mindshift.nexre.domain.model.SummarySource
import com.mindshift.nexre.domain.repository.LinkRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class SaveTextUseCase @Inject constructor(
    private val keywordTagger: KeywordTagger,
    private val linkRepository: LinkRepository,
) {
    suspend operator fun invoke(text: String): Link {
        val trimmed = text.trim()
        val title = trimmed.lines().firstOrNull { it.isNotBlank() }?.take(120) ?: "Note"
        val tags = keywordTagger.tag(title, trimmed)
        val link = Link(
            id = UUID.randomUUID().toString(),
            url = "",
            title = title,
            description = trimmed,
            thumbnailUrl = "",
            sourcePlatform = SourcePlatform.TEXT,
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
        linkRepository.saveLink(link, "KEYWORD")
        return link
    }
}
