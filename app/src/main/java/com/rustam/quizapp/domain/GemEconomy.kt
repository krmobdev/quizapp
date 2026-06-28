package com.rustam.quizapp.domain

/**
 * Central tuning for the gem (💎) currency — the rare, prestige currency that sits on top of the
 * coin/XP economy ([EconomyBalance]). One place to balance every gem faucet and the gem→coin
 * exchange so the whole layer stays coherent.
 *
 * Pace target: an active player earns roughly 10–15 gems on a busy day, so premium cosmetics
 * (60–180 gems) are 1–2 weeks of dedicated play rather than instant or unreachable.
 *
 * Spends (premium avatars/titles/themes, the Mythic Chest and gem bundles) live in [ShopCatalog]
 * and [MythicBox]; this object only holds the earning side and shared rates.
 */
object GemEconomy {
    /** Gems granted per unlocked achievement (one-time milestones). */
    const val ACHIEVEMENT_GEMS = 5

    /** Gems granted per completed timed event (daily/weekly/weekend/marathon). */
    const val EVENT_GEMS = 2

    /** Gems for finishing a real quiz with a perfect (100%) score. */
    const val PERFECT_QUIZ_GEMS = 2

    /** Gems per character level gained from a quiz reward. */
    const val GEMS_PER_LEVEL = 2

    /** Every Nth day of the daily-login cycle also pays gems. */
    const val DAILY_LOGIN_INTERVAL = 7

    /** Gems paid on a qualifying daily-login day. */
    const val DAILY_LOGIN_GEMS = 8

    /** Bonus gems for claiming all of today's daily quests. */
    const val ALL_QUESTS_GEMS = 5

    /** Gems earned from leveling up between [previousLevel] and [newLevel] during one reward. */
    fun levelUpGems(previousLevel: Int, newLevel: Int): Int =
        (newLevel - previousLevel).coerceAtLeast(0) * GEMS_PER_LEVEL

    /** Gem bonus for the daily-login reward at 1-based [dayIndex] (0 on non-milestone days). */
    fun dailyLoginGems(dayIndex: Int): Int =
        if (dayIndex > 0 && dayIndex % DAILY_LOGIN_INTERVAL == 0) DAILY_LOGIN_GEMS else 0
}
