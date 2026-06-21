package com.rustam.quizapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rustam.quizapp.domain.Achievement
import com.rustam.quizapp.domain.AchievementMetrics
import com.rustam.quizapp.domain.Achievements
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.achievementsDataStore: DataStore<Preferences> by preferencesDataStore(name = "quiz_achievements")

/** Persists the set of achievement ids the player has unlocked. */
class AchievementsRepository(context: Context) {

    private val dataStore = context.applicationContext.achievementsDataStore

    fun observeUnlocked(): Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[UNLOCKED] ?: emptySet()
    }

    /**
     * Evaluates [metrics], persists any newly satisfied achievements and returns those
     * unlocked by this call (empty when nothing new was earned). The diff is computed
     * inside the [edit] block so concurrent evaluations cannot double-grant.
     */
    suspend fun unlockNew(metrics: AchievementMetrics): List<Achievement> {
        val satisfied = Achievements.satisfied(metrics)
        val newlyUnlocked = mutableListOf<Achievement>()
        dataStore.edit { prefs ->
            val current = prefs[UNLOCKED] ?: emptySet()
            val added = satisfied - current
            if (added.isNotEmpty()) {
                prefs[UNLOCKED] = current + added
                newlyUnlocked += added.mapNotNull { Achievements.byId(it) }
            }
        }
        return newlyUnlocked
    }

    private companion object {
        val UNLOCKED = stringSetPreferencesKey("unlocked_achievements")
    }
}
