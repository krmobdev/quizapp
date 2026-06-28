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
    val charisma: Int = 0,
    /** Knowledge: flat XP added per correct answer (scales with quiz length and accuracy). */
    val knowledge: Int = 0,
    /** Wealth: flat coins added per correct answer. */
    val wealth: Int = 0,
    /** Precision: extra Critical Success chance, stacking with Luck and Charisma. */
    val precision: Int = 0,
    /** Insight: extra XP percent, stacking with Strength. */
    val insight: Int = 0
) {
    val xpBonusPercent: Int get() = strength * 3 + insight * 2
    val coinBonusPercent: Int get() = intelligence * 3
    val extraTimeSeconds: Float get() = agility * 0.6f
    val doubleRewardChancePercent: Int get() = luck * 2

    /** Wisdom: flat XP added to every finished quiz. */
    val flatXpBonus: Int get() = wisdom * 5

    /** Endurance: flat coins added to every finished quiz. */
    val flatCoinBonus: Int get() = endurance * 2

    /** Charisma + Precision: extra Critical Success chance on top of [doubleRewardChancePercent]. */
    val critChanceBonusPercent: Int get() = charisma * 2 + precision * 2

    /** Focus: reward multiplier applied on a Critical Success (base x2, sharpened by Focus). */
    val critMultiplier: Float get() = 2f + focus * 0.15f

    /** Knowledge: flat XP granted for each correct answer in the quiz. */
    val flatXpPerCorrect: Int get() = knowledge * 2

    /** Wealth: flat coins granted for each correct answer in the quiz. */
    val flatCoinPerCorrect: Int get() = wealth
}

/**
 * Progression logic for computing levels and ranks from overall lifetime points (XP).
 *
 * Tuned for ~4–5 quizzes/day: level 120 and full stat + skill progression in roughly 3–6 months.
 */
object CharacterLevelCalculator {
    /**
     * Current level cap. XP beyond [lifetimeCap] is stored in [PlayerEntity.bankedLifetimePoints]
     * (hidden). Raising this constant automatically credits banked XP toward the new cap.
     */
    const val MAX_LEVEL = 1024

    /** Maximum value any single character stat can be upgraded to. */
    const val MAX_STAT = 50

    /**
     * Upper bound on the combined Critical Success chance (Luck + Charisma + Precision + Fortune
     * skill + Fortune talents). Keeps the crit roll a gamble even at full investment instead of a
     * guaranteed double.
     */
    const val MAX_CRIT_CHANCE_PERCENT = 90f

    /**
     * Cumulative lifetime XP to reach level L: [XP_PER_LEVEL_UNIT] × L × (L − 1).
     * Level 1024 ≈ 23 046 144 XP at the current unit.
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

    /**
     * Multiplier applied to daily quest rewards (coins + XP). Grows at 4% per level — slower than
     * the quiz multiplier (7.5%) because quests are guaranteed completions without a skill element.
     * Level 1 → ×1.0, Level 10 → ×1.36, Level 25 → ×1.96, Level 50 → ×2.96, Level 100 → ×4.96.
     */
    const val QUEST_BONUS_PER_LEVEL = 0.04f

    fun questRewardMultiplier(level: Int): Float =
        1f + (level - 1).coerceAtLeast(0) * QUEST_BONUS_PER_LEVEL

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
            level <= 119 -> "Бессмертный знаток"
            level <= 139 -> "Воплощение мудрости"
            level <= 159 -> "Покоритель знаний"
            level <= 179 -> "Властелин разума"
            level <= 199 -> "Титан эрудиции"
            level <= 219 -> "Вечный оракул"
            level <= 239 -> "Хранитель истины"
            level <= 279 -> "Абсолют"
            level <= 319 -> "Верховный мудрец"
            level <= 359 -> "Повелитель знаний"
            level <= 399 -> "Создатель истин"
            level <= 439 -> "Вечный архивариус"
            level <= 479 -> "Хранитель вечности"
            level <= 519 -> "Покоритель миров"
            level <= 559 -> "Суверен разума"
            level <= 599 -> "Божество эрудиции"
            level <= 639 -> "Архитектор реальности"
            level <= 679 -> "Властелин хроноса"
            level <= 719 -> "Оракул бесконечности"
            level <= 759 -> "Титан аксиом"
            level <= 799 -> "Первопричина"
            level <= 839 -> "Небесный картограф"
            level <= 879 -> "Повелитель парадоксов"
            level <= 919 -> "Созвездие разума"
            level <= 959 -> "Космический архонт"
            level <= 999 -> "Вечный первоисточник"
            level <= 1023 -> "Апофеоз знания"
            else -> "Омниарх"
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
            level <= 119 -> "Immortal Sage"
            level <= 139 -> "Wisdom Incarnate"
            level <= 159 -> "Knowledge Conqueror"
            level <= 179 -> "Mind Sovereign"
            level <= 199 -> "Erudition Titan"
            level <= 219 -> "Eternal Oracle"
            level <= 239 -> "Truth Keeper"
            level <= 279 -> "Absolute"
            level <= 319 -> "Sovereign Sage"
            level <= 359 -> "Knowledge Lord"
            level <= 399 -> "Truth Forger"
            level <= 439 -> "Eternal Archivist"
            level <= 479 -> "Eternity Keeper"
            level <= 519 -> "World Conqueror"
            level <= 559 -> "Mind Sovereign"
            level <= 599 -> "Erudition Deity"
            level <= 639 -> "Reality Architect"
            level <= 679 -> "Chronos Lord"
            level <= 719 -> "Infinity Oracle"
            level <= 759 -> "Axiom Titan"
            level <= 799 -> "Prime Cause"
            level <= 839 -> "Celestial Cartographer"
            level <= 879 -> "Paradox Ruler"
            level <= 919 -> "Constellation of Mind"
            level <= 959 -> "Cosmic Archon"
            level <= 999 -> "Eternal Wellspring"
            level <= 1023 -> "Apotheosis of Knowledge"
            else -> "Omniarch"
        }
    }

    /**
     * Free-XP cost to raise a stat from [currentValue] to the next point. Cheaper and gentler than
     * the old curve so early upgrades feel rewarding; the deep sink now comes from breadth
     * (12 characteristics + the Mastery and Talent trees) rather than punishing per-point cost.
     */
    fun statUpgradeCost(currentValue: Int): Int =
        EconomyBalance.scale(35 + currentValue * 9)
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