package com.rustam.quizapp.data

import android.content.Context
import androidx.room.withTransaction
import com.rustam.quizapp.data.db.AppDatabase
import com.rustam.quizapp.data.db.DailyRewardEntity
import com.rustam.quizapp.domain.GemEconomy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** State of the daily login reward, exposed to the home screen. */
data class DailyRewardState(
    val canClaim: Boolean = false,
    /** Position in the reward cycle the current/next claim corresponds to (1-based). */
    val dayIndex: Int = 1,
    /** Coins awarded for [dayIndex]. */
    val rewardCoins: Int = 0,
    /** Gems awarded for [dayIndex] (0 on non-milestone days). */
    val rewardGems: Int = 0
)

/** What a daily-login claim grants, so the caller can credit both currencies. */
data class DailyRewardClaim(val coins: Int, val gems: Int)

/**
 * Tracks the daily login reward. The reward grows with the login streak over a fixed cycle
 * and resets if a day is missed. Claiming is allowed once per calendar day.
 */
class DailyRewardRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.dailyRewardDao()

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
        val safeIndex = index.coerceAtLeast(1)
        DailyRewardState(
            canClaim = canClaim,
            dayIndex = safeIndex,
            rewardCoins = rewardForDay(safeIndex),
            rewardGems = GemEconomy.dailyLoginGems(safeIndex)
        )
    }

    /**
     * Claims today's reward if available. Returns the coins and gems awarded, or zero of each if it
     * was already claimed today. The caller is responsible for crediting both currencies.
     */
    suspend fun claim(): DailyRewardClaim {
        val today = LocalDate.now().toEpochDay()
        return db.withTransaction {
            val reward = dao.get() ?: DailyRewardEntity()
            if (reward.lastClaimDay == today) return@withTransaction DailyRewardClaim(0, 0)
            val newIndex = if (reward.lastClaimDay == today - 1) reward.claimStreak + 1 else 1
            dao.upsert(reward.copy(lastClaimDay = today, claimStreak = newIndex))
            DailyRewardClaim(rewardForDay(newIndex), GemEconomy.dailyLoginGems(newIndex))
        }
    }

    companion object {
        /** Coins for each day in the 7-day cycle; it repeats after day 7. */
        val REWARD_CYCLE = listOf(24, 36, 48, 72, 96, 144, 240)

        fun rewardForDay(dayIndex: Int): Int {
            if (dayIndex <= 0) return REWARD_CYCLE.first()
            return REWARD_CYCLE[(dayIndex - 1) % REWARD_CYCLE.size]
        }
    }
}
