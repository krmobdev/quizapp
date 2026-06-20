package com.rustam.quizapp.ui.screens.shop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.R
import com.rustam.quizapp.ui.components.AppDimens
import com.rustam.quizapp.ui.components.AppShapes
import com.rustam.quizapp.ui.components.AppThemeColors
import com.rustam.quizapp.ui.components.GlassCard
import com.rustam.quizapp.ui.components.ScreenTitle
import com.rustam.quizapp.ui.components.appTextColor
import com.rustam.quizapp.ui.components.rememberAppThemeColors

@Composable
fun ShopScreen(
    modifier: Modifier = Modifier,
    viewModel: ShopViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(AppDimens.CardSpacing)
    ) {
        item {
            ScreenTitle(
                title = stringResource(R.string.shop_title),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        item {
            BalanceCard(coins = state.coins, colors = colors, textColor = textColor)
        }
        item {
            SectionHeader(text = stringResource(R.string.shop_avatars), textColor = textColor)
        }
        item {
            AvatarsCard(
                avatars = state.avatars,
                coins = state.coins,
                colors = colors,
                textColor = textColor,
                onClick = viewModel::onAvatarClick
            )
        }
        item {
            SectionHeader(text = stringResource(R.string.shop_themes), textColor = textColor)
        }
        items(state.themes, key = { it.item.id }) { theme ->
            ThemeCard(
                theme = theme,
                coins = state.coins,
                colors = colors,
                textColor = textColor,
                onClick = { viewModel.onThemeClick(theme) }
            )
        }
    }
}

@Composable
private fun BalanceCard(
    coins: Int,
    colors: AppThemeColors,
    textColor: Color
) {
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.shop_balance),
                style = MaterialTheme.typography.titleMedium,
                color = textColor
            )
            Text(
                text = "🪙 $coins",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String, textColor: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun AvatarsCard(
    avatars: List<AvatarUi>,
    coins: Int,
    colors: AppThemeColors,
    textColor: Color,
    onClick: (AvatarUi) -> Unit
) {
    GlassCard(colors = colors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            avatars.chunked(4).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { avatar ->
                        AvatarCell(
                            avatar = avatar,
                            coins = coins,
                            colors = colors,
                            textColor = textColor,
                            onClick = { onClick(avatar) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Pad the last row so cells keep their width.
                    repeat(4 - rowItems.size) { Box(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun AvatarCell(
    avatar: AvatarUi,
    coins: Int,
    colors: AppThemeColors,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val affordable = avatar.owned || coins >= avatar.item.priceCoins
    val tileColor = when {
        avatar.equipped -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        else -> colors.answerCard
    }
    val borderColor = if (avatar.equipped) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    } else {
        colors.glassBorder
    }

    Column(
        modifier = modifier.alpha(if (affordable) 1f else 0.45f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(AppShapes.Card)
                .background(tileColor)
                .border(
                    BorderStroke(if (avatar.equipped) 2.dp else 1.dp, borderColor),
                    AppShapes.Card
                )
                .clickable(enabled = affordable, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text = avatar.item.emoji, fontSize = 30.sp)
        }
        AvatarCellLabel(avatar = avatar, textColor = textColor)
    }
}

@Composable
private fun AvatarCellLabel(avatar: AvatarUi, textColor: Color) {
    val text = when {
        avatar.equipped -> "✓ " + stringResource(R.string.shop_equipped)
        avatar.owned -> stringResource(R.string.shop_equip)
        else -> "🪙 ${avatar.item.priceCoins}"
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (avatar.equipped) FontWeight.SemiBold else FontWeight.Normal,
        color = if (avatar.equipped) MaterialTheme.colorScheme.primary else textColor,
        textAlign = TextAlign.Center,
        maxLines = 1
    )
}

@Composable
private fun ThemeCard(
    theme: ThemeUi,
    coins: Int,
    colors: AppThemeColors,
    textColor: Color,
    onClick: () -> Unit
) {
    val affordable = theme.owned || coins >= theme.item.priceCoins
    GlassCard(colors = colors, onClick = if (affordable) onClick else null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (affordable) 1f else 0.45f)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(theme.accent.swatch)
                    .border(
                        BorderStroke(
                            if (theme.equipped) 3.dp else 1.dp,
                            if (theme.equipped) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                colors.glassBorder
                            }
                        ),
                        CircleShape
                    )
            )
            Text(
                text = stringResource(theme.item.labelRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            ThemeTrailing(theme = theme, textColor = textColor)
        }
    }
}

@Composable
private fun ThemeTrailing(theme: ThemeUi, textColor: Color) {
    val text = when {
        theme.equipped -> "✓ " + stringResource(R.string.shop_equipped)
        theme.owned -> stringResource(R.string.shop_equip)
        else -> "🪙 ${theme.item.priceCoins}"
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = if (theme.equipped) MaterialTheme.colorScheme.primary else textColor
    )
}
