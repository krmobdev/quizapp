package com.rustam.quizapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rustam.quizapp.domain.AnswerReward
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
import kotlinx.coroutines.flow.first
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

private val Context.playerDataStore: DataStore<Preferences> by preferencesDataStore(name = "quiz_player")

class PlayerRepository(
    private val context: Context,
    private val questionRepository: QuestionRepository
) {
    private val dataStore = context.applicationContext.playerDataStore

    fun observeProfile(): Flow<PlayerProfile> = dataStore.data.map { prefs ->
        val snapshot = readEventProgress(prefs)
        val categories = questionRepository.getCategories()
        val currentPoints = prefs[POINTS] ?: 0
        val lifetime = prefs[LIFETIME_POINTS] ?: currentPoints
        PlayerProfile(
            name = prefs[PLAYER_NAME].orEmpty(),
            points = currentPoints,
            coins = prefs[COINS] ?: 0,
            avatarEmoji = ShopCatalog.avatarEmoji(prefs[EQUIPPED_AVATAR] ?: ShopCatalog.DEFAULT_AVATAR_ID),
            eventProgress = QuizEvents.activeEvents(categories, snapshot),
            stats = readStats(prefs),
            lifetimePoints = lifetime
        )
    }

    /** Coins and owned/equipped cosmetics, used by the shop screen. */
    fun observeShop(): Flow<ShopState> = dataStore.data.map { prefs ->
        ShopState(
            coins = prefs[COINS] ?: 0,
            ownedItemIds = (prefs[OWNED_ITEMS] ?: emptySet()) + ShopCatalog.freeItemIds,
            equippedAvatarId = prefs[EQUIPPED_AVATAR] ?: ShopCatalog.DEFAULT_AVATAR_ID,
            equippedThemeId = prefs[EQUIPPED_THEME] ?: ShopCatalog.DEFAULT_THEME_ID
        )
    }

    /** The accent theme id applied app-wide. Defaults to the free theme. */
    val equippedThemeId: Flow<String> = dataStore.data.map { prefs ->
        prefs[EQUIPPED_THEME] ?: ShopCatalog.DEFAULT_THEME_ID
    }

    /**
     * Buys [itemId] for [price] coins if affordable and not already owned.
     * Returns `true` on a successful purchase. The read-and-write happens inside a
     * single [edit] block so the balance check and deduction are atomic.
     */
    suspend fun purchase(itemId: String, price: Int): Boolean {
        var purchased = false
        dataStore.edit { prefs ->
            val owned = (prefs[OWNED_ITEMS] ?: emptySet()) + ShopCatalog.freeItemIds
            val coins = prefs[COINS] ?: 0
            if (itemId !in owned && coins >= price) {
                prefs[COINS] = coins - price
                prefs[OWNED_ITEMS] = (prefs[OWNED_ITEMS] ?: emptySet()) + itemId
                purchased = true
            }
        }
        return purchased
    }

    suspend fun equipAvatar(avatarId: String) {
        dataStore.edit { prefs -> prefs[EQUIPPED_AVATAR] = avatarId }
    }

    suspend fun equipTheme(themeId: String) {
        dataStore.edit { prefs -> prefs[EQUIPPED_THEME] = themeId }
    }

    suspend fun setPlayerName(name: String) {
        val trimmed = name.trim().take(MAX_NAME_LENGTH)
        if (trimmed.isEmpty()) return
        dataStore.edit { prefs -> prefs[PLAYER_NAME] = trimmed }
    }

    /** Adds coins without touching points/XP (used for achievement rewards). */
    suspend fun addCoins(amount: Int) {
        if (amount <= 0) return
        dataStore.edit { prefs -> prefs[COINS] = (prefs[COINS] ?: 0) + amount }
    }

    suspend fun grantReward(reward: QuizReward) {
        dataStore.edit { prefs ->
            prefs[POINTS] = (prefs[POINTS] ?: 0) + reward.points
            prefs[COINS] = (prefs[COINS] ?: 0) + reward.coins
            prefs[LIFETIME_POINTS] = (prefs[LIFETIME_POINTS] ?: (prefs[POINTS] ?: 0)) + reward.points
        }
    }

    val promoRedeemed: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PROMO_REDEEMED] ?: false
    }

    suspend fun redeemPromoCode(code: String): PromoRedeemResult {
        val normalized = code.trim().uppercase()
        if (normalized != PROMO_CODE) return PromoRedeemResult.INVALID_CODE

        val alreadyRedeemed = dataStore.data.first()[PROMO_REDEEMED] ?: false
        if (alreadyRedeemed) return PromoRedeemResult.ALREADY_REDEEMED

        dataStore.edit { prefs ->
            prefs[PROMO_REDEEMED] = true
            prefs[COINS] = (prefs[COINS] ?: 0) + PROMO_REWARD_COINS
        }
        return PromoRedeemResult.SUCCESS
    }

    suspend fun eventProgressSnapshot(): EventProgressSnapshot {
        return readEventProgress(dataStore.data.first())
    }

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

        // Read current stats from preferences
        val stats = readStats(dataStore.data.first())

        // Calculate bonuses: percentage bonuses (Strength/Intelligence) plus
        // flat bonuses (Wisdom/Endurance).
        val xpBonus = (baseReward.points * (stats.xpBonusPercent / 100f)).toInt() + stats.flatXpBonus
        val coinBonus = (baseReward.coins * (stats.coinBonusPercent / 100f)).toInt() + stats.flatCoinBonus

        var pointsEarned = baseReward.points + xpBonus
        var coinsEarned = baseReward.coins + coinBonus

        // Luck (+ Charisma) roll for double rewards (Critical Success).
        // Focus sharpens the multiplier applied when it triggers.
        val doubleChance = stats.doubleRewardChancePercent + stats.critChanceBonusPercent
        val isCriticalSuccess = if (doubleChance > 0) {
            (1..100).random() <= doubleChance
        } else false

        if (isCriticalSuccess) {
            pointsEarned = (pointsEarned * stats.critMultiplier).toInt()
            coinsEarned = (coinsEarned * stats.critMultiplier).toInt()
        }
        
        val finalReward = baseReward.copy(
            points = pointsEarned,
            coins = coinsEarned,
            isCriticalSuccess = isCriticalSuccess,
            xpBonus = xpBonus,
            coinBonus = coinBonus
        )
        
        grantReward(finalReward)
        if (activeEvent != null) markEventCompleted(activeEvent.type)
        return finalReward
    }

    suspend fun upgradeStat(statName: String): Boolean {
        var upgraded = false
        dataStore.edit { prefs ->
            val key = when (statName) {
                "strength" -> CHAR_STRENGTH
                "intelligence" -> CHAR_INTELLIGENCE
                "agility" -> CHAR_AGILITY
                "luck" -> CHAR_LUCK
                "wisdom" -> CHAR_WISDOM
                "endurance" -> CHAR_ENDURANCE
                "focus" -> CHAR_FOCUS
                "charisma" -> CHAR_CHARISMA
                else -> null
            }
            if (key != null) {
                val currentVal = prefs[key] ?: 0
                val cost = 100 + currentVal * 25
                val points = prefs[POINTS] ?: 0
                if (currentVal < 20 && points >= cost) {
                    prefs[key] = currentVal + 1
                    prefs[POINTS] = points - cost
                    upgraded = true
                }
            }
        }
        return upgraded
    }

    private suspend fun markEventCompleted(type: QuizEventType) {
        val day = LocalDate.now()
        dataStore.edit { prefs ->
            when (type) {
                QuizEventType.DAILY -> prefs[DAILY_COMPLETED_DAY] = QuizEvents.epochDay(day)
                QuizEventType.WEEKLY -> {
                    val week = QuizEvents.epochWeek(day)
                    val currentWeek = prefs[WEEKLY_EPOCH] ?: -1L
                    prefs[WEEKLY_EPOCH] = week
                    prefs[WEEKLY_COMPLETIONS] = if (currentWeek == week) {
                        (prefs[WEEKLY_COMPLETIONS] ?: 0) + 1
                    } else {
                        1
                    }
                }
                QuizEventType.WEEKEND_BLITZ -> {
                    val weekend = QuizEvents.epochWeekend(day)
                    val currentWeekend = prefs[WEEKEND_EPOCH] ?: -1L
                    prefs[WEEKEND_EPOCH] = weekend
                    prefs[WEEKEND_COMPLETIONS] = if (currentWeekend == weekend) {
                        (prefs[WEEKEND_COMPLETIONS] ?: 0) + 1
                    } else {
                        1
                    }
                }
                QuizEventType.MARATHON -> {
                    val epochDay = QuizEvents.epochDay(day)
                    val currentDay = prefs[MARATHON_DAY] ?: -1L
                    prefs[MARATHON_DAY] = epochDay
                    prefs[MARATHON_COMPLETIONS] = if (currentDay == epochDay) {
                        (prefs[MARATHON_COMPLETIONS] ?: 0) + 1
                    } else {
                        1
                    }
                }
            }
        }
    }

    private fun readStats(prefs: Preferences): CharacterStats = CharacterStats(
        strength = prefs[CHAR_STRENGTH] ?: 0,
        intelligence = prefs[CHAR_INTELLIGENCE] ?: 0,
        agility = prefs[CHAR_AGILITY] ?: 0,
        luck = prefs[CHAR_LUCK] ?: 0,
        wisdom = prefs[CHAR_WISDOM] ?: 0,
        endurance = prefs[CHAR_ENDURANCE] ?: 0,
        focus = prefs[CHAR_FOCUS] ?: 0,
        charisma = prefs[CHAR_CHARISMA] ?: 0
    )

    private fun readEventProgress(prefs: Preferences): EventProgressSnapshot =
        EventProgressSnapshot(
            dailyCompletedDay = prefs[DAILY_COMPLETED_DAY] ?: -1L,
            weeklyEpoch = prefs[WEEKLY_EPOCH] ?: -1L,
            weeklyCompletions = prefs[WEEKLY_COMPLETIONS] ?: 0,
            weekendEpoch = prefs[WEEKEND_EPOCH] ?: -1L,
            weekendCompletions = prefs[WEEKEND_COMPLETIONS] ?: 0,
            marathonDay = prefs[MARATHON_DAY] ?: -1L,
            marathonCompletions = prefs[MARATHON_COMPLETIONS] ?: 0
        )

    fun getRecentQuestions(categoryId: String): Flow<List<String>> = dataStore.data.map { prefs ->
        val key = stringPreferencesKey("recent_questions_$categoryId")
        val raw = prefs[key].orEmpty()
        if (raw.isEmpty()) emptyList() else raw.split(",")
    }

    suspend fun saveRecentQuestions(categoryId: String, questionIds: List<String>) {
        dataStore.edit { prefs ->
            val key = stringPreferencesKey("recent_questions_$categoryId")
            val raw = prefs[key].orEmpty()
            val current = if (raw.isEmpty()) emptyList() else raw.split(",")
            val orderedList = (current - questionIds.toSet()) + questionIds
            val limited = orderedList.takeLast(200)
            prefs[key] = limited.joinToString(",")
        }
    }

    private companion object {
        const val MAX_NAME_LENGTH = 24
        const val PROMO_CODE = "AAAAAA"
        const val PROMO_REWARD_COINS = 3000
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val POINTS = intPreferencesKey("points")
        val COINS = intPreferencesKey("coins")
        val DAILY_COMPLETED_DAY = longPreferencesKey("daily_completed_day")
        val WEEKLY_EPOCH = longPreferencesKey("weekly_epoch")
        val WEEKLY_COMPLETIONS = intPreferencesKey("weekly_completions")
        val WEEKEND_EPOCH = longPreferencesKey("weekend_epoch")
        val WEEKEND_COMPLETIONS = intPreferencesKey("weekend_completions")
        val MARATHON_DAY = longPreferencesKey("marathon_day")
        val MARATHON_COMPLETIONS = intPreferencesKey("marathon_completions")
        val OWNED_ITEMS = stringSetPreferencesKey("owned_items")
        val EQUIPPED_AVATAR = stringPreferencesKey("equipped_avatar")
        val EQUIPPED_THEME = stringPreferencesKey("equipped_theme")
        val PROMO_REDEEMED = booleanPreferencesKey("promo_redeemed")

        val CHAR_STRENGTH = intPreferencesKey("char_strength")
        val CHAR_INTELLIGENCE = intPreferencesKey("char_intelligence")
        val CHAR_AGILITY = intPreferencesKey("char_agility")
        val CHAR_LUCK = intPreferencesKey("char_luck")
        val CHAR_WISDOM = intPreferencesKey("char_wisdom")
        val CHAR_ENDURANCE = intPreferencesKey("char_endurance")
        val CHAR_FOCUS = intPreferencesKey("char_focus")
        val CHAR_CHARISMA = intPreferencesKey("char_charisma")
        val LIFETIME_POINTS = intPreferencesKey("lifetime_points")
    }
}

enum class PromoRedeemResult {
    SUCCESS,
    INVALID_CODE,
    ALREADY_REDEEMED
}