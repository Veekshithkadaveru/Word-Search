package app.krafted.wordsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.krafted.wordsearch.data.PuzzleRepository
import app.krafted.wordsearch.data.db.AppDatabase
import app.krafted.wordsearch.ui.CompleteScreen
import app.krafted.wordsearch.ui.GameScreen
import app.krafted.wordsearch.ui.HomeScreen
import app.krafted.wordsearch.ui.PuzzleSelectScreen
import app.krafted.wordsearch.ui.theme.WordSearchTheme
import app.krafted.wordsearch.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordSearchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WordSearchNavHost()
                }
            }
        }
    }
}

@Composable
fun WordSearchNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = remember { PuzzleRepository(context) }
    val dao = remember { AppDatabase.getInstance(context).puzzleDao() }
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(repository, dao)
    )
    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable("splash") {
            PlaceholderScreen("Splash Screen") {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
        composable("home") {
            HomeScreen(
                viewModel = homeViewModel,
                onCategorySelected = { id ->
                    navController.navigate("puzzle_select/$id")
                }
            )
        }
        composable(
            route = "puzzle_select/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 1
            PuzzleSelectScreen(
                categoryId = categoryId,
                viewModel = homeViewModel,
                onPuzzleSelected = { cat, num ->
                    navController.navigate("mode_select/$cat/$num")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "mode_select/{categoryId}/{puzzleNumber}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("puzzleNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 1
            val puzzleNumber = backStackEntry.arguments?.getInt("puzzleNumber") ?: 1
            PlaceholderScreen("Mode Select $categoryId - $puzzleNumber") {
                navController.navigate("game/$categoryId/$puzzleNumber/true")
            }
        }
        composable(
            route = "game/{categoryId}/{puzzleNumber}/{isTimed}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("puzzleNumber") { type = NavType.IntType },
                navArgument("isTimed") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 1
            val puzzleNumber = backStackEntry.arguments?.getInt("puzzleNumber") ?: 1
            val isTimed = backStackEntry.arguments?.getBoolean("isTimed") ?: true
            GameScreen(
                categoryId = categoryId,
                puzzleNumber = puzzleNumber,
                isTimedMode = isTimed,
                repository = repository,
                dao = dao,
                onComplete = { score, time, isNewBest ->
                    navController.navigate(
                        "complete/$categoryId/$puzzleNumber/$score/$time/$isNewBest"
                    ) {
                        popUpTo("game/$categoryId/$puzzleNumber/$isTimed") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "complete/{categoryId}/{puzzleNumber}/{score}/{time}/{isNewBest}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("puzzleNumber") { type = NavType.IntType },
                navArgument("score") { type = NavType.IntType },
                navArgument("time") { type = NavType.IntType },
                navArgument("isNewBest") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 1
            val puzzleNumber = backStackEntry.arguments?.getInt("puzzleNumber") ?: 1
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val time = backStackEntry.arguments?.getInt("time") ?: 0
            val isNewBest = backStackEntry.arguments?.getBoolean("isNewBest") ?: false
            CompleteScreen(
                categoryId = categoryId,
                puzzleNumber = puzzleNumber,
                score = score,
                timeSeconds = time,
                isNewBest = isNewBest,
                repository = repository,
                onNextPuzzle = {
                    navController.navigate("mode_select/$categoryId/${puzzleNumber + 1}") {
                        popUpTo("home")
                    }
                },
                onHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
        composable("leaderboard") {
            PlaceholderScreen("Leaderboard") {
                navController.popBackStack()
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = name, style = MaterialTheme.typography.headlineLarge)
    }
}
