package com.rustam.quizapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rustam.quizapp.R
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.ui.components.AppBackground
import com.rustam.quizapp.ui.screens.home.HomeScreen
import com.rustam.quizapp.ui.screens.settings.SettingsScreen
import com.rustam.quizapp.ui.screens.stats.StatsScreen

private enum class MainTab { Home, Stats, Settings }

@Composable
fun MainShell(
    onStartQuiz: (categoryId: String, difficulty: Difficulty?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Home) }
    var hideBottomBar by rememberSaveable { mutableStateOf(false) }

    AppBackground(modifier = modifier) {
        Scaffold(
            modifier = Modifier,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            bottomBar = {
                if (!hideBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == MainTab.Home,
                            onClick = { selectedTab = MainTab.Home },
                            icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                            label = { Text(stringResource(R.string.home_title)) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.Stats,
                            onClick = { selectedTab = MainTab.Stats },
                            icon = { Icon(Icons.Rounded.BarChart, contentDescription = null) },
                            label = { Text(stringResource(R.string.stats)) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.Settings,
                            onClick = { selectedTab = MainTab.Settings },
                            icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                            label = { Text(stringResource(R.string.settings)) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            when (selectedTab) {
                MainTab.Home -> HomeScreen(
                    onStartQuiz = onStartQuiz,
                    onOverlayModeChange = { hideBottomBar = it },
                    modifier = Modifier.padding(innerPadding)
                )
                MainTab.Stats -> StatsScreen(modifier = Modifier.padding(innerPadding))
                MainTab.Settings -> SettingsScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}