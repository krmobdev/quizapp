package com.rustam.quizapp

import com.rustam.quizapp.domain.AnswerReward
import com.rustam.quizapp.domain.RewardCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardCalculatorTest {

    private fun makeAnswers(correct: Int, total: Int, elapsedSeconds: Int = 3): List<AnswerReward> =
        (0 until total).map { i -> AnswerReward(isCorrect = i < correct, elapsedSeconds = elapsedSeconds) }

    @Test
    fun perfect_quiz_earns_more_than_partial() {
        val perfect  = RewardCalculator.calculate(makeAnswers(10, 10), total = 10, activeEvent = null)
        val partial  = RewardCalculator.calculate(makeAnswers(5,  10), total = 10, activeEvent = null)
        assertTrue(perfect.points >= partial.points)
        assertTrue(perfect.coins  >= partial.coins)
    }

    @Test
    fun zero_correct_answers_gives_zero_points() {
        val reward = RewardCalculator.calculate(makeAnswers(0, 10), total = 10, activeEvent = null)
        assertEquals(0, reward.points)
        assertEquals(0, reward.coins)
    }

    @Test
    fun slow_answers_give_less_than_fast_answers() {
        val fast = RewardCalculator.calculate(makeAnswers(10, 10, elapsedSeconds = 1), total = 10, activeEvent = null)
        val slow = RewardCalculator.calculate(makeAnswers(10, 10, elapsedSeconds = 8), total = 10, activeEvent = null)
        assertTrue(fast.points >= slow.points)
    }

    @Test
    fun speedMultiplier_is_1_at_zero_seconds() {
        assertEquals(1f, RewardCalculator.speedMultiplier(0), 0.001f)
    }

    @Test
    fun speedMultiplier_floors_at_zero() {
        assertEquals(0f, RewardCalculator.speedMultiplier(100), 0.001f)
    }
}
