package com.rustam.quizapp.domain

import com.rustam.quizapp.data.Question

/** Outcome of a finished quiz run, shared between the quiz and result screens. */
data class QuizResult(
    val correct: Int,
    val total: Int,
    val penalties: Int = 0,
    val mistakes: List<Question>
) {
    /** Final score: correct answers minus timeout penalties. */
    val score: Int get() = correct - penalties
}