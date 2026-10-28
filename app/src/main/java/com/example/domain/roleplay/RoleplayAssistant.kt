package com.example.domain.roleplay

import com.example.data.local.entity.PersonaEntity

object RoleplayAssistant {

    enum class RoleplayTone(val label: String, val icon: String) {
        CASUAL("Casual & Chill", "☕"),
        DRAMATIC("Dramatic & Intense", "🎭"),
        MYSTERIOUS("Cryptic & Whispers", "🔮"),
        SARCASTIC("Snarky & Sarcastic", "😏"),
        FORMAL("Formal & Diplomatic", "📜"),
        PHILOSOPHICAL("Reflective & Deep", "🌌")
    }

    data class AssistantSuggestion(
        val category: String,
        val text: String
    )

    fun generatePostSuggestions(persona: PersonaEntity, tone: RoleplayTone): List<AssistantSuggestion> {
        val name = persona.displayName
        val traits = persona.personalityTraits.ifBlank { "Unpredictable" }
        val style = persona.speakingStyle.ifBlank { "Distinctive" }
        val instructions = persona.roleplayInstructions.ifBlank { "Stay true to character" }

        val suggestions = mutableListOf<AssistantSuggestion>()

        when (tone) {
            RoleplayTone.CASUAL -> {
                suggestions.add(AssistantSuggestion("Daily Observation", "Another quiet hour in the district. Note to self: never underestimate the value of silence."))
                suggestions.add(AssistantSuggestion("Quick Question", "If you had five minutes to pack up and vanish, what's the one thing you grab? Asking for a friend."))
                suggestions.add(AssistantSuggestion("Mood Check", "Brewed fresh tea, checked the horizon. Still standing, despite yesterday's mess."))
            }
            RoleplayTone.DRAMATIC -> {
                suggestions.add(AssistantSuggestion("Climactic Thought", "The storm isn't approaching—it's already here, and we're standing right in the epicenter."))
                suggestions.add(AssistantSuggestion("Ominous Warning", "Remember my words when the clocks strike zero: some debts can never be repaid in coin."))
                suggestions.add(AssistantSuggestion("Battle Cry", "Let them assemble their legions. When dawn breaks, history will remember who stood firm."))
            }
            RoleplayTone.MYSTERIOUS -> {
                suggestions.add(AssistantSuggestion("Cryptic Note", "They looked for the signal in the sky, never realizing it was carved beneath their boots all along."))
                suggestions.add(AssistantSuggestion("Riddle", "Three keys, two locks, zero survivors. Connect the whispers."))
                suggestions.add(AssistantSuggestion("Observation", "Shadows don't lie, but they certainly know when to hide."))
            }
            RoleplayTone.SARCASTIC -> {
                suggestions.add(AssistantSuggestion("Snarky Remark", "Fascinating plan. Truly. Did you consult a magic 8-ball or just roll dice down a staircase?"))
                suggestions.add(AssistantSuggestion("Biting Truth", "If common sense were currency, this entire sector would be filing for bankruptcy."))
                suggestions.add(AssistantSuggestion("Unimpressed", "Another supposed 'crisis'? Call me when someone actually invents a new mistake."))
            }
            RoleplayTone.FORMAL -> {
                suggestions.add(AssistantSuggestion("Official Bulletin", "Notice to all concerned parties: previous agreements remain binding until formal renegotiation."))
                suggestions.add(AssistantSuggestion("Diplomatic Statement", "We urge all factions to exercise restraint. The peace we hold is fragile, yet necessary."))
                suggestions.add(AssistantSuggestion("Proclamation", "Let the ledger reflect our compliance, and our vigilance."))
            }
            RoleplayTone.PHILOSOPHICAL -> {
                suggestions.add(AssistantSuggestion("Existential Query", "Are we crafting destiny, or merely walking lines sketched by those who came before?"))
                suggestions.add(AssistantSuggestion("Memory", "Memories fade like ink in rain, yet some scars remain sharp as obsidian."))
                suggestions.add(AssistantSuggestion("Contemplation", "True power isn't holding the sword; it's choosing when to sheath it."))
            }
        }

        // Add a trait-specific suggestion
        if (persona.personalityTraits.isNotBlank()) {
            suggestions.add(
                AssistantSuggestion(
                    "Trait Inspired ($traits)",
                    "As someone known to be $traits: '$instructions. We see through the façade.'"
                )
            )
        }

        return suggestions
    }

    fun generateReplySuggestions(persona: PersonaEntity, tone: RoleplayTone): List<String> {
        return when (tone) {
            RoleplayTone.CASUAL -> listOf(
                "Spot on. Couldn't have phrased it better myself.",
                "Fair point, though I'd keep an eye on how that develops.",
                "Haha, you're not wrong at all."
            )
            RoleplayTone.DRAMATIC -> listOf(
                "You speak the truth that none dare whisper!",
                "Do not tempt fate with such careless certainty.",
                "Then our paths must cross at the precipice."
            )
            RoleplayTone.MYSTERIOUS -> listOf(
                "Perhaps... or perhaps that is merely what you were meant to see.",
                "The thread unravels faster than you think.",
                "Curious. The echoes match an older tale."
            )
            RoleplayTone.SARCASTIC -> listOf(
                "Groundbreaking revelation. Do you want a medal or a monument?",
                "Oh wonderful, another expert weighing in.",
                "I'll file that under 'Things I'll politely ignore'."
            )
            RoleplayTone.FORMAL -> listOf(
                "Your point is duly noted and recorded in our considerations.",
                "An astute assessment of current circumstances.",
                "We acknowledge your perspective with respect."
            )
            RoleplayTone.PHILOSOPHICAL -> listOf(
                "A thought that lingers long after the words fade.",
                "Every perspective holds a fragment of the mirror.",
                "Indeed; we are all travelers through the same quiet night."
            )
        }
    }
}
