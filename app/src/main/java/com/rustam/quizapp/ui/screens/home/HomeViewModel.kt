package com.rustam.quizapp.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.audio.SoundManager
import com.rustam.quizapp.audio.SoundResources
import com.rustam.quizapp.audio.SoundType
import com.rustam.quizapp.data.Category
import com.rustam.quizapp.data.DailyRewardRepository
import com.rustam.quizapp.data.DailyRewardState
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.data.MistakesRepository
import com.rustam.quizapp.data.PlayerRepository
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.SettingsRepository
import com.rustam.quizapp.data.StreakRepository
import com.rustam.quizapp.domain.QuizEventProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Difficulty options offered on the home screen. [ANY] maps to "no filter" (null);
 * [ADAPTIVE] also has no fixed difficulty but tells the quiz to build a mix tuned to
 * the player's accuracy in the chosen category.
 */
enum class DifficultyFilter(val emoji: String, val difficulty: Difficulty?) {
    EASY("🌱", Difficulty.EASY),
    MEDIUM("⚡", Difficulty.MEDIUM),
    HARD("🔥", Difficulty.HARD),
    ANY("🎲", null),
    ADAPTIVE("🧭", null);

    val isAdaptive: Boolean get() = this == ADAPTIVE
}

data class HomeUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val selectedDifficulty: DifficultyFilter = DifficultyFilter.ANY,
    val events: List<QuizEventProgress> = emptyList(),
    val streak: Int = 0,
    val dailyReward: DailyRewardState = DailyRewardState(),
    val mistakesCount: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository(application)
    private val playerRepository = PlayerRepository(application, repository)
    private val streakRepository = StreakRepository(application)
    private val dailyRewardRepository = DailyRewardRepository(application)
    private val mistakesRepository = MistakesRepository(application)
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
        playerRepository.observeProfile(),
        streakRepository.observeStreak(),
        dailyRewardRepository.observeReward(),
        mistakesRepository.count
    ) { nav, profile, streak, dailyReward, mistakesCount ->
        HomeUiState(
            categories = nav.categories,
            selectedCategory = nav.selectedCategory,
            selectedDifficulty = nav.selectedDifficulty,
            events = profile.eventProgress,
            streak = streak.current,
            dailyReward = dailyReward,
            mistakesCount = mistakesCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /** Claims today's daily reward and credits the coins. */
    fun claimDailyReward() {
        viewModelScope.launch {
            val reward = dailyRewardRepository.claim()
            if (reward > 0) {
                playerRepository.addCoins(reward)
                soundManager.play(SoundType.COMPLETE)
            }
        }
    }

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