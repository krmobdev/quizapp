package com.rustam.quizapp.domain

import androidx.annotation.StringRes
import com.rustam.quizapp.R

/**
 * Passive talent tree — a deep XP-only sink with small incremental bonuses.
 *
 * Each [TalentNode] can be upgraded up to [MAX_RANK] times. Bonuses per rank are deliberately
 * below character stats (2% per point) and the Mastery Tree (3–5% per tier).
 *
 * Progress is stored as `nodeId:rank` pairs in [PlayerEntity.talentProgressCsv].
 */
enum class TalentBranch(
    val id: String,
    val emoji: String,
    @param:StringRes val titleRes: Int,
    val kind: SkillBonusKind,
    /** Bonus granted per rank of any node in this branch. */
    val perRank: Float,
    val nodeCount: Int = 10
) {
    INSIGHT("insight", "💡", R.string.talent_branch_insight, SkillBonusKind.XP_PERCENT, 0.25f),
    PROSPERITY("prosperity", "🪙", R.string.talent_branch_prosperity, SkillBonusKind.COIN_PERCENT, 0.25f),
    FORTUNE("fortune", "✨", R.string.talent_branch_fortune, SkillBonusKind.CRIT_CHANCE, 0.15f),
    CELERITY("celerity", "⏱️", R.string.talent_branch_celerity, SkillBonusKind.EXTRA_TIME, 0.08f),
    ACUMEN("acumen", "📘", R.string.talent_branch_acumen, SkillBonusKind.FLAT_XP, 1f),
    FORTITUDE("fortitude", "🔰", R.string.talent_branch_fortitude, SkillBonusKind.FLAT_COINS, 1f);

    fun nodeId(depth: Int): String = "${id}_$depth"
}

data class TalentNode(
    val branch: TalentBranch,
    val depth: Int,
    val maxRank: Int = PassiveTalentTree.MAX_RANK
) {
    val id: String get() = branch.nodeId(depth)

    /** Previous node in the same branch, or null for the branch entry. */
    val prerequisiteId: String? get() = if (depth == 0) null else branch.nodeId(depth - 1)
}

data class TalentTreeState(val ranks: Map<String, Int> = emptyMap()) {

    fun rank(nodeId: String): Int = (ranks[nodeId] ?: 0).coerceAtLeast(0)

    fun rank(node: TalentNode): Int = rank(node.id)

    val totalRanks: Int get() = ranks.values.sum()

    val xpBonusPercent: Float get() = branchBonus(TalentBranch.INSIGHT)
    val coinBonusPercent: Float get() = branchBonus(TalentBranch.PROSPERITY)
    val critChanceBonusPercent: Float get() = branchBonus(TalentBranch.FORTUNE)
    val extraTimeSeconds: Float get() = branchBonus(TalentBranch.CELERITY)
    val flatXpBonus: Int get() = branchBonus(TalentBranch.ACUMEN).toInt()
    val flatCoinBonus: Int get() = branchBonus(TalentBranch.FORTITUDE).toInt()

    private fun branchBonus(branch: TalentBranch): Float {
        var total = 0f
        for (depth in 0 until branch.nodeCount) {
            total += rank(branch.nodeId(depth)) * branch.perRank
        }
        return total
    }
}

object PassiveTalentTree {
    const val MAX_RANK = 8

    val branches: List<TalentBranch> = TalentBranch.entries

    val nodes: List<TalentNode> = branches.flatMap { branch ->
        (0 until branch.nodeCount).map { depth -> TalentNode(branch, depth) }
    }

    val totalMaxRanks: Int get() = nodes.size * MAX_RANK

    fun node(id: String): TalentNode? = nodes.find { it.id == id }

    fun parseProgress(csv: String): TalentTreeState {
        if (csv.isBlank()) return TalentTreeState()
        val ranks = csv.split(",")
            .mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size != 2) return@mapNotNull null
                val rank = parts[1].toIntOrNull() ?: return@mapNotNull null
                parts[0] to rank.coerceIn(0, MAX_RANK)
            }
            .toMap()
        return TalentTreeState(ranks)
    }

    fun serializeProgress(state: TalentTreeState): String =
        state.ranks
            .filter { it.value > 0 }
            .entries
            .sortedBy { it.key }
            .joinToString(",") { "${it.key}:${it.value}" }

    /** Free-XP cost for the next rank on [node] given [state]. */
    fun nextXpCost(node: TalentNode, state: TalentTreeState): Int {
        val completed = state.totalRanks
        return EconomyBalance.scale(60) + completed * EconomyBalance.scale(14) +
            node.depth * EconomyBalance.scale(18)
    }

    fun isUnlocked(node: TalentNode, state: TalentTreeState): Boolean {
        val prereq = node.prerequisiteId ?: return true
        return state.rank(prereq) >= 1
    }

    fun canUpgrade(node: TalentNode, state: TalentTreeState, freeXp: Int): Boolean {
        if (state.rank(node) >= node.maxRank) return false
        if (!isUnlocked(node, state)) return false
        return freeXp >= nextXpCost(node, state)
    }

    fun bonusDescriptionRes(kind: SkillBonusKind): Int = when (kind) {
        SkillBonusKind.XP_PERCENT -> R.string.talent_bonus_xp
        SkillBonusKind.COIN_PERCENT -> R.string.talent_bonus_coins
        SkillBonusKind.CRIT_CHANCE -> R.string.talent_bonus_crit
        SkillBonusKind.EXTRA_TIME -> R.string.talent_bonus_time
        SkillBonusKind.FLAT_XP -> R.string.talent_bonus_flat_xp
        SkillBonusKind.FLAT_COINS -> R.string.talent_bonus_flat_coins
    }
}