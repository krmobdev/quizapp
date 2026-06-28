package com.rustam.quizapp

import com.rustam.quizapp.domain.SeasonPass
import com.rustam.quizapp.domain.SeasonRewardKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonPassTest {

    @Test
    fun level_returns_correct_track_level_from_xp() {
        assertEquals(0, SeasonPass.level(0))
        assertEquals(0, SeasonPass.level(SeasonPass.XP_PER_LEVEL - 1))
        assertEquals(1, SeasonPass.level(SeasonPass.XP_PER_LEVEL))
        assertEquals(SeasonPass.MAX_LEVEL, SeasonPass.level(SeasonPass.XP_PER_LEVEL * SeasonPass.MAX_LEVEL))
        // Capped at MAX_LEVEL even with extra XP
        assertEquals(SeasonPass.MAX_LEVEL, SeasonPass.level(SeasonPass.XP_PER_LEVEL * 100))
    }

    @Test
    fun xpForQuiz_grows_with_correct_answers() {
        val baseXp = SeasonPass.xpForQuiz(0)
        assertEquals(SeasonPass.XP_PER_QUIZ, baseXp)
        assertTrue(SeasonPass.xpForQuiz(10) > baseXp)
        assertEquals(
            SeasonPass.XP_PER_QUIZ + 10 * SeasonPass.XP_PER_CORRECT,
            SeasonPass.xpForQuiz(10)
        )
    }

    @Test
    fun canClaim_is_true_only_when_reached_and_not_yet_claimed() {
        val xpForLevel3 = SeasonPass.xpForLevel(3)
        assertFalse(SeasonPass.canClaim(3, xpForLevel3 - 1, 0L))
        assertTrue(SeasonPass.canClaim(3, xpForLevel3, 0L))
        // After claiming
        val claimedMask = SeasonPass.withClaimed(0L, 3)
        assertFalse(SeasonPass.canClaim(3, xpForLevel3, claimedMask))
    }

    @Test
    fun isClaimed_and_withClaimed_are_inverse_operations() {
        var mask = 0L
        assertFalse(SeasonPass.isClaimed(mask, 1))
        mask = SeasonPass.withClaimed(mask, 1)
        assertTrue(SeasonPass.isClaimed(mask, 1))
        assertFalse(SeasonPass.isClaimed(mask, 2))
    }

    @Test
    fun daysLeft_decreases_within_30_day_season() {
        val epochDay = 19900L
        val daysLeft = SeasonPass.daysLeft(epochDay)
        assertTrue(daysLeft in 1..SeasonPass.LENGTH_DAYS)
    }

    @Test
    fun reward_milestone_levels_give_gems() {
        // Level 10, 20, 30 give gems
        assertEquals(SeasonRewardKind.GEMS, SeasonPass.reward(10).kind)
        assertEquals(SeasonRewardKind.GEMS, SeasonPass.reward(20).kind)
        assertEquals(SeasonRewardKind.GEMS, SeasonPass.reward(30).kind)
    }

    @Test
    fun reward_every_5th_non_milestone_gives_booster() {
        // Level 5 and 15 (multiples of 5 but not 10) give boosters
        assertEquals(SeasonRewardKind.BOOSTER, SeasonPass.reward(5).kind)
        assertEquals(SeasonRewardKind.BOOSTER, SeasonPass.reward(15).kind)
        assertEquals(SeasonRewardKind.BOOSTER, SeasonPass.reward(25).kind)
    }

    @Test
    fun all_30_reward_levels_are_defined() {
        // No level should throw an exception
        (1..SeasonPass.MAX_LEVEL).forEach { level ->
            val reward = SeasonPass.reward(level)
            assertTrue(reward.amount > 0 || reward.itemId != null)
        }
    }
}
