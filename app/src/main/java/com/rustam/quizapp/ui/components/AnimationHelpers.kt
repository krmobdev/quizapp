package com.rustam.quizapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput

/**
 * iOS-style spring press animation: scales down to [pressedScale] on press,
 * springs back with Apple's default spring parameters on release.
 *
 * Usage:
 * ```
 * Box(modifier = Modifier.iosSpringPress { onClick() }) { ... }
 * ```
 */
fun Modifier.iosSpringPress(
    pressedScale: Float = 0.95f,
    onPress: (() -> Unit)? = null
): Modifier = composed {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) { scale.snapTo(1f) }

    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessMedium
    )

    scale(scale.value).pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                scale.animateTo(pressedScale, spring(stiffness = Spring.StiffnessHigh))
                val released = tryAwaitRelease()
                scale.animateTo(1f, springSpec)
                if (released) onPress?.invoke()
            }
        )
    }
}

/**
 * Reads the [MutableInteractionSource] pressed state and applies an iOS spring scale.
 * Use this when the clickable/button already owns the interaction source (avoids double handling).
 */
@Composable
fun Modifier.iosSpringScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.95f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }

    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessMediumLow
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scale.animateTo(pressedScale, spring(stiffness = Spring.StiffnessHigh))
        } else {
            scale.animateTo(1f, springSpec)
        }
    }
    return scale(scale.value)
}
