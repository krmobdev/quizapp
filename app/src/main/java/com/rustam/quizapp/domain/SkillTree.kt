package com.rustam.quizapp.domain

import androidx.annotation.StringRes
import com.rustam.quizapp.R

/** The kind of passive bonus a [SkillBranch] grants for each unlocked tier. */
enum class SkillBonusKind {
    XP_PERCENT,
    COIN_PERCENT,
    CRIT_CHANCE,
    EXTRA_TIME,
    FLAT_XP,
    FLAT_COINS
}

/**
 * A branch of the Mastery Tree. Each branch is a linear path of tiers the player buys in order,
 * spending BOTH free XP and coins. Every unlocked tier adds [perTier] of the branch's [kind] of
 * bonus, so a fully-invested branch is a large, permanent power spike — and a deep sink for both
 * currencies, which is the point.
 *
 * The per-tier cost escalates with the tier number (see [SkillTree.nextXpCost]/[SkillTree.nextCoinCost]).
 */
enum class SkillBranch(
    val id: String,
    val emoji: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descRes: Int,
    val kind: SkillBonusKind,
    val maxTier: Int,
    /** Bonus added per unlocked tier (percent, seconds, or flat amount depending on [kind]). */
    val perTier: Float,
    val xpCostBase: Int,
    val coinCostBase: Int
) {
    ERUDITION("erudition", "📚", R.string.skill_erudition, R.string.skill_erudition_desc,
        SkillBonusKind.XP_PERCENT, maxTier = 10, perTier = 4f, xpCostBase = 450, coinCostBase = 190),
    COMMERCE("commerce", "💰", R.string.skill_commerce, R.string.skill_commerce_desc,
        SkillBonusKind.COIN_PERCENT, maxTier = 10, perTier = 5f, xpCostBase = 450, coinCostBase = 190),
    FORTUNE("fortune", "🎲", R.string.skill_fortune, R.string.skill_fortune_desc,
        SkillBonusKind.CRIT_CHANCE, maxTier = 8, perTier = 3f, xpCostBase = 600, coinCostBase = 300),
    CHRONOS("chronos", "⏳", R.string.skill_chronos, R.string.skill_chronos_desc,
        SkillBonusKind.EXTRA_TIME, maxTier = 6, perTier = 1f, xpCostBase = 525, coinCostBase = 265),
    SAGE("sage", "🦉", R.string.skill_sage, R.string.skill_sage_desc,
        SkillBonusKind.FLAT_XP, maxTier = 8, perTier = 25f, xpCostBase = 375, coinCostBase = 225),
    RESILIENCE("resilience", "🛡️", R.string.skill_resilience, R.string.skill_resilience_desc,
        SkillBonusKind.FLAT_COINS, maxTier = 8, perTier = 10f, xpCostBase = 375, coinCostBase = 265);

    /** The aggregate bonus value from [tier] unlocked levels of this branch. */
    fun bonusAt(tier: Int): Float = tier.coerceIn(0, maxTier) * perTier
}

/**
 * The player's progress in the Mastery Tree: how many tiers are unlocked per branch.
 * Exposes the aggregated bonuses fed into the reward engine and the quiz timer.
 */
data class SkillTreeState(val tiers: Map<SkillBranch, Int> = emptyMap()) {

    fun tier(branch: SkillBranch): Int = (tiers[branch] ?: 0).coerceIn(0, branch.maxTier)

    val xpBonusPercent: Int get() = SkillBranch.ERUDITION.bonusAt(tier(SkillBranch.ERUDITION)).toInt()
    val coinBonusPercent: Int get() = SkillBranch.COMMERCE.bonusAt(tier(SkillBranch.COMMERCE)).toInt()
    val critChanceBonusPercent: Int get() = SkillBranch.FORTUNE.bonusAt(tier(SkillBranch.FORTUNE)).toInt()
    val extraTimeSeconds: Float get() = SkillBranch.CHRONOS.bonusAt(tier(SkillBranch.CHRONOS))
    val flatXpBonus: Int get() = SkillBranch.SAGE.bonusAt(tier(SkillBranch.SAGE)).toInt()
    val flatCoinBonus: Int get() = SkillBranch.RESILIENCE.bonusAt(tier(SkillBranch.RESILIENCE)).toInt()
}

/** Cost progression helpers for the Mastery Tree. */
object SkillTree {
    val branches: List<SkillBranch> = SkillBranch.entries

    fun branch(id: String): SkillBranch? = SkillBranch.entries.find { it.id == id }

    /** Free-XP cost to buy the next tier when the branch is currently at [currentTier]. */
    fun nextXpCost(branch: SkillBranch, currentTier: Int): Int =
        branch.xpCostBase * (currentTier + 1)

    /** Coin cost to buy the next tier when the branch is currently at [currentTier]. */
    fun nextCoinCost(branch: SkillBranch, currentTier: Int): Int =
        branch.coinCostBase * (currentTier + 1)
}
