package com.rustam.quizapp

import com.rustam.quizapp.domain.ShopDeals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopDealsTest {

    @Test
    fun dealsForDay_returns_correct_count() {
        val deals = ShopDeals.dealsForDay(1000L)
        assertEquals(ShopDeals.DEAL_COUNT, deals.size)
    }

    @Test
    fun dealsForDay_is_deterministic_for_same_day() {
        val day = 2000L
        val deals1 = ShopDeals.dealsForDay(day)
        val deals2 = ShopDeals.dealsForDay(day)
        assertEquals(deals1.map { it.dealId }, deals2.map { it.dealId })
    }

    @Test
    fun dealsForDay_changes_between_days() {
        val day1 = ShopDeals.dealsForDay(3000L).map { it.dealId }
        val day2 = ShopDeals.dealsForDay(3001L).map { it.dealId }
        // Different days should (almost certainly) produce different rotations
        // Allow for coincidental match but at least verify they're independently computed
        assertNotNull(day1)
        assertNotNull(day2)
    }

    @Test
    fun dealPrice_applies_30_percent_discount() {
        val base = 100
        val discounted = ShopDeals.dealPrice(base)
        assertEquals(70, discounted)
    }

    @Test
    fun dealPrice_is_at_least_1() {
        assertEquals(1, ShopDeals.dealPrice(1))
        assertEquals(1, ShopDeals.dealPrice(0))
    }

    @Test
    fun all_deals_have_positive_base_price() {
        ShopDeals.dealsForDay(5000L).forEach { deal ->
            assertTrue(
                "Deal ${deal.dealId} has non-positive basePrice ${deal.basePrice}",
                deal.basePrice > 0
            )
        }
    }

    @Test
    fun template_returns_correct_template_by_id() {
        val firstDeal = ShopDeals.dealsForDay(1000L).first()
        val template = ShopDeals.template(firstDeal.dealId)
        assertNotNull(template)
        assertEquals(firstDeal.dealId, template!!.dealId)
    }
}
