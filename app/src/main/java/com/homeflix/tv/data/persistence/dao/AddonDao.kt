package com.homeflix.tv.data.persistence.dao

import androidx.room.*
import com.homeflix.tv.data.persistence.entity.AddonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddonDao {

    @Query("SELECT * FROM installed_addons ORDER BY priority ASC")
    fun getAll(): Flow<List<AddonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(addon: AddonEntity)

    @Query("DELETE FROM installed_addons WHERE id = :addonId")
    suspend fun delete(addonId: String)

    @Query("UPDATE installed_addons SET priority = :newPriority WHERE id = :addonId")
    suspend fun updatePriority(addonId: String, newPriority: Int)

    @Query("UPDATE installed_addons SET is_enabled = :enabled WHERE id = :addonId")
    suspend fun toggleEnabled(addonId: String, enabled: Boolean)
}
