package com.example.data.local.model

import androidx.compose.runtime.Immutable
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PersonaEntity

@Immutable
data class ConversationWithDetails(
    val conversation: ConversationEntity,
    val otherPersona: PersonaEntity,
    val lastMessage: MessageEntity? = null,
    val unreadCount: Int = 0
)

@Immutable
data class MessageWithDetails(
    val message: MessageEntity,
    val sender: PersonaEntity,
    val replyToText: String? = null,
    val replyToSenderName: String? = null
)
