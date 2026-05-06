package com.mindshift.nexre.data.repository

import com.mindshift.nexre.data.local.dao.LinkDao
import com.mindshift.nexre.data.local.dao.TagDao
import com.mindshift.nexre.data.local.entity.LinkEntity
import com.mindshift.nexre.data.local.entity.LinkTagCrossRef
import com.mindshift.nexre.data.local.entity.TagEntity
import com.mindshift.nexre.data.local.entity.LinkWithTags
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.LinkStatus
import com.mindshift.nexre.domain.model.SourcePlatform
import com.mindshift.nexre.domain.model.SummarySource
import com.mindshift.nexre.domain.repository.LinkRepository
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkRepositoryImpl @Inject constructor(
    private val linkDao: LinkDao,
    private val tagDao: TagDao,
) : LinkRepository {

    override fun getHomeLinks() = linkDao.getHomeLinks().map { it.map(::toLink) }
    override fun getLinksByStatus(status: LinkStatus) =
        linkDao.getLinksByStatus(status.name).map { it.map(::toLink) }
    override fun getFavouriteLinks() = linkDao.getFavouriteLinks().map { it.map(::toLink) }
    override fun getAllLinks() = linkDao.getAllLinks().map { it.map(::toLink) }
    override fun getLinksSortedByOpened() = linkDao.getLinksSortedByOpened().map { it.map(::toLink) }
    override fun searchLinks(query: String) = linkDao.searchLinks(query).map { it.map(::toLink) }
    override fun getLinkById(id: String) = linkDao.getLinkById(id).map { it?.let(::toLink) }
    override fun getLinksByTag(tagName: String) = linkDao.getLinksByTag(tagName).map { it.map(::toLink) }

    override suspend fun saveLink(link: Link, tagSource: String) {
        linkDao.upsertLink(toEntity(link))
        linkDao.deleteLinkTags(link.id)
        val refs = link.tags.mapNotNull { tagName ->
            val inserted = tagDao.insertTag(TagEntity(name = tagName))
            val tagId = if (inserted != -1L) inserted.toInt() else tagDao.getTagIdByName(tagName)
            tagId?.let { LinkTagCrossRef(link.id, it, tagSource) }
        }
        if (refs.isNotEmpty()) linkDao.upsertLinkTagCrossRefs(refs)
    }

    override suspend fun updateStatus(id: String, status: LinkStatus) =
        linkDao.updateStatus(id, status.name)

    override suspend fun updateFavourite(id: String, isFavourite: Boolean) =
        linkDao.updateFavourite(id, isFavourite)

    override suspend fun recordOpen(id: String) =
        linkDao.recordOpen(id, Instant.now().toEpochMilli())

    override suspend fun addReadDuration(id: String, seconds: Int) =
        linkDao.addReadDuration(id, seconds)

    override suspend fun updateNote(id: String, note: String) =
        linkDao.updateNote(id, note)

    override suspend fun updateSummary(id: String, summary: String, summarySource: String) =
        linkDao.updateSummary(id, summary, summarySource)

    override suspend fun deleteLink(id: String) = linkDao.deleteLink(id)
    override suspend fun deleteArchivedLinks() = linkDao.deleteArchivedLinks()
    override suspend fun deleteAllLinks() = linkDao.deleteAllLinks()

    override suspend fun getWeeklyReadCounts(since: Long): List<Pair<String, Int>> =
        linkDao.getWeeklyReadCounts(since).map { it.day_of_week to it.count }

    private fun toLink(lwt: LinkWithTags) = Link(
        id = lwt.link.id,
        url = lwt.link.url,
        title = lwt.link.title,
        description = lwt.link.description,
        thumbnailUrl = lwt.link.thumbnailUrl,
        sourcePlatform = runCatching { SourcePlatform.valueOf(lwt.link.sourcePlatform) }
            .getOrDefault(SourcePlatform.WEB),
        status = runCatching { LinkStatus.valueOf(lwt.link.status) }.getOrDefault(LinkStatus.UNREAD),
        isFavourite = lwt.link.isFavourite,
        personalNote = lwt.link.personalNote,
        summary = lwt.link.summary,
        summarySource = runCatching { SummarySource.valueOf(lwt.link.summarySource) }
            .getOrDefault(SummarySource.NONE),
        tags = lwt.tags.map { it.name },
        savedAt = lwt.link.savedAt,
        openedAt = lwt.link.openedAt,
        readDurationSec = lwt.link.readDurationSec,
        readCount = lwt.link.readCount,
    )

    private fun toEntity(link: Link) = LinkEntity(
        id = link.id,
        url = link.url,
        title = link.title,
        description = link.description,
        thumbnailUrl = link.thumbnailUrl,
        sourcePlatform = link.sourcePlatform.name,
        status = link.status.name,
        isFavourite = link.isFavourite,
        personalNote = link.personalNote,
        summary = link.summary,
        summarySource = link.summarySource.name,
        savedAt = link.savedAt,
        openedAt = link.openedAt,
        readDurationSec = link.readDurationSec,
        readCount = link.readCount,
    )
}
