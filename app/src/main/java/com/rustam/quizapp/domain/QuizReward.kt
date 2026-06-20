package com.rustam.quizapp.domain

/** Points and coins earned from a single quiz run. */
data class QuizReward(
    val points: Int,
    val coins: Int,
    val dailyQuestBonus: Boolean = false
)

object RewardCalculator {
    private const val POINTS_PER_SCORE = 10
    private const val PERFECT_BONUS_POINTS = 50
    private const val DAILY_QUEST_BONUS_POINTS = 100
    private const val COINS_PER_SCORE = 2
    private const val DAILY_QUEST_COIN_MULTIPLIER = 3

    fun calculate(score: Int, total: Int, isDailyQuest: Boolean): QuizReward {
        val perfectBonus = if (total > 0 && score == total) PERFECT_BONUS_POINTS else 0
        val dailyPoints = if (isDailyQuest) DAILY_QUEST_BONUS_POINTS else 0
        val baseCoins = score * COINS_PER_SCORE
        val dailyCoins = if (isDailyQuest) score * COINS_PER_SCORE * DAILY_QUEST_COIN_MULTIPLIER else 0
        return QuizReward(
            points = score * POINTS_PER_SCORE + perfectBonus + dailyPoints,
            coins = baseCoins + dailyCoins,
            dailyQuestBonus = isDailyQuest
        )
    }
}