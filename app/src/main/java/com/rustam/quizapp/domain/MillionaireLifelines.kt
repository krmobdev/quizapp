package com.rustam.quizapp.domain

import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.data.Question
import kotlin.random.Random

/** A friend's guess: which option they'd pick, and whether they sound sure. */
data class PhoneFriendHint(val suggestedIndex: Int, val confident: Boolean)

/**
 * Pure helpers for the three Millionaire lifelines. Kept side-effect-free so they're trivially
 * testable; the ViewModel owns the once-per-run bookkeeping and UI state.
 */
object MillionaireLifelines {

    /** 50/50: two wrong option indices to hide (mirrors [QuizViewModel]'s 50/50). */
    fun fiftyFifty(question: Question, rng: Random = Random.Default): Set<Int> =
        question.options.indices
            .filter { it != question.correctIndex }
            .shuffled(rng)
            .take(2)
            .toSet()

    /** Phone a friend: suggests an option, correct more often on easier questions. */
    fun phoneFriend(question: Question, rng: Random = Random.Default): PhoneFriendHint {
        val pCorrect = when (question.difficulty) {
            Difficulty.EASY -> 0.9
            Difficulty.MEDIUM -> 0.75
            Difficulty.HARD -> 0.6
        }
        val correct = rng.nextDouble() < pCorrect
        val index = if (correct) {
            question.correctIndex
        } else {
            question.options.indices.filter { it != question.correctIndex }.random(rng)
        }
        return PhoneFriendHint(index, correct)
    }

    /**
     * Ask the audience: a percentage per option summing to exactly 100, biased toward the correct
     * answer (more so on easier questions). Any rounding remainder is folded into the correct option.
     */
    fun askAudience(question: Question, rng: Random = Random.Default): IntArray {
        val n = question.options.size
        if (n == 0) return IntArray(0)
        val correctBias = when (question.difficulty) {
            Difficulty.EASY -> 6.0
            Difficulty.MEDIUM -> 4.0
            Difficulty.HARD -> 2.5
        }
        val weights = DoubleArray(n) { i ->
            rng.nextDouble(0.2, 1.0) + if (i == question.correctIndex) correctBias else 0.0
        }
        val sum = weights.sum()
        val pct = IntArray(n) { Math.floor(weights[it] / sum * 100).toInt() }
        val remainder = 100 - pct.sum()
        val correct = question.correctIndex.coerceIn(0, n - 1)
        pct[correct] += remainder
        return pct
    }
}
