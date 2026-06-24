package com.rustam.quizapp.domain

import com.rustam.quizapp.data.Question

/**
 * Holds the state of a single quiz run: the question list, the current position,
 * the number of correct answers, timeout penalties and mistakes.
 */
class QuizSession(
    val questions: List<Question>,
    startIndex: Int = 0,
    initialCorrect: Int = 0,
    initialPenalties: Int = 0,
    initialMistakes: List<Question> = emptyList(),
    initialAnswerRewards: List<AnswerReward> = emptyList()
) {

    var currentIndex: Int = startIndex
        private set

    var correctCount: Int = initialCorrect
        private set

    var penaltyCount: Int = initialPenalties
        private set

    private val _mistakes = initialMistakes.toMutableList()
    val mistakes: List<Question> get() = _mistakes

    private val _answerRewards = initialAnswerRewards.toMutableList()
    val answerRewards: List<AnswerReward> get() = _answerRewards

    val total: Int get() = questions.size

    val score: Int get() = correctCount - penaltyCount

    fun currentQuestion(): Question? = questions.getOrNull(currentIndex)

    fun submitAnswer(selectedIndex: Int, elapsedSeconds: Int): Boolean {
        val question = currentQuestion() ?: return false
        val isCorrect = selectedIndex == question.correctIndex
        if (isCorrect) {
            correctCount++
        } else {
            _mistakes.add(question)
        }
        _answerRewards.add(AnswerReward(isCorrect = isCorrect, elapsedSeconds = elapsedSeconds))
        return isCorrect
    }

    /** Time ran out — counts as a mistake and applies a −1 penalty. */
    fun submitTimeout(questionTimeSeconds: Int) {
        val question = currentQuestion() ?: return
        _mistakes.add(question)
        penaltyCount++
        _answerRewards.add(
            AnswerReward(isCorrect = false, elapsedSeconds = questionTimeSeconds)
        )
    }

    fun nextQuestion() {
        if (!isFinished()) currentIndex++
    }

    fun isFinished(): Boolean = currentIndex >= questions.size
}