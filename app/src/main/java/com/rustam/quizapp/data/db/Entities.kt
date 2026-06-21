package com.rustam.quizapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Room schema for all persisted player progress. Single-row tables use a fixed
 * [PrimaryKey] of `0` so there is always exactly one row to read/upsert.
 *
 * The two in-progress blobs (saved quiz, mistakes pool) and a few cross-cutting
 * scalars/settings live in [AppStateEntity] as they did in the old `quiz_stats`
 * DataStore — they are heterogeneous and not worth normalising.
 *
 * All entities are also [Serializable] so they can be reused directly as the backup
 * file format (see `BackupRepository`).
 */

const val SINGLE_ROW_ID = 0

@Serializable
@Entity(tableName = "player")
data class PlayerEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val name: String = "",
    val points: Int = 0,
    val coins: Int = 0,
    val lifetimePoints: Int = 0,
    val avatarId: String? = null,
    val themeId: String? = null,
    val promoRedeemed: Boolean = false,
    val strength: Int = 0,
    val intelligence: Int = 0,
    val agility: Int = 0,
    val luck: Int = 0,
    val wisdom: Int = 0,
    val endurance: Int = 0,
    val focus: Int = 0,
    val charisma: Int = 0,
    val dailyCompletedDay: Long = -1L,
    val weeklyEpoch: Long = -1L,
    val weeklyCompletions: Int = 0,
    val weekendEpoch: Long = -1L,
    val weekendCompletions: Int = 0,
    val marathonDay: Long = -1L,
    val marathonCompletions: Int = 0
)

@Serializable
@Entity(tableName = "owned_item")
data class OwnedItemEntity(
    @PrimaryKey val itemId: String
)

@Serializable
@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey val itemId: String,
    val count: Int
)

@Serializable
@Entity(tableName = "recent_questions")
data class RecentQuestionsEntity(
    @PrimaryKey val categoryId: String,
    /** Question ids in play order, comma-separated (oldest first). */
    val idsCsv: String
)

@Serializable
@Entity(tableName = "category_stats")
data class CategoryStatsEntity(
    @PrimaryKey val categoryId: String,
    val quizzesCompleted: Int = 0,
    val correctAnswers: Int = 0,
    val questionsAnswered: Int = 0,
    val bestScorePercent: Int = 0
)

@Serializable
@Entity(tableName = "streak")
data class StreakEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val current: Int = 0,
    val best: Int = 0,
    val freezeCount: Int = 0,
    val lastPlayedDay: Long = -1L
)

@Serializable
@Entity(tableName = "daily_reward")
data class DailyRewardEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val lastClaimDay: Long = -1L,
    val claimStreak: Int = 0
)

@Serializable
@Entity(tableName = "achievement")
data class AchievementEntity(
    @PrimaryKey val id: String
)

@Serializable
@Entity(tableName = "app_state")
data class AppStateEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val totalQuizzes: Int = 0,
    val savedQuizJson: String? = null,
    val mistakesJson: String? = null,
    val soundEnabled: Boolean = true,
    val themeMode: String? = null,
    val appLanguage: String? = null,
    /** Guards the one-time DataStore -> Room import. */
    val migratedFromDataStore: Boolean = false
)
