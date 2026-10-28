package com.example.data.repository

import android.net.Uri
import com.example.data.local.dao.ConversationDao
import com.example.data.local.dao.PersonaDao
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.model.ConversationWithDetails
import com.example.data.local.model.MessageWithDetails
import com.example.data.media.MediaManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID

class ConversationRepository(
    private val conversationDao: ConversationDao,
    private val personaDao: PersonaDao,
    private val mediaManager: MediaManager
) {
    fun getConversations(activePersonaId: String): Flow<List<ConversationWithDetails>> {
        return combine(
            conversationDao.getConversationsForPersona(activePersonaId),
            personaDao.getAllPersonas()
        ) { conversations, personas ->
            val personaMap = personas.associateBy { it.id }
            conversations.mapNotNull { conv ->
                val otherId = if (conv.personaAId == activePersonaId) conv.personaBId else conv.personaAId
                val otherPersona = personaMap[otherId] ?: return@mapNotNull null
                val lastMsg = conversationDao.getLastMessageDirect(conv.id)

                ConversationWithDetails(
                    conversation = conv,
                    otherPersona = otherPersona,
                    lastMessage = lastMsg,
                    unreadCount = 0
                )
            }
        }
    }

    suspend fun getOrCreateConversation(p1: String, p2: String): String {
        val existing = conversationDao.getConversationBetween(p1, p2)
        if (existing != null) return existing.id

        val id = UUID.randomUUID().toString()
        val newConv = ConversationEntity(
            id = id,
            personaAId = p1,
            personaBId = p2,
            createdAt = System.currentTimeMillis(),
            lastActivityAt = System.currentTimeMillis(),
            lastMessagePreview = ""
        )
        conversationDao.insertConversation(newConv)
        return id
    }

    fun getMessages(conversationId: String): Flow<List<MessageWithDetails>> {
        return combine(
            conversationDao.getMessagesForConversation(conversationId),
            personaDao.getAllPersonas()
        ) { messages, personas ->
            val personaMap = personas.associateBy { it.id }
            val messageMap = messages.associateBy { it.id }

            messages.mapNotNull { msg ->
                val sender = personaMap[msg.senderPersonaId] ?: return@mapNotNull null
                val replyToMsg = msg.replyToMessageId?.let { messageMap[it] }
                val replyToSender = replyToMsg?.let { personaMap[it.senderPersonaId]?.displayName }

                MessageWithDetails(
                    message = msg,
                    sender = sender,
                    replyToText = replyToMsg?.textContent,
                    replyToSenderName = replyToSender
                )
            }
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        senderPersonaId: String,
        textContent: String,
        mediaUri: Uri? = null,
        mimeType: String? = null,
        replyToId: String? = null
    ): String {
        var mediaPath: String? = null
        var resolvedMime: String? = null

        if (mediaUri != null) {
            val asset = mediaManager.importMedia(mediaUri, mimeType)
            if (asset != null) {
                mediaPath = asset.relativePath
                resolvedMime = asset.mimeType
            }
        }

        val msgId = UUID.randomUUID().toString()
        val message = MessageEntity(
            id = msgId,
            conversationId = conversationId,
            senderPersonaId = senderPersonaId,
            replyToMessageId = replyToId,
            textContent = textContent,
            mediaPath = mediaPath,
            mimeType = resolvedMime,
            createdAt = System.currentTimeMillis(),
            isEdited = false,
            isRead = false
        )
        conversationDao.insertMessage(message)

        val conv = conversationDao.getConversationByIdDirect(conversationId)
        if (conv != null) {
            val preview = if (textContent.isNotBlank()) textContent else if (mediaPath != null) "[Media]" else ""
            conversationDao.updateConversation(
                conv.copy(
                    lastActivityAt = System.currentTimeMillis(),
                    lastMessagePreview = preview
                )
            )
        }

        return msgId
    }

    suspend fun editMessage(messageId: String, newText: String) {
        val msg = conversationDao.getMessageByIdDirect(messageId) ?: return
        conversationDao.updateMessage(
            msg.copy(textContent = newText, isEdited = true)
        )
    }

    suspend fun deleteMessage(messageId: String) {
        conversationDao.deleteMessageById(messageId)
    }

    suspend fun markAsRead(conversationId: String, currentPersonaId: String) {
        conversationDao.markMessagesAsRead(conversationId, currentPersonaId)
    }

    suspend fun deleteConversation(conversationId: String) {
        conversationDao.deleteConversationById(conversationId)
    }
}
