package com.rustam.quizapp.data

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.rustam.quizapp.data.db.AchievementEntity
import com.rustam.quizapp.data.db.AppDatabase
import com.rustam.quizapp.data.db.AppStateEntity
import com.rustam.quizapp.data.db.CategoryStatsEntity
import com.rustam.quizapp.data.db.DailyQuestEntity
import com.rustam.quizapp.data.db.DailyRewardEntity
import com.rustam.quizapp.data.db.InventoryEntity
import com.rustam.quizapp.data.db.OwnedItemEntity
import com.rustam.quizapp.data.db.PlayerEntity
import com.rustam.quizapp.data.db.RecentQuestionsEntity
import com.rustam.quizapp.data.db.RedeemedPromoEntity
import com.rustam.quizapp.data.db.StreakEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** The complete, portable snapshot of a player's progress. */
@Serializable
data class BackupFile(
    val version: Int = CURRENT_VERSION,
    val exportedAt: Long = 0L,
    val data: BackupData
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

@Serializable
data class BackupData(
    val player: PlayerEntity? = null,
    val owned: List<OwnedItemEntity> = emptyList(),
    val inventory: List<InventoryEntity> = emptyList(),
    val recent: List<RecentQuestionsEntity> = emptyList(),
    val categoryStats: List<CategoryStatsEntity> = emptyList(),
    val streak: StreakEntity? = null,
    val dailyReward: DailyRewardEntity? = null,
    val dailyQuest: DailyQuestEntity? = null,
    val achievements: List<AchievementEntity> = emptyList(),
    val redeemedPromo: List<RedeemedPromoEntity> = emptyList(),
    val appState: AppStateEntity? = null
)

/** Outcome of an import attempt, surfaced to the UI. */
enum class ImportResult { SUCCESS, INVALID_FILE, UNSUPPORTED_VERSION }

/**
 * Exports/imports the whole Room database as a single JSON file (via SAF [Uri]s) and
 * performs a full progress reset. Settings (sound/theme/language) survive a reset since
 * they are not "progress".
 */
class BackupRepository(context: Context) {

    private val appContext = context.applicationContext
    private val db = AppDatabase.getInstance(appContext)
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    /** Serializes the entire database and writes it to [target]. Throws on I/O failure. */
    suspend fun export(target: Uri) = withContext(Dispatchers.IO) {
        val backup = BackupFile(
            exportedAt = System.currentTimeMillis(),
            data = BackupData(
                player = db.playerDao().getPlayer(),
                owned = db.backupDao().allOwned(),
                inventory = db.backupDao().allInventory(),
                recent = db.backupDao().allRecent(),
                categoryStats = db.backupDao().allCategoryStats(),
                streak = db.streakDao().get(),
                dailyReward = db.dailyRewardDao().get(),
                dailyQuest = db.dailyQuestDao().get(),
                achievements = db.backupDao().allAchievements(),
                redeemedPromo = db.backupDao().allRedeemedPromo(),
                appState = db.appStateDao().get()
            )
        )
        val text = json.encodeToString(backup)
        appContext.contentResolver.openOutputStream(target, "wt")?.use { out ->
            out.write(text.toByteArray())
        } ?: error("Cannot open output stream for $target")
    }

    /** Reads a backup from [source] and replaces all progress in a single transaction. */
    suspend fun import(source: Uri): ImportResult = withContext(Dispatchers.IO) {
        val text = runCatching {
            appContext.contentResolver.openInputStream(source)?.use { it.readBytes().decodeToString() }
        }.getOrNull() ?: return@withContext ImportResult.INVALID_FILE

        val backup = runCatching { json.decodeFromString<BackupFile>(text) }.getOrNull()
            ?: return@withContext ImportResult.INVALID_FILE
        if (backup.version > BackupFile.CURRENT_VERSION) return@withContext ImportResult.UNSUPPORTED_VERSION

        val d = backup.data
        db.withTransaction {
            clearAll()
            db.playerDao().upsertPlayer(d.player ?: PlayerEntity())
            db.backupDao().insertOwned(d.owned)
            db.backupDao().insertInventory(d.inventory)
            db.backupDao().insertRecent(d.recent)
            db.backupDao().insertCategoryStats(d.categoryStats)
            db.backupDao().insertAchievements(d.achievements)
            db.backupDao().insertRedeemedPromo(d.redeemedPromo)
            db.streakDao().upsert(d.streak ?: StreakEntity())
            db.dailyRewardDao().upsert(d.dailyReward ?: DailyRewardEntity())
            db.dailyQuestDao().upsert(d.dailyQuest ?: DailyQuestEntity())
            // Force the migrated flag so the legacy DataStore import can never clobber
            // freshly imported data on a later launch.
            db.appStateDao().upsert((d.appState ?: AppStateEntity()).copy(migratedFromDataStore = true))
        }
        ImportResult.SUCCESS
    }

    /**
     * Wipes all gameplay progress, keeping user settings (sound/theme/language) and the
     * migration guard intact.
     */
    suspend fun resetProgress() = withContext(Dispatchers.IO) {
        db.withTransaction {
            clearAll()
            val current = db.appStateDao().get() ?: AppStateEntity()
            db.appStateDao().upsert(
                AppStateEntity(
                    totalQuizzes = 0,
                    savedQuizJson = null,
                    mistakesJson = null,
                    soundEnabled = current.soundEnabled,
                    themeMode = current.themeMode,
                    appLanguage = current.appLanguage,
                    migratedFromDataStore = true
                )
            )
        }
    }

    private suspend fun clearAll() {
        db.backupDao().apply {
            clearPlayer()
            clearOwned()
            clearInventory()
            clearRecent()
            clearCategoryStats()
            clearStreak()
            clearDailyReward()
            clearDailyQuest()
            clearAchievements()
            clearRedeemedPromo()
            clearAppState()
        }
    }
}
