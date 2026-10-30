package com.example.ui.screens.feed

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PersonaEntity
import com.example.data.local.model.CommentWithDetails
import com.example.data.local.model.PostWithDetails
import com.example.domain.roleplay.RoleplayAssistant
import com.example.ui.components.MentionText
import com.example.ui.components.PersonaAvatar
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LikeRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postWithDetails: PostWithDetails,
    commentsTree: List<CommentWithDetails>,
    activePersona: PersonaEntity,
    allPersonas: List<PersonaEntity>,
    onLikePost: () -> Unit,
    onLikeComment: (String) -> Unit,
    onDeleteComment: (String) -> Unit,
    onAddComment: (String, String?) -> Unit,
    onProfileClick: (String) -> Unit,
    onMediaClick: (String) -> Unit,
    onMentionClick: (String) -> Unit,
    onEditPost: (PostWithDetails) -> Unit = {},
    onDeletePost: (String) -> Unit = {},
    onEditComment: (String, String) -> Unit = { _, _ -> },
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var commentText by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<CommentWithDetails?>(null) }
    var editingComment by remember { mutableStateOf<CommentWithDetails?>(null) }
    var showRpReplies by remember { mutableStateOf(false) }

    val rpQuickReplies = remember(activePersona) {
        RoleplayAssistant.generateReplySuggestions(activePersona, RoleplayAssistant.RoleplayTone.CASUAL)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        bottomBar = {
            // Comment input bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // If replying to a specific comment
                replyingToComment?.let { replyTarget ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Reply, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Replying to @${replyTarget.author.username}",
                                fontSize = 12.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = { replyingToComment = null },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // Quick roleplay reply chips
                if (showRpReplies) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rpQuickReplies.take(2).forEach { reply ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { commentText = reply },
                                color = NeonViolet.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonViolet.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = reply,
                                    fontSize = 11.sp,
                                    color = NeonViolet,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PersonaAvatar(
                        name = activePersona.displayName,
                        avatarPath = activePersona.avatarPath,
                        size = 36.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = {
                            Text("Reply as @${activePersona.username}...", fontSize = 13.sp)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_field"),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedContainerColor = DarkSurfaceElevated
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onAddComment(commentText.trim(), replyingToComment?.comment?.id)
                                commentText = ""
                                replyingToComment = null
                            }
                        },
                        enabled = commentText.isNotBlank(),
                        modifier = Modifier.testTag("submit_comment_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send comment",
                            tint = if (commentText.isNotBlank()) NeonCyan else Color.Gray
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Main Post
            item {
                val isPostAuthor = postWithDetails.author.id == activePersona.id
                PostCard(
                    postWithDetails = postWithDetails,
                    onPostClick = {},
                    onLikeClick = onLikePost,
                    onProfileClick = { onProfileClick(postWithDetails.author.id) },
                    onMediaClick = onMediaClick,
                    onMentionClick = onMentionClick,
                    onEditClick = if (isPostAuthor) { { onEditPost(postWithDetails) } } else null,
                    onDeleteClick = if (isPostAuthor) { { onDeletePost(postWithDetails.post.id) } } else null
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Replies (${postWithDetails.commentCount})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (showRpReplies) "Hide RP helpers" else "✨ RP Helpers",
                        fontSize = 12.sp,
                        color = NeonViolet,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showRpReplies = !showRpReplies }
                            .padding(4.dp)
                    )
                }
            }

            if (commentsTree.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No comments yet. Be the first to reply as ${activePersona.displayName}!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(commentsTree, key = { it.comment.id }) { rootComment ->
                    CommentNodeView(
                        commentNode = rootComment,
                        depth = 0,
                        activePersonaId = activePersona.id,
                        onReplyClick = { target ->
                            replyingToComment = target
                            commentText = "@${target.author.username} "
                        },
                        onEditClick = { target -> editingComment = target },
                        onLikeClick = onLikeComment,
                        onDeleteClick = onDeleteComment,
                        onProfileClick = onProfileClick,
                        onMentionClick = onMentionClick
                    )
                }
            }
        }
    }

    // Full-Window Comment Editor
    editingComment?.let { targetComment ->
        EditCommentDialog(
            commentWithDetails = targetComment,
            activePersona = activePersona,
            allPersonas = allPersonas,
            onSave = { newText ->
                onEditComment(targetComment.comment.id, newText)
                editingComment = null
            },
            onDismiss = { editingComment = null }
        )
    }
}

@Composable
fun CommentNodeView(
    commentNode: CommentWithDetails,
    depth: Int,
    activePersonaId: String,
    onReplyClick: (CommentWithDetails) -> Unit,
    onEditClick: (CommentWithDetails) -> Unit,
    onLikeClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onMentionClick: (String) -> Unit
) {
    val comment = commentNode.comment
    val author = commentNode.author
    val indentPadding = minOf(depth * 16, 48).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indentPadding)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = BorderStroke(1.dp, if (depth > 0) DarkCardBorder.copy(alpha = 0.5f) else DarkCardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Comment header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onProfileClick(author.id) }
                    ) {
                        PersonaAvatar(
                            name = author.displayName,
                            avatarPath = author.avatarPath,
                            size = 32.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = author.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "@${author.username}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                commentNode.replyToAuthorName?.let { replyToName ->
                                    Text(
                                        text = " ↳ @$replyToName",
                                        fontSize = 11.sp,
                                        color = NeonCyan
                                    )
                                }
                            }
                        }
                    }

                    if (author.id == activePersonaId) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onEditClick(commentNode) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit comment",
                                    tint = NeonCyan.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { onDeleteClick(comment.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete comment",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                MentionText(
                    text = comment.textContent,
                    onMentionClick = onMentionClick,
                    fontSize = 14.sp,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Actions: Like comment & Reply
                var isLikedOptimistic by remember(commentNode.isLikedByActivePersona) {
                    mutableStateOf(commentNode.isLikedByActivePersona)
                }
                var likeCountOptimistic by remember(commentNode.likeCount) {
                    mutableIntStateOf(commentNode.likeCount)
                }
                var animateTrigger by remember { mutableStateOf(false) }

                val heartScale by animateFloatAsState(
                    targetValue = if (animateTrigger) 1.35f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    finishedListener = { animateTrigger = false },
                    label = "comment_heart_bounce"
                )

                val handleLikeTap = {
                    val willBeLiked = !isLikedOptimistic
                    isLikedOptimistic = willBeLiked
                    likeCountOptimistic = if (willBeLiked) likeCountOptimistic + 1 else maxOf(0, likeCountOptimistic - 1)
                    if (willBeLiked) {
                        animateTrigger = true
                    }
                    onLikeClick(comment.id)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { handleLikeTap() }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isLikedOptimistic) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like comment",
                            tint = if (isLikedOptimistic) LikeRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(16.dp)
                                .graphicsLayer {
                                    scaleX = heartScale
                                    scaleY = heartScale
                                }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$likeCountOptimistic",
                            fontSize = 12.sp,
                            fontWeight = if (isLikedOptimistic) FontWeight.Bold else FontWeight.Normal,
                            color = if (isLikedOptimistic) LikeRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onReplyClick(commentNode) }
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.Reply, contentDescription = "Reply", tint = NeonCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reply", fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Render nested replies recursively
        if (commentNode.replies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            commentNode.replies.forEach { childReply ->
                CommentNodeView(
                    commentNode = childReply,
                    depth = depth + 1,
                    activePersonaId = activePersonaId,
                    onReplyClick = onReplyClick,
                    onEditClick = onEditClick,
                    onLikeClick = onLikeClick,
                    onDeleteClick = onDeleteClick,
                    onProfileClick = onProfileClick,
                    onMentionClick = onMentionClick
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}
