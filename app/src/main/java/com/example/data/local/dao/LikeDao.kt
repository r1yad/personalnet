package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CommentLikeEntity
import com.example.data.local.entity.PostLikeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LikeDao {
    // Post Likes
    @Query("SELECT COUNT(*) FROM post_likes WHERE postId = :postId")
    fun getPostLikeCount(postId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM post_likes WHERE postId = :postId")
    suspend fun getPostLikeCountDirect(postId: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM post_likes WHERE postId = :postId AND personaId = :personaId)")
    fun isPostLikedByPersona(postId: String, personaId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM post_likes WHERE postId = :postId AND personaId = :personaId)")
    suspend fun isPostLikedByPersonaDirect(postId: String, personaId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPostLike(postLike: PostLikeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPostLikes(postLikes: List<PostLikeEntity>)

    @Query("DELETE FROM post_likes WHERE postId = :postId AND personaId = :personaId")
    suspend fun deletePostLike(postId: String, personaId: String)

    @Query("SELECT * FROM post_likes")
    suspend fun getAllPostLikesDirect(): List<PostLikeEntity>

    // Comment Likes
    @Query("SELECT COUNT(*) FROM comment_likes WHERE commentId = :commentId")
    fun getCommentLikeCount(commentId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM comment_likes WHERE commentId = :commentId")
    suspend fun getCommentLikeCountDirect(commentId: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM comment_likes WHERE commentId = :commentId AND personaId = :personaId)")
    fun isCommentLikedByPersona(commentId: String, personaId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM comment_likes WHERE commentId = :commentId AND personaId = :personaId)")
    suspend fun isCommentLikedByPersonaDirect(commentId: String, personaId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCommentLike(commentLike: CommentLikeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCommentLikes(commentLikes: List<CommentLikeEntity>)

    @Query("DELETE FROM comment_likes WHERE commentId = :commentId AND personaId = :personaId")
    suspend fun deleteCommentLike(commentId: String, personaId: String)

    @Query("SELECT * FROM comment_likes")
    suspend fun getAllCommentLikesDirect(): List<CommentLikeEntity>
}
