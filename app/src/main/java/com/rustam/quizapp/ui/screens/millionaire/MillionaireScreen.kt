package com.rustam.quizapp.ui.screens.millionaire

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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
import com.rustam.quizapp.domain.Lifeline
import com.rustam.quizapp.domain.MillionaireCatalog
import com.rustam.quizapp.domain.MillionairePack
import com.rustam.quizapp.domain.PackCurrency
import com.rustam.quizapp.ui.components.AppShapes
import com.rustam.quizapp.ui.components.AppThemeColors
import com.rustam.quizapp.ui.components.GlassCard
import com.rustam.quizapp.ui.components.ScreenTitle
import com.rustam.quizapp.ui.components.appTextColor
import com.rustam.quizapp.ui.components.rememberAppThemeColors

/** Icon for a currency, used in inline amount labels like "🪙 5000". */
internal fun currencyIcon(currency: PackCurrency): String = when (currency) {
    PackCurrency.XP -> "✨"
    PackCurrency.COINS -> "🪙"
    PackCurrency.GEMS -> "💎"
}

internal fun amountLabel(currency: PackCurrency, amount: Int): String =
    "${currencyIcon(currency)} $amount"

@Composable
fun MillionaireScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MillionaireViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()
    var showLeaveConfirm by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (state.phase == MillionairePhase.PLAYING) {
            showLeaveConfirm = true
        } else {
            onExit()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (state.phase) {
            MillionairePhase.CATALOG -> CatalogContent(
                state = state,
                colors = colors,
                textColor = textColor,
                onBuy = viewModel::buyAndStart
            )
            MillionairePhase.PLAYING, MillionairePhase.OUTCOME -> MillionaireGameContent(
                state = state,
                colors = colors,
                textColor = textColor,
                onAnswer = viewModel::answer,
                onProceed = viewModel::proceed,
                onCashOut = viewModel::cashOut,
                onLifeline = viewModel::useLifeline
            )
        }
    }

    if (state.phase == MillionairePhase.OUTCOME) {
        OutcomeDialog(
            currency = state.currency,
            amount = state.outcomeAmount,
            won = state.outcomeWon,
            allCorrect = state.outcomeAllCorrect,
            onDismiss = viewModel::dismissOutcome
        )
    }

    if (showLeaveConfirm) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirm = false },
            title = { Text(stringResource(R.string.millionaire_leave_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.millionaire_leave_text,
                        amountLabel(state.currency, state.guaranteedAmount)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveConfirm = false
                    viewModel.leaveRun()
                }) { Text(stringResource(R.string.millionaire_leave_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirm = false }) {
                    Text(stringResource(R.string.millionaire_leave_cancel))
                }
            }
        )
    }
}

@Composable
private fun CatalogContent(
    state: MillionaireUiState,
    colors: AppThemeColors,
    textColor: Color,
    onBuy: (String) -> Unit
) {
    var currencyTab by rememberSaveable { mutableIntStateOf(1) } // default Coins
    val currencies = PackCurrency.entries
    val selected = currencies[currencyTab]
    val balance = when (selected) {
        PackCurrency.XP -> state.balanceXp
        PackCurrency.COINS -> state.balanceCoins
        PackCurrency.GEMS -> state.balanceGems
    }
    val packs = state.packs.filter { it.currency == selected }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenTitle(
                title = stringResource(R.string.millionaire_title),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        item {
            Text(
                text = stringResource(R.string.millionaire_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.75f)
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currencies.forEachIndexed { i, currency ->
                    val label = when (currency) {
                        PackCurrency.XP -> stringResource(R.string.millionaire_tab_xp)
                        PackCurrency.COINS -> stringResource(R.string.millionaire_tab_coins)
                        PackCurrency.GEMS -> stringResource(R.string.millionaire_tab_gems)
                    }
                    CurrencyTab(
                        text = "${currencyIcon(currency)} $label",
                        selected = currencyTab == i,
                        onClick = { currencyTab = i },
                        colors = colors,
                        textColor = textColor
                    )
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.millionaire_balance, amountLabel(selected, balance)),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        items(packs, key = { it.id }) { pack ->
            PackCard(
                pack = pack,
                balance = balance,
                colors = colors,
                textColor = textColor,
                onBuy = { onBuy(pack.id) }
            )
        }
    }
}

@Composable
private fun PackCard(
    pack: MillionairePack,
    balance: Int,
    colors: AppThemeColors,
    textColor: Color,
    onBuy: () -> Unit
) {
    val affordable = balance >= pack.cost
    GlassCard(colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = pack.emoji, fontSize = 30.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(pack.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = stringResource(
                        R.string.millionaire_cost_prize,
                        amountLabel(pack.currency, pack.cost),
                        amountLabel(pack.currency, pack.prize)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.75f)
                )
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
                Text(stringResource(R.string.millionaire_buy), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CurrencyTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: AppThemeColors,
    textColor: Color
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    } else {
        colors.progressTrack.copy(alpha = 0.1f)
    }
    Surface(
        onClick = onClick,
        color = background,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else textColor.copy(alpha = 0.1f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun OutcomeDialog(
    currency: PackCurrency,
    amount: Int,
    won: Boolean,
    allCorrect: Boolean,
    onDismiss: () -> Unit
) {
    val emoji = when {
        allCorrect -> "🏆"
        won -> "🎉"
        else -> "💔"
    }
    val message = when {
        allCorrect -> stringResource(R.string.millionaire_won_all, amountLabel(currency, amount))
        won -> stringResource(R.string.millionaire_won, amountLabel(currency, amount))
        amount > 0 -> stringResource(R.string.millionaire_lost, amountLabel(currency, amount))
        else -> stringResource(R.string.millionaire_lost_zero)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.millionaire_outcome_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = emoji, fontSize = 56.sp)
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.millionaire_outcome_ok))
            }
        }
    )
}
