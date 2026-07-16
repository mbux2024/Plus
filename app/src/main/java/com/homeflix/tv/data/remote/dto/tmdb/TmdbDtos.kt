package com.homeflix.tv.data.remote.dto.tmdb

import com.google.gson.annotations.SerializedName
import com.homeflix.tv.domain.model.*
import com.homeflix.tv.domain.repository.ExternalIds

/**
 * TMDB API response DTOs and mappers to domain models.
 */

// ─── Paged Response ──────────────────────────────────────────────────────────

data class TmdbPagedResponse(
    val page: Int = 1,
    val results: List<TmdbMediaDto> = emptyList(),
    @SerializedName("total_pages")
    val totalPages: Int = 0,
    @SerializedName("total_results")
    val totalResults: Int = 0
)

// ─── Media Item (works for both movie and TV) ────────────────────────────────

data class TmdbMediaDto(
    val id: Int,
    val title: String? = null,           // Movie title
    val name: String? = null,            // TV show name
    @SerializedName("original_title")
    val originalTitle: String? = null,
    @SerializedName("original_name")
    val originalName: String? = null,
    @SerializedName("media_type")
    val mediaType: String? = null,       // "movie", "tv", "person"
    val overview: String? = null,
    @SerializedName("poster_path")
    val posterPath: String? = null,
    @SerializedName("backdrop_path")
    val backdropPath: String? = null,
    @SerializedName("release_date")
    val releaseDate: String? = null,
    @SerializedName("first_air_date")
    val firstAirDate: String? = null,
    @SerializedName("vote_average")
    val voteAverage: Double = 0.0,
    @SerializedName("vote_count")
    val voteCount: Int = 0,
    val popularity: Double = 0.0,
    @SerializedName("genre_ids")
    val genreIds: List<Int> = emptyList(),
    val adult: Boolean = false,
    @SerializedName("original_language")
    val originalLanguage: String? = null
)

fun TmdbMediaDto.toDomain(forceType: TmdbMediaType? = null): TmdbMedia? {
    // Skip "person" results from multi search
    if (mediaType == "person") return null

    val resolvedType = forceType ?: when (mediaType) {
        "movie" -> TmdbMediaType.MOVIE
        "tv" -> TmdbMediaType.TV
        else -> {
            // Infer from available fields
            if (title != null || releaseDate != null) TmdbMediaType.MOVIE
            else TmdbMediaType.TV
        }
    }

    return TmdbMedia(
        id = id,
        title = title ?: name ?: "Unknown",
        originalTitle = originalTitle ?: originalName,
        mediaType = resolvedType,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        firstAirDate = firstAirDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        genreIds = genreIds,
        adult = adult,
        originalLanguage = originalLanguage
    )
}

// ─── Movie Detail ────────────────────────────────────────────────────────────

data class TmdbMovieDetailDto(
    val id: Int,
    val title: String,
    @SerializedName("original_title")
    val originalTitle: String? = null,
    val overview: String? = null,
    @SerializedName("poster_path")
    val posterPath: String? = null,
    @SerializedName("backdrop_path")
    val backdropPath: String? = null,
    @SerializedName("release_date")
    val releaseDate: String? = null,
    @SerializedName("vote_average")
    val voteAverage: Double = 0.0,
    @SerializedName("vote_count")
    val voteCount: Int = 0,
    val popularity: Double = 0.0,
    val runtime: Int? = null,
    val tagline: String? = null,
    val status: String? = null,
    val homepage: String? = null,
    @SerializedName("imdb_id")
    val imdbId: String? = null,
    val adult: Boolean = false,
    @SerializedName("original_language")
    val originalLanguage: String? = null,
    val genres: List<TmdbGenreDto> = emptyList(),
    @SerializedName("belongs_to_collection")
    val belongsToCollection: TmdbCollectionDto? = null,
    // Appended responses
    val credits: TmdbCreditsDto? = null,
    val videos: TmdbVideosDto? = null,
    val similar: TmdbPagedResponse? = null,
    val images: TmdbImagesDto? = null
)

fun TmdbMovieDetailDto.toDomain(): TmdbMediaDetail {
    val logoPath = images?.logos?.firstOrNull()?.filePath

    val media = TmdbMedia(
        id = id,
        title = title,
        originalTitle = originalTitle,
        mediaType = TmdbMediaType.MOVIE,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        logoPath = logoPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        genres = genres.map { it.toDomain() },
        genreIds = genres.map { it.id },
        adult = adult,
        originalLanguage = originalLanguage,
        imdbId = imdbId,
        tagline = tagline,
        runtime = runtime,
        status = status,
        homepage = homepage
    )

    return TmdbMediaDetail(
        media = media,
        credits = credits?.toDomain() ?: TmdbCredits(),
        videos = videos?.results?.map { it.toDomain() } ?: emptyList(),
        similar = similar?.results?.mapNotNull { it.toDomain(TmdbMediaType.MOVIE) } ?: emptyList(),
        collection = belongsToCollection?.toDomain()
    )
}

// ─── TV Detail ───────────────────────────────────────────────────────────────

data class TmdbTvDetailDto(
    val id: Int,
    val name: String,
    @SerializedName("original_name")
    val originalName: String? = null,
    val overview: String? = null,
    @SerializedName("poster_path")
    val posterPath: String? = null,
    @SerializedName("backdrop_path")
    val backdropPath: String? = null,
    @SerializedName("first_air_date")
    val firstAirDate: String? = null,
    @SerializedName("vote_average")
    val voteAverage: Double = 0.0,
    @SerializedName("vote_count")
    val voteCount: Int = 0,
    val popularity: Double = 0.0,
    val tagline: String? = null,
    val status: String? = null,
    val homepage: String? = null,
    @SerializedName("number_of_seasons")
    val numberOfSeasons: Int? = null,
    @SerializedName("number_of_episodes")
    val numberOfEpisodes: Int? = null,
    @SerializedName("original_language")
    val originalLanguage: String? = null,
    val genres: List<TmdbGenreDto> = emptyList(),
    val seasons: List<TmdbSeasonDto> = emptyList(),
    // Appended responses
    val credits: TmdbCreditsDto? = null,
    val videos: TmdbVideosDto? = null,
    val similar: TmdbPagedResponse? = null,
    val images: TmdbImagesDto? = null
)

fun TmdbTvDetailDto.toDomain(): TmdbMediaDetail {
    val logoPath = images?.logos?.firstOrNull()?.filePath

    val media = TmdbMedia(
        id = id,
        title = name,
        originalTitle = originalName,
        mediaType = TmdbMediaType.TV,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        logoPath = logoPath,
        firstAirDate = firstAirDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        genres = genres.map { it.toDomain() },
        genreIds = genres.map { it.id },
        originalLanguage = originalLanguage,
        tagline = tagline,
        status = status,
        numberOfSeasons = numberOfSeasons,
        numberOfEpisodes = numberOfEpisodes,
        homepage = homepage
    )

    return TmdbMediaDetail(
        media = media,
        credits = credits?.toDomain() ?: TmdbCredits(),
        videos = videos?.results?.map { it.toDomain() } ?: emptyList(),
        seasons = seasons.filter { it.seasonNumber > 0 }.map { it.toDomain() }, // Skip specials (S0)
        similar = similar?.results?.mapNotNull { it.toDomain(TmdbMediaType.TV) } ?: emptyList()
    )
}

// ─── Season Detail ───────────────────────────────────────────────────────────

data class TmdbSeasonDetailDto(
    val id: Int,
    @SerializedName("season_number")
    val seasonNumber: Int,
    val name: String? = null,
    val overview: String? = null,
    @SerializedName("poster_path")
    val posterPath: String? = null,
    @SerializedName("air_date")
    val airDate: String? = null,
    val episodes: List<TmdbEpisodeDto> = emptyList()
)

data class TmdbEpisodeDto(
    val id: Int,
    @SerializedName("episode_number")
    val episodeNumber: Int,
    @SerializedName("season_number")
    val seasonNumber: Int,
    val name: String? = null,
    val overview: String? = null,
    @SerializedName("still_path")
    val stillPath: String? = null,
    @SerializedName("air_date")
    val airDate: String? = null,
    val runtime: Int? = null,
    @SerializedName("vote_average")
    val voteAverage: Double = 0.0
)

fun TmdbEpisodeDto.toDomain(): TmdbEpisode = TmdbEpisode(
    id = id,
    episodeNumber = episodeNumber,
    seasonNumber = seasonNumber,
    name = name ?: "Episode $episodeNumber",
    overview = overview,
    stillPath = stillPath,
    airDate = airDate,
    runtime = runtime,
    voteAverage = voteAverage
)

// ─── Genres ──────────────────────────────────────────────────────────────────

data class TmdbGenreListDto(
    val genres: List<TmdbGenreDto> = emptyList()
)

data class TmdbGenreDto(
    val id: Int,
    val name: String
)

fun TmdbGenreDto.toDomain(): TmdbGenre = TmdbGenre(id = id, name = name)

// ─── Credits ─────────────────────────────────────────────────────────────────

data class TmdbCreditsDto(
    val cast: List<TmdbCastDto> = emptyList(),
    val crew: List<TmdbCrewDto> = emptyList()
)

data class TmdbCastDto(
    val id: Int,
    val name: String,
    val character: String? = null,
    @SerializedName("profile_path")
    val profilePath: String? = null,
    val order: Int = 0
)

data class TmdbCrewDto(
    val id: Int,
    val name: String,
    @SerializedName("profile_path")
    val profilePath: String? = null,
    val department: String? = null,
    val job: String? = null
)

fun TmdbCreditsDto.toDomain(): TmdbCredits = TmdbCredits(
    cast = cast.map { castDto ->
        TmdbCastMember(
            id = castDto.id,
            name = castDto.name,
            character = castDto.character,
            profilePath = castDto.profilePath,
            order = castDto.order
        )
    },
    crew = crew.map { crewDto ->
        TmdbCastMember(
            id = crewDto.id,
            name = crewDto.name,
            profilePath = crewDto.profilePath,
            department = crewDto.department,
            job = crewDto.job
        )
    }
)

// ─── Videos ──────────────────────────────────────────────────────────────────

data class TmdbVideosDto(
    val results: List<TmdbVideoDto> = emptyList()
)

data class TmdbVideoDto(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val official: Boolean = false
)

fun TmdbVideoDto.toDomain(): TmdbVideo = TmdbVideo(
    id = id,
    key = key,
    name = name,
    site = site,
    type = type,
    official = official
)

// ─── Seasons ─────────────────────────────────────────────────────────────────

data class TmdbSeasonDto(
    val id: Int,
    @SerializedName("season_number")
    val seasonNumber: Int,
    val name: String? = null,
    val overview: String? = null,
    @SerializedName("poster_path")
    val posterPath: String? = null,
    @SerializedName("air_date")
    val airDate: String? = null,
    @SerializedName("episode_count")
    val episodeCount: Int = 0
)

fun TmdbSeasonDto.toDomain(): TmdbSeason = TmdbSeason(
    id = id,
    seasonNumber = seasonNumber,
    name = name ?: "Season $seasonNumber",
    overview = overview,
    posterPath = posterPath,
    airDate = airDate,
    episodeCount = episodeCount
)

// ─── External IDs ────────────────────────────────────────────────────────────

data class TmdbExternalIdsDto(
    @SerializedName("imdb_id")
    val imdbId: String? = null,
    @SerializedName("tvdb_id")
    val tvdbId: Int? = null,
    @SerializedName("facebook_id")
    val facebookId: String? = null,
    @SerializedName("instagram_id")
    val instagramId: String? = null,
    @SerializedName("twitter_id")
    val twitterId: String? = null
)

fun TmdbExternalIdsDto.toDomain(): ExternalIds = ExternalIds(
    imdbId = imdbId,
    tvdbId = tvdbId,
    facebookId = facebookId,
    instagramId = instagramId,
    twitterId = twitterId
)

// ─── Watch Providers ─────────────────────────────────────────────────────────

data class TmdbWatchProviderListDto(
    val results: List<TmdbWatchProviderDto> = emptyList()
)

data class TmdbWatchProviderDto(
    @SerializedName("provider_id")
    val providerId: Int,
    @SerializedName("provider_name")
    val providerName: String,
    @SerializedName("logo_path")
    val logoPath: String? = null,
    @SerializedName("display_priority")
    val displayPriority: Int = 0
)

fun TmdbWatchProviderDto.toDomain(): TmdbWatchProvider = TmdbWatchProvider(
    providerId = providerId,
    providerName = providerName,
    logoPath = logoPath,
    displayPriority = displayPriority
)

// ─── Images ──────────────────────────────────────────────────────────────────

data class TmdbImagesDto(
    val logos: List<TmdbImageDto> = emptyList(),
    val backdrops: List<TmdbImageDto> = emptyList(),
    val posters: List<TmdbImageDto> = emptyList()
)

data class TmdbImageDto(
    @SerializedName("file_path")
    val filePath: String,
    val width: Int = 0,
    val height: Int = 0,
    @SerializedName("vote_average")
    val voteAverage: Double = 0.0,
    @SerializedName("iso_639_1")
    val language: String? = null
)

// ─── Collection ──────────────────────────────────────────────────────────────

data class TmdbCollectionDto(
    val id: Int,
    val name: String,
    @SerializedName("poster_path")
    val posterPath: String? = null,
    @SerializedName("backdrop_path")
    val backdropPath: String? = null
)

fun TmdbCollectionDto.toDomain(): TmdbCollection = TmdbCollection(
    id = id,
    name = name,
    posterPath = posterPath,
    backdropPath = backdropPath
)
