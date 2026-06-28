package com.rustam.quizapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Convenience wrapper that surfaces the platform haptic feedback through a stable
 * helper object, keeping all [HapticFeedbackType] semantics in one place.
 *
 * Usage:
 * ```
 * val haptic = rememberHapticHelper()
 * Button(onClick = { haptic.confirm() }) { ... }
 * ```
 */
class HapticHelper(private val feedback: HapticFeedback) {
    /** Short tick — correct answer, successful purchase, claim. */
    fun confirm() = feedback.performHapticFeedback(HapticFeedbackType.Confirm)

    /** Two-pulse error — wrong answer, insufficient funds. */
    fun reject() = feedback.performHapticFeedback(HapticFeedbackType.Reject)

    /** Light tap — generic button press, menu selection. */
    fun click() = feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)

    /** Long pulse — level-up, rare drop, big reward. */
    fun celebrate() {
        feedback.performHapticFeedback(HapticFeedbackType.Confirm)
        feedback.performHapticFeedback(HapticFeedbackType.Confirm)
    }
}

@Composable
fun rememberHapticHelper(): HapticHelper {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) { HapticHelper(feedback) }
}
