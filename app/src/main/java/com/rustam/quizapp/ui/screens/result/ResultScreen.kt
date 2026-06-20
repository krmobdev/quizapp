package com.rustam.quizapp.ui.screens.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rustam.quizapp.R
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.data.Question
import com.rustam.quizapp.domain.QuizResult
import com.rustam.quizapp.ui.theme.QuizappTheme

private val ResultButtonShape = RoundedCornerShape(24.dp)
private val ResultButtonHeight = 72.dp
private val ResultButtonSpacing = 12.dp

@Composable
private fun ResultActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(ResultButtonHeight),
        shape = ResultButtonShape,
        colors = if (primary) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
            )
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ResultScreen(
    result: QuizResult?,
    onRetryMistakes: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onHome)

    // Defensive: if the shared result was lost (e.g. process death), offer a way home.
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = headlineFor(result.score, result.total),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.result_score, result.score, result.total),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        if (result.penalties > 0) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.result_penalties, result.correct, result.penalties),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(24.dp))

        if (result.mistakes.isNotEmpty()) {
            Text(
                text = stringResource(R.string.result_mistakes),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(result.mistakes, key = { it.id }) { question ->
                    MistakeItem(question)
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
        ResultActionButton(
            text = stringResource(R.string.result_retry_mistakes),
            onClick = onRetryMistakes,
            enabled = result.mistakes.isNotEmpty(),
            primary = true
        )
        Spacer(Modifier.height(ResultButtonSpacing))
        ResultActionButton(
            text = stringResource(R.string.result_home),
            onClick = onHome,
            primary = false
        )
    }
}

@Composable
private fun MistakeItem(question: Question) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = question.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(
                    R.string.result_correct_answer,
                    question.options[question.correctIndex]
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MissingResult(onHome: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
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
        ResultActionButton(
            text = stringResource(R.string.result_home),
            onClick = onHome,
            primary = true
        )
    }
}

@Composable
private fun headlineFor(correct: Int, total: Int): String {
    val ratio = if (total == 0) 0f else correct.toFloat() / total
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
                correct = 7,
                total = 10,
                mistakes = listOf(
                    Question(
                        id = "chem_018",
                        category = "chemistry",
                        difficulty = Difficulty.MEDIUM,
                        text = "Какова формула серной кислоты?",
                        options = listOf("HNO₃", "H₂SO₃", "H₂SO₄", "HCl"),
                        correctIndex = 2,
                        explanation = null
                    ),
                    Question(
                        id = "chem_040",
                        category = "chemistry",
                        difficulty = Difficulty.HARD,
                        text = "Какой тип связи в молекуле NaCl?",
                        options = listOf("Ковалентная", "Металлическая", "Водородная", "Ионная"),
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

@Preview(showBackground = true)
@Composable
private fun ResultPerfectPreview() {
    QuizappTheme {
        ResultContent(
            result = QuizResult(correct = 10, total = 10, mistakes = emptyList()),
            onRetryMistakes = {},
            onHome = {}
        )
    }
}
