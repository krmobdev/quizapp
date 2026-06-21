package com.rustam.quizapp.ui.screens.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextAlign
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
import com.rustam.quizapp.domain.QuizEventProgress
import com.rustam.quizapp.domain.QuizEventType
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
        onUpgradeStat = viewModel::upgradeStat,
        modifier = modifier
    )
}

@Composable
private fun StatsContent(
    state: PlayerUiState,
    onUpdateName: (String) -> Unit,
    onUpgradeStat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()
    var showNameDialog by rememberSaveable { mutableStateOf(false) }
    var selectedSubTab by rememberSaveable { mutableStateOf(0) }

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
                lifetimePoints = state.lifetimePoints,
                coins = state.coins,
                colors = colors,
                textColor = textColor,
                onEditName = { showNameDialog = true }
            )
        }
        item {
            val isEnglish = stringResource(R.string.player_tab) == "Player"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubTabButton(
                    text = if (isEnglish) "Characteristics" else "Характеристики",
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    colors = colors,
                    textColor = textColor,
                    modifier = Modifier.weight(1f)
                )
                SubTabButton(
                    text = if (isEnglish) "Game Stats" else "Статистика игр",
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    colors = colors,
                    textColor = textColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (selectedSubTab == 0) {
            item {
                CharacterStatsSection(
                    stats = state.stats,
                    freeXp = state.points,
                    textColor = textColor,
                    colors = colors,
                    onUpgrade = onUpgradeStat
                )
            }
        } else {
            if (state.events.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.events_section_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                items(state.events, key = { it.event.type.name }) { progress ->
                    EventProgressCard(
                        progress = progress,
                        categories = state.categories,
                        colors = colors,
                        textColor = textColor
                    )
                }
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
}

@Composable
private fun PlayerProfileCard(
    displayName: String,
    avatarEmoji: String,
    points: Int,
    lifetimePoints: Int,
    coins: Int,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    onEditName: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember(lifetimePoints) {
        com.rustam.quizapp.domain.CharacterLevelCalculator.getLevelProgress(lifetimePoints)
    }
    val isEnglish = stringResource(R.string.player_tab) == "Player"
    val rankName = if (isEnglish) {
        com.rustam.quizapp.domain.CharacterLevelCalculator.getLevelRankEn(progress.level)
    } else {
        com.rustam.quizapp.domain.CharacterLevelCalculator.getLevelRank(progress.level)
    }

    GlassCard(modifier = modifier, colors = colors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = avatarEmoji,
                    fontSize = 42.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        IconButton(
                            onClick = onEditName,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.player_edit_name),
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "$rankName • " + stringResource(R.string.char_level, progress.level),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.char_xp_progress, lifetimePoints, com.rustam.quizapp.domain.CharacterLevelCalculator.xpRequiredForLevel(progress.level + 1)),
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${(progress.progressFraction * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
                LinearProgressIndicator(
                    progress = { progress.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    strokeCap = StrokeCap.Round,
                    trackColor = colors.progressTrack,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryMetric(
                    value = points.toString(),
                    label = stringResource(R.string.char_free_xp),
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
private fun CharacterStatsSection(
    stats: com.rustam.quizapp.domain.CharacterStats,
    freeXp: Int,
    textColor: androidx.compose.ui.graphics.Color,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    onUpgrade: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnglish = stringResource(R.string.player_tab) == "Player"
    val sectionTitle = if (isEnglish) "Character Stats" else "Характеристики персонажа"
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )
        
        StatUpgradeCard(
            title = stringResource(R.string.char_stat_strength),
            value = stats.strength,
            description = stringResource(R.string.char_stat_strength_desc, stats.xpBonusPercent),
            freeXp = freeXp,
            colors = colors,
            textColor = textColor,
            onUpgrade = { onUpgrade("strength") }
        )

        StatUpgradeCard(
            title = stringResource(R.string.char_stat_intelligence),
            value = stats.intelligence,
            description = stringResource(R.string.char_stat_intelligence_desc, stats.coinBonusPercent),
            freeXp = freeXp,
            colors = colors,
            textColor = textColor,
            onUpgrade = { onUpgrade("intelligence") }
        )

        StatUpgradeCard(
            title = stringResource(R.string.char_stat_agility),
            value = stats.agility,
            description = stringResource(R.string.char_stat_agility_desc, stats.extraTimeSeconds),
            freeXp = freeXp,
            colors = colors,
            textColor = textColor,
            onUpgrade = { onUpgrade("agility") }
        )

        StatUpgradeCard(
            title = stringResource(R.string.char_stat_luck),
            value = stats.luck,
            description = stringResource(R.string.char_stat_luck_desc, stats.doubleRewardChancePercent),
            freeXp = freeXp,
            colors = colors,
            textColor = textColor,
            onUpgrade = { onUpgrade("luck") }
        )
    }
}

@Composable
private fun StatUpgradeCard(
    title: String,
    value: Int,
    description: String,
    freeXp: Int,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    onUpgrade: () -> Unit
) {
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "$value / 20",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
                
                LinearProgressIndicator(
                    progress = { value / 20f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .height(6.dp),
                    strokeCap = StrokeCap.Round,
                    trackColor = colors.progressTrack,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
            
            androidx.compose.material3.Button(
                onClick = onUpgrade,
                enabled = freeXp >= 50 && value < 20,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = textColor.copy(alpha = 0.08f),
                    disabledContentColor = textColor.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.char_upgrade_cost),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EventProgressCard(
    progress: QuizEventProgress,
    categories: List<CategoryStatsUi>,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val event = progress.event
    val questCategory = categories.find { it.id == event.category.id }

    GlassCard(modifier = modifier, colors = colors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(eventTitleRes(event.type)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            if (questCategory != null) {
                Text(
                    text = stringResource(
                        R.string.event_topic,
                        "${questCategory.emoji} ${stringResource(questCategory.titleRes)}"
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
            }
            Text(
                text = stringResource(eventBonusRes(event.type), event.coinMultiplier, event.bonusPoints),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            Text(
                text = if (progress.available) {
                    if (event.maxCompletions > 1) {
                        stringResource(
                            R.string.event_progress,
                            progress.completions,
                            event.maxCompletions
                        )
                    } else {
                        stringResource(R.string.event_start)
                    }
                } else {
                    stringResource(eventCompletedRes(event.type))
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (!progress.available) FontWeight.SemiBold else FontWeight.Normal,
                color = if (!progress.available) {
                    MaterialTheme.colorScheme.primary
                } else {
                    textColor
                }
            )
        }
    }
}

private fun eventTitleRes(type: QuizEventType): Int = when (type) {
    QuizEventType.DAILY -> R.string.event_daily_title
    QuizEventType.WEEKLY -> R.string.event_weekly_title
    QuizEventType.WEEKEND_BLITZ -> R.string.event_weekend_blitz_title
    QuizEventType.MARATHON -> R.string.event_marathon_title
}

private fun eventBonusRes(type: QuizEventType): Int = when (type) {
    QuizEventType.DAILY -> R.string.event_bonus_daily
    QuizEventType.WEEKLY -> R.string.event_bonus_weekly
    QuizEventType.WEEKEND_BLITZ -> R.string.event_bonus_weekend_blitz
    QuizEventType.MARATHON -> R.string.event_bonus_marathon
}

private fun eventCompletedRes(type: QuizEventType): Int = when (type) {
    QuizEventType.DAILY -> R.string.event_completed_daily
    QuizEventType.WEEKLY -> R.string.event_completed_weekly
    QuizEventType.WEEKEND_BLITZ -> R.string.event_completed_weekend_blitz
    QuizEventType.MARATHON -> R.string.event_completed_marathon
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

@Composable
private fun SubTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    } else {
        colors.progressTrack.copy(alpha = 0.1f)
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    } else {
        textColor.copy(alpha = 0.1f)
    }
    
    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp)
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
                totalQuizzes = 7,
                averageAccuracyPercent = 74,
                categories = listOf(
                    CategoryStatsUi("chemistry", R.string.category_chemistry, "🧪", attempts = 4, accuracyPercent = 80, bestScorePercent = 90),
                    CategoryStatsUi("physics", R.string.category_physics, "⚛️", attempts = 0, accuracyPercent = 0, bestScorePercent = 0)
                )
            ),
            onUpdateName = {},
            onUpgradeStat = {}
        )
    }
}