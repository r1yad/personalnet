package com.example.data.repository

import com.example.data.local.dao.PersonaDao
import com.example.data.local.entity.PersonaEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class PersonaRepository(private val personaDao: PersonaDao) {
    val allPersonas: Flow<List<PersonaEntity>> = personaDao.getAllPersonas()

    fun getPersona(id: String): Flow<PersonaEntity?> = personaDao.getPersonaById(id)

    suspend fun getPersonaDirect(id: String): PersonaEntity? = personaDao.getPersonaByIdDirect(id)

    suspend fun getPersonaCountDirect(): Int = personaDao.getPersonaCountDirect()

    fun getReceivedLikes(personaId: String): Flow<Int> = personaDao.getReceivedLikesCount(personaId)

    fun getPostCount(personaId: String): Flow<Int> = personaDao.getPostCountForPersona(personaId)

    fun searchPersonas(query: String): Flow<List<PersonaEntity>> = personaDao.searchPersonas(query)

    suspend fun insertOrUpdate(persona: PersonaEntity) {
        val existing = personaDao.getPersonaByIdDirect(persona.id)
        if (existing == null) {
            personaDao.insertPersona(persona)
        } else {
            personaDao.updatePersona(persona)
        }
    }

    suspend fun duplicatePersona(original: PersonaEntity): PersonaEntity {
        var newUsername = "${original.username}_copy"
        var counter = 1
        while (personaDao.getPersonaByUsername(newUsername) != null) {
            newUsername = "${original.username}_copy$counter"
            counter++
        }

        val clone = original.copy(
            id = UUID.randomUUID().toString(),
            displayName = "${original.displayName} (Copy)",
            username = newUsername,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        personaDao.insertPersona(clone)
        return clone
    }

    suspend fun deletePersona(id: String) {
        personaDao.deletePersonaById(id)
    }
}
