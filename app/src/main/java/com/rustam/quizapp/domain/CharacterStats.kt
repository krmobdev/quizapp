package com.rustam.quizapp.domain

/**
 * Represents the player's RPG characteristics (from 0 to 20).
 */
data class CharacterStats(
    val strength: Int = 0,
    val intelligence: Int = 0,
    val agility: Int = 0,
    val luck: Int = 0
) {
    // Current active bonus multipliers and absolute values
    val xpBonusPercent: Int get() = strength * 2
    val coinBonusPercent: Int get() = intelligence * 2
    val extraTimeSeconds: Float get() = agility * 0.5f
    val doubleRewardChancePercent: Int get() = luck * 2
}

/**
 * Progression logic for computing levels and ranks from overall lifetime points (XP).
 */
object CharacterLevelCalculator {
    /**
     * Calculates the player level (1-based) from total lifetime XP.
     * Formula: XP(L) = 50 * L * (L - 1)
     * L = floor( (1 + sqrt(1 + XP / 12.5)) / 2 )
     */
    fun calculateLevel(lifetimePoints: Int): Int {
        if (lifetimePoints <= 0) return 1
        val xp = lifetimePoints.toDouble()
        val level = ((1.0 + Math.sqrt(1.0 + xp / 12.5)) / 2.0).toInt()
        return level.coerceAtLeast(1)
    }

    /** Returns cumulative lifetime XP required to reach the given level. */
    fun xpRequiredForLevel(level: Int): Int {
        if (level <= 1) return 0
        return 50 * level * (level - 1)
    }

    /** Returns details about current level progression for progress bars. */
    fun getLevelProgress(lifetimePoints: Int): LevelProgress {
        val currentLevel = calculateLevel(lifetimePoints)
        val xpForCurrent = xpRequiredForLevel(currentLevel)
        val xpForNext = xpRequiredForLevel(currentLevel + 1)
        val xpInCurrentLevel = lifetimePoints - xpForCurrent
        val xpRequiredForNextLevel = xpForNext - xpForCurrent
        return LevelProgress(
            level = currentLevel,
            currentXp = xpInCurrentLevel.coerceAtLeast(0),
            requiredXp = xpRequiredForNextLevel,
            progressFraction = if (xpRequiredForNextLevel > 0) {
                xpInCurrentLevel.toFloat() / xpRequiredForNextLevel
            } else 0f
        )
    }

    /** Returns localized rank name (RU) based on the level. */
    fun getLevelRank(level: Int): String {
        return when {
            level <= 2 -> "Новичок"
            level <= 5 -> "Ученик"
            level <= 9 -> "Искатель"
            level <= 14 -> "Мудрец"
            level <= 20 -> "Магистр"
            level <= 30 -> "Архимаг"
            else -> "Легенда"
        }
    }

    /** Returns localized rank name (EN) based on the level. */
    fun getLevelRankEn(level: Int): String {
        return when {
            level <= 2 -> "Novice"
            level <= 5 -> "Apprentice"
            level <= 9 -> "Seeker"
            level <= 14 -> "Scholar"
            level <= 20 -> "Sage"
            level <= 30 -> "Archmage"
            else -> "Legend"
        }
    }
}

/** Helper data structure representing current level progress. */
data class LevelProgress(
    val level: Int,
    val currentXp: Int,
    val requiredXp: Int,
    val progressFraction: Float
)
