package com.rustam.quizapp.ui.navigation

import androidx.lifecycle.ViewModel
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.data.Question
import com.rustam.quizapp.domain.QuizResult

/**
 * In-memory holder shared across the quiz flow (scoped to the nav graph).
 *
 * The quiz screen publishes its [QuizResult] here when finished; the result screen
 * reads it. For "retry mistakes" the result screen stores the mistaken questions,
 * which the next quiz screen consumes as its preset list.
 */
class QuizFlowViewModel : ViewModel() {

    var result: QuizResult? = null
        private set

    /** Category/difficulty of the last run, reused when retrying mistakes. */
    var categoryId: String? = null
        private set
    var difficulty: Difficulty? = null
        private set

    private var retryQuestions: List<Question>? = null

    fun publishResult(result: QuizResult, categoryId: String, difficulty: Difficulty?) {
        this.result = result
        this.categoryId = categoryId
        this.difficulty = difficulty
    }

    fun setRetryQuestions(questions: List<Question>) {
        retryQuestions = questions
    }

    /** Returns the pending retry questions once, then clears them. */
    fun consumeRetryQuestions(): List<Question>? = retryQuestions?.also { retryQuestions = null }
}
