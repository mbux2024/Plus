package com.homeflix.tv.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.homeflix.tv.HomeFlixTVApplication
import com.homeflix.tv.data.model.MediaType
import com.homeflix.tv.di.AppContainer
import com.homeflix.tv.presentation.components.LoadingIndicator
import com.homeflix.tv.presentation.screens.home.NetflixHomeScreen
import com.homeflix.tv.presentation.screens.player.PlayerScreen
import com.homeflix.tv.presentation.screens.player.PlayerViewModel
import com.homeflix.tv.presentation.screens.settings.SettingsScreen
import com.homeflix.tv.presentation.screens.settings.SettingsViewModel

@UnstableApi
@Composable
fun HomeFlixNavigation(container: AppContainer? = null) {
    val navController = rememberNavController()

    // Get container from Application if not provided
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as HomeFlixTVApplication
    val appContainer = container ?: app.container

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            NetflixHomeScreen(navController = navController)
        }

        composable(Screen.Browse.route) {
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

        // ─── SETTINGS (Full NuvioTV-style settings from MyBuild) ─────────
        composable(Screen.Settings.route) {
            val vm: SettingsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        SettingsViewModel(
                            settings = appContainer.settingsRepository,
                            addons = appContainer.addonRepository,
                            trakt = appContainer.traktAuthRepository
                        )
                    }
                }
            )
            SettingsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        // ─── TV Series ───────────────────────────────────────────────────
        composable(
            route = Screen.TvSeriesDetails.route,
            arguments = Screen.TvSeriesDetails.arguments
        ) { entry ->
            val seriesId = entry.arguments?.getString("seriesId") ?: ""
            com.homeflix.tv.presentation.screens.tvshows.TvSeriesDetailsScreen(
                seriesId = seriesId,
                navController = navController
            )
        }

        composable(
            route = Screen.TvSeriesSeason.route,
            arguments = Screen.TvSeriesSeason.arguments
        ) { entry ->
            val seriesId = entry.arguments?.getString("seriesId") ?: ""
            val seasonNumber = entry.arguments?.getInt("seasonNumber") ?: 1
            com.homeflix.tv.presentation.screens.tvshows.TvSeriesSeasonScreen(
                seriesId = seriesId,
                seasonNumber = seasonNumber,
                navController = navController
            )
        }

        // ─── PLAYER (resolves streams via TorBox/RD) ─────────────────────
        composable(
            route = Routes.PLAYER,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("id") { type = NavType.IntType },
                navArgument("season") { type = NavType.IntType },
                navArgument("episode") { type = NavType.IntType },
                navArgument("title") { type = NavType.StringType; defaultValue = "" },
                navArgument("stream") { type = NavType.StringType; defaultValue = "" },
                navArgument("poster") { type = NavType.StringType; defaultValue = "" },
                navArgument("backdrop") { type = NavType.StringType; defaultValue = "" },
                navArgument("hash") { type = NavType.StringType; defaultValue = "" },
                navArgument("debrid") { type = NavType.StringType; defaultValue = "" }
            )
        ) { entry ->
            val mediaType = MediaType.from(entry.arguments?.getString("type"))
            val id = entry.arguments?.getInt("id") ?: 0
            val season = entry.arguments?.getInt("season") ?: -1
            val episode = entry.arguments?.getInt("episode") ?: -1
            val title = entry.arguments?.getString("title").orEmpty()
            val streamUrl = entry.arguments?.getString("stream").orEmpty()
            val poster = entry.arguments?.getString("poster").orEmpty()
            val backdrop = entry.arguments?.getString("backdrop").orEmpty()
            val hash = entry.arguments?.getString("hash").orEmpty()
            val debrid = entry.arguments?.getString("debrid").orEmpty()

            val vm: PlayerViewModel = viewModel(
                key = "player_${mediaType}${id}_${season}_${episode}_${streamUrl.hashCode()}_${hash.hashCode()}",
                factory = viewModelFactory {
                    initializer {
                        PlayerViewModel(
                            tmdb = appContainer.tmdbRepository,
                            stream = appContainer.streamRepository,
                            progress = appContainer.progressRepository,
                            settings = appContainer.settingsRepository,
                            subtitles = appContainer.subtitleRepository,
                            type = mediaType,
                            id = id,
                            season = season,
                            episode = episode,
                            title = title,
                            posterUrl = poster,
                            backdropUrl = backdrop,
                            directUrl = streamUrl,
                            directHash = hash,
                            directDebrid = debrid
                        )
                    }
                }
            )
            PlayerScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onNextEpisode = if (mediaType == MediaType.TV && episode > 0) {
                    {
                        navController.navigate(
                            Routes.player(mediaType, id, season, episode + 1, "", "", poster, backdrop)
                        )
                    }
                } else null
            )
        }

        // ─── DETAIL PAGE (with source picker + play button) ──────────────
        composable(
            route = Routes.DETAIL,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("id") { type = NavType.IntType }
            )
        ) { entry ->
            val mediaType = MediaType.from(entry.arguments?.getString("type"))
            val id = entry.arguments?.getInt("id") ?: 0

            val vm: com.homeflix.tv.presentation.screens.detail.DetailViewModel = viewModel(
                key = "detail_${mediaType}${id}",
                factory = viewModelFactory {
                    initializer {
                        com.homeflix.tv.presentation.screens.detail.DetailViewModel(
                            repo = appContainer.tmdbRepository,
                            streams = appContainer.streamRepository,
                            myList = appContainer.myListRepository,
                            omdb = appContainer.omdbRepository,
                            mdblist = appContainer.mdbListRepository,
                            watchedRepo = appContainer.watchedRepository,
                            settings = appContainer.settingsRepository,
                            badges = appContainer.badgeRepository,
                            type = mediaType,
                            id = id
                        )
                    }
                }
            )
            com.homeflix.tv.presentation.screens.detail.DetailScreen(
                viewModel = vm,
                onPlayAuto = { season, episode, titleStr, posterStr, backdropStr ->
                    navController.navigate(
                        Routes.player(mediaType, id, season, episode, titleStr, "", posterStr, backdropStr)
                    )
                },
                onPlayStream = { titleStr, url, hashStr, posterStr, backdropStr, debridStr ->
                    navController.navigate(
                        Routes.player(
                            type = mediaType, id = id, season = -1, episode = -1,
                            title = titleStr, streamUrl = url, posterUrl = posterStr,
                            backdropUrl = backdropStr, hash = hashStr, debrid = debridStr
                        )
                    )
                },
                onSelectRelated = { item ->
                    navController.navigate(Routes.detail(item.type, item.id))
                },
                onOpenPerson = { /* TODO: person screen */ },
                onOpenTrailer = { /* TODO: trailer screen */ },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// ─── Legacy Screen routes (used by existing home/browse/etc. screens) ────────

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Browse : Screen("browse")
    object Search : Screen("search")
    object TvShows : Screen("tv-shows")
    object MyList : Screen("my-list")
    object Settings : Screen("settings")

    object TvSeriesDetails : Screen("tv-series/{seriesId}") {
        fun createRoute(seriesId: String) = "tv-series/$seriesId"
        val arguments = listOf(navArgument("seriesId") { type = NavType.StringType })
    }
    object TvSeriesSeason : Screen("tv-series/{seriesId}/season/{seasonNumber}") {
        fun createRoute(seriesId: String, seasonNumber: Int) = "tv-series/$seriesId/season/$seasonNumber"
        val arguments = listOf(
            navArgument("seriesId") { type = NavType.StringType },
            navArgument("seasonNumber") { type = NavType.IntType }
        )
    }
    object Details : Screen(Routes.DETAIL) {
        fun createRoute(tmdbId: Int, mediaType: String = "movie") = Routes.detail(MediaType.from(mediaType), tmdbId)
        fun createRoute(mediaId: String) = Routes.detail(MediaType.MOVIE, mediaId.toIntOrNull() ?: 0)
    }
    object VideoPlayer : Screen(Routes.PLAYER) {
        fun createRoute(mediaId: Int, startTime: Long = 0L, forceStartFromBeginning: Boolean = false) =
            Routes.player(MediaType.MOVIE, mediaId)
        fun createRoute(streamUrl: String, title: String = "", tmdbId: Int = 0) =
            Routes.player(MediaType.MOVIE, tmdbId, title = title, streamUrl = streamUrl)
    }
}
