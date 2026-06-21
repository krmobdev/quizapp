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
            level <= 4 -> "Новичок"
            level <= 9 -> "Ученик"
            level <= 14 -> "Адепт"
            level <= 19 -> "Искатель"
            level <= 24 -> "Специалист"
            level <= 29 -> "Ученый"
            level <= 34 -> "Мудрец"
            level <= 39 -> "Магистр"
            level <= 44 -> "Магистр разума"
            level <= 49 -> "Просвещенный"
            level <= 54 -> "Хранитель знаний"
            level <= 59 -> "Вершитель судеб"
            else -> "Легенда"
        }
    }

    fun getLevelRankEn(level: Int): String {
        return when {
            level <= 4 -> "Novice"
            level <= 9 -> "Apprentice"
            level <= 14 -> "Adept"
            level <= 19 -> "Seeker"
            level <= 24 -> "Expert"
            level <= 29 -> "Scholar"
            level <= 34 -> "Sage"
            level <= 39 -> "Mage"
            level <= 44 -> "Archmage"
            level <= 49 -> "Enlightened"
            level <= 54 -> "Loremaster"
            level <= 59 -> "Grandmaster"
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
