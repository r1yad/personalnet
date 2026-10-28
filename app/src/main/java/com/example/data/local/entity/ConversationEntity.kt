package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "conversations",
    foreignKeys = [
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["personaAId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["personaBId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("personaAId"),
        Index("personaBId"),
        Index(value = ["personaAId", "personaBId"], unique = true),
        Index("lastActivityAt")
    ]
)
data class ConversationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val personaAId: String,
    val personaBId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActivityAt: Long = System.currentTimeMillis(),
    val lastMessagePreview: String = ""
)
