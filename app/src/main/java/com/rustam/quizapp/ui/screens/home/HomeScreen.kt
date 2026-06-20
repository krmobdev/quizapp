package com.rustam.quizapp.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.data.Category
import com.rustam.quizapp.data.Difficulty
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
                title = { Text("Викторина") },
                actions = {
                    TextButton(onClick = onOpenStats) {
                        Text("Статистика")
                    }
                    TextButton(onClick = onOpenSettings) {
                        Text("Настройки")
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
                onBack = onBack,
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
        modifier = modifier.fillMaxWidth()
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
                text = category.title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DifficultyPanel(
    category: Category,
    selectedDifficulty: DifficultyFilter,
    onDifficultySelected: (DifficultyFilter) -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "${category.emoji}  ${category.title}",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Выберите сложность",
            style = MaterialTheme.typography.titleMedium
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DifficultyFilter.entries.forEach { filter ->
                FilterChip(
                    selected = filter == selectedDifficulty,
                    onClick = { onDifficultySelected(filter) },
                    label = { Text(filter.label) }
                )
            }
        }
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Начать квиз")
        }
        TextButton(onClick = onBack) {
            Text("Назад")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentCategoriesPreview() {
    QuizappTheme {
        HomeContent(
            state = HomeUiState(
                categories = listOf(Category("chemistry", "Химия", "🧪"))
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
                categories = listOf(Category("chemistry", "Химия", "🧪")),
                selectedCategory = Category("chemistry", "Химия", "🧪"),
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
