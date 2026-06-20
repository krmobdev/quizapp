package com.rustam.quizapp.ui.screens.stats

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.data.AppStats
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.StatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Per-category statistics prepared for display. [hasData] gates the neutral state. */
data class CategoryStatsUi(
    val id: String,
    @param:StringRes val titleRes: Int,
    val emoji: String,
    val attempts: Int,
    val accuracyPercent: Int,
    val bestScorePercent: Int
) {
    val hasData: Boolean get() = attempts > 0
}

data class StatsUiState(
    val totalQuizzes: Int = 0,
    val averageAccuracyPercent: Int? = null,
    val categories: List<CategoryStatsUi> = emptyList()
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val questionRepository = QuestionRepository(application)
    private val statsRepository = StatsRepository(application)

    val uiState: StateFlow<StatsUiState> = statsRepository.observeStats()
        .map(::toUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    /**
     * Joins the stored stats with the full category list so that every category is
     * shown — even ones without any completed quiz, which render the neutral state.
     */
    private fun toUiState(stats: AppStats): StatsUiState {
        val statsById = stats.categories.associateBy { it.categoryId }

        val categories = questionRepository.getCategories().map { category ->
            val saved = statsById[category.id]
            CategoryStatsUi(
                id = category.id,
                titleRes = category.titleRes,
                emoji = category.emoji,
                attempts = saved?.quizzesCompleted ?: 0,
                accuracyPercent = if (saved != null && saved.questionsAnswered > 0) {
                    saved.correctAnswers * 100 / saved.questionsAnswered
                } else 0,
                bestScorePercent = saved?.bestScorePercent ?: 0
            )
        }

        val totalCorrect = stats.categories.sumOf { it.correctAnswers }
        val totalAnswered = stats.categories.sumOf { it.questionsAnswered }
        val averageAccuracy = if (totalAnswered > 0) totalCorrect * 100 / totalAnswered else null

        return StatsUiState(
            totalQuizzes = stats.totalQuizzesCompleted,
            averageAccuracyPercent = averageAccuracy,
            categories = categories
        )
    }
}
