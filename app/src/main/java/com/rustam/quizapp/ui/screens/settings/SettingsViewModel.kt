package com.rustam.quizapp.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.rustam.quizapp.R
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.data.AppLanguage
import com.rustam.quizapp.data.PlayerRepository
import com.rustam.quizapp.data.PromoRedeemResult
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.QuizProgressRepository
import com.rustam.quizapp.data.SettingsRepository
import com.rustam.quizapp.data.ThemeMode
import com.rustam.quizapp.ui.theme.AccentTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val progressRepository = QuizProgressRepository(application)
    private val playerRepository = PlayerRepository(application, QuestionRepository(application))

    /** Mirrors the persisted `sound_enabled` flag (default `true`) for the UI switch. */
    val soundEnabled: StateFlow<Boolean> = settingsRepository.soundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val appLanguage: StateFlow<AppLanguage> = settingsRepository.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLanguage.defaultForSystem())

    /** Accent palette equipped in the shop, applied app-wide by the theme. */
    val accentTheme: StateFlow<AccentTheme> = playerRepository.equippedThemeId
        .map { AccentTheme.fromId(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccentTheme.DEFAULT)

    val promoRedeemed: StateFlow<Boolean> = playerRepository.promoRedeemed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _promoMessageRes = MutableStateFlow<Int?>(null)
    val promoMessageRes: StateFlow<Int?> = _promoMessageRes.asStateFlow()

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundEnabled(enabled) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.setAppLanguage(language)
            progressRepository.clear()
        }
    }

    fun redeemPromoCode(code: String) {
        viewModelScope.launch {
            when (playerRepository.redeemPromoCode(code)) {
                PromoRedeemResult.SUCCESS -> _promoMessageRes.value = R.string.settings_promo_success
                PromoRedeemResult.INVALID_CODE -> _promoMessageRes.value = R.string.settings_promo_invalid
                PromoRedeemResult.ALREADY_REDEEMED -> _promoMessageRes.value = R.string.settings_promo_already
            }
        }
    }

    fun clearPromoMessage() {
        _promoMessageRes.value = null
    }
}