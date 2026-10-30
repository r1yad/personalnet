package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.backup.ExportSummary
import com.example.data.backup.ImportSummary
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PersonaEntity
import com.example.data.local.model.CommentWithDetails
import com.example.data.local.model.ConversationWithDetails
import com.example.data.local.model.MessageWithDetails
import com.example.data.local.model.PostWithDetails
import com.example.data.media.MediaManager
import com.example.data.preferences.AppPreferences
import com.example.data.repository.CommentRepository
import com.example.data.repository.ConversationRepository
import com.example.data.repository.PersonaRepository
import com.example.data.repository.PostRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PersonaNetViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val preferences = AppPreferences(application)
    val mediaManager = MediaManager(application, database.mediaAssetDao())

    val personaRepo = PersonaRepository(database.personaDao())
    val postRepo = PostRepository(
        database.postDao(),
        database.personaDao(),
        database.likeDao(),
        database.commentDao(),
        mediaManager
    )
    val commentRepo = CommentRepository(
        database.commentDao(),
        database.personaDao(),
        database.likeDao()
    )
    val conversationRepo = ConversationRepository(
        database.conversationDao(),
        database.personaDao(),
        mediaManager
    )
    private val backupManager = BackupManager(application, database)

    val allPersonas: StateFlow<List<PersonaEntity>> = personaRepo.allPersonas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePersonaId: StateFlow<String?> = preferences.activePersonaId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activePersona: StateFlow<PersonaEntity?> = combine(allPersonas, activePersonaId) { personas, activeId ->
        if (personas.isEmpty()) null
        else if (activeId != null) {
            personas.find { it.id == activeId } ?: personas.firstOrNull()
        } else {
            personas.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val feedPosts: StateFlow<List<PostWithDetails>> = activePersona.flatMapLatest { persona ->
        postRepo.getFeedPosts(persona?.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversations: StateFlow<List<ConversationWithDetails>> = activePersona.flatMapLatest { persona ->
        if (persona != null) conversationRepo.getConversations(persona.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPostId = MutableStateFlow<String?>(null)
    val selectedPostId = _selectedPostId.asStateFlow()

    val currentPostDetails: StateFlow<PostWithDetails?> = combine(_selectedPostId, activePersona) { postId, persona ->
        Pair(postId, persona?.id)
    }.flatMapLatest { (postId, activeId) ->
        if (postId != null) postRepo.getPostDetails(postId, activeId)
        else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentPostComments: StateFlow<List<CommentWithDetails>> = combine(_selectedPostId, activePersona) { postId, persona ->
        Pair(postId, persona?.id)
    }.flatMapLatest { (postId, activeId) ->
        if (postId != null) commentRepo.getCommentsTreeForPost(postId, activeId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId = _activeConversationId.asStateFlow()

    val currentChatMessages: StateFlow<List<MessageWithDetails>> = _activeConversationId.flatMapLatest { convId ->
        if (convId != null) conversationRepo.getMessages(convId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mediaQuality: StateFlow<String> = preferences.mediaQuality
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "STANDARD")

    private val _isProcessingBackup = MutableStateFlow(false)
    val isProcessingBackup = _isProcessingBackup.asStateFlow()

    private val _exportSummary = MutableStateFlow<ExportSummary?>(null)
    val exportSummary = _exportSummary.asStateFlow()

    private val _importSummary = MutableStateFlow<ImportSummary?>(null)
    val importSummary = _importSummary.asStateFlow()

    fun selectActivePersona(personaId: String) {
        viewModelScope.launch {
            preferences.setActivePersonaId(personaId)
        }
    }

    fun savePersona(persona: PersonaEntity, avatarUri: Uri?, coverUri: Uri?) {
        viewModelScope.launch {
            var updatedPersona = persona
            if (avatarUri != null) {
                val asset = mediaManager.importMedia(avatarUri, "image/webp")
                if (asset != null) {
                    updatedPersona = updatedPersona.copy(avatarPath = asset.relativePath)
                }
            }
            if (coverUri != null) {
                val asset = mediaManager.importMedia(coverUri, "image/webp")
                if (asset != null) {
                    updatedPersona = updatedPersona.copy(coverPath = asset.relativePath)
                }
            }
            personaRepo.insertOrUpdate(updatedPersona)
            // Auto-select as active if none was active
            if (activePersonaId.value == null) {
                preferences.setActivePersonaId(updatedPersona.id)
            }
        }
    }

    fun duplicatePersona(persona: PersonaEntity) {
        viewModelScope.launch {
            personaRepo.duplicatePersona(persona)
        }
    }

    fun deletePersona(personaId: String) {
        viewModelScope.launch {
            personaRepo.deletePersona(personaId)
            if (activePersonaId.value == personaId) {
                val remaining = allPersonas.value.filter { it.id != personaId }
                preferences.setActivePersonaId(remaining.firstOrNull()?.id)
            }
        }
    }

    private val _isPublishingPost = MutableStateFlow(false)
    val isPublishingPost = _isPublishingPost.asStateFlow()

    private val _scrollToTopTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrollToTopTrigger = _scrollToTopTrigger.asSharedFlow()

    fun createPost(
        textContent: String,
        mediaList: List<Pair<Uri, String?>>,
        onFinished: ((String) -> Unit)? = null
    ) {
        val currentPersona = activePersona.value ?: return
        viewModelScope.launch {
            _isPublishingPost.value = true
            try {
                val newPostId = postRepo.createPost(
                    authorPersonaId = currentPersona.id,
                    textContent = textContent,
                    mediaUris = mediaList
                )
                preferences.clearPostDraft(currentPersona.id)
                _scrollToTopTrigger.tryEmit(Unit)
                onFinished?.invoke(newPostId)
            } finally {
                _isPublishingPost.value = false
            }
        }
    }

    fun getPostDraft(personaId: String) = preferences.getPostDraft(personaId)

    fun savePostDraft(personaId: String, text: String) {
        viewModelScope.launch {
            preferences.savePostDraft(personaId, text)
        }
    }

    fun clearPostDraft(personaId: String) {
        viewModelScope.launch {
            preferences.clearPostDraft(personaId)
        }
    }

    fun updatePost(postId: String, newText: String) {
        viewModelScope.launch {
            postRepo.updatePost(postId, newText)
        }
    }

    fun updatePostWithMedia(
        postId: String,
        newText: String,
        keptMediaIds: List<String>,
        newMediaUris: List<Pair<Uri, String?>>,
        onFinished: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            postRepo.updatePostWithMedia(postId, newText, keptMediaIds, newMediaUris)
            onFinished?.invoke()
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            postRepo.deletePost(postId)
            if (_selectedPostId.value == postId) {
                _selectedPostId.value = null
            }
        }
    }

    fun togglePostLike(postId: String) {
        val currentPersona = activePersona.value ?: return
        viewModelScope.launch {
            postRepo.toggleLike(postId, currentPersona.id)
        }
    }

    fun openPostDetails(postId: String) {
        _selectedPostId.value = postId
    }

    fun closePostDetails() {
        _selectedPostId.value = null
    }

    fun addComment(postId: String, textContent: String, parentCommentId: String?) {
        val currentPersona = activePersona.value ?: return
        viewModelScope.launch {
            commentRepo.addComment(
                postId = postId,
                authorPersonaId = currentPersona.id,
                textContent = textContent,
                parentCommentId = parentCommentId
            )
        }
    }

    fun toggleCommentLike(commentId: String) {
        val currentPersona = activePersona.value ?: return
        viewModelScope.launch {
            commentRepo.toggleCommentLike(commentId, currentPersona.id)
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            commentRepo.deleteComment(commentId)
        }
    }

    fun updateComment(commentId: String, newText: String) {
        viewModelScope.launch {
            commentRepo.updateComment(commentId, newText)
        }
    }

    fun openChat(conversationId: String) {
        _activeConversationId.value = conversationId
        val currentPersona = activePersona.value ?: return
        viewModelScope.launch {
            conversationRepo.markAsRead(conversationId, currentPersona.id)
        }
    }

    fun closeChat() {
        _activeConversationId.value = null
    }

    fun startChatWith(partnerPersonaId: String, onOpened: (String) -> Unit) {
        val currentPersona = activePersona.value ?: return
        viewModelScope.launch {
            val convId = conversationRepo.getOrCreateConversation(currentPersona.id, partnerPersonaId)
            _activeConversationId.value = convId
            onOpened(convId)
        }
    }

    fun sendMessage(
        conversationId: String,
        textContent: String,
        mediaUri: Uri?,
        mimeType: String?,
        replyToId: String?
    ) {
        val currentPersona = activePersona.value ?: return
        viewModelScope.launch {
            conversationRepo.sendMessage(
                conversationId = conversationId,
                senderPersonaId = currentPersona.id,
                textContent = textContent,
                mediaUri = mediaUri,
                mimeType = mimeType,
                replyToId = replyToId
            )
        }
    }

    fun editMessage(messageId: String, newText: String) {
        viewModelScope.launch {
            conversationRepo.editMessage(messageId, newText)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            conversationRepo.deleteMessage(messageId)
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            conversationRepo.deleteConversation(conversationId)
            if (_activeConversationId.value == conversationId) {
                _activeConversationId.value = null
            }
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _isProcessingBackup.value = true
            _exportSummary.value = null
            val summary = backupManager.exportToUri(uri)
            _exportSummary.value = summary
            _isProcessingBackup.value = false
        }
    }

    fun importBackup(uri: Uri, isRestoreMode: Boolean) {
        viewModelScope.launch {
            _isProcessingBackup.value = true
            _importSummary.value = null
            val summary = backupManager.importFromUri(uri, isRestoreMode)
            _importSummary.value = summary
            _isProcessingBackup.value = false
        }
    }

    fun setMediaQuality(quality: String) {
        viewModelScope.launch {
            preferences.setMediaQuality(quality)
        }
    }

    fun clearBackupResults() {
        _exportSummary.value = null
        _importSummary.value = null
    }
}
