package com.rustam.quizapp.domain

/**
 * The "Мифический сундук" (Mythic Chest): a premium loot box bought with gems. Compared to the
 * coin [LootBox] it costs the rare currency and pays out richer rewards — it rolls an unowned
 * cosmetic first (across every catalogue, including premium gem items), falling through to large
 * coin or XP piles only when nothing is left to win.
 *
 * The reward roll lives in `PlayerRepository.openMythicChest` because it needs to know which
 * cosmetics the player already owns; this object just holds the price and payout tables.
 */
object MythicBox {
    /** Gem cost to open one Mythic Chest. */
    const val PRICE_GEMS = 45

    /** Roll thresholds (1..100); a failed cosmetic roll (nothing left to win) falls through. */
    const val COSMETIC_MAX_ROLL = 55
    const val XP_MAX_ROLL = 78
    // 79..100 -> coins

    /** Coin payouts as (amount to weight) — meaningfully larger than the coin chest. */
    val coinPayouts: List<Pair<Int, Int>> = listOf(
        EconomyBalance.scale(800) to 35,
        EconomyBalance.scale(1500) to 30,
        EconomyBalance.scale(2600) to 20,
        EconomyBalance.scale(4200) to 10,
        EconomyBalance.scale(7000) to 5
    )

    /** Free-XP payouts as (amount to weight). */
    val xpPayouts: List<Pair<Int, Int>> = listOf(
        EconomyBalance.scale(1500) to 35,
        EconomyBalance.scale(3000) to 30,
        EconomyBalance.scale(5200) to 20,
        EconomyBalance.scale(8500) to 10,
        EconomyBalance.scale(14000) to 5
    )
}
