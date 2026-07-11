package com.waffensachkunde.trainer.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.waffensachkunde.trainer.ui.bookmarks.BookmarksScreen
import com.waffensachkunde.trainer.ui.exam.ExamIntroScreen
import com.waffensachkunde.trainer.ui.exam.ExamResultScreen
import com.waffensachkunde.trainer.ui.exam.ExamScreen
import com.waffensachkunde.trainer.ui.exam.ExamViewModel
import com.waffensachkunde.trainer.ui.home.HomeScreen
import com.waffensachkunde.trainer.ui.info.InfoScreen
import com.waffensachkunde.trainer.ui.learn.CategoryListScreen
import com.waffensachkunde.trainer.ui.learn.QuizMode
import com.waffensachkunde.trainer.ui.learn.QuizScreen
import com.waffensachkunde.trainer.ui.stats.StatsScreen

private const val ROUTE_HOME = "home"
private const val ROUTE_CATEGORIES = "categories"
private const val ROUTE_QUIZ_CATEGORY = "quiz_category/{categoryId}"
private const val ROUTE_QUIZ_ALL = "quiz_all"
private const val ROUTE_QUIZ_BOOKMARKS = "quiz_bookmarks"
private const val ROUTE_QUIZ_WRONG = "quiz_wrong"
private const val ROUTE_BOOKMARKS = "bookmarks"
private const val ROUTE_STATS = "stats"
private const val ROUTE_INFO = "info"
private const val ROUTE_EXAM_FLOW = "exam_flow"
private const val ROUTE_EXAM_INTRO = "exam_intro"
private const val ROUTE_EXAM_RUN = "exam_run"
private const val ROUTE_EXAM_RESULT = "exam_result"

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_HOME) {
        composable(ROUTE_HOME) {
            HomeScreen(
                onNavigateLearn = { navController.navigate(ROUTE_CATEGORIES) },
                onNavigateExam = { navController.navigate(ROUTE_EXAM_FLOW) },
                onNavigateStats = { navController.navigate(ROUTE_STATS) },
                onNavigateBookmarks = { navController.navigate(ROUTE_BOOKMARKS) },
                onNavigateInfo = { navController.navigate(ROUTE_INFO) }
            )
        }

        composable(ROUTE_CATEGORIES) {
            CategoryListScreen(
                onCategorySelected = { categoryId -> navController.navigate("quiz_category/$categoryId") },
                onLearnAll = { navController.navigate(ROUTE_QUIZ_ALL) }
            )
        }

        composable(
            route = ROUTE_QUIZ_CATEGORY,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { entry ->
            val categoryId = entry.arguments?.getString("categoryId")
            QuizScreen(
                mode = QuizMode.LEARN_CATEGORY,
                categoryId = categoryId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(ROUTE_QUIZ_ALL) {
            QuizScreen(mode = QuizMode.LEARN_ALL, categoryId = null, onBack = { navController.popBackStack() })
        }

        composable(ROUTE_QUIZ_BOOKMARKS) {
            QuizScreen(mode = QuizMode.BOOKMARKS, categoryId = null, onBack = { navController.popBackStack() })
        }

        composable(ROUTE_QUIZ_WRONG) {
            QuizScreen(mode = QuizMode.WRONG_ANSWERS, categoryId = null, onBack = { navController.popBackStack() })
        }

        composable(ROUTE_BOOKMARKS) {
            BookmarksScreen(onStartBookmarkQuiz = { navController.navigate(ROUTE_QUIZ_BOOKMARKS) })
        }

        composable(ROUTE_STATS) {
            StatsScreen()
        }

        composable(ROUTE_INFO) {
            InfoScreen()
        }

        navigation(startDestination = ROUTE_EXAM_INTRO, route = ROUTE_EXAM_FLOW) {
            composable(ROUTE_EXAM_INTRO) { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry(ROUTE_EXAM_FLOW) }
                val examViewModel: ExamViewModel = viewModel(parentEntry)
                ExamIntroScreen(
                    onStart = {
                        examViewModel.startExam()
                        navController.navigate(ROUTE_EXAM_RUN)
                    }
                )
            }
            composable(ROUTE_EXAM_RUN) { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry(ROUTE_EXAM_FLOW) }
                val examViewModel: ExamViewModel = viewModel(parentEntry)
                ExamScreen(
                    viewModel = examViewModel,
                    onSubmitted = {
                        navController.navigate(ROUTE_EXAM_RESULT) {
                            popUpTo(ROUTE_EXAM_RUN) { inclusive = true }
                        }
                    }
                )
            }
            composable(ROUTE_EXAM_RESULT) { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry(ROUTE_EXAM_FLOW) }
                val examViewModel: ExamViewModel = viewModel(parentEntry)
                ExamResultScreen(
                    viewModel = examViewModel,
                    onDone = {
                        examViewModel.reset()
                        navController.navigate(ROUTE_HOME) {
                            popUpTo(ROUTE_HOME) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
