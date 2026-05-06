package com.mindshift.nexre.data.repository

import com.mindshift.nexre.data.local.dao.TagDao
import com.mindshift.nexre.data.local.entity.TagEntity
import com.mindshift.nexre.domain.model.Tag
import com.mindshift.nexre.domain.repository.TagRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl @Inject constructor(private val tagDao: TagDao) : TagRepository {

    override fun getTagsWithCounts() = tagDao.getTagsWithCounts().map { list ->
        list.map { Tag(it.id, it.name, it.total, it.unread) }
    }

    override suspend fun upsertTag(name: String): Int {
        val inserted = tagDao.insertTag(TagEntity(name = name))
        return if (inserted == -1L) tagDao.getTagIdByName(name) ?: 0
        else inserted.toInt()
    }

    override suspend fun renameTag(id: Int, newName: String) = tagDao.renameTag(id, newName)

    override suspend fun mergeTags(fromId: Int, toId: Int) {
        tagDao.reassignTagRefs(fromId, toId)
        tagDao.deleteTag(fromId)
    }

    override suspend fun deleteTag(id: Int) = tagDao.deleteTag(id)
    override suspend fun deleteUnusedTags() = tagDao.deleteUnusedTags()
}
