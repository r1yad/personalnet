package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PersonaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonaDao {
    @Query("SELECT * FROM personas ORDER BY createdAt ASC")
    fun getAllPersonas(): Flow<List<PersonaEntity>>

    @Query("SELECT * FROM personas WHERE id = :id LIMIT 1")
    fun getPersonaById(id: String): Flow<PersonaEntity?>

    @Query("SELECT * FROM personas WHERE id = :id LIMIT 1")
    suspend fun getPersonaByIdDirect(id: String): PersonaEntity?

    @Query("SELECT * FROM personas WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getPersonaByUsername(username: String): PersonaEntity?

    @Query("SELECT * FROM personas WHERE displayName LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' ORDER BY displayName ASC")
    fun searchPersonas(query: String): Flow<List<PersonaEntity>>

    @Query("SELECT COUNT(*) FROM personas")
    fun getPersonaCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM personas")
    suspend fun getPersonaCountDirect(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersona(persona: PersonaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonas(personas: List<PersonaEntity>)

    @Update
    suspend fun updatePersona(persona: PersonaEntity)

    @Delete
    suspend fun deletePersona(persona: PersonaEntity)

    @Query("DELETE FROM personas WHERE id = :id")
    suspend fun deletePersonaById(id: String)

    @Query("SELECT COUNT(*) FROM post_likes WHERE postId IN (SELECT id FROM posts WHERE authorPersonaId = :personaId)")
    fun getReceivedLikesCount(personaId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM posts WHERE authorPersonaId = :personaId")
    fun getPostCountForPersona(personaId: String): Flow<Int>

    @Query("SELECT * FROM personas")
    suspend fun getAllPersonasDirect(): List<PersonaEntity>
}
