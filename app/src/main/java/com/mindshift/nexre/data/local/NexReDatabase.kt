package com.mindshift.nexre.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mindshift.nexre.data.local.dao.LinkDao
import com.mindshift.nexre.data.local.dao.TagDao
import com.mindshift.nexre.data.local.entity.LinkEntity
import com.mindshift.nexre.data.local.entity.LinkTagCrossRef
import com.mindshift.nexre.data.local.entity.TagEntity

@Database(
    entities = [LinkEntity::class, TagEntity::class, LinkTagCrossRef::class],
    version = 1,
    exportSchema = false,
)
abstract class NexReDatabase : RoomDatabase() {
    abstract fun linkDao(): LinkDao
    abstract fun tagDao(): TagDao
}
