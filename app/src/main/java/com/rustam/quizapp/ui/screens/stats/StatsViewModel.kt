package com.rustam.quizapp.ui.screens.stats

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.audio.SoundManager
import com.rustam.quizapp.audio.SoundResources
import com.rustam.quizapp.audio.SoundType
import com.rustam.quizapp.data.AchievementsRepository
import com.rustam.quizapp.data.AppStats
import com.rustam.quizapp.data.PlayerProfile
import com.rustam.quizapp.data.PlayerRepository
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.SettingsRepository
import com.rustam.quizapp.data.StatsRepository
import com.rustam.quizapp.data.StreakRepository
import com.rustam.quizapp.data.StreakState
import com.rustam.quizapp.domain.AchievementEvaluator
import com.rustam.quizapp.domain.Achievements
import com.rustam.quizapp.domain.CharacterStats
import com.rustam.quizapp.domain.QuizEventProgress
import com.rustam.quizapp.domain.SkillTreeState
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

data class AchievementUi(
    val id: String,
    val emoji: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descRes: Int,
    val rewardCoins: Int,
    val unlocked: Boolean,
    val current: Int = 0,
    val target: Int = 1
) {
    val progressFraction: Float
        get() = if (target > 0) (current.toFloat() / target).coerceIn(0f, 1f) else 0f
}

data class PlayerUiState(
    val playerName: String = "",
    val avatarEmoji: String = "🙂",
    val points: Int = 0,
    val coins: Int = 0,
    val events: List<QuizEventProgress> = emptyList(),
    val totalQuizzes: Int = 0,
    val averageAccuracyPercent: Int? = null,
    val categories: List<CategoryStatsUi> = emptyList(),
    val stats: CharacterStats = CharacterStats(),
    val skillTree: SkillTreeState = SkillTreeState(),
    val lifetimePoints: Int = 0,
    val lifetimeCoins: Int = 0,
    val equippedTitleId: String? = null,
    val streakCurrent: Int = 0,
    val streakBest: Int = 0,
    val achievements: List<AchievementUi> = emptyList()
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val questionRepository = QuestionRepository(application)
    private val statsRepository = StatsRepository(application)
    private val playerRepository = PlayerRepository(application, questionRepository)
    private val streakRepository = StreakRepository(application)
    private val achievementsRepository = AchievementsRepository(application)
    private val achievementEvaluator = AchievementEvaluator(
        statsRepository = statsRepository,
        streakRepository = streakRepository,
        playerRepository = playerRepository,
        achievementsRepository = achievementsRepository,
        questionRepository = questionRepository
    )
    private val settingsRepository = SettingsRepository(application)
    private val soundManager = SoundManager(
        context = application,
        sounds = SoundResources.load(application),
        soundEnabled = settingsRepository.soundEnabled,
        scope = viewModelScope
    )

    val uiState: StateFlow<PlayerUiState> = combine(
        statsRepository.observeStats(),
        playerRepository.observeProfile(),
        streakRepository.observeStreak(),
        achievementsRepository.observeUnlocked()
    ) { stats, profile, streak, unlocked ->
        toUiState(stats, profile, streak, unlocked)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerUiState())

    fun updatePlayerName(name: String) {
        viewModelScope.launch {
            playerRepository.setPlayerName(name)
        }
    }

    fun upgradeStat(statName: String) {
        viewModelScope.launch {
            val upgraded = playerRepository.upgradeStat(statName)
            if (upgraded) {
                soundManager.play(SoundType.CLICK)
                // Maxing a characteristic can unlock an achievement.
                achievementEvaluator.evaluate()
            }
        }
    }

    fun upgradeSkill(branchId: String) {
        viewModelScope.launch {
            if (playerRepository.upgradeSkill(branchId)) {
                soundManager.play(SoundType.CLICK)
            }
        }
    }

    override fun onCleared() {
        soundManager.release()
    }

    private fun toUiState(
        stats: AppStats,
        profile: PlayerProfile,
        streak: StreakState,
        unlocked: Set<String>
    ): PlayerUiState {
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

        val metrics = com.rustam.quizapp.domain.AchievementMetrics(
            totalQuizzes = stats.totalQuizzesCompleted,
            totalCorrect = totalCorrect,
            bestStreak = streak.best,
            level = com.rustam.quizapp.domain.CharacterLevelCalculator.calculateLevel(profile.lifetimePoints),
            hasMaxedStat = listOf(
                profile.stats.strength, profile.stats.intelligence, profile.stats.agility,
                profile.stats.luck, profile.stats.wisdom, profile.stats.endurance,
                profile.stats.focus, profile.stats.charisma
            ).any { it >= 20 },
            hasPerfectQuiz = stats.categories.any { it.bestScorePercent >= 100 },
            categoriesPlayed = stats.categories.count { it.quizzesCompleted > 0 },
            totalCategories = questionRepository.getCategories().size
        )

        val achievements = Achievements.all.map { achievement ->
            val (current, target) = achievement.progressOf(metrics)
            AchievementUi(
                id = achievement.id,
                emoji = achievement.emoji,
                titleRes = achievement.titleRes,
                descRes = achievement.descRes,
                rewardCoins = achievement.rewardCoins,
                unlocked = achievement.id in unlocked,
                current = current.coerceAtMost(target),
                target = target
            )
        }

        return PlayerUiState(
            playerName = profile.name,
            avatarEmoji = profile.avatarEmoji,
            points = profile.points,
            coins = profile.coins,
            events = profile.eventProgress,
            totalQuizzes = stats.totalQuizzesCompleted,
            averageAccuracyPercent = averageAccuracy,
            categories = categories,
            stats = profile.stats,
            skillTree = profile.skillTree,
            lifetimePoints = profile.lifetimePoints,
            lifetimeCoins = profile.lifetimeCoins,
            equippedTitleId = profile.equippedTitleId,
            streakCurrent = streak.current,
            streakBest = streak.best,
            achievements = achievements
        )
    }
}