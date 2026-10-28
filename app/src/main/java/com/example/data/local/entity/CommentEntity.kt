package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorPersonaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("postId"),
        Index("parentCommentId"),
        Index("authorPersonaId"),
        Index("createdAt")
    ]
)
data class CommentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val parentCommentId: String? = null,
    val authorPersonaId: String,
    val textContent: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
