package com.homeflix.tv.data.persistence.dao

import androidx.room.*
import com.homeflix.tv.data.persistence.entity.MyListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MyListDao {

    @Query("SELECT * FROM my_list ORDER BY added_at DESC")
    fun getAll(): Flow<List<MyListEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM my_list WHERE tmdb_id = :tmdbId AND media_type = :mediaType)")
    suspend fun exists(tmdbId: Int, mediaType: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MyListEntity)

    @Query("DELETE FROM my_list WHERE tmdb_id = :tmdbId AND media_type = :mediaType")
    suspend fun delete(tmdbId: Int, mediaType: String)
}
