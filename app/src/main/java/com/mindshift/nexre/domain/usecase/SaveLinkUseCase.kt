package com.mindshift.nexre.domain.usecase

import com.mindshift.nexre.data.remote.KeywordTagger
import com.mindshift.nexre.data.remote.OgFetcher
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.LinkStatus
import com.mindshift.nexre.domain.model.SourcePlatform
import com.mindshift.nexre.domain.model.SummarySource
import com.mindshift.nexre.domain.repository.LinkRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class SaveLinkUseCase @Inject constructor(
    private val ogFetcher: OgFetcher,
    private val keywordTagger: KeywordTagger,
    private val linkRepository: LinkRepository,
) {
    suspend operator fun invoke(url: String): Link {
        val existing = linkRepository.getLinkByUrl(url)
        if (existing != null) return existing
        val og = ogFetcher.fetch(url)
        val tags = keywordTagger.tag(og.title, og.description)
        val wordCount = og.bodyText.split("\\s+".toRegex()).count { it.isNotBlank() }
        val link = Link(
            id = UUID.randomUUID().toString(),
            url = url,
            title = og.title,
            description = og.description,
            thumbnailUrl = og.imageUrl,
            sourcePlatform = runCatching { SourcePlatform.valueOf(og.sourcePlatform) }.getOrDefault(SourcePlatform.WEB),
            status = LinkStatus.UNREAD,
            isFavourite = false,
            personalNote = "",
            summary = og.description,
            summarySource = SummarySource.OG_META,
            tags = tags,
            savedAt = Instant.now().toEpochMilli(),
            openedAt = 0L,
            readDurationSec = 0,
            readCount = 0,
            wordCount = wordCount,
        )
        linkRepository.saveLink(link, "KEYWORD")
        return link
    }
}
