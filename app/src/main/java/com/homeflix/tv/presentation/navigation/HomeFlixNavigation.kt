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
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { NetflixHomeScreen(navController = navController) }
        composable(Screen.Browse.route) { com.homeflix.tv.presentation.screens.browse.BrowseScreen(navController = navController) }
        composable(Screen.Search.route) { com.homeflix.tv.presentation.screens.search.SearchScreen(navController = navController) }
        composable(Screen.TvShows.route) { com.homeflix.tv.presentation.screens.tvshows.TvShowsScreen(navController = navController) }
        composable(Screen.MyList.route) { com.homeflix.tv.presentation.screens.mylist.MyListScreen(navController = navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController = navController) }
        composable(route = Screen.TvSeriesDetails.route, arguments = Screen.TvSeriesDetails.arguments) { entry ->
            val seriesId = entry.arguments?.getString("seriesId") ?: ""
            com.homeflix.tv.presentation.screens.tvshows.TvSeriesDetailsScreen(seriesId = seriesId, navController = navController)
        }
        composable(route = Screen.TvSeriesSeason.route, arguments = Screen.TvSeriesSeason.arguments) { entry ->
            val seriesId = entry.arguments?.getString("seriesId") ?: ""
            val seasonNumber = entry.arguments?.getInt("seasonNumber") ?: 1
            com.homeflix.tv.presentation.screens.tvshows.TvSeriesSeasonScreen(seriesId = seriesId, seasonNumber = seasonNumber, navController = navController)
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

    object TvSeriesDetails : Screen("tv-series/{seriesId}") {
        fun createRoute(seriesId: String) = "tv-series/$seriesId"
        val arguments = listOf(androidx.navigation.navArgument("seriesId") { type = androidx.navigation.NavType.StringType })
    }
    object TvSeriesSeason : Screen("tv-series/{seriesId}/season/{seasonNumber}") {
        fun createRoute(seriesId: String, seasonNumber: Int) = "tv-series/$seriesId/season/$seasonNumber"
        val arguments = listOf(
            androidx.navigation.navArgument("seriesId") { type = androidx.navigation.NavType.StringType },
            androidx.navigation.navArgument("seasonNumber") { type = androidx.navigation.NavType.IntType }
        )
    }
    object Details : Screen("details/{tmdbId}/{mediaType}") {
        fun createRoute(tmdbId: Int, mediaType: String = "MOVIE") = "details/$tmdbId/$mediaType"
        fun createRoute(mediaId: String) = "details/${mediaId.toIntOrNull() ?: 0}/MOVIE"
        val arguments = listOf(
            androidx.navigation.navArgument("tmdbId") { type = androidx.navigation.NavType.IntType },
            androidx.navigation.navArgument("mediaType") { type = androidx.navigation.NavType.StringType; defaultValue = "MOVIE" }
        )
    }
    object VideoPlayer : Screen("player/{mediaId}") {
        fun createRoute(mediaId: Int, startTime: Long = 0L, forceStartFromBeginning: Boolean = false) = "player/$mediaId"
        fun createRoute(streamUrl: String, title: String = "", tmdbId: Int = 0) = "player/$tmdbId"
        val arguments = listOf(androidx.navigation.navArgument("mediaId") { type = androidx.navigation.NavType.IntType; defaultValue = 0 })
    }
}
