package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entity.PersonaEntity
import com.example.domain.roleplay.RoleplayAssistant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PersonaNet", appName)
    }

    @Test
    fun `roleplay assistant generates suggestions for persona`() {
        val persona = PersonaEntity(
            displayName = "Eldrin the Wise",
            username = "eldrin",
            personalityTraits = "Mysterious, Arcane",
            speakingStyle = "Speaks in ancient riddles",
            roleplayInstructions = "Always seek knowledge"
        )
        val suggestions = RoleplayAssistant.generatePostSuggestions(persona, RoleplayAssistant.RoleplayTone.MYSTERIOUS)
        assertTrue(suggestions.isNotEmpty())
        assertTrue(suggestions.any { it.text.isNotBlank() })
    }
}
