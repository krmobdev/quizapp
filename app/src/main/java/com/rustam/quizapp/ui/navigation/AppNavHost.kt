package com.rustam.quizapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.data.SettingsRepository
import com.rustam.quizapp.ui.screens.onboarding.OnboardingViewModel
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

import com.rustam.quizapp.domain.QuizEventType
import com.rustam.quizapp.ui.screens.onboarding.OnboardingScreen
import com.rustam.quizapp.ui.screens.quiz.QuizScreen
import com.rustam.quizapp.ui.screens.result.ResultScreen

/** Route definitions and helpers for building/parsing the quiz flow destinations. */
object Routes {
    const val GRAPH = "quiz_flow"
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
    const val QUIZ = "quiz/{categoryId}/{difficulty}/{event}/{timeLimit}/{questionCount}/{adaptive}"
    const val RESULT = "result"

    private const val ANY = "ANY"
    private const val NONE = "NONE"

    fun quiz(
        categoryId: String,
        difficulty: Difficulty?,
        event: QuizEventType? = null,
        questionTimeSeconds: Int = 10,
        questionCount: Int = 10,
        adaptive: Boolean = false
    ): String =
        "quiz/$categoryId/${difficulty?.name ?: ANY}/${event?.name ?: NONE}/$questionTimeSeconds/$questionCount/$adaptive"

    fun parseDifficulty(token: String?): Difficulty? =
        if (token == null || token == ANY) null else Difficulty.valueOf(token)

    fun parseEvent(token: String?): QuizEventType? =
        if (token == null || token == NONE) null else QuizEventType.valueOf(token)
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    settingsRepository: SettingsRepository
) {
    val onboardingShown by settingsRepository.onboardingShown.collectAsState(initial = true)
    val startDest = if (onboardingShown) Routes.MAIN else Routes.ONBOARDING

    NavHost(navController = navController, startDestination = Routes.GRAPH) {
        navigation(route = Routes.GRAPH, startDestination = startDest) {

            composable(Routes.ONBOARDING) {
                val onboardingVm: OnboardingViewModel = viewModel()
                OnboardingScreen(
                    onFinish = {
                        onboardingVm.markShown()
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.MAIN) {
                MainShell(
                    onStartQuiz = { categoryId, difficulty, event, timeLimit, questionCount, adaptive ->
                        navController.navigate(
                            Routes.quiz(categoryId, difficulty, event, timeLimit, questionCount, adaptive)
                        )
                    }
                )
            }

            composable(
                route = Routes.QUIZ,
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.StringType },
                    navArgument("difficulty") { type = NavType.StringType },
                    navArgument("event") { type = NavType.StringType },
                    navArgument("timeLimit") { type = NavType.IntType },
                    navArgument("questionCount") { type = NavType.IntType },
                    navArgument("adaptive") { type = NavType.BoolType }
                )
            ) { entry ->
                val shared = entry.sharedViewModel<QuizFlowViewModel>(navController)
                val categoryId = entry.arguments?.getString("categoryId").orEmpty()
                val difficulty = Routes.parseDifficulty(entry.arguments?.getString("difficulty"))
                val event = Routes.parseEvent(entry.arguments?.getString("event"))
                val timeLimit = entry.arguments?.getInt("timeLimit") ?: 10
                val questionCount = entry.arguments?.getInt("questionCount") ?: 10
                val adaptive = entry.arguments?.getBoolean("adaptive") ?: false

                QuizScreen(
                    categoryId = categoryId,
                    difficulty = difficulty,
                    eventType = event,
                    questionTimeSeconds = timeLimit,
                    questionCount = questionCount,
                    adaptive = adaptive,
                    onBack = { navController.popBackToHome() },
                    onFinished = { result ->
                        shared.publishResult(result)
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
                    onHome = {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.MAIN) { inclusive = true }
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
    navigate(Routes.MAIN) {
        popUpTo(Routes.MAIN) { inclusive = false }
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