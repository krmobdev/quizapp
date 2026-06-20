package com.rustam.quizapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rustam.quizapp.data.Difficulty
import com.rustam.quizapp.ui.screens.home.HomeScreen
import com.rustam.quizapp.ui.screens.quiz.QuizScreen
import com.rustam.quizapp.ui.screens.result.ResultScreen
import com.rustam.quizapp.ui.screens.settings.SettingsScreen
import com.rustam.quizapp.ui.screens.stats.StatsScreen

/** Route definitions and helpers for building/parsing the quiz flow destinations. */
object Routes {
    const val GRAPH = "quiz_flow"
    const val HOME = "home"
    const val QUIZ = "quiz/{categoryId}/{difficulty}"
    const val RESULT = "result"
    const val STATS = "stats"
    const val SETTINGS = "settings"

    private const val ANY = "ANY"

    fun quiz(categoryId: String, difficulty: Difficulty?): String =
        "quiz/$categoryId/${difficulty?.name ?: ANY}"

    fun parseDifficulty(token: String?): Difficulty? =
        if (token == null || token == ANY) null else Difficulty.valueOf(token)
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.GRAPH) {
        navigation(route = Routes.GRAPH, startDestination = Routes.HOME) {

            composable(Routes.HOME) {
                HomeScreen(
                    onStartQuiz = { categoryId, difficulty ->
                        navController.navigate(Routes.quiz(categoryId, difficulty))
                    },
                    onOpenStats = { navController.navigate(Routes.STATS) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }

            composable(Routes.STATS) {
                StatsScreen(onBack = { navController.navigateUp() })
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.navigateUp() })
            }

            composable(
                route = Routes.QUIZ,
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.StringType },
                    navArgument("difficulty") { type = NavType.StringType }
                )
            ) { entry ->
                val shared = entry.sharedViewModel<QuizFlowViewModel>(navController)
                val categoryId = entry.arguments?.getString("categoryId").orEmpty()
                val difficulty = Routes.parseDifficulty(entry.arguments?.getString("difficulty"))
                // Consumed once per quiz entry; null for a normal (non-retry) run.
                val retry = remember { shared.consumeRetryQuestions() }

                QuizScreen(
                    categoryId = categoryId,
                    difficulty = difficulty,
                    retryQuestions = retry,
                    onBack = { navController.popBackToHome() },
                    onFinished = { result ->
                        shared.publishResult(result, categoryId, difficulty)
                        navController.navigate(Routes.RESULT) {
                            popUpTo(Routes.QUIZ) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.RESULT) { entry ->
                val shared = entry.sharedViewModel<QuizFlowViewModel>(navController)
                ResultScreen(
                    result = shared.result,
                    onRetryMistakes = {
                        val mistakes = shared.result?.mistakes.orEmpty()
                        shared.setRetryQuestions(mistakes)
                        navController.navigate(
                            Routes.quiz(shared.categoryId.orEmpty(), shared.difficulty)
                        ) {
                            popUpTo(Routes.RESULT) { inclusive = true }
                        }
                    },
                    onHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Returns to the home screen inside the quiz flow graph.
 * Uses explicit navigation instead of a bare [NavController.popBackStack] so that
 * predictive-back gestures cannot pop past home and finish the activity.
 */
private fun NavController.popBackToHome() {
    navigate(Routes.HOME) {
        popUpTo(Routes.HOME) { inclusive = false }
        launchSingleTop = true
    }
}

/** Obtains a [ViewModel] scoped to the parent nav graph, shared across its destinations. */
@Composable
private inline fun <reified VM : ViewModel> NavBackStackEntry.sharedViewModel(
    navController: NavController
): VM {
    val graphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this) { navController.getBackStackEntry(graphRoute) }
    return viewModel(viewModelStoreOwner = parentEntry)
}
