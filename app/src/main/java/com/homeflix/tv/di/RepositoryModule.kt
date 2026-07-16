package com.homeflix.tv.di

import com.homeflix.tv.data.persistence.LocalRepositoryImpl
import com.homeflix.tv.data.repository.TmdbRepositoryImpl
import com.homeflix.tv.data.resolver.StreamResolverImpl
import com.homeflix.tv.data.settings.SettingsRepositoryImpl
import com.homeflix.tv.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTmdbRepository(
        impl: TmdbRepositoryImpl
    ): TmdbRepository

    @Binds
    @Singleton
    abstract fun bindStreamRepository(
        impl: StreamResolverImpl
    ): StreamRepository

    @Binds
    @Singleton
    abstract fun bindLocalRepository(
        impl: LocalRepositoryImpl
    ): LocalRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    /** Backward compat: old VideoPlayer.kt uses MediaRepository via EntryPoint */
    @Binds
    @Singleton
    abstract fun bindLegacyMediaRepository(
        impl: com.homeflix.tv.data.repository.MediaRepositoryStub
    ): com.homeflix.tv.domain.repository.MediaRepository
}
