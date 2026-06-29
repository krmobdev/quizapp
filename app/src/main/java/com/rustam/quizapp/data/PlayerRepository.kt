package com.rustam.quizapp.data

import android.content.Context
import androidx.room.withTransaction
import com.rustam.quizapp.data.db.AppDatabase
import com.rustam.quizapp.data.db.InventoryEntity
import com.rustam.quizapp.data.db.OwnedItemEntity
import com.rustam.quizapp.data.db.PlayerEntity
import com.rustam.quizapp.data.db.RecentQuestionsEntity
import com.rustam.quizapp.data.db.RedeemedPromoEntity
import com.rustam.quizapp.data.db.ShopDealEntity
import com.rustam.quizapp.domain.DealKind
import com.rustam.quizapp.domain.DealTemplate
import com.rustam.quizapp.domain.SeasonPass
import com.rustam.quizapp.domain.SeasonReward
import com.rustam.quizapp.domain.SeasonRewardKind
import com.rustam.quizapp.domain.ShopDeals
import com.rustam.quizapp.domain.AnswerReward
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.domain.BoostType
import com.rustam.quizapp.domain.CharacterLevelCalculator
import com.rustam.quizapp.domain.GemEconomy
import com.rustam.quizapp.domain.LootBox
import com.rustam.quizapp.domain.LootResult
import com.rustam.quizapp.domain.MythicBox
import com.rustam.quizapp.domain.PackCurrency
import com.rustam.quizapp.domain.CharacterStats
import com.rustam.quizapp.domain.EventProgressSnapshot
import com.rustam.quizapp.domain.QuizEvent
import com.rustam.quizapp.domain.QuizEventProgress
import com.rustam.quizapp.domain.QuizEventType
import com.rustam.quizapp.domain.QuizEvents
import com.rustam.quizapp.domain.QuizReward
import com.rustam.quizapp.domain.RewardCalculator
import com.rustam.quizapp.domain.ShopCatalog
import com.rustam.quizapp.domain.PassiveTalentTree
import com.rustam.quizapp.domain.PromoCodes
import com.rustam.quizapp.domain.SkillBranch
import com.rustam.quizapp.domain.SkillTree
import com.rustam.quizapp.domain.SkillTreeState
import com.rustam.quizapp.domain.TalentTreeState
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
    val skillTree: SkillTreeState,
    val talentTree: TalentTreeState,
    val lifetimePoints: Int,
    val bankedLifetimePoints: Int = 0,
    val lifetimeCoins: Int = 0,
    val equippedTitleId: String? = null,
    /** Remaining quizzes the active temporary boosts apply to (0 = inactive). */
    val coinBoostQuizzesLeft: Int = 0,
    val xpBoostQuizzesLeft: Int = 0,
    /** Rare premium currency. */
    val gems: Int = 0,
    /** Season-track state for the current period (reset automatically when the season rolls over). */
    val seasonXp: Int = 0,
    val seasonClaimedMask: Long = 0L,
    val seasonDaysLeft: Int = SeasonPass.LENGTH_DAYS
)

/** Coins and the cosmetic items a player owns / has equipped, for the shop screen. */
data class ShopState(
    val coins: Int,
    val gems: Int,
    val ownedItemIds: Set<String>,
    val equippedAvatarId: String,
    val equippedThemeId: String,
    val equippedTitleId: String? = null,
    val coinBoostQuizzesLeft: Int = 0,
    val xpBoostQuizzesLeft: Int = 0
)

/** One of today's discounted shop deals, with its current daily purchase count. */
data class ShopDealState(
    val template: DealTemplate,
    val dealPrice: Int,
    val purchasedToday: Int
) {
    val soldOut: Boolean get() = purchasedToday >= ShopDeals.MAX_PER_DAY
}

class PlayerRepository(
    context: Context,
    private val questionRepository: QuestionRepository
) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.playerDao()
    private val dealDao = db.shopDealDao()
    private val promoDao = db.promoDao()

    fun observeProfile(): Flow<PlayerProfile> = dao.observePlayer().map { entity ->
        val player = entity ?: PlayerEntity()
        val categories = questionRepository.getCategories()
        val today = LocalDate.now().toEpochDay()
        val currentSeason = SeasonPass.currentSeasonId(today)
        val seasonActive = player.seasonId == currentSeason
        PlayerProfile(
            name = player.name,
            points = player.points,
            coins = player.coins,
            avatarEmoji = ShopCatalog.avatarEmoji(player.avatarId ?: ShopCatalog.DEFAULT_AVATAR_ID),
            eventProgress = QuizEvents.activeEvents(categories, eventSnapshot(player)),
            stats = player.toStats(),
            skillTree = player.toSkillTree(),
            talentTree = player.toTalentTree(),
            lifetimePoints = player.lifetimePoints,
            bankedLifetimePoints = player.bankedLifetimePoints,
            lifetimeCoins = player.lifetimeCoins,
            equippedTitleId = player.equippedTitleId,
            coinBoostQuizzesLeft = player.coinBoostQuizzesLeft,
            xpBoostQuizzesLeft = player.xpBoostQuizzesLeft,
            gems = player.gems,
            seasonXp = if (seasonActive) player.seasonXp else 0,
            seasonClaimedMask = if (seasonActive) player.seasonClaimedMask else 0L,
            seasonDaysLeft = SeasonPass.daysLeft(today)
        )
    }

    /** Coins and owned/equipped cosmetics, used by the shop screen. */
    fun observeShop(): Flow<ShopState> =
        combine(dao.observePlayer(), dao.observeOwnedIds()) { entity, owned ->
            val player = entity ?: PlayerEntity()
            ShopState(
                coins = player.coins,
                gems = player.gems,
                ownedItemIds = owned.toSet() + ShopCatalog.freeItemIds,
                equippedAvatarId = player.avatarId ?: ShopCatalog.DEFAULT_AVATAR_ID,
                equippedThemeId = player.themeId ?: ShopCatalog.DEFAULT_THEME_ID,
                equippedTitleId = player.equippedTitleId,
                coinBoostQuizzesLeft = player.coinBoostQuizzesLeft,
                xpBoostQuizzesLeft = player.xpBoostQuizzesLeft
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
                dao.upsertPlayer(player.withEarnedXp(booster.rewardPoints))
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

    /** Buys a power-up (or bundle) for [price] coins if affordable. Returns `true` on success. */
    suspend fun purchasePowerUp(powerUpId: String, price: Int): Boolean {
        val item = ShopCatalog.powerUp(powerUpId) ?: return false
        var purchased = false
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            if (player.coins >= price) {
                dao.upsertPlayer(player.copy(coins = player.coins - price))
                val count = dao.getInventoryCount(powerUpId) ?: 0
                dao.upsertInventory(InventoryEntity(powerUpId, count + item.packSize))
                purchased = true
            }
        }
        return purchased
    }

    /** Boost id -> how many of that temporary boost the player currently holds in the backpack. */
    fun observeBoosts(): Flow<Map<String, Int>> = dao.observeInventory().map { rows ->
        val counts = rows.associate { it.itemId to it.count }
        ShopCatalog.boosts.associate { it.id to (counts[it.id] ?: 0) }
    }

    /** Buys one temporary boost for [price] coins if affordable, adding it to the backpack. */
    suspend fun purchaseBoost(boostId: String, price: Int): Boolean = purchaseBooster(boostId, price)

    /**
     * Activates one [boostId] from the backpack: consumes it and adds its charges to the matching
     * boost counter so the next finished quizzes get the doubled reward. Returns `true` on success.
     */
    suspend fun activateBoost(boostId: String): Boolean {
        val boost = ShopCatalog.boost(boostId) ?: return false
        var activated = false
        db.withTransaction {
            val count = dao.getInventoryCount(boostId) ?: 0
            if (count > 0) {
                dao.upsertInventory(InventoryEntity(boostId, count - 1))
                val player = dao.getPlayer() ?: PlayerEntity()
                val updated = when (boost.type) {
                    BoostType.COINS ->
                        player.copy(coinBoostQuizzesLeft = player.coinBoostQuizzesLeft + boost.quizzes)
                    BoostType.XP ->
                        player.copy(xpBoostQuizzesLeft = player.xpBoostQuizzesLeft + boost.quizzes)
                }
                dao.upsertPlayer(updated)
                activated = true
            }
        }
        return activated
    }

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

    /** Equips [titleId] (must already be owned), or pass `null` to show no title. */
    suspend fun equipTitle(titleId: String?) = updatePlayer { it.copy(equippedTitleId = titleId) }

    suspend fun setPlayerName(name: String) {
        val trimmed = name.trim().take(MAX_NAME_LENGTH)
        if (trimmed.isEmpty()) return
        updatePlayer { it.copy(name = trimmed) }
    }

    /** Current player level, derived from lifetime XP. Usable from any coroutine context. */
    suspend fun getPlayerLevel(): Int {
        val player = dao.getPlayer() ?: PlayerEntity()
        return CharacterLevelCalculator.calculateLevel(player.lifetimePoints, player.bankedLifetimePoints)
    }

    /** Adds coins without touching points/XP (used for achievement and daily rewards). */
    suspend fun addCoins(amount: Int) {
        if (amount <= 0) return
        updatePlayer { it.copy(coins = it.coins + amount, lifetimeCoins = it.lifetimeCoins + amount) }
    }

    /** Adds rare gems (achievements, events, season rewards). */
    suspend fun addGems(amount: Int) {
        if (amount <= 0) return
        updatePlayer { it.copy(gems = it.gems + amount) }
    }

    /**
     * Buys a premium cosmetic [itemId] for [priceGems] gems if affordable and not already owned.
     * Returns `true` on success. Mirrors [purchase] but spends gems instead of coins.
     */
    suspend fun purchaseWithGems(itemId: String, priceGems: Int): Boolean {
        var purchased = false
        db.withTransaction {
            val owned = dao.getOwnedIds().toSet() + ShopCatalog.freeItemIds
            val player = dao.getPlayer() ?: PlayerEntity()
            if (itemId !in owned && player.gems >= priceGems) {
                dao.upsertPlayer(player.copy(gems = player.gems - priceGems))
                dao.addOwned(OwnedItemEntity(itemId))
                purchased = true
            }
        }
        return purchased
    }

    /**
     * Adds free + lifetime XP without touching coins (used for daily-quest XP rewards and other
     * non-quiz XP grants). Mirrors how booster activation grows the player's level.
     */
    suspend fun addXp(amount: Int) {
        if (amount <= 0) return
        updatePlayer { it.withEarnedXp(amount) }
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

    /** Atomically deducts [amount] spendable XP (points) if affordable. Returns `true` on success. */
    suspend fun spendXp(amount: Int): Boolean {
        if (amount <= 0) return false
        var spent = false
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            if (player.points >= amount) {
                dao.upsertPlayer(player.copy(points = player.points - amount))
                spent = true
            }
        }
        return spent
    }

    /** Atomically deducts [amount] gems if affordable. Returns `true` on success. */
    suspend fun spendGems(amount: Int): Boolean {
        if (amount <= 0) return false
        var spent = false
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            if (player.gems >= amount) {
                dao.upsertPlayer(player.copy(gems = player.gems - amount))
                spent = true
            }
        }
        return spent
    }

    /** Pays a Millionaire pack buy-in in [currency]; returns `true` if the player could afford it. */
    suspend fun payPackEntry(currency: PackCurrency, amount: Int): Boolean = when (currency) {
        PackCurrency.XP -> spendXp(amount)
        PackCurrency.COINS -> spendCoins(amount)
        PackCurrency.GEMS -> spendGems(amount)
    }

    /** Awards a Millionaire prize in [currency] (XP also grows lifetime XP/level). */
    suspend fun awardPackPrize(currency: PackCurrency, amount: Int) {
        if (amount <= 0) return
        when (currency) {
            PackCurrency.XP -> addXp(amount)
            PackCurrency.COINS -> addCoins(amount)
            PackCurrency.GEMS -> addGems(amount)
        }
    }

    /**
     * Opens one [LootBox] if the player can afford it: deducts the price, rolls a weighted reward
     * and grants it, all in a single transaction. Cosmetic rolls only pick items the player does
     * not own yet; if none remain the roll falls through to XP or coins. Returns the won reward,
     * or `null` if the player could not afford a chest.
     */
    suspend fun openLootBox(): LootResult? {
        var result: LootResult? = null
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            if (player.coins < LootBox.PRICE) return@withTransaction

            val owned = dao.getOwnedIds().toSet() + ShopCatalog.freeItemIds
            val unownedAvatar = ShopCatalog.avatars
                .filter { it.priceCoins > 0 && it.id !in owned }.randomOrNull()
            val unownedTitle = ShopCatalog.titles.filter { it.id !in owned }.randomOrNull()
            val roll = (1..100).random()

            var updated = player.copy(coins = player.coins - LootBox.PRICE)
            result = when {
                roll <= LootBox.AVATAR_MAX_ROLL && unownedAvatar != null -> {
                    dao.addOwned(OwnedItemEntity(unownedAvatar.id))
                    LootResult.Avatar(unownedAvatar)
                }
                roll <= LootBox.TITLE_MAX_ROLL && unownedTitle != null -> {
                    dao.addOwned(OwnedItemEntity(unownedTitle.id))
                    LootResult.Title(unownedTitle)
                }
                roll <= LootBox.XP_MAX_ROLL -> {
                    val xp = LootBox.weightedPick(LootBox.xpPayouts)
                    updated = updated.withEarnedXp(xp)
                    LootResult.Xp(xp)
                }
                else -> {
                    val coins = LootBox.weightedPick(LootBox.coinPayouts)
                    updated = updated.copy(
                        coins = updated.coins + coins,
                        lifetimeCoins = updated.lifetimeCoins + coins
                    )
                    LootResult.Coins(coins)
                }
            }
            dao.upsertPlayer(updated)
        }
        return result
    }

    /**
     * Opens one [MythicBox] if the player can afford its gem price: deducts gems, then rolls an
     * unowned cosmetic across every catalogue (avatars, titles and themes, including premium ones),
     * falling through to a large coin or XP payout when nothing is left to win. All in one
     * transaction. Returns the won reward, or `null` if the player could not afford a chest.
     */
    suspend fun openMythicChest(): LootResult? {
        var result: LootResult? = null
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            if (player.gems < MythicBox.PRICE_GEMS) return@withTransaction

            val owned = dao.getOwnedIds().toSet() + ShopCatalog.freeItemIds
            val unownedAvatar = (ShopCatalog.avatars + ShopCatalog.premiumAvatars)
                .filter { (it.priceCoins > 0 || it.priceGems > 0) && it.id !in owned }.randomOrNull()
            val unownedTitle = (ShopCatalog.titles + ShopCatalog.premiumTitles)
                .filter { it.id !in owned }.randomOrNull()
            val unownedTheme = (ShopCatalog.themes + ShopCatalog.premiumThemes)
                .filter { (it.priceCoins > 0 || it.priceGems > 0) && it.id !in owned }.randomOrNull()
            val cosmetics: List<LootResult> = listOfNotNull(
                unownedAvatar?.let { LootResult.Avatar(it) },
                unownedTitle?.let { LootResult.Title(it) },
                unownedTheme?.let { LootResult.Theme(it) }
            )
            val roll = (1..100).random()

            var updated = player.copy(gems = player.gems - MythicBox.PRICE_GEMS)
            result = when {
                roll <= MythicBox.COSMETIC_MAX_ROLL && cosmetics.isNotEmpty() -> {
                    val won = cosmetics.random()
                    val itemId = when (won) {
                        is LootResult.Avatar -> won.item.id
                        is LootResult.Title -> won.item.id
                        is LootResult.Theme -> won.item.id
                        else -> null
                    }
                    itemId?.let { dao.addOwned(OwnedItemEntity(it)) }
                    won
                }
                roll <= MythicBox.XP_MAX_ROLL -> {
                    val xp = LootBox.weightedPick(MythicBox.xpPayouts)
                    updated = updated.withEarnedXp(xp)
                    LootResult.Xp(xp)
                }
                else -> {
                    val coins = LootBox.weightedPick(MythicBox.coinPayouts)
                    updated = updated.copy(
                        coins = updated.coins + coins,
                        lifetimeCoins = updated.lifetimeCoins + coins
                    )
                    LootResult.Coins(coins)
                }
            }
            dao.upsertPlayer(updated)
        }
        return result
    }

    /**
     * Buys a [com.rustam.quizapp.domain.GemBundle] for its gem price if affordable, atomically
     * granting its coins, free XP and/or inventory items. Powers both the gem→coin exchange and the
     * premium consumable packs. Returns `true` on success.
     */
    suspend fun purchaseGemBundle(bundleId: String): Boolean {
        val bundle = ShopCatalog.gemBundle(bundleId) ?: return false
        var purchased = false
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            if (player.gems < bundle.priceGems) return@withTransaction
            var updated = player.copy(gems = player.gems - bundle.priceGems)
            if (bundle.coins > 0) {
                updated = updated.copy(
                    coins = updated.coins + bundle.coins,
                    lifetimeCoins = updated.lifetimeCoins + bundle.coins
                )
            }
            if (bundle.xp > 0) updated = updated.withEarnedXp(bundle.xp)
            dao.upsertPlayer(updated)
            bundle.items.forEach { (itemId, count) ->
                val current = dao.getInventoryCount(itemId) ?: 0
                dao.upsertInventory(InventoryEntity(itemId, current + count))
            }
            purchased = true
        }
        return purchased
    }

    /**
     * Redeems a one-time promo [code] (see [PromoCodes]). Unknown codes return [PromoRedeemResult.Invalid];
     * codes already claimed on this device return [PromoRedeemResult.AlreadyRedeemed]. On success the
     * reward (coins / gems / XP) is granted atomically and the code is recorded so it can't be reused.
     */
    suspend fun redeemPromoCode(code: String): PromoRedeemResult {
        val promo = PromoCodes.find(code) ?: return PromoRedeemResult.Invalid
        var result: PromoRedeemResult = PromoRedeemResult.AlreadyRedeemed
        db.withTransaction {
            // Infinite codes bypass the already-redeemed gate and are never recorded.
            if (!promo.infinite) {
                if (promo.code in promoDao.getRedeemed().toSet()) return@withTransaction
                promoDao.insert(RedeemedPromoEntity(promo.code))
            }
            val player = dao.getPlayer() ?: PlayerEntity()
            val reward = promo.reward
            var updated = player
            if (reward.coins > 0) {
                updated = updated.copy(
                    coins = updated.coins + reward.coins,
                    lifetimeCoins = updated.lifetimeCoins + reward.coins
                )
            }
            if (reward.gems > 0) updated = updated.copy(gems = updated.gems + reward.gems)
            if (reward.xp > 0) updated = updated.withEarnedXp(reward.xp)
            dao.upsertPlayer(updated)
            result = PromoRedeemResult.Success(reward.coins, reward.gems, reward.xp)
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
        val skills = player.toSkillTree()
        val talents = player.toTalentTree()

        // Level scaling: higher level → higher base reward (active + hidden banked lifetime XP).
        val lifetime = player.lifetimePoints
        val banked = player.bankedLifetimePoints
        val level = CharacterLevelCalculator.calculateLevel(lifetime, banked)
        val levelMultiplier = CharacterLevelCalculator.rewardMultiplier(level)
        // Coins scale faster than XP past level 40.
        val coinLevelMultiplier = CharacterLevelCalculator.coinRewardMultiplier(level)
        val scaledBasePoints = (baseReward.points * levelMultiplier).toInt()
        val scaledBaseCoins = (baseReward.coins * coinLevelMultiplier).toInt()

        // Percentage bonuses (Strength/Insight/Intelligence + Erudition/Commerce skills) plus flat
        // bonuses (Wisdom/Endurance + Sage/Resilience skills) and the per-correct channels
        // (Knowledge/Wealth), which scale with how many answers were correct this run.
        // Flat bonuses are multiplied by the level multiplier so they grow meaningfully over time,
        // just like the base reward — without this they'd stay constant and feel irrelevant at high level.
        val correctCount = answers.count { it.isCorrect }
        val xpBonusPercent = stats.xpBonusPercent + skills.xpBonusPercent + talents.xpBonusPercent
        val coinBonusPercent = stats.coinBonusPercent + skills.coinBonusPercent + talents.coinBonusPercent
        val rawFlatXpBonus = stats.flatXpBonus + skills.flatXpBonus + talents.flatXpBonus +
            stats.flatXpPerCorrect * correctCount
        val rawFlatCoinBonus = stats.flatCoinBonus + skills.flatCoinBonus + talents.flatCoinBonus +
            stats.flatCoinPerCorrect * correctCount
        val flatXpBonus = (rawFlatXpBonus * levelMultiplier).toInt()
        val flatCoinBonus = (rawFlatCoinBonus * coinLevelMultiplier).toInt()
        val xpBonus = (scaledBasePoints * (xpBonusPercent / 100f)).toInt() + flatXpBonus
        val coinBonus = (scaledBaseCoins * (coinBonusPercent / 100f)).toInt() + flatCoinBonus

        var pointsEarned = scaledBasePoints + xpBonus
        var coinsEarned = scaledBaseCoins + coinBonus

        // Luck (+ Charisma + Precision + Fortune skill/talents) roll for double rewards (Critical
        // Success). Capped so a fully-invested build still gambles rather than always critting.
        val doubleChance = (stats.doubleRewardChancePercent + stats.critChanceBonusPercent +
            skills.critChanceBonusPercent + talents.critChanceBonusPercent)
            .coerceAtMost(CharacterLevelCalculator.MAX_CRIT_CHANCE_PERCENT)
        val isCriticalSuccess = if (doubleChance > 0f) (1..100).random() <= doubleChance else false

        if (isCriticalSuccess) {
            pointsEarned = (pointsEarned * stats.critMultiplier).toInt()
            coinsEarned = (coinsEarned * stats.critMultiplier).toInt()
        }

        // Temporary 2x boosts apply only to real quizzes.
        val xpBoosted = allowEventBonus && player.xpBoostQuizzesLeft > 0
        val coinBoosted = allowEventBonus && player.coinBoostQuizzesLeft > 0
        if (xpBoosted) pointsEarned *= 2
        if (coinBoosted) coinsEarned *= 2

        val (newLifetime, newBanked) = CharacterLevelCalculator.distributeLifetimeXp(
            lifetime, banked, pointsEarned
        )
        val newLevel = CharacterLevelCalculator.calculateLevel(newLifetime, newBanked)

        // Gems (💎) woven into the core loop: a bonus for a perfect run plus a payout per level
        // gained. Only real quizzes (not previews) earn the perfect-quiz bonus.
        val perfectQuizGems = if (allowEventBonus && total > 0 && correctCount == total) {
            GemEconomy.PERFECT_QUIZ_GEMS
        } else {
            0
        }
        val gemsEarned = perfectQuizGems + GemEconomy.levelUpGems(level, newLevel)

        val finalReward = baseReward.copy(
            points = pointsEarned,
            coins = coinsEarned,
            isCriticalSuccess = isCriticalSuccess,
            critMultiplier = if (isCriticalSuccess) stats.critMultiplier else 1f,
            xpBonus = xpBonus,
            coinBonus = coinBonus,
            xpBoosted = xpBoosted,
            coinBoosted = coinBoosted,
            basePoints = baseReward.points,
            baseCoins = baseReward.coins,
            levelMultiplier = levelMultiplier,
            coinLevelMultiplier = coinLevelMultiplier,
            previousLevel = level,
            newLevel = newLevel,
            gemsEarned = gemsEarned
        )

        // Credit the reward, advance the season track, and spend one charge per applied boost.
        val today = LocalDate.now().toEpochDay()
        val currentSeason = SeasonPass.currentSeasonId(today)
        val seasonGain = SeasonPass.xpForQuiz(correctCount)
        updatePlayer {
            val seasonActive = it.seasonId == currentSeason
            val baseSeasonXp = if (seasonActive) it.seasonXp else 0
            val baseSeasonMask = if (seasonActive) it.seasonClaimedMask else 0L
            it.withEarnedXp(pointsEarned).copy(
                coins = it.coins + coinsEarned,
                lifetimeCoins = it.lifetimeCoins + coinsEarned,
                gems = it.gems + gemsEarned,
                coinBoostQuizzesLeft = if (coinBoosted) it.coinBoostQuizzesLeft - 1 else it.coinBoostQuizzesLeft,
                xpBoostQuizzesLeft = if (xpBoosted) it.xpBoostQuizzesLeft - 1 else it.xpBoostQuizzesLeft,
                seasonId = currentSeason,
                seasonXp = baseSeasonXp + seasonGain,
                seasonClaimedMask = baseSeasonMask
            )
        }
        if (activeEvent != null) {
            markEventCompleted(activeEvent.type)
            addGems(EVENT_GEM_REWARD)
        }
        return finalReward
    }

    /**
     * Claims the season-track reward at [level] if it has been reached and not yet claimed, granting
     * coins / gems / XP / a booster accordingly. Returns the granted reward, or `null` if not claimable.
     */
    suspend fun claimSeasonReward(level: Int): SeasonReward? {
        val today = LocalDate.now().toEpochDay()
        val currentSeason = SeasonPass.currentSeasonId(today)
        var granted: SeasonReward? = null
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            val seasonActive = player.seasonId == currentSeason
            val seasonXp = if (seasonActive) player.seasonXp else 0
            val mask = if (seasonActive) player.seasonClaimedMask else 0L
            if (!SeasonPass.canClaim(level, seasonXp, mask)) return@withTransaction
            val reward = SeasonPass.reward(level)
            var updated = player.copy(
                seasonId = currentSeason,
                seasonXp = seasonXp,
                seasonClaimedMask = SeasonPass.withClaimed(mask, level)
            )
            when (reward.kind) {
                SeasonRewardKind.COINS -> updated = updated.copy(
                    coins = updated.coins + reward.amount,
                    lifetimeCoins = updated.lifetimeCoins + reward.amount
                )
                SeasonRewardKind.GEMS -> updated = updated.copy(gems = updated.gems + reward.amount)
                SeasonRewardKind.XP -> updated = updated.withEarnedXp(reward.amount)
                SeasonRewardKind.BOOSTER -> reward.itemId?.let { id ->
                    val count = dao.getInventoryCount(id) ?: 0
                    dao.upsertInventory(InventoryEntity(id, count + reward.amount))
                }
            }
            dao.upsertPlayer(updated)
            granted = reward
        }
        return granted
    }

    /** Today's discounted deals, with how many times each has been bought today (for the daily cap). */
    fun observeDeals(): Flow<List<ShopDealState>> = dealDao.observe().map { entity ->
        val today = LocalDate.now().toEpochDay()
        val purchases = if (entity?.day == today) parseDealPurchases(entity.purchasesCsv) else emptyMap()
        ShopDeals.dealsForDay(today).map { template ->
            ShopDealState(
                template = template,
                dealPrice = ShopDeals.dealPrice(template.basePrice),
                purchasedToday = purchases[template.dealId] ?: 0
            )
        }
    }

    /**
     * Buys one unit of today's deal [dealId] at its discounted price if affordable and under the
     * daily cap, granting the consumable. Returns `true` on success.
     */
    suspend fun purchaseDeal(dealId: String): Boolean {
        val today = LocalDate.now().toEpochDay()
        val template = ShopDeals.dealsForDay(today).find { it.dealId == dealId } ?: return false
        var purchased = false
        db.withTransaction {
            val deal = dealDao.get()
            val purchases = if (deal?.day == today) {
                parseDealPurchases(deal.purchasesCsv).toMutableMap()
            } else {
                mutableMapOf()
            }
            val boughtToday = purchases[dealId] ?: 0
            if (boughtToday >= ShopDeals.MAX_PER_DAY) return@withTransaction

            val price = ShopDeals.dealPrice(template.basePrice)
            val player = dao.getPlayer() ?: PlayerEntity()
            if (player.coins < price) return@withTransaction
            dao.upsertPlayer(player.copy(coins = player.coins - price))
            grantDealItem(template)

            purchases[dealId] = boughtToday + 1
            dealDao.upsert(ShopDealEntity(day = today, purchasesCsv = serializeDealPurchases(purchases)))
            purchased = true
        }
        return purchased
    }

    private suspend fun grantDealItem(template: DealTemplate) {
        val addCount = when (template.kind) {
            DealKind.POWERUP -> ShopCatalog.powerUp(template.dealId)?.packSize ?: 1
            else -> 1
        }
        val count = dao.getInventoryCount(template.dealId) ?: 0
        dao.upsertInventory(InventoryEntity(template.dealId, count + addCount))
    }

    private fun parseDealPurchases(csv: String): Map<String, Int> {
        if (csv.isBlank()) return emptyMap()
        return csv.split(",").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size != 2) return@mapNotNull null
            val count = parts[1].toIntOrNull() ?: return@mapNotNull null
            parts[0] to count
        }.toMap()
    }

    private fun serializeDealPurchases(purchases: Map<String, Int>): String =
        purchases.filter { it.value > 0 }.entries.joinToString(",") { "${it.key}:${it.value}" }

    suspend fun upgradeStat(statName: String): Boolean {
        var upgraded = false
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            val currentVal = statValue(player, statName) ?: return@withTransaction
            val cost = CharacterLevelCalculator.statUpgradeCost(currentVal)
            if (currentVal < CharacterLevelCalculator.MAX_STAT && player.points >= cost) {
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

    /**
     * Buys the next tier of a Mastery Tree [branchId] if the player can afford both the free-XP
     * and coin cost and the branch is not maxed. Cost and deduction are atomic. Returns `true` on success.
     */
    /**
     * Upgrades one rank of a passive talent [nodeId] if prerequisites are met and the player
     * can afford the free-XP cost. Returns `true` on success.
     */
    suspend fun upgradeTalent(nodeId: String): Boolean {
        val node = PassiveTalentTree.node(nodeId) ?: return false
        var upgraded = false
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            val state = player.toTalentTree()
            if (!PassiveTalentTree.canUpgrade(node, state, player.points)) return@withTransaction
            val cost = PassiveTalentTree.nextXpCost(node, state)
            val newRanks = state.ranks.toMutableMap()
            newRanks[node.id] = state.rank(node) + 1
            val updated = player.copy(
                points = player.points - cost,
                talentProgressCsv = PassiveTalentTree.serializeProgress(TalentTreeState(newRanks))
            )
            dao.upsertPlayer(updated)
            upgraded = true
        }
        return upgraded
    }

    suspend fun upgradeSkill(branchId: String): Boolean {
        val branch = SkillTree.branch(branchId) ?: return false
        var upgraded = false
        db.withTransaction {
            val player = dao.getPlayer() ?: PlayerEntity()
            val tier = skillTier(player, branch)
            if (tier >= branch.maxTier) return@withTransaction
            val xpCost = SkillTree.nextXpCost(branch, tier)
            val coinCost = SkillTree.nextCoinCost(branch, tier)
            if (player.points >= xpCost && player.coins >= coinCost) {
                val updated = applySkill(
                    player.copy(points = player.points - xpCost, coins = player.coins - coinCost),
                    branch,
                    tier + 1
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

    private fun PlayerEntity.toTalentTree(): TalentTreeState =
        PassiveTalentTree.parseProgress(talentProgressCsv)

    private fun PlayerEntity.toSkillTree(): SkillTreeState = SkillTreeState(
        mapOf(
            SkillBranch.ERUDITION to skillErudition,
            SkillBranch.COMMERCE to skillCommerce,
            SkillBranch.FORTUNE to skillFortune,
            SkillBranch.CHRONOS to skillChronos,
            SkillBranch.SAGE to skillSage,
            SkillBranch.RESILIENCE to skillResilience
        )
    )

    private fun skillTier(player: PlayerEntity, branch: SkillBranch): Int = when (branch) {
        SkillBranch.ERUDITION -> player.skillErudition
        SkillBranch.COMMERCE -> player.skillCommerce
        SkillBranch.FORTUNE -> player.skillFortune
        SkillBranch.CHRONOS -> player.skillChronos
        SkillBranch.SAGE -> player.skillSage
        SkillBranch.RESILIENCE -> player.skillResilience
    }

    private fun applySkill(player: PlayerEntity, branch: SkillBranch, tier: Int): PlayerEntity = when (branch) {
        SkillBranch.ERUDITION -> player.copy(skillErudition = tier)
        SkillBranch.COMMERCE -> player.copy(skillCommerce = tier)
        SkillBranch.FORTUNE -> player.copy(skillFortune = tier)
        SkillBranch.CHRONOS -> player.copy(skillChronos = tier)
        SkillBranch.SAGE -> player.copy(skillSage = tier)
        SkillBranch.RESILIENCE -> player.copy(skillResilience = tier)
    }

    private fun PlayerEntity.toStats(): CharacterStats = CharacterStats(
        strength = strength,
        intelligence = intelligence,
        agility = agility,
        luck = luck,
        wisdom = wisdom,
        endurance = endurance,
        focus = focus,
        charisma = charisma,
        knowledge = knowledge,
        wealth = wealth,
        precision = precision,
        insight = insight
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
        "knowledge" -> player.knowledge
        "wealth" -> player.wealth
        "precision" -> player.precision
        "insight" -> player.insight
        else -> null
    }

    /** Credits spendable XP and splits lifetime XP between the active cap and the hidden bank. */
    private fun PlayerEntity.withEarnedXp(amount: Int): PlayerEntity {
        if (amount <= 0) return this
        val (newLifetime, newBanked) = CharacterLevelCalculator.distributeLifetimeXp(
            currentLifetime = lifetimePoints,
            currentBanked = bankedLifetimePoints,
            earned = amount
        )
        return copy(
            points = points + amount,
            lifetimePoints = newLifetime,
            bankedLifetimePoints = newBanked
        )
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
        "knowledge" -> player.copy(knowledge = value)
        "wealth" -> player.copy(wealth = value)
        "precision" -> player.copy(precision = value)
        "insight" -> player.copy(insight = value)
        else -> player
    }

    private companion object {
        const val MAX_NAME_LENGTH = 24
        const val MAX_RECENT = 200
        const val EVENT_GEM_REWARD = 2
    }
}

/** Outcome of redeeming a promo code. [Success] carries the granted reward for the UI message. */
sealed interface PromoRedeemResult {
    data class Success(val coins: Int, val gems: Int, val xp: Int) : PromoRedeemResult
    data object Invalid : PromoRedeemResult
    data object AlreadyRedeemed : PromoRedeemResult
}
