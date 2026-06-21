package com.rustam.quizapp.data.db

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.withTransaction
import kotlinx.coroutines.flow.first

// Read-only access to the legacy DataStore Preferences files. Kept solely so existing
// players' progress can be imported into Room once; safe to delete after a few releases.
private val Context.legacyPlayer: DataStore<Preferences> by preferencesDataStore(name = "quiz_player")
private val Context.legacyStats: DataStore<Preferences> by preferencesDataStore(name = "quiz_stats")
private val Context.legacyStreak: DataStore<Preferences> by preferencesDataStore(name = "quiz_streak")
private val Context.legacyDailyReward: DataStore<Preferences> by preferencesDataStore(name = "quiz_daily_reward")
private val Context.legacyAchievements: DataStore<Preferences> by preferencesDataStore(name = "quiz_achievements")

/**
 * One-time import of the pre-Room DataStore Preferences into the Room database. Runs at
 * most once (guarded by [AppStateEntity.migratedFromDataStore]); a fresh install simply
 * imports empty stores and flips the flag.
 */
object LegacyMigration {

    suspend fun runOnce(context: Context) {
        val db = AppDatabase.getInstance(context)
        val appStateDao = db.appStateDao()
        if (appStateDao.get()?.migratedFromDataStore == true) return

        val player = context.legacyPlayer.data.first().asNameMap()
        val stats = context.legacyStats.data.first().asNameMap()
        val streak = context.legacyStreak.data.first().asNameMap()
        val daily = context.legacyDailyReward.data.first().asNameMap()
        val achievements = context.legacyAchievements.data.first().asNameMap()

        db.withTransaction {
            db.playerDao().upsertPlayer(player.toPlayerEntity())
            player.toOwnedItems().forEach { db.playerDao().addOwned(it) }
            player.toInventory().forEach { db.playerDao().upsertInventory(it) }
            player.toRecentQuestions().forEach { db.playerDao().upsertRecent(it) }

            stats.toCategoryStats().forEach { db.categoryStatsDao().upsert(it) }
            achievements.toAchievementIds().forEach { db.achievementDao().unlock(AchievementEntity(it)) }

            db.streakDao().upsert(streak.toStreakEntity())
            db.dailyRewardDao().upsert(daily.toDailyRewardEntity())
            appStateDao.upsert(stats.toAppStateEntity(migrated = true))
        }
    }

    private fun Preferences.asNameMap(): Map<String, Any> =
        asMap().entries.associate { it.key.name to it.value }

    private inline fun <reified T> Map<String, Any>.read(name: String): T? = this[name] as? T

    // --- quiz_player ---

    private fun Map<String, Any>.toPlayerEntity(): PlayerEntity = PlayerEntity(
        name = read<String>("player_name") ?: "",
        points = read<Int>("points") ?: 0,
        coins = read<Int>("coins") ?: 0,
        lifetimePoints = read<Int>("lifetime_points") ?: (read<Int>("points") ?: 0),
        avatarId = read<String>("equipped_avatar"),
        themeId = read<String>("equipped_theme"),
        promoRedeemed = read<Boolean>("promo_redeemed") ?: false,
        strength = read<Int>("char_strength") ?: 0,
        intelligence = read<Int>("char_intelligence") ?: 0,
        agility = read<Int>("char_agility") ?: 0,
        luck = read<Int>("char_luck") ?: 0,
        wisdom = read<Int>("char_wisdom") ?: 0,
        endurance = read<Int>("char_endurance") ?: 0,
        focus = read<Int>("char_focus") ?: 0,
        charisma = read<Int>("char_charisma") ?: 0,
        dailyCompletedDay = read<Long>("daily_completed_day") ?: -1L,
        weeklyEpoch = read<Long>("weekly_epoch") ?: -1L,
        weeklyCompletions = read<Int>("weekly_completions") ?: 0,
        weekendEpoch = read<Long>("weekend_epoch") ?: -1L,
        weekendCompletions = read<Int>("weekend_completions") ?: 0,
        marathonDay = read<Long>("marathon_day") ?: -1L,
        marathonCompletions = read<Int>("marathon_completions") ?: 0
    )

    private fun Map<String, Any>.toOwnedItems(): List<OwnedItemEntity> =
        (read<Set<*>>("owned_items") ?: emptySet<Any>())
            .filterIsInstance<String>()
            .map { OwnedItemEntity(it) }

    private fun Map<String, Any>.toInventory(): List<InventoryEntity> = entries
        .filter { it.key.startsWith("inv_") }
        .mapNotNull { (k, v) ->
            (v as? Int)?.takeIf { it > 0 }?.let { InventoryEntity(k.removePrefix("inv_"), it) }
        }

    private fun Map<String, Any>.toRecentQuestions(): List<RecentQuestionsEntity> = entries
        .filter { it.key.startsWith("recent_questions_") }
        .mapNotNull { (k, v) ->
            (v as? String)?.takeIf { it.isNotEmpty() }
                ?.let { RecentQuestionsEntity(k.removePrefix("recent_questions_"), it) }
        }

    // --- quiz_stats ---

    private fun Map<String, Any>.toCategoryStats(): List<CategoryStatsEntity> {
        val ids = keys
            .filter { it.startsWith("cat.") }
            .map { it.removePrefix("cat.").substringBefore(".") }
            .distinct()
        return ids.map { id ->
            CategoryStatsEntity(
                categoryId = id,
                quizzesCompleted = read<Int>("cat.$id.quizzes") ?: 0,
                correctAnswers = read<Int>("cat.$id.correct") ?: 0,
                questionsAnswered = read<Int>("cat.$id.answered") ?: 0,
                bestScorePercent = read<Int>("cat.$id.best") ?: 0
            )
        }
    }

    private fun Map<String, Any>.toAppStateEntity(migrated: Boolean): AppStateEntity = AppStateEntity(
        totalQuizzes = read<Int>("total_quizzes") ?: 0,
        savedQuizJson = read<String>("saved_quiz_json"),
        mistakesJson = read<String>("mistakes_json"),
        soundEnabled = read<Boolean>("sound_enabled") ?: true,
        themeMode = read<String>("theme_mode"),
        appLanguage = read<String>("app_language"),
        migratedFromDataStore = migrated
    )

    // --- quiz_streak / quiz_daily_reward / quiz_achievements ---

    private fun Map<String, Any>.toStreakEntity(): StreakEntity = StreakEntity(
        current = read<Int>("current_streak") ?: 0,
        best = read<Int>("best_streak") ?: 0,
        freezeCount = read<Int>("streak_freeze_count") ?: 0,
        lastPlayedDay = read<Long>("last_played_day") ?: -1L
    )

    private fun Map<String, Any>.toDailyRewardEntity(): DailyRewardEntity = DailyRewardEntity(
        lastClaimDay = read<Long>("daily_last_claim_day") ?: -1L,
        claimStreak = read<Int>("daily_claim_streak") ?: 0
    )

    private fun Map<String, Any>.toAchievementIds(): Set<String> =
        (read<Set<*>>("unlocked_achievements") ?: emptySet<Any>())
            .filterIsInstance<String>()
            .toSet()
}
