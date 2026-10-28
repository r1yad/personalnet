package com.example.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import com.example.data.local.dao.MediaAssetDao
import com.example.data.local.entity.MediaAssetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MediaManager(
    private val context: Context,
    private val mediaAssetDao: MediaAssetDao
) {
    private val mediaDir: File
        get() {
            val dir = File(context.filesDir, "media")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    fun getAbsoluteFile(relativePath: String): File {
        return if (relativePath.startsWith("/")) {
            File(relativePath)
        } else {
            File(context.filesDir, relativePath)
        }
    }

    suspend fun importMedia(uri: Uri, mimeType: String?, preserveOriginal: Boolean = false): MediaAssetEntity? = withContext(Dispatchers.IO) {
        try {
            val isVideo = mimeType?.startsWith("video/") == true ||
                    context.contentResolver.getType(uri)?.startsWith("video/") == true

            val fileExt = if (isVideo) "mp4" else "webp"
            val filename = "asset_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.$fileExt"
            val targetFile = File(mediaDir, filename)
            val relativePath = "media/$filename"

            var width = 0
            var height = 0
            val resolvedMimeType = if (isVideo) (mimeType ?: "video/mp4") else "image/webp"

            if (isVideo || preserveOriginal) {
                // Copy stream directly
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                // Image processing: read dimensions, downsample, compress to WebP
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, boundsOptions)
                }

                val origWidth = boundsOptions.outWidth
                val origHeight = boundsOptions.outHeight
                val maxDim = 1600
                var sampleSize = 1
                while ((origWidth / sampleSize) > maxDim * 1.5 || (origHeight / sampleSize) > maxDim * 1.5) {
                    sampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }

                val rawBitmap: Bitmap? = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, decodeOptions)
                }

                if (rawBitmap != null) {
                    var rotationDegrees = 0f
                    try {
                        context.contentResolver.openInputStream(uri)?.use { inStream ->
                            val exif = ExifInterface(inStream)
                            val orientation = exif.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL
                            )
                            rotationDegrees = when (orientation) {
                                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                                else -> 0f
                            }
                        }
                    } catch (_: Exception) {}

                    val orientedBitmap: Bitmap = if (rotationDegrees != 0f) {
                        val matrix = Matrix().apply { postRotate(rotationDegrees) }
                        val rotated = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                        if (rotated != rawBitmap) {
                            rawBitmap.recycle()
                        }
                        rotated
                    } else {
                        rawBitmap
                    }

                    val currentW = orientedBitmap.width
                    val currentH = orientedBitmap.height
                    val finalBitmap: Bitmap = if (currentW > maxDim || currentH > maxDim) {
                        val ratio = minOf(maxDim.toFloat() / currentW, maxDim.toFloat() / currentH)
                        val scaledW = (currentW * ratio).toInt()
                        val scaledH = (currentH * ratio).toInt()
                        val scaled = Bitmap.createScaledBitmap(orientedBitmap, scaledW, scaledH, true)
                        if (scaled != orientedBitmap) {
                            orientedBitmap.recycle()
                        }
                        scaled
                    } else {
                        orientedBitmap
                    }

                    width = finalBitmap.width
                    height = finalBitmap.height

                    FileOutputStream(targetFile).use { outStream ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            finalBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 85, outStream)
                        } else {
                            @Suppress("DEPRECATION")
                            finalBitmap.compress(Bitmap.CompressFormat.WEBP, 85, outStream)
                        }
                    }
                    finalBitmap.recycle()
                } else {
                    // Fallback to raw copy
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }

            val asset = MediaAssetEntity(
                id = UUID.randomUUID().toString(),
                relativePath = relativePath,
                mimeType = resolvedMimeType,
                width = width,
                height = height,
                fileSizeBytes = targetFile.length(),
                createdAt = System.currentTimeMillis()
            )
            mediaAssetDao.insertMediaAsset(asset)
            asset
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
