package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "media_assets")
data class MediaAssetEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val relativePath: String, // e.g. "media/img_12345.webp"
    val mimeType: String,
    val width: Int = 0,
    val height: Int = 0,
    val fileSizeBytes: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
