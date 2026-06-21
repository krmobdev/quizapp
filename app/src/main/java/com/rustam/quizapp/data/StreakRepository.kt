package com.rustam.quizapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Current consecutive-day play streak, exposed to the UI. */
data class StreakState(
    val current: Int = 0,
    val best: Int = 0,
    val playedToday: Boolean = false
)

private val Context.streakDataStore: DataStore<Preferences> by preferencesDataStore(name = "quiz_streak")

/**
 * Tracks how many days in a row the player has finished at least one quiz.
 *
 * A streak is considered alive when the last play was today or yesterday; once a full
 * day is missed it collapses back to zero. The stored counter is only mutated on
 * [recordPlayed]; reads ([observeStreak]) compute the live value so a stale counter from
 * days ago is never shown as still running.
 */
class StreakRepository(context: Context) {

    private val dataStore = context.applicationContext.streakDataStore

    fun observeStreak(): Flow<StreakState> = dataStore.data.map { prefs ->
        val today = LocalDate.now().toEpochDay()
        val last = prefs[LAST_PLAYED_DAY] ?: -1L
        val stored = prefs[CURRENT_STREAK] ?: 0
        val current = if (last == today || last == today - 1) stored else 0
        StreakState(
            current = current,
            best = prefs[BEST_STREAK] ?: 0,
            playedToday = last == today
        )
    }

    suspend fun hasPlayedToday(): Boolean {
        val prefs = dataStore.data.first()
        return (prefs[LAST_PLAYED_DAY] ?: -1L) == LocalDate.now().toEpochDay()
    }

    /** Records that a quiz was finished today and returns the updated streak. */
    suspend fun recordPlayed(): StreakState {
        val today = LocalDate.now().toEpochDay()
        var result = StreakState()
        dataStore.edit { prefs ->
            val last = prefs[LAST_PLAYED_DAY] ?: -1L
            val stored = prefs[CURRENT_STREAK] ?: 0
            val newCurrent = when (last) {
                today -> stored.coerceAtLeast(1) // already counted today
                today - 1 -> stored + 1          // consecutive day
                else -> 1                         // streak (re)starts
            }
            val newBest = maxOf(prefs[BEST_STREAK] ?: 0, newCurrent)
            prefs[LAST_PLAYED_DAY] = today
            prefs[CURRENT_STREAK] = newCurrent
            prefs[BEST_STREAK] = newBest
            result = StreakState(current = newCurrent, best = newBest, playedToday = true)
        }
        return result
    }

    private companion object {
        val LAST_PLAYED_DAY = longPreferencesKey("last_played_day")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val BEST_STREAK = intPreferencesKey("best_streak")
    }
}
