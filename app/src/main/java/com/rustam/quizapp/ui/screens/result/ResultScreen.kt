package com.rustam.quizapp.ui.screens.result

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.rustam.quizapp.domain.Achievement
import com.rustam.quizapp.domain.CharacterLevelCalculator
import com.rustam.quizapp.domain.QuizEventType
import com.rustam.quizapp.domain.QuizResult
import com.rustam.quizapp.domain.QuizReward
import com.rustam.quizapp.ui.components.AppActionButton
import com.rustam.quizapp.ui.components.AppBackground
import com.rustam.quizapp.ui.components.AppDimens
import com.rustam.quizapp.ui.components.AppThemeColors
import com.rustam.quizapp.ui.components.ConfettiBurst
import com.rustam.quizapp.ui.components.GlassCard
import com.rustam.quizapp.ui.components.ScoreRing
import com.rustam.quizapp.ui.components.appTextColor
import com.rustam.quizapp.ui.components.rememberAppThemeColors
import com.rustam.quizapp.ui.components.rememberHapticHelper
import com.rustam.quizapp.ui.theme.QuizappTheme

@Composable
fun ResultScreen(
    result: QuizResult?,
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
        onHome = onHome,
        modifier = modifier
    )
}

@Composable
private fun ResultContent(
    result: QuizResult,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()
    val haptic = rememberHapticHelper()
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
    // Perfect run (e.g. 10/10 or 15/15): celebrate with a confetti cannon.
    val isPerfect = result.total > 0 && result.correct == result.total
    val reward = result.reward
    // Level-up also triggers confetti burst and celebration haptic.
    val isLevelUp = reward?.leveledUp == true
    LaunchedEffect(isLevelUp) {
        if (isLevelUp) haptic.celebrate()
    }
    LaunchedEffect(isPerfect) {
        if (isPerfect) haptic.celebrate()
    }

    AppBackground(modifier = modifier) {
      Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                            color = textColor,
                            modifier = Modifier.alpha(headlineAlpha)
                        )
                        Spacer(Modifier.height(20.dp))

                        ScoreRing(score = result.score, total = result.total)

                        result.reward?.let { reward ->
                            Spacer(Modifier.height(12.dp))

                            if (reward.isCriticalSuccess) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.char_critical_success),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                            }

                            Text(
                                text = rewardText(reward),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (reward.speedBonusPercent > 0) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = stringResource(
                                        R.string.rewards_speed_bonus,
                                        reward.speedBonusPercent
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = textColor
                                )
                            }
                            if (reward.gemsEarned > 0) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.rewards_gems_earned, reward.gemsEarned),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            RewardBreakdownCard(reward = reward, colors = colors, textColor = textColor)

                            if (reward.leveledUp) {
                                Spacer(Modifier.height(12.dp))
                                LevelUpBanner(reward = reward, textColor = textColor)
                            }
                        }

                        if (result.penalties > 0) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.result_penalties, result.correct, result.penalties),
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                        }

                        if (result.newAchievements.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            NewAchievementsBanner(result.newAchievements, textColor)
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }

                if (result.mistakes.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.result_mistakes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                    items(result.mistakes, key = { it.id }) { question ->
                        MistakeItem(question, colors)
                        Spacer(Modifier.height(AppDimens.CardSpacing))
                    }
                } else {
                    item {
                        Text(
                            text = stringResource(R.string.result_perfect),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            AppActionButton(
                text = stringResource(R.string.result_home),
                onClick = onHome,
                primary = true
            )
        }
        if (isPerfect || isLevelUp) {
            ConfettiBurst(
                particleCount = if (isLevelUp && !isPerfect) 80 else 150
            )
        }
      }
    }
}

@Composable
private fun NewAchievementsBanner(
    achievements: List<Achievement>,
    textColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.result_new_achievements),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            achievements.forEach { achievement ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = achievement.emoji, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = stringResource(achievement.titleRes),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.achievement_reward, achievement.rewardCoins),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
                fontWeight = FontWeight.Medium,
                color = appTextColor()
            )
            Text(
                text = stringResource(
                    R.string.result_correct_answer,
                    question.options[question.correctIndex]
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            question.explanation?.let { explanation ->
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = appTextColor().copy(alpha = 0.8f)
                )
            }
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
                style = MaterialTheme.typography.titleLarge,
                color = appTextColor()
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
private fun RewardBreakdownCard(
    reward: QuizReward,
    colors: AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color
) {
    val scaledPoints = (reward.basePoints * reward.levelMultiplier).toInt()
    val scaledCoins = (reward.baseCoins * reward.coinLevelMultiplier).toInt()

    GlassCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.reward_breakdown_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            BreakdownRow(
                label = stringResource(R.string.reward_breakdown_base),
                value = stringResource(R.string.reward_breakdown_value, reward.basePoints, reward.baseCoins),
                textColor = textColor
            )
            if (reward.levelMultiplier > 1f) {
                BreakdownRow(
                    label = stringResource(R.string.reward_breakdown_level, reward.levelMultiplier),
                    value = stringResource(R.string.reward_breakdown_value, scaledPoints, scaledCoins),
                    textColor = textColor
                )
            }
            if (reward.xpBonus > 0 || reward.coinBonus > 0) {
                BreakdownRow(
                    label = stringResource(R.string.reward_breakdown_stats),
                    value = stringResource(R.string.reward_breakdown_value, reward.xpBonus, reward.coinBonus),
                    textColor = textColor
                )
            }
            if (reward.isCriticalSuccess) {
                BreakdownRow(
                    label = stringResource(R.string.reward_breakdown_crit, reward.critMultiplier),
                    value = stringResource(R.string.reward_breakdown_value, reward.points, reward.coins),
                    textColor = MaterialTheme.colorScheme.primary
                )
            }
            if (reward.xpBoosted || reward.coinBoosted) {
                BreakdownRow(
                    label = stringResource(R.string.reward_breakdown_boost),
                    value = stringResource(R.string.reward_breakdown_value, reward.points, reward.coins),
                    textColor = MaterialTheme.colorScheme.primary
                )
            }
            BreakdownRow(
                label = stringResource(R.string.reward_breakdown_total),
                value = stringResource(R.string.reward_breakdown_value, reward.points, reward.coins),
                textColor = MaterialTheme.colorScheme.primary,
                emphasised = true
            )
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    textColor: androidx.compose.ui.graphics.Color,
    emphasised: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasised) FontWeight.Bold else FontWeight.Normal,
            color = textColor.copy(alpha = if (emphasised) 1f else 0.85f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasised) FontWeight.Bold else FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun LevelUpBanner(
    reward: QuizReward,
    textColor: androidx.compose.ui.graphics.Color
) {
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val rankName = if (locale.language == "en") {
        CharacterLevelCalculator.getLevelRankEn(reward.newLevel)
    } else {
        CharacterLevelCalculator.getLevelRank(reward.newLevel)
    }
    val multiplier = CharacterLevelCalculator.rewardMultiplier(reward.newLevel)

    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.result_levelup_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.result_levelup_detail, reward.newLevel, rankName, multiplier),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun rewardText(reward: QuizReward): String {
    val xpBonusText = if (reward.xpBonus > 0) {
        stringResource(R.string.char_bonus_xp_detail, reward.xpBonus)
    } else ""
    val coinBonusText = if (reward.coinBonus > 0) {
        stringResource(R.string.char_bonus_coins_detail, reward.coinBonus)
    } else ""

    val pointsLabel = stringResource(R.string.rewards_points_label)
    val coinsLabel = stringResource(R.string.rewards_coins_label)
    
    val finalPointsText = "+${reward.points} $pointsLabel$xpBonusText"
    val finalCoinsText = "+${reward.coins} $coinsLabel$coinBonusText"
    val baseText = "$finalPointsText · $finalCoinsText"
    
    if (!reward.hasEventBonus) return baseText
    val eventLabel = when (reward.eventBonus) {
        QuizEventType.DAILY -> stringResource(R.string.event_daily_title)
        QuizEventType.WEEKLY -> stringResource(R.string.event_weekly_title)
        QuizEventType.WEEKEND_BLITZ -> stringResource(R.string.event_weekend_blitz_title)
        QuizEventType.MARATHON -> stringResource(R.string.event_marathon_title)
        null -> return baseText
    }
    return "$baseText · $eventLabel"
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
            onHome = {}
        )
    }
}