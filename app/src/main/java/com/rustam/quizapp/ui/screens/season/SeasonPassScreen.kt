package com.rustam.quizapp.ui.screens.season

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.R
import com.rustam.quizapp.domain.SeasonPass
import com.rustam.quizapp.domain.SeasonRewardKind
import com.rustam.quizapp.ui.components.AppDimens
import com.rustam.quizapp.ui.components.GlassCard
import com.rustam.quizapp.ui.components.appTextColor
import com.rustam.quizapp.ui.components.rememberAppThemeColors

@Composable
fun SeasonPassDialog(
    onDismiss: () -> Unit,
    viewModel: SeasonPassViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        SeasonPassContent(
            state = state,
            onClaim = viewModel::claimReward,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun SeasonPassContent(
    state: SeasonPassUiState,
    onClaim: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()
    val currentLevel = SeasonPass.level(state.seasonXp)
    val xpInCurrentLevel = state.seasonXp - SeasonPass.xpForLevel(currentLevel)
    val xpProgress = if (currentLevel < SeasonPass.MAX_LEVEL) {
        xpInCurrentLevel / SeasonPass.XP_PER_LEVEL.toFloat()
    } else 1f

    val progressAnim by animateFloatAsState(
        targetValue = xpProgress,
        animationSpec = tween(600),
        label = "seasonXpProgress"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxSize(0.9f),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.season_pass_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = stringResource(R.string.season_pass_days_left, state.daysLeft),
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.semantics {
                                contentDescription = "Close Season Pass"
                            }
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = null, tint = textColor)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(
                            R.string.season_pass_level,
                            currentLevel,
                            SeasonPass.MAX_LEVEL
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressAnim },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(
                                R.string.season_pass_xp_progress,
                                state.seasonXp,
                                SeasonPass.xpForLevel(currentLevel + 1)
                                    .coerceAtMost(SeasonPass.xpForLevel(SeasonPass.MAX_LEVEL))
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.7f)
                        )
                        Text(
                            text = stringResource(
                                R.string.season_pass_xp_hint,
                                SeasonPass.XP_PER_QUIZ,
                                SeasonPass.XP_PER_LEVEL
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.55f)
                        )
                    }
                }
            }

            // Reward track list
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed((1..SeasonPass.MAX_LEVEL).toList()) { _, level ->
                    val reward = SeasonPass.reward(level)
                    val reached = currentLevel >= level
                    val claimed = SeasonPass.isClaimed(state.seasonClaimedMask, level)
                    val canClaim = SeasonPass.canClaim(level, state.seasonXp, state.seasonClaimedMask)

                    SeasonRewardRow(
                        level = level,
                        reward = rewardLabel(reward.kind, reward.amount),
                        reached = reached,
                        claimed = claimed,
                        canClaim = canClaim,
                        onClaim = { onClaim(level) },
                        colors = colors,
                        textColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SeasonRewardRow(
    level: Int,
    reward: String,
    reached: Boolean,
    claimed: Boolean,
    canClaim: Boolean,
    onClaim: () -> Unit,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color
) {
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Level badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (reached) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$level",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (reached) MaterialTheme.colorScheme.onPrimary
                    else textColor.copy(alpha = 0.5f)
                )
            }

            // Reward label
            Text(
                text = reward,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (reached) textColor else textColor.copy(alpha = 0.45f),
                modifier = Modifier.weight(1f)
            )

            // Action button
            when {
                claimed -> Text(
                    text = stringResource(R.string.season_pass_claimed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
                canClaim -> Button(
                    onClick = onClaim,
                    modifier = Modifier
                        .width(84.dp)
                        .height(32.dp)
                        .semantics { contentDescription = "Claim season level $level reward" },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.season_pass_claim),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                !reached -> Text(
                    text = stringResource(R.string.season_pass_locked),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.35f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun rewardLabel(kind: SeasonRewardKind, amount: Int): String = when (kind) {
    SeasonRewardKind.COINS -> stringResource(R.string.season_pass_reward_coins, amount)
    SeasonRewardKind.GEMS -> stringResource(R.string.season_pass_reward_gems, amount)
    SeasonRewardKind.XP -> stringResource(R.string.inventory_booster_reward, amount)
    SeasonRewardKind.BOOSTER -> stringResource(R.string.season_pass_reward_booster)
}
