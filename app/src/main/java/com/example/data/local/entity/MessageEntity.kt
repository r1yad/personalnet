package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["senderPersonaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("conversationId"),
        Index("senderPersonaId"),
        Index("createdAt")
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val senderPersonaId: String,
    val replyToMessageId: String? = null,
    val textContent: String,
    val mediaPath: String? = null,
    val mimeType: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isEdited: Boolean = false,
    val isRead: Boolean = false
)
