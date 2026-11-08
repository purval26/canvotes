package com.canvotes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.canvotes.CanvotesApplication
import com.canvotes.feature.canvas.CanvasScreen
import com.canvotes.feature.canvas.CanvasViewModel
import com.canvotes.feature.canvas.CanvasViewModelFactory
import com.canvotes.feature.home.HomeScreen
import com.canvotes.feature.home.HomeViewModel
import com.canvotes.feature.home.HomeViewModelFactory
import com.canvotes.feature.notebook.NotebookDetailScreen
import com.canvotes.feature.notebook.NotebookDetailViewModel
import com.canvotes.feature.notebook.NotebookDetailViewModelFactory
import com.canvotes.feature.settings.SettingsScreen
import com.canvotes.feature.settings.SettingsViewModel
import com.canvotes.feature.settings.SettingsViewModelFactory

@Composable
fun CanvotesNavHost(
    app: CanvotesApplication,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(app.notebookRepository)
            )
            HomeScreen(
                viewModel = homeViewModel,
                onOpenNotebook = { notebookId ->
                    navController.navigate(Screen.NotebookDetail.createRoute(notebookId))
                },
                onOpenSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.NotebookDetail.route,
            arguments = listOf(navArgument("notebookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val notebookId = backStackEntry.arguments?.getString("notebookId") ?: return@composable
            val notebookDetailViewModel: NotebookDetailViewModel = viewModel(
                factory = NotebookDetailViewModelFactory(notebookId, app.notebookRepository)
            )
            NotebookDetailScreen(
                viewModel = notebookDetailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOpenPage = { nbId, pageId ->
                    navController.navigate(Screen.Canvas.createRoute(nbId, pageId))
                }
            )
        }

        composable(
            route = Screen.Canvas.route,
            arguments = listOf(
                navArgument("notebookId") { type = NavType.StringType },
                navArgument("pageId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val notebookId = backStackEntry.arguments?.getString("notebookId") ?: return@composable
            val pageId = backStackEntry.arguments?.getString("pageId") ?: return@composable

            val canvasViewModel: CanvasViewModel = viewModel(
                key = "$notebookId-$pageId",
                factory = CanvasViewModelFactory(
                    notebookId = notebookId,
                    initialPageId = pageId,
                    notebookRepository = app.notebookRepository,
                    canvasRepository = app.canvasRepository,
                    preferencesRepository = app.userPreferencesRepository
                )
            )

            CanvasScreen(
                viewModel = canvasViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(app.userPreferencesRepository)
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
