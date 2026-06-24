package com.rustam.quizapp.domain

/**
 * The "Сундук удачи" (Lucky Chest): spend coins for a random reward. It is both a coin sink and a
 * little burst of excitement — most opens return coins or XP (sometimes more than the price, often
 * a bit less), with a rarer chance of an unowned avatar or title.
 *
 * The reward roll lives in `PlayerRepository.openLootBox` because it needs to know which cosmetics
 * the player already owns; this object just holds the price and payout tables.
 */
object LootBox {
    /** Coin cost to open one chest. */
    const val PRICE = 500

    /** Roll thresholds (1..100). A failed cosmetic roll (nothing left to win) falls through. */
    const val AVATAR_MAX_ROLL = 15
    const val TITLE_MAX_ROLL = 25
    const val XP_MAX_ROLL = 55
    // 56..100 -> coins

    /** Coin payouts as (amount to weight). Average sits a little under [PRICE] — the gamble. */
    val coinPayouts: List<Pair<Int, Int>> = listOf(
        100 to 35,
        250 to 30,
        450 to 20,
        700 to 10,
        1200 to 5
    )

    /** Free-XP payouts as (amount to weight). */
    val xpPayouts: List<Pair<Int, Int>> = listOf(
        200 to 35,
        450 to 30,
        800 to 20,
        1400 to 10,
        2200 to 5
    )

    /** Picks a value from a weighted (value to weight) table. */
    fun weightedPick(table: List<Pair<Int, Int>>): Int {
        val total = table.sumOf { it.second }
        if (total <= 0) return table.firstOrNull()?.first ?: 0
        var roll = (1..total).random()
        for ((value, weight) in table) {
            roll -= weight
            if (roll <= 0) return value
        }
        return table.last().first
    }
}

/** The outcome of opening one [LootBox], surfaced to the shop screen for the reveal dialog. */
sealed interface LootResult {
    data class Coins(val amount: Int) : LootResult
    data class Xp(val amount: Int) : LootResult
    data class Avatar(val item: AvatarItem) : LootResult
    data class Title(val item: TitleItem) : LootResult
}
