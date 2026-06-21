package com.rustam.quizapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Player profile, owned cosmetics, consumable inventory and recent-question history. */
@Dao
interface PlayerDao {

    @Query("SELECT * FROM player WHERE id = 0")
    fun observePlayer(): Flow<PlayerEntity?>

    @Query("SELECT * FROM player WHERE id = 0")
    suspend fun getPlayer(): PlayerEntity?

    @Upsert
    suspend fun upsertPlayer(player: PlayerEntity)

    // --- Owned cosmetics ---

    @Query("SELECT itemId FROM owned_item")
    fun observeOwnedIds(): Flow<List<String>>

    @Query("SELECT itemId FROM owned_item")
    suspend fun getOwnedIds(): List<String>

    @Upsert
    suspend fun addOwned(item: OwnedItemEntity)

    // --- Consumable inventory (boosters + power-ups) ---

    @Query("SELECT * FROM inventory")
    fun observeInventory(): Flow<List<InventoryEntity>>

    @Query("SELECT count FROM inventory WHERE itemId = :itemId")
    suspend fun getInventoryCount(itemId: String): Int?

    @Upsert
    suspend fun upsertInventory(item: InventoryEntity)

    // --- Recent questions ---

    @Query("SELECT * FROM recent_questions WHERE categoryId = :categoryId")
    fun observeRecent(categoryId: String): Flow<RecentQuestionsEntity?>

    @Query("SELECT * FROM recent_questions WHERE categoryId = :categoryId")
    suspend fun getRecent(categoryId: String): RecentQuestionsEntity?

    @Upsert
    suspend fun upsertRecent(recent: RecentQuestionsEntity)
}

/** Per-category quiz statistics. */
@Dao
interface CategoryStatsDao {

    @Query("SELECT * FROM category_stats ORDER BY categoryId")
    fun observeAll(): Flow<List<CategoryStatsEntity>>

    @Query("SELECT * FROM category_stats WHERE categoryId = :categoryId")
    suspend fun get(categoryId: String): CategoryStatsEntity?

    @Upsert
    suspend fun upsert(stats: CategoryStatsEntity)
}

/** Cross-cutting scalars, settings and in-progress JSON blobs (single row). */
@Dao
interface AppStateDao {

    @Query("SELECT * FROM app_state WHERE id = 0")
    fun observe(): Flow<AppStateEntity?>

    @Query("SELECT * FROM app_state WHERE id = 0")
    suspend fun get(): AppStateEntity?

    @Upsert
    suspend fun upsert(state: AppStateEntity)
}

@Dao
interface StreakDao {

    @Query("SELECT * FROM streak WHERE id = 0")
    fun observe(): Flow<StreakEntity?>

    @Query("SELECT * FROM streak WHERE id = 0")
    suspend fun get(): StreakEntity?

    @Upsert
    suspend fun upsert(streak: StreakEntity)
}

@Dao
interface DailyRewardDao {

    @Query("SELECT * FROM daily_reward WHERE id = 0")
    fun observe(): Flow<DailyRewardEntity?>

    @Query("SELECT * FROM daily_reward WHERE id = 0")
    suspend fun get(): DailyRewardEntity?

    @Upsert
    suspend fun upsert(reward: DailyRewardEntity)
}

@Dao
interface AchievementDao {

    @Query("SELECT id FROM achievement")
    fun observeUnlocked(): Flow<List<String>>

    @Query("SELECT id FROM achievement")
    suspend fun getUnlocked(): List<String>

    @Upsert
    suspend fun unlock(achievement: AchievementEntity)
}

/** Bulk operations used by backup import and full reset. */
@Dao
interface BackupDao {

    // Full snapshot for export.
    @Query("SELECT * FROM owned_item") suspend fun allOwned(): List<OwnedItemEntity>
    @Query("SELECT * FROM inventory") suspend fun allInventory(): List<InventoryEntity>
    @Query("SELECT * FROM recent_questions") suspend fun allRecent(): List<RecentQuestionsEntity>
    @Query("SELECT * FROM category_stats") suspend fun allCategoryStats(): List<CategoryStatsEntity>
    @Query("SELECT * FROM achievement") suspend fun allAchievements(): List<AchievementEntity>

    @Upsert suspend fun insertOwned(items: List<OwnedItemEntity>)
    @Upsert suspend fun insertInventory(items: List<InventoryEntity>)
    @Upsert suspend fun insertRecent(items: List<RecentQuestionsEntity>)
    @Upsert suspend fun insertCategoryStats(items: List<CategoryStatsEntity>)
    @Upsert suspend fun insertAchievements(items: List<AchievementEntity>)

    @Query("DELETE FROM player") suspend fun clearPlayer()
    @Query("DELETE FROM owned_item") suspend fun clearOwned()
    @Query("DELETE FROM inventory") suspend fun clearInventory()
    @Query("DELETE FROM recent_questions") suspend fun clearRecent()
    @Query("DELETE FROM category_stats") suspend fun clearCategoryStats()
    @Query("DELETE FROM streak") suspend fun clearStreak()
    @Query("DELETE FROM daily_reward") suspend fun clearDailyReward()
    @Query("DELETE FROM achievement") suspend fun clearAchievements()
}
