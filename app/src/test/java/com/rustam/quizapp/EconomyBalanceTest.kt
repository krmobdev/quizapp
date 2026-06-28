package com.rustam.quizapp

import com.rustam.quizapp.domain.CharacterLevelCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class EconomyBalanceTest {

    @Test
    fun level120_requires_expectedLifetimeXp() {
        assertEquals(314_160, CharacterLevelCalculator.xpRequiredForLevel(120))
        val level121Xp = CharacterLevelCalculator.xpRequiredForLevel(121)
        assertEquals(120, CharacterLevelCalculator.calculateLevel(level121Xp - 1))
    }

    @Test
    fun level240_requires_expectedLifetimeXp() {
        assertEquals(1_261_920, CharacterLevelCalculator.xpRequiredForLevel(240))
        val level241Xp = CharacterLevelCalculator.xpRequiredForLevel(241)
        assertEquals(240, CharacterLevelCalculator.calculateLevel(level241Xp - 1))
        assertEquals(240, CharacterLevelCalculator.calculateLevel(level241Xp - 1, 50_000))
    }

    @Test
    fun statUpgradeCosts_sumToExpectedSink() {
        val perStat = (0 until CharacterLevelCalculator.MAX_STAT)
            .sumOf { CharacterLevelCalculator.statUpgradeCost(it) }
        assertEquals(24_550, perStat)
        assertEquals(196_400, perStat * 8)
    }

    @Test
    fun bankedXp_accumulates_after_level_cap() {
        val cap = CharacterLevelCalculator.lifetimeCap()
        val (lifetime, banked) = CharacterLevelCalculator.distributeLifetimeXp(cap, 0, 5_000)
        assertEquals(cap, lifetime)
        assertEquals(5_000, banked)
        assertEquals(CharacterLevelCalculator.MAX_LEVEL, CharacterLevelCalculator.calculateLevel(lifetime, banked))
    }

    @Test
    fun bankedXp_counts_toward_level_when_stored() {
        val cap120 = CharacterLevelCalculator.xpRequiredForLevel(120)
        val overflow = 100_000
        // Legacy saves may hold banked XP from the old cap; it counts toward level automatically.
        assertEquals(137, CharacterLevelCalculator.calculateLevel(cap120, overflow))
        assertEquals(cap120 + overflow, CharacterLevelCalculator.effectiveLifetimePoints(cap120, overflow))
    }

}