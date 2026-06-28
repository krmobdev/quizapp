package com.rustam.quizapp

import com.rustam.quizapp.domain.PromoCodes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PromoCodesTest {

    @Test
    fun codes_areUnique() {
        val codes = PromoCodes.all.map { it.code }
        assertEquals(codes.size, codes.toSet().size)
        assertTrue("expected a healthy batch of codes", codes.size >= 20)
    }

    @Test
    fun everyCode_grantsSomethingAndIsUppercase() {
        PromoCodes.all.forEach { promo ->
            val r = promo.reward
            assertTrue("${promo.code} must grant something", r.coins > 0 || r.gems > 0 || r.xp > 0)
            assertTrue("${promo.code} amounts must be non-negative", r.coins >= 0 && r.gems >= 0 && r.xp >= 0)
            assertEquals("${promo.code} must be uppercase", promo.code.uppercase(), promo.code)
        }
    }

    @Test
    fun find_normalisesInput() {
        val sample = PromoCodes.all.first()
        assertSame(sample, PromoCodes.find(sample.code))
        assertSame(sample, PromoCodes.find("  ${sample.code.lowercase()}  "))
        assertNull(PromoCodes.find("definitely-not-a-code"))
        assertNull(PromoCodes.find(""))
    }
}
