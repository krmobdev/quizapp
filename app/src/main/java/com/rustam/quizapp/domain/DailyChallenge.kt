package com.rustam.quizapp.domain

import androidx.annotation.StringRes
import com.rustam.quizapp.R
import java.time.LocalDate

/** What a daily challenge measures over the current calendar day. */
enum class DailyChallengeType { PLAY_QUIZZES, ANSWER_CORRECT, PERFECT_QUIZ, EARN_COINS }

/**
 * A single daily-challenge template: a goal of [type] with a [target], plus the coin/XP reward for
 * completing it. The catalogue rotates a fresh set in every day to give a reason to come back.
 */
data class DailyChallenge(
    val id: String,
    val type: DailyChallengeType,
    val emoji: String,
    @param:StringRes val titleRes: Int,
    val target: Int,
    val rewardCoins: Int,
    val rewardXp: Int
)

object DailyChallenges {

    /** How many challenges are offered each day. */
    const val DAILY_COUNT = 3

    /** Pool of templates; [dailySet] picks [DAILY_COUNT] of distinct types each day. */
    val pool: List<DailyChallenge> = listOf(
        DailyChallenge("play3", DailyChallengeType.PLAY_QUIZZES, "🎯", R.string.quest_play_quizzes, 3, 100, 130),
        DailyChallenge("play5", DailyChallengeType.PLAY_QUIZZES, "🎯", R.string.quest_play_quizzes, 5, 180, 240),
        DailyChallenge("correct20", DailyChallengeType.ANSWER_CORRECT, "✅", R.string.quest_answer_correct, 20, 130, 180),
        DailyChallenge("correct40", DailyChallengeType.ANSWER_CORRECT, "✅", R.string.quest_answer_correct, 40, 220, 280),
        DailyChallenge("perfect", DailyChallengeType.PERFECT_QUIZ, "💯", R.string.quest_perfect_quiz, 1, 180, 220),
        DailyChallenge("coins400", DailyChallengeType.EARN_COINS, "🪙", R.string.quest_earn_coins, 400, 120, 140),
        DailyChallenge("coins1200", DailyChallengeType.EARN_COINS, "🪙", R.string.quest_earn_coins, 1200, 200, 200)
    )

    /**
     * The deterministic set of [DAILY_COUNT] challenges for [day]. The pool is shuffled with the
     * date as seed (so the set is stable within a day but rotates daily) and the first challenges
     * of distinct types are taken, to avoid offering two goals of the same kind.
     */
    fun dailySet(day: LocalDate = LocalDate.now()): List<DailyChallenge> {
        val shuffled = pool.shuffled(kotlin.random.Random(day.toEpochDay()))
        val picked = mutableListOf<DailyChallenge>()
        val usedTypes = mutableSetOf<DailyChallengeType>()
        for (challenge in shuffled) {
            if (challenge.type in usedTypes) continue
            picked.add(challenge)
            usedTypes.add(challenge.type)
            if (picked.size == DAILY_COUNT) break
        }
        return picked
    }
}
