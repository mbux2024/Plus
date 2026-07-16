package com.homeflix.tv.domain.model

/**
 * App settings stored in DataStore. Organized into NuvioTV-style categories:
 * - Playback (engine, HW decode, quality, autoplay, subtitle prefs)
 * - Content Discovery (TMDB key, enrichment toggles)
 * - Integration (TorBox/RD keys, MDBList, OMDb)
 * - Add-ons (stream source mode, installed addons)
 * - About
 */
data class AppSettings(
    // ─── Playback ─────────────────────────────────────────────────────────
    val playerEngine: PlayerEngine = PlayerEngine.MPV,
    val hardwareDecoding: Boolean = true,
    val preferredQuality: PreferredQuality = PreferredQuality.BEST_AVAILABLE,
    val preferredAudioLanguage: String = "en",   // ISO 639-1
    val preferredSubtitleLanguage: String = "en",
    val subtitleSize: SubtitleSize = SubtitleSize.MEDIUM,
    val autoplayNextEpisode: Boolean = true,
    val skipIntroEnabled: Boolean = true,

    // ─── Content Discovery ────────────────────────────────────────────────
    val tmdbApiKey: String = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJmN2MwZGJkMjg0NzI1NGRlMmExYmE5ODQ0MDYwZGQ4NCIsIm5iZiI6MTc3ODc5NDkwMy43NjcsInN1YiI6IjZhMDY0MTk3MWJkYmI1OTNkNzIwMjI0MiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.HydrwkZvHc2OIXbCqnqe9sNhHsLt_FXhf98VqI7j4gQ",
    val tmdbEnrichArtwork: Boolean = true,
    val tmdbEnrichBasicInfo: Boolean = true,
    val tmdbEnrichDetails: Boolean = true,
    val tmdbEnrichCast: Boolean = true,
    val tmdbEnrichTrailers: Boolean = true,
    val tmdbEnrichMoreLikeThis: Boolean = true,
    val tmdbEnrichCollections: Boolean = true,

    // ─── Integration ──────────────────────────────────────────────────────
    val torboxApiKey: String = "",
    val realDebridApiKey: String = "",
    val mdbListApiKey: String = "",
    val omdbApiKey: String = "",
    // MDBList rating provider toggles
    val showImdbRating: Boolean = true,
    val showRottenTomatoes: Boolean = true,
    val showAudienceScore: Boolean = true,
    val showMetacritic: Boolean = true,
    val showTmdbRating: Boolean = true,
    val showTraktRating: Boolean = true,
    val showLetterboxdRating: Boolean = true,

    // ─── Add-ons ──────────────────────────────────────────────────────────
    val streamSourceMode: StreamSourceMode = StreamSourceMode.TORRENTIO_DEBRID,
    val customAddonUrl: String = "", // For Comet custom URL mode
    val subtitleAddonUrl: String = "https://opensubtitles-v3.strem.io" // Default OpenSubtitles v3
) {
    companion object {
        /** TMDB v4 Read Access Token (Bearer auth). Works out of the box. */
        const val DEFAULT_TMDB_API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJmN2MwZGJkMjg0NzI1NGRlMmExYmE5ODQ0MDYwZGQ4NCIsIm5iZiI6MTc3ODc5NDkwMy43NjcsInN1YiI6IjZhMDY0MTk3MWJkYmI1OTNkNzIwMjI0MiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.HydrwkZvHc2OIXbCqnqe9sNhHsLt_FXhf98VqI7j4gQ"
    }
}

enum class PlayerEngine(val label: String) {
    MPV("MPV (Recommended)"),
    EXOPLAYER("ExoPlayer");
}

enum class PreferredQuality(val label: String) {
    BEST_AVAILABLE("Best Available"),
    UHD_4K("4K UHD"),
    FHD_1080P("1080p"),
    HD_720P("720p");
}

enum class SubtitleSize(val label: String, val scaleFactor: Float) {
    SMALL("Small", 0.75f),
    MEDIUM("Medium", 1.0f),
    LARGE("Large", 1.5f);
}

enum class StreamSourceMode(val label: String, val description: String) {
    TORRENTIO_DEBRID(
        "Torrentio + Debrid",
        "Enter your TorBox and/or Real-Debrid key. Plays whichever has an instant cached copy."
    ),
    CUSTOM_ADDON(
        "Custom (Comet) URL",
        "Paste a fully-configured addon URL."
    );
}
