package com.rustam.quizapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rustam.quizapp.R
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.domain.QuizEventType
import com.rustam.quizapp.ui.components.AppBackground
import com.rustam.quizapp.ui.screens.home.HomeScreen
import com.rustam.quizapp.ui.screens.settings.SettingsScreen
import com.rustam.quizapp.ui.screens.shop.ShopScreen
import com.rustam.quizapp.ui.screens.stats.StatsScreen

private enum class MainTab { Home, Stats, Shop, Settings }

@Composable
fun MainShell(
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
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Home) }
    // Controls bottom nav visibility when HomeScreen enters "overlay" mode (category selected for difficulty picker).
    // HomeScreen notifies via onOverlayModeChange when selectedCategory changes.
    var hideBottomBar by rememberSaveable { mutableStateOf(false) }

    val textColor = MaterialTheme.colorScheme.onSurface

    AppBackground(modifier = modifier) {
        Scaffold(
            modifier = Modifier,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            bottomBar = {
                if (!hideBottomBar) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedTextColor = textColor,
                        unselectedTextColor = textColor,
                        selectedIconColor = textColor,
                        unselectedIconColor = textColor,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                    )
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == MainTab.Home,
                            onClick = { selectedTab = MainTab.Home },
                            icon = {
                                Icon(
                                    Icons.Rounded.Home,
                                    contentDescription = stringResource(R.string.home_title)
                                )
                            },
                            label = { Text(stringResource(R.string.home_title)) },
                            colors = navItemColors
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.Stats,
                            onClick = { selectedTab = MainTab.Stats },
                            icon = {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = stringResource(R.string.player_tab)
                                )
                            },
                            label = { Text(stringResource(R.string.player_tab)) },
                            colors = navItemColors
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.Shop,
                            onClick = { selectedTab = MainTab.Shop },
                            icon = {
                                Icon(
                                    Icons.Rounded.ShoppingCart,
                                    contentDescription = stringResource(R.string.shop_title)
                                )
                            },
                            label = { Text(stringResource(R.string.shop_title)) },
                            colors = navItemColors
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.Settings,
                            onClick = { selectedTab = MainTab.Settings },
                            icon = {
                                Icon(
                                    Icons.Rounded.Settings,
                                    contentDescription = stringResource(R.string.settings)
                                )
                            },
                            label = { Text(stringResource(R.string.settings)) },
                            colors = navItemColors
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
                MainTab.Stats -> StatsScreen(modifier = Modifier.padding(innerPadding).statusBarsPadding())
                MainTab.Shop -> ShopScreen(modifier = Modifier.padding(innerPadding).statusBarsPadding())
                MainTab.Settings -> SettingsScreen(modifier = Modifier.padding(innerPadding).statusBarsPadding())
            }
        }
    }
}