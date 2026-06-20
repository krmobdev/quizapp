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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.R
import com.rustam.quizapp.data.Category
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.ui.localization.labelRes
import com.rustam.quizapp.ui.localization.subtitleRes
import com.rustam.quizapp.ui.theme.QuizappTheme

@Composable
fun HomeScreen(
    onStartQuiz: (categoryId: String, difficulty: Difficulty?) -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    HomeContent(
        state = state,
        onCategoryClick = viewModel::selectCategory,
        onDifficultySelected = viewModel::selectDifficulty,
        onBack = viewModel::clearSelection,
        onStartQuiz = { categoryId, difficulty ->
            viewModel.playClick()
            onStartQuiz(categoryId, difficulty)
        },
        onOpenStats = {
            viewModel.playClick()
            onOpenStats()
        },
        onOpenSettings = {
            viewModel.playClick()
            onOpenSettings()
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
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = state.selectedCategory != null, onBack = onBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.selectedCategory == null) R.string.home_title
                            else R.string.difficulty_screen_title
                        )
                    )
                },
                navigationIcon = {
                    if (state.selectedCategory != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                actions = {
                    if (state.selectedCategory == null) {
                        TextButton(onClick = onOpenStats) {
                            Text(stringResource(R.string.stats))
                        }
                        TextButton(onClick = onOpenSettings) {
                            Text(stringResource(R.string.settings))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val selected = state.selectedCategory
        if (selected == null) {
            CategoryGrid(
                categories = state.categories,
                onCategoryClick = onCategoryClick,
                modifier = Modifier.padding(innerPadding)
            )
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
private fun CategoryGrid(
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = category.emoji, style = MaterialTheme.typography.displaySmall)
            Text(
                text = stringResource(category.titleRes),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
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
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.quiz_rules),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.start_quiz),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CategoryHeroCard(
    category: Category,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer
        )
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.question_bank_size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
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
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        animationSpec = tween(200),
        label = "difficultyContainer"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
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
        shape = RoundedCornerShape(20.dp),
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
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(filter.subtitleRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
            onStartQuiz = { _, _ -> },
            onOpenStats = {},
            onOpenSettings = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentDifficultyPreview() {
    QuizappTheme {
        HomeContent(
            state = HomeUiState(
                categories = listOf(Category("chemistry", R.string.category_chemistry, "🧪")),
                selectedCategory = Category("chemistry", R.string.category_chemistry, "🧪"),
                selectedDifficulty = DifficultyFilter.MEDIUM
            ),
            onCategoryClick = {},
            onDifficultySelected = {},
            onBack = {},
            onStartQuiz = { _, _ -> },
            onOpenStats = {},
            onOpenSettings = {}
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
            onStartQuiz = { _, _ -> },
            onOpenStats = {},
            onOpenSettings = {}
        )
    }
}