package com.rustam.quizapp

import com.rustam.quizapp.domain.CharacterLevelCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class EconomyBalanceTest {

    @Test
    fun level120_requires_expectedLifetimeXp() {
        assertEquals(313_560, CharacterLevelCalculator.xpRequiredForLevel(120))
        assertEquals(120, CharacterLevelCalculator.calculateLevel(313_560))
        assertEquals(120, CharacterLevelCalculator.calculateLevel(313_560, 50_000))
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
        assertEquals(120, CharacterLevelCalculator.calculateLevel(lifetime, banked))
    }

    @Test
    fun bankedXp_counts_when_level_cap_is_raised() {
        val cap120 = CharacterLevelCalculator.lifetimeCap()
        val (_, banked) = CharacterLevelCalculator.distributeLifetimeXp(cap120, 0, 100_000)
        assertEquals(100_000, banked)
        // Simulates raising MAX_LEVEL: effective XP would unlock levels beyond 120.
        val effective = cap120 + banked
        assertEquals(413_560, effective)
        assertEquals(128, uncappedLevel(effective))
    }

    @Test
    fun bankedXp_applies_to_higher_cap_without_migration() {
        val cap120 = CharacterLevelCalculator.lifetimeCap()
        val (_, banked) = CharacterLevelCalculator.distributeLifetimeXp(cap120, 0, 100_000)
        // After MAX_LEVEL is raised, the same split fields already represent 413_560 effective XP.
        assertEquals(cap120 + banked, CharacterLevelCalculator.effectiveLifetimePoints(cap120, banked))
    }

    private fun uncappedLevel(effectiveXp: Int): Int {
        val xp = effectiveXp.toDouble()
        return ((1.0 + Math.sqrt(1.0 + xp / (CharacterLevelCalculator.XP_PER_LEVEL_UNIT / 4.0))) / 2.0).toInt()
    }
}