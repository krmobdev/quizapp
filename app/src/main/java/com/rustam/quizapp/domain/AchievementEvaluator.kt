package com.rustam.quizapp.domain

import com.rustam.quizapp.data.AchievementsRepository
import com.rustam.quizapp.data.PlayerRepository
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.StatsRepository
import com.rustam.quizapp.data.StreakRepository
import kotlinx.coroutines.flow.first

/**
 * Builds an [AchievementMetrics] snapshot from the various repositories, unlocks any newly
 * earned achievements and grants their coin rewards. Safe to call after any event that can
 * change progress (a finished quiz, a stat upgrade).
 */
class AchievementEvaluator(
    private val statsRepository: StatsRepository,
    private val streakRepository: StreakRepository,
    private val playerRepository: PlayerRepository,
    private val achievementsRepository: AchievementsRepository,
    private val questionRepository: QuestionRepository
) {
    suspend fun evaluate(): List<Achievement> {
        val stats = statsRepository.observeStats().first()
        val streak = streakRepository.observeStreak().first()
        val profile = playerRepository.observeProfile().first()

        val charStats = profile.stats
        val metrics = AchievementMetrics(
            totalQuizzes = stats.totalQuizzesCompleted,
            totalCorrect = stats.categories.sumOf { it.correctAnswers },
            bestStreak = streak.best,
            level = CharacterLevelCalculator.calculateLevel(
                profile.lifetimePoints,
                profile.bankedLifetimePoints
            ),
            hasMaxedStat = listOf(
                charStats.strength,
                charStats.intelligence,
                charStats.agility,
                charStats.luck,
                charStats.wisdom,
                charStats.endurance,
                charStats.focus,
                charStats.charisma,
                charStats.knowledge,
                charStats.wealth,
                charStats.precision,
                charStats.insight
            ).any { it >= CharacterLevelCalculator.MAX_STAT },
            hasPerfectQuiz = stats.categories.any { it.bestScorePercent >= 100 },
            categoriesPlayed = stats.categories.count { it.quizzesCompleted > 0 },
            totalCategories = questionRepository.getCategories().size
        )

        val newlyUnlocked = achievementsRepository.unlockNew(metrics)
        val coins = newlyUnlocked.sumOf { it.rewardCoins }
        if (coins > 0) playerRepository.addCoins(coins)
        // Each achievement also grants a few gems (the rare currency).
        val gems = newlyUnlocked.size * GEMS_PER_ACHIEVEMENT
        if (gems > 0) playerRepository.addGems(gems)
        return newlyUnlocked
    }

    private companion object {
        const val GEMS_PER_ACHIEVEMENT = 5
    }
}
