package com.homeflix.tv.di

import android.content.Context
import com.homeflix.tv.data.NetworkModule
import com.homeflix.tv.data.mylist.MyListRepository
import com.homeflix.tv.data.omdb.OmdbRepository
import com.homeflix.tv.data.progress.ProgressRepository
import com.homeflix.tv.data.realdebrid.RealDebridRepository
import com.homeflix.tv.data.settings.SettingsRepository
import com.homeflix.tv.data.stream.StreamRepository
import com.homeflix.tv.data.stream.SubtitleRepository
import com.homeflix.tv.data.tmdb.TmdbRepository
import com.homeflix.tv.data.torbox.TorBoxRepository
import com.homeflix.tv.data.watched.WatchedRepository

/**
 * Manual dependency container. Constructed once in [com.homeflix.tv.HomeFlixTVApplication]
 * and passed down to navigation/ViewModels. Keeps the project free of an
 * annotation-processor DI framework.
 */
class AppContainer(context: Context) {

    val settingsRepository = SettingsRepository(context.applicationContext)

    val progressRepository = ProgressRepository(context.applicationContext)

    val myListRepository = MyListRepository(context.applicationContext)

    val watchedRepository = WatchedRepository(context.applicationContext)

    val searchHistoryRepository =
        com.homeflix.tv.data.search.SearchHistoryRepository(context.applicationContext)

    val tmdbRepository: TmdbRepository by lazy {
        TmdbRepository(
            api = NetworkModule.tmdbApi(settingsRepository),
            settings = settingsRepository
        )
    }

    val torBoxRepository: TorBoxRepository by lazy {
        TorBoxRepository(
            api = NetworkModule.torboxApi(settingsRepository),
            settings = settingsRepository
        )
    }

    val realDebridRepository: RealDebridRepository by lazy {
        RealDebridRepository(
            api = NetworkModule.realDebridApi(),
            settings = settingsRepository
        )
    }

    /** Resolves YouTube trailer ids (from TMDB) to direct streams for the MPV player. */
    val youTubeExtractor: com.homeflix.tv.data.trailer.YouTubeExtractor by lazy {
        com.homeflix.tv.data.trailer.YouTubeExtractor()
    }

    /**
     * Default playback resolver: queries the user's configured Stremio addon(s)
     * and resolves instant TorBox/RD streams. This is what the player uses.
     */
    val streamRepository: StreamRepository by lazy {
        StreamRepository(
            api = NetworkModule.stremioApi(),
            torbox = torBoxRepository,
            realDebrid = realDebridRepository,
            settings = settingsRepository
        )
    }

    val subtitleRepository: SubtitleRepository by lazy {
        SubtitleRepository(
            api = NetworkModule.stremioApi(),
            settings = settingsRepository
        )
    }

    val omdbRepository: OmdbRepository by lazy {
        OmdbRepository(
            api = NetworkModule.omdbApi(),
            settings = settingsRepository
        )
    }

    val mdbListRepository: com.homeflix.tv.data.mdblist.MDBListRepository by lazy {
        com.homeflix.tv.data.mdblist.MDBListRepository(
            api = NetworkModule.mdbListApi(),
            settings = settingsRepository
        )
    }

    val badgeRepository: com.homeflix.tv.data.badges.BadgeRepository by lazy {
        com.homeflix.tv.data.badges.BadgeRepository(settings = settingsRepository)
    }

    /** Google Gemini client (optional AI features; no-op until a key is set). */
    val geminiRepository: com.homeflix.tv.data.gemini.GeminiRepository by lazy {
        com.homeflix.tv.data.gemini.GeminiRepository(
            api = NetworkModule.geminiApi(),
            settings = settingsRepository
        )
    }

    /** AI natural-language search + recommendations (Gemini titles resolved via TMDB). */
    val aiCatalog: com.homeflix.tv.data.gemini.AiCatalog by lazy {
        com.homeflix.tv.data.gemini.AiCatalog(
            gemini = geminiRepository,
            tmdb = tmdbRepository
        )
    }

    private val traktApi: com.homeflix.tv.data.trakt.TraktApi by lazy {
        NetworkModule.traktApi(settingsRepository)
    }

    val traktAuthRepository: com.homeflix.tv.data.trakt.TraktAuthRepository by lazy {
        com.homeflix.tv.data.trakt.TraktAuthRepository(
            api = traktApi,
            settings = settingsRepository
        )
    }

    val traktRepository: com.homeflix.tv.data.trakt.TraktRepository by lazy {
        com.homeflix.tv.data.trakt.TraktRepository(
            api = traktApi,
            auth = traktAuthRepository,
            tmdb = tmdbRepository
        )
    }

    val addonRepository: com.homeflix.tv.data.addons.AddonRepository by lazy {
        com.homeflix.tv.data.addons.AddonRepository(
            api = NetworkModule.addonApi(),
            settings = settingsRepository
        )
    }
}
