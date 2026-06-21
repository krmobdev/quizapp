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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.rustam.quizapp.domain.ShopCatalog
import com.rustam.quizapp.ui.components.AppDimens
import com.rustam.quizapp.ui.components.AppShapes
import com.rustam.quizapp.ui.components.AppThemeColors
import com.rustam.quizapp.ui.components.GlassCard
import com.rustam.quizapp.ui.components.ScreenTitle
import com.rustam.quizapp.ui.components.appTextColor
import com.rustam.quizapp.ui.components.rememberAppThemeColors
import kotlinx.coroutines.delay

@Composable
fun ShopScreen(
    modifier: Modifier = Modifier,
    viewModel: ShopViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()
    var selectedSubTab by rememberSaveable { mutableIntStateOf(0) }
    var boosterToConfirm by remember { mutableStateOf<BoosterUi?>(null) }
    var snackbarPoints by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.boosterActivated.collect { points -> snackbarPoints = points }
    }
    LaunchedEffect(snackbarPoints) {
        if (snackbarPoints != null) {
            delay(2500)
            snackbarPoints = null
        }
    }

    // Activating the bigger tiers is a meaningful spend, so ask for confirmation first.
    val activateHandler: (BoosterUi) -> Unit = { booster ->
        if (booster.item.rewardPoints >= BOOSTER_CONFIRM_THRESHOLD) {
            boosterToConfirm = booster
        } else {
            viewModel.onBoosterActivate(booster)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubTabButton(
                        text = stringResource(R.string.shop_tab_store),
                        selected = selectedSubTab == 0,
                        onClick = { selectedSubTab = 0 },
                        colors = colors,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    SubTabButton(
                        text = stringResource(R.string.inventory_title),
                        selected = selectedSubTab == 1,
                        onClick = { selectedSubTab = 1 },
                        colors = colors,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (selectedSubTab == 0) {
                storeContent(
                    state = state,
                    colors = colors,
                    textColor = textColor,
                    viewModel = viewModel
                )
            } else {
                backpackContent(
                    state = state,
                    colors = colors,
                    textColor = textColor,
                    onActivate = activateHandler
                )
            }
        }

        snackbarPoints?.let { points ->
            ActivationSnackbar(
                points = points,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }

    boosterToConfirm?.let { booster ->
        AlertDialog(
            onDismissRequest = { boosterToConfirm = null },
            title = { Text(stringResource(R.string.inventory_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.inventory_confirm_text,
                        stringResource(booster.item.labelRes),
                        booster.item.rewardPoints
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onBoosterActivate(booster)
                    boosterToConfirm = null
                }) {
                    Text(stringResource(R.string.inventory_activate))
                }
            },
            dismissButton = {
                TextButton(onClick = { boosterToConfirm = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/** Boosters granting at least this much XP ask for confirmation before activation. */
private const val BOOSTER_CONFIRM_THRESHOLD = 2500

private fun androidx.compose.foundation.lazy.LazyListScope.storeContent(
    state: ShopUiState,
    colors: AppThemeColors,
    textColor: Color,
    viewModel: ShopViewModel
) {
    item {
        SectionHeader(text = stringResource(R.string.shop_boosters), textColor = textColor)
    }
    items(state.boosters, key = { it.item.id }) { booster ->
        BoosterCard(
            booster = booster,
            coins = state.coins,
            colors = colors,
            textColor = textColor,
            onBuy = { viewModel.onBoosterBuy(booster) }
        )
    }
    item {
        SectionHeader(text = stringResource(R.string.shop_powerups_section), textColor = textColor)
    }
    items(state.powerUps, key = { it.item.id }) { powerUp ->
        PowerUpCard(
            powerUp = powerUp,
            coins = state.coins,
            colors = colors,
            textColor = textColor,
            onBuy = { viewModel.onPowerUpBuy(powerUp) }
        )
    }
    item {
        SectionHeader(text = stringResource(R.string.shop_streak_freeze_section), textColor = textColor)
    }
    item {
        StreakFreezeCard(
            owned = state.streakFreezeCount,
            price = state.streakFreezePrice,
            coins = state.coins,
            colors = colors,
            textColor = textColor,
            onBuy = viewModel::onStreakFreezeBuy
        )
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

private fun androidx.compose.foundation.lazy.LazyListScope.backpackContent(
    state: ShopUiState,
    colors: AppThemeColors,
    textColor: Color,
    onActivate: (BoosterUi) -> Unit
) {
    val owned = state.boosters.filter { it.owned > 0 }
    val ownedPowerUps = state.powerUps.filter { it.owned > 0 }
    val hasFreeze = state.streakFreezeCount > 0
    if (owned.isEmpty() && ownedPowerUps.isEmpty() && !hasFreeze) {
        item {
            EmptyBackpackCard(colors = colors, textColor = textColor)
        }
    } else {
        items(owned, key = { it.item.id }) { booster ->
            InventoryItemCard(
                booster = booster,
                colors = colors,
                textColor = textColor,
                onActivate = { onActivate(booster) }
            )
        }
        items(ownedPowerUps, key = { it.item.id }) { powerUp ->
            PowerUpInventoryCard(
                powerUp = powerUp,
                colors = colors,
                textColor = textColor
            )
        }
        if (hasFreeze) {
            item {
                StreakFreezeInfoCard(
                    count = state.streakFreezeCount,
                    colors = colors,
                    textColor = textColor
                )
            }
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
private fun BoosterCard(
    booster: BoosterUi,
    coins: Int,
    colors: AppThemeColors,
    textColor: Color,
    onBuy: () -> Unit
) {
    val affordable = coins >= booster.item.priceCoins
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = booster.item.emoji, fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(booster.item.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = stringResource(R.string.shop_booster_desc, booster.item.rewardPoints),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.75f)
                )
                if (booster.owned > 0) {
                    Text(
                        text = stringResource(R.string.shop_owned_count, booster.owned),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Button(
                onClick = onBuy,
                enabled = affordable,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = textColor.copy(alpha = 0.08f),
                    disabledContentColor = textColor.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "🪙 ${booster.item.priceCoins}",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PowerUpCard(
    powerUp: PowerUpUi,
    coins: Int,
    colors: AppThemeColors,
    textColor: Color,
    onBuy: () -> Unit
) {
    val affordable = coins >= powerUp.item.priceCoins
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = powerUp.item.emoji, fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(powerUp.item.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = stringResource(powerUp.item.descRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.75f)
                )
                if (powerUp.owned > 0) {
                    Text(
                        text = stringResource(R.string.shop_owned_count, powerUp.owned),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Button(
                onClick = onBuy,
                enabled = affordable,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = textColor.copy(alpha = 0.08f),
                    disabledContentColor = textColor.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "🪙 ${powerUp.item.priceCoins}", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PowerUpInventoryCard(
    powerUp: PowerUpUi,
    colors: AppThemeColors,
    textColor: Color
) {
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = powerUp.item.emoji, fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(powerUp.item.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = stringResource(R.string.inventory_powerup_auto),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.75f)
                )
                Text(
                    text = stringResource(R.string.inventory_count, powerUp.owned),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StreakFreezeCard(
    owned: Int,
    price: Int,
    coins: Int,
    colors: AppThemeColors,
    textColor: Color,
    onBuy: () -> Unit
) {
    val affordable = coins >= price
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = ShopCatalog.STREAK_FREEZE_EMOJI, fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.shop_streak_freeze_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = stringResource(R.string.shop_streak_freeze_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.75f)
                )
                if (owned > 0) {
                    Text(
                        text = stringResource(R.string.shop_owned_count, owned),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Button(
                onClick = onBuy,
                enabled = affordable,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = textColor.copy(alpha = 0.08f),
                    disabledContentColor = textColor.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "🪙 $price", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StreakFreezeInfoCard(count: Int, colors: AppThemeColors, textColor: Color) {
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = ShopCatalog.STREAK_FREEZE_EMOJI, fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.shop_streak_freeze_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = stringResource(R.string.inventory_streak_freeze_auto),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.75f)
                )
                Text(
                    text = stringResource(R.string.inventory_count, count),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ActivationSnackbar(points: Int, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.inventory_activated_snackbar, points),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun EmptyBackpackCard(colors: AppThemeColors, textColor: Color) {
    GlassCard(colors = colors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "🎒", fontSize = 44.sp)
            Text(
                text = stringResource(R.string.inventory_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InventoryItemCard(
    booster: BoosterUi,
    colors: AppThemeColors,
    textColor: Color,
    onActivate: () -> Unit
) {
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = booster.item.emoji, fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(booster.item.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = stringResource(R.string.inventory_booster_reward, booster.item.rewardPoints),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.75f)
                )
                Text(
                    text = stringResource(R.string.inventory_count, booster.owned),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(
                onClick = onActivate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.inventory_activate),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SubTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: AppThemeColors,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    } else {
        colors.progressTrack.copy(alpha = 0.1f)
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    } else {
        textColor.copy(alpha = 0.1f)
    }

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        )
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
