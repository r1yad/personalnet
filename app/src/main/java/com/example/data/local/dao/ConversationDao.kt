package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("""
        SELECT * FROM conversations 
        WHERE personaAId = :personaId OR personaBId = :personaId 
        ORDER BY lastActivityAt DESC
    """)
    fun getConversationsForPersona(personaId: String): Flow<List<ConversationEntity>>

    @Query("""
        SELECT * FROM conversations 
        WHERE (personaAId = :p1 AND personaBId = :p2) 
           OR (personaAId = :p2 AND personaBId = :p1) 
        LIMIT 1
    """)
    suspend fun getConversationBetween(p1: String, p2: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    fun getConversationById(id: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationByIdDirect(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: String)

    @Query("SELECT * FROM conversations")
    suspend fun getAllConversationsDirect(): List<ConversationEntity>

    // Messages
    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY createdAt ASC")
    fun getMessagesForConversation(convId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY createdAt DESC LIMIT 1")
    fun getLastMessageForConversation(convId: String): Flow<MessageEntity?>

    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastMessageDirect(convId: String): MessageEntity?

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :convId AND senderPersonaId != :currentPersonaId AND isRead = 0")
    fun getUnreadCount(convId: String, currentPersonaId: String): Flow<Int>

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageByIdDirect(id: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)

    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :convId AND senderPersonaId != :currentPersonaId")
    suspend fun markMessagesAsRead(convId: String, currentPersonaId: String)

    @Query("SELECT * FROM messages")
    suspend fun getAllMessagesDirect(): List<MessageEntity>
}
