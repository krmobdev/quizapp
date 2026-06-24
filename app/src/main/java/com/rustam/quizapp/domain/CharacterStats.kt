package com.rustam.quizapp.domain

/**
 * Represents the player's RPG characteristics (0 to [CharacterLevelCalculator.MAX_STAT]).
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
 *
 * Tuned for ~4–5 quizzes/day: level 120 and full stat + skill progression in roughly 3–6 months.
 */
object CharacterLevelCalculator {
    /**
     * Current level cap. XP beyond [lifetimeCap] is stored in [PlayerEntity.bankedLifetimePoints]
     * (hidden). Raising this constant (e.g. to 200) automatically credits banked XP toward the new cap.
     */
    const val MAX_LEVEL = 120

    /** Maximum value any single character stat can be upgraded to. */
    const val MAX_STAT = 50

    /**
     * Cumulative lifetime XP to reach level L: [XP_PER_LEVEL_UNIT] × L × (L − 1).
     * Level 120 ≈ 313 560 XP — aligns with maxing stats + Mastery Tree at average pace.
     */
    const val XP_PER_LEVEL_UNIT = 22

    /** Lifetime XP credited toward the current [MAX_LEVEL] cap (never above [lifetimeCap]). */
    fun lifetimeCap(): Int = xpRequiredForLevel(MAX_LEVEL)

    /** Active + banked lifetime XP — used for level/rank; banked XP applies when [MAX_LEVEL] rises. */
    fun effectiveLifetimePoints(lifetimePoints: Int, bankedLifetimePoints: Int = 0): Int =
        lifetimePoints + bankedLifetimePoints

    fun calculateLevel(lifetimePoints: Int, bankedLifetimePoints: Int = 0): Int {
        val effective = effectiveLifetimePoints(lifetimePoints, bankedLifetimePoints)
        if (effective <= 0) return 1
        val xp = effective.toDouble()
        val level = ((1.0 + Math.sqrt(1.0 + xp / (XP_PER_LEVEL_UNIT / 4.0))) / 2.0).toInt()
        return level.coerceIn(1, MAX_LEVEL)
    }

    fun xpRequiredForLevel(level: Int): Int {
        if (level <= 1) return 0
        return XP_PER_LEVEL_UNIT * level * (level - 1)
    }

    fun isMaxLevel(lifetimePoints: Int, bankedLifetimePoints: Int = 0): Boolean =
        calculateLevel(lifetimePoints, bankedLifetimePoints) >= MAX_LEVEL

    /**
     * Splits newly earned lifetime XP between the active counter (up to [lifetimeCap]) and the
     * hidden bank. When [MAX_LEVEL] is raised later, banked XP counts toward the new cap automatically.
     */
    fun distributeLifetimeXp(
        currentLifetime: Int,
        currentBanked: Int,
        earned: Int
    ): Pair<Int, Int> {
        if (earned <= 0) return currentLifetime to currentBanked
        val cap = lifetimeCap()
        val newEffective = (currentLifetime + currentBanked + earned).coerceAtLeast(0)
        val newLifetime = newEffective.coerceAtMost(cap)
        val newBanked = (newEffective - cap).coerceAtLeast(0)
        return newLifetime to newBanked
    }

    /** Extra reward multiplier gained per player level above 1. */
    const val REWARD_BONUS_PER_LEVEL = 0.075f

    /** Level beyond which coins start scaling faster than XP. */
    const val HIGH_LEVEL_THRESHOLD = 40

    const val COIN_BONUS_PER_LEVEL = 0.18f
    const val HIGH_LEVEL_COIN_BONUS_PER_LEVEL = 0.30f

    fun rewardMultiplier(level: Int): Float =
        1f + (level - 1).coerceAtLeast(0) * REWARD_BONUS_PER_LEVEL

    fun coinRewardMultiplier(level: Int): Float =
        1f + (level - 1).coerceAtLeast(0) * COIN_BONUS_PER_LEVEL +
            (level - HIGH_LEVEL_THRESHOLD).coerceAtLeast(0) * HIGH_LEVEL_COIN_BONUS_PER_LEVEL

    fun getLevelProgress(lifetimePoints: Int, bankedLifetimePoints: Int = 0): LevelProgress {
        val effective = effectiveLifetimePoints(lifetimePoints, bankedLifetimePoints)
        val currentLevel = calculateLevel(lifetimePoints, bankedLifetimePoints)
        if (currentLevel >= MAX_LEVEL) {
            return LevelProgress(
                level = MAX_LEVEL,
                currentXp = effective - xpRequiredForLevel(MAX_LEVEL),
                requiredXp = 0,
                progressFraction = 1f,
                isMaxLevel = true,
                bankedLifetimePoints = bankedLifetimePoints
            )
        }
        val xpForCurrent = xpRequiredForLevel(currentLevel)
        val xpForNext = xpRequiredForLevel(currentLevel + 1)
        val xpInCurrentLevel = effective - xpForCurrent
        val xpRequiredForNextLevel = xpForNext - xpForCurrent
        return LevelProgress(
            level = currentLevel,
            currentXp = xpInCurrentLevel.coerceAtLeast(0),
            requiredXp = xpRequiredForNextLevel,
            progressFraction = if (xpRequiredForNextLevel > 0) {
                xpInCurrentLevel.toFloat() / xpRequiredForNextLevel
            } else 0f,
            isMaxLevel = false,
            bankedLifetimePoints = bankedLifetimePoints
        )
    }

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
            level <= 49 -> "Просвещённый"
            level <= 54 -> "Хранитель знаний"
            level <= 59 -> "Вершитель судеб"
            level <= 69 -> "Легенда"
            level <= 79 -> "Миф"
            level <= 89 -> "Вечный знаток"
            level <= 99 -> "Архонт"
            level <= 109 -> "Превосходящий"
            else -> "Бессмертный знаток"
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
            level <= 69 -> "Legend"
            level <= 79 -> "Mythic"
            level <= 89 -> "Eternal Scholar"
            level <= 99 -> "Archon"
            level <= 109 -> "Transcendent"
            else -> "Immortal Sage"
        }
    }

    /** Free-XP cost to raise a stat from [currentValue] to the next point. */
    fun statUpgradeCost(currentValue: Int): Int = 50 + currentValue * 18
}

data class LevelProgress(
    val level: Int,
    val currentXp: Int,
    val requiredXp: Int,
    val progressFraction: Float,
    val isMaxLevel: Boolean = false,
    /** Hidden overflow XP stored while [CharacterLevelCalculator.MAX_LEVEL] is reached. */
    val bankedLifetimePoints: Int = 0
)