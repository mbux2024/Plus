package com.homeflix.tv.data.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.homeflix.tv.data.persistence.dao.AddonDao
import com.homeflix.tv.data.persistence.dao.MyListDao
import com.homeflix.tv.data.persistence.dao.WatchProgressDao
import com.homeflix.tv.data.persistence.entity.*

@Database(
    entities = [
        MyListEntity::class,
        WatchProgressEntity::class,
        EpisodeWatchedEntity::class,
        AddonEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun myListDao(): MyListDao
    abstract fun watchProgressDao(): WatchProgressDao
    abstract fun addonDao(): AddonDao
}
