package com.rustam.quizapp.ui.screens.stats

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.data.AppStats
import com.rustam.quizapp.data.PlayerProfile
import com.rustam.quizapp.data.PlayerRepository
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.StatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

data class PlayerUiState(
    val playerName: String = "",
    val avatarEmoji: String = "🙂",
    val points: Int = 0,
    val coins: Int = 0,
    val dailyQuestCategoryId: String? = null,
    val dailyQuestCompleted: Boolean = false,
    val totalQuizzes: Int = 0,
    val averageAccuracyPercent: Int? = null,
    val categories: List<CategoryStatsUi> = emptyList()
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val questionRepository = QuestionRepository(application)
    private val statsRepository = StatsRepository(application)
    private val playerRepository = PlayerRepository(application, questionRepository)

    val uiState: StateFlow<PlayerUiState> = combine(
        statsRepository.observeStats(),
        playerRepository.observeProfile()
    ) { stats, profile -> toUiState(stats, profile) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerUiState())

    fun updatePlayerName(name: String) {
        viewModelScope.launch {
            playerRepository.setPlayerName(name)
        }
    }

    private fun toUiState(stats: AppStats, profile: PlayerProfile): PlayerUiState {
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

        return PlayerUiState(
            playerName = profile.name,
            avatarEmoji = profile.avatarEmoji,
            points = profile.points,
            coins = profile.coins,
            dailyQuestCategoryId = profile.dailyQuestCategoryId,
            dailyQuestCompleted = profile.dailyQuestCompleted,
            totalQuizzes = stats.totalQuizzesCompleted,
            averageAccuracyPercent = averageAccuracy,
            categories = categories
        )
    }
}