package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "comment_likes",
    primaryKeys = ["commentId", "personaId"],
    foreignKeys = [
        ForeignKey(
            entity = CommentEntity::class,
            parentColumns = ["id"],
            childColumns = ["commentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["personaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("commentId"),
        Index("personaId")
    ]
)
data class CommentLikeEntity(
    val commentId: String,
    val personaId: String,
    val createdAt: Long = System.currentTimeMillis()
)
