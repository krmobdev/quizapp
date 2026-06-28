package com.rustam.quizapp

import com.rustam.quizapp.domain.PassiveTalentTree
import com.rustam.quizapp.domain.TalentTreeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PassiveTalentTreeTest {

    @Test
    fun serializeAndParse_roundTrips() {
        val state = TalentTreeState(mapOf("insight_0" to 3, "prosperity_1" to 1))
        val csv = PassiveTalentTree.serializeProgress(state)
        assertEquals("insight_0:3,prosperity_1:1", csv)
        assertEquals(state, PassiveTalentTree.parseProgress(csv))
    }

    @Test
    fun firstNode_isAlwaysUnlockable() {
        val node = PassiveTalentTree.node("insight_0")!!
        val state = TalentTreeState()
        assertTrue(PassiveTalentTree.isUnlocked(node, state))
        assertTrue(PassiveTalentTree.canUpgrade(node, state, 10_000))
    }

    @Test
    fun secondNode_requiresFirstRank() {
        val node = PassiveTalentTree.node("insight_1")!!
        assertFalse(PassiveTalentTree.isUnlocked(node, TalentTreeState()))
        val withFirst = TalentTreeState(mapOf("insight_0" to 1))
        assertTrue(PassiveTalentTree.isUnlocked(node, withFirst))
    }

    @Test
    fun bonuses_stackAcrossRanks() {
        val ranks = (0 until 8).associate { "insight_$it" to 5 }
        val state = TalentTreeState(ranks)
        assertEquals(10f, state.xpBonusPercent, 0.01f)
    }
}