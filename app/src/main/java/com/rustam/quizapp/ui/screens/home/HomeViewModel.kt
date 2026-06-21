package com.rustam.quizapp.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.audio.SoundManager
import com.rustam.quizapp.audio.SoundResources
import com.rustam.quizapp.audio.SoundType
import com.rustam.quizapp.data.Category
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.data.PlayerRepository
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.SettingsRepository
import com.rustam.quizapp.domain.QuizEventProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Difficulty options offered on the home screen. [ANY] maps to "no filter" (null). */
enum class DifficultyFilter(val emoji: String, val difficulty: Difficulty?) {
    EASY("🌱", Difficulty.EASY),
    MEDIUM("⚡", Difficulty.MEDIUM),
    HARD("🔥", Difficulty.HARD),
    ANY("🎲", null)
}

data class HomeUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val selectedDifficulty: DifficultyFilter = DifficultyFilter.ANY,
    val events: List<QuizEventProgress> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository(application)
    private val playerRepository = PlayerRepository(application, repository)
    private val soundManager = SoundManager(
        context = application,
        sounds = SoundResources.load(application),
        soundEnabled = SettingsRepository(application).soundEnabled,
        scope = viewModelScope
    )

    private val navigationState = MutableStateFlow(
        NavigationSlice(categories = repository.getCategories())
    )

    val uiState: StateFlow<HomeUiState> = combine(
        navigationState,
        playerRepository.observeProfile()
    ) { nav, profile ->
        HomeUiState(
            categories = nav.categories,
            selectedCategory = nav.selectedCategory,
            selectedDifficulty = nav.selectedDifficulty,
            events = profile.eventProgress
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun selectCategory(category: Category) {
        soundManager.play(SoundType.CLICK)
        navigationState.update { it.copy(selectedCategory = category) }
    }

    fun clearSelection() {
        soundManager.play(SoundType.CLICK)
        navigationState.update {
            it.copy(selectedCategory = null, selectedDifficulty = DifficultyFilter.ANY)
        }
    }

    fun selectDifficulty(filter: DifficultyFilter) {
        soundManager.play(SoundType.CLICK)
        navigationState.update { it.copy(selectedDifficulty = filter) }
    }

    fun playClick() {
        soundManager.play(SoundType.CLICK)
    }

    override fun onCleared() {
        soundManager.release()
    }

    private data class NavigationSlice(
        val categories: List<Category>,
        val selectedCategory: Category? = null,
        val selectedDifficulty: DifficultyFilter = DifficultyFilter.ANY
    )
}