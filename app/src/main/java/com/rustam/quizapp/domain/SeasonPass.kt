package com.rustam.quizapp.domain

/** What a single season-track level hands out when claimed. */
enum class SeasonRewardKind { COINS, GEMS, XP, BOOSTER }

data class SeasonReward(
    val kind: SeasonRewardKind,
    val amount: Int,
    /** Item id for [SeasonRewardKind.BOOSTER] rewards (an inventory consumable). */
    val itemId: String? = null
)

/**
 * A free, repeating 30-day "season" track. Players earn season XP by finishing quizzes; every
 * [XP_PER_LEVEL] of it unlocks the next of [MAX_LEVEL] reward tiers, claimable once each. The
 * season id is derived from the calendar ([currentSeasonId]) with no server — when a new period
 * starts the track resets automatically. A recurring reason to come back even after maxing out the
 * permanent progression, and a steady gem/booster faucet that does not inflate coins.
 */
object SeasonPass {
    const val LENGTH_DAYS = 30
    const val MAX_LEVEL = 30
    const val XP_PER_LEVEL = 100

    /** Base season XP granted per finished quiz, before the per-correct bonus. */
    const val XP_PER_QUIZ = 8
    const val XP_PER_CORRECT = 2

    /** The season period the given epoch day belongs to. */
    fun currentSeasonId(epochDay: Long): Int = Math.floorDiv(epochDay, LENGTH_DAYS.toLong()).toInt()

    /** Day index within the current season (0-based), for the "X days left" hint. */
    fun dayInSeason(epochDay: Long): Int = Math.floorMod(epochDay, LENGTH_DAYS.toLong()).toInt()

    fun daysLeft(epochDay: Long): Int = LENGTH_DAYS - dayInSeason(epochDay)

    /** Current track level reached from accumulated [seasonXp] (0..[MAX_LEVEL]). */
    fun level(seasonXp: Int): Int = (seasonXp / XP_PER_LEVEL).coerceIn(0, MAX_LEVEL)

    /** Cumulative season XP needed to reach [level] (1-based). */
    fun xpForLevel(level: Int): Int = level * XP_PER_LEVEL

    /** Season XP earned for a finished quiz with [correctCount] correct answers. */
    fun xpForQuiz(correctCount: Int): Int = XP_PER_QUIZ + correctCount * XP_PER_CORRECT

    /** The reward at [level] (1-based). Coins on most tiers, gems on milestones, periodic boosters. */
    fun reward(level: Int): SeasonReward = when {
        level % 10 == 0 -> SeasonReward(SeasonRewardKind.GEMS, 30 + level)
        level % 5 == 0 -> SeasonReward(SeasonRewardKind.BOOSTER, 1, itemId = "booster_xp_medium")
        level % 3 == 0 -> SeasonReward(SeasonRewardKind.GEMS, 6)
        else -> SeasonReward(SeasonRewardKind.COINS, EconomyBalance.scale(90 + level * 18))
    }

    fun isClaimed(mask: Long, level: Int): Boolean = (mask and bit(level)) != 0L

    fun withClaimed(mask: Long, level: Int): Long = mask or bit(level)

    /** A level is claimable when it has been reached and not yet claimed. */
    fun canClaim(level: Int, seasonXp: Int, mask: Long): Boolean =
        level in 1..MAX_LEVEL && level(seasonXp) >= level && !isClaimed(mask, level)

    private fun bit(level: Int): Long = 1L shl (level - 1)
}
