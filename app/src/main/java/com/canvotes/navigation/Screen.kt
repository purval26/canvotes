package com.canvotes.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")

    data object NotebookDetail : Screen("notebook/{notebookId}") {
        fun createRoute(notebookId: String) = "notebook/$notebookId"
    }

    data object Canvas : Screen("canvas/{notebookId}/{pageId}") {
        fun createRoute(notebookId: String, pageId: String) = "canvas/$notebookId/$pageId"
    }

    data object Settings : Screen("settings")
}
