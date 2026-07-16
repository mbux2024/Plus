package com.homeflix.tv.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.media3.common.util.UnstableApi
import com.homeflix.tv.presentation.screens.home.NetflixHomeScreen
import com.homeflix.tv.presentation.screens.settings.SettingsScreen

@UnstableApi
@Composable
fun HomeFlixNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            NetflixHomeScreen(navController = navController)
        }

        composable(Screen.Browse.route) {
            // BrowseScreen now shows TMDB movies/TV with pagination
            com.homeflix.tv.presentation.screens.browse.BrowseScreen(navController = navController)
        }

        composable(Screen.Search.route) {
            com.homeflix.tv.presentation.screens.search.SearchScreen(navController = navController)
        }

        composable(Screen.TvShows.route) {
            com.homeflix.tv.presentation.screens.tvshows.TvShowsScreen(navController = navController)
        }

        composable(Screen.MyList.route) {
            com.homeflix.tv.presentation.screens.mylist.MyListScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(
            route = Screen.Details.route,
            arguments = Screen.Details.arguments
        ) { backStackEntry ->
            val tmdbId = backStackEntry.arguments?.getInt("tmdbId") ?: 0
            val mediaType = backStackEntry.arguments?.getString("mediaType") ?: "MOVIE"
            com.homeflix.tv.presentation.screens.details.DetailsScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.VideoPlayer.route,
            arguments = Screen.VideoPlayer.arguments
        ) { backStackEntry ->
            val streamUrl = backStackEntry.arguments?.getString("streamUrl") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val tmdbId = backStackEntry.arguments?.getInt("tmdbId") ?: 0

            com.homeflix.tv.presentation.screens.player.VideoPlayerScreen(
                streamUrl = java.net.URLDecoder.decode(streamUrl, "UTF-8"),
                title = java.net.URLDecoder.decode(title, "UTF-8"),
                tmdbId = tmdbId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Browse : Screen("browse")
    object Search : Screen("search")
    object TvShows : Screen("tv-shows")
    object MyList : Screen("my-list")
    object Settings : Screen("settings")

    object Details : Screen("details/{tmdbId}/{mediaType}") {
        fun createRoute(tmdbId: Int, mediaType: String = "MOVIE") = "details/$tmdbId/$mediaType"
        // Backward compat: old code passed media.id.toString()
        fun createRoute(mediaId: String) = "details/${mediaId.toIntOrNull() ?: 0}/MOVIE"
        val arguments = listOf(
            androidx.navigation.navArgument("tmdbId") {
                type = androidx.navigation.NavType.IntType
            },
            androidx.navigation.navArgument("mediaType") {
                type = androidx.navigation.NavType.StringType
                defaultValue = "MOVIE"
            }
        )
    }

    object VideoPlayer : Screen("player/{streamUrl}/{title}/{tmdbId}") {
        fun createRoute(
            streamUrl: String,
            title: String = "",
            tmdbId: Int = 0
        ): String {
            val encodedUrl = java.net.URLEncoder.encode(streamUrl, "UTF-8")
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
            return "player/$encodedUrl/$encodedTitle/$tmdbId"
        }

        /**
         * Backward compat: old code called createRoute(mediaId: Int, startTime: Long)
         * Now this creates a placeholder route — actual stream resolution happens in DetailsScreen.
         */
        fun createRoute(mediaId: Int, startTime: Long = 0L, forceStartFromBeginning: Boolean = false): String {
            // Navigate to details page instead — streaming requires debrid resolution now
            return "details/$mediaId/MOVIE"
        }

        val arguments = listOf(
            androidx.navigation.navArgument("streamUrl") {
                type = androidx.navigation.NavType.StringType
            },
            androidx.navigation.navArgument("title") {
                type = androidx.navigation.NavType.StringType
                defaultValue = ""
            },
            androidx.navigation.navArgument("tmdbId") {
                type = androidx.navigation.NavType.IntType
                defaultValue = 0
            }
        )
    }
}
