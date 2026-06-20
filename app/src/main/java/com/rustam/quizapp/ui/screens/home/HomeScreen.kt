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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.R
import com.rustam.quizapp.data.Category
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.ui.components.AppActionButton
import com.rustam.quizapp.ui.components.AppDimens
import com.rustam.quizapp.ui.components.AppShapes
import com.rustam.quizapp.ui.components.GlassCard
import com.rustam.quizapp.ui.components.rememberAppThemeColors
import com.rustam.quizapp.ui.components.appTextColor
import com.rustam.quizapp.ui.components.rememberDailyQuote
import com.rustam.quizapp.ui.localization.labelRes
import com.rustam.quizapp.ui.localization.subtitleRes
import com.rustam.quizapp.ui.theme.QuizappTheme

@Composable
fun HomeScreen(
    onStartQuiz: (categoryId: String, difficulty: Difficulty?) -> Unit,
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
        onStartQuiz = { categoryId, difficulty ->
            viewModel.playClick()
            onStartQuiz(categoryId, difficulty)
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
    onStartQuiz: (categoryId: String, difficulty: Difficulty?) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = state.selectedCategory != null, onBack = onBack)

    val selected = state.selectedCategory

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                HomeHeroHeader(modifier = Modifier.fillMaxWidth())
                state.dailyQuest?.let { quest ->
                    DailyQuestCard(
                        quest = quest,
                        onStartQuest = { onStartQuiz(quest.category.id, null) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
                CategoryGrid(
                    categories = state.categories,
                    onCategoryClick = onCategoryClick,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            DifficultyPanel(
                category = selected,
                selectedDifficulty = state.selectedDifficulty,
                onDifficultySelected = onDifficultySelected,
                onStart = { onStartQuiz(selected.id, state.selectedDifficulty.difficulty) },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun HomeHeroHeader(modifier: Modifier = Modifier) {
    val quote = rememberDailyQuote()
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val lift = (topInset + 16.dp) * 0.3f

    Column(
        modifier = modifier
            .offset(y = -lift)
            .padding(horizontal = 24.dp)
            .padding(top = 0.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (quote.isNotBlank()) {
            Text(
                text = quote,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun DailyQuestCard(
    quest: DailyQuestUi,
    onStartQuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()

    GlassCard(modifier = modifier, colors = colors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPaddingH, AppDimens.CardPaddingV),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.daily_quest_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Text(
                text = stringResource(
                    R.string.daily_quest_topic,
                    "${quest.category.emoji} ${stringResource(quest.category.titleRes)}"
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            Text(
                text = stringResource(R.string.daily_quest_bonus),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            if (quest.completed) {
                Text(
                    text = stringResource(R.string.daily_quest_completed),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                AppActionButton(
                    text = stringResource(R.string.daily_quest_start),
                    onClick = onStartQuest,
                    primary = true
                )
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories, key = { it.id }) { category ->
            CategoryCard(category = category, onClick = { onCategoryClick(category) })
        }
    }
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

    GlassCard(
        modifier = modifier,
        colors = colors,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconCircleColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = category.emoji, style = MaterialTheme.typography.headlineLarge)
            }
            Text(
                text = stringResource(category.titleRes),
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
            .padding(horizontal = 20.dp),
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

        Spacer(Modifier.weight(1f))

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
            onStartQuiz = { _, _ -> }
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
            onStartQuiz = { _, _ -> }
        )
    }
}