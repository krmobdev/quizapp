package com.rustam.quizapp.ui.navigation

import androidx.lifecycle.ViewModel
import com.rustam.quizapp.domain.QuizResult

/**
 * In-memory holder shared across the quiz flow (scoped to the nav graph).
 *
 * The quiz screen publishes its [QuizResult] here when finished; the result screen
 * reads it.
 */
class QuizFlowViewModel : ViewModel() {

    var result: QuizResult? = null
        private set

    fun publishResult(result: QuizResult) {
        this.result = result
    }
}
