package com.rustam.quizapp.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.R
import com.rustam.quizapp.ui.components.AppDimens
import com.rustam.quizapp.ui.components.GlassCard
import com.rustam.quizapp.ui.components.ScreenTitle
import com.rustam.quizapp.ui.components.appTextColor
import com.rustam.quizapp.ui.components.rememberAppThemeColors
import com.rustam.quizapp.ui.theme.QuizappTheme

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    StatsContent(
        state = state,
        onUpdateName = viewModel::updatePlayerName,
        modifier = modifier
    )
}

@Composable
private fun StatsContent(
    state: PlayerUiState,
    onUpdateName: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()
    var showNameDialog by rememberSaveable { mutableStateOf(false) }

    if (showNameDialog) {
        EditNameDialog(
            currentName = state.playerName,
            onDismiss = { showNameDialog = false },
            onSave = { name ->
                onUpdateName(name)
                showNameDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(AppDimens.CardSpacing)
    ) {
        item {
            ScreenTitle(
                title = stringResource(R.string.player_tab),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        item {
            PlayerProfileCard(
                displayName = state.playerName.ifBlank {
                    stringResource(R.string.player_default_name)
                },
                avatarEmoji = state.avatarEmoji,
                points = state.points,
                coins = state.coins,
                colors = colors,
                textColor = textColor,
                onEditName = { showNameDialog = true }
            )
        }
        item {
            DailyQuestCard(
                categoryId = state.dailyQuestCategoryId,
                categories = state.categories,
                completed = state.dailyQuestCompleted,
                colors = colors,
                textColor = textColor
            )
        }
        item {
            SummaryCard(
                totalQuizzes = state.totalQuizzes,
                averageAccuracyPercent = state.averageAccuracyPercent,
                colors = colors,
                textColor = textColor
            )
        }
        item {
            Text(
                text = stringResource(R.string.player_stats_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }
        items(state.categories, key = { it.id }) { category ->
            CategoryStatCard(
                category = category,
                colors = colors,
                textColor = textColor
            )
        }
    }
}

@Composable
private fun PlayerProfileCard(
    displayName: String,
    avatarEmoji: String,
    points: Int,
    coins: Int,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    onEditName: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, colors = colors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = avatarEmoji,
                    fontSize = 34.sp
                )
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEditName) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.player_edit_name),
                        tint = textColor
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryMetric(
                    value = points.toString(),
                    label = stringResource(R.string.player_points),
                    textColor = textColor
                )
                SummaryMetric(
                    value = coins.toString(),
                    label = stringResource(R.string.player_coins),
                    textColor = textColor
                )
            }
        }
    }
}

@Composable
private fun DailyQuestCard(
    categoryId: String?,
    categories: List<CategoryStatsUi>,
    completed: Boolean,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val questCategory = categoryId?.let { id -> categories.find { it.id == id } }

    GlassCard(modifier = modifier, colors = colors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.daily_quest_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            if (questCategory != null) {
                Text(
                    text = stringResource(
                        R.string.daily_quest_topic,
                        "${questCategory.emoji} ${stringResource(questCategory.titleRes)}"
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
            }
            Text(
                text = stringResource(R.string.daily_quest_bonus),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            Text(
                text = if (completed) {
                    stringResource(R.string.daily_quest_completed)
                } else {
                    stringResource(R.string.daily_quest_start)
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (completed) FontWeight.SemiBold else FontWeight.Normal,
                color = if (completed) {
                    MaterialTheme.colorScheme.primary
                } else {
                    textColor
                }
            )
        }
    }
}

@Composable
private fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var draft by remember(currentName) {
        mutableStateOf(currentName.ifBlank { "" })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.player_edit_name)) },
        text = {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text(stringResource(R.string.player_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(draft) },
                enabled = draft.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun SummaryCard(
    totalQuizzes: Int,
    averageAccuracyPercent: Int?,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryMetric(
                value = totalQuizzes.toString(),
                label = stringResource(R.string.stats_quizzes_completed),
                textColor = textColor
            )
            SummaryMetric(
                value = averageAccuracyPercent?.let { "$it%" }
                    ?: stringResource(R.string.em_dash),
                label = stringResource(R.string.stats_avg_accuracy),
                textColor = textColor
            )
        }
    }
}

@Composable
private fun SummaryMetric(
    value: String,
    label: String,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun CategoryStatCard(
    category: CategoryStatsUi,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, colors = colors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPaddingH, AppDimens.CardPaddingV),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "${category.emoji}  ${stringResource(category.titleRes)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )

            if (category.hasData) {
                LinearProgressIndicator(
                    progress = { category.accuracyPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    strokeCap = StrokeCap.Round,
                    trackColor = colors.progressTrack,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatLine(
                        label = stringResource(R.string.stats_accuracy),
                        value = "${category.accuracyPercent}%",
                        textColor = textColor
                    )
                    StatLine(
                        label = stringResource(R.string.stats_best),
                        value = "${category.bestScorePercent}%",
                        textColor = textColor
                    )
                    StatLine(
                        label = stringResource(R.string.stats_attempts),
                        value = category.attempts.toString(),
                        textColor = textColor
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.stats_not_played),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun StatLine(
    label: String,
    value: String,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsContentPreview() {
    QuizappTheme {
        StatsContent(
            state = PlayerUiState(
                playerName = "Alex",
                points = 1250,
                coins = 84,
                dailyQuestCategoryId = "chemistry",
                dailyQuestCompleted = false,
                totalQuizzes = 7,
                averageAccuracyPercent = 74,
                categories = listOf(
                    CategoryStatsUi("chemistry", R.string.category_chemistry, "🧪", attempts = 4, accuracyPercent = 80, bestScorePercent = 90),
                    CategoryStatsUi("physics", R.string.category_physics, "⚛️", attempts = 0, accuracyPercent = 0, bestScorePercent = 0)
                )
            ),
            onUpdateName = {}
        )
    }
}