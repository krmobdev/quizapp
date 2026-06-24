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
        InventoryEntity::class,
        RecentQuestionsEntity::class,
        CategoryStatsEntity::class,
        StreakEntity::class,
        DailyRewardEntity::class,
        DailyQuestEntity::class,
        AchievementEntity::class,
        AppStateEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun categoryStatsDao(): CategoryStatsDao
    abstract fun appStateDao(): AppStateDao
    abstract fun streakDao(): StreakDao
    abstract fun dailyRewardDao(): DailyRewardDao
    abstract fun dailyQuestDao(): DailyQuestDao
    abstract fun achievementDao(): AchievementDao
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

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
