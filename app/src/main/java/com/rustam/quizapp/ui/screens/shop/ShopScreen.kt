package com.rustam.quizapp.ui.screens.shop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.rustam.quizapp.domain.GemBundle
import com.rustam.quizapp.domain.LootBox
import com.rustam.quizapp.domain.LootResult
import com.rustam.quizapp.domain.MythicBox
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
    var snackbarText by remember { mutableStateOf<String?>(null) }
    val lootResult by viewModel.lootResult.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.snackbar.collect { message -> snackbarText = message }
    }
    LaunchedEffect(snackbarText) {
        if (snackbarText != null) {
            delay(2500)
            snackbarText = null
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
                BalanceCard(coins = state.coins, gems = state.gems, colors = colors, textColor = textColor)
            }
            if (state.deals.isNotEmpty()) {
                item {
                    SectionHeader(text = stringResource(R.string.deals_section_title), textColor = textColor)
                }
                items(state.deals, key = { "deal_${it.template.dealId}" }) { deal ->
                    DailyDealCard(
                        deal = deal,
                        coins = state.coins,
                        colors = colors,
                        textColor = textColor,
                        onBuy = { viewModel.onDealBuy(deal.template.dealId) }
                    )
                }
            }
            item {
                val tabs = listOf(
                    stringResource(R.string.shop_tab_consumables),
                    stringResource(R.string.shop_tab_avatars),
                    stringResource(R.string.shop_tab_themes),
                    stringResource(R.string.shop_tab_titles),
                    stringResource(R.string.inventory_title)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, label ->
                        SubTabButton(
                            text = label,
                            selected = selectedSubTab == index,
                            onClick = { selectedSubTab = index },
                            colors = colors,
                            textColor = textColor
                        )
                    }
                }
            }

            when (selectedSubTab) {
                0 -> consumablesContent(state, colors, textColor, viewModel)
                1 -> avatarsGridContent(state, colors, textColor, viewModel)
                2 -> themesListContent(state, colors, textColor, viewModel)
                3 -> titlesListContent(state, colors, textColor, viewModel)
                else -> backpackContent(
                    state = state,
                    colors = colors,
                    textColor = textColor,
                    onActivate = activateHandler,
                    onBoostActivate = viewModel::onBoostActivate
                )
            }
        }

        snackbarText?.let { text ->
            ActivationSnackbar(
                text = text,
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

    lootResult?.let { result ->
        LootRevealDialog(result = result, onDismiss = viewModel::onLootDismiss)
    }
}

@Composable
private fun LootRevealDialog(result: LootResult, onDismiss: () -> Unit) {
    val emoji: String
    val text: String
    when (result) {
        is LootResult.Coins -> {
            emoji = "🪙"
            text = stringResource(R.string.loot_coins, result.amount)
        }
        is LootResult.Xp -> {
            emoji = "🎓"
            text = stringResource(R.string.loot_xp, result.amount)
        }
        is LootResult.Avatar -> {
            emoji = result.item.emoji
            text = stringResource(R.string.loot_avatar)
        }
        is LootResult.Title -> {
            emoji = result.item.emoji
            text = stringResource(R.string.loot_title, stringResource(result.item.labelRes))
        }
        is LootResult.Theme -> {
            emoji = "🎨"
            text = stringResource(R.string.loot_theme, stringResource(result.item.labelRes))
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.loot_reveal_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = emoji, fontSize = 56.sp)
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.loot_close))
            }
        }
    )
}

@Composable
private fun DailyDealCard(
    deal: com.rustam.quizapp.data.ShopDealState,
    coins: Int,
    colors: AppThemeColors,
    textColor: Color,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, colors = colors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ── Row 1: emoji · label · discount badge ───────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = deal.template.emoji, fontSize = 22.sp)
                Text(
                    text = stringResource(deal.template.labelRes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75f)
                ) {
                    Text(
                        text = stringResource(
                            R.string.deals_discount,
                            com.rustam.quizapp.domain.ShopDeals.DISCOUNT_PERCENT
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            // ── Row 2: bought count · action ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        R.string.deals_bought,
                        deal.purchasedToday,
                        com.rustam.quizapp.domain.ShopDeals.MAX_PER_DAY
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor.copy(alpha = 0.65f)
                )
                if (deal.soldOut) {
                    Text(
                        text = stringResource(R.string.deals_sold_out),
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor.copy(alpha = 0.45f)
                    )
                } else {
                    Button(
                        onClick = onBuy,
                        enabled = coins >= deal.dealPrice,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.deals_buy, deal.dealPrice),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LootBoxCard(
    coins: Int,
    colors: AppThemeColors,
    textColor: Color,
    onOpen: () -> Unit
) {
    val affordable = coins >= LootBox.PRICE
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "🎁", fontSize = 26.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.shop_lootbox_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = stringResource(R.string.shop_lootbox_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.75f)
                )
            }
            Button(
                onClick = onOpen,
                enabled = affordable,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = textColor.copy(alpha = 0.08f),
                    disabledContentColor = textColor.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "🪙 ${LootBox.PRICE}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun MythicChestCard(
    gems: Int,
    colors: AppThemeColors,
    textColor: Color,
    onOpen: () -> Unit
) {
    val affordable = gems >= MythicBox.PRICE_GEMS
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "🔮", fontSize = 26.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.shop_mythic_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = stringResource(R.string.shop_mythic_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.75f)
                )
            }
            // Always enabled — ViewModel emits snackbar on insufficient gems.
            Button(
                onClick = onOpen,
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (affordable)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (affordable)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        textColor.copy(alpha = 0.55f)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(text = "💎 ${MythicBox.PRICE_GEMS}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}


@Composable
private fun GemBundleCard(
    bundle: GemBundle,
    gems: Int,
    colors: AppThemeColors,
    textColor: Color,
    onBuy: () -> Unit
) {
    val affordable = gems >= bundle.priceGems
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = bundle.emoji, fontSize = 26.sp)
            Text(
                text = stringResource(bundle.labelRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            // Always enabled — ViewModel emits snackbar on insufficient gems.
            Button(
                onClick = onBuy,
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (affordable)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (affordable)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        textColor.copy(alpha = 0.55f)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(text = "💎 ${bundle.priceGems}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

/** Boosters granting at least this much XP ask for confirmation before activation. */
private const val BOOSTER_CONFIRM_THRESHOLD = 1500

private fun androidx.compose.foundation.lazy.LazyListScope.consumablesContent(
    state: ShopUiState,
    colors: AppThemeColors,
    textColor: Color,
    viewModel: ShopViewModel
) {
    item {
        SectionHeader(text = stringResource(R.string.shop_quiz_boosters_section), textColor = textColor)
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
        SectionHeader(text = stringResource(R.string.shop_lootbox_section), textColor = textColor)
    }
    item {
        LootBoxCard(
            coins = state.coins,
            colors = colors,
            textColor = textColor,
            onOpen = viewModel::onOpenLootBox
        )
    }
    item {
        MythicChestCard(
            gems = state.gems,
            colors = colors,
            textColor = textColor,
            onOpen = viewModel::onOpenMythicChest
        )
    }
    item {
        SectionHeader(text = stringResource(R.string.shop_gem_exchange_section), textColor = textColor)
    }
    items(ShopCatalog.gemBundles, key = { it.id }) { bundle ->
        GemBundleCard(
            bundle = bundle,
            gems = state.gems,
            colors = colors,
            textColor = textColor,
            onBuy = { viewModel.onGemBundleBuy(bundle.id) }
        )
    }
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
        SectionHeader(text = stringResource(R.string.shop_boosts_section), textColor = textColor)
    }
    items(state.boosts, key = { it.item.id }) { boost ->
        BoostStoreCard(
            boost = boost,
            coins = state.coins,
            colors = colors,
            textColor = textColor,
            onBuy = { viewModel.onBoostBuy(boost) }
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
}

/** Avatars rendered as lazy rows of four cells, so only on-screen rows compose (no lag). */
private fun androidx.compose.foundation.lazy.LazyListScope.avatarsGridContent(
    state: ShopUiState,
    colors: AppThemeColors,
    textColor: Color,
    viewModel: ShopViewModel
) {
    item {
        SectionHeader(text = stringResource(R.string.shop_avatars), textColor = textColor)
    }
    items(state.avatars.chunked(4), key = { row -> row.first().item.id }) { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rowItems.forEach { avatar ->
                AvatarCell(
                    avatar = avatar,
                    coins = state.coins,
                    gems = state.gems,
                    colors = colors,
                    textColor = textColor,
                    onClick = { viewModel.onAvatarClick(avatar) },
                    modifier = Modifier.weight(1f)
                )
            }
            repeat(4 - rowItems.size) { Box(modifier = Modifier.weight(1f)) }
        }
    }
    if (state.premiumAvatars.isNotEmpty()) {
        item {
            SectionHeader(text = stringResource(R.string.shop_premium_avatars), textColor = textColor)
        }
        items(state.premiumAvatars.chunked(4), key = { row -> "premium_${row.first().item.id}" }) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { avatar ->
                    AvatarCell(
                        avatar = avatar,
                        coins = state.coins,
                        gems = state.gems,
                        colors = colors,
                        textColor = textColor,
                        onClick = { viewModel.onPremiumAvatarClick(avatar) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - rowItems.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.themesListContent(
    state: ShopUiState,
    colors: AppThemeColors,
    textColor: Color,
    viewModel: ShopViewModel
) {
    item {
        SectionHeader(text = stringResource(R.string.shop_themes), textColor = textColor)
    }
    items(state.themes, key = { it.item.id }) { theme ->
        ThemeCard(
            theme = theme,
            coins = state.coins,
            gems = state.gems,
            colors = colors,
            textColor = textColor,
            onClick = { viewModel.onThemeClick(theme) }
        )
    }
    if (state.premiumThemes.isNotEmpty()) {
        item {
            SectionHeader(text = stringResource(R.string.shop_premium_themes), textColor = textColor)
        }
        items(state.premiumThemes, key = { "premium_${it.item.id}" }) { theme ->
            ThemeCard(
                theme = theme,
                coins = state.coins,
                gems = state.gems,
                colors = colors,
                textColor = textColor,
                onClick = { viewModel.onPremiumThemeClick(theme) }
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.titlesListContent(
    state: ShopUiState,
    colors: AppThemeColors,
    textColor: Color,
    viewModel: ShopViewModel
) {
    item {
        SectionHeader(text = stringResource(R.string.shop_titles), textColor = textColor)
    }
    item {
        Text(
            text = stringResource(R.string.shop_titles_hint),
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
    items(state.titles, key = { it.item.id }) { title ->
        TitleCard(
            title = title,
            coins = state.coins,
            gems = state.gems,
            colors = colors,
            textColor = textColor,
            onClick = { viewModel.onTitleClick(title) }
        )
    }
    if (state.premiumTitles.isNotEmpty()) {
        item {
            SectionHeader(text = stringResource(R.string.shop_premium_titles), textColor = textColor)
        }
        items(state.premiumTitles, key = { "premium_${it.item.id}" }) { title ->
            TitleCard(
                title = title,
                coins = state.coins,
                gems = state.gems,
                colors = colors,
                textColor = textColor,
                onClick = { viewModel.onPremiumTitleClick(title) }
            )
        }
    }
}

@Composable
private fun TitleCard(
    title: TitleUi,
    coins: Int,
    gems: Int,
    colors: AppThemeColors,
    textColor: Color,
    onClick: () -> Unit
) {
    val isPremium = title.item.priceGems > 0
    val affordable = title.owned ||
        if (isPremium) gems >= title.item.priceGems else coins >= title.item.priceCoins
    GlassCard(colors = colors, onClick = if (affordable) onClick else null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (affordable) 1f else 0.45f)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = title.item.emoji, fontSize = 28.sp)
            Text(
                text = stringResource(title.item.labelRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            TitleTrailing(title = title, textColor = textColor)
        }
    }
}

@Composable
private fun TitleTrailing(title: TitleUi, textColor: Color) {
    val text = when {
        title.equipped -> "✓ " + stringResource(R.string.shop_equipped)
        title.owned -> stringResource(R.string.shop_equip)
        title.item.priceGems > 0 -> "💎 ${title.item.priceGems}"
        else -> "🪙 ${title.item.priceCoins}"
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = if (title.equipped) MaterialTheme.colorScheme.primary else textColor
    )
}

private fun androidx.compose.foundation.lazy.LazyListScope.backpackContent(
    state: ShopUiState,
    colors: AppThemeColors,
    textColor: Color,
    onActivate: (BoosterUi) -> Unit,
    onBoostActivate: (BoostUi) -> Unit
) {
    val owned = state.boosters.filter { it.owned > 0 }
    val ownedPowerUps = state.powerUps.filter { it.owned > 0 }
    val backpackBoosts = state.boosts.filter { it.owned > 0 || it.activeQuizzesLeft > 0 }
    val hasFreeze = state.streakFreezeCount > 0
    if (owned.isEmpty() && ownedPowerUps.isEmpty() && backpackBoosts.isEmpty() && !hasFreeze) {
        item {
            EmptyBackpackCard(colors = colors, textColor = textColor)
        }
    } else {
        items(backpackBoosts, key = { it.item.id }) { boost ->
            BoostInventoryCard(
                boost = boost,
                colors = colors,
                textColor = textColor,
                onActivate = { onBoostActivate(boost) }
            )
        }
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
private fun BoostStoreCard(
    boost: BoostUi,
    coins: Int,
    colors: AppThemeColors,
    textColor: Color,
    onBuy: () -> Unit
) {
    val affordable = coins >= boost.item.priceCoins
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = boost.item.emoji, fontSize = 26.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(boost.item.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = stringResource(boost.item.descRes, boost.item.quizzes),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.75f)
                )
                if (boost.owned > 0) {
                    Text(
                        text = stringResource(R.string.shop_owned_count, boost.owned),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (boost.activeQuizzesLeft > 0) {
                    Text(
                        text = stringResource(R.string.boost_inventory_active, boost.activeQuizzesLeft),
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
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "🪙 ${boost.item.priceCoins}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun BoostInventoryCard(
    boost: BoostUi,
    colors: AppThemeColors,
    textColor: Color,
    onActivate: () -> Unit
) {
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = boost.item.emoji, fontSize = 26.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(boost.item.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                if (boost.activeQuizzesLeft > 0) {
                    Text(
                        text = stringResource(R.string.boost_inventory_active, boost.activeQuizzesLeft),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = stringResource(boost.item.descRes, boost.item.quizzes),
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.75f)
                    )
                }
                if (boost.owned > 0) {
                    Text(
                        text = stringResource(R.string.inventory_count, boost.owned),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Button(
                onClick = onActivate,
                enabled = boost.owned > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = textColor.copy(alpha = 0.08f),
                    disabledContentColor = textColor.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.inventory_activate),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(
    coins: Int,
    gems: Int,
    colors: AppThemeColors,
    textColor: Color
) {
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.shop_balance),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "💎 $gems",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "🪙 $coins",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = booster.item.emoji, fontSize = 26.sp)
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
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "🪙 ${booster.item.priceCoins}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = powerUp.item.emoji, fontSize = 26.sp)
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
                if (powerUp.item.packSize > 1) {
                    Text(
                        text = stringResource(R.string.shop_powerup_pack_size, powerUp.item.packSize),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "🪙 ${powerUp.item.priceCoins}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = powerUp.item.emoji, fontSize = 26.sp)
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = ShopCatalog.STREAK_FREEZE_EMOJI, fontSize = 26.sp)
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
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "🪙 $price", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = ShopCatalog.STREAK_FREEZE_EMOJI, fontSize = 26.sp)
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
private fun ActivationSnackbar(text: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Text(
            text = text,
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = booster.item.emoji, fontSize = 26.sp)
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
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.inventory_activate),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
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
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
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
private fun AvatarCell(
    avatar: AvatarUi,
    coins: Int,
    gems: Int,
    colors: AppThemeColors,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPremium = avatar.item.priceGems > 0
    val affordable = avatar.owned ||
        if (isPremium) gems >= avatar.item.priceGems else coins >= avatar.item.priceCoins
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
        avatar.item.priceGems > 0 -> "💎 ${avatar.item.priceGems}"
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
    gems: Int,
    colors: AppThemeColors,
    textColor: Color,
    onClick: () -> Unit
) {
    val isPremium = theme.item.priceGems > 0
    val affordable = theme.owned ||
        if (isPremium) gems >= theme.item.priceGems else coins >= theme.item.priceCoins
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
        theme.item.priceGems > 0 -> "💎 ${theme.item.priceGems}"
        else -> "🪙 ${theme.item.priceCoins}"
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = if (theme.equipped) MaterialTheme.colorScheme.primary else textColor
    )
}
