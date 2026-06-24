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
     * Picks [quizSize] questions from a shuffled pool of up to [bankSize] items in
     * [categoryId], optionally filtered by [difficulty]. Answer options are shuffled
     * per question. Excludes questions in [excludeIds] to minimize repetition.
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
            
        var currentExcludeList = excludeIds
        var filteredQuestions = allQuestions.filter { it.id !in currentExcludeList }
        
        while (filteredQuestions.size < quizSize && currentExcludeList.isNotEmpty()) {
            currentExcludeList = currentExcludeList.drop(1)
            filteredQuestions = allQuestions.filter { it.id !in currentExcludeList }
        }
        
        val pool = if (filteredQuestions.size >= quizSize) filteredQuestions else allQuestions
        
        return pool.shuffled()
            .take(bankSize)
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
        val excluded = excludeIds.toSet()
        val chosen = LinkedHashMap<String, Question>()

        counts.forEach { (difficulty, want) ->
            all.filter { it.difficulty == difficulty && it.id !in excluded && it.id !in chosen }
                .shuffled()
                .take(want)
                .forEach { chosen[it.id] = it }
        }

        val target = counts.values.sum()
        if (chosen.size < target) {
            all.filter { it.id !in chosen && it.id !in excluded }
                .shuffled()
                .take(target - chosen.size)
                .forEach { chosen[it.id] = it }
        }
        // Last resort: reuse recently-seen questions rather than return an undersized quiz.
        if (chosen.size < target) {
            all.filter { it.id !in chosen }
                .shuffled()
                .take(target - chosen.size)
                .forEach { chosen[it.id] = it }
        }

        return chosen.values.shuffled().map { it.shuffledOptions() }
    }

    companion object {
        const val BANK_SIZE = 400
        const val QUIZ_SIZE = 10

        private val questionsCache = java.util.concurrent.ConcurrentHashMap<String, List<Question>>()
    }
}
