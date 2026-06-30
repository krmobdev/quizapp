package com.rustam.quizapp.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustam.quizapp.R
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.domain.QuizEventType
import com.rustam.quizapp.ui.components.AppBackground
import com.rustam.quizapp.ui.components.LocalAppFeedback
import com.rustam.quizapp.ui.components.rememberAppThemeColors
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
    onOpenMillionaire: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab  by rememberSaveable { mutableStateOf(MainTab.Home) }
    var hideBottomBar by rememberSaveable { mutableStateOf(false) }
    val colors = rememberAppThemeColors()

    AppBackground(modifier = modifier) {
        Scaffold(
            modifier       = Modifier,
            containerColor = Color.Transparent,
            bottomBar      = {
                AnimatedVisibility(
                    visible = !hideBottomBar,
                    enter   = slideInVertically { it } + fadeIn(),
                    exit    = slideOutVertically { it } + fadeOut()
                ) {
                    IosTabBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        colors = colors
                    )
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState   = selectedTab,
                transitionSpec = {
                    (fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.97f))
                        .togetherWith(fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.97f))
                },
                label = "tabContent"
            ) { tab ->
                when (tab) {
                    MainTab.Home -> HomeScreen(
                        onStartQuiz          = onStartQuiz,
                        onOpenShop           = { selectedTab = MainTab.Shop },
                        onOpenMillionaire    = onOpenMillionaire,
                        onOverlayModeChange  = { hideBottomBar = it },
                        modifier             = Modifier.padding(innerPadding)
                    )
                    MainTab.Stats    -> StatsScreen(modifier    = Modifier.padding(innerPadding).statusBarsPadding())
                    MainTab.Shop     -> ShopScreen(modifier     = Modifier.padding(innerPadding).statusBarsPadding())
                    MainTab.Settings -> SettingsScreen(modifier = Modifier.padding(innerPadding).statusBarsPadding())
                }
            }
        }
    }
}

// в”Ђв”Ђ iOS-style tab bar в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
@Composable
private fun IosTabBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    colors: com.rustam.quizapp.ui.components.AppThemeColors
) {
    // Frosted glass backing вЂ” white/dark translucent strip
    val bgColor = if (colors.isDark) Color(0xFF1C1C1E).copy(alpha = 0.92f)
                  else Color.White.copy(alpha = 0.92f)
    val borderColor = if (colors.isDark) Color(0xFF3A3A3C) else Color(0xFFD1D1D6)

    // windowInsetsPadding is on the outer Box so the background extends behind the
    // system navigation bar while the Row keeps its full 56 dp for content.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Top hairline separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(borderColor)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IosTabItem(
                icon     = Icons.Rounded.Home,
                label    = stringResource(R.string.home_title),
                selected = selectedTab == MainTab.Home,
                onClick  = { onTabSelected(MainTab.Home) }
            )
            IosTabItem(
                icon     = Icons.Rounded.Person,
                label    = stringResource(R.string.player_tab),
                selected = selectedTab == MainTab.Stats,
                onClick  = { onTabSelected(MainTab.Stats) }
            )
            IosTabItem(
                icon     = Icons.Rounded.ShoppingCart,
                label    = stringResource(R.string.shop_title),
                selected = selectedTab == MainTab.Shop,
                onClick  = { onTabSelected(MainTab.Shop) }
            )
            IosTabItem(
                icon     = Icons.Rounded.Settings,
                label    = stringResource(R.string.settings),
                selected = selectedTab == MainTab.Settings,
                onClick  = { onTabSelected(MainTab.Settings) }
            )
        }
    }
}

@Composable
private fun IosTabItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val activeColor   = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f)
    val iconColor     = if (selected) activeColor else inactiveColor

    // Spring scale bounce on selection
    val scale by animateFloatAsState(
        targetValue   = if (selected) 1.10f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "tabScale"
    )

    // Pill indicator alpha
    val pillAlpha by animateFloatAsState(
        targetValue   = if (selected) 1f else 0f,
        animationSpec = tween(200),
        label = "pillAlpha"
    )

    val feedback = LocalAppFeedback.current
    // Layout budget: vertical padding 4+4 = 8dp, pill 26dp, gap 2dp, text ~12dp → total ~48dp
    // Fits comfortably inside the 56dp Row.
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = { feedback?.click(); onClick() }
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Subtle pill behind icon when selected
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(activeColor.copy(alpha = 0.12f * pillAlpha))
            )
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = iconColor,
                modifier           = Modifier.size(20.dp).scale(scale)
            )
        }
        Text(
            text       = label,
            fontSize   = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color      = iconColor
        )
    }
}
