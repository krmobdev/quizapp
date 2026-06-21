package com.rustam.quizapp.domain

import androidx.annotation.StringRes
import com.rustam.quizapp.R

/** A snapshot of player progress used to evaluate which achievements are unlocked. */
data class AchievementMetrics(
    val totalQuizzes: Int = 0,
    val totalCorrect: Int = 0,
    val bestStreak: Int = 0,
    val level: Int = 1,
    val hasMaxedStat: Boolean = false,
    val hasPerfectQuiz: Boolean = false,
    val categoriesPlayed: Int = 0,
    val totalCategories: Int = 0
)

/** A single unlockable achievement. [isUnlocked] decides eligibility from a metrics snapshot. */
data class Achievement(
    val id: String,
    val emoji: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descRes: Int,
    val rewardCoins: Int,
    val isUnlocked: (AchievementMetrics) -> Boolean
)

/**
 * Static catalogue of achievements. All conditions are derived from data the app already
 * tracks (quiz stats, play streak and character progress), so no extra bookkeeping is
 * needed beyond persisting which ids have been unlocked.
 */
object Achievements {
    val all: List<Achievement> = listOf(
        Achievement("first_quiz", "🎓", R.string.ach_first_quiz_title, R.string.ach_first_quiz_desc, 50) { it.totalQuizzes >= 1 },
        Achievement("ten_quizzes", "📚", R.string.ach_ten_quizzes_title, R.string.ach_ten_quizzes_desc, 100) { it.totalQuizzes >= 10 },
        Achievement("fifty_quizzes", "🏅", R.string.ach_fifty_quizzes_title, R.string.ach_fifty_quizzes_desc, 250) { it.totalQuizzes >= 50 },
        Achievement("perfect_quiz", "💯", R.string.ach_perfect_title, R.string.ach_perfect_desc, 150) { it.hasPerfectQuiz },
        Achievement("hundred_correct", "✅", R.string.ach_hundred_correct_title, R.string.ach_hundred_correct_desc, 100) { it.totalCorrect >= 100 },
        Achievement("five_hundred_correct", "🧠", R.string.ach_five_hundred_correct_title, R.string.ach_five_hundred_correct_desc, 300) { it.totalCorrect >= 500 },
        Achievement("streak_3", "🔥", R.string.ach_streak3_title, R.string.ach_streak3_desc, 75) { it.bestStreak >= 3 },
        Achievement("streak_7", "⚡", R.string.ach_streak7_title, R.string.ach_streak7_desc, 200) { it.bestStreak >= 7 },
        Achievement("streak_30", "🌟", R.string.ach_streak30_title, R.string.ach_streak30_desc, 500) { it.bestStreak >= 30 },
        Achievement("maxed_stat", "💪", R.string.ach_maxed_stat_title, R.string.ach_maxed_stat_desc, 300) { it.hasMaxedStat },
        Achievement("all_categories", "🗺️", R.string.ach_all_categories_title, R.string.ach_all_categories_desc, 200) { it.totalCategories > 0 && it.categoriesPlayed >= it.totalCategories },
        Achievement("level_10", "👑", R.string.ach_level10_title, R.string.ach_level10_desc, 250) { it.level >= 10 }
    )

    fun satisfied(metrics: AchievementMetrics): Set<String> =
        all.filter { it.isUnlocked(metrics) }.map { it.id }.toSet()

    fun byId(id: String): Achievement? = all.find { it.id == id }
}
