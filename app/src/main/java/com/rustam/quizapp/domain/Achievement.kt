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

/**
 * A single unlockable achievement. [progressOf] returns the player's current value and the
 * target needed to unlock; the achievement is earned once current >= target.
 */
data class Achievement(
    val id: String,
    val emoji: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descRes: Int,
    val rewardCoins: Int,
    val progressOf: (AchievementMetrics) -> Pair<Int, Int>
) {
    fun isUnlocked(metrics: AchievementMetrics): Boolean {
        val (current, target) = progressOf(metrics)
        return current >= target
    }
}

/**
 * Static catalogue of achievements. All conditions are derived from data the app already
 * tracks (quiz stats, play streak and character progress), so no extra bookkeeping is
 * needed beyond persisting which ids have been unlocked.
 */
object Achievements {
    private fun c(value: Int): Int = EconomyBalance.scale(value)

    val all: List<Achievement> = listOf(
        Achievement("first_quiz", "🎓", R.string.ach_first_quiz_title, R.string.ach_first_quiz_desc, c(50)) { it.totalQuizzes to 1 },
        Achievement("ten_quizzes", "📚", R.string.ach_ten_quizzes_title, R.string.ach_ten_quizzes_desc, c(100)) { it.totalQuizzes to 10 },
        Achievement("fifty_quizzes", "🏅", R.string.ach_fifty_quizzes_title, R.string.ach_fifty_quizzes_desc, c(250)) { it.totalQuizzes to 50 },
        Achievement("hundred_quizzes", "🎖️", R.string.ach_hundred_quizzes_title, R.string.ach_hundred_quizzes_desc, c(500)) { it.totalQuizzes to 100 },
        Achievement("perfect_quiz", "💯", R.string.ach_perfect_title, R.string.ach_perfect_desc, c(150)) { (if (it.hasPerfectQuiz) 1 else 0) to 1 },
        Achievement("hundred_correct", "✅", R.string.ach_hundred_correct_title, R.string.ach_hundred_correct_desc, c(100)) { it.totalCorrect to 100 },
        Achievement("five_hundred_correct", "🧠", R.string.ach_five_hundred_correct_title, R.string.ach_five_hundred_correct_desc, c(300)) { it.totalCorrect to 500 },
        Achievement("streak_3", "🔥", R.string.ach_streak3_title, R.string.ach_streak3_desc, c(75)) { it.bestStreak to 3 },
        Achievement("streak_7", "⚡", R.string.ach_streak7_title, R.string.ach_streak7_desc, c(200)) { it.bestStreak to 7 },
        Achievement("streak_14", "🌠", R.string.ach_streak14_title, R.string.ach_streak14_desc, c(350)) { it.bestStreak to 14 },
        Achievement("streak_30", "🌟", R.string.ach_streak30_title, R.string.ach_streak30_desc, c(500)) { it.bestStreak to 30 },
        Achievement("streak_100", "💎", R.string.ach_streak100_title, R.string.ach_streak100_desc, c(1000)) { it.bestStreak to 100 },
        Achievement("maxed_stat", "💪", R.string.ach_maxed_stat_title, R.string.ach_maxed_stat_desc, c(500)) { (if (it.hasMaxedStat) 1 else 0) to 1 },
        Achievement("all_categories", "🗺️", R.string.ach_all_categories_title, R.string.ach_all_categories_desc, c(200)) { it.categoriesPlayed to it.totalCategories.coerceAtLeast(1) },
        Achievement("level_10", "👑", R.string.ach_level10_title, R.string.ach_level10_desc, c(200)) { it.level to 10 },
        Achievement("level_30", "🌟", R.string.ach_level30_title, R.string.ach_level30_desc, c(350)) { it.level to 30 },
        Achievement("level_60", "🏆", R.string.ach_level60_title, R.string.ach_level60_desc, c(500)) { it.level to 60 },
        Achievement("level_90", "💫", R.string.ach_level90_title, R.string.ach_level90_desc, c(750)) { it.level to 90 },
        Achievement("level_120", "💎", R.string.ach_level120_title, R.string.ach_level120_desc, c(1500)) { it.level to 120 },
        Achievement("level_256", "🌌", R.string.ach_level256_title, R.string.ach_level256_desc, c(2500)) { it.level to 256 },
        Achievement("level_512", "🪐", R.string.ach_level512_title, R.string.ach_level512_desc, c(4000)) { it.level to 512 },
        Achievement("level_1024", "👁️", R.string.ach_level1024_title, R.string.ach_level1024_desc, c(7500)) { it.level to 1024 }
    )

    fun satisfied(metrics: AchievementMetrics): Set<String> =
        all.filter { it.isUnlocked(metrics) }.map { it.id }.toSet()

    fun byId(id: String): Achievement? = all.find { it.id == id }
}
