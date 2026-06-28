package com.rustam.quizapp.domain

/** Which catalogue list a [DealTemplate] draws from (decides how a purchase is granted). */
enum class DealKind { POWERUP, BOOSTER, BOOST }

data class DealTemplate(
    val dealId: String,
    val kind: DealKind,
    val emoji: String,
    val labelRes: Int,
    val basePrice: Int
)

/**
 * Daily rotating "Deals" — a handful of consumables offered at a discount, refreshed each day and
 * capped per day. The selection is derived deterministically from the calendar day (no server), so
 * every session agrees on today's offers. Gives a reason to check in daily and a recurring coin sink
 * for late-game players who already own all the cosmetics.
 */
object ShopDeals {
    const val DEAL_COUNT = 3
    const val DISCOUNT_PERCENT = 30
    const val MAX_PER_DAY = 3

    fun dealPrice(basePrice: Int): Int =
        (basePrice * (100 - DISCOUNT_PERCENT) / 100).coerceAtLeast(1)

    private val pool: List<DealTemplate> = buildList {
        ShopCatalog.powerUps.forEach {
            add(DealTemplate(it.id, DealKind.POWERUP, it.emoji, it.labelRes, it.priceCoins))
        }
        ShopCatalog.boosters.forEach {
            add(DealTemplate(it.id, DealKind.BOOSTER, it.emoji, it.labelRes, it.priceCoins))
        }
        ShopCatalog.boosts.forEach {
            add(DealTemplate(it.id, DealKind.BOOST, it.emoji, it.labelRes, it.priceCoins))
        }
    }

    /** Deterministic daily selection of [DEAL_COUNT] offers for [epochDay]. */
    fun dealsForDay(epochDay: Long): List<DealTemplate> {
        if (pool.isEmpty()) return emptyList()
        return pool.shuffled(kotlin.random.Random(epochDay)).take(DEAL_COUNT.coerceAtMost(pool.size))
    }

    fun template(dealId: String): DealTemplate? = pool.find { it.dealId == dealId }
}
