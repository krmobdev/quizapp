package com.rustam.quizapp.data

import android.content.Context
import androidx.room.withTransaction
import com.rustam.quizapp.data.db.AppDatabase
import com.rustam.quizapp.data.db.InventoryEntity
import com.rustam.quizapp.data.db.OwnedItemEntity
import com.rustam.quizapp.data.db.PlayerEntity
import com.rustam.quizapp.data.db.RecentQuestionsEntity
import com.rustam.quizapp.domain.AnswerReward
import com.rustam.quizapp.domain.CharacterLevelCalculator
import com.rustam.quizapp.domain.CharacterStats
import com.rustam.quizapp.domain.EventProgressSnapshot
import com.rustam.quizapp.domain.QuizEvent
import com.rustam.quizapp.domain.QuizEventProgress
import com.rustam.quizapp.domain.QuizEventType
import com.rustam.quizapp.domain.QuizEvents
import com.rustam.quizapp.domain.QuizReward
import com.rustam.quizapp.domain.RewardCalculator
import com.rustam.quizapp.domain.ShopCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate

data class PlayerProfile(
    val name: String,
    val points: Int,
    val coins: Int,
    val avatarEmoji: String,
    val eventProgress: List<QuizEventProgress>,
    val stats: CharacterStats,
    val lifetimePoints: Int
)

/** Coins and the cosmetic items a player owns / has equipped, for the shop screen. */
data class ShopState(
    val coins: Int,
    val ownedItemIds: Set<String>,
    val equippedAvatarId: String,
    val equippedThemeId: String
)

class PlayerRepository(
    context: Context,
    private val questionRepository: QuestionRepository
) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.playerDao()

    fun observeProfile(): Flow<PlayerProfile> = dao.observePlayer().map { entity ->
        val player = entity ?: PlayerEntity()
        val categories = questionRepository.getCategories()
        PlayerProfile(
            name = player.name,
            points = player.points,
            coins = player.coins,
            avatarEmoji = ShopCatalog.avatarEmoji(player.avatarId ?: ShopCatalog.DEFAULT_AVATAR_ID),
            eventProgress = QuizEvents.activeEvents(categories, eventSnapshot(player)),
            stats = player.toStats(),
            lifetimePoints = player.lifetimePoints
        )
    }

    /** Coins and owned/equipped cosmetics, used by the shop screen. */
    fun observeShop(): Flow<ShopState> =
        combine(dao.observePlayer(), dao.observeOwnedIds()) { entity, owned ->
            val player = entity ?: PlayerEntity()
            ShopState(
                coins = player.coins,
                ownedItemIds = owned.toSet() + ShopCatalog.freeItemIds,
                equippedAvatarId = player.avatarId ?: ShopCatalog.DEFAULT_AVATAR_ID,
                equippedThemeId = player.themeId ?: ShopCatalog.DEFAULT_THEME_ID
            )
        }

    /** The accent theme id applied app-wide. Defaults to the free theme. */
    val equippedThemeId: Flow<String> =
        dao.observePlayer().map { it?.themeId ?: ShopCatalog.DEFAULT_THEME_ID }

    /**
     * Buys [itemId] for [price] coins if affordable and not already owned.
     * Returns `true` on a successful purchase. The check and deduction happen in a single
     * transaction so the balance check and write are atomic.
     */
    suspend fun purchase(itemId: String, price: Int): Boolean {
        var purchased = false
        db.withTransaction {
            val owned = dao.getOwnedIds().toSet() + ShopCatalog.freeItemIds
            val player = dao.getPlayer() ?: PlayerEntity()
            if (itemId !in owned && player.coins >= price) {
                dao.upsertPlayer(player.copy(coins = player.coins - price))
                dao.addOwned(OwnedItemEntity(itemId))
                purchased = true
            }
        }
        return purchased
    }

    /** Booster id -> how many of that booster the player currently holds. */
    fun observeInventory(): Flow<Map<String, Int>> = dao.observeInventory().map { rows ->
        val counts = rows.associate { it.itemId to it.count }
        ShopCatalog.boosters.associate { it.id to (counts[it.id] ?: 0) }
    }

    /**
     * Buys one [boosterId] for [price] coins if affordable, adding it to the backpack.
     * Boosters are consumables, so each purchase increments a stored count. Returns `true` on success.
     */
    suspend fun purchaseBooster(boosterId: String, price: Int): Boolean {
        var purchased = false
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            if (player.coins >= price) {
                dao.upsertPlayer(player.copy(coins = player.coins - price))
                val count = dao.getInventoryCount(boosterId) ?: 0
                dao.upsertInventory(InventoryEntity(boosterId, count + 1))
                purchased = true
            }
        }
        return purchased
    }

    /**
     * Consumes one [boosterId] from the backpack and grants its XP. The reward is added
     * to both the spendable XP (points) and the lifetime XP, so the player's level/rank
     * grows just like with earned quiz rewards. Returns `true` if a booster was activated.
     */
    suspend fun activateBooster(boosterId: String): Boolean {
        val booster = ShopCatalog.booster(boosterId) ?: return false
        var activated = false
        db.withTransaction {
            val count = dao.getInventoryCount(boosterId) ?: 0
            if (count > 0) {
                dao.upsertInventory(InventoryEntity(boosterId, count - 1))
                val player = dao.getPlayer() ?: PlayerEntity()
                dao.upsertPlayer(
                    player.copy(
                        points = player.points + booster.rewardPoints,
                        lifetimePoints = player.lifetimePoints + booster.rewardPoints
                    )
                )
                activated = true
            }
        }
        return activated
    }

    /** Power-up id -> how many of that power-up the player currently holds. */
    fun observePowerUps(): Flow<Map<String, Int>> = dao.observeInventory().map { rows ->
        val counts = rows.associate { it.itemId to it.count }
        ShopCatalog.powerUps.associate { it.id to (counts[it.id] ?: 0) }
    }

    /** Buys one power-up for [price] coins if affordable. Returns `true` on success. */
    suspend fun purchasePowerUp(powerUpId: String, price: Int): Boolean =
        purchaseBooster(powerUpId, price)

    /** Consumes one [powerUpId] from the backpack. Returns `true` if one was available. */
    suspend fun consumePowerUp(powerUpId: String): Boolean {
        var consumed = false
        db.withTransaction {
            val count = dao.getInventoryCount(powerUpId) ?: 0
            if (count > 0) {
                dao.upsertInventory(InventoryEntity(powerUpId, count - 1))
                consumed = true
            }
        }
        return consumed
    }

    suspend fun equipAvatar(avatarId: String) = updatePlayer { it.copy(avatarId = avatarId) }

    suspend fun equipTheme(themeId: String) = updatePlayer { it.copy(themeId = themeId) }

    suspend fun setPlayerName(name: String) {
        val trimmed = name.trim().take(MAX_NAME_LENGTH)
        if (trimmed.isEmpty()) return
        updatePlayer { it.copy(name = trimmed) }
    }

    /** Adds coins without touching points/XP (used for achievement and daily rewards). */
    suspend fun addCoins(amount: Int) {
        if (amount <= 0) return
        updatePlayer { it.copy(coins = it.coins + amount) }
    }

    /**
     * Atomically deducts [amount] coins if the player can afford it. Returns `true` on success.
     * Used for purchases whose item lives outside this store (e.g. the Streak Freeze).
     */
    suspend fun spendCoins(amount: Int): Boolean {
        if (amount <= 0) return false
        var spent = false
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            if (player.coins >= amount) {
                dao.upsertPlayer(player.copy(coins = player.coins - amount))
                spent = true
            }
        }
        return spent
    }

    suspend fun grantReward(reward: QuizReward) {
        updatePlayer {
            it.copy(
                points = it.points + reward.points,
                coins = it.coins + reward.coins,
                lifetimePoints = it.lifetimePoints + reward.points
            )
        }
    }

    val promoRedeemed: Flow<Boolean> = dao.observePlayer().map { it?.promoRedeemed ?: false }

    suspend fun redeemPromoCode(code: String): PromoRedeemResult {
        val normalized = code.trim().uppercase()
        if (normalized != PROMO_CODE) return PromoRedeemResult.INVALID_CODE
        var result = PromoRedeemResult.ALREADY_REDEEMED
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            if (!player.promoRedeemed) {
                dao.upsertPlayer(
                    player.copy(promoRedeemed = true, coins = player.coins + PROMO_REWARD_COINS)
                )
                result = PromoRedeemResult.SUCCESS
            }
        }
        return result
    }

    suspend fun eventProgressSnapshot(): EventProgressSnapshot =
        eventSnapshot(dao.getPlayer() ?: PlayerEntity())

    fun resolveActiveEvent(
        eventType: QuizEventType?,
        categoryId: String,
        difficulty: Difficulty?,
        snapshot: EventProgressSnapshot,
        day: LocalDate = LocalDate.now()
    ): QuizEvent? {
        val type = eventType ?: return null
        val completions = snapshot.completionsFor(type, day)
        val event = QuizEvents.eventFor(type, questionRepository.getCategories(), completions, day) ?: return null
        if (event.category.id != categoryId) return null
        if (event.difficulty != difficulty) return null
        if (completions >= event.maxCompletions) return null
        return event
    }

    suspend fun grantQuizReward(
        categoryId: String,
        difficulty: Difficulty?,
        answers: List<AnswerReward>,
        total: Int,
        eventType: QuizEventType?,
        allowEventBonus: Boolean
    ): QuizReward {
        val snapshot = eventProgressSnapshot()
        val activeEvent = if (allowEventBonus) {
            resolveActiveEvent(eventType, categoryId, difficulty, snapshot)
        } else {
            null
        }
        val baseReward = RewardCalculator.calculate(answers, total, activeEvent)

        val player = dao.getPlayer() ?: PlayerEntity()
        val stats = player.toStats()

        // Level scaling: the higher the player's level, the higher the base reward
        // (+10% per level above 1). Level is derived from lifetime XP.
        val lifetime = player.lifetimePoints
        val level = CharacterLevelCalculator.calculateLevel(lifetime)
        val levelMultiplier = CharacterLevelCalculator.rewardMultiplier(level)
        // Coins scale faster than XP past level 20 to reward high-level players.
        val coinLevelMultiplier = CharacterLevelCalculator.coinRewardMultiplier(level)
        val scaledBasePoints = (baseReward.points * levelMultiplier).toInt()
        val scaledBaseCoins = (baseReward.coins * coinLevelMultiplier).toInt()

        // Percentage bonuses (Strength/Intelligence) plus flat bonuses (Wisdom/Endurance).
        val xpBonus = (scaledBasePoints * (stats.xpBonusPercent / 100f)).toInt() + stats.flatXpBonus
        val coinBonus = (scaledBaseCoins * (stats.coinBonusPercent / 100f)).toInt() + stats.flatCoinBonus

        var pointsEarned = scaledBasePoints + xpBonus
        var coinsEarned = scaledBaseCoins + coinBonus

        // Luck (+ Charisma) roll for double rewards (Critical Success). Focus sharpens it.
        val doubleChance = stats.doubleRewardChancePercent + stats.critChanceBonusPercent
        val isCriticalSuccess = if (doubleChance > 0) (1..100).random() <= doubleChance else false

        if (isCriticalSuccess) {
            pointsEarned = (pointsEarned * stats.critMultiplier).toInt()
            coinsEarned = (coinsEarned * stats.critMultiplier).toInt()
        }

        val newLevel = CharacterLevelCalculator.calculateLevel(lifetime + pointsEarned)

        val finalReward = baseReward.copy(
            points = pointsEarned,
            coins = coinsEarned,
            isCriticalSuccess = isCriticalSuccess,
            critMultiplier = if (isCriticalSuccess) stats.critMultiplier else 1f,
            xpBonus = xpBonus,
            coinBonus = coinBonus,
            basePoints = baseReward.points,
            baseCoins = baseReward.coins,
            levelMultiplier = levelMultiplier,
            coinLevelMultiplier = coinLevelMultiplier,
            previousLevel = level,
            newLevel = newLevel
        )

        grantReward(finalReward)
        if (activeEvent != null) markEventCompleted(activeEvent.type)
        return finalReward
    }

    suspend fun upgradeStat(statName: String): Boolean {
        var upgraded = false
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            val currentVal = statValue(player, statName) ?: return@withTransaction
            val cost = 100 + currentVal * 25
            if (currentVal < MAX_STAT && player.points >= cost) {
                val updated = applyStat(
                    player.copy(points = player.points - cost),
                    statName,
                    currentVal + 1
                )
                dao.upsertPlayer(updated)
                upgraded = true
            }
        }
        return upgraded
    }

    private suspend fun markEventCompleted(type: QuizEventType) {
        val day = LocalDate.now()
        updatePlayer { player ->
            when (type) {
                QuizEventType.DAILY -> player.copy(dailyCompletedDay = QuizEvents.epochDay(day))
                QuizEventType.WEEKLY -> {
                    val week = QuizEvents.epochWeek(day)
                    player.copy(
                        weeklyEpoch = week,
                        weeklyCompletions = if (player.weeklyEpoch == week) player.weeklyCompletions + 1 else 1
                    )
                }
                QuizEventType.WEEKEND_BLITZ -> {
                    val weekend = QuizEvents.epochWeekend(day)
                    player.copy(
                        weekendEpoch = weekend,
                        weekendCompletions = if (player.weekendEpoch == weekend) player.weekendCompletions + 1 else 1
                    )
                }
                QuizEventType.MARATHON -> {
                    val epochDay = QuizEvents.epochDay(day)
                    player.copy(
                        marathonDay = epochDay,
                        marathonCompletions = if (player.marathonDay == epochDay) player.marathonCompletions + 1 else 1
                    )
                }
            }
        }
    }

    fun getRecentQuestions(categoryId: String): Flow<List<String>> =
        dao.observeRecent(categoryId).map { it.toIds() }

    suspend fun saveRecentQuestions(categoryId: String, questionIds: List<String>) {
        db.withTransaction {
            val current = dao.getRecent(categoryId).toIds()
            val ordered = (current - questionIds.toSet()) + questionIds
            dao.upsertRecent(
                RecentQuestionsEntity(categoryId, ordered.takeLast(MAX_RECENT).joinToString(","))
            )
        }
    }

    private suspend inline fun updatePlayer(crossinline transform: (PlayerEntity) -> PlayerEntity) {
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            dao.upsertPlayer(transform(player))
        }
    }

    private fun RecentQuestionsEntity?.toIds(): List<String> {
        val raw = this?.idsCsv.orEmpty()
        return if (raw.isEmpty()) emptyList() else raw.split(",")
    }

    private fun PlayerEntity.toStats(): CharacterStats = CharacterStats(
        strength = strength,
        intelligence = intelligence,
        agility = agility,
        luck = luck,
        wisdom = wisdom,
        endurance = endurance,
        focus = focus,
        charisma = charisma
    )

    private fun eventSnapshot(player: PlayerEntity): EventProgressSnapshot = EventProgressSnapshot(
        dailyCompletedDay = player.dailyCompletedDay,
        weeklyEpoch = player.weeklyEpoch,
        weeklyCompletions = player.weeklyCompletions,
        weekendEpoch = player.weekendEpoch,
        weekendCompletions = player.weekendCompletions,
        marathonDay = player.marathonDay,
        marathonCompletions = player.marathonCompletions
    )

    private fun statValue(player: PlayerEntity, statName: String): Int? = when (statName) {
        "strength" -> player.strength
        "intelligence" -> player.intelligence
        "agility" -> player.agility
        "luck" -> player.luck
        "wisdom" -> player.wisdom
        "endurance" -> player.endurance
        "focus" -> player.focus
        "charisma" -> player.charisma
        else -> null
    }

    private fun applyStat(player: PlayerEntity, statName: String, value: Int): PlayerEntity = when (statName) {
        "strength" -> player.copy(strength = value)
        "intelligence" -> player.copy(intelligence = value)
        "agility" -> player.copy(agility = value)
        "luck" -> player.copy(luck = value)
        "wisdom" -> player.copy(wisdom = value)
        "endurance" -> player.copy(endurance = value)
        "focus" -> player.copy(focus = value)
        "charisma" -> player.copy(charisma = value)
        else -> player
    }

    private companion object {
        const val MAX_NAME_LENGTH = 24
        const val MAX_STAT = 20
        const val MAX_RECENT = 200
        const val PROMO_CODE = "AAAAAA"
        const val PROMO_REWARD_COINS = 30000
    }
}

enum class PromoRedeemResult {
    SUCCESS,
    INVALID_CODE,
    ALREADY_REDEEMED
}
