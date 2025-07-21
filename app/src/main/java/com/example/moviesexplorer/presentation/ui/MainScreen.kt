package com.example.moviesexplorer.presentation.ui

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moviesexplorer.data.datastore.FavoritesDataStore
import com.example.moviesexplorer.data.repository.MovieRepository
import com.example.moviesexplorer.presentation.movie.MovieViewModel
import com.example.moviesexplorer.presentation.movie.MovieViewModelFactory
import com.example.moviesexplorer.presentation.theme.ThemeViewModel
import com.example.moviesexplorer.ui.theme.MoviesExplorerTheme

@Composable
fun MainScreen(context: Context) {
    val themeViewModel: ThemeViewModel = viewModel()
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    MoviesExplorerTheme(darkTheme = isDarkTheme) {
        // Create repository and datastore instances
        val repository = remember { MovieRepository() }
        val dataStore = remember { FavoritesDataStore(context) }

        // Pass dependencies to the factory
        val viewModel: MovieViewModel = viewModel(
            factory = MovieViewModelFactory(repository, dataStore)
        )
        val navController = rememberNavController()

        Scaffold(
            bottomBar = { BottomNavigationBar(navController = navController) }
        ) { padding ->
            AppNavigation(
                navController = navController,
                viewModel = viewModel,
                themeViewModel = themeViewModel,
                padding = padding
            )
        }
    }
}


@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MovieViewModel,
    themeViewModel: ThemeViewModel,
    padding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(padding)
    ) {
        composable("home") {
            MovieExplorerScreen(
                viewModel = viewModel,
                onMovieClick = { movie ->
                    navController.navigate("details/${movie.id}")
                }
            )
        }
        composable("favorites") {
            FavoritesScreen(
                viewModel = viewModel,
                onMovieClick = { movie ->
                    navController.navigate("details/${movie.id}")
                }
            )
        }
        composable("settings") {
            SettingsScreen(themeViewModel)
        }
        composable(
            "details/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
            MovieDetailsScreen(movieId = movieId, viewModel = viewModel, navController = navController)
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf( "home" to Icons.Filled.Home,
        "favorites" to Icons.Filled.Favorite,
        "settings" to Icons.Filled.Settings)
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        items.forEach { (route, icon) ->
            NavigationBarItem(
                selected = currentDestination == route,
                onClick = {
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo("home") { inclusive = false }
                    }
                },
                icon = { Icon(icon, contentDescription = route.capitalize()) },
                label = { Text(route.capitalize()) }
            )
        }
    }
}