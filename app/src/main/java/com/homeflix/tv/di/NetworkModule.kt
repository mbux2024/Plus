package com.homeflix.tv.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.homeflix.tv.BuildConfig
import com.homeflix.tv.data.remote.api.TmdbApiService
import com.homeflix.tv.data.remote.debrid.RealDebridApiService
import com.homeflix.tv.data.remote.debrid.TorBoxApiService
import com.homeflix.tv.data.remote.interceptor.TmdbAuthInterceptor
import com.homeflix.tv.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ─── Shared ──────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

    @Provides
    @Singleton
    @Named("base")
    fun provideBaseOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ─── TMDB ────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideTmdbAuthInterceptor(
        settingsRepository: SettingsRepository
    ): TmdbAuthInterceptor = TmdbAuthInterceptor(settingsRepository)

    @Provides
    @Singleton
    @Named("tmdb")
    fun provideTmdbOkHttpClient(
        @Named("base") baseClient: OkHttpClient,
        tmdbAuthInterceptor: TmdbAuthInterceptor
    ): OkHttpClient = baseClient.newBuilder()
        .addInterceptor(tmdbAuthInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideTmdbApiService(
        @Named("tmdb") okHttpClient: OkHttpClient,
        gson: Gson
    ): TmdbApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.TMDB_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(TmdbApiService::class.java)

    // ─── TorBox ──────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideTorBoxApiService(
        @Named("base") okHttpClient: OkHttpClient,
        gson: Gson
    ): TorBoxApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.TORBOX_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(TorBoxApiService::class.java)

    // ─── Real-Debrid ─────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideRealDebridApiService(
        @Named("base") okHttpClient: OkHttpClient,
        gson: Gson
    ): RealDebridApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.REALDEBRID_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(RealDebridApiService::class.java)

    // ─── Stremio / General OkHttp (for addon requests) ───────────────────

    @Provides
    @Singleton
    @Named("stremio")
    fun provideStremioOkHttpClient(
        @Named("base") baseClient: OkHttpClient
    ): OkHttpClient = baseClient.newBuilder()
        .readTimeout(20, TimeUnit.SECONDS) // Addons can be slow
        .build()

    /**
     * General purpose OkHttpClient for non-Retrofit usage
     * (StremioAddonClient, TrailerResolver, etc.)
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @Named("stremio") stremioClient: OkHttpClient
    ): OkHttpClient = stremioClient
}
