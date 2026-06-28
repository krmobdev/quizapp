package com.rustam.quizapp.domain

import kotlinx.serialization.Serializable

/** Points and coins earned from a single quiz run. */
data class QuizReward(
    val points: Int,
    val coins: Int,
    val eventBonus: QuizEventType? = null,
    val speedBonusPercent: Int = 0,
    val isCriticalSuccess: Boolean = false,
    /** Multiplier applied on a Critical Success (1.0 when none). */
    val critMultiplier: Float = 1f,
    val xpBonus: Int = 0,
    val coinBonus: Int = 0,
    /** Whether an active temporary boost doubled the XP / coins of this reward. */
    val xpBoosted: Boolean = false,
    val coinBoosted: Boolean = false,
    /** Raw base reward (before level scaling and stat bonuses), for the result breakdown. */
    val basePoints: Int = 0,
    val baseCoins: Int = 0,
    /** Level reward multiplier applied to the base points (1.0 = no bonus). */
    val levelMultiplier: Float = 1f,
    /** Level reward multiplier applied to the base coins (grows faster past level 20). */
    val coinLevelMultiplier: Float = 1f,
    /** Player level before and after this reward was granted (for level-up detection). */
    val previousLevel: Int = 1,
    val newLevel: Int = 1,
    /** Gems (💎) earned from this quiz: a perfect-score bonus plus any level-up payouts. */
    val gemsEarned: Int = 0
) {
    val hasEventBonus: Boolean get() = eventBonus != null

    /** Whether this reward pushed the player to a new level. */
    val leveledUp: Boolean get() = newLevel > previousLevel
}

@Serializable
data class AnswerReward(
    val isCorrect: Boolean,
    val elapsedSeconds: Int
)

object RewardCalculator {
    private val POINTS_PER_CORRECT = EconomyBalance.scale(12)
    private val COINS_PER_CORRECT = EconomyBalance.scale(4)
    private val PERFECT_BONUS_POINTS = EconomyBalance.scale(45)
    private const val SPEED_LOSS_PER_SECOND = 0.1f

    fun speedMultiplier(elapsedSeconds: Int): Float =
        (1f - SPEED_LOSS_PER_SECOND * elapsedSeconds).coerceAtLeast(0f)

    fun calculate(
        answers: List<AnswerReward>,
        total: Int,
        activeEvent: QuizEvent?
    ): QuizReward {
        var points = 0f
        var coins = 0f
        var speedWeightedCorrect = 0
        val correctCount = answers.count { it.isCorrect }

        answers.forEach { answer ->
            if (!answer.isCorrect) return@forEach
            val multiplier = speedMultiplier(answer.elapsedSeconds)
            points += POINTS_PER_CORRECT * multiplier
            coins += COINS_PER_CORRECT * multiplier
            speedWeightedCorrect++
        }

        val perfectBonus = if (total > 0 && correctCount == total) PERFECT_BONUS_POINTS else 0
        points += perfectBonus

        var eventBonus: QuizEventType? = null
        if (activeEvent != null) {
            eventBonus = activeEvent.type
            points += activeEvent.bonusPoints
            val bonusCoins = correctCount * COINS_PER_CORRECT * (activeEvent.coinMultiplier - 1)
            coins += bonusCoins
        }

        val baseMaxPoints = correctCount * POINTS_PER_CORRECT + perfectBonus +
            (activeEvent?.bonusPoints ?: 0)
        val speedBonusPercent = if (baseMaxPoints > 0) {
            ((points / baseMaxPoints) * 100f).toInt().coerceIn(0, 100)
        } else {
            0
        }

        return QuizReward(
            points = points.toInt(),
            coins = coins.toInt(),
            eventBonus = eventBonus,
            speedBonusPercent = if (speedWeightedCorrect > 0) speedBonusPercent else 0
        )
    }
}