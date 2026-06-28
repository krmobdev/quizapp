package com.rustam.quizapp.data

import android.content.Context
import com.rustam.quizapp.R
import kotlinx.serialization.json.Json

/**
 * Loads quiz questions from the app's assets and serves them per category.
 *
 * Russian banks live in `questions_<categoryId>.json`; English banks in
 * `questions_<categoryId>_en.json`. Parsed lists are cached per language and category.
 */
class QuestionRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private fun cacheKey(language: AppLanguage, categoryId: String): String =
        "${language.name}:$categoryId"

    private fun questionsFor(language: AppLanguage, categoryId: String): List<Question> {
        val key = cacheKey(language, categoryId)
        synchronized(questionsCache) {
            return questionsCache.getOrPut(key) {
                val suffix = if (language == AppLanguage.EN) "_en" else ""
                val fileName = "questions_${categoryId}$suffix.json"
                context.assets.open(fileName).bufferedReader().use { reader ->
                    json.decodeFromString<List<Question>>(reader.readText())
                }
            }
        }
    }

    /** Categories shown on the home screen. Hardcoded for now. */
    fun getCategories(): List<Category> = listOf(
        Category(id = "chemistry", titleRes = R.string.category_chemistry, emoji = "🧪"),
        Category(id = "physics", titleRes = R.string.category_physics, emoji = "⚛️"),
        Category(id = "history", titleRes = R.string.category_history, emoji = "📜"),
        Category(id = "movies", titleRes = R.string.category_movies, emoji = "🎬"),
        Category(id = "art", titleRes = R.string.category_art, emoji = "🎨"),
        Category(id = "animals", titleRes = R.string.category_animals, emoji = "🐾"),
        Category(id = "geography", titleRes = R.string.category_geography, emoji = "🌍"),
        Category(id = "math", titleRes = R.string.category_math, emoji = "🔢"),
        Category(id = "informatics", titleRes = R.string.category_informatics, emoji = "💻"),
        Category(id = "astronomy", titleRes = R.string.category_astronomy, emoji = "🔭")
    )

    /**
     * Picks [quizSize] questions from a pool biased toward least-recently-seen items.
     * [excludeIds] is oldest-first history; questions seen longest ago are preferred.
     */
    fun getQuestions(
        categoryId: String,
        difficulty: Difficulty?,
        language: AppLanguage = AppLanguage.RU,
        bankSize: Int = BANK_SIZE,
        quizSize: Int = QUIZ_SIZE,
        excludeIds: List<String> = emptyList()
    ): List<Question> {
        val allQuestions = questionsFor(language, categoryId)
            .filter { difficulty == null || it.difficulty == difficulty }

        val pool = buildRecencyPool(allQuestions, excludeIds, quizSize)
        return pool.shuffled()
            .take(bankSize.coerceAtMost(pool.size))
            .shuffled()
            .take(quizSize)
            .map { it.shuffledOptions() }
    }

    /**
     * Builds a quiz whose difficulty mix is given by [counts] (difficulty -> number of
     * questions). Used by the adaptive mode. Buckets that run dry are topped up from the
     * remaining pool so the quiz always reaches the requested total when the bank allows.
     */
    fun getAdaptiveQuestions(
        categoryId: String,
        counts: Map<Difficulty, Int>,
        language: AppLanguage = AppLanguage.RU,
        excludeIds: List<String> = emptyList()
    ): List<Question> {
        val all = questionsFor(language, categoryId)
        val target = counts.values.sum()
        val chosen = LinkedHashMap<String, Question>()

        counts.forEach { (difficulty, want) ->
            val bucket = all.filter { it.difficulty == difficulty }
            pickFromRecencyPool(
                candidates = bucket.filter { it.id !in chosen },
                excludeIds = excludeIds,
                count = want
            ).forEach { chosen[it.id] = it }
        }

        if (chosen.size < target) {
            val remaining = all.filter { it.id !in chosen }
            pickFromRecencyPool(remaining, excludeIds, target - chosen.size)
                .forEach { chosen[it.id] = it }
        }

        return chosen.values.shuffled().map { it.shuffledOptions() }
    }

    /**
     * Builds an ordered ramp for the Millionaire mode: [easy] easy, then [medium] medium, then
     * [hard] hard questions. [categoryId] `null` draws from every category. If a difficulty bucket
     * is short, the shortfall is filled from any remaining questions so the run always has the full
     * [easy] + [medium] + [hard] count. Options are shuffled; question order (the ramp) is kept.
     */
    fun getMillionaireQuestions(
        categoryId: String?,
        language: AppLanguage = AppLanguage.RU,
        easy: Int = 5,
        medium: Int = 5,
        hard: Int = 5
    ): List<Question> {
        val all = if (categoryId == null) {
            getCategories().flatMap { questionsFor(language, it.id) }
        } else {
            questionsFor(language, categoryId)
        }
        val chosen = LinkedHashSet<String>()
        val result = mutableListOf<Question>()
        fun pick(difficulty: Difficulty, count: Int) {
            all.filter { it.difficulty == difficulty && it.id !in chosen }
                .shuffled()
                .take(count)
                .forEach { chosen.add(it.id); result.add(it) }
        }
        pick(Difficulty.EASY, easy)
        pick(Difficulty.MEDIUM, medium)
        pick(Difficulty.HARD, hard)
        val target = easy + medium + hard
        if (result.size < target) {
            all.filter { it.id !in chosen }
                .shuffled()
                .take(target - result.size)
                .forEach { chosen.add(it.id); result.add(it) }
        }
        return result.map { it.shuffledOptions() }
    }

    /**
     * Orders candidates so never-seen and oldest-seen come first, then takes a random
     * slice from the freshest portion of that ordering.
     */
    private fun buildRecencyPool(
        candidates: List<Question>,
        excludeIds: List<String>,
        quizSize: Int
    ): List<Question> {
        if (candidates.isEmpty()) return emptyList()
        val recentWindow = excludeIds.takeLast(RECENT_WINDOW).toSet()
        val fresh = candidates.filter { it.id !in recentWindow }
        val preferred = if (fresh.size >= quizSize) fresh else {
            candidates.filter { it.id !in excludeIds.takeLast(quizSize * RECENT_QUIZ_MULTIPLIER).toSet() }
                .ifEmpty { candidates }
        }
        // History is oldest-first, so a higher index means seen more recently. Sorting the rank
        // ascending (never-seen = -1) puts never-seen first, then seen-longest-ago, then recent.
        val rankById = HashMap<String, Int>(excludeIds.size)
        excludeIds.forEachIndexed { index, id -> rankById[id] = index }
        val ordered = preferred.sortedWith(
            compareBy<Question> { rankById[it.id] ?: -1 }
                .thenBy { if (it.id.startsWith(IMPORTED_ID_PREFIX)) 1 else 0 }
        )
        val poolSize = (quizSize * POOL_MULTIPLIER).coerceAtMost(ordered.size).coerceAtLeast(quizSize)
        return ordered.take(poolSize)
    }

    private fun pickFromRecencyPool(
        candidates: List<Question>,
        excludeIds: List<String>,
        count: Int
    ): List<Question> =
        buildRecencyPool(candidates, excludeIds, count).shuffled().take(count)

    companion object {
        const val BANK_SIZE = 400
        const val QUIZ_SIZE = 10

        /** Strongly deprioritize questions from the last ~5 quizzes. */
        const val RECENT_WINDOW = 50

        /** Wider soft window before falling back to full pool. */
        const val RECENT_QUIZ_MULTIPLIER = 3

        /** Draw from the freshest slice of the ordered pool. */
        const val POOL_MULTIPLIER = 4

        /** Prefix for questions imported from Open Trivia DB (EN banks). */
        const val IMPORTED_ID_PREFIX = "otdb_"

        private val questionsCache = java.util.concurrent.ConcurrentHashMap<String, List<Question>>()
    }
}