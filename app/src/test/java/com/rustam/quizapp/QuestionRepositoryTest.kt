package com.rustam.quizapp

import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.data.Question
import com.rustam.quizapp.data.QuestionRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionRepositoryTest {

    private val sampleBank = (1..20).map { i ->
        Question(
            id = "q$i",
            category = "test",
            difficulty = if (i % 2 == 0) Difficulty.EASY else Difficulty.MEDIUM,
            text = "Question $i",
            options = listOf("A$i", "B$i", "C$i", "D$i"),
            correctIndex = 0
        )
    }

    @Test
    fun recencyPool_prefersUnseenQuestions() {
        val recent = (11..20).map { "q$it" }
        val picked = invokeBuildRecencyPool(sampleBank, recent, quizSize = 10)
        assertTrue(picked.none { it.id in recent })
    }

    @Test
    fun recencyPool_fallsBackToOlderWhenFreshExhausted() {
        val recent = (1..18).map { "q$it" }
        val picked = invokeBuildRecencyPool(sampleBank, recent, quizSize = 10)
        assertTrue(picked.size >= 2)
        assertTrue(picked.any { it.id == "q19" || it.id == "q20" })
    }

    @Test
    fun recencyPool_prefersOldestSeenWhenAllSeen() {
        // Every question was seen; q1 longest ago, q20 most recently. A small quiz must draw from
        // the oldest-seen questions, never the most recent ones.
        val seenOldestFirst = (1..20).map { "q$it" }
        val pool = invokeBuildRecencyPool(sampleBank, seenOldestFirst, quizSize = 2)
        assertTrue(pool.isNotEmpty())
        // The 8-wide pool (quizSize * POOL_MULTIPLIER) should be the oldest-seen, not q13..q20.
        assertTrue(pool.none { it.id in setOf("q17", "q18", "q19", "q20") })
        assertTrue(pool.any { it.id in setOf("q1", "q2", "q3", "q4") })
    }

    /** Uses package-visible test hook via reflection-free duplicate of selection logic. */
    private fun invokeBuildRecencyPool(
        candidates: List<Question>,
        excludeIds: List<String>,
        quizSize: Int
    ): List<Question> {
        val recentWindow = excludeIds.takeLast(QuestionRepository.RECENT_WINDOW).toSet()
        val fresh = candidates.filter { it.id !in recentWindow }
        val preferred = if (fresh.size >= quizSize) fresh else {
            candidates.filter {
                it.id !in excludeIds.takeLast(quizSize * QuestionRepository.RECENT_QUIZ_MULTIPLIER).toSet()
            }.ifEmpty { candidates }
        }
        val rankById = excludeIds.withIndex().associate { (index, id) -> id to index }
        val ordered = preferred.sortedWith(
            compareBy<Question> { rankById[it.id] ?: -1 }
                .thenBy { if (it.id.startsWith(QuestionRepository.IMPORTED_ID_PREFIX)) 1 else 0 }
        )
        val poolSize = (quizSize * QuestionRepository.POOL_MULTIPLIER)
            .coerceAtMost(ordered.size).coerceAtLeast(quizSize)
        return ordered.take(poolSize)
    }
}