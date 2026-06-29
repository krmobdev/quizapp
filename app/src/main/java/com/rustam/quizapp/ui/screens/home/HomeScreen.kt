package com.rustam.quizapp.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.R
import com.rustam.quizapp.data.Category
import com.rustam.quizapp.data.DailyChallengeProgress
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.domain.QuizEventProgress
import com.rustam.quizapp.domain.QuizEventType
import com.rustam.quizapp.ui.components.AppActionButton
import com.rustam.quizapp.ui.components.AppDimens
import com.rustam.quizapp.ui.components.AppShapes
import com.rustam.quizapp.ui.components.GlassCard
import com.rustam.quizapp.ui.components.rememberAppThemeColors
import com.rustam.quizapp.ui.components.appTextColor
import com.rustam.quizapp.ui.components.rememberDailyQuote
import com.rustam.quizapp.ui.localization.labelRes
import com.rustam.quizapp.ui.localization.subtitleRes
import com.rustam.quizapp.ui.screens.season.SeasonPassDialog
import com.rustam.quizapp.ui.theme.QuizappTheme
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onStartQuiz: (
        categoryId: String,
        difficulty: Difficulty?,
        event: QuizEventType?,
        questionTimeSeconds: Int,
        questionCount: Int,
        adaptive: Boolean
    ) -> Unit,
    onOpenShop: () -> Unit = {},
    onOpenMillionaire: () -> Unit = {},
    onOverlayModeChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.selectedCategory) {
        onOverlayModeChange(state.selectedCategory != null)
    }
    HomeContent(
        state = state,
        onCategoryClick = viewModel::selectCategory,
        onDifficultySelected = viewModel::selectDifficulty,
        onBack = viewModel::clearSelection,
        onClaimDaily = viewModel::claimDailyReward,
        onClaimQuest = viewModel::claimDailyQuest,
        onOpenShop = onOpenShop,
        onOpenMillionaire = onOpenMillionaire,
        onStartQuiz = { categoryId, difficulty, event, timeLimit, questionCount, adaptive ->
            viewModel.playClick()
            onStartQuiz(categoryId, difficulty, event, timeLimit, questionCount, adaptive)
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState,
    onCategoryClick: (Category) -> Unit,
    onDifficultySelected: (DifficultyFilter) -> Unit,
    onBack: () -> Unit,
    onClaimDaily: () -> Unit,
    onClaimQuest: (Int) -> Unit,
    onOpenShop: () -> Unit,
    onOpenMillionaire: () -> Unit,
    onStartQuiz: (
        categoryId: String,
        difficulty: Difficulty?,
        event: QuizEventType?,
        questionTimeSeconds: Int,
        questionCount: Int,
        adaptive: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = state.selectedCategory != null, onBack = onBack)

    val selected = state.selectedCategory

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            if (selected != null) {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    title = {
                        Text(
                            stringResource(R.string.difficulty_screen_title),
                            color = appTextColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        if (selected == null) {
            HomeScrollContent(
                events = state.events,
                categories = state.categories,
                streak = state.streak,
                coinBoostLeft = state.coinBoostLeft,
                xpBoostLeft = state.xpBoostLeft,
                dailyReward = state.dailyReward,
                dailyQuests = state.dailyQuests,
                seasonLevel = state.seasonLevel,
                seasonDaysLeft = state.seasonDaysLeft,
                playerLevel = state.playerLevel,
                onOpenShop = onOpenShop,
                onOpenMillionaire = onOpenMillionaire,
                onClaimDaily = onClaimDaily,
                onClaimQuest = onClaimQuest,
                onCategoryClick = onCategoryClick,
                onStartEvent = { progress ->
                    val event = progress.event
                    onStartQuiz(
                        event.category.id,
                        event.difficulty,
                        event.type,
                        event.questionTimeSeconds,
                        event.questionCount,
                        false
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            DifficultyPanel(
                category = selected,
                selectedDifficulty = state.selectedDifficulty,
                onDifficultySelected = onDifficultySelected,
                onStart = {
                    onStartQuiz(
                        selected.id,
                        state.selectedDifficulty.difficulty,
                        null,
                        10,
                        10,
                        state.selectedDifficulty.isAdaptive
                    )
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun HomeScrollContent(
    events: List<QuizEventProgress>,
    categories: List<Category>,
    streak: Int,
    coinBoostLeft: Int,
    xpBoostLeft: Int,
    dailyReward: com.rustam.quizapp.data.DailyRewardState,
    dailyQuests: List<DailyChallengeProgress>,
    seasonLevel: Int,
    seasonDaysLeft: Int,
    playerLevel: Int,
    onOpenShop: () -> Unit,
    onOpenMillionaire: () -> Unit,
    onClaimDaily: () -> Unit,
    onClaimQuest: (Int) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onStartEvent: (QuizEventProgress) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
        )
    ) {
        item {
            HomeHeroHeader(
                streak = streak,
                coinBoostLeft = coinBoostLeft,
                xpBoostLeft = xpBoostLeft,
                seasonLevel = seasonLevel,
                seasonDaysLeft = seasonDaysLeft,
                onOpenShop = onOpenShop,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            MillionaireEntryCard(
                onClick = onOpenMillionaire,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }
        if (dailyReward.canClaim) {
            item {
                DailyRewardCard(
                    reward = dailyReward,
                    onClaim = onClaimDaily,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        }
        if (dailyQuests.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.quests_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = appTextColor(),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
            itemsIndexed(dailyQuests, key = { _, quest -> quest.challenge.id }) { index, quest ->
                DailyQuestCard(
                    quest = quest,
                    playerLevel = playerLevel,
                    onClaim = { onClaimQuest(index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        }
        if (events.isNotEmpty()) {
            item {
                EventsCarousel(
                    events = events,
                    onStartEvent = onStartEvent,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.home_categories_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = appTextColor(),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
        items(categories.chunked(2), key = { row -> row.joinToString { it.id } }) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MillionaireEntryCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()
    GlassCard(modifier = modifier, colors = colors, onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "💰", fontSize = 34.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.millionaire_home_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = stringResource(R.string.millionaire_home_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.75f)
                )
            }
            Text(
                text = stringResource(R.string.millionaire_buy),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DailyRewardCard(
    reward: com.rustam.quizapp.data.DailyRewardState,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()
    GlassCard(modifier = modifier, colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "🎁", fontSize = 34.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.daily_reward_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = stringResource(R.string.daily_reward_subtitle, reward.dayIndex, reward.rewardCoins),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.75f)
                )
                if (reward.rewardGems > 0) {
                    Text(
                        text = stringResource(R.string.daily_reward_gems, reward.rewardGems),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = AppShapes.Button
            ) {
                Text(
                    text = stringResource(R.string.daily_reward_claim),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DailyQuestCard(
    quest: DailyChallengeProgress,
    playerLevel: Int,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()
    val challenge = quest.challenge
    val questMult = com.rustam.quizapp.domain.CharacterLevelCalculator.questRewardMultiplier(playerLevel)
    // Round the same way HomeViewModel.claimDailyQuest credits the reward, so the preview
    // matches the coins/XP actually granted (toInt would truncate and show one less).
    val scaledCoins = (challenge.rewardCoins * questMult).roundToInt()
    val scaledXp = (challenge.rewardXp * questMult).roundToInt()
    val isScaled = playerLevel > 1

    GlassCard(modifier = modifier, colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = challenge.emoji, fontSize = 30.sp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(challenge.titleRes, challenge.target),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                LinearProgressIndicator(
                    progress = { quest.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    strokeCap = StrokeCap.Round,
                    trackColor = colors.progressTrack,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(R.string.player_fraction, quest.current, challenge.target),
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.75f)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.quest_reward, scaledCoins, scaledXp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isScaled) {
                            Text(
                                text = stringResource(
                                    R.string.quest_level_bonus,
                                    playerLevel,
                                    "×${"%.2f".format(questMult)}"
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
            when {
                quest.claimed -> {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = stringResource(R.string.quest_claimed),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                quest.canClaim -> {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = AppShapes.Button
                    ) {
                        Text(
                            text = stringResource(R.string.daily_reward_claim),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeroHeader(
    streak: Int,
    coinBoostLeft: Int,
    xpBoostLeft: Int,
    seasonLevel: Int,
    seasonDaysLeft: Int,
    onOpenShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quote = rememberDailyQuote()
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var showSeasonPass by remember { mutableStateOf(false) }

    if (showSeasonPass) {
        SeasonPassDialog(onDismiss = { showSeasonPass = false })
    }

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .padding(top = topInset + 8.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (streak > 0) {
            StreakChip(streak = streak)
        }
        if (coinBoostLeft > 0 || xpBoostLeft > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (coinBoostLeft > 0) {
                    BoostChip(
                        text = stringResource(R.string.boost_active_coins, coinBoostLeft),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (xpBoostLeft > 0) {
                    BoostChip(
                        text = stringResource(R.string.boost_active_xp, xpBoostLeft),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { showSeasonPass = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = AppShapes.Button
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(text = "🏆", fontSize = 20.sp)
                    Text(
                        text = stringResource(R.string.season_pass_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.season_pass_home_card, seasonLevel, seasonDaysLeft),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Button(
                onClick = onOpenShop,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                shape = AppShapes.Button
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(text = "🏷️", fontSize = 20.sp)
                    Text(
                        text = stringResource(R.string.home_shop_deals_btn),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.home_shop_deals_hint),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        if (quote.isNotBlank()) {
            Text(
                text = quote,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun StreakChip(streak: Int, modifier: Modifier = Modifier) {
    Surface(
        shape = AppShapes.Badge,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.home_streak_chip, streak),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = appTextColor(),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun BoostChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        shape = AppShapes.Badge,
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = appTextColor(),
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EventsCarousel(
    events: List<QuizEventProgress>,
    onStartEvent: (QuizEventProgress) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.events_section_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = appTextColor(),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(events, key = { it.event.type.name }) { progress ->
                EventCard(
                    progress = progress,
                    onStart = { onStartEvent(progress) },
                    modifier = Modifier.width(280.dp)
                )
            }
        }
    }
}

@Composable
private fun EventCard(
    progress: QuizEventProgress,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()
    val event = progress.event
    val statusText = when {
        !progress.available -> stringResource(eventCompletedRes(event.type))
        event.maxCompletions > 1 -> stringResource(
            R.string.event_progress,
            progress.completions,
            event.maxCompletions
        )
        else -> eventRulesText(event)
    }

    GlassCard(modifier = modifier, colors = colors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(eventTitleRes(event.type)),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 1
            )
            Text(
                text = "${event.category.emoji} ${stringResource(event.category.titleRes)}",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                maxLines = 1
            )
            Text(
                text = stringResource(eventBonusRes(event.type), event.coinMultiplier, event.bonusPoints),
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.85f),
                maxLines = 1
            )
            if (statusText != null) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (!progress.available) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (!progress.available) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        textColor.copy(alpha = 0.75f)
                    },
                    maxLines = 2
                )
            }
            if (progress.available) {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = AppShapes.Button,
                    colors = ButtonDefaults.buttonColors(contentColor = appTextColor())
                ) {
                    Text(
                        text = stringResource(R.string.event_start),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun eventRulesText(event: com.rustam.quizapp.domain.QuizEvent): String? = when (event.type) {
    QuizEventType.DAILY -> stringResource(R.string.event_rules_daily)
    QuizEventType.WEEKLY -> stringResource(R.string.event_rules_weekly)
    QuizEventType.WEEKEND_BLITZ -> stringResource(
        R.string.event_rules_weekend_blitz,
        event.questionTimeSeconds
    )
    QuizEventType.MARATHON -> stringResource(
        R.string.event_rules_marathon,
        event.questionCount
    )
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
private fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()
    val iconCircleColor = if (colors.isDark) {
        Color(0xFF141A18)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    }

    val categoryName = stringResource(category.titleRes)
    val categoryDesc = stringResource(R.string.a11y_category_card, categoryName)
    GlassCard(
        modifier = modifier.semantics { contentDescription = categoryDesc },
        colors = colors,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconCircleColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = category.emoji, style = MaterialTheme.typography.headlineLarge)
            }
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DifficultyPanel(
    category: Category,
    selectedDifficulty: DifficultyFilter,
    onDifficultySelected: (DifficultyFilter) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CategoryHeroCard(category = category)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.choose_level),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = appTextColor()
                )
                Text(
                    text = stringResource(R.string.quiz_rules),
                    style = MaterialTheme.typography.bodyMedium,
                    color = appTextColor()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DifficultyFilter.entries.forEach { filter ->
                    DifficultyOptionCard(
                        filter = filter,
                        selected = filter == selectedDifficulty,
                        onClick = { onDifficultySelected(filter) }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        AppActionButton(
            text = stringResource(R.string.start_quiz),
            onClick = onStart,
            primary = true
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CategoryHeroCard(
    category: Category,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer
        )
    )
    GlassCard(modifier = modifier, colors = colors) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = category.emoji, style = MaterialTheme.typography.displayMedium)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(category.titleRes),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = appTextColor()
                    )
                    Text(
                        text = stringResource(R.string.question_bank_size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = appTextColor()
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultyOptionCard(
    filter: DifficultyFilter,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
        } else {
            colors.glassCard
        },
        animationSpec = tween(200),
        label = "difficultyContainer"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            colors.glassBorder
        },
        animationSpec = tween(200),
        label = "difficultyBorder"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = tween(200),
        label = "difficultyScale"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 4.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = filter.emoji, style = MaterialTheme.typography.headlineSmall)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(filter.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = appTextColor()
                )
                Text(
                    text = stringResource(filter.subtitleRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = appTextColor()
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentCategoriesPreview() {
    QuizappTheme {
        HomeContent(
            state = HomeUiState(
                categories = listOf(Category("chemistry", R.string.category_chemistry, "🧪"))
            ),
            onCategoryClick = {},
            onDifficultySelected = {},
            onBack = {},
            onClaimDaily = {},
            onClaimQuest = {},
            onOpenShop = {},
            onOpenMillionaire = {},
            onStartQuiz = { _, _, _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Difficulty dark")
@Composable
private fun HomeContentDifficultyDarkPreview() {
    QuizappTheme(darkTheme = true) {
        HomeContent(
            state = HomeUiState(
                categories = listOf(Category("chemistry", R.string.category_chemistry, "🧪")),
                selectedCategory = Category("chemistry", R.string.category_chemistry, "🧪"),
                selectedDifficulty = DifficultyFilter.HARD
            ),
            onCategoryClick = {},
            onDifficultySelected = {},
            onBack = {},
            onClaimDaily = {},
            onClaimQuest = {},
            onOpenShop = {},
            onOpenMillionaire = {},
            onStartQuiz = { _, _, _, _, _, _ -> }
        )
    }
}