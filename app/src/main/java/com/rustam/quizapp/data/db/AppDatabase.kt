package com.rustam.quizapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlayerEntity::class,
        OwnedItemEntity::class,
        InventoryEntity::class,
        RecentQuestionsEntity::class,
        CategoryStatsEntity::class,
        StreakEntity::class,
        DailyRewardEntity::class,
        AchievementEntity::class,
        AppStateEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun categoryStatsDao(): CategoryStatsDao
    abstract fun appStateDao(): AppStateDao
    abstract fun streakDao(): StreakDao
    abstract fun dailyRewardDao(): DailyRewardDao
    abstract fun achievementDao(): AchievementDao
    abstract fun backupDao(): BackupDao

    companion object {
        private const val DB_NAME = "quizapp.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
