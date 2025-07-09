package com.example.taskmanager.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class AppRoute(val route: String) {
    object Process : AppRoute("process")
    object Resource : AppRoute("resource")
    object FileSystem : AppRoute("file_system")
}


@Composable
fun NavigationView() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = AppRoute.Process.route) {
        listOf(
            AppRoute.Process to @Composable { ProcessView() },
            AppRoute.Resource to @Composable { Text("this is Resource") },
            AppRoute.FileSystem to @Composable { Text("this is FileSystem") }
        ).forEach { (route, content) ->
            composable(route.route) { content() }
        }
    }
}
