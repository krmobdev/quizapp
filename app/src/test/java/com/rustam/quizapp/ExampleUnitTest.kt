package com.rustam.quizapp

import com.rustam.quizapp.data.Question
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun validateJsonAssets() {
        val assetsDir = File("src/main/assets")
        assertTrue("Assets directory should exist: ${assetsDir.absolutePath}", assetsDir.exists())
        val jsonFiles = assetsDir.listFiles { _, name -> name.startsWith("questions_") && name.endsWith(".json") }
        assertNotNull("Should find json files", jsonFiles)
        assertTrue("Should have questions JSON files", jsonFiles!!.isNotEmpty())

        val json = Json { ignoreUnknownKeys = false }

        for (file in jsonFiles) {
            val text = file.readText()
            try {
                val questions = json.decodeFromString<List<Question>>(text)
                assertFalse("Questions list in ${file.name} should not be empty", questions.isEmpty())
                for (q in questions) {
                    val expectedCategory = file.name.substringAfter("questions_").substringBefore("_en").substringBefore(".json")
                    assertEquals("Question ${q.id} in ${file.name} has wrong category", expectedCategory, q.category)
                    assertEquals("Question ${q.id} in ${file.name} should have 4 options", 4, q.options.size)
                    assertEquals("Question ${q.id} in ${file.name} should have correctIndex = 0", 0, q.correctIndex)
                    assertFalse("Question ${q.id} text in ${file.name} should not be blank", q.text.isBlank())
                }
                println("Successfully validated ${file.name} with ${questions.size} questions.")
            } catch (e: Exception) {
                fail("Failed to parse ${file.name}: ${e.message}")
            }
        }
    }

    @Test
    fun validateStatUpgradeCosts() {
        val testCases = mapOf(
            0 to 100,
            1 to 125,
            2 to 150,
            3 to 175,
            4 to 200,
            10 to 350,
            19 to 575
        )
        for ((value, expectedCost) in testCases) {
            val actualCost = 100 + value * 25
            assertEquals("Upgrade cost for level $value should be $expectedCost", expectedCost, actualCost)
        }
    }
}