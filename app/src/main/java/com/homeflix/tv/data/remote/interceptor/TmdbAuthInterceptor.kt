package com.homeflix.tv.data.remote.interceptor

import com.homeflix.tv.domain.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp interceptor that adds the TMDB read-access token as a Bearer header
 * to all TMDB API requests (v4 auth style — works with both v3 and v4 endpoints).
 */
class TmdbAuthInterceptor @Inject constructor(
    private val settingsRepository: SettingsRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { settingsRepository.getSettings().tmdbApiKey }

        val request = if (token.isNotBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
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
