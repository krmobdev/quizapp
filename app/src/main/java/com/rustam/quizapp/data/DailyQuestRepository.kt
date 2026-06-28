package com.rustam.quizapp.data

import android.content.Context
import androidx.room.withTransaction
import com.rustam.quizapp.data.db.AppDatabase
import com.rustam.quizapp.data.db.DailyQuestEntity
import com.rustam.quizapp.domain.DailyChallenge
import com.rustam.quizapp.domain.DailyChallengeType
import com.rustam.quizapp.domain.DailyChallenges
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** A daily challenge paired with the player's progress towards it for today. */
data class DailyChallengeProgress(
    val challenge: DailyChallenge,
    val current: Int,
    val claimed: Boolean
) {
    val completed: Boolean get() = current >= challenge.target
    val canClaim: Boolean get() = completed && !claimed
    val progressFraction: Float
        get() = if (challenge.target > 0) (current.toFloat() / challenge.target).coerceIn(0f, 1f) else 0f
}

/** Today's challenge set with progress, exposed to the home screen. */
data class DailyQuestsState(
    val challenges: List<DailyChallengeProgress> = emptyList()
) {
    val claimableCount: Int get() = challenges.count { it.canClaim }
}

/**
 * Tracks progress on the rotating [DailyChallenges]. Per-day counters live in a single row; when a
 * quiz is recorded on a new day the counters and the claimed bitmask reset, so each day starts
 * fresh. Reads ([observe]) treat a stored row from a previous day as "no progress yet".
 */
class DailyQuestRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.dailyQuestDao()

    fun observe(): Flow<DailyQuestsState> = dao.observe().map { entity ->
        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay()
        val current = entity?.takeIf { it.day == todayEpoch }
        val set = DailyChallenges.dailySet(today)
        val challenges = set.mapIndexed { index, challenge ->
            val progress = current?.let { progressFor(challenge.type, it) } ?: 0
            val claimed = current != null && (current.claimedMask and (1 shl index)) != 0
            DailyChallengeProgress(
                challenge = challenge,
                current = progress.coerceAtMost(challenge.target),
                claimed = claimed
            )
        }
        DailyQuestsState(challenges)
    }

    /**
     * Records one finished quiz against today's counters, rolling over (and resetting) if the
     * stored row is from a previous day.
     */
    suspend fun recordQuizFinished(correct: Int, total: Int, coinsEarned: Int) {
        val todayEpoch = LocalDate.now().toEpochDay()
        db.withTransaction {
            val stored = dao.get()
            val base = if (stored == null || stored.day != todayEpoch) {
                DailyQuestEntity(day = todayEpoch)
            } else {
                stored
            }
            val perfect = if (total > 0 && correct >= total) 1 else 0
            dao.upsert(
                base.copy(
                    day = todayEpoch,
                    quizzesPlayed = base.quizzesPlayed + 1,
                    correctAnswers = base.correctAnswers + correct,
                    perfectQuizzes = base.perfectQuizzes + perfect,
                    coinsEarned = base.coinsEarned + coinsEarned
                )
            )
        }
    }

    /**
     * Claims the challenge at [index] in today's set if it is complete and not yet claimed. Returns
     * the claimed [DailyChallenge] (so the caller can credit its reward), or `null` otherwise.
     */
    suspend fun claim(index: Int): DailyChallenge? {
        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay()
        val challenge = DailyChallenges.dailySet(today).getOrNull(index) ?: return null
        var claimed: DailyChallenge? = null
        db.withTransaction {
            val entity = dao.get()?.takeIf { it.day == todayEpoch } ?: return@withTransaction
            val bit = 1 shl index
            if (entity.claimedMask and bit != 0) return@withTransaction
            if (progressFor(challenge.type, entity) < challenge.target) return@withTransaction
            dao.upsert(entity.copy(claimedMask = entity.claimedMask or bit))
            claimed = challenge
        }
        return claimed
    }

    /**
     * Grants the once-per-day "all daily quests done" gem bonus exactly once: returns `true` only
     * the first time today's full set has been claimed. Uses the high bit ([ALL_DONE_BIT]) of the
     * existing [DailyQuestEntity.claimedMask] as the "bonus already paid today" flag, so no schema
     * change is needed. The caller credits the gems.
     */
    suspend fun claimAllCompleteBonus(): Boolean {
        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay()
        val slots = DailyChallenges.dailySet(today).size
        if (slots <= 0) return false
        val fullMask = (1 shl slots) - 1
        var granted = false
        db.withTransaction {
            val entity = dao.get()?.takeIf { it.day == todayEpoch } ?: return@withTransaction
            if (entity.claimedMask and ALL_DONE_BIT != 0) return@withTransaction
            if (entity.claimedMask and fullMask != fullMask) return@withTransaction
            dao.upsert(entity.copy(claimedMask = entity.claimedMask or ALL_DONE_BIT))
            granted = true
        }
        return granted
    }

    private fun progressFor(type: DailyChallengeType, entity: DailyQuestEntity): Int = when (type) {
        DailyChallengeType.PLAY_QUIZZES -> entity.quizzesPlayed
        DailyChallengeType.ANSWER_CORRECT -> entity.correctAnswers
        DailyChallengeType.PERFECT_QUIZ -> entity.perfectQuizzes
        DailyChallengeType.EARN_COINS -> entity.coinsEarned
    }

    private companion object {
        /** High bit of [DailyQuestEntity.claimedMask] marking the all-quests gem bonus as paid. */
        const val ALL_DONE_BIT = 1 shl 31
    }
}
