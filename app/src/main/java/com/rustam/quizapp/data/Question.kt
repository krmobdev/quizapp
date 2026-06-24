package com.rustam.quizapp.data

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    val category: String,
    val difficulty: Difficulty,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null
) {
    /** Returns a copy with answer options shuffled and correctIndex updated accordingly. */
    fun shuffledOptions(): Question {
        val order = options.indices.shuffled()
        return copy(
            options = order.map { options[it] },
            correctIndex = order.indexOf(correctIndex)
        )
    }
}

enum class Difficulty { EASY, MEDIUM, HARD }
