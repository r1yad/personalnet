package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.PersonaEntity
import com.example.data.local.model.PostWithDetails
import com.example.ui.components.MediaViewerDialog
import com.example.ui.components.PersonaAvatar
import com.example.ui.components.PersonaSwitcherBottomSheet
import com.example.ui.screens.backup.BackupSettingsScreen
import com.example.ui.screens.dm.ChatScreen
import com.example.ui.screens.dm.ConversationsScreen
import com.example.ui.screens.feed.CreatePostDialog
import com.example.ui.screens.feed.EditPostDialog
import com.example.ui.screens.feed.FeedScreen
import com.example.ui.screens.feed.PostDetailScreen
import com.example.ui.screens.persona.EditPersonaScreen
import com.example.ui.screens.persona.PersonaProfileScreen
import com.example.ui.screens.persona.PersonasScreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import kotlinx.coroutines.launch

sealed class Screen {
    object Feed : Screen()
    object Messages : Screen()
    object Personas : Screen()
    object Backup : Screen()
    object ActiveProfile : Screen()

    data class EditPersona(val personaId: String? = null) : Screen()
    data class ViewProfile(val personaId: String) : Screen()
    data class Chat(val conversationId: String, val otherPersonaId: String) : Screen()
    data class PostDetail(val postId: String) : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaNetApp(viewModel: PersonaNetViewModel) {
    val coroutineScope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Feed) }

    val activePersona by viewModel.activePersona.collectAsStateWithLifecycle()
    val allPersonas by viewModel.allPersonas.collectAsStateWithLifecycle()
    val feedPosts by viewModel.feedPosts.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val currentChatMessages by viewModel.currentChatMessages.collectAsStateWithLifecycle()
    val currentPostDetails by viewModel.currentPostDetails.collectAsStateWithLifecycle()
    val currentPostComments by viewModel.currentPostComments.collectAsStateWithLifecycle()
    val mediaQuality by viewModel.mediaQuality.collectAsStateWithLifecycle()
    val isProcessingBackup by viewModel.isProcessingBackup.collectAsStateWithLifecycle()
    val exportSummary by viewModel.exportSummary.collectAsStateWithLifecycle()
    val importSummary by viewModel.importSummary.collectAsStateWithLifecycle()
    val isPublishingPost by viewModel.isPublishingPost.collectAsStateWithLifecycle()
    val activePersonaDraft by viewModel.getPostDraft(activePersona?.id ?: "")
        .collectAsStateWithLifecycle(initialValue = "")

    var showCreatePostSheet by remember { mutableStateOf(false) }
    var showPersonaSwitcherSheet by remember { mutableStateOf(false) }
    var postToEdit by remember { mutableStateOf<PostWithDetails?>(null) }
    var fullscreenMediaPath by remember { mutableStateOf<String?>(null) }

    val switcherSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle back button for secondary screens
    BackHandler(enabled = currentScreen !is Screen.Feed) {
        when (currentScreen) {
            is Screen.Chat -> {
                viewModel.closeChat()
                currentScreen = Screen.Messages
            }
            is Screen.PostDetail -> {
                viewModel.closePostDetails()
                currentScreen = Screen.Feed
            }
            is Screen.EditPersona -> {
                currentScreen = Screen.Personas
            }
            is Screen.ViewProfile -> {
                currentScreen = Screen.Personas
            }
            else -> {
                currentScreen = Screen.Feed
            }
        }
    }

    val isTopLevelScreen = currentScreen is Screen.Feed ||
            currentScreen is Screen.Messages ||
            currentScreen is Screen.Personas ||
            currentScreen is Screen.Backup ||
            currentScreen is Screen.ActiveProfile

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        containerColor = DarkSurface,
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Feed,
                        onClick = { currentScreen = Screen.Feed },
                        icon = { Icon(Icons.Default.DynamicFeed, contentDescription = "Feed") },
                        label = { Text("Feed", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_feed")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Messages,
                        onClick = { currentScreen = Screen.Messages },
                        icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Messages") },
                        label = { Text("DMs", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_messages")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Personas,
                        onClick = { currentScreen = Screen.Personas },
                        icon = { Icon(Icons.Default.Group, contentDescription = "Personas") },
                        label = { Text("Personas", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_personas")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.ActiveProfile,
                        onClick = {
                            if (activePersona != null) {
                                currentScreen = Screen.ActiveProfile
                            } else {
                                currentScreen = Screen.EditPersona(null)
                            }
                        },
                        icon = {
                            if (activePersona != null) {
                                PersonaAvatar(
                                    name = activePersona!!.displayName,
                                    avatarPath = activePersona!!.avatarPath,
                                    size = 24.dp,
                                    borderWidth = if (currentScreen is Screen.ActiveProfile) 1.5.dp else 0.dp,
                                    borderColor = NeonCyan
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = "Profile")
                            }
                        },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Backup,
                        onClick = { currentScreen = Screen.Backup },
                        icon = { Icon(Icons.Default.Archive, contentDescription = "Backup & Settings") },
                        label = { Text("Backup", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_backup")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (val screen = currentScreen) {
                is Screen.Feed -> {
                    FeedScreen(
                        posts = feedPosts,
                        activePersona = activePersona,
                        scrollToTopTrigger = viewModel.scrollToTopTrigger,
                        onOpenPersonaSwitcher = { showPersonaSwitcherSheet = true },
                        onCreatePost = { showCreatePostSheet = true },
                        onPostClick = { postId ->
                            viewModel.openPostDetails(postId)
                            currentScreen = Screen.PostDetail(postId)
                        },
                        onLikePost = { postId -> viewModel.togglePostLike(postId) },
                        onProfileClick = { personaId -> currentScreen = Screen.ViewProfile(personaId) },
                        onMediaClick = { path -> fullscreenMediaPath = path },
                        onMentionClick = { username ->
                            val target = allPersonas.find { it.username.equals(username, ignoreCase = true) }
                            if (target != null) currentScreen = Screen.ViewProfile(target.id)
                        },
                        onEditPost = { postWithDetails -> postToEdit = postWithDetails },
                        onDeletePost = { postId -> viewModel.deletePost(postId) },
                        onRefresh = {}
                    )
                }

                is Screen.Messages -> {
                    ConversationsScreen(
                        conversations = conversations,
                        activePersona = activePersona,
                        allPersonas = allPersonas,
                        onOpenConversation = { convId, otherId ->
                            viewModel.openChat(convId)
                            currentScreen = Screen.Chat(convId, otherId)
                        },
                        onStartNewConversation = { partnerId ->
                            viewModel.startChatWith(partnerId) { convId ->
                                currentScreen = Screen.Chat(convId, partnerId)
                            }
                        },
                        onDeleteConversation = { convId -> viewModel.deleteConversation(convId) }
                    )
                }

                is Screen.Personas -> {
                    PersonasScreen(
                        personas = allPersonas,
                        activePersonaId = activePersona?.id,
                        onSelectActivePersona = { personaId -> viewModel.selectActivePersona(personaId) },
                        onViewProfile = { personaId -> currentScreen = Screen.ViewProfile(personaId) },
                        onEditPersona = { personaId -> currentScreen = Screen.EditPersona(personaId) },
                        onDuplicatePersona = { persona -> viewModel.duplicatePersona(persona) },
                        onDeletePersona = { personaId -> viewModel.deletePersona(personaId) },
                        onCreatePersona = { currentScreen = Screen.EditPersona(null) }
                    )
                }

                is Screen.ActiveProfile -> {
                    activePersona?.let { persona ->
                        val personaPosts = feedPosts.filter { it.author.id == persona.id }
                        val likesReceived = personaPosts.sumOf { it.likeCount }
                        PersonaProfileScreen(
                            persona = persona,
                            posts = personaPosts,
                            totalLikesReceived = likesReceived,
                            isActivePersona = true,
                            onSetActivePersona = {},
                            onEditPersona = { currentScreen = Screen.EditPersona(persona.id) },
                            onPostClick = { postId ->
                                viewModel.openPostDetails(postId)
                                currentScreen = Screen.PostDetail(postId)
                            },
                            onLikePost = { postId -> viewModel.togglePostLike(postId) },
                            onOpenMedia = { path -> fullscreenMediaPath = path },
                            onMentionClick = { username ->
                                val target = allPersonas.find { it.username.equals(username, ignoreCase = true) }
                                if (target != null) currentScreen = Screen.ViewProfile(target.id)
                            },
                            onEditPost = { postWithDetails -> postToEdit = postWithDetails },
                            onDeletePost = { postId -> viewModel.deletePost(postId) },
                            onBack = { currentScreen = Screen.Feed }
                        )
                    } ?: run {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No active persona. Create one first!")
                        }
                    }
                }

                is Screen.Backup -> {
                    BackupSettingsScreen(
                        mediaQuality = mediaQuality,
                        onSetMediaQuality = { viewModel.setMediaQuality(it) },
                        onExportBackup = { uri -> viewModel.exportBackup(uri) },
                        onImportBackup = { uri, isRestore -> viewModel.importBackup(uri, isRestore) },
                        isProcessing = isProcessingBackup,
                        exportResult = exportSummary,
                        importResult = importSummary,
                        onClearResults = { viewModel.clearBackupResults() }
                    )
                }

                is Screen.EditPersona -> {
                    val initialPersona = screen.personaId?.let { id -> allPersonas.find { it.id == id } }
                    EditPersonaScreen(
                        initialPersona = initialPersona,
                        onSave = { updatedPersona, avatarUri, coverUri ->
                            viewModel.savePersona(updatedPersona, avatarUri, coverUri)
                            currentScreen = Screen.Personas
                        },
                        onBack = { currentScreen = Screen.Personas }
                    )
                }

                is Screen.ViewProfile -> {
                    val persona = allPersonas.find { it.id == screen.personaId }
                    if (persona != null) {
                        val personaPosts = feedPosts.filter { it.author.id == persona.id }
                        val likesReceived = personaPosts.sumOf { it.likeCount }
                        PersonaProfileScreen(
                            persona = persona,
                            posts = personaPosts,
                            totalLikesReceived = likesReceived,
                            isActivePersona = activePersona?.id == persona.id,
                            onSetActivePersona = { viewModel.selectActivePersona(persona.id) },
                            onEditPersona = { currentScreen = Screen.EditPersona(persona.id) },
                            onPostClick = { postId ->
                                viewModel.openPostDetails(postId)
                                currentScreen = Screen.PostDetail(postId)
                            },
                            onLikePost = { postId -> viewModel.togglePostLike(postId) },
                            onOpenMedia = { path -> fullscreenMediaPath = path },
                            onMentionClick = { username ->
                                val target = allPersonas.find { it.username.equals(username, ignoreCase = true) }
                                if (target != null) currentScreen = Screen.ViewProfile(target.id)
                            },
                            onEditPost = { postWithDetails -> postToEdit = postWithDetails },
                            onDeletePost = { postId -> viewModel.deletePost(postId) },
                            onBack = { currentScreen = Screen.Personas }
                        )
                    }
                }

                is Screen.Chat -> {
                    val other = allPersonas.find { it.id == screen.otherPersonaId }
                    if (activePersona != null && other != null) {
                        ChatScreen(
                            conversationId = screen.conversationId,
                            activePersona = activePersona!!,
                            otherPersona = other,
                            messages = currentChatMessages,
                            onSendMessage = { text, uri, mime, replyId ->
                                viewModel.sendMessage(screen.conversationId, text, uri, mime, replyId)
                            },
                            onEditMessage = { msgId, newText -> viewModel.editMessage(msgId, newText) },
                            onDeleteMessage = { msgId -> viewModel.deleteMessage(msgId) },
                            onMediaClick = { path -> fullscreenMediaPath = path },
                            onProfileClick = { personaId -> currentScreen = Screen.ViewProfile(personaId) },
                            onBack = {
                                viewModel.closeChat()
                                currentScreen = Screen.Messages
                            }
                        )
                    }
                }

                is Screen.PostDetail -> {
                    if (currentPostDetails != null && activePersona != null) {
                        PostDetailScreen(
                            postWithDetails = currentPostDetails!!,
                            commentsTree = currentPostComments,
                            activePersona = activePersona!!,
                            allPersonas = allPersonas,
                            onLikePost = { viewModel.togglePostLike(currentPostDetails!!.post.id) },
                            onLikeComment = { commentId -> viewModel.toggleCommentLike(commentId) },
                            onDeleteComment = { commentId -> viewModel.deleteComment(commentId) },
                            onAddComment = { text, parentId ->
                                viewModel.addComment(currentPostDetails!!.post.id, text, parentId)
                            },
                            onProfileClick = { personaId -> currentScreen = Screen.ViewProfile(personaId) },
                            onMediaClick = { path -> fullscreenMediaPath = path },
                            onMentionClick = { username ->
                                val target = allPersonas.find { it.username.equals(username, ignoreCase = true) }
                                if (target != null) currentScreen = Screen.ViewProfile(target.id)
                            },
                            onEditPost = { postWithDetails -> postToEdit = postWithDetails },
                            onDeletePost = { postId ->
                                viewModel.deletePost(postId)
                                viewModel.closePostDetails()
                                currentScreen = Screen.Feed
                            },
                            onEditComment = { commentId, newText ->
                                viewModel.updateComment(commentId, newText)
                            },
                            onBack = {
                                viewModel.closePostDetails()
                                currentScreen = Screen.Feed
                            }
                        )
                    }
                }
            }

            // Create Post Sheet (Full Window)
            if (showCreatePostSheet && activePersona != null) {
                CreatePostDialog(
                    activePersona = activePersona!!,
                    allPersonas = allPersonas,
                    initialDraft = activePersonaDraft,
                    isPublishing = isPublishingPost,
                    onDraftChange = { text ->
                        viewModel.savePostDraft(activePersona!!.id, text)
                    },
                    onPostCreated = { text, mediaList ->
                        viewModel.createPost(text, mediaList) {
                            showCreatePostSheet = false
                        }
                    },
                    onDismiss = { showCreatePostSheet = false }
                )
            }

            // Edit Post Dialog (Full Window)
            postToEdit?.let { targetPost ->
                if (activePersona != null) {
                    EditPostDialog(
                        postWithDetails = targetPost,
                        activePersona = activePersona!!,
                        allPersonas = allPersonas,
                        onSave = { newText, keptMediaIds, newMediaUris ->
                            viewModel.updatePostWithMedia(
                                postId = targetPost.post.id,
                                newText = newText,
                                keptMediaIds = keptMediaIds,
                                newMediaUris = newMediaUris
                            ) {
                                postToEdit = null
                            }
                        },
                        onDismiss = { postToEdit = null }
                    )
                }
            }

            // Persona Switcher Bottom Sheet
            if (showPersonaSwitcherSheet) {
                PersonaSwitcherBottomSheet(
                    sheetState = switcherSheetState,
                    personas = allPersonas,
                    activePersonaId = activePersona?.id,
                    onPersonaSelect = { personaId ->
                        viewModel.selectActivePersona(personaId)
                        coroutineScope.launch { switcherSheetState.hide() }
                            .invokeOnCompletion { showPersonaSwitcherSheet = false }
                    },
                    onCreateNewPersona = {
                        coroutineScope.launch { switcherSheetState.hide() }
                            .invokeOnCompletion {
                                showPersonaSwitcherSheet = false
                                currentScreen = Screen.EditPersona(null)
                            }
                    },
                    onEditPersona = { personaId ->
                        coroutineScope.launch { switcherSheetState.hide() }
                            .invokeOnCompletion {
                                showPersonaSwitcherSheet = false
                                currentScreen = Screen.EditPersona(personaId)
                            }
                    },
                    onDismiss = { showPersonaSwitcherSheet = false }
                )
            }

            // Fullscreen Media Viewer Dialog
            fullscreenMediaPath?.let { path ->
                MediaViewerDialog(
                    mediaPath = path,
                    onDismiss = { fullscreenMediaPath = null }
                )
            }
        }
    }
}
