package com.mindshift.nexre.domain.repository

import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.LinkStatus
import kotlinx.coroutines.flow.Flow

interface LinkRepository {
    fun getHomeLinks(): Flow<List<Link>>
    fun getLinksByStatus(status: LinkStatus): Flow<List<Link>>
    fun getFavouriteLinks(): Flow<List<Link>>
    fun getAllLinks(): Flow<List<Link>>
    fun getLinksSortedByOpened(): Flow<List<Link>>
    fun getAllLinksOldestFirst(): Flow<List<Link>>
    fun getAllLinksMostOpened(): Flow<List<Link>>
    fun searchLinks(query: String): Flow<List<Link>>
    fun getLinkById(id: String): Flow<Link?>
    suspend fun getLinkByUrl(url: String): Link?
    fun getLinksByTag(tagName: String): Flow<List<Link>>
    suspend fun saveLink(link: Link, tagSource: String = "KEYWORD")
    suspend fun updateStatus(id: String, status: LinkStatus)
    suspend fun updateFavourite(id: String, isFavourite: Boolean)
    suspend fun recordOpen(id: String)
    suspend fun addReadDuration(id: String, seconds: Int)
    suspend fun updateNote(id: String, note: String)
    suspend fun updateSummary(id: String, summary: String, summarySource: String)
    suspend fun deleteLink(id: String)
    suspend fun deleteArchivedLinks()
    suspend fun deleteAllLinks()
    suspend fun getWeeklyReadCounts(since: Long): List<Pair<String, Int>>
}
