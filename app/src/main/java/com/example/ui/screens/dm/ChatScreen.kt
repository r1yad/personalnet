package com.example.ui.screens.dm

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.PersonaEntity
import com.example.data.local.model.MessageWithDetails
import com.example.domain.roleplay.RoleplayAssistant
import com.example.ui.components.PersonaAvatar
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    activePersona: PersonaEntity,
    otherPersona: PersonaEntity,
    messages: List<MessageWithDetails>,
    onSendMessage: (String, Uri?, String?, String?) -> Unit,
    onEditMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onMediaClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var replyingToMessage by remember { mutableStateOf<MessageWithDetails?>(null) }
    var editingMessage by remember { mutableStateOf<MessageWithDetails?>(null) }
    var showAssistantChips by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) selectedMediaUri = uri
    }

    val rpQuickResponses = remember(activePersona) {
        RoleplayAssistant.generateReplySuggestions(activePersona, RoleplayAssistant.RoleplayTone.CASUAL)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onProfileClick(otherPersona.id) }
                    ) {
                        PersonaAvatar(
                            name = otherPersona.displayName,
                            avatarPath = otherPersona.avatarPath,
                            size = 38.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(otherPersona.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("@${otherPersona.username}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Reply target banner
                replyingToMessage?.let { replyTarget ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Replying to ${replyTarget.sender.displayName}", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                            Text(replyTarget.message.textContent, fontSize = 12.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(onClick = { replyingToMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // Media attachment preview
                selectedMediaUri?.let { uri ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(uri).build(),
                                contentDescription = "Attached media",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { selectedMediaUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }

                // RP Quick responses
                if (showAssistantChips) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rpQuickResponses.forEach { prompt ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { inputText = prompt },
                                color = NeonViolet.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonViolet.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = prompt,
                                    fontSize = 11.sp,
                                    color = NeonViolet,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Input bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showAssistantChips = !showAssistantChips },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "RP Helpers", tint = NeonViolet, modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = { mediaPicker.launch("image/*") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Attach image", tint = NeonCyan, modifier = Modifier.size(20.dp))
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Message as @${activePersona.username}...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(22.dp),
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
                            if (inputText.isNotBlank() || selectedMediaUri != null) {
                                val mime = selectedMediaUri?.let { context.contentResolver.getType(it) ?: "image/webp" }
                                onSendMessage(inputText.trim(), selectedMediaUri, mime, replyingToMessage?.message?.id)
                                inputText = ""
                                selectedMediaUri = null
                                replyingToMessage = null
                            }
                        },
                        enabled = inputText.isNotBlank() || selectedMediaUri != null,
                        modifier = Modifier.testTag("send_chat_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() || selectedMediaUri != null) NeonCyan else Color.Gray
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.message.id }) { msgDetails ->
                val isFromMe = msgDetails.sender.id == activePersona.id
                MessageBubble(
                    msgDetails = msgDetails,
                    isFromMe = isFromMe,
                    onReply = { replyingToMessage = msgDetails },
                    onEdit = { editingMessage = msgDetails },
                    onDelete = { onDeleteMessage(msgDetails.message.id) },
                    onMediaClick = onMediaClick
                )
            }
        }
    }

    // Edit message modal
    editingMessage?.let { msg ->
        var editDraft by remember { mutableStateOf(msg.message.textContent) }
        AlertDialog(
            onDismissRequest = { editingMessage = null },
            title = { Text("Edit Message") },
            text = {
                OutlinedTextField(
                    value = editDraft,
                    onValueChange = { editDraft = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onEditMessage(msg.message.id, editDraft.trim())
                    editingMessage = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMessage = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MessageBubble(
    msgDetails: MessageWithDetails,
    isFromMe: Boolean,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMediaClick: (String) -> Unit
) {
    val context = LocalContext.current
    val message = msgDetails.message
    var menuExpanded by remember { mutableStateOf(false) }

    val formattedTime = remember(message.createdAt) {
        DateFormat.format("hh:mm a", message.createdAt).toString()
    }

    val bubbleColor = if (isFromMe) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceElevated
    val borderColor = if (isFromMe) NeonCyan.copy(alpha = 0.5f) else DarkCardBorder

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromMe) {
            PersonaAvatar(name = msgDetails.sender.displayName, avatarPath = msgDetails.sender.avatarPath, size = 32.dp)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box {
            Card(
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isFromMe) 14.dp else 2.dp,
                    bottomEnd = if (isFromMe) 2.dp else 14.dp
                ),
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clickable { menuExpanded = true }
                    .testTag("chat_bubble_${message.id}")
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Quoted reply if exists
                    if (!msgDetails.replyToText.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.25f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text(
                                    text = msgDetails.replyToSenderName ?: "Reply",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                                Text(
                                    text = msgDetails.replyToText,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Media
                    if (!message.mediaPath.isNullOrBlank()) {
                        val file = File(context.filesDir, message.mediaPath)
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(file).crossfade(true).build(),
                            contentDescription = "Message media",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onMediaClick(message.mediaPath) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Message text
                    if (message.textContent.isNotBlank()) {
                        Text(
                            text = message.textContent,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 19.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isEdited) {
                            Text("edited • ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formattedTime, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Reply") },
                    leadingIcon = { Icon(Icons.Default.Reply, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onReply()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Copy Text") },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Message Text", message.textContent))
                    }
                )
                if (isFromMe) {
                    DropdownMenuItem(
                        text = { Text("Edit Message") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
