package com.example.data.backup

data class ImportSummary(
    val success: Boolean,
    val message: String,
    val personasAdded: Int = 0,
    val personasUpdated: Int = 0,
    val postsAdded: Int = 0,
    val commentsAdded: Int = 0,
    val conversationsAdded: Int = 0,
    val messagesAdded: Int = 0,
    val mediaFilesExtracted: Int = 0,
    val errors: List<String> = emptyList()
)

data class ExportSummary(
    val success: Boolean,
    val message: String,
    val filePath: String? = null,
    val totalPersonas: Int = 0,
    val totalPosts: Int = 0,
    val totalMessages: Int = 0,
    val totalMedia: Int = 0
)
