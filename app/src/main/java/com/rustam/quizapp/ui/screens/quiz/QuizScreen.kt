package com.rustam.quizapp.ui.screens.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.R
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.data.Question
import com.rustam.quizapp.domain.QuizResult
import com.rustam.quizapp.ui.theme.CorrectGreen
import com.rustam.quizapp.ui.theme.QuizappTheme
import com.rustam.quizapp.ui.theme.WrongRed

@Composable
fun QuizScreen(
    categoryId: String,
    difficulty: Difficulty?,
    onFinished: (QuizResult) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    retryQuestions: List<Question>? = null,
    viewModel: QuizViewModel = viewModel()
) {
    BackHandler {
        viewModel.saveAndExit(onDone = onBack)
    }

    LaunchedEffect(categoryId, difficulty, retryQuestions) {
        viewModel.prepare(categoryId, difficulty, retryQuestions)
    }
    val state by viewModel.uiState.collectAsState()

    state.resumePrompt?.let { saved ->
        ResumeDialog(
            questionNumber = saved.currentIndex + 1,
            total = saved.questions.size,
            onContinue = viewModel::continueSaved,
            onNewGame = viewModel::startNewGame
        )
    }

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) onFinished(viewModel.currentResult())
    }

    if (state.resumePrompt == null) {
        QuizContent(
            state = state,
            onAnswerSelected = viewModel::selectAnswer,
            onNext = viewModel::next,
            modifier = modifier
        )
    }
}

@Composable
private fun ResumeDialog(
    questionNumber: Int,
    total: Int,
    onContinue: () -> Unit,
    onNewGame: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onContinue,
        title = { Text(stringResource(R.string.resume_title)) },
        text = {
            Text(stringResource(R.string.resume_message, questionNumber, total))
        },
        confirmButton = {
            Button(onClick = onContinue) { Text(stringResource(R.string.resume_continue)) }
        },
        dismissButton = {
            TextButton(onClick = onNewGame) { Text(stringResource(R.string.resume_new_game)) }
        }
    )
}

@Composable
private fun QuizContent(
    state: QuizUiState,
    onAnswerSelected: (Int) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            state.question != null -> QuestionLayout(
                state = state,
                onAnswerSelected = onAnswerSelected,
                onNext = onNext
            )
        }
    }
}

@Composable
private fun QuestionLayout(
    state: QuizUiState,
    onAnswerSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )
    val animatedTimer by animateFloatAsState(
        targetValue = state.timerProgress,
        animationSpec = tween(durationMillis = 300),
        label = "timer"
    )
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    R.string.quiz_question_progress,
                    state.questionNumber,
                    state.totalQuestions
                ),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = stringResource(R.string.quiz_score, state.score),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        if (!state.isAnswered) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = if (state.timeLeftSeconds <= 3) WrongRed else MaterialTheme.colorScheme.primary
                )
                LinearProgressIndicator(
                    progress = { animatedTimer },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp),
                    strokeCap = StrokeCap.Round,
                    color = if (state.timeLeftSeconds <= 3) WrongRed else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = stringResource(R.string.quiz_seconds, state.timeLeftSeconds),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (state.timeLeftSeconds <= 3) WrongRed else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(16.dp))
        } else if (state.isTimeout) {
            Text(
                text = stringResource(R.string.quiz_timeout),
                style = MaterialTheme.typography.titleSmall,
                color = WrongRed,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
        }

        AnimatedContent(
            targetState = state,
            contentKey = { it.questionNumber },
            transitionSpec = {
                (slideInHorizontally(tween(300)) { width -> width } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(tween(300)) { width -> -width } + fadeOut(tween(300)))
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            label = "question"
        ) { pageState ->
            val question = pageState.question ?: return@AnimatedContent
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = question.text,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(24.dp))

                question.options.forEachIndexed { index, option ->
                    AnswerButton(
                        text = option,
                        answerState = answerStateFor(index, pageState, question),
                        enabled = !pageState.isAnswered,
                        onClick = {
                            haptics.performHapticFeedback(
                                if (index == question.correctIndex) HapticFeedbackType.Confirm
                                else HapticFeedbackType.Reject
                            )
                            onAnswerSelected(index)
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                if (pageState.showExplanation && question.explanation != null) {
                    Spacer(Modifier.height(8.dp))
                    ExplanationCard(question.explanation)
                }
            }
        }

        if (state.isAnswered) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(
                        if (state.questionNumber == state.totalQuestions) R.string.quiz_finish
                        else R.string.quiz_next
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private enum class AnswerVisual { NEUTRAL, CORRECT, WRONG }

private fun answerStateFor(index: Int, state: QuizUiState, question: Question): AnswerVisual = when {
    !state.isAnswered -> AnswerVisual.NEUTRAL
    state.isTimeout && index == question.correctIndex -> AnswerVisual.CORRECT
    state.isTimeout -> AnswerVisual.NEUTRAL
    index == question.correctIndex -> AnswerVisual.CORRECT
    index == state.selectedAnswer -> AnswerVisual.WRONG
    else -> AnswerVisual.NEUTRAL
}

@Composable
private fun AnswerButton(
    text: String,
    answerState: AnswerVisual,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val container = when (answerState) {
        AnswerVisual.CORRECT -> CorrectGreen
        AnswerVisual.WRONG -> WrongRed
        AnswerVisual.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant
    }
    val content = when (answerState) {
        AnswerVisual.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> Color.White
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = container,
        contentColor = content,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            when (answerState) {
                AnswerVisual.CORRECT -> Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(R.string.answer_correct),
                    modifier = Modifier.size(22.dp)
                )
                AnswerVisual.WRONG -> Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.answer_wrong),
                    modifier = Modifier.size(22.dp)
                )
                AnswerVisual.NEUTRAL -> Unit
            }
        }
    }
}

@Composable
private fun ExplanationCard(explanation: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = explanation,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun QuizContentUnansweredPreview() {
    QuizappTheme {
        QuizContent(
            state = QuizUiState(
                isLoading = false,
                question = sampleQuestion,
                questionNumber = 3,
                totalQuestions = 10,
                timeLeftSeconds = 7
            ),
            onAnswerSelected = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun QuizContentAnsweredWrongPreview() {
    QuizappTheme {
        QuizContent(
            state = QuizUiState(
                isLoading = false,
                question = sampleQuestion,
                questionNumber = 3,
                totalQuestions = 10,
                selectedAnswer = 0,
                showExplanation = true,
                penaltyCount = 1,
                isTimeout = true
            ),
            onAnswerSelected = {},
            onNext = {}
        )
    }
}

private val sampleQuestion = Question(
    id = "chem_004",
    category = "chemistry",
    difficulty = Difficulty.EASY,
    text = "Какова химическая формула поваренной соли?",
    options = listOf("NaCl", "KCl", "HCl", "CaCO₃"),
    correctIndex = 0,
    explanation = "Поваренная соль — это хлорид натрия, NaCl."
)