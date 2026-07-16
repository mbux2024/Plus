package com.homeflix.tv.data.remote.interceptor

import com.homeflix.tv.domain.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp interceptor that adds the TMDB API key as a Bearer token
 * to all TMDB API requests.
 *
 * The TMDB v3 API supports both query-param (?api_key=...) and
 * Bearer token auth. We use Bearer for cleaner URLs.
 */
class TmdbAuthInterceptor @Inject constructor(
    private val settingsRepository: SettingsRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = runBlocking { settingsRepository.getSettings().tmdbApiKey }

        val request = if (apiKey.isNotBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Accept", "application/json")
                .build()
        } else {
            // If no API key set, still proceed (will get 401 from TMDB)
            chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .build()
        }

        return chain.proceed(request)
    }
}
