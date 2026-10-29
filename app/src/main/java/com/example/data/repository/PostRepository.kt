package com.example.data.repository

import android.net.Uri
import com.example.data.local.dao.CommentDao
import com.example.data.local.dao.LikeDao
import com.example.data.local.dao.PersonaDao
import com.example.data.local.dao.PostDao
import com.example.data.local.entity.PostEntity
import com.example.data.local.entity.PostLikeEntity
import com.example.data.local.entity.PostMediaEntity
import com.example.data.local.model.PostWithDetails
import com.example.data.media.MediaManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID

class PostRepository(
    private val postDao: PostDao,
    private val personaDao: PersonaDao,
    private val likeDao: LikeDao,
    private val commentDao: CommentDao,
    private val mediaManager: MediaManager
) {
    fun getFeedPosts(activePersonaId: String?): Flow<List<PostWithDetails>> {
        return combine(
            postDao.getAllPosts(),
            personaDao.getAllPersonas(),
            postDao.getAllPostMediaFlow(),
            likeDao.getAllPostLikesFlow(),
            commentDao.getAllCommentCountsFlow()
        ) { posts, personas, allMedia, allLikes, commentCounts ->
            val personaMap = personas.associateBy { it.id }
            val mediaMap = allMedia.groupBy { it.postId }
            val likesMap = allLikes.groupBy { it.postId }
            val commentMap = commentCounts.associate { it.postId to it.count }

            posts.mapNotNull { post ->
                val author = personaMap[post.authorPersonaId] ?: return@mapNotNull null
                val media = mediaMap[post.id]?.sortedBy { it.orderIndex } ?: emptyList()
                val postLikes = likesMap[post.id] ?: emptyList()
                val likeCount = postLikes.size
                val isLiked = if (activePersonaId != null) {
                    postLikes.any { it.personaId == activePersonaId }
                } else false
                val commentCount = commentMap[post.id] ?: 0

                PostWithDetails(
                    post = post,
                    author = author,
                    media = media,
                    likeCount = likeCount,
                    isLikedByActivePersona = isLiked,
                    commentCount = commentCount
                )
            }
        }
    }

    fun getPostsByPersona(personaId: String, activePersonaId: String?): Flow<List<PostWithDetails>> {
        return combine(
            postDao.getPostsByPersona(personaId),
            personaDao.getAllPersonas(),
            postDao.getAllPostMediaFlow(),
            likeDao.getAllPostLikesFlow(),
            commentDao.getAllCommentCountsFlow()
        ) { posts, personas, allMedia, allLikes, commentCounts ->
            val personaMap = personas.associateBy { it.id }
            val mediaMap = allMedia.groupBy { it.postId }
            val likesMap = allLikes.groupBy { it.postId }
            val commentMap = commentCounts.associate { it.postId to it.count }

            posts.mapNotNull { post ->
                val author = personaMap[post.authorPersonaId] ?: return@mapNotNull null
                val media = mediaMap[post.id]?.sortedBy { it.orderIndex } ?: emptyList()
                val postLikes = likesMap[post.id] ?: emptyList()
                val likeCount = postLikes.size
                val isLiked = if (activePersonaId != null) {
                    postLikes.any { it.personaId == activePersonaId }
                } else false
                val commentCount = commentMap[post.id] ?: 0

                PostWithDetails(
                    post = post,
                    author = author,
                    media = media,
                    likeCount = likeCount,
                    isLikedByActivePersona = isLiked,
                    commentCount = commentCount
                )
            }
        }
    }

    fun getPostDetails(postId: String, activePersonaId: String?): Flow<PostWithDetails?> {
        return combine(
            postDao.getPostById(postId),
            personaDao.getAllPersonas(),
            postDao.getAllPostMediaFlow(),
            likeDao.getAllPostLikesFlow(),
            commentDao.getAllCommentCountsFlow()
        ) { post, personas, allMedia, allLikes, commentCounts ->
            if (post == null) return@combine null
            val personaMap = personas.associateBy { it.id }
            val author = personaMap[post.authorPersonaId] ?: return@combine null
            val media = allMedia.filter { it.postId == post.id }.sortedBy { it.orderIndex }
            val postLikes = allLikes.filter { it.postId == post.id }
            val likeCount = postLikes.size
            val isLiked = if (activePersonaId != null) {
                postLikes.any { it.personaId == activePersonaId }
            } else false
            val commentMap = commentCounts.associate { it.postId to it.count }
            val commentCount = commentMap[post.id] ?: 0

            PostWithDetails(
                post = post,
                author = author,
                media = media,
                likeCount = likeCount,
                isLikedByActivePersona = isLiked,
                commentCount = commentCount
            )
        }
    }

    suspend fun createPost(
        authorPersonaId: String,
        textContent: String,
        mediaUris: List<Pair<Uri, String?>>
    ): String {
        val postId = UUID.randomUUID().toString()

        // 1. Ingest & compress all media first to prevent empty-media UI race conditions
        val mediaEntities = mutableListOf<PostMediaEntity>()
        mediaUris.forEachIndexed { index, (uri, mimeType) ->
            val asset = mediaManager.importMedia(uri, mimeType)
            if (asset != null) {
                mediaEntities.add(
                    PostMediaEntity(
                        id = UUID.randomUUID().toString(),
                        postId = postId,
                        mediaPath = asset.relativePath,
                        mimeType = asset.mimeType,
                        orderIndex = index
                    )
                )
            }
        }

        // 2. Insert post entity
        val post = PostEntity(
            id = postId,
            authorPersonaId = authorPersonaId,
            textContent = textContent,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        postDao.insertPost(post)

        // 3. Insert post media records
        if (mediaEntities.isNotEmpty()) {
            postDao.insertPostMedia(mediaEntities)
        }
        return postId
    }

    suspend fun updatePost(postId: String, newText: String) {
        val post = postDao.getPostByIdDirect(postId) ?: return
        postDao.updatePost(post.copy(textContent = newText, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePost(postId: String) {
        postDao.deletePostById(postId)
    }

    suspend fun toggleLike(postId: String, personaId: String) {
        val isLiked = likeDao.isPostLikedByPersonaDirect(postId, personaId)
        if (isLiked) {
            likeDao.deletePostLike(postId, personaId)
        } else {
            likeDao.insertPostLike(PostLikeEntity(postId = postId, personaId = personaId))
        }
    }
}
