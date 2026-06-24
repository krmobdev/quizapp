package com.rustam.quizapp.ui.screens.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.R
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.data.Question
import com.rustam.quizapp.domain.PowerUpType
import com.rustam.quizapp.domain.QuizEventType
import com.rustam.quizapp.domain.QuizResult
import com.rustam.quizapp.domain.ShopCatalog
import com.rustam.quizapp.ui.components.AppActionButton
import com.rustam.quizapp.ui.components.AppBackground
import com.rustam.quizapp.ui.components.AppDimens
import com.rustam.quizapp.ui.components.AppShapes
import com.rustam.quizapp.ui.components.AppThemeColors
import com.rustam.quizapp.ui.components.GlassCard
import com.rustam.quizapp.ui.components.OptionBadge
import com.rustam.quizapp.ui.components.OptionLabels
import com.rustam.quizapp.ui.components.appTextColor
import com.rustam.quizapp.ui.components.rememberAppThemeColors
import com.rustam.quizapp.ui.theme.QuizappTheme

@Composable
fun QuizScreen(
    categoryId: String,
    difficulty: Difficulty?,
    onFinished: (QuizResult) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    eventType: QuizEventType? = null,
    questionTimeSeconds: Int = DEFAULT_QUESTION_TIME_SECONDS,
    questionCount: Int = 10,
    adaptive: Boolean = false,
    viewModel: QuizViewModel = viewModel()
) {
    BackHandler {
        viewModel.saveAndExit(onDone = onBack)
    }

    LaunchedEffect(Unit) {
        viewModel.prepare(
            categoryId = categoryId,
            difficulty = difficulty,
            eventType = eventType,
            questionTimeSeconds = questionTimeSeconds,
            questionCount = questionCount,
            adaptive = adaptive
        )
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
            onFiftyFifty = viewModel::useFiftyFifty,
            onAddTime = viewModel::useAddTime,
            onSkip = viewModel::useSkip,
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
    onFiftyFifty: () -> Unit,
    onAddTime: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()

    AppBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.question != null -> QuestionLayout(
                    state = state,
                    colors = colors,
                    onAnswerSelected = onAnswerSelected,
                    onNext = onNext,
                    onFiftyFifty = onFiftyFifty,
                    onAddTime = onAddTime,
                    onSkip = onSkip
                )
            }
        }
    }
}

@Composable
private fun QuestionLayout(
    state: QuizUiState,
    colors: AppThemeColors,
    onAnswerSelected: (Int) -> Unit,
    onNext: () -> Unit,
    onFiftyFifty: () -> Unit,
    onAddTime: () -> Unit,
    onSkip: () -> Unit
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
    val timerScale by animateFloatAsState(
        targetValue = if (!state.isAnswered && state.timeLeftSeconds <= 3) 1.08f else 1f,
        animationSpec = tween(200),
        label = "timerScale"
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
                style = MaterialTheme.typography.labelLarge,
                color = appTextColor()
            )
            Surface(
                shape = AppShapes.Badge,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
            ) {
                Text(
                    text = stringResource(R.string.quiz_score, state.score),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = appTextColor(),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth(),
            strokeCap = StrokeCap.Round,
            trackColor = colors.progressTrack
        )
        Spacer(Modifier.height(12.dp))

        if (!state.isAnswered) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(timerScale),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = if (state.timeLeftSeconds <= 3) colors.wrong else appTextColor()
                )
                LinearProgressIndicator(
                    progress = { animatedTimer },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    strokeCap = StrokeCap.Round,
                    color = if (state.timeLeftSeconds <= 3) colors.wrong else MaterialTheme.colorScheme.primary,
                    trackColor = colors.progressTrack
                )
                Text(
                    text = stringResource(R.string.quiz_seconds, state.timeLeftSeconds),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (state.timeLeftSeconds <= 3) colors.wrong else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(12.dp))
            PowerUpBar(
                state = state,
                colors = colors,
                onFiftyFifty = onFiftyFifty,
                onAddTime = onAddTime,
                onSkip = onSkip
            )
            Spacer(Modifier.height(16.dp))
        } else if (state.isTimeout) {
            Text(
                text = stringResource(R.string.quiz_timeout),
                style = MaterialTheme.typography.titleSmall,
                color = colors.wrong,
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
                GlassCard(colors = colors) {
                    Text(
                        text = question.text,
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.questionText,
                        modifier = Modifier.padding(
                            horizontal = AppDimens.CardPaddingH,
                            vertical = 18.dp
                        )
                    )
                }
                Spacer(Modifier.height(16.dp))

                question.options.forEachIndexed { index, option ->
                    val eliminated = index in pageState.hiddenOptions
                    AnswerButton(
                        label = OptionLabels.getOrElse(index) { "?" },
                        text = option,
                        answerState = answerStateFor(index, pageState, question),
                        colors = colors,
                        enabled = !pageState.isAnswered && !eliminated,
                        eliminated = eliminated,
                        onClick = {
                            haptics.performHapticFeedback(
                                if (index == question.correctIndex) HapticFeedbackType.Confirm
                                else HapticFeedbackType.Reject
                            )
                            onAnswerSelected(index)
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                if (pageState.showExplanation && question.explanation != null) {
                    Spacer(Modifier.height(6.dp))
                    ExplanationCard(question.explanation, colors)
                }
            }
        }

        if (state.isAnswered) {
            Spacer(Modifier.height(12.dp))
            AppActionButton(
                text = stringResource(
                    if (state.questionNumber == state.totalQuestions) R.string.quiz_finish
                    else R.string.quiz_next
                ),
                onClick = onNext,
                primary = true
            )
        }
    }
}

@Composable
private fun PowerUpBar(
    state: QuizUiState,
    colors: AppThemeColors,
    onFiftyFifty: () -> Unit,
    onAddTime: () -> Unit,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ShopCatalog.powerUps.forEach { powerUp ->
            val count = state.powerUpCounts[powerUp.id] ?: 0
            val usable = when (powerUp.type) {
                PowerUpType.FIFTY_FIFTY -> count > 0 && state.hiddenOptions.isEmpty()
                else -> count > 0
            }
            PowerUpButton(
                emoji = powerUp.emoji,
                count = count,
                enabled = usable,
                colors = colors,
                onClick = when (powerUp.type) {
                    PowerUpType.FIFTY_FIFTY -> onFiftyFifty
                    PowerUpType.ADD_TIME -> onAddTime
                    PowerUpType.SKIP -> onSkip
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PowerUpButton(
    emoji: String,
    count: Int,
    enabled: Boolean,
    colors: AppThemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = AppShapes.Card,
        color = colors.answerCard,
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            .border(1.dp, colors.answerBorder, AppShapes.Card)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "×$count",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = appTextColor()
            )
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
    label: String,
    text: String,
    answerState: AnswerVisual,
    colors: AppThemeColors,
    enabled: Boolean,
    eliminated: Boolean = false,
    onClick: () -> Unit
) {
    val container by animateColorAsState(
        targetValue = when (answerState) {
            AnswerVisual.CORRECT -> colors.correct
            AnswerVisual.WRONG -> colors.wrong
            AnswerVisual.NEUTRAL -> colors.answerCard
        },
        animationSpec = tween(250),
        label = "answerBg"
    )
    val content = when (answerState) {
        AnswerVisual.NEUTRAL -> colors.answerText
        else -> Color.White
    }
    val borderColor = when (answerState) {
        AnswerVisual.NEUTRAL -> colors.answerBorder
        AnswerVisual.CORRECT -> Color.White.copy(alpha = 0.55f)
        AnswerVisual.WRONG -> colors.wrong.copy(alpha = 0.6f)
    }
    val badgeBg = when (answerState) {
        AnswerVisual.NEUTRAL -> colors.answerBadgeBg
        else -> Color.White.copy(alpha = 0.22f)
    }
    val badgeText = when (answerState) {
        AnswerVisual.NEUTRAL -> colors.answerBadgeText
        else -> Color.White
    }
    val isCorrect = answerState == AnswerVisual.CORRECT

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = AppShapes.Card,
        color = container,
        contentColor = content,
        shadowElevation = if (isCorrect) 6.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (eliminated) 0.35f else 1f)
            .border(
                width = if (isCorrect) 1.5.dp else 1.dp,
                color = borderColor,
                shape = AppShapes.Card
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            OptionBadge(label = label, background = badgeBg, content = badgeText, size = 38.dp)
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            when (answerState) {
                AnswerVisual.CORRECT -> ResultBadge(correct = true, accent = colors.correct)
                AnswerVisual.WRONG -> ResultBadge(correct = false, accent = colors.wrong)
                AnswerVisual.NEUTRAL -> Unit
            }
        }
    }
}

/** A crisp white circular chip with the result glyph, used on answered option cards. */
@Composable
private fun ResultBadge(correct: Boolean, accent: Color) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (correct) Icons.Rounded.Check else Icons.Rounded.Close,
            contentDescription = stringResource(
                if (correct) R.string.answer_correct else R.string.answer_wrong
            ),
            tint = accent,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ExplanationCard(explanation: String, colors: AppThemeColors) {
    Surface(
        shape = AppShapes.Card,
        color = colors.explanationCard,
        contentColor = colors.explanationText,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.answerBorder, AppShapes.Card)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AppDimens.CardPaddingH,
                vertical = AppDimens.CardPaddingV
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.Lightbulb,
                contentDescription = null,
                tint = appTextColor(),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
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
            onNext = {},
            onFiftyFifty = {},
            onAddTime = {},
            onSkip = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark answered")
@Composable
private fun QuizContentDarkPreview() {
    QuizappTheme(darkTheme = true) {
        QuizContent(
            state = QuizUiState(
                isLoading = false,
                question = sampleQuestion.copy(
                    text = "What is the force if mass is 28 kg and acceleration is 14 m/s²?",
                    options = listOf("393", "42", "420", "392"),
                    correctIndex = 3,
                    explanation = "F = m·a = 28·14 = 392 N."
                ),
                questionNumber = 1,
                totalQuestions = 10,
                selectedAnswer = 3,
                showExplanation = true,
                correctCount = 1,
                isTimeout = true,
                penaltyCount = 1
            ),
            onAnswerSelected = {},
            onNext = {},
            onFiftyFifty = {},
            onAddTime = {},
            onSkip = {}
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