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
import com.rustam.quizapp.domain.CharacterLevelCalculator
import com.rustam.quizapp.domain.GemEconomy
import com.rustam.quizapp.domain.QuizEventProgress
import com.rustam.quizapp.domain.SeasonPass
import kotlin.math.roundToInt
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
    val seasonDaysLeft: Int = 30,
    /** Current player level — used to preview quest reward scaling in the UI. */
    val playerLevel: Int = 1
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
            seasonDaysLeft = profile.seasonDaysLeft,
            playerLevel = CharacterLevelCalculator.calculateLevel(
                profile.lifetimePoints, profile.bankedLifetimePoints
            )
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(
        coreState,
        dailyQuestRepository.observe()
    ) { core, quests ->
        core.copy(dailyQuests = quests.challenges)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /** Claims today's daily reward and credits the coins plus any milestone-day gems. */
    fun claimDailyReward() {
        viewModelScope.launch {
            val reward = dailyRewardRepository.claim()
            if (reward.coins > 0 || reward.gems > 0) {
                playerRepository.addCoins(reward.coins)
                playerRepository.addGems(reward.gems)
                soundManager.play(SoundType.COMPLETE)
            }
        }
    }

    /** Claims the daily quest at [index] (if complete) and credits its coin + XP reward.
     *  Reward is scaled by [CharacterLevelCalculator.questRewardMultiplier] so higher-level
     *  players receive meaningfully larger payouts for the same tasks. Claiming the final quest
     *  of the day also pays the one-time [GemEconomy.ALL_QUESTS_GEMS] bonus. */
    fun claimDailyQuest(index: Int) {
        viewModelScope.launch {
            val challenge = dailyQuestRepository.claim(index)
            if (challenge != null) {
                val level = playerRepository.getPlayerLevel()
                val mult = CharacterLevelCalculator.questRewardMultiplier(level)
                val scaledCoins = (challenge.rewardCoins * mult).roundToInt()
                val scaledXp = (challenge.rewardXp * mult).roundToInt()
                playerRepository.addCoins(scaledCoins)
                playerRepository.addXp(scaledXp)
                if (dailyQuestRepository.claimAllCompleteBonus()) {
                    playerRepository.addGems(GemEconomy.ALL_QUESTS_GEMS)
                }
                soundManager.play(SoundType.COMPLETE)
            }
        }
    }

    fun selectCategory(category: Category) {
        navigationState.update { it.copy(selectedCategory = category) }
    }

    fun clearSelection() {
        navigationState.update {
            it.copy(selectedCategory = null, selectedDifficulty = DifficultyFilter.ANY)
        }
    }

    fun selectDifficulty(filter: DifficultyFilter) {
        navigationState.update { it.copy(selectedDifficulty = filter) }
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