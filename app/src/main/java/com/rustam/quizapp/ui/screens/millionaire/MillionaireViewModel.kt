package com.rustam.quizapp.ui.screens.millionaire

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.R
import com.rustam.quizapp.audio.SoundManager
import com.rustam.quizapp.audio.SoundResources
import com.rustam.quizapp.audio.SoundType
import com.rustam.quizapp.data.PlayerRepository
import com.rustam.quizapp.data.Question
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.SettingsRepository
import com.rustam.quizapp.domain.Lifeline
import com.rustam.quizapp.domain.MillionaireCatalog
import com.rustam.quizapp.domain.MillionaireLadder
import com.rustam.quizapp.domain.MillionaireLifelines
import com.rustam.quizapp.domain.MillionairePack
import com.rustam.quizapp.domain.PackCurrency
import com.rustam.quizapp.domain.PhoneFriendHint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MillionairePhase { CATALOG, PLAYING, OUTCOME }

data class LifelineState(
    val fiftyFiftyUsed: Boolean = false,
    val phoneUsed: Boolean = false,
    val audienceUsed: Boolean = false
)

data class MillionaireUiState(
    val phase: MillionairePhase = MillionairePhase.CATALOG,
    val loading: Boolean = false,
    val packs: List<MillionairePack> = emptyList(),
    val balanceXp: Int = 0,
    val balanceCoins: Int = 0,
    val balanceGems: Int = 0,
    // Active run:
    val currency: PackCurrency = PackCurrency.COINS,
    val prize: Int = 0,
    val question: Question? = null,
    val rungNumber: Int = 0,
    val clearedRungs: Int = 0,
    val currentAmount: Int = 0,
    val nextAmount: Int = 0,
    val guaranteedAmount: Int = 0,
    val hiddenOptions: Set<Int> = emptySet(),
    val phoneHint: PhoneFriendHint? = null,
    val audienceHint: List<Int>? = null,
    val lifelines: LifelineState = LifelineState(),
    val selectedAnswer: Int? = null,
    val revealed: Boolean = false,
    val lastAnswerCorrect: Boolean = false,
    // Outcome:
    val outcomeAmount: Int = 0,
    val outcomeWon: Boolean = false,
    val outcomeAllCorrect: Boolean = false
) {
    /** Whether the player can act on the current question (answer / lifeline / cash out). */
    val canAct: Boolean get() = phase == MillionairePhase.PLAYING && !revealed && question != null
}

class MillionaireViewModel(application: Application) : AndroidViewModel(application) {

    private val questionRepository = QuestionRepository(application)
    private val playerRepository = PlayerRepository(application, questionRepository)
    private val settingsRepository = SettingsRepository(application)
    private val soundManager = SoundManager(
        context = application,
        sounds = SoundResources.load(application),
        soundEnabled = settingsRepository.soundEnabled,
        scope = viewModelScope
    )

    private var questions: List<Question> = emptyList()
    private var index = 0
    private var activePack: MillionairePack? = null

    private val _uiState = MutableStateFlow(MillionaireUiState())
    val uiState: StateFlow<MillionaireUiState> = _uiState.asStateFlow()

    init {
        val packs = buildPacks()
        _uiState.update { it.copy(packs = packs) }
        viewModelScope.launch {
            playerRepository.observeProfile().collect { profile ->
                _uiState.update {
                    it.copy(
                        balanceXp = profile.points,
                        balanceCoins = profile.coins,
                        balanceGems = profile.gems
                    )
                }
            }
        }
    }

    private fun buildPacks(): List<MillionairePack> {
        val categories = questionRepository.getCategories()
        return PackCurrency.entries.flatMap { currency ->
            val general = MillionairePack(
                id = MillionaireCatalog.packId(currency, null),
                currency = currency,
                scope = null,
                cost = MillionaireCatalog.cost(currency, isCategory = false),
                prize = MillionaireCatalog.prize(currency),
                emoji = "🎲",
                labelRes = R.string.millionaire_all_categories
            )
            val perCategory = categories.map { category ->
                MillionairePack(
                    id = MillionaireCatalog.packId(currency, category.id),
                    currency = currency,
                    scope = category.id,
                    cost = MillionaireCatalog.cost(currency, isCategory = true),
                    prize = MillionaireCatalog.prize(currency),
                    emoji = category.emoji,
                    labelRes = category.titleRes
                )
            }
            listOf(general) + perCategory
        }
    }

    fun buyAndStart(packId: String) {
        if (_uiState.value.phase == MillionairePhase.PLAYING) return
        val pack = _uiState.value.packs.find { it.id == packId } ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            if (!playerRepository.payPackEntry(pack.currency, pack.cost)) {
                _uiState.update { it.copy(loading = false) }
                return@launch
            }
            val language = settingsRepository.appLanguage.first()
            val loaded = withContext(Dispatchers.IO) {
                questionRepository.getMillionaireQuestions(
                    categoryId = pack.scope,
                    language = language,
                    easy = MillionaireCatalog.EASY_COUNT,
                    medium = MillionaireCatalog.MEDIUM_COUNT,
                    hard = MillionaireCatalog.HARD_COUNT
                )
            }
            questions = loaded
            index = 0
            activePack = pack
            if (loaded.isEmpty()) {
                // No questions available: refund and bail.
                playerRepository.awardPackPrize(pack.currency, pack.cost)
                _uiState.update { it.copy(loading = false) }
                return@launch
            }
            _uiState.update {
                it.copy(
                    loading = false,
                    phase = MillionairePhase.PLAYING,
                    currency = pack.currency,
                    prize = pack.prize
                )
            }
            showQuestionAt(0)
        }
    }

    private fun showQuestionAt(i: Int) {
        val cleared = i
        val prize = _uiState.value.prize
        _uiState.update {
            it.copy(
                question = questions[i],
                rungNumber = i + 1,
                clearedRungs = cleared,
                currentAmount = MillionaireLadder.amountAt(cleared, prize),
                nextAmount = MillionaireLadder.amountAt(cleared + 1, prize),
                guaranteedAmount = MillionaireLadder.guaranteedAmount(cleared, prize),
                hiddenOptions = emptySet(),
                phoneHint = null,
                audienceHint = null,
                selectedAnswer = null,
                revealed = false,
                lastAnswerCorrect = false
            )
        }
    }

    fun answer(optionIndex: Int) {
        val state = _uiState.value
        if (!state.canAct) return
        val question = state.question ?: return
        val correct = optionIndex == question.correctIndex
        soundManager.play(if (correct) SoundType.CORRECT else SoundType.INCORRECT)
        _uiState.update {
            it.copy(selectedAnswer = optionIndex, revealed = true, lastAnswerCorrect = correct)
        }
    }

    /** Advances after a revealed answer: next rung on a correct answer, otherwise ends the run. */
    fun proceed() {
        val state = _uiState.value
        if (state.phase != MillionairePhase.PLAYING || !state.revealed) return
        if (state.lastAnswerCorrect) {
            val cleared = state.clearedRungs + 1
            if (cleared >= MillionaireLadder.LENGTH) {
                finish(MillionaireLadder.amountAt(MillionaireLadder.LENGTH, state.prize), won = true, allCorrect = true)
            } else {
                index = cleared
                showQuestionAt(index)
            }
        } else {
            val guaranteed = MillionaireLadder.guaranteedAmount(state.clearedRungs, state.prize)
            finish(guaranteed, won = guaranteed > 0, allCorrect = false)
        }
    }

    /** Voluntarily banks the current winnings and ends the run. */
    fun cashOut() {
        val state = _uiState.value
        if (state.phase != MillionairePhase.PLAYING || state.revealed) return
        finish(state.currentAmount, won = true, allCorrect = false)
    }

    /** Leaving mid-run banks only the guaranteed safe amount (less than a voluntary cash-out). */
    fun leaveRun() {
        val state = _uiState.value
        if (state.phase != MillionairePhase.PLAYING) return
        val guaranteed = MillionaireLadder.guaranteedAmount(state.clearedRungs, state.prize)
        finish(guaranteed, won = guaranteed > 0, allCorrect = false)
    }

    private fun finish(amount: Int, won: Boolean, allCorrect: Boolean) {
        val currency = _uiState.value.currency
        viewModelScope.launch {
            playerRepository.awardPackPrize(currency, amount)
            soundManager.play(if (won) SoundType.COMPLETE else SoundType.INCORRECT)
            _uiState.update {
                it.copy(
                    phase = MillionairePhase.OUTCOME,
                    revealed = false,
                    outcomeAmount = amount,
                    outcomeWon = won,
                    outcomeAllCorrect = allCorrect
                )
            }
        }
    }

    fun useLifeline(type: Lifeline) {
        val state = _uiState.value
        if (!state.canAct) return
        val question = state.question ?: return
        when (type) {
            Lifeline.FIFTY_FIFTY -> {
                if (state.lifelines.fiftyFiftyUsed) return
                _uiState.update {
                    it.copy(
                        hiddenOptions = MillionaireLifelines.fiftyFifty(question),
                        lifelines = it.lifelines.copy(fiftyFiftyUsed = true)
                    )
                }
            }
            Lifeline.PHONE_FRIEND -> {
                if (state.lifelines.phoneUsed) return
                _uiState.update {
                    it.copy(
                        phoneHint = MillionaireLifelines.phoneFriend(question),
                        lifelines = it.lifelines.copy(phoneUsed = true)
                    )
                }
            }
            Lifeline.ASK_AUDIENCE -> {
                if (state.lifelines.audienceUsed) return
                _uiState.update {
                    it.copy(
                        audienceHint = MillionaireLifelines.askAudience(question).toList(),
                        lifelines = it.lifelines.copy(audienceUsed = true)
                    )
                }
            }
        }
        soundManager.play(SoundType.CLICK)
    }

    /** Closes the outcome dialog and returns to the pack catalog, resetting the run. */
    fun dismissOutcome() {
        questions = emptyList()
        index = 0
        activePack = null
        _uiState.update {
            MillionaireUiState(
                phase = MillionairePhase.CATALOG,
                packs = it.packs,
                balanceXp = it.balanceXp,
                balanceCoins = it.balanceCoins,
                balanceGems = it.balanceGems
            )
        }
    }

    override fun onCleared() {
        soundManager.release()
    }
}
