package com.example.data.repository

import com.example.data.local.dao.CommentDao
import com.example.data.local.dao.LikeDao
import com.example.data.local.dao.PersonaDao
import com.example.data.local.entity.CommentEntity
import com.example.data.local.entity.CommentLikeEntity
import com.example.data.local.model.CommentWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID

class CommentRepository(
    private val commentDao: CommentDao,
    private val personaDao: PersonaDao,
    private val likeDao: LikeDao
) {
    fun getCommentsTreeForPost(postId: String, activePersonaId: String?): Flow<List<CommentWithDetails>> {
        return combine(
            commentDao.getCommentsForPost(postId),
            personaDao.getAllPersonas()
        ) { rawComments, personas ->
            val personaMap = personas.associateBy { it.id }
            val rawCommentMap = rawComments.associateBy { it.id }

            val allDetails = rawComments.mapNotNull { comment ->
                val author = personaMap[comment.authorPersonaId] ?: return@mapNotNull null
                val isLiked = if (activePersonaId != null) {
                    likeDao.isCommentLikedByPersonaDirect(comment.id, activePersonaId)
                } else false
                val likeCount = likeDao.getCommentLikeCountDirect(comment.id)
                val replyToAuthor = comment.parentCommentId?.let { parentId ->
                    rawCommentMap[parentId]?.let { parent ->
                        personaMap[parent.authorPersonaId]?.displayName
                    }
                }

                CommentWithDetails(
                    comment = comment,
                    author = author,
                    likeCount = likeCount,
                    isLikedByActivePersona = isLiked,
                    replyToAuthorName = replyToAuthor,
                    replies = emptyList()
                )
            }

            // Group into hierarchical tree
            val byParent = allDetails.groupBy { it.comment.parentCommentId }

            fun buildTree(parentId: String?): List<CommentWithDetails> {
                val children = byParent[parentId] ?: return emptyList()
                return children.map { node ->
                    node.copy(replies = buildTree(node.comment.id))
                }
            }

            buildTree(null)
        }
    }

    suspend fun addComment(
        postId: String,
        authorPersonaId: String,
        textContent: String,
        parentCommentId: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val comment = CommentEntity(
            id = id,
            postId = postId,
            parentCommentId = parentCommentId,
            authorPersonaId = authorPersonaId,
            textContent = textContent,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        commentDao.insertComment(comment)
        return id
    }

    suspend fun updateComment(commentId: String, newText: String) {
        val comment = commentDao.getCommentByIdDirect(commentId) ?: return
        commentDao.updateComment(comment.copy(textContent = newText, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteComment(commentId: String) {
        commentDao.deleteCommentById(commentId)
    }

    suspend fun toggleCommentLike(commentId: String, personaId: String) {
        val isLiked = likeDao.isCommentLikedByPersonaDirect(commentId, personaId)
        if (isLiked) {
            likeDao.deleteCommentLike(commentId, personaId)
        } else {
            likeDao.insertCommentLike(CommentLikeEntity(commentId = commentId, personaId = personaId))
        }
    }
}
