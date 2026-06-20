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

    /** Parsed questions per language and category, loaded on first access. */
    private val questionsByLanguageAndCategory: MutableMap<String, List<Question>> = mutableMapOf()

    private fun cacheKey(language: AppLanguage, categoryId: String): String =
        "${language.name}:$categoryId"

    private fun questionsFor(language: AppLanguage, categoryId: String): List<Question> =
        questionsByLanguageAndCategory.getOrPut(cacheKey(language, categoryId)) {
            val suffix = if (language == AppLanguage.EN) "_en" else ""
            val fileName = "questions_${categoryId}$suffix.json"
            context.assets.open(fileName).bufferedReader().use { reader ->
                json.decodeFromString<List<Question>>(reader.readText())
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
     * per question.
     */
    fun getQuestions(
        categoryId: String,
        difficulty: Difficulty?,
        language: AppLanguage = AppLanguage.RU,
        bankSize: Int = BANK_SIZE,
        quizSize: Int = QUIZ_SIZE
    ): List<Question> =
        questionsFor(language, categoryId)
            .filter { difficulty == null || it.difficulty == difficulty }
            .shuffled()
            .take(bankSize)
            .shuffled()
            .take(quizSize)
            .map { it.withShuffledOptions() }

    companion object {
        const val BANK_SIZE = 400
        const val QUIZ_SIZE = 10
    }

    private fun Question.withShuffledOptions(): Question {
        val order = options.indices.shuffled()
        return copy(
            options = order.map { options[it] },
            correctIndex = order.indexOf(correctIndex)
        )
    }
}
