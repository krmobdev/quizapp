package com.rustam.quizapp.ui.screens.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.audio.SoundManager
import com.rustam.quizapp.audio.SoundResources
import com.rustam.quizapp.audio.SoundType
import com.rustam.quizapp.data.AppLanguage
import com.rustam.quizapp.data.DailyQuestRepository
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.data.Question
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.QuizProgressRepository
import com.rustam.quizapp.data.SavedQuizProgress
import com.rustam.quizapp.data.SettingsRepository
import com.rustam.quizapp.data.PlayerRepository
import com.rustam.quizapp.data.StatsRepository
import com.rustam.quizapp.data.StreakRepository
import com.rustam.quizapp.data.AchievementsRepository
import com.rustam.quizapp.domain.Achievement
import com.rustam.quizapp.domain.AchievementEvaluator
import com.rustam.quizapp.domain.AdaptiveDifficulty
import com.rustam.quizapp.domain.PowerUpType
import com.rustam.quizapp.domain.QuizEventType
import com.rustam.quizapp.domain.QuizReward
import com.rustam.quizapp.domain.QuizResult
import com.rustam.quizapp.domain.QuizSession
import com.rustam.quizapp.domain.ShopCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val DEFAULT_QUESTION_TIME_SECONDS = 10

data class QuizUiState(
    val isLoading: Boolean = true,
    val question: Question? = null,
    val questionNumber: Int = 0,
    val totalQuestions: Int = 0,
    val selectedAnswer: Int? = null,
    val showExplanation: Boolean = false,
    val isTimeout: Boolean = false,
    val isFinished: Boolean = false,
    val correctCount: Int = 0,
    val penaltyCount: Int = 0,
    val timeLeftSeconds: Int = DEFAULT_QUESTION_TIME_SECONDS,
    val questionTimeSeconds: Int = DEFAULT_QUESTION_TIME_SECONDS,
    val resumePrompt: SavedQuizProgress? = null,
    /** Power-up id -> owned count, for the in-quiz power-up bar. */
    val powerUpCounts: Map<String, Int> = emptyMap(),
    /** Option indices removed by the 50/50 power-up on the current question. */
    val hiddenOptions: Set<Int> = emptySet()
) {
    val isAnswered: Boolean get() = selectedAnswer != null || isTimeout

    val score: Int get() = correctCount - penaltyCount

    val progress: Float
        get() = if (totalQuestions == 0) 0f else questionNumber.toFloat() / totalQuestions

    val timerProgress: Float
        get() = if (questionTimeSeconds == 0) 0f
        else timeLeftSeconds.toFloat() / questionTimeSeconds
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository(application)
    private val statsRepository = StatsRepository(application)
    private val playerRepository = PlayerRepository(application, repository)
    private val streakRepository = StreakRepository(application)
    private val achievementsRepository = AchievementsRepository(application)
    private val achievementEvaluator = AchievementEvaluator(
        statsRepository = statsRepository,
        streakRepository = streakRepository,
        playerRepository = playerRepository,
        achievementsRepository = achievementsRepository,
        questionRepository = repository
    )
    private val progressRepository = QuizProgressRepository(application)
    private val dailyQuestRepository = DailyQuestRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val soundManager = SoundManager(
        context = application,
        sounds = SoundResources.load(application),
        soundEnabled = settingsRepository.soundEnabled,
        scope = viewModelScope
    )
    private var session: QuizSession? = null
    private var categoryId: String? = null
    private var difficulty: Difficulty? = null
    private var eventType: QuizEventType? = null
    private var quizLanguage: AppLanguage = AppLanguage.RU
    private var questionTimeSeconds: Int = DEFAULT_QUESTION_TIME_SECONDS
    private var baseQuestionTimeSeconds: Int = DEFAULT_QUESTION_TIME_SECONDS
    private var questionCount: Int = QuestionRepository.QUIZ_SIZE
    private var adaptive: Boolean = false
    // Guards against re-preparing the quiz on every fresh composition (e.g. screen rotation):
    // the ViewModel survives configuration changes, so the in-progress run and its ticking timer
    // must be preserved instead of being rebuilt by a second prepare() call.
    private var hasPrepared = false
    /** Prevents finishing the same quiz twice when Next is tapped rapidly on the last question. */
    private var resultRecorded = false
    private var timerJob: Job? = null
    private var lastReward: QuizReward? = null
    private var lastNewAchievements: List<Achievement> = emptyList()

    /**
     * Latest owned power-up counts from the inventory. Kept in a field so that the value
     * survives whenever the UI state is rebuilt from scratch (prepare/startNew/continueSaved):
     * the inventory Flow only re-emits when the counts change, so a freshly built state must
     * seed itself from here instead of resetting to an empty map.
     */
    @Volatile
    private var powerUpCounts: Map<String, Int> = emptyMap()

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        // Keep the in-quiz power-up counts in sync with the player's inventory.
        viewModelScope.launch {
            playerRepository.observePowerUps().collect { counts ->
                powerUpCounts = counts
                _uiState.update { it.copy(powerUpCounts = counts) }
            }
        }
    }

    private companion object {
        const val ADD_TIME_SECONDS = 5
    }

    /**
     * Checks for a saved run matching [categoryId]/[difficulty]/[eventType]. Shows a resume prompt
     * when found; otherwise starts a new quiz. [preset] (retry) always starts fresh.
     */
    fun prepare(
        categoryId: String,
        difficulty: Difficulty?,
        eventType: QuizEventType? = null,
        questionTimeSeconds: Int = DEFAULT_QUESTION_TIME_SECONDS,
        questionCount: Int = QuestionRepository.QUIZ_SIZE,
        adaptive: Boolean = false
    ) {
        // Only prepare once per ViewModel; a rotation re-runs this from the new composition but the
        // existing session/timer in this surviving ViewModel must not be reset.
        if (hasPrepared) return
        hasPrepared = true
        this.eventType = eventType
        this.questionTimeSeconds = questionTimeSeconds
        this.baseQuestionTimeSeconds = questionTimeSeconds
        this.questionCount = questionCount
        this.adaptive = adaptive
        viewModelScope.launch {
            val language = settingsRepository.appLanguage.first()
            val saved = progressRepository.savedProgress.first()
            if (saved != null &&
                saved.categoryId == categoryId &&
                saved.difficulty == difficulty &&
                saved.language == language &&
                saved.eventType == eventType
            ) {
                _uiState.value = QuizUiState(
                    isLoading = false,
                    resumePrompt = saved,
                    questionTimeSeconds = saved.questionTimeSeconds,
                    powerUpCounts = powerUpCounts
                )
            } else {
                startNew(categoryId, difficulty)
            }
        }
    }

    fun continueSaved() {
        val saved = _uiState.value.resumePrompt ?: return
        categoryId = saved.categoryId
        difficulty = saved.difficulty
        eventType = saved.eventType
        quizLanguage = saved.language
        questionTimeSeconds = saved.questionTimeSeconds
        questionCount = saved.questions.size
        val restored = QuizSession(
            questions = saved.questions,
            startIndex = saved.currentIndex,
            initialCorrect = saved.correctCount,
            initialPenalties = saved.penaltyCount,
            initialMistakes = saved.mistakes,
            initialAnswerRewards = saved.answerRewards
        )
        session = restored
        _uiState.value = QuizUiState(
            isLoading = false,
            question = restored.currentQuestion(),
            questionNumber = restored.currentIndex + 1,
            totalQuestions = restored.total,
            correctCount = restored.correctCount,
            penaltyCount = restored.penaltyCount,
            selectedAnswer = saved.selectedAnswer,
            showExplanation = saved.showExplanation,
            isTimeout = saved.isTimeout,
            questionTimeSeconds = questionTimeSeconds,
            timeLeftSeconds = if (saved.showExplanation) 0 else questionTimeSeconds,
            powerUpCounts = powerUpCounts
        )
        if (!saved.showExplanation) startTimer()
    }

    fun startNewGame() {
        val saved = _uiState.value.resumePrompt ?: return
        viewModelScope.launch {
            progressRepository.clear()
            startNew(saved.categoryId, saved.difficulty)
        }
    }

    private fun startNew(
        categoryId: String,
        difficulty: Difficulty?
    ) {
        this.categoryId = categoryId
        this.difficulty = difficulty
        viewModelScope.launch {
            progressRepository.clear()
            quizLanguage = settingsRepository.appLanguage.first()
            
            val profile = playerRepository.observeProfile().first()
            val extraSeconds = profile.stats.extraTimeSeconds +
                profile.skillTree.extraTimeSeconds + profile.talentTree.extraTimeSeconds
            this@QuizViewModel.questionTimeSeconds = (this@QuizViewModel.baseQuestionTimeSeconds + extraSeconds).toInt()

            val recentIds = playerRepository.getRecentQuestions(categoryId).first()
            val accuracy = if (adaptive) categoryAccuracy(categoryId) else null

            val questions = withContext(Dispatchers.IO) {
                when {
                    adaptive -> repository.getAdaptiveQuestions(
                        categoryId = categoryId,
                        counts = AdaptiveDifficulty.mix(accuracy, questionCount),
                        language = quizLanguage,
                        excludeIds = recentIds
                    )

                    else -> repository.getQuestions(
                        categoryId = categoryId,
                        difficulty = difficulty,
                        language = quizLanguage,
                        quizSize = questionCount,
                        excludeIds = recentIds
                    )
                }
            }
            applySession(QuizSession(questions))
        }
    }

    private fun applySession(newSession: QuizSession) {
        session = newSession
        if (newSession.questions.isEmpty()) {
            _uiState.value = QuizUiState(
                isLoading = false,
                isFinished = true,
                questionTimeSeconds = questionTimeSeconds,
                powerUpCounts = powerUpCounts
            )
        } else {
            _uiState.value = QuizUiState(
                isLoading = false,
                question = newSession.currentQuestion(),
                questionNumber = newSession.currentIndex + 1,
                totalQuestions = newSession.total,
                questionTimeSeconds = questionTimeSeconds,
                timeLeftSeconds = questionTimeSeconds,
                powerUpCounts = powerUpCounts
            )
            startTimer()
        }
    }

    fun selectAnswer(index: Int) {
        val current = session ?: return
        if (_uiState.value.isAnswered) return
        val elapsed = questionTimeSeconds - _uiState.value.timeLeftSeconds
        stopTimer()
        val isCorrect = current.submitAnswer(index, elapsed)
        soundManager.play(if (isCorrect) SoundType.CORRECT else SoundType.INCORRECT)
        _uiState.update {
            it.copy(
                selectedAnswer = index,
                showExplanation = true,
                correctCount = current.correctCount,
                penaltyCount = current.penaltyCount,
                timeLeftSeconds = 0
            )
        }
    }

    fun next() {
        val current = session ?: return
        stopTimer()
        current.nextQuestion()
        if (current.isFinished()) {
            if (!resultRecorded) {
                resultRecorded = true
                viewModelScope.launch {
                    progressRepository.clear()
                    lastReward = recordResult(current)
                    soundManager.play(SoundType.COMPLETE)
                    _uiState.update { it.copy(isFinished = true) }
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    question = current.currentQuestion(),
                    questionNumber = current.currentIndex + 1,
                    selectedAnswer = null,
                    showExplanation = false,
                    isTimeout = false,
                    correctCount = current.correctCount,
                    penaltyCount = current.penaltyCount,
                    timeLeftSeconds = questionTimeSeconds,
                    hiddenOptions = emptySet()
                )
            }
            startTimer()
        }
    }

    /** 50/50: spend one power-up to hide two wrong options on the current question. */
    fun useFiftyFifty() {
        val state = _uiState.value
        val question = session?.currentQuestion() ?: return
        if (state.isAnswered || state.hiddenOptions.isNotEmpty()) return
        val id = powerUpId(PowerUpType.FIFTY_FIFTY) ?: return
        viewModelScope.launch {
            if (playerRepository.consumePowerUp(id)) {
                val wrong = question.options.indices.filter { it != question.correctIndex }
                val toHide = wrong.shuffled().take(2).toSet()
                _uiState.update { it.copy(hiddenOptions = toHide) }
                soundManager.play(SoundType.CLICK)
            }
        }
    }

    /** +Time: spend one power-up to add seconds to the current question timer. */
    fun useAddTime() {
        val id = powerUpId(PowerUpType.ADD_TIME) ?: return
        // Apply the extra time synchronously (and optimistically drop the count) so it can't
        // lose the race with an expiring timer — otherwise the question can time out while the
        // inventory write is in flight, which looks like the question was skipped. The actual
        // inventory write happens in the background and reconciles the count via its Flow.
        var applied = false
        _uiState.update { state ->
            val count = state.powerUpCounts[id] ?: 0
            if (state.isAnswered || count <= 0) {
                state
            } else {
                applied = true
                state.copy(
                    timeLeftSeconds = state.timeLeftSeconds + ADD_TIME_SECONDS,
                    powerUpCounts = state.powerUpCounts + (id to count - 1)
                )
            }
        }
        if (!applied) return
        soundManager.play(SoundType.CLICK)
        viewModelScope.launch { playerRepository.consumePowerUp(id) }
    }

    /** Skip: spend one power-up to move to the next question with no penalty (and no points). */
    fun useSkip() {
        if (_uiState.value.isAnswered) return
        if (session == null) return
        val id = powerUpId(PowerUpType.SKIP) ?: return
        viewModelScope.launch {
            if (playerRepository.consumePowerUp(id)) {
                soundManager.play(SoundType.CLICK)
                next()
            }
        }
    }

    private fun powerUpId(type: PowerUpType): String? =
        ShopCatalog.powerUps.find { it.type == type }?.id

    /** Persists the current run and invokes [onDone] when written. */
    fun saveAndExit(onDone: () -> Unit) {
        val current = session ?: run { onDone(); return }
        if (_uiState.value.isFinished) {
            onDone()
            return
        }
        val state = _uiState.value
        val saved = SavedQuizProgress(
            categoryId = categoryId.orEmpty(),
            difficulty = difficulty,
            language = quizLanguage,
            questions = current.questions,
            currentIndex = current.currentIndex,
            correctCount = current.correctCount,
            penaltyCount = current.penaltyCount,
            mistakes = current.mistakes,
            answerRewards = current.answerRewards,
            selectedAnswer = state.selectedAnswer,
            showExplanation = state.showExplanation,
            isTimeout = state.isTimeout,
            eventType = eventType,
            questionTimeSeconds = questionTimeSeconds
        )
        stopTimer()
        viewModelScope.launch {
            progressRepository.save(saved)
            onDone()
        }
    }

    fun currentResult(): QuizResult {
        val current = session
        return QuizResult(
            correct = _uiState.value.correctCount,
            total = _uiState.value.totalQuestions,
            penalties = _uiState.value.penaltyCount,
            mistakes = current?.mistakes ?: emptyList(),
            reward = lastReward,
            newAchievements = lastNewAchievements
        )
    }

    private fun startTimer() {
        stopTimer()
        // Drives off the live timeLeftSeconds in state so the +time power-up can extend it.
        timerJob = viewModelScope.launch {
            while (isActive) {
                if (_uiState.value.timeLeftSeconds <= 0) {
                    onTimeout()
                    break
                }
                delay(1_000)
                _uiState.update { it.copy(timeLeftSeconds = (it.timeLeftSeconds - 1).coerceAtLeast(0)) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun onTimeout() {
        val current = session ?: return
        if (_uiState.value.isAnswered) return
        current.submitTimeout(questionTimeSeconds)
        soundManager.play(SoundType.INCORRECT)
        _uiState.update {
            it.copy(
                isTimeout = true,
                showExplanation = true,
                penaltyCount = current.penaltyCount,
                correctCount = current.correctCount,
                timeLeftSeconds = 0
            )
        }
    }

    private suspend fun recordResult(session: QuizSession): QuizReward? {
        val category = categoryId ?: return null

        statsRepository.recordQuizResult(category, session.correctCount, session.total)
        playerRepository.saveRecentQuestions(category, session.questions.map { it.id })

        val reward = playerRepository.grantQuizReward(
            categoryId = category,
            difficulty = difficulty,
            answers = session.answerRewards,
            total = session.total,
            eventType = eventType,
            allowEventBonus = true
        )

        dailyQuestRepository.recordQuizFinished(
            correct = session.correctCount,
            total = session.total,
            coinsEarned = reward.coins
        )

        // Update the day streak first so streak-based achievements see the fresh value,
        // then unlock any newly earned achievements (which may grant bonus coins).
        streakRepository.recordPlayed()
        lastNewAchievements = achievementEvaluator.evaluate()

        return reward
    }

    /** Lifetime accuracy (0..100) in [categoryId], or null when the category has no history yet. */
    private suspend fun categoryAccuracy(categoryId: String): Int? {
        val stats = statsRepository.observeStats().first()
        val category = stats.categories.find { it.categoryId == categoryId } ?: return null
        return if (category.questionsAnswered > 0) {
            category.correctAnswers * 100 / category.questionsAnswered
        } else null
    }

    override fun onCleared() {
        stopTimer()
        soundManager.release()
    }

}