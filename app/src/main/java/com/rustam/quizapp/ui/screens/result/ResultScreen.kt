package com.rustam.quizapp.ui.screens.result

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rustam.quizapp.R
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.data.Question
import com.rustam.quizapp.domain.QuizResult
import com.rustam.quizapp.ui.components.AppActionButton
import com.rustam.quizapp.ui.components.AppBackground
import com.rustam.quizapp.ui.components.AppDimens
import com.rustam.quizapp.ui.components.AppThemeColors
import com.rustam.quizapp.ui.components.GlassCard
import com.rustam.quizapp.ui.components.ScoreRing
import com.rustam.quizapp.ui.components.rememberAppThemeColors
import com.rustam.quizapp.ui.theme.QuizappTheme

@Composable
fun ResultScreen(
    result: QuizResult?,
    onRetryMistakes: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onHome)

    if (result == null) {
        MissingResult(onHome = onHome, modifier = modifier)
        return
    }
    ResultContent(
        result = result,
        onRetryMistakes = onRetryMistakes,
        onHome = onHome,
        modifier = modifier
    )
}

@Composable
private fun ResultContent(
    result: QuizResult,
    onRetryMistakes: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()
    val headlineAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "headlineAlpha"
    )
    val ratio = if (result.total == 0) 0f else result.score.toFloat() / result.total
    val emoji = when {
        ratio >= 1f -> "🏆"
        ratio >= 0.8f -> "🎉"
        ratio >= 0.5f -> "📚"
        else -> "💪"
    }

    AppBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = emoji,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.alpha(headlineAlpha)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = headlineFor(result.score, result.total),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(headlineAlpha)
            )
            Spacer(Modifier.height(20.dp))

            ScoreRing(score = result.score, total = result.total)

            if (result.penalties > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.result_penalties, result.correct, result.penalties),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(20.dp))

            if (result.mistakes.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.result_mistakes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.CardSpacing)
                ) {
                    items(result.mistakes, key = { it.id }) { question ->
                        MistakeItem(question, colors)
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.result_perfect),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))
            AppActionButton(
                text = stringResource(R.string.result_retry_mistakes),
                onClick = onRetryMistakes,
                enabled = result.mistakes.isNotEmpty(),
                primary = true
            )
            Spacer(Modifier.height(AppDimens.ButtonSpacing))
            AppActionButton(
                text = stringResource(R.string.result_home),
                onClick = onHome,
                primary = false
            )
        }
    }
}

@Composable
private fun MistakeItem(
    question: Question,
    colors: AppThemeColors
) {
    GlassCard(colors = colors) {
        Column(
            modifier = Modifier.padding(
                horizontal = AppDimens.CardPaddingH,
                vertical = AppDimens.CardPaddingV
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = question.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(
                    R.string.result_correct_answer,
                    question.options[question.correctIndex]
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MissingResult(onHome: () -> Unit, modifier: Modifier = Modifier) {
    AppBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.result_unavailable),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(16.dp))
            AppActionButton(
                text = stringResource(R.string.result_home),
                onClick = onHome,
                primary = true
            )
        }
    }
}

@Composable
private fun headlineFor(score: Int, total: Int): String {
    val ratio = if (total == 0) 0f else score.toFloat() / total
    return stringResource(
        when {
            ratio >= 1f -> R.string.result_headline_perfect
            ratio >= 0.8f -> R.string.result_headline_great
            ratio >= 0.5f -> R.string.result_headline_ok
            else -> R.string.result_headline_low
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ResultWithMistakesPreview() {
    QuizappTheme {
        ResultContent(
            result = QuizResult(
                correct = 4,
                total = 10,
                penalties = 1,
                mistakes = listOf(
                    Question(
                        id = "phys_001",
                        category = "physics",
                        difficulty = Difficulty.MEDIUM,
                        text = "What is the force if mass is 28 kg and acceleration is 14 m/s²?",
                        options = listOf("393", "42", "420", "392"),
                        correctIndex = 3,
                        explanation = null
                    )
                )
            ),
            onRetryMistakes = {},
            onHome = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
private fun ResultDarkPreview() {
    QuizappTheme(darkTheme = true) {
        ResultContent(
            result = QuizResult(correct = 10, total = 10, mistakes = emptyList()),
            onRetryMistakes = {},
            onHome = {}
        )
    }
}