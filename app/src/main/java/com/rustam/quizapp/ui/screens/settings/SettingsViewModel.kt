package com.rustam.quizapp.ui.screens.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.rustam.quizapp.R
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.data.AppLanguage
import com.rustam.quizapp.data.BackupRepository
import com.rustam.quizapp.data.ImportResult
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

/** A promo-redemption result message for the UI: the text to show and whether it was a success. */
data class PromoUiMessage(val text: String, val success: Boolean)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val progressRepository = QuizProgressRepository(application)
    private val playerRepository = PlayerRepository(application, QuestionRepository(application))
    private val backupRepository = BackupRepository(application)

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

    private val _promoMessage = MutableStateFlow<PromoUiMessage?>(null)
    val promoMessage: StateFlow<PromoUiMessage?> = _promoMessage.asStateFlow()

    /** Result message for the last export/import/reset action (string res id), or null. */
    private val _backupMessageRes = MutableStateFlow<Int?>(null)
    val backupMessageRes: StateFlow<Int?> = _backupMessageRes.asStateFlow()

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
            _promoMessage.value = when (val result = playerRepository.redeemPromoCode(code)) {
                is PromoRedeemResult.Success -> PromoUiMessage(
                    text = getApplication<Application>().getString(
                        R.string.settings_promo_success_format,
                        formatReward(result.coins, result.gems, result.xp)
                    ),
                    success = true
                )
                PromoRedeemResult.Invalid -> PromoUiMessage(
                    text = getApplication<Application>().getString(R.string.settings_promo_invalid),
                    success = false
                )
                PromoRedeemResult.AlreadyRedeemed -> PromoUiMessage(
                    text = getApplication<Application>().getString(R.string.settings_promo_already),
                    success = false
                )
            }
        }
    }

    private fun formatReward(coins: Int, gems: Int, xp: Int): String = buildList {
        if (coins > 0) add("+$coins 🪙")
        if (gems > 0) add("+$gems 💎")
        if (xp > 0) add("+$xp ✨")
    }.joinToString("  ")

    fun clearPromoMessage() {
        _promoMessage.value = null
    }

    fun exportProgress(target: Uri) {
        viewModelScope.launch {
            val ok = runCatching { backupRepository.export(target) }.isSuccess
            _backupMessageRes.value =
                if (ok) R.string.settings_backup_export_success else R.string.settings_backup_export_error
        }
    }

    fun importProgress(source: Uri) {
        viewModelScope.launch {
            _backupMessageRes.value = when (backupRepository.import(source)) {
                ImportResult.SUCCESS -> R.string.settings_backup_import_success
                ImportResult.UNSUPPORTED_VERSION -> R.string.settings_backup_import_version
                ImportResult.INVALID_FILE -> R.string.settings_backup_import_error
            }
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            backupRepository.resetProgress()
            _backupMessageRes.value = R.string.settings_backup_reset_success
        }
    }

    fun clearBackupMessage() {
        _backupMessageRes.value = null
    }

    /** Resets the onboarding flag; MainActivity observes it and reopens the tutorial. */
    fun showOnboardingAgain() {
        viewModelScope.launch { settingsRepository.resetOnboarding() }
    }
}
