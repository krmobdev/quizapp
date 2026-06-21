package com.rustam.quizapp.domain

import com.rustam.quizapp.data.Difficulty
import kotlin.math.roundToInt

/**
 * Picks how many questions of each [Difficulty] a quiz should contain based on how
 * accurately the player has answered in a category so far. Weaker players get an
 * easier mix; stronger players are pushed towards harder questions, keeping the run
 * in the "flow zone" — challenging but not hopeless.
 */
object AdaptiveDifficulty {

    /**
     * Returns a difficulty -> question-count map summing to [quizSize].
     * [accuracyPercent] is the player's lifetime accuracy in the category (0..100),
     * or `null` when they have not played it yet (treated as a beginner).
     */
    fun mix(accuracyPercent: Int?, quizSize: Int): Map<Difficulty, Int> {
        if (quizSize <= 0) return emptyMap()

        val (easyFrac, hardFrac) = when {
            accuracyPercent == null || accuracyPercent < 50 -> 0.6f to 0.1f
            accuracyPercent < 75 -> 0.3f to 0.25f
            accuracyPercent < 90 -> 0.15f to 0.45f
            else -> 0.05f to 0.65f
        }

        val easy = (quizSize * easyFrac).roundToInt().coerceIn(0, quizSize)
        val hard = (quizSize * hardFrac).roundToInt().coerceIn(0, quizSize - easy)
        val medium = quizSize - easy - hard

        return buildMap {
            if (easy > 0) put(Difficulty.EASY, easy)
            if (medium > 0) put(Difficulty.MEDIUM, medium)
            if (hard > 0) put(Difficulty.HARD, hard)
        }
    }
}
