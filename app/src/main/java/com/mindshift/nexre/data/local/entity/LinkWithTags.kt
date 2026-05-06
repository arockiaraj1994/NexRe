package com.mindshift.nexre.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class LinkWithTags(
    @Embedded val link: LinkEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = LinkTagCrossRef::class,
            parentColumn = "link_id",
            entityColumn = "tag_id",
        ),
    )
    val tags: List<TagEntity>,
)
