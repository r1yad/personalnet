package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "personanet_prefs")

class AppPreferences(private val context: Context) {
    companion object {
        private val KEY_ACTIVE_PERSONA_ID = stringPreferencesKey("active_persona_id")
        private val KEY_MEDIA_QUALITY = stringPreferencesKey("media_quality")
    }

    val activePersonaId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_PERSONA_ID]
    }

    suspend fun setActivePersonaId(personaId: String?) {
        context.dataStore.edit { prefs ->
            if (personaId != null) {
                prefs[KEY_ACTIVE_PERSONA_ID] = personaId
            } else {
                prefs.remove(KEY_ACTIVE_PERSONA_ID)
            }
        }
    }

    val mediaQuality: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_MEDIA_QUALITY] ?: "STANDARD"
    }

    suspend fun setMediaQuality(quality: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MEDIA_QUALITY] = quality
        }
    }

    fun getPostDraft(personaId: String): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[stringPreferencesKey("draft_post_$personaId")] ?: ""
    }

    suspend fun savePostDraft(personaId: String, text: String) {
        context.dataStore.edit { prefs ->
            if (text.isBlank()) {
                prefs.remove(stringPreferencesKey("draft_post_$personaId"))
            } else {
                prefs[stringPreferencesKey("draft_post_$personaId")] = text
            }
        }
    }

    suspend fun clearPostDraft(personaId: String) {
        context.dataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey("draft_post_$personaId"))
        }
    }
}
