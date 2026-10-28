package com.example.data.local.model

import com.example.data.local.entity.CommentEntity
import com.example.data.local.entity.PersonaEntity

data class CommentWithDetails(
    val comment: CommentEntity,
    val author: PersonaEntity,
    val likeCount: Int = 0,
    val isLikedByActivePersona: Boolean = false,
    val replyToAuthorName: String? = null,
    val replies: List<CommentWithDetails> = emptyList()
)
