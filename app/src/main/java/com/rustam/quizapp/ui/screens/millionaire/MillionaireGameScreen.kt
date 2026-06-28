package com.rustam.quizapp.ui.screens.millionaire

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustam.quizapp.R
import com.rustam.quizapp.domain.Lifeline
import com.rustam.quizapp.domain.MillionaireLadder
import com.rustam.quizapp.ui.components.AppShapes
import com.rustam.quizapp.ui.components.AppThemeColors
import com.rustam.quizapp.ui.components.GlassCard

@Composable
internal fun MillionaireGameContent(
    state: MillionaireUiState,
    colors: AppThemeColors,
    textColor: Color,
    onAnswer: (Int) -> Unit,
    onProceed: () -> Unit,
    onCashOut: () -> Unit,
    onLifeline: (Lifeline) -> Unit
) {
    val question = state.question ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Prize header.
        GlassCard(colors = colors) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.millionaire_rung, state.rungNumber, MillionaireLadder.LENGTH),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = stringResource(
                        R.string.millionaire_next,
                        amountLabel(state.currency, state.nextAmount)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(
                        R.string.millionaire_guaranteed,
                        amountLabel(state.currency, state.guaranteedAmount)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }

        // Lifelines.
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LifelineButton("➗", !state.lifelines.fiftyFiftyUsed && state.canAct, textColor) {
                onLifeline(Lifeline.FIFTY_FIFTY)
            }
            LifelineButton("☎️", !state.lifelines.phoneUsed && state.canAct, textColor) {
                onLifeline(Lifeline.PHONE_FRIEND)
            }
            LifelineButton("👥", !state.lifelines.audienceUsed && state.canAct, textColor) {
                onLifeline(Lifeline.ASK_AUDIENCE)
            }
        }

        // Phone-a-friend hint.
        state.phoneHint?.let { hint ->
            val letter = ('A' + hint.suggestedIndex).toString()
            val text = if (hint.confident) {
                stringResource(R.string.millionaire_phone_confident, letter)
            } else {
                stringResource(R.string.millionaire_phone_unsure, letter)
            }
            HintBanner("☎️ $text", colors, textColor)
        }

        // Question.
        GlassCard(colors = colors) {
            Text(
                text = question.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            )
        }

        // Options.
        question.options.forEachIndexed { i, option ->
            val hidden = i in state.hiddenOptions
            val audiencePct = state.audienceHint?.getOrNull(i)
            OptionRow(
                letter = ('A' + i).toString(),
                text = option,
                hidden = hidden,
                revealed = state.revealed,
                isCorrect = i == question.correctIndex,
                isSelected = i == state.selectedAnswer,
                audiencePct = audiencePct,
                enabled = state.canAct && !hidden,
                colors = colors,
                textColor = textColor,
                onClick = { onAnswer(i) }
            )
        }

        Spacer(Modifier.height(4.dp))

        // Actions.
        if (state.revealed) {
            Button(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.Button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (state.lastAnswerCorrect) {
                        stringResource(R.string.millionaire_continue)
                    } else {
                        stringResource(R.string.millionaire_outcome_ok)
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        } else if (state.currentAmount > 0) {
            OutlinedButton(
                onClick = onCashOut,
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.Button
            ) {
                Text(
                    text = stringResource(
                        R.string.millionaire_take_money,
                        amountLabel(state.currency, state.currentAmount)
                    ),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LifelineButton(
    emoji: String,
    enabled: Boolean,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(
                if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                else textColor.copy(alpha = 0.06f)
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else textColor.copy(alpha = 0.1f)
                ),
                CircleShape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.4f),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 22.sp)
    }
}

@Composable
private fun HintBanner(text: String, colors: AppThemeColors, textColor: Color) {
    GlassCard(colors = colors) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun OptionRow(
    letter: String,
    text: String,
    hidden: Boolean,
    revealed: Boolean,
    isCorrect: Boolean,
    isSelected: Boolean,
    audiencePct: Int?,
    enabled: Boolean,
    colors: AppThemeColors,
    textColor: Color,
    onClick: () -> Unit
) {
    val correctColor = Color(0xFF2E7D32)
    val container = when {
        hidden -> textColor.copy(alpha = 0.04f)
        revealed && isCorrect -> correctColor.copy(alpha = 0.22f)
        revealed && isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.20f)
        else -> colors.answerCard
    }
    val border = when {
        revealed && isCorrect -> correctColor.copy(alpha = 0.7f)
        revealed && isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        else -> colors.glassBorder
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.Card)
            .background(container)
            .border(BorderStroke(1.dp, border), AppShapes.Card)
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (hidden) 0.4f else 1f)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "$letter.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (hidden) "" else text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        if (audiencePct != null && !hidden) {
            Text(
                text = "$audiencePct%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}
