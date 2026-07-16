package com.homeflix.tv.data.remote.interceptor

import com.homeflix.tv.domain.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp interceptor that adds the TMDB API key to all TMDB API requests.
 *
 * Uses query-parameter style (?api_key=...) which works with both v3 API keys
 * and read access tokens. The key is read from Settings (pre-configured with default).
 */
class TmdbAuthInterceptor @Inject constructor(
    private val settingsRepository: SettingsRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = runBlocking { settingsRepository.getSettings().tmdbApiKey }

        val request = if (apiKey.isNotBlank()) {
            // Add api_key as query parameter (works with v3 API keys)
            val originalUrl = chain.request().url
            val newUrl = originalUrl.newBuilder()
                .addQueryParameter("api_key", apiKey)
                .build()
            chain.request().newBuilder()
                .url(newUrl)
                .addHeader("Accept", "application/json")
                .build()
        } else {
            chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .build()
        }

        return chain.proceed(request)
    }
}
