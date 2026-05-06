package com.mindshift.nexre.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindshift.nexre.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT id FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagIdByName(name: String): Int?

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("""
        SELECT t.id, t.name,
               COUNT(lt.link_id) as total,
               SUM(CASE WHEN l.status = 'UNREAD' THEN 1 ELSE 0 END) as unread
        FROM tags t
        LEFT JOIN link_tags lt ON t.id = lt.tag_id
        LEFT JOIN links l ON lt.link_id = l.id AND l.status != 'ARCHIVED'
        GROUP BY t.id
        ORDER BY unread DESC, total DESC
    """)
    fun getTagsWithCounts(): Flow<List<TagWithCount>>

    @Query("UPDATE tags SET name = :newName WHERE id = :id")
    suspend fun renameTag(id: Int, newName: String)

    @Query("UPDATE link_tags SET tag_id = :toId WHERE tag_id = :fromId")
    suspend fun reassignTagRefs(fromId: Int, toId: Int)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTag(id: Int)

    @Query("DELETE FROM tags WHERE id NOT IN (SELECT DISTINCT tag_id FROM link_tags)")
    suspend fun deleteUnusedTags()
}

data class TagWithCount(val id: Int, val name: String, val total: Int, val unread: Int)
