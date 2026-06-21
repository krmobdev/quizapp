package com.rustam.quizapp.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rustam.quizapp.data.StreakRepository
import kotlinx.coroutines.flow.first

/**
 * Daily worker that nudges the player to keep their streak alive. It only posts a
 * notification when no quiz has been finished yet today.
 */
class StreakReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val streakRepository = StreakRepository(applicationContext)
        if (!streakRepository.hasPlayedToday()) {
            val streak = streakRepository.observeStreak().first().current
            ReminderScheduler.showReminder(applicationContext, streak)
        }
        return Result.success()
    }
}
