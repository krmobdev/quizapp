package com.rustam.quizapp.domain

/**
 * Global economy tuning. All coin/XP prices and payouts use [scale] (60% of the original = −40%).
 */
object EconomyBalance {
    private const val NUMERATOR = 3
    private const val DENOMINATOR = 5

    fun scale(value: Int): Int {
        if (value <= 0) return value
        return (value * NUMERATOR / DENOMINATOR).coerceAtLeast(1)
    }
}