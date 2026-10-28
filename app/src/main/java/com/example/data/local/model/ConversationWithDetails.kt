package com.example.data.local.model

import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PersonaEntity

data class ConversationWithDetails(
    val conversation: ConversationEntity,
    val otherPersona: PersonaEntity,
    val lastMessage: MessageEntity? = null,
    val unreadCount: Int = 0
)

data class MessageWithDetails(
    val message: MessageEntity,
    val sender: PersonaEntity,
    val replyToText: String? = null,
    val replyToSenderName: String? = null
)
