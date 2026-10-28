package com.example.data.local.model

import com.example.data.local.entity.PersonaEntity
import com.example.data.local.entity.PostEntity
import com.example.data.local.entity.PostMediaEntity

data class PostWithDetails(
    val post: PostEntity,
    val author: PersonaEntity,
    val media: List<PostMediaEntity> = emptyList(),
    val likeCount: Int = 0,
    val isLikedByActivePersona: Boolean = false,
    val commentCount: Int = 0
)
