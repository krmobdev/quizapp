package com.rustam.quizapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

// ── Shape tokens — iOS-style large rounded corners ────────────────────────────
object AppShapes {
    val Card   = RoundedCornerShape(16.dp)   // iOS grouped cell radius
    val Choice = RoundedCornerShape(14.dp)
    val Button = RoundedCornerShape(14.dp)   // iOS filled button radius
    val Badge  = CircleShape
}

// ── Dimension tokens ──────────────────────────────────────────────────────────
object AppDimens {
    val CardPaddingH       = 20.dp
    val CardPaddingV       = 18.dp
    val CardSpacing        = 10.dp
    val ButtonHeight       = 56.dp          // iOS button height
    val ButtonSpacing      = 10.dp
    val SettingChoiceHeight = 56.dp
}

// ── Text colour helper ────────────────────────────────────────────────────────
@Composable
fun appTextColor(): Color = MaterialTheme.colorScheme.onSurface

// ── Screen header ─────────────────────────────────────────────────────────────
@Composable
fun ScreenTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text     = title,
        modifier = modifier,
        style    = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color    = appTextColor()
    )
}

@Composable
fun ScreenSubtitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text       = text,
        modifier   = modifier,
        style      = MaterialTheme.typography.bodyMedium,
        lineHeight = 21.sp,
        color      = appTextColor().copy(alpha = 0.55f)
    )
}

// ── Setting choice row (iOS check-cell style) ─────────────────────────────────
@Composable
fun SettingChoiceCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback  = LocalAppFeedback.current
    val colors    = rememberAppThemeColors()
    val textColor = appTextColor()
    val containerColor by animateColorAsState(
        targetValue  = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                       else colors.glassCard,
        animationSpec = tween(180),
        label = "choiceContainer"
    )
    val borderColor by animateColorAsState(
        targetValue  = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.70f)
                       else colors.glassBorder,
        animationSpec = tween(180),
        label = "choiceBorder"
    )

    Surface(
        onClick  = { feedback?.click(); onClick() },
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.SettingChoiceHeight)
            .border(width = if (selected) 1.5.dp else 1.dp, color = borderColor, shape = AppShapes.Choice),
        shape    = AppShapes.Choice,
        color    = containerColor,
        shadowElevation = 0.dp,
        tonalElevation  = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text       = label,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color      = textColor
            )
            if (selected) {
                Icon(
                    imageVector        = Icons.Rounded.Check,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}

val OptionLabels = listOf("A", "B", "C", "D")

// ── Theme colour token bag ─────────────────────────────────────────────────────
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

// ── iOS-tuned theme colours ───────────────────────────────────────────────────
@Composable
fun rememberAppThemeColors(): AppThemeColors {
    val scheme = MaterialTheme.colorScheme
    val dark   = scheme.background.luminance() < 0.05f

    return remember(scheme, dark) {
        if (dark) {
            AppThemeColors(
                isDark       = true,
                // Pure black → dark secondary bg — matches OLED iOS look
                baseGradient = Brush.verticalGradient(
                    listOf(Color(0xFF000000), Color(0xFF0D0D0F), Color(0xFF1C1C1E))
                ),
                blobPrimary   = scheme.primary.copy(alpha = 0.05f),
                blobSecondary = scheme.tertiary.copy(alpha = 0.04f),
                questionCard  = QuizQuestionCardDark,
                questionText  = scheme.onSurface,
                answerCard    = QuizAnswerCardDark,
                answerText    = scheme.onSurface,
                answerBorder  = QuizCardBorderDark,
                answerBadgeBg   = Color(0xFF3A3A3C),
                answerBadgeText = scheme.onSurface,
                // iOS dark card: slightly lighter than background
                glassCard    = Color(0xFF2C2C2E),
                glassBorder  = Color(0xFF3A3A3C),
                explanationCard = QuizExplanationCardDark,
                explanationText = scheme.onSurface,
                progressTrack   = Color(0xFF3A3A3C),
                correct = CorrectGreenDark,
                wrong   = WrongRedDark
            )
        } else {
            AppThemeColors(
                isDark       = false,
                // iOS light: very subtle white gradient
                baseGradient = Brush.verticalGradient(
                    listOf(Color(0xFFF2F2F7), Color(0xFFF8F8FC), Color(0xFFEEEEF5))
                ),
                blobPrimary   = scheme.primary.copy(alpha = 0.06f),
                blobSecondary = scheme.tertiary.copy(alpha = 0.05f),
                questionCard  = Color.White,
                questionText  = scheme.onSurface,
                answerCard    = Color.White,
                answerText    = scheme.onSurface,
                answerBorder  = Color(0xFFD1D1D6),
                answerBadgeBg   = scheme.primaryContainer.copy(alpha = 0.50f),
                answerBadgeText = scheme.onSurface,
                // iOS grouped cell: white on gray background
                glassCard    = Color.White,
                glassBorder  = Color(0xFFD1D1D6),
                explanationCard = Color(0xFFEDF4FF),
                explanationText = scheme.onSurface,
                progressTrack   = Color(0xFFE5E5EA),
                correct = CorrectGreen,
                wrong   = WrongRed
            )
        }
    }
}

// ── App background — very subtle, no large blobs in iOS style ─────────────────
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    colors: AppThemeColors = rememberAppThemeColors(),
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(colors.baseGradient))
        // Tiny, very-low-opacity accent blobs — barely visible, iOS-appropriate
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = 40.dp)
                .clip(CircleShape)
                .background(colors.blobPrimary)
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 90.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(colors.blobSecondary)
        )
        content()
    }
}

@Composable
fun Modifier.glassBorder(colors: AppThemeColors): Modifier =
    border(width = 1.dp, color = colors.glassBorder, shape = AppShapes.Card)

// ── Glass Card — iOS grouped-list-card style ───────────────────────────────────
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    colors: AppThemeColors = rememberAppThemeColors(),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = modifier.fillMaxWidth().glassBorder(colors)
    val shape        = AppShapes.Card

    if (onClick != null) {
        val feedback = LocalAppFeedback.current
        val interactionSource = remember { MutableInteractionSource() }
        Surface(
            onClick           = { feedback?.click(); onClick() },
            interactionSource = interactionSource,
            shape             = shape,
            color             = colors.glassCard,
            modifier          = cardModifier.iosSpringScale(interactionSource, 0.97f),
            shadowElevation   = 0.dp,
            tonalElevation    = 0.dp
        ) {
            Box(Modifier.clip(shape)) { content() }
        }
    } else {
        Surface(
            shape           = shape,
            color           = colors.glassCard,
            modifier        = cardModifier,
            shadowElevation = 0.dp,
            tonalElevation  = 0.dp
        ) {
            Box(Modifier.clip(shape)) { content() }
        }
    }
}

// ── Primary action button — iOS filled button style ────────────────────────────
@Composable
fun AppActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = true
) {
    val feedback = LocalAppFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    Button(
        onClick           = { feedback?.click(); onClick() },
        enabled           = enabled,
        interactionSource = interactionSource,
        modifier          = modifier
            .fillMaxWidth()
            .height(AppDimens.ButtonHeight)
            .iosSpringScale(interactionSource, 0.96f),
        shape  = AppShapes.Button,
        colors = if (primary) {
            ButtonDefaults.buttonColors(contentColor = Color.White)
        } else {
            ButtonDefaults.buttonColors(
                containerColor         = MaterialTheme.colorScheme.secondaryContainer,
                contentColor           = appTextColor(),
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                disabledContentColor   = appTextColor().copy(alpha = 0.4f)
            )
        },
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = if (enabled) Color.White else appTextColor().copy(alpha = 0.4f)
        )
    }
}

// ── Score ring ────────────────────────────────────────────────────────────────
@Composable
fun ScoreRing(
    score: Int,
    total: Int,
    modifier: Modifier = Modifier,
    size: Dp = 168.dp
) {
    val progress by animateFloatAsState(
        targetValue   = if (total == 0) 0f else score.toFloat() / total.coerceAtLeast(1),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "scoreRing"
    )
    val scheme     = MaterialTheme.colorScheme
    val trackColor = scheme.surfaceVariant.copy(alpha = 0.60f)

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke   = 14.dp.toPx()
            val diameter = this.size.minDimension - stroke
            val topLeft  = Offset(stroke / 2f, stroke / 2f)
            drawArc(
                color      = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter  = false,
                topLeft    = topLeft,
                size       = androidx.compose.ui.geometry.Size(diameter, diameter),
                style      = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(scheme.primary, scheme.tertiary, scheme.primary),
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter  = false,
                topLeft    = topLeft,
                size       = androidx.compose.ui.geometry.Size(diameter, diameter),
                style      = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = score.toString(),
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color      = scheme.onSurface
            )
            Text(
                text  = "/ $total",
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

// ── Option badge (A/B/C/D) ────────────────────────────────────────────────────
@Composable
fun OptionBadge(
    label: String,
    background: Color,
    content: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier          = modifier.size(size).clip(AppShapes.Badge).background(background),
        contentAlignment  = Alignment.Center
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = content
        )
    }
}