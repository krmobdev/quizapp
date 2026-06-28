package com.rustam.quizapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Room schema for all persisted player progress. Single-row tables use a fixed
 * [PrimaryKey] of `0` so there is always exactly one row to read/upsert.
 *
 * The in-progress saved-quiz blob and a few cross-cutting
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
    /**
     * Hidden lifetime XP banked after reaching [com.rustam.quizapp.domain.CharacterLevelCalculator.MAX_LEVEL].
     * Counts toward level when the cap is raised (e.g. to 200). Not shown in the UI.
     */
    val bankedLifetimePoints: Int = 0,
    val avatarId: String? = null,
    val themeId: String? = null,
    /** Equipped cosmetic title id (see ShopCatalog.titles); null = no title shown. */
    val equippedTitleId: String? = null,
    val promoRedeemed: Boolean = false,
    /** Total coins ever earned (never decreases when spending) — powers the player-card dashboard. */
    val lifetimeCoins: Int = 0,
    /** Remaining quizzes the active temporary boosts apply to (0 = inactive). */
    val coinBoostQuizzesLeft: Int = 0,
    val xpBoostQuizzesLeft: Int = 0,
    /** Rare premium currency (gems 💎), earned from achievements, events and the season track. */
    val gems: Int = 0,
    /** Season track state: the season period currently tracked, accumulated season XP and a
     *  bitmask of reward levels already claimed (added in DB v8). */
    val seasonId: Int = -1,
    val seasonXp: Int = 0,
    val seasonClaimedMask: Long = 0L,
    val strength: Int = 0,
    val intelligence: Int = 0,
    val agility: Int = 0,
    val luck: Int = 0,
    val wisdom: Int = 0,
    val endurance: Int = 0,
    val focus: Int = 0,
    val charisma: Int = 0,
    // Expanded characteristics (added in DB v7).
    val knowledge: Int = 0,
    val wealth: Int = 0,
    val precision: Int = 0,
    val insight: Int = 0,
    // Mastery Tree: unlocked tier count per skill branch (see domain SkillBranch).
    val skillErudition: Int = 0,
    val skillCommerce: Int = 0,
    val skillFortune: Int = 0,
    val skillChronos: Int = 0,
    val skillSage: Int = 0,
    val skillResilience: Int = 0,
    /** Passive talent tree progress: comma-separated `nodeId:rank` pairs (see PassiveTalentTree). */
    val talentProgressCsv: String = "",
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

/**
 * Daily shop deals state (single row). [day] is the epoch day the current rotation belongs to;
 * [purchasesCsv] holds `dealId:count` pairs counting how many times each of today's deals has been
 * bought, enforcing per-deal daily caps. Both reset when a new day's rotation is generated.
 */
@Serializable
@Entity(tableName = "shop_deal")
data class ShopDealEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val day: Long = -1L,
    val purchasesCsv: String = ""
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

/**
 * Per-day progress for the daily quests (single row). [day] is the epoch day the counters belong
 * to; when a quiz is recorded on a new day the counters and [claimedMask] reset. [claimedMask] is a
 * bitmask over the slots in the day's challenge set (see domain DailyChallenges).
 */
@Serializable
@Entity(tableName = "daily_quest")
data class DailyQuestEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val day: Long = -1L,
    val quizzesPlayed: Int = 0,
    val correctAnswers: Int = 0,
    val perfectQuizzes: Int = 0,
    val coinsEarned: Int = 0,
    val claimedMask: Int = 0
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
    val migratedFromDataStore: Boolean = false,
    /** Set to true after the user has seen the first-launch onboarding screens. */
    val onboardingShown: Boolean = false
)
