package com.rustam.quizapp.data

import android.content.Context
import com.rustam.quizapp.data.db.AppDatabase
import com.rustam.quizapp.data.db.StreakEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Current consecutive-day play streak, exposed to the UI. */
data class StreakState(
    val current: Int = 0,
    val best: Int = 0,
    val playedToday: Boolean = false
)

/**
 * Tracks how many days in a row the player has finished at least one quiz.
 *
 * A streak is considered alive when the last play was today or yesterday; once a full
 * day is missed it collapses back to zero. The stored counter is only mutated on
 * [recordPlayed]; reads ([observeStreak]) compute the live value so a stale counter from
 * days ago is never shown as still running.
 */
class StreakRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).streakDao()

    fun observeStreak(): Flow<StreakState> = dao.observe().map { entity ->
        val streak = entity ?: StreakEntity()
        val today = LocalDate.now().toEpochDay()
        val last = streak.lastPlayedDay
        val stored = streak.current
        val freezes = streak.freezeCount
        val missedDays = today - last - 1
        // The streak survives a gap if the player owns enough Streak Freezes to cover
        // every missed day; the freezes are actually consumed on the next [recordPlayed].
        val current = when {
            last == today || last == today - 1 -> stored
            missedDays in 1..freezes -> stored
            else -> 0
        }
        StreakState(
            current = current,
            best = streak.best,
            playedToday = last == today
        )
    }

    /** Number of Streak Freezes the player currently owns. */
    fun observeFreezeCount(): Flow<Int> = dao.observe().map { it?.freezeCount ?: 0 }

    /** Adds one Streak Freeze to the player's stock (called after a successful purchase). */
    suspend fun addFreeze() {
        val streak = dao.get() ?: StreakEntity()
        dao.upsert(streak.copy(freezeCount = streak.freezeCount + 1))
    }

    suspend fun hasPlayedToday(): Boolean {
        val last = dao.get()?.lastPlayedDay ?: -1L
        return last == LocalDate.now().toEpochDay()
    }

    /** Records that a quiz was finished today and returns the updated streak. */
    suspend fun recordPlayed(): StreakState {
        val today = LocalDate.now().toEpochDay()
        val streak = dao.get() ?: StreakEntity()
        val last = streak.lastPlayedDay
        val stored = streak.current
        val freezes = streak.freezeCount
        val missedDays = today - last - 1
        var newFreezes = freezes
        val newCurrent = when {
            last == today -> stored.coerceAtLeast(1) // already counted today
            last == today - 1 -> stored + 1          // consecutive day
            last >= 0 && missedDays in 1..freezes -> {
                // Spend one freeze per missed day to bridge the gap and keep the streak.
                newFreezes = freezes - missedDays.toInt()
                stored + 1
            }
            else -> 1                                 // streak (re)starts
        }
        val newBest = maxOf(streak.best, newCurrent)
        dao.upsert(
            streak.copy(
                current = newCurrent,
                best = newBest,
                freezeCount = newFreezes,
                lastPlayedDay = today
            )
        )
        return StreakState(current = newCurrent, best = newBest, playedToday = true)
    }
}
