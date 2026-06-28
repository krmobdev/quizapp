package com.rustam.quizapp

import com.rustam.quizapp.domain.GemEconomy
import com.rustam.quizapp.domain.ShopCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GemEconomyTest {

    @Test
    fun levelUpGems_scaleWithLevelsGained() {
        assertEquals(0, GemEconomy.levelUpGems(5, 5))
        assertEquals(GemEconomy.GEMS_PER_LEVEL, GemEconomy.levelUpGems(5, 6))
        assertEquals(3 * GemEconomy.GEMS_PER_LEVEL, GemEconomy.levelUpGems(5, 8))
        // A non-increase (or corrupt data) never grants negative gems.
        assertEquals(0, GemEconomy.levelUpGems(8, 5))
    }

    @Test
    fun dailyLoginGems_onlyOnMilestoneDays() {
        for (day in 1 until GemEconomy.DAILY_LOGIN_INTERVAL) {
            assertEquals(0, GemEconomy.dailyLoginGems(day))
        }
        assertEquals(GemEconomy.DAILY_LOGIN_GEMS, GemEconomy.dailyLoginGems(GemEconomy.DAILY_LOGIN_INTERVAL))
        assertEquals(GemEconomy.DAILY_LOGIN_GEMS, GemEconomy.dailyLoginGems(GemEconomy.DAILY_LOGIN_INTERVAL * 2))
        assertEquals(0, GemEconomy.dailyLoginGems(0))
    }

    @Test
    fun gemBundles_arePositivelyPricedAndGrantSomething() {
        assertTrue(ShopCatalog.gemBundles.isNotEmpty())
        ShopCatalog.gemBundles.forEach { bundle ->
            assertTrue("bundle ${bundle.id} must cost gems", bundle.priceGems > 0)
            val grantsSomething = bundle.coins > 0 || bundle.xp > 0 || bundle.items.isNotEmpty()
            assertTrue("bundle ${bundle.id} must grant something", grantsSomething)
            bundle.items.forEach { (itemId, count) ->
                assertTrue("bundle ${bundle.id} count for $itemId must be positive", count > 0)
            }
        }
    }

    @Test
    fun gemBundleItems_referenceRealInventoryIds() {
        val knownIds = ShopCatalog.powerUps.map { it.id }.toSet() +
            ShopCatalog.boosts.map { it.id }.toSet() +
            ShopCatalog.boosters.map { it.id }.toSet()
        ShopCatalog.gemBundles.flatMap { it.items }.forEach { (itemId, _) ->
            assertTrue("gem bundle references unknown item id: $itemId", itemId in knownIds)
        }
    }

    @Test
    fun premiumCatalog_isGemPricedAndNotFree() {
        val premium = ShopCatalog.premiumAvatars + ShopCatalog.premiumThemes
        assertTrue(premium.isNotEmpty())
        ShopCatalog.premiumAvatars.forEach {
            assertTrue("premium avatar ${it.id} must be gem-priced", it.priceCoins == 0 && it.priceGems > 0)
            assertTrue("premium avatar ${it.id} must not be free", it.id !in ShopCatalog.freeItemIds)
        }
        ShopCatalog.premiumThemes.forEach {
            assertTrue("premium theme ${it.id} must be gem-priced", it.priceCoins == 0 && it.priceGems > 0)
            assertTrue("premium theme ${it.id} must not be free", it.id !in ShopCatalog.freeItemIds)
        }
    }
}
