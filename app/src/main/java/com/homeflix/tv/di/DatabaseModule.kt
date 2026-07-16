package com.homeflix.tv.di

import android.content.Context
import androidx.room.Room
import com.homeflix.tv.data.persistence.AppDatabase
import com.homeflix.tv.data.persistence.dao.AddonDao
import com.homeflix.tv.data.persistence.dao.MyListDao
import com.homeflix.tv.data.persistence.dao.WatchProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "homeflix_database"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMyListDao(db: AppDatabase): MyListDao = db.myListDao()

    @Provides
    fun provideWatchProgressDao(db: AppDatabase): WatchProgressDao = db.watchProgressDao()

    @Provides
    fun provideAddonDao(db: AppDatabase): AddonDao = db.addonDao()
}
