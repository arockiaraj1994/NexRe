package com.mindshift.nexre.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "links")
data class LinkEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String,
    @ColumnInfo(name = "source_platform") val sourcePlatform: String,
    val status: String,
    @ColumnInfo(name = "is_favourite") val isFavourite: Boolean,
    @ColumnInfo(name = "personal_note") val personalNote: String,
    val summary: String,
    @ColumnInfo(name = "summary_source") val summarySource: String,
    @ColumnInfo(name = "saved_at") val savedAt: Long,
    @ColumnInfo(name = "opened_at") val openedAt: Long,
    @ColumnInfo(name = "read_duration_sec") val readDurationSec: Int,
    @ColumnInfo(name = "read_count") val readCount: Int,
    @ColumnInfo(name = "word_count") val wordCount: Int = 0,
)
