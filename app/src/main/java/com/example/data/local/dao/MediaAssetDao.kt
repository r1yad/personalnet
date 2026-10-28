package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MediaAssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaAssetDao {
    @Query("SELECT * FROM media_assets ORDER BY createdAt DESC")
    fun getAllMediaAssets(): Flow<List<MediaAssetEntity>>

    @Query("SELECT * FROM media_assets ORDER BY createdAt DESC")
    suspend fun getAllMediaAssetsDirect(): List<MediaAssetEntity>

    @Query("SELECT * FROM media_assets WHERE relativePath = :path LIMIT 1")
    suspend fun getMediaByPath(path: String): MediaAssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaAsset(asset: MediaAssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaAssets(assets: List<MediaAssetEntity>)

    @Query("DELETE FROM media_assets WHERE id = :id")
    suspend fun deleteMediaAssetById(id: String)
}
