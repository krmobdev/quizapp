package com.rustam.quizapp.domain

import androidx.annotation.StringRes

/** Currency a Millionaire pack is bought with — the prize is paid in the same currency. */
enum class PackCurrency { XP, COINS, GEMS }

/** The three one-time-per-run lifelines, in the show's spirit. */
enum class Lifeline { FIFTY_FIFTY, PHONE_FRIEND, ASK_AUDIENCE }

/**
 * One buyable Millionaire pack. Built at runtime from the category list so the per-category packs
 * stay in sync with [com.rustam.quizapp.data.QuestionRepository.getCategories].
 *
 * [scope] is the category id, or `null` for an "all categories" pack (cheaper). The prize is the
 * same for both scopes — the category premium buys a more familiar question set.
 */
data class MillionairePack(
    val id: String,
    val currency: PackCurrency,
    val scope: String?,
    val cost: Int,
    val prize: Int,
    val emoji: String,
    @param:StringRes val labelRes: Int
)

/**
 * The 15-rung prize ladder. Each correct answer climbs one rung; the player's current winnings are
 * [amountAt] the number of rungs cleared. Two rungs ([SAFE_RUNGS]) are guaranteed safe havens: a
 * wrong answer drops the player to [guaranteedAmount] (the highest safe rung already cleared, or 0)
 * rather than wiping everything. Clearing all [LENGTH] rungs pays the full prize.
 */
object MillionaireLadder {
    const val LENGTH = 15

    /** Cumulative winnings as a fraction of the prize for clearing rung 1..15. */
    private val FRACTIONS = listOf(
        0.01, 0.025, 0.045, 0.07, 0.10,
        0.14, 0.19, 0.25, 0.32, 0.40,
        0.50, 0.62, 0.75, 0.87, 1.00
    )

    /** 1-based rungs whose winnings are guaranteed once cleared. */
    val SAFE_RUNGS = setOf(5, 10)

    /** Winnings (in prize currency) for clearing [rung] rungs (0 = nothing cleared). */
    fun amountAt(rung: Int, prize: Int): Int {
        if (rung <= 0) return 0
        val idx = (rung - 1).coerceAtMost(FRACTIONS.lastIndex)
        return Math.round(FRACTIONS[idx] * prize).toInt()
    }

    fun isSafe(rung: Int): Boolean = rung in SAFE_RUNGS

    /** Guaranteed fallback after clearing [clearedRungs] rungs: the highest safe rung ≤ it. */
    fun guaranteedAmount(clearedRungs: Int, prize: Int): Int {
        val safeRung = SAFE_RUNGS.filter { it <= clearedRungs }.maxOrNull() ?: 0
        return amountAt(safeRung, prize)
    }
}

/**
 * Static tuning for the Millionaire mode: pack costs/prizes per currency and the difficulty mix.
 * Pack ids are stable strings (`CURRENCY:scope`) so they survive a navigation argument round-trip.
 */
object MillionaireCatalog {
    const val EASY_COUNT = 5
    const val MEDIUM_COUNT = 5
    const val HARD_COUNT = 5
    const val TOTAL = EASY_COUNT + MEDIUM_COUNT + HARD_COUNT

    private const val ALL_SCOPE = "all"

    /** Buy-in cost; per-category packs cost more than the cheaper "all categories" pack. */
    fun cost(currency: PackCurrency, isCategory: Boolean): Int = when (currency) {
        PackCurrency.COINS -> if (isCategory) 7000 else 5000
        PackCurrency.XP -> if (isCategory) 6000 else 4000
        PackCurrency.GEMS -> if (isCategory) 75 else 50
    }

    /** Full prize for clearing the whole ladder (same for both scopes of a currency). */
    fun prize(currency: PackCurrency): Int = when (currency) {
        PackCurrency.COINS -> 10000
        PackCurrency.XP -> 8000
        PackCurrency.GEMS -> 100
    }

    fun packId(currency: PackCurrency, scope: String?): String =
        "${currency.name}:${scope ?: ALL_SCOPE}"

    /** Parses a [packId] back into (currency, scope) — scope `null` means all categories. */
    fun parse(packId: String): Pair<PackCurrency, String?>? {
        val parts = packId.split(":", limit = 2)
        if (parts.size != 2) return null
        val currency = PackCurrency.entries.find { it.name == parts[0] } ?: return null
        val scope = parts[1].takeUnless { it == ALL_SCOPE }
        return currency to scope
    }
}
