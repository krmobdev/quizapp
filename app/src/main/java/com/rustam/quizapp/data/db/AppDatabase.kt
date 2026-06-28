package com.rustam.quizapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PlayerEntity::class,
        OwnedItemEntity::class,
        ShopDealEntity::class,
        InventoryEntity::class,
        RecentQuestionsEntity::class,
        CategoryStatsEntity::class,
        StreakEntity::class,
        DailyRewardEntity::class,
        DailyQuestEntity::class,
        AchievementEntity::class,
        RedeemedPromoEntity::class,
        AppStateEntity::class
    ],
    version = 10,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun shopDealDao(): ShopDealDao
    abstract fun categoryStatsDao(): CategoryStatsDao
    abstract fun appStateDao(): AppStateDao
    abstract fun streakDao(): StreakDao
    abstract fun dailyRewardDao(): DailyRewardDao
    abstract fun dailyQuestDao(): DailyQuestDao
    abstract fun achievementDao(): AchievementDao
    abstract fun promoDao(): PromoDao
    abstract fun backupDao(): BackupDao

    companion object {
        private const val DB_NAME = "quizapp.db"

        @Volatile
        private var instance: AppDatabase? = null

        /** Adds the Mastery Tree skill columns to the existing player row (keeps progress). */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf(
                    "skillErudition", "skillCommerce", "skillFortune",
                    "skillChronos", "skillSage", "skillResilience"
                ).forEach { column ->
                    db.execSQL("ALTER TABLE player ADD COLUMN $column INTEGER NOT NULL DEFAULT 0")
                }
            }
        }

        /**
         * Economy expansion: equipped title, lifetime-coins counter and the temporary-boost
         * counters. All added to the existing player row so progress is preserved.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE player ADD COLUMN equippedTitleId TEXT")
                listOf(
                    "lifetimeCoins", "coinBoostQuizzesLeft", "xpBoostQuizzesLeft"
                ).forEach { column ->
                    db.execSQL("ALTER TABLE player ADD COLUMN $column INTEGER NOT NULL DEFAULT 0")
                }
                // Seed the new lifetime-coins counter with the current balance so existing players
                // don't start their "coins all-time" stat from zero.
                db.execSQL("UPDATE player SET lifetimeCoins = coins")
            }
        }

        /** Adds the daily-quest progress table (one row tracking the current day's challenges). */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_quest (
                        id INTEGER NOT NULL PRIMARY KEY,
                        day INTEGER NOT NULL DEFAULT -1,
                        quizzesPlayed INTEGER NOT NULL DEFAULT 0,
                        correctAnswers INTEGER NOT NULL DEFAULT 0,
                        perfectQuizzes INTEGER NOT NULL DEFAULT 0,
                        coinsEarned INTEGER NOT NULL DEFAULT 0,
                        claimedMask INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * Hidden XP bank for players at the level cap. Any lifetime XP above the level-120 threshold
         * is moved into [bankedLifetimePoints] so a future cap raise can credit it automatically.
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE player ADD COLUMN bankedLifetimePoints INTEGER NOT NULL DEFAULT 0"
                )
                val level120Cap = 22 * 120 * 119
                db.execSQL(
                    """
                    UPDATE player SET
                        bankedLifetimePoints = MAX(0, lifetimePoints - $level120Cap),
                        lifetimePoints = MIN(lifetimePoints, $level120Cap)
                    """.trimIndent()
                )
            }
        }

        /** Passive talent tree progress stored as `nodeId:rank` CSV on the player row. */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE player ADD COLUMN talentProgressCsv TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        /** Four new character stats (Knowledge, Wealth, Precision, Insight) added to the player row. */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf("knowledge", "wealth", "precision", "insight").forEach { column ->
                    db.execSQL("ALTER TABLE player ADD COLUMN $column INTEGER NOT NULL DEFAULT 0")
                }
            }
        }

        /**
         * Economy v2: rare gems currency and the season-track state (period id, accumulated XP and
         * a claimed-levels bitmask), plus the daily shop-deals table.
         */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE player ADD COLUMN gems INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE player ADD COLUMN seasonId INTEGER NOT NULL DEFAULT -1")
                db.execSQL("ALTER TABLE player ADD COLUMN seasonXp INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE player ADD COLUMN seasonClaimedMask INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS shop_deal (
                        id INTEGER NOT NULL PRIMARY KEY,
                        day INTEGER NOT NULL DEFAULT -1,
                        purchasesCsv TEXT NOT NULL DEFAULT ''
                    )
                    """.trimIndent()
                )
            }
        }

        /** Adds onboardingShown flag to app_state (keeps all existing user data). */
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE app_state ADD COLUMN onboardingShown INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        /** Adds the redeemed-promo table tracking one-time promo codes claimed on this device. */
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS redeemed_promo (
                        code TEXT NOT NULL PRIMARY KEY
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .addMigrations(
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
                    MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
