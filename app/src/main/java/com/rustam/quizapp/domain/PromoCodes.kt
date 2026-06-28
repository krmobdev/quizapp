package com.rustam.quizapp.domain

/** A resource pack granted by a promo code: any mix of coins, gems and free XP. */
data class PromoReward(val coins: Int = 0, val gems: Int = 0, val xp: Int = 0)

/** A single-use promo code and the [reward] it grants. Codes are matched case-insensitively. */
data class PromoCode(val code: String, val reward: PromoReward)

/**
 * Catalogue of redeemable promo codes. Each code is one-time **per device** — once redeemed it is
 * recorded in the `redeemed_promo` table and cannot be claimed again on that install. Rewards are a
 * balanced mix of coin / gem / XP / combo packs of varying sizes.
 *
 * To add a campaign code, append a [PromoCode] here; no other change is needed.
 */
object PromoCodes {
    val all: List<PromoCode> = listOf(
        // Combos.
        PromoCode("WELCOME2026", PromoReward(coins = 1000, gems = 20)),
        PromoCode("FIRSTWIN", PromoReward(coins = 500, xp = 1000)),
        PromoCode("MEGAMIND", PromoReward(coins = 5000, gems = 50, xp = 3000)),
        PromoCode("GENIUS", PromoReward(coins = 3000, gems = 30)),
        PromoCode("LUCKYSEVEN", PromoReward(coins = 777, gems = 7)),
        PromoCode("KNOWITALL", PromoReward(xp = 4000, gems = 20)),
        PromoCode("GRANDPRIZE", PromoReward(coins = 10000, gems = 100, xp = 5000)),
        // Coin packs.
        PromoCode("QUIZSTART", PromoReward(coins = 500)),
        PromoCode("BRAINPOWER", PromoReward(coins = 1500)),
        PromoCode("STREAKHERO", PromoReward(coins = 2000)),
        PromoCode("COINRAIN", PromoReward(coins = 4000)),
        PromoCode("TREASURE", PromoReward(coins = 6000)),
        PromoCode("RICHQUIZ", PromoReward(coins = 7500)),
        PromoCode("JACKPOT", PromoReward(coins = 10000)),
        // Gem packs.
        PromoCode("GEMDROP", PromoReward(gems = 15)),
        PromoCode("SMARTGEMS", PromoReward(gems = 40)),
        PromoCode("POLYMATH", PromoReward(gems = 50)),
        PromoCode("GEMSTORM", PromoReward(gems = 75)),
        PromoCode("GEMVAULT", PromoReward(gems = 100)),
        // XP packs.
        PromoCode("XPBOOST", PromoReward(xp = 1000)),
        PromoCode("NIGHTOWL", PromoReward(xp = 1500)),
        PromoCode("SCHOLAR", PromoReward(xp = 2500)),
        PromoCode("LEVELUP", PromoReward(xp = 3000)),
        PromoCode("EINSTEIN", PromoReward(xp = 8000))
    )

    private val byCode: Map<String, PromoCode> = all.associateBy { it.code }

    /** Normalises [input] (trim + uppercase) and returns the matching code, or `null`. */
    fun find(input: String): PromoCode? = byCode[input.trim().uppercase()]
}
