package com.rustam.quizapp.data

import android.content.Context
import com.rustam.quizapp.data.db.AchievementEntity
import com.rustam.quizapp.data.db.AppDatabase
import com.rustam.quizapp.domain.Achievement
import com.rustam.quizapp.domain.AchievementMetrics
import com.rustam.quizapp.domain.Achievements
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Persists the set of achievement ids the player has unlocked. */
class AchievementsRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).achievementDao()

    fun observeUnlocked(): Flow<Set<String>> = dao.observeUnlocked().map { it.toSet() }

    /**
     * Evaluates [metrics], persists any newly satisfied achievements and returns those
     * unlocked by this call (empty when nothing new was earned).
     */
    suspend fun unlockNew(metrics: AchievementMetrics): List<Achievement> {
        val satisfied = Achievements.satisfied(metrics)
        val current = dao.getUnlocked().toSet()
        val added = satisfied - current
        added.forEach { dao.unlock(AchievementEntity(it)) }
        return added.mapNotNull { Achievements.byId(it) }
    }
}
