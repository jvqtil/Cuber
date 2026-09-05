package dev.jvqtil.cuber.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.jvqtil.cuber.CuberViewModel
import dev.jvqtil.cuber.ui.screens.SolveDetailsScreen
import dev.jvqtil.cuber.ui.screens.SolvesScreen
import dev.jvqtil.cuber.ui.screens.TimerScreen

private const val TIMER_ROUTE = "timer"
private const val SOLVES_ROUTE = "solves"
private const val DETAILS_ROUTE = "details/{solveId}"
private const val SOLVE_ID = "solveId"

private const val SWIPE_THRESHOLD = 120f

@Composable
fun CuberNavHost(
    viewModel: CuberViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = TIMER_ROUTE,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        enterTransition = {
            slideInVertically(
                animationSpec = tween(
                    durationMillis = 320,
                    easing = FastOutSlowInEasing
                ),
                initialOffsetY = { it }
            ) + fadeIn(
                animationSpec = tween(220)
            )
        },
        exitTransition = {
            slideOutVertically(
                animationSpec = tween(
                    durationMillis = 280,
                    easing = FastOutSlowInEasing
                ),
                targetOffsetY = { -it / 5 }
            ) + fadeOut(
                animationSpec = tween(180)
            )
        },
        popEnterTransition = {
            slideInVertically(
                animationSpec = tween(
                    durationMillis = 320,
                    easing = FastOutSlowInEasing
                ),
                initialOffsetY = { -it / 5 }
            ) + fadeIn(
                animationSpec = tween(220)
            )
        },
        popExitTransition = {
            slideOutVertically(
                animationSpec = tween(
                    durationMillis = 280,
                    easing = FastOutSlowInEasing
                ),
                targetOffsetY = { it }
            ) + fadeOut(
                animationSpec = tween(180)
            )
        }
    ) {
        composable(TIMER_ROUTE) {
            TimerScreen(
                viewModel = viewModel,
                onOpenSolves = {
                    navController.navigate(SOLVES_ROUTE)
                }
            )
        }

        composable(SOLVES_ROUTE) {
            SwipeBackScreen(
                onBack = {
                    navController.popBackStack()
                }
            ) {
                SolvesScreen(
                    viewModel = viewModel,
                    onOpenSolve = { solveId ->
                        navController.navigate("details/$solveId")
                    }
                )
            }
        }

        composable(
            route = DETAILS_ROUTE,
            arguments = listOf(
                navArgument(SOLVE_ID) {
                    type = NavType.LongType
                }
            )
        ) { entry ->
            val solveId = entry.arguments?.getLong(SOLVE_ID) ?: return@composable

            SolveDetailsScreen(
                solveId = solveId,
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                },
                onDeleted = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun SwipeBackScreen(
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    var dragDistance by remember {
        mutableFloatStateOf(0f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        dragDistance = 0f
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 0f) {
                            dragDistance += dragAmount
                        }
                    },
                    onDragEnd = {
                        if (dragDistance >= SWIPE_THRESHOLD) {
                            onBack()
                        }

                        dragDistance = 0f
                    },
                    onDragCancel = {
                        dragDistance = 0f
                    }
                )
            }
    ) {
        content()
    }
}