package com.mindshift.nexre.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mindshift.nexre.data.local.dao.LinkDao
import com.mindshift.nexre.data.local.dao.TagDao
import com.mindshift.nexre.data.local.entity.LinkEntity
import com.mindshift.nexre.data.local.entity.LinkTagCrossRef
import com.mindshift.nexre.data.local.entity.TagEntity

@Database(
    entities = [LinkEntity::class, TagEntity::class, LinkTagCrossRef::class],
    version = 2,
    exportSchema = false,
)
abstract class NexReDatabase : RoomDatabase() {
    abstract fun linkDao(): LinkDao
    abstract fun tagDao(): TagDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE links ADD COLUMN word_count INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
