package com.rustam.quizapp

import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.data.Question
import com.rustam.quizapp.domain.MillionaireCatalog
import com.rustam.quizapp.domain.MillionaireLadder
import com.rustam.quizapp.domain.MillionaireLifelines
import com.rustam.quizapp.domain.PackCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MillionaireTest {

    private fun question(correct: Int = 0, difficulty: Difficulty = Difficulty.MEDIUM) = Question(
        id = "q",
        category = "math",
        difficulty = difficulty,
        text = "2+2?",
        options = listOf("3", "4", "5", "6"),
        correctIndex = correct
    )

    @Test
    fun ladder_amounts_atKeyRungs() {
        val prize = 10_000
        assertEquals(0, MillionaireLadder.amountAt(0, prize))
        assertEquals(1_000, MillionaireLadder.amountAt(5, prize))   // 0.10
        assertEquals(4_000, MillionaireLadder.amountAt(10, prize))  // 0.40
        assertEquals(prize, MillionaireLadder.amountAt(MillionaireLadder.LENGTH, prize)) // 1.00
        // Out-of-range rungs clamp to the full prize rather than overflow.
        assertEquals(prize, MillionaireLadder.amountAt(99, prize))
    }

    @Test
    fun ladder_safeRungs_andGuaranteedFallback() {
        val prize = 10_000
        assertTrue(MillionaireLadder.isSafe(5))
        assertTrue(MillionaireLadder.isSafe(10))
        assertFalse(MillionaireLadder.isSafe(7))
        assertEquals(0, MillionaireLadder.guaranteedAmount(4, prize))
        assertEquals(1_000, MillionaireLadder.guaranteedAmount(5, prize))
        assertEquals(1_000, MillionaireLadder.guaranteedAmount(9, prize))
        assertEquals(4_000, MillionaireLadder.guaranteedAmount(10, prize))
        assertEquals(4_000, MillionaireLadder.guaranteedAmount(14, prize))
    }

    @Test
    fun catalog_costsPrizes_andIdRoundTrip() {
        assertEquals(5_000, MillionaireCatalog.cost(PackCurrency.COINS, isCategory = false))
        assertEquals(7_000, MillionaireCatalog.cost(PackCurrency.COINS, isCategory = true))
        assertEquals(10_000, MillionaireCatalog.prize(PackCurrency.COINS))
        assertEquals(50, MillionaireCatalog.cost(PackCurrency.GEMS, isCategory = false))
        assertEquals(75, MillionaireCatalog.cost(PackCurrency.GEMS, isCategory = true))
        assertEquals(100, MillionaireCatalog.prize(PackCurrency.GEMS))
        assertEquals(15, MillionaireCatalog.TOTAL)

        val all = MillionaireCatalog.packId(PackCurrency.XP, null)
        assertEquals(PackCurrency.XP to null, MillionaireCatalog.parse(all))
        val cat = MillionaireCatalog.packId(PackCurrency.COINS, "history")
        assertEquals(PackCurrency.COINS to "history", MillionaireCatalog.parse(cat))
    }

    @Test
    fun fiftyFifty_hidesExactlyTwoWrongOptions() {
        repeat(20) {
            val q = question(correct = 1)
            val hidden = MillionaireLifelines.fiftyFifty(q, Random(it.toLong()))
            assertEquals(2, hidden.size)
            assertFalse("never hides the correct option", q.correctIndex in hidden)
        }
    }

    @Test
    fun askAudience_sumsTo100_overOptions() {
        repeat(20) {
            val q = question(correct = 2, difficulty = Difficulty.HARD)
            val pct = MillionaireLifelines.askAudience(q, Random(it.toLong()))
            assertEquals(q.options.size, pct.size)
            assertEquals(100, pct.sum())
            assertTrue(pct.all { p -> p >= 0 })
        }
    }

    @Test
    fun phoneFriend_returnsValidOption() {
        repeat(20) {
            val q = question(correct = 3, difficulty = Difficulty.EASY)
            val hint = MillionaireLifelines.phoneFriend(q, Random(it.toLong()))
            assertNotNull(hint)
            assertTrue(hint.suggestedIndex in q.options.indices)
        }
    }
}
