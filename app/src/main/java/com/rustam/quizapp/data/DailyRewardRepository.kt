package com.rustam.quizapp.data

import android.content.Context
import com.rustam.quizapp.data.db.AppDatabase
import com.rustam.quizapp.data.db.DailyRewardEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** State of the daily login reward, exposed to the home screen. */
data class DailyRewardState(
    val canClaim: Boolean = false,
    /** Position in the reward cycle the current/next claim corresponds to (1-based). */
    val dayIndex: Int = 1,
    /** Coins awarded for [dayIndex]. */
    val rewardCoins: Int = 0
)

/**
 * Tracks the daily login reward. The reward grows with the login streak over a fixed cycle
 * and resets if a day is missed. Claiming is allowed once per calendar day.
 */
class DailyRewardRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).dailyRewardDao()

    fun observeReward(): Flow<DailyRewardState> = dao.observe().map { entity ->
        val reward = entity ?: DailyRewardEntity()
        val today = LocalDate.now().toEpochDay()
        val last = reward.lastClaimDay
        val streak = reward.claimStreak
        val canClaim = last != today
        val nextIndex = when {
            last == today -> streak                    // already claimed today
            last == today - 1 -> streak + 1            // continuing the streak
            else -> 1                                  // streak (re)starts
        }
        val index = if (canClaim) nextIndex else streak
        DailyRewardState(
            canClaim = canClaim,
            dayIndex = index.coerceAtLeast(1),
            rewardCoins = rewardForDay(index)
        )
    }

    /**
     * Claims today's reward if available. Returns the number of coins awarded, or 0 if it
     * was already claimed today. The caller is responsible for crediting the coins.
     */
    suspend fun claim(): Int {
        val today = LocalDate.now().toEpochDay()
        val reward = dao.get() ?: DailyRewardEntity()
        if (reward.lastClaimDay == today) return 0
        val newIndex = if (reward.lastClaimDay == today - 1) reward.claimStreak + 1 else 1
        dao.upsert(reward.copy(lastClaimDay = today, claimStreak = newIndex))
        return rewardForDay(newIndex)
    }

    companion object {
        /** Coins for each day in the 7-day cycle; it repeats after day 7. */
        val REWARD_CYCLE = listOf(50, 75, 100, 150, 200, 300, 500)

        fun rewardForDay(dayIndex: Int): Int {
            if (dayIndex <= 0) return REWARD_CYCLE.first()
            return REWARD_CYCLE[(dayIndex - 1) % REWARD_CYCLE.size]
        }
    }
}
