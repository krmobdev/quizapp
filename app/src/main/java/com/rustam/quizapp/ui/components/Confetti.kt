package com.rustam.quizapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

/** Bright, varied palette so the burst reads as celebratory regardless of the active accent theme. */
private val ConfettiColors = listOf(
    Color(0xFFFF5252), Color(0xFFFFD740), Color(0xFF69F0AE),
    Color(0xFF40C4FF), Color(0xFFE040FB), Color(0xFFFF6E40),
    Color(0xFF7C4DFF), Color(0xFF18FFFF), Color(0xFFFFAB40)
)

private class ConfettiParticle(
    val originX: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val sizeDp: Float,
    val widthRatio: Float,
    val startRotation: Float,
    val rotationSpeed: Float
)

/**
 * A one-shot confetti cannon: particles launch up-and-inward from the two bottom corners, arc under
 * gravity and fade out. Pure Compose Canvas — no external dependency. Drop it as a full-size overlay
 * (e.g. on top of the result screen) and it animates once on first composition.
 */
@Composable
fun ConfettiBurst(
    modifier: Modifier = Modifier,
    particleCount: Int = 150,
    durationMillis: Int = 3400
) {
    val particles = remember {
        List(particleCount) { index ->
            val fromLeft = index % 2 == 0
            val direction = if (fromLeft) 1f else -1f
            ConfettiParticle(
                originX = if (fromLeft) 0.04f else 0.96f,
                velocityX = direction * (0.22f + Random.nextFloat() * 0.6f),
                velocityY = 1.15f + Random.nextFloat() * 0.75f,
                color = ConfettiColors[Random.nextInt(ConfettiColors.size)],
                sizeDp = 7f + Random.nextFloat() * 7f,
                widthRatio = 0.4f + Random.nextFloat() * 0.6f,
                startRotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 760f
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis, easing = LinearEasing))
    }

    val gravity = 1.7f
    val t = progress.value
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            // Projectile motion in fractional coords; y grows downward, origin at the bottom (y=1).
            val x = p.originX + p.velocityX * t
            val y = 1f - (p.velocityY * t - 0.5f * gravity * t * t)
            if (y !in -0.1f..1.15f) return@forEach
            val cx = x * w
            val cy = y * h
            val alpha = if (t > 0.78f) ((1f - t) / 0.22f).coerceIn(0f, 1f) else 1f
            val sizePx = p.sizeDp * density
            val halfW = sizePx / 2f
            val halfH = sizePx * p.widthRatio / 2f
            rotate(degrees = p.startRotation + p.rotationSpeed * t, pivot = Offset(cx, cy)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(cx - halfW, cy - halfH),
                    size = Size(sizePx, sizePx * p.widthRatio)
                )
            }
        }
    }
}
