package com.mindshift.nexre.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "link_tags",
    primaryKeys = ["link_id", "tag_id"],
    foreignKeys = [
        ForeignKey(entity = LinkEntity::class, parentColumns = ["id"], childColumns = ["link_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tag_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("link_id"), Index("tag_id")],
)
data class LinkTagCrossRef(
    @ColumnInfo(name = "link_id") val linkId: String,
    @ColumnInfo(name = "tag_id") val tagId: Int,
    val source: String,
)
