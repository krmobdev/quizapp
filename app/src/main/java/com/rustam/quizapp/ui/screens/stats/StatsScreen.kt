package com.rustam.quizapp.ui.screens.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.R
import com.rustam.quizapp.domain.CharacterLevelCalculator
import com.rustam.quizapp.domain.CharacterStats
import com.rustam.quizapp.domain.QuizEventProgress
import com.rustam.quizapp.domain.QuizEventType
import com.rustam.quizapp.domain.ShopCatalog
import com.rustam.quizapp.domain.SkillBranch
import com.rustam.quizapp.domain.SkillBonusKind
import com.rustam.quizapp.domain.SkillTree
import com.rustam.quizapp.domain.SkillTreeState
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
        onUpgradeSkill = viewModel::upgradeSkill,
        modifier = modifier
    )
}

@Composable
private fun StatsContent(
    state: PlayerUiState,
    onUpdateName: (String) -> Unit,
    onUpgradeStat: (String) -> Unit,
    onUpgradeSkill: (String) -> Unit = {},
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
                state = state,
                colors = colors,
                textColor = textColor,
                onEditName = { showNameDialog = true }
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubTabButton(
                    text = stringResource(R.string.stats_characteristics_tab),
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    colors = colors,
                    textColor = textColor,
                    modifier = Modifier.weight(1f)
                )
                SubTabButton(
                    text = stringResource(R.string.stats_skilltree_tab),
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    colors = colors,
                    textColor = textColor,
                    modifier = Modifier.weight(1f)
                )
                SubTabButton(
                    text = stringResource(R.string.stats_game_stats_tab),
                    selected = selectedSubTab == 2,
                    onClick = { selectedSubTab = 2 },
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
        } else if (selectedSubTab == 1) {
            item {
                SkillTreeSection(
                    skillTree = state.skillTree,
                    freeXp = state.points,
                    coins = state.coins,
                    textColor = textColor,
                    colors = colors,
                    onUpgrade = onUpgradeSkill
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
                    streakCurrent = state.streakCurrent,
                    colors = colors,
                    textColor = textColor
                )
            }
            if (state.achievements.isNotEmpty()) {
                item {
                    val unlockedCount = state.achievements.count { it.unlocked }
                    Text(
                        text = stringResource(
                            R.string.achievements_section_title,
                            unlockedCount,
                            state.achievements.size
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                items(state.achievements, key = { it.id }) { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        colors = colors,
                        textColor = textColor
                    )
                }
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
    state: PlayerUiState,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    onEditName: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lifetimePoints = state.lifetimePoints
    val bankedLifetimePoints = state.bankedLifetimePoints
    val progress = remember(lifetimePoints, bankedLifetimePoints) {
        CharacterLevelCalculator.getLevelProgress(lifetimePoints, bankedLifetimePoints)
    }
    val displayLevel = progress.level
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val rankName = if (locale.language == "en") {
        CharacterLevelCalculator.getLevelRankEn(displayLevel)
    } else {
        CharacterLevelCalculator.getLevelRank(displayLevel)
    }
    val displayName = state.playerName.ifBlank { stringResource(R.string.player_default_name) }
    val title = ShopCatalog.title(state.equippedTitleId)

    // Aggregated bonuses currently in effect (characteristics + Mastery Tree combined).
    val xpBonus = state.stats.xpBonusPercent + state.skillTree.xpBonusPercent
    val coinBonus = state.stats.coinBonusPercent + state.skillTree.coinBonusPercent
    val critChance = state.stats.doubleRewardChancePercent +
        state.stats.critChanceBonusPercent + state.skillTree.critChanceBonusPercent
    val extraTime = state.stats.extraTimeSeconds + state.skillTree.extraTimeSeconds
    val unlockedAchievements = state.achievements.count { it.unlocked }

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
                    text = state.avatarEmoji,
                    fontSize = 42.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
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
                        text = "$rankName • " + stringResource(R.string.char_level, displayLevel),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    )
                    if (title != null) {
                        TitleBadge(
                            text = "${title.emoji} ${stringResource(title.labelRes)}"
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.char_level_reward_bonus,
                            CharacterLevelCalculator.rewardMultiplier(displayLevel)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (progress.isMaxLevel) {
                            stringResource(R.string.char_xp_max_level, lifetimePoints)
                        } else {
                            stringResource(
                                R.string.char_xp_progress,
                                progress.currentXp,
                                progress.requiredXp
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.8f)
                    )
                    Text(
                        text = if (progress.isMaxLevel) {
                            stringResource(R.string.char_level_max)
                        } else {
                            "${(progress.progressFraction * 100).toInt()}%"
                        },
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMetric(
                    value = state.points.toString(),
                    label = stringResource(R.string.char_free_xp),
                    textColor = textColor,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    value = state.coins.toString(),
                    label = stringResource(R.string.player_coins),
                    textColor = textColor,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = textColor.copy(alpha = 0.08f))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryMetric(
                        value = "🔥 ${state.streakBest}",
                        label = stringResource(R.string.player_best_streak),
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetric(
                        value = stringResource(R.string.player_fraction, unlockedAchievements, state.achievements.size),
                        label = stringResource(R.string.player_achievements),
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryMetric(
                        value = state.lifetimeCoins.toString(),
                        label = stringResource(R.string.player_lifetime_coins),
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetric(
                        value = state.totalQuizzes.toString(),
                        label = stringResource(R.string.player_total_quizzes),
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.player_bonuses_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                val bonuses = buildList {
                    if (xpBonus > 0) add(stringResource(R.string.player_bonus_xp, xpBonus))
                    if (coinBonus > 0) add(stringResource(R.string.player_bonus_coins, coinBonus))
                    if (critChance > 0) add(stringResource(R.string.player_bonus_crit, critChance))
                    if (extraTime > 0f) add(stringResource(R.string.player_bonus_time, extraTime))
                }
                if (bonuses.isEmpty()) {
                    Text(
                        text = stringResource(R.string.player_bonus_none),
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                } else {
                    bonuses.chunked(2).forEach { rowChips ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowChips.forEach { chip ->
                                BonusChip(text = chip, modifier = Modifier.weight(1f))
                            }
                            if (rowChips.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TitleBadge(text: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun BonusChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 18.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun CharacterStatsSection(
    stats: CharacterStats,
    freeXp: Int,
    textColor: androidx.compose.ui.graphics.Color,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    onUpgrade: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sectionTitle = stringResource(R.string.stats_character_stats_title)
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Text(
                text = stringResource(R.string.char_free_xp_balance, freeXp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

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

        StatUpgradeCard(
            title = stringResource(R.string.char_stat_wisdom),
            value = stats.wisdom,
            description = stringResource(R.string.char_stat_wisdom_desc, stats.flatXpBonus),
            freeXp = freeXp,
            colors = colors,
            textColor = textColor,
            onUpgrade = { onUpgrade("wisdom") }
        )

        StatUpgradeCard(
            title = stringResource(R.string.char_stat_endurance),
            value = stats.endurance,
            description = stringResource(R.string.char_stat_endurance_desc, stats.flatCoinBonus),
            freeXp = freeXp,
            colors = colors,
            textColor = textColor,
            onUpgrade = { onUpgrade("endurance") }
        )

        StatUpgradeCard(
            title = stringResource(R.string.char_stat_focus),
            value = stats.focus,
            description = stringResource(R.string.char_stat_focus_desc, stats.critMultiplier),
            freeXp = freeXp,
            colors = colors,
            textColor = textColor,
            onUpgrade = { onUpgrade("focus") }
        )

        StatUpgradeCard(
            title = stringResource(R.string.char_stat_charisma),
            value = stats.charisma,
            description = stringResource(R.string.char_stat_charisma_desc, stats.critChanceBonusPercent),
            freeXp = freeXp,
            colors = colors,
            textColor = textColor,
            onUpgrade = { onUpgrade("charisma") }
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
    val maxStat = CharacterLevelCalculator.MAX_STAT
    val cost = CharacterLevelCalculator.statUpgradeCost(value)
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
                        text = "$value / $maxStat",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }

                LinearProgressIndicator(
                    progress = { value / maxStat.toFloat() },
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
                enabled = freeXp >= cost && value < maxStat,
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
                    text = stringResource(R.string.char_upgrade_cost, cost),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SkillTreeSection(
    skillTree: SkillTreeState,
    freeXp: Int,
    coins: Int,
    textColor: androidx.compose.ui.graphics.Color,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    onUpgrade: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.skill_tree_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Text(
                text = stringResource(R.string.skill_tree_balance, freeXp, coins),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = stringResource(R.string.skill_tree_hint),
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.7f)
        )

        SkillTree.branches.forEach { branch ->
            SkillBranchCard(
                branch = branch,
                tier = skillTree.tier(branch),
                freeXp = freeXp,
                coins = coins,
                colors = colors,
                textColor = textColor,
                onUpgrade = { onUpgrade(branch.id) }
            )
        }
    }
}

@Composable
private fun SkillBranchCard(
    branch: SkillBranch,
    tier: Int,
    freeXp: Int,
    coins: Int,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    onUpgrade: () -> Unit
) {
    val maxed = tier >= branch.maxTier
    val xpCost = SkillTree.nextXpCost(branch, tier)
    val coinCost = SkillTree.nextCoinCost(branch, tier)
    val affordable = !maxed && freeXp >= xpCost && coins >= coinCost
    val bonus = branch.bonusAt(tier)
    val description = when (branch.kind) {
        SkillBonusKind.EXTRA_TIME ->
            stringResource(branch.descRes, bonus)
        else ->
            stringResource(branch.descRes, bonus.toInt())
    }

    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = branch.emoji, fontSize = 30.sp)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(branch.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "$tier / ${branch.maxTier}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }

                LinearProgressIndicator(
                    progress = { tier / branch.maxTier.toFloat() },
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
                enabled = affordable,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = textColor.copy(alpha = 0.08f),
                    disabledContentColor = textColor.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (maxed) {
                    Text(
                        text = stringResource(R.string.skill_maxed),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.skill_cost_xp, xpCost),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.skill_cost_coins, coinCost),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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
    streakCurrent: Int,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryMetric(
                value = totalQuizzes.toString(),
                label = stringResource(R.string.stats_quizzes_completed),
                textColor = textColor,
                modifier = Modifier.weight(1f)
            )
            SummaryMetric(
                value = averageAccuracyPercent?.let { "$it%" }
                    ?: stringResource(R.string.em_dash),
                label = stringResource(R.string.stats_avg_accuracy),
                textColor = textColor,
                modifier = Modifier.weight(1f)
            )
            SummaryMetric(
                value = stringResource(R.string.stats_streak_value, streakCurrent),
                label = stringResource(R.string.stats_streak),
                textColor = textColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: AchievementUi,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val contentAlpha = if (achievement.unlocked) 1f else 0.45f
    GlassCard(modifier = modifier, colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPaddingH, AppDimens.CardPaddingV),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = achievement.emoji,
                fontSize = 30.sp,
                modifier = Modifier.alpha(contentAlpha)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(achievement.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor.copy(alpha = contentAlpha)
                )
                Text(
                    text = stringResource(achievement.descRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = contentAlpha * 0.8f)
                )
                if (!achievement.unlocked && achievement.target > 1) {
                    LinearProgressIndicator(
                        progress = { achievement.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .height(5.dp),
                        strokeCap = StrokeCap.Round,
                        trackColor = colors.progressTrack,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${achievement.current} / ${achievement.target}",
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            if (achievement.unlocked) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.achievement_reward, achievement.rewardCoins),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
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
    val valueStyle = if (value.length > 5) {
        MaterialTheme.typography.titleMedium
    } else {
        MaterialTheme.typography.titleLarge
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = valueStyle,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 14.sp,
            modifier = Modifier.fillMaxWidth()
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
        modifier = modifier.height(56.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
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