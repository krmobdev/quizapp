package com.rustam.quizapp.data

import android.content.Context
import androidx.room.withTransaction
import com.rustam.quizapp.data.db.AppDatabase
import com.rustam.quizapp.data.db.AppStateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * User-facing settings (sound, theme mode, language), persisted in the shared
 * `app_state` Room row.
 */
class SettingsRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.appStateDao()

    /** Whether sound effects are enabled. Defaults to `true` when never set. */
    val soundEnabled: Flow<Boolean> = dao.observe().map { it?.soundEnabled ?: DEFAULT_SOUND_ENABLED }

    suspend fun setSoundEnabled(enabled: Boolean) = update { it.copy(soundEnabled = enabled) }

    val themeMode: Flow<ThemeMode> = dao.observe().map { ThemeMode.fromStored(it?.themeMode) }

    suspend fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode.name) }

    val appLanguage: Flow<AppLanguage> = dao.observe().map { AppLanguage.fromStored(it?.appLanguage) }

    suspend fun setAppLanguage(language: AppLanguage) = update { it.copy(appLanguage = language.name) }

    /** Whether the first-launch onboarding has been completed. */
    val onboardingShown: Flow<Boolean> = dao.observe().map { it?.onboardingShown ?: false }

    suspend fun setOnboardingShown() = update { it.copy(onboardingShown = true) }

    private suspend inline fun update(crossinline transform: (AppStateEntity) -> AppStateEntity) {
        db.withTransaction {
            val current = dao.get() ?: AppStateEntity()
            dao.upsert(transform(current))
        }
    }

    private companion object {
        const val DEFAULT_SOUND_ENABLED = true
    }
}
