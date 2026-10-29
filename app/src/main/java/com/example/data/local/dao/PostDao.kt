package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PostEntity
import com.example.data.local.entity.PostMediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    suspend fun getAllPostsDirect(): List<PostEntity>

    @Query("SELECT * FROM posts WHERE authorPersonaId = :personaId ORDER BY createdAt DESC")
    fun getPostsByPersona(personaId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    fun getPostById(id: String): Flow<PostEntity?>

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun getPostByIdDirect(id: String): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deletePostById(id: String)

    // Media
    @Query("SELECT * FROM post_media WHERE postId = :postId ORDER BY orderIndex ASC")
    fun getMediaForPost(postId: String): Flow<List<PostMediaEntity>>

    @Query("SELECT * FROM post_media WHERE postId = :postId ORDER BY orderIndex ASC")
    suspend fun getMediaForPostDirect(postId: String): List<PostMediaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostMedia(media: List<PostMediaEntity>)

    @Query("DELETE FROM post_media WHERE postId = :postId")
    suspend fun deleteMediaForPost(postId: String)

    @Query("SELECT * FROM post_media")
    suspend fun getAllPostMediaDirect(): List<PostMediaEntity>

    @Query("SELECT * FROM post_media")
    fun getAllPostMediaFlow(): Flow<List<PostMediaEntity>>
}
