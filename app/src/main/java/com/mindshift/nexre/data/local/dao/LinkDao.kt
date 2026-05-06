package com.mindshift.nexre.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mindshift.nexre.data.local.entity.LinkEntity
import com.mindshift.nexre.data.local.entity.LinkTagCrossRef
import com.mindshift.nexre.data.local.entity.LinkWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLink(link: LinkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLinkTagCrossRefs(refs: List<LinkTagCrossRef>)

    @Query("DELETE FROM link_tags WHERE link_id = :linkId")
    suspend fun deleteLinkTags(linkId: String)

    @Transaction
    @Query("SELECT * FROM links WHERE status != 'ARCHIVED' ORDER BY saved_at DESC")
    fun getHomeLinks(): Flow<List<LinkWithTags>>

    @Transaction
    @Query("SELECT * FROM links WHERE status = :status ORDER BY saved_at DESC")
    fun getLinksByStatus(status: String): Flow<List<LinkWithTags>>

    @Transaction
    @Query("SELECT * FROM links WHERE is_favourite = 1 AND status != 'ARCHIVED' ORDER BY saved_at DESC")
    fun getFavouriteLinks(): Flow<List<LinkWithTags>>

    @Transaction
    @Query("SELECT * FROM links ORDER BY saved_at DESC")
    fun getAllLinks(): Flow<List<LinkWithTags>>

    @Transaction
    @Query("SELECT * FROM links WHERE status != 'ARCHIVED' ORDER BY opened_at DESC")
    fun getLinksSortedByOpened(): Flow<List<LinkWithTags>>

    @Transaction
    @Query("""
        SELECT DISTINCT l.* FROM links l
        LEFT JOIN link_tags lt ON l.id = lt.link_id
        LEFT JOIN tags t ON lt.tag_id = t.id
        WHERE l.title LIKE '%' || :query || '%'
           OR l.description LIKE '%' || :query || '%'
           OR l.personal_note LIKE '%' || :query || '%'
           OR l.url LIKE '%' || :query || '%'
           OR t.name LIKE '%' || :query || '%'
        ORDER BY l.saved_at DESC
    """)
    fun searchLinks(query: String): Flow<List<LinkWithTags>>

    @Transaction
    @Query("SELECT * FROM links WHERE id = :id")
    fun getLinkById(id: String): Flow<LinkWithTags?>

    @Query("UPDATE links SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE links SET is_favourite = :isFavourite WHERE id = :id")
    suspend fun updateFavourite(id: String, isFavourite: Boolean)

    @Query("UPDATE links SET opened_at = :openedAt, read_count = read_count + 1 WHERE id = :id")
    suspend fun recordOpen(id: String, openedAt: Long)

    @Query("UPDATE links SET read_duration_sec = read_duration_sec + :seconds WHERE id = :id")
    suspend fun addReadDuration(id: String, seconds: Int)

    @Query("UPDATE links SET personal_note = :note WHERE id = :id")
    suspend fun updateNote(id: String, note: String)

    @Query("UPDATE links SET summary = :summary, summary_source = :summarySource WHERE id = :id")
    suspend fun updateSummary(id: String, summary: String, summarySource: String)

    @Query("DELETE FROM links WHERE id = :id")
    suspend fun deleteLink(id: String)

    @Query("DELETE FROM links WHERE status = 'ARCHIVED'")
    suspend fun deleteArchivedLinks()

    @Query("DELETE FROM links")
    suspend fun deleteAllLinks()

    @Query("""
        SELECT strftime('%w', datetime(saved_at / 1000, 'unixepoch')) as day_of_week,
               COUNT(*) as count
        FROM links
        WHERE status = 'READ'
          AND saved_at >= :since
        GROUP BY day_of_week
    """)
    suspend fun getWeeklyReadCounts(since: Long): List<DayCount>

    @Transaction
    @Query("""
        SELECT l.* FROM links l
        JOIN link_tags lt ON l.id = lt.link_id
        JOIN tags t ON lt.tag_id = t.id
        WHERE t.name = :tagName AND l.status != 'ARCHIVED'
        ORDER BY l.saved_at DESC
    """)
    fun getLinksByTag(tagName: String): Flow<List<LinkWithTags>>
}

data class DayCount(val day_of_week: String, val count: Int)
