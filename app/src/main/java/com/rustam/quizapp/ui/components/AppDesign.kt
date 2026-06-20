package com.rustam.quizapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    val Button = RoundedCornerShape(28.dp)
    val Badge = CircleShape
}

object AppDimens {
    val CardPaddingH = 24.dp
    val CardPaddingV = 22.dp
    val CardSpacing = 14.dp
    val ButtonHeight = 72.dp
    val ButtonSpacing = 12.dp
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
                blobPrimary = scheme.primary.copy(alpha = 0.14f),
                blobSecondary = scheme.tertiary.copy(alpha = 0.1f),
                questionCard = QuizQuestionCardDark,
                questionText = scheme.onSurface,
                answerCard = QuizAnswerCardDark,
                answerText = scheme.onSurface,
                answerBorder = QuizCardBorderDark,
                answerBadgeBg = Color(0xFF3A4643),
                answerBadgeText = scheme.primary,
                glassCard = QuizQuestionCardDark,
                glassBorder = QuizCardBorderDark,
                explanationCard = QuizExplanationCardDark,
                explanationText = scheme.onSecondaryContainer,
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
                answerBadgeText = scheme.primary,
                glassCard = Color.White.copy(alpha = 0.82f),
                glassBorder = scheme.outlineVariant.copy(alpha = 0.45f),
                explanationCard = scheme.secondaryContainer.copy(alpha = 0.85f),
                explanationText = scheme.onSecondaryContainer,
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
                .size(280.dp)
                .offset(x = (-80).dp, y = 40.dp)
                .clip(CircleShape)
                .background(colors.blobPrimary)
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = (-40).dp)
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

    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = AppShapes.Card,
            color = colors.glassCard,
            modifier = cardModifier
        ) {
            content()
        }
    } else {
        Surface(
            shape = AppShapes.Card,
            color = colors.glassCard,
            modifier = cardModifier
        ) {
            content()
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
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
            )
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
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
                color = scheme.primary
            )
            Text(
                text = "/ $total",
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OptionBadge(
    label: String,
    background: Color,
    content: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
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