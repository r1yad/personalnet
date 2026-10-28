package com.example.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CommentEntity
import com.example.data.local.entity.CommentLikeEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MediaAssetEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PersonaEntity
import com.example.data.local.entity.PostEntity
import com.example.data.local.entity.PostLikeEntity
import com.example.data.local.entity.PostMediaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(
    private val context: Context,
    private val database: AppDatabase
) {
    suspend fun exportToUri(destinationUri: Uri): ExportSummary = withContext(Dispatchers.IO) {
        try {
            val personas = database.personaDao().getAllPersonasDirect()
            val posts = database.postDao().getAllPostsDirect()
            val postMedia = database.postDao().getAllPostMediaDirect()
            val comments = database.commentDao().getAllCommentsDirect()
            val postLikes = database.likeDao().getAllPostLikesDirect()
            val commentLikes = database.likeDao().getAllCommentLikesDirect()
            val conversations = database.conversationDao().getAllConversationsDirect()
            val messages = database.conversationDao().getAllMessagesDirect()
            val mediaAssets = database.mediaAssetDao().getAllMediaAssetsDirect()

            // Build data JSON
            val rootJson = JSONObject().apply {
                put("version", 1)
                put("exportedAt", System.currentTimeMillis())

                put("personas", JSONArray().apply {
                    personas.forEach { p ->
                        put(JSONObject().apply {
                            put("id", p.id)
                            put("displayName", p.displayName)
                            put("username", p.username)
                            put("bio", p.bio)
                            put("avatarPath", p.avatarPath ?: "")
                            put("coverPath", p.coverPath ?: "")
                            put("personalityTraits", p.personalityTraits)
                            put("speakingStyle", p.speakingStyle)
                            put("roleplayInstructions", p.roleplayInstructions)
                            put("backgroundStory", p.backgroundStory)
                            put("createdAt", p.createdAt)
                            put("updatedAt", p.updatedAt)
                        })
                    }
                })

                put("posts", JSONArray().apply {
                    posts.forEach { post ->
                        put(JSONObject().apply {
                            put("id", post.id)
                            put("authorPersonaId", post.authorPersonaId)
                            put("textContent", post.textContent)
                            put("createdAt", post.createdAt)
                            put("updatedAt", post.updatedAt)
                        })
                    }
                })

                put("postMedia", JSONArray().apply {
                    postMedia.forEach { pm ->
                        put(JSONObject().apply {
                            put("id", pm.id)
                            put("postId", pm.postId)
                            put("mediaPath", pm.mediaPath)
                            put("mimeType", pm.mimeType)
                            put("orderIndex", pm.orderIndex)
                        })
                    }
                })

                put("comments", JSONArray().apply {
                    comments.forEach { c ->
                        put(JSONObject().apply {
                            put("id", c.id)
                            put("postId", c.postId)
                            put("parentCommentId", c.parentCommentId ?: "")
                            put("authorPersonaId", c.authorPersonaId)
                            put("textContent", c.textContent)
                            put("createdAt", c.createdAt)
                            put("updatedAt", c.updatedAt)
                        })
                    }
                })

                put("postLikes", JSONArray().apply {
                    postLikes.forEach { pl ->
                        put(JSONObject().apply {
                            put("postId", pl.postId)
                            put("personaId", pl.personaId)
                            put("createdAt", pl.createdAt)
                        })
                    }
                })

                put("commentLikes", JSONArray().apply {
                    commentLikes.forEach { cl ->
                        put(JSONObject().apply {
                            put("commentId", cl.commentId)
                            put("personaId", cl.personaId)
                            put("createdAt", cl.createdAt)
                        })
                    }
                })

                put("conversations", JSONArray().apply {
                    conversations.forEach { conv ->
                        put(JSONObject().apply {
                            put("id", conv.id)
                            put("personaAId", conv.personaAId)
                            put("personaBId", conv.personaBId)
                            put("createdAt", conv.createdAt)
                            put("lastActivityAt", conv.lastActivityAt)
                            put("lastMessagePreview", conv.lastMessagePreview)
                        })
                    }
                })

                put("messages", JSONArray().apply {
                    messages.forEach { msg ->
                        put(JSONObject().apply {
                            put("id", msg.id)
                            put("conversationId", msg.conversationId)
                            put("senderPersonaId", msg.senderPersonaId)
                            put("replyToMessageId", msg.replyToMessageId ?: "")
                            put("textContent", msg.textContent)
                            put("mediaPath", msg.mediaPath ?: "")
                            put("mimeType", msg.mimeType ?: "")
                            put("createdAt", msg.createdAt)
                            put("isEdited", msg.isEdited)
                            put("isRead", msg.isRead)
                        })
                    }
                })

                put("mediaAssets", JSONArray().apply {
                    mediaAssets.forEach { ma ->
                        put(JSONObject().apply {
                            put("id", ma.id)
                            put("relativePath", ma.relativePath)
                            put("mimeType", ma.mimeType)
                            put("width", ma.width)
                            put("height", ma.height)
                            put("fileSizeBytes", ma.fileSizeBytes)
                            put("createdAt", ma.createdAt)
                        })
                    }
                })
            }

            // Write ZIP to destination stream
            val outputStream = context.contentResolver.openOutputStream(destinationUri)
                ?: return@withContext ExportSummary(false, "Could not open output destination")

            var mediaCount = 0
            ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                // 1. manifest.json
                val manifestJson = JSONObject().apply {
                    put("app", "PersonaNet")
                    put("version", 1)
                    put("exportedAt", System.currentTimeMillis())
                    put("personasCount", personas.size)
                    put("postsCount", posts.size)
                    put("conversationsCount", conversations.size)
                    put("messagesCount", messages.size)
                }
                zipOut.putNextEntry(ZipEntry("manifest.json"))
                zipOut.write(manifestJson.toString(2).toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                // 2. data.json
                zipOut.putNextEntry(ZipEntry("data.json"))
                zipOut.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                // 3. Media files
                val writtenMediaPaths = mutableSetOf<String>()
                mediaAssets.forEach { asset ->
                    if (asset.relativePath.isNotBlank() && !writtenMediaPaths.contains(asset.relativePath)) {
                        val file = File(context.filesDir, asset.relativePath)
                        if (file.exists() && file.isFile) {
                            zipOut.putNextEntry(ZipEntry(asset.relativePath))
                            FileInputStream(file).use { input ->
                                input.copyTo(zipOut)
                            }
                            zipOut.closeEntry()
                            writtenMediaPaths.add(asset.relativePath)
                            mediaCount++
                        }
                    }
                }
            }

            ExportSummary(
                success = true,
                message = "Backup exported successfully with $mediaCount media files",
                totalPersonas = personas.size,
                totalPosts = posts.size,
                totalMessages = messages.size,
                totalMedia = mediaCount
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ExportSummary(false, "Export failed: ${e.localizedMessage}")
        }
    }

    suspend fun importFromUri(sourceUri: Uri, isRestoreMode: Boolean): ImportSummary = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return@withContext ImportSummary(false, "Could not open source backup file")

            var dataJsonString: String? = null
            var mediaExtracted = 0

            // Read ZIP
            ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val name = entry.name
                    if (name == "data.json") {
                        val bytes = zipIn.readBytes()
                        dataJsonString = String(bytes, Charsets.UTF_8)
                    } else if (name.startsWith("media/") && !entry.isDirectory) {
                        val targetFile = File(context.filesDir, name)
                        targetFile.parentFile?.mkdirs()
                        FileOutputStream(targetFile).use { out ->
                            zipIn.copyTo(out)
                        }
                        mediaExtracted++
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            if (dataJsonString.isNullOrBlank()) {
                return@withContext ImportSummary(false, "Invalid backup: data.json not found in archive")
            }

            val rootJson = JSONObject(dataJsonString)

            var personasAdded = 0
            var personasUpdated = 0
            var postsAdded = 0
            var commentsAdded = 0
            var conversationsAdded = 0
            var messagesAdded = 0

            database.withTransaction {
                if (isRestoreMode) {
                    // Clear existing tables
                    database.clearAllTables()
                }

                // 1. Personas
                val personasArray = rootJson.optJSONArray("personas") ?: JSONArray()
                val existingPersonas = database.personaDao().getAllPersonasDirect().associateBy { it.id }
                val existingUsernames = database.personaDao().getAllPersonasDirect().map { it.username.lowercase() }.toMutableSet()

                for (i in 0 until personasArray.length()) {
                    val obj = personasArray.getJSONObject(i)
                    val id = obj.getString("id")
                    var username = obj.getString("username")
                    val existing = existingPersonas[id]

                    if (existing == null && existingUsernames.contains(username.lowercase())) {
                        username = "${username}_imported"
                    }

                    val persona = PersonaEntity(
                        id = id,
                        displayName = obj.getString("displayName"),
                        username = username,
                        bio = obj.optString("bio", ""),
                        avatarPath = obj.optString("avatarPath").takeIf { it.isNotBlank() },
                        coverPath = obj.optString("coverPath").takeIf { it.isNotBlank() },
                        personalityTraits = obj.optString("personalityTraits", ""),
                        speakingStyle = obj.optString("speakingStyle", ""),
                        roleplayInstructions = obj.optString("roleplayInstructions", ""),
                        backgroundStory = obj.optString("backgroundStory", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )

                    if (existing == null) {
                        database.personaDao().insertPersona(persona)
                        existingUsernames.add(username.lowercase())
                        personasAdded++
                    } else if (persona.updatedAt > existing.updatedAt || isRestoreMode) {
                        database.personaDao().updatePersona(persona)
                        personasUpdated++
                    }
                }

                // 2. Posts
                val postsArray = rootJson.optJSONArray("posts") ?: JSONArray()
                val existingPosts = database.postDao().getAllPostsDirect().associateBy { it.id }
                for (i in 0 until postsArray.length()) {
                    val obj = postsArray.getJSONObject(i)
                    val id = obj.getString("id")
                    if (!existingPosts.containsKey(id) || isRestoreMode) {
                        val post = PostEntity(
                            id = id,
                            authorPersonaId = obj.getString("authorPersonaId"),
                            textContent = obj.getString("textContent"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                        database.postDao().insertPost(post)
                        postsAdded++
                    }
                }

                // 3. PostMedia
                val postMediaArray = rootJson.optJSONArray("postMedia") ?: JSONArray()
                val postMediaList = mutableListOf<PostMediaEntity>()
                for (i in 0 until postMediaArray.length()) {
                    val obj = postMediaArray.getJSONObject(i)
                    postMediaList.add(
                        PostMediaEntity(
                            id = obj.getString("id"),
                            postId = obj.getString("postId"),
                            mediaPath = obj.getString("mediaPath"),
                            mimeType = obj.optString("mimeType", "image/webp"),
                            orderIndex = obj.optInt("orderIndex", 0)
                        )
                    )
                }
                if (postMediaList.isNotEmpty()) {
                    database.postDao().insertPostMedia(postMediaList)
                }

                // 4. Comments
                val commentsArray = rootJson.optJSONArray("comments") ?: JSONArray()
                val existingComments = database.commentDao().getAllCommentsDirect().associateBy { it.id }
                for (i in 0 until commentsArray.length()) {
                    val obj = commentsArray.getJSONObject(i)
                    val id = obj.getString("id")
                    if (!existingComments.containsKey(id) || isRestoreMode) {
                        val parentId = obj.optString("parentCommentId").takeIf { it.isNotBlank() }
                        val comment = CommentEntity(
                            id = id,
                            postId = obj.getString("postId"),
                            parentCommentId = parentId,
                            authorPersonaId = obj.getString("authorPersonaId"),
                            textContent = obj.getString("textContent"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                        database.commentDao().insertComment(comment)
                        commentsAdded++
                    }
                }

                // 5. PostLikes
                val postLikesArray = rootJson.optJSONArray("postLikes") ?: JSONArray()
                val postLikesList = mutableListOf<PostLikeEntity>()
                for (i in 0 until postLikesArray.length()) {
                    val obj = postLikesArray.getJSONObject(i)
                    postLikesList.add(
                        PostLikeEntity(
                            postId = obj.getString("postId"),
                            personaId = obj.getString("personaId"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (postLikesList.isNotEmpty()) {
                    database.likeDao().insertPostLikes(postLikesList)
                }

                // 6. CommentLikes
                val commentLikesArray = rootJson.optJSONArray("commentLikes") ?: JSONArray()
                val commentLikesList = mutableListOf<CommentLikeEntity>()
                for (i in 0 until commentLikesArray.length()) {
                    val obj = commentLikesArray.getJSONObject(i)
                    commentLikesList.add(
                        CommentLikeEntity(
                            commentId = obj.getString("commentId"),
                            personaId = obj.getString("personaId"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (commentLikesList.isNotEmpty()) {
                    database.likeDao().insertCommentLikes(commentLikesList)
                }

                // 7. Conversations
                val convArray = rootJson.optJSONArray("conversations") ?: JSONArray()
                val existingConversations = database.conversationDao().getAllConversationsDirect().associateBy { it.id }
                for (i in 0 until convArray.length()) {
                    val obj = convArray.getJSONObject(i)
                    val id = obj.getString("id")
                    if (!existingConversations.containsKey(id) || isRestoreMode) {
                        val conv = ConversationEntity(
                            id = id,
                            personaAId = obj.getString("personaAId"),
                            personaBId = obj.getString("personaBId"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            lastActivityAt = obj.optLong("lastActivityAt", System.currentTimeMillis()),
                            lastMessagePreview = obj.optString("lastMessagePreview", "")
                        )
                        database.conversationDao().insertConversation(conv)
                        conversationsAdded++
                    }
                }

                // 8. Messages
                val messagesArray = rootJson.optJSONArray("messages") ?: JSONArray()
                val existingMessages = database.conversationDao().getAllMessagesDirect().associateBy { it.id }
                for (i in 0 until messagesArray.length()) {
                    val obj = messagesArray.getJSONObject(i)
                    val id = obj.getString("id")
                    if (!existingMessages.containsKey(id) || isRestoreMode) {
                        val msg = MessageEntity(
                            id = id,
                            conversationId = obj.getString("conversationId"),
                            senderPersonaId = obj.getString("senderPersonaId"),
                            replyToMessageId = obj.optString("replyToMessageId").takeIf { it.isNotBlank() },
                            textContent = obj.getString("textContent"),
                            mediaPath = obj.optString("mediaPath").takeIf { it.isNotBlank() },
                            mimeType = obj.optString("mimeType").takeIf { it.isNotBlank() },
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            isEdited = obj.optBoolean("isEdited", false),
                            isRead = obj.optBoolean("isRead", true)
                        )
                        database.conversationDao().insertMessage(msg)
                        messagesAdded++
                    }
                }

                // 9. MediaAssets
                val mediaAssetsArray = rootJson.optJSONArray("mediaAssets") ?: JSONArray()
                val mediaAssetList = mutableListOf<MediaAssetEntity>()
                for (i in 0 until mediaAssetsArray.length()) {
                    val obj = mediaAssetsArray.getJSONObject(i)
                    mediaAssetList.add(
                        MediaAssetEntity(
                            id = obj.getString("id"),
                            relativePath = obj.getString("relativePath"),
                            mimeType = obj.optString("mimeType", "image/webp"),
                            width = obj.optInt("width", 0),
                            height = obj.optInt("height", 0),
                            fileSizeBytes = obj.optLong("fileSizeBytes", 0L),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (mediaAssetList.isNotEmpty()) {
                    database.mediaAssetDao().insertMediaAssets(mediaAssetList)
                }
            }

            ImportSummary(
                success = true,
                message = if (isRestoreMode) "Full restore completed successfully" else "Merge import completed successfully",
                personasAdded = personasAdded,
                personasUpdated = personasUpdated,
                postsAdded = postsAdded,
                commentsAdded = commentsAdded,
                conversationsAdded = conversationsAdded,
                messagesAdded = messagesAdded,
                mediaFilesExtracted = mediaExtracted,
                errors = errors
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ImportSummary(
                success = false,
                message = "Import error: ${e.localizedMessage}",
                errors = listOf(e.localizedMessage ?: "Unknown error")
            )
        }
    }
}
