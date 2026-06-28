package com.rustam.quizapp.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.audio.SoundManager
import com.rustam.quizapp.audio.SoundResources
import com.rustam.quizapp.audio.SoundType
import com.rustam.quizapp.data.Category
import com.rustam.quizapp.data.DailyChallengeProgress
import com.rustam.quizapp.data.DailyQuestRepository
import com.rustam.quizapp.data.DailyRewardRepository
import com.rustam.quizapp.data.DailyRewardState
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.data.PlayerRepository
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.SettingsRepository
import com.rustam.quizapp.data.StreakRepository
import com.rustam.quizapp.domain.QuizEventProgress
import com.rustam.quizapp.domain.SeasonPass
import kotlinx.coroutines.flow.Flow
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
    /** Remaining quizzes for the active temporary boosts (0 = inactive). */
    val coinBoostLeft: Int = 0,
    val xpBoostLeft: Int = 0,
    val dailyQuests: List<DailyChallengeProgress> = emptyList(),
    val seasonLevel: Int = 0,
    val seasonDaysLeft: Int = 30
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository(application)
    private val playerRepository = PlayerRepository(application, repository)
    private val streakRepository = StreakRepository(application)
    private val dailyRewardRepository = DailyRewardRepository(application)
    private val dailyQuestRepository = DailyQuestRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val soundManager = SoundManager(
        context = application,
        sounds = SoundResources.load(application),
        soundEnabled = settingsRepository.soundEnabled,
        scope = viewModelScope
    )

    private val navigationState = MutableStateFlow(
        NavigationSlice(categories = repository.getCategories())
    )

    private val coreState: Flow<HomeUiState> = combine(
        navigationState,
        playerRepository.observeProfile(),
        streakRepository.observeStreak(),
        dailyRewardRepository.observeReward()
    ) { nav, profile, streak, dailyReward ->
        HomeUiState(
            categories = nav.categories,
            selectedCategory = nav.selectedCategory,
            selectedDifficulty = nav.selectedDifficulty,
            events = profile.eventProgress,
            streak = streak.current,
            dailyReward = dailyReward,
            coinBoostLeft = profile.coinBoostQuizzesLeft,
            xpBoostLeft = profile.xpBoostQuizzesLeft,
            seasonLevel = SeasonPass.level(profile.seasonXp),
            seasonDaysLeft = profile.seasonDaysLeft
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(
        coreState,
        dailyQuestRepository.observe()
    ) { core, quests ->
        core.copy(dailyQuests = quests.challenges)
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

    /** Claims the daily quest at [index] (if complete) and credits its coin + XP reward. */
    fun claimDailyQuest(index: Int) {
        viewModelScope.launch {
            val challenge = dailyQuestRepository.claim(index)
            if (challenge != null) {
                playerRepository.addCoins(challenge.rewardCoins)
                playerRepository.addXp(challenge.rewardXp)
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