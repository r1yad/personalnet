package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "personas",
    indices = [Index(value = ["username"], unique = true)]
)
data class PersonaEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val displayName: String,
    val username: String, // e.g. "cyber_bard"
    val bio: String = "",
    val avatarPath: String? = null,
    val coverPath: String? = null,
    val personalityTraits: String = "", // e.g. "Witty, Cautious, Dramatic"
    val speakingStyle: String = "", // e.g. "Uses archaic slang, speaks in riddles"
    val roleplayInstructions: String = "", // e.g. "Never breaks character; loves tea"
    val backgroundStory: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
