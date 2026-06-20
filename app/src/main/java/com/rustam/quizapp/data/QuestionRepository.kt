package com.rustam.quizapp.data

import android.content.Context
import kotlinx.serialization.json.Json

/**
 * Loads quiz questions from the app's assets and serves them per category.
 *
 * Each `assets/questions_<categoryId>.json` file is parsed on first use for that
 * category and cached for the lifetime of the repository instance.
 */
class QuestionRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    /** Parsed questions per category, loaded on first access. */
    private val questionsByCategory: MutableMap<String, List<Question>> = mutableMapOf()

    private fun questionsFor(categoryId: String): List<Question> =
        questionsByCategory.getOrPut(categoryId) {
            val fileName = "questions_$categoryId.json"
            context.assets.open(fileName).bufferedReader().use { reader ->
                json.decodeFromString<List<Question>>(reader.readText())
            }
        }

    /** Categories shown on the home screen. Hardcoded for now. */
    fun getCategories(): List<Category> = listOf(
        Category(id = "chemistry", title = "Химия", emoji = "🧪"),
        Category(id = "physics", title = "Физика", emoji = "⚛️"),
        Category(id = "history", title = "История", emoji = "📜"),
        Category(id = "movies", title = "Кино и сериалы", emoji = "🎬"),
        Category(id = "art", title = "Искусство", emoji = "🎨"),
        Category(id = "animals", title = "Животные", emoji = "🐾"),
        Category(id = "geography", title = "География", emoji = "🌍"),
        Category(id = "math", title = "Математика", emoji = "🔢"),
        Category(id = "informatics", title = "Информатика", emoji = "💻"),
        Category(id = "astronomy", title = "Астрономия", emoji = "🔭")
    )

    /**
     * Picks [quizSize] questions from a shuffled pool of up to [bankSize] items in
     * [categoryId], optionally filtered by [difficulty]. Answer options are shuffled
     * per question.
     */
    fun getQuestions(
        categoryId: String,
        difficulty: Difficulty?,
        bankSize: Int = BANK_SIZE,
        quizSize: Int = QUIZ_SIZE
    ): List<Question> =
        questionsFor(categoryId)
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
