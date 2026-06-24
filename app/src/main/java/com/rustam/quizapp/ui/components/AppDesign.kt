package com.rustam.quizapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustam.quizapp.ui.theme.CorrectGreen
import com.rustam.quizapp.ui.theme.CorrectGreenDark
import com.rustam.quizapp.ui.theme.QuizAnswerCardDark
import com.rustam.quizapp.ui.theme.QuizBgBottomDark
import com.rustam.quizapp.ui.theme.QuizBgMidDark
import com.rustam.quizapp.ui.theme.QuizBgTopDark
import com.rustam.quizapp.ui.theme.QuizCardBorderDark
import com.rustam.quizapp.ui.theme.QuizExplanationCardDark
import com.rustam.quizapp.ui.theme.QuizQuestionCardDark
import com.rustam.quizapp.ui.theme.WrongRed
import com.rustam.quizapp.ui.theme.WrongRedDark

object AppShapes {
    val Card = RoundedCornerShape(28.dp)
    val Choice = RoundedCornerShape(20.dp)
    val Button = RoundedCornerShape(28.dp)
    val Badge = CircleShape
}

object AppDimens {
    val CardPaddingH = 24.dp
    val CardPaddingV = 22.dp
    val CardSpacing = 14.dp
    val ButtonHeight = 72.dp
    val ButtonSpacing = 12.dp
    val SettingChoiceHeight = 64.dp
}

@Composable
fun appTextColor(): Color = MaterialTheme.colorScheme.onSurface

@Composable
fun ScreenTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        color = appTextColor()
    )
}

@Composable
fun ScreenSubtitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        color = appTextColor()
    )
}

@Composable
fun SettingChoiceCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            colors.glassCard
        },
        animationSpec = tween(200),
        label = "settingChoiceContainer"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            colors.glassBorder
        },
        animationSpec = tween(200),
        label = "settingChoiceBorder"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.SettingChoiceHeight)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = AppShapes.Choice
            ),
        shape = AppShapes.Choice,
        color = containerColor,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

val OptionLabels = listOf("A", "B", "C", "D")

@Immutable
data class AppThemeColors(
    val isDark: Boolean,
    val baseGradient: Brush,
    val blobPrimary: Color,
    val blobSecondary: Color,
    val questionCard: Color,
    val questionText: Color,
    val answerCard: Color,
    val answerText: Color,
    val answerBorder: Color,
    val answerBadgeBg: Color,
    val answerBadgeText: Color,
    val glassCard: Color,
    val glassBorder: Color,
    val explanationCard: Color,
    val explanationText: Color,
    val progressTrack: Color,
    val correct: Color,
    val wrong: Color
)

@Composable
fun rememberAppThemeColors(): AppThemeColors {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < 0.5f

    return remember(scheme, dark) {
        if (dark) {
            AppThemeColors(
                isDark = true,
                baseGradient = Brush.verticalGradient(
                    listOf(QuizBgTopDark, QuizBgMidDark, QuizBgBottomDark)
                ),
                blobPrimary = scheme.primary.copy(alpha = 0.07f),
                blobSecondary = scheme.tertiary.copy(alpha = 0.05f),
                questionCard = QuizQuestionCardDark,
                questionText = scheme.onSurface,
                answerCard = QuizAnswerCardDark,
                answerText = scheme.onSurface,
                answerBorder = QuizCardBorderDark,
                answerBadgeBg = Color(0xFF3A4643),
                answerBadgeText = scheme.onSurface,
                glassCard = QuizQuestionCardDark,
                glassBorder = QuizCardBorderDark,
                explanationCard = QuizExplanationCardDark,
                explanationText = scheme.onSurface,
                progressTrack = QuizCardBorderDark,
                correct = CorrectGreenDark,
                wrong = WrongRedDark
            )
        } else {
            AppThemeColors(
                isDark = false,
                baseGradient = Brush.verticalGradient(
                    listOf(
                        Color(0xFFE8FAF4),
                        Color(0xFFF4FBF7),
                        Color(0xFFE4F2EC)
                    )
                ),
                blobPrimary = scheme.primary.copy(alpha = 0.12f),
                blobSecondary = scheme.tertiary.copy(alpha = 0.1f),
                questionCard = Color.White.copy(alpha = 0.92f),
                questionText = scheme.onSurface,
                answerCard = Color.White.copy(alpha = 0.88f),
                answerText = scheme.onSurface,
                answerBorder = scheme.outlineVariant.copy(alpha = 0.55f),
                answerBadgeBg = scheme.primaryContainer.copy(alpha = 0.55f),
                answerBadgeText = scheme.onSurface,
                glassCard = Color.White.copy(alpha = 0.82f),
                glassBorder = scheme.outlineVariant.copy(alpha = 0.45f),
                explanationCard = scheme.secondaryContainer.copy(alpha = 0.85f),
                explanationText = scheme.onSurface,
                progressTrack = scheme.surfaceVariant,
                correct = CorrectGreen,
                wrong = WrongRed
            )
        }
    }
}

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    colors: AppThemeColors = rememberAppThemeColors(),
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.baseGradient)
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = (-130).dp, y = 80.dp)
                .clip(CircleShape)
                .background(colors.blobPrimary)
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 110.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(colors.blobSecondary)
        )
        content()
    }
}

@Composable
fun Modifier.glassBorder(colors: AppThemeColors): Modifier =
    border(width = 1.dp, color = colors.glassBorder, shape = AppShapes.Card)

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    colors: AppThemeColors = rememberAppThemeColors(),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .glassBorder(colors)

    val shape = AppShapes.Card
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = colors.glassCard,
            modifier = cardModifier
        ) {
            Box(Modifier.clip(shape)) { content() }
        }
    } else {
        Surface(
            shape = shape,
            color = colors.glassCard,
            modifier = cardModifier
        ) {
            Box(Modifier.clip(shape)) { content() }
        }
    }
}

@Composable
fun AppActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.ButtonHeight),
        shape = AppShapes.Button,
        colors = if (primary) {
            ButtonDefaults.buttonColors(
                contentColor = appTextColor()
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = appTextColor(),
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                disabledContentColor = appTextColor().copy(alpha = 0.5f)
            )
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = appTextColor()
        )
    }
}

@Composable
fun ScoreRing(
    score: Int,
    total: Int,
    modifier: Modifier = Modifier,
    size: Dp = 168.dp
) {
    val progress by animateFloatAsState(
        targetValue = if (total == 0) 0f else score.toFloat() / total.coerceAtLeast(1),
        animationSpec = tween(durationMillis = 900),
        label = "scoreRing"
    )
    val scheme = MaterialTheme.colorScheme
    val trackColor = scheme.surfaceVariant.copy(alpha = 0.65f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 14.dp.toPx()
            val diameter = this.size.minDimension - stroke
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(scheme.primary, scheme.tertiary, scheme.primary),
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
            Text(
                text = "/ $total",
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface
            )
        }
    }
}

@Composable
fun OptionBadge(
    label: String,
    background: Color,
    content: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(AppShapes.Badge)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = content
        )
    }
}