package com.rustam.quizapp.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.audio.SoundManager
import com.rustam.quizapp.audio.SoundResources
import com.rustam.quizapp.audio.SoundType
import com.rustam.quizapp.data.Category
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Difficulty options offered on the home screen. [ANY] maps to "no filter" (null). */
enum class DifficultyFilter(val label: String, val difficulty: Difficulty?) {
    EASY("Лёгкий", Difficulty.EASY),
    MEDIUM("Средний", Difficulty.MEDIUM),
    HARD("Сложный", Difficulty.HARD),
    ANY("Любой", null)
}

data class HomeUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val selectedDifficulty: DifficultyFilter = DifficultyFilter.ANY
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository(application)
    private val soundManager = SoundManager(
        context = application,
        sounds = SoundResources.load(application),
        soundEnabled = SettingsRepository(application).soundEnabled,
        scope = viewModelScope
    )

    private val _uiState = MutableStateFlow(
        HomeUiState(categories = repository.getCategories())
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun selectCategory(category: Category) {
        soundManager.play(SoundType.CLICK)
        _uiState.update { it.copy(selectedCategory = category) }
    }

    /** Returns to the category list, resetting the difficulty choice. */
    fun clearSelection() {
        soundManager.play(SoundType.CLICK)
        _uiState.update { it.copy(selectedCategory = null, selectedDifficulty = DifficultyFilter.ANY) }
    }

    fun selectDifficulty(filter: DifficultyFilter) {
        soundManager.play(SoundType.CLICK)
        _uiState.update { it.copy(selectedDifficulty = filter) }
    }

    fun playClick() {
        soundManager.play(SoundType.CLICK)
    }

    override fun onCleared() {
        soundManager.release()
    }
}
