package com.mindshift.nexre.domain.repository

import com.mindshift.nexre.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getTagsWithCounts(): Flow<List<Tag>>
    suspend fun upsertTag(name: String): Int
    suspend fun renameTag(id: Int, newName: String)
    suspend fun mergeTags(fromId: Int, toId: Int)
    suspend fun deleteTag(id: Int)
    suspend fun deleteUnusedTags()
}
