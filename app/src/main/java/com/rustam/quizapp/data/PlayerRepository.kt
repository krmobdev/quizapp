package com.rustam.quizapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rustam.quizapp.domain.DailyQuest
import com.rustam.quizapp.domain.QuizReward
import com.rustam.quizapp.domain.RewardCalculator
import com.rustam.quizapp.domain.ShopCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class PlayerProfile(
    val name: String,
    val points: Int,
    val coins: Int,
    val avatarEmoji: String,
    val dailyQuestCategoryId: String?,
    val dailyQuestCompleted: Boolean
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
        val questCategory = DailyQuest.categoryForDay(questionRepository.getCategories())
        val completedDay = prefs[DAILY_COMPLETED_DAY] ?: -1L
        PlayerProfile(
            name = prefs[PLAYER_NAME].orEmpty(),
            points = prefs[POINTS] ?: 0,
            coins = prefs[COINS] ?: 0,
            avatarEmoji = ShopCatalog.avatarEmoji(prefs[EQUIPPED_AVATAR] ?: ShopCatalog.DEFAULT_AVATAR_ID),
            dailyQuestCategoryId = questCategory?.id,
            dailyQuestCompleted = completedDay == DailyQuest.epochDay()
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

    suspend fun grantReward(reward: QuizReward) {
        dataStore.edit { prefs ->
            prefs[POINTS] = (prefs[POINTS] ?: 0) + reward.points
            prefs[COINS] = (prefs[COINS] ?: 0) + reward.coins
        }
    }

    fun isDailyQuestCategory(categoryId: String): Boolean {
        val quest = DailyQuest.categoryForDay(questionRepository.getCategories()) ?: return false
        return quest.id == categoryId
    }

    suspend fun isDailyQuestCompletedToday(): Boolean {
        val prefs = dataStore.data.first()
        return (prefs[DAILY_COMPLETED_DAY] ?: -1L) == DailyQuest.epochDay()
    }

    suspend fun markDailyQuestCompleted() {
        dataStore.edit { prefs ->
            prefs[DAILY_COMPLETED_DAY] = DailyQuest.epochDay()
        }
    }

    suspend fun grantQuizReward(
        categoryId: String,
        score: Int,
        total: Int,
        allowDailyBonus: Boolean
    ): QuizReward {
        val isDaily = allowDailyBonus &&
            isDailyQuestCategory(categoryId) &&
            !isDailyQuestCompletedToday()
        val reward = RewardCalculator.calculate(score, total, isDaily)
        grantReward(reward)
        if (isDaily) markDailyQuestCompleted()
        return reward
    }

    private companion object {
        const val MAX_NAME_LENGTH = 24
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val POINTS = intPreferencesKey("points")
        val COINS = intPreferencesKey("coins")
        val DAILY_COMPLETED_DAY = longPreferencesKey("daily_completed_day")
        val OWNED_ITEMS = stringSetPreferencesKey("owned_items")
        val EQUIPPED_AVATAR = stringPreferencesKey("equipped_avatar")
        val EQUIPPED_THEME = stringPreferencesKey("equipped_theme")
    }
}