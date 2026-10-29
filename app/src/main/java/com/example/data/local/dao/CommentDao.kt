package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CommentEntity
import kotlinx.coroutines.flow.Flow

data class PostCommentCount(
    val postId: String,
    val count: Int
)

@Dao
interface CommentDao {
    @Query("SELECT postId, COUNT(*) as count FROM comments GROUP BY postId")
    fun getAllCommentCountsFlow(): Flow<List<PostCommentCount>>

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    suspend fun getCommentsForPostDirect(postId: String): List<CommentEntity>

    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    fun getCommentCountForPost(postId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    suspend fun getCommentCountForPostDirect(postId: String): Int

    @Query("SELECT * FROM comments WHERE id = :id LIMIT 1")
    suspend fun getCommentByIdDirect(id: String): CommentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    @Update
    suspend fun updateComment(comment: CommentEntity)

    @Delete
    suspend fun deleteComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :id")
    suspend fun deleteCommentById(id: String)

    @Query("SELECT * FROM comments")
    suspend fun getAllCommentsDirect(): List<CommentEntity>
}
