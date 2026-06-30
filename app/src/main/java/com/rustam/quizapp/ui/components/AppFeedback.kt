package com.rustam.quizapp.ui.components

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.audio.SoundManager
import com.rustam.quizapp.audio.SoundResources
import com.rustam.quizapp.audio.SoundType
import com.rustam.quizapp.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Activity-scoped ViewModel that owns the single SoundManager used for UI button clicks.
 * Lives for the entire session, keeping one SoundPool alive instead of one per screen.
 */
class AppFeedbackViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepo = SettingsRepository(application)

    val hapticEnabled: StateFlow<Boolean> = settingsRepo.hapticEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val soundManager = SoundManager(
        context = application,
        sounds = SoundResources.load(application),
        soundEnabled = settingsRepo.soundEnabled,
        scope = viewModelScope
    )

    fun playClick() = soundManager.play(SoundType.CLICK)

    override fun onCleared() = soundManager.release()
}

/**
 * Stable holder for app-wide button feedback.
 * Values are read from [vm] at call time so toggling the setting takes effect immediately
 * without requiring recomposition of every consumer.
 */
class AppFeedback(
    private val vm: AppFeedbackViewModel,
    private val haptic: HapticFeedback
) {
    fun click() {
        if (vm.hapticEnabled.value) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        vm.playClick()
    }
}

/** Composition local providing the shared [AppFeedback] instance. Null outside the app root. */
val LocalAppFeedback = staticCompositionLocalOf<AppFeedback?> { null }

/**
 * Wraps [content] with a provided [LocalAppFeedback] so that [AppActionButton], [GlassCard],
 * tab items, and any other component can fire consistent sound + haptic on tap.
 */
@Composable
fun ProvideAppFeedback(content: @Composable () -> Unit) {
    val vm: AppFeedbackViewModel = viewModel()
    val haptic = LocalHapticFeedback.current
    val feedback = remember(vm, haptic) { AppFeedback(vm, haptic) }
    CompositionLocalProvider(LocalAppFeedback provides feedback) {
        content()
    }
}
