package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "post_media",
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("postId")
    ]
)
data class PostMediaEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val mediaPath: String, // Relative path inside app files/media
    val mimeType: String, // e.g. "image/webp", "video/mp4"
    val orderIndex: Int = 0
)
