package com.rustam.quizapp.ui.screens.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.audio.SoundManager
import com.rustam.quizapp.audio.SoundResources
import com.rustam.quizapp.audio.SoundType
import com.rustam.quizapp.data.AppLanguage
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.data.Question
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.QuizProgressRepository
import com.rustam.quizapp.data.SavedQuizProgress
import com.rustam.quizapp.data.SettingsRepository
import com.rustam.quizapp.data.StatsRepository
import com.rustam.quizapp.domain.QuizResult
import com.rustam.quizapp.domain.QuizSession
import com.rustam.quizapp.ui.screens.quiz.QuizViewModel.Companion.QUESTION_TIME_SECONDS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val timeLeftSeconds: Int = QUESTION_TIME_SECONDS,
    val resumePrompt: SavedQuizProgress? = null
) {
    val isAnswered: Boolean get() = selectedAnswer != null || isTimeout

    val score: Int get() = correctCount - penaltyCount

    val progress: Float
        get() = if (totalQuestions == 0) 0f else questionNumber.toFloat() / totalQuestions

    val timerProgress: Float
        get() = timeLeftSeconds.toFloat() / QUESTION_TIME_SECONDS
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository(application)
    private val statsRepository = StatsRepository(application)
    private val progressRepository = QuizProgressRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val soundManager = SoundManager(
        context = application,
        sounds = SoundResources.load(application),
        soundEnabled = SettingsRepository(application).soundEnabled,
        scope = viewModelScope
    )
    private var session: QuizSession? = null
    private var categoryId: String? = null
    private var difficulty: Difficulty? = null
    private var quizLanguage: AppLanguage = AppLanguage.RU
    private var timerJob: Job? = null

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    /**
     * Checks for a saved run matching [categoryId]/[difficulty]. Shows a resume prompt
     * when found; otherwise starts a new quiz. [preset] (retry) always starts fresh.
     */
    fun prepare(
        categoryId: String,
        difficulty: Difficulty?,
        preset: List<Question>? = null
    ) {
        if (preset != null) {
            startNew(categoryId, difficulty, preset)
            return
        }
        viewModelScope.launch {
            val language = settingsRepository.appLanguage.first()
            val saved = progressRepository.savedProgress.first()
            if (saved != null &&
                saved.categoryId == categoryId &&
                saved.difficulty == difficulty &&
                saved.language == language
            ) {
                _uiState.value = QuizUiState(
                    isLoading = false,
                    resumePrompt = saved
                )
            } else {
                startNew(categoryId, difficulty, preset = null)
            }
        }
    }

    fun continueSaved() {
        val saved = _uiState.value.resumePrompt ?: return
        categoryId = saved.categoryId
        difficulty = saved.difficulty
        quizLanguage = saved.language
        val restored = QuizSession(
            questions = saved.questions,
            startIndex = saved.currentIndex,
            initialCorrect = saved.correctCount,
            initialPenalties = saved.penaltyCount,
            initialMistakes = saved.mistakes
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
            timeLeftSeconds = if (saved.showExplanation) 0 else QUESTION_TIME_SECONDS
        )
        if (!saved.showExplanation) startTimer()
    }

    fun startNewGame() {
        val saved = _uiState.value.resumePrompt ?: return
        viewModelScope.launch {
            progressRepository.clear()
            startNew(saved.categoryId, saved.difficulty, preset = null)
        }
    }

    private fun startNew(
        categoryId: String,
        difficulty: Difficulty?,
        preset: List<Question>?
    ) {
        this.categoryId = categoryId
        this.difficulty = difficulty
        viewModelScope.launch {
            progressRepository.clear()
            quizLanguage = settingsRepository.appLanguage.first()
            val questions = preset ?: withContext(Dispatchers.IO) {
                repository.getQuestions(categoryId, difficulty, quizLanguage)
            }
            applySession(QuizSession(questions))
        }
    }

    private fun applySession(newSession: QuizSession) {
        session = newSession
        if (newSession.questions.isEmpty()) {
            _uiState.value = QuizUiState(isLoading = false, isFinished = true)
        } else {
            _uiState.value = QuizUiState(
                isLoading = false,
                question = newSession.currentQuestion(),
                questionNumber = newSession.currentIndex + 1,
                totalQuestions = newSession.total,
                timeLeftSeconds = QUESTION_TIME_SECONDS
            )
            startTimer()
        }
    }

    fun selectAnswer(index: Int) {
        val current = session ?: return
        if (_uiState.value.isAnswered) return
        stopTimer()
        val isCorrect = current.submitAnswer(index)
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
            if (!_uiState.value.isFinished) {
                _uiState.update { it.copy(isFinished = true) }
                soundManager.play(SoundType.COMPLETE)
                viewModelScope.launch { progressRepository.clear() }
                recordResult(current)
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
                    timeLeftSeconds = QUESTION_TIME_SECONDS
                )
            }
            startTimer()
        }
    }

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
            selectedAnswer = state.selectedAnswer,
            showExplanation = state.showExplanation,
            isTimeout = state.isTimeout
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
            mistakes = current?.mistakes ?: emptyList()
        )
    }

    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            for (seconds in QUESTION_TIME_SECONDS downTo 0) {
                _uiState.update { it.copy(timeLeftSeconds = seconds) }
                if (seconds == 0) {
                    onTimeout()
                    break
                }
                delay(1_000)
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
        current.submitTimeout()
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

    private fun recordResult(session: QuizSession) {
        val category = categoryId ?: return
        viewModelScope.launch {
            statsRepository.recordQuizResult(category, session.correctCount, session.total)
        }
    }

    override fun onCleared() {
        stopTimer()
        soundManager.release()
    }

    companion object {
        const val QUESTION_TIME_SECONDS = 10
    }
}