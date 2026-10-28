package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "post_likes",
    primaryKeys = ["postId", "personaId"],
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
            childColumns = ["personaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("postId"),
        Index("personaId")
    ]
)
data class PostLikeEntity(
    val postId: String,
    val personaId: String,
    val createdAt: Long = System.currentTimeMillis()
)
