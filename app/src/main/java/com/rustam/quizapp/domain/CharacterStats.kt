package com.rustam.quizapp.domain

/**
 * Represents the player's RPG characteristics (from 0 to 20).
 */
data class CharacterStats(
    val strength: Int = 0,
    val intelligence: Int = 0,
    val agility: Int = 0,
    val luck: Int = 0,
    val wisdom: Int = 0,
    val endurance: Int = 0,
    val focus: Int = 0,
    val charisma: Int = 0
) {
    // Current active bonus multipliers and absolute values
    val xpBonusPercent: Int get() = strength * 2
    val coinBonusPercent: Int get() = intelligence * 2
    val extraTimeSeconds: Float get() = agility * 0.5f
    val doubleRewardChancePercent: Int get() = luck * 2

    /** Wisdom: flat XP added to every finished quiz. */
    val flatXpBonus: Int get() = wisdom * 3

    /** Endurance: flat coins added to every finished quiz. */
    val flatCoinBonus: Int get() = endurance

    /** Charisma: extra Critical Success chance, stacking on top of [doubleRewardChancePercent]. */
    val critChanceBonusPercent: Int get() = charisma

    /** Focus: reward multiplier applied on a Critical Success (base x2, sharpened by Focus). */
    val critMultiplier: Float get() = 2f + focus * 0.1f
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

    /** Extra reward multiplier gained per player level above 1 (0.10 = +10% per level). */
    const val REWARD_BONUS_PER_LEVEL = 0.10f

    /** Level beyond which coins start scaling faster than XP. */
    const val HIGH_LEVEL_THRESHOLD = 20

    /** Extra coin multiplier gained per player level above [HIGH_LEVEL_THRESHOLD]. */
    const val HIGH_LEVEL_COIN_BONUS_PER_LEVEL = 0.15f

    /**
     * Reward multiplier applied to quiz rewards based on the player's level.
     * Higher level → higher rewards. Level 1 = x1.0, level 10 = x1.9, etc.
     */
    fun rewardMultiplier(level: Int): Float =
        1f + (level - 1).coerceAtLeast(0) * REWARD_BONUS_PER_LEVEL

    /**
     * Coin reward multiplier. Matches [rewardMultiplier] up to [HIGH_LEVEL_THRESHOLD], then
     * grows faster so high-level players earn noticeably more coins.
     * E.g. level 20 = x2.9, level 30 = x5.4, level 40 = x7.9.
     */
    fun coinRewardMultiplier(level: Int): Float =
        rewardMultiplier(level) +
            (level - HIGH_LEVEL_THRESHOLD).coerceAtLeast(0) * HIGH_LEVEL_COIN_BONUS_PER_LEVEL

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
