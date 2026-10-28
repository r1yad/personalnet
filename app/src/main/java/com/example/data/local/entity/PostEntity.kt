package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorPersonaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("authorPersonaId"),
        Index("createdAt")
    ]
)
data class PostEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val authorPersonaId: String,
    val textContent: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
