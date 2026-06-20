package com.rustam.quizapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * User-facing settings, stored in the same [statsDataStore] as the quiz statistics.
 *
 * Currently exposes the "sound enabled" toggle (default `true`).
 */
class SettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.statsDataStore

    /** Whether sound effects are enabled. Defaults to `true` when never set. */
    val soundEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SOUND_ENABLED] ?: DEFAULT_SOUND_ENABLED
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[SOUND_ENABLED] = enabled }
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.fromStored(prefs[THEME_MODE])
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[THEME_MODE] = mode.name }
    }

    private companion object {
        const val DEFAULT_SOUND_ENABLED = true
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
