package com.example.ui.screens.feed

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.PersonaEntity
import com.example.domain.roleplay.RoleplayAssistant
import com.example.ui.components.PersonaAvatar
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostDialog(
    activePersona: PersonaEntity,
    allPersonas: List<PersonaEntity>,
    initialDraft: String = "",
    isPublishing: Boolean = false,
    onDraftChange: (String) -> Unit = {},
    onPostCreated: (String, List<Pair<Uri, String?>>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var textContent by remember { mutableStateOf(initialDraft) }
    val selectedMediaUris = remember { mutableStateListOf<Pair<Uri, String?>>() }
    var selectedTone by remember { mutableStateOf(RoleplayAssistant.RoleplayTone.CASUAL) }
    var showAssistant by remember { mutableStateOf(false) }

    // Automatic draft saving with 500ms debounce
    LaunchedEffect(textContent) {
        delay(500)
        onDraftChange(textContent)
    }

    val maxPhotos = 5
    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val availableSlots = maxPhotos - selectedMediaUris.size
        uris.take(availableSlots).forEach { uri ->
            val mimeType = context.contentResolver.getType(uri) ?: "image/webp"
            selectedMediaUris.add(Pair(uri, mimeType))
        }
    }

    // Mention suggestions: check if user is typing @
    val mentionQuery = remember(textContent) {
        val lastWord = textContent.split(" ", "\n").lastOrNull() ?: ""
        if (lastWord.startsWith("@") && lastWord.length > 1) {
            lastWord.removePrefix("@").lowercase()
        } else null
    }

    val suggestedPersonas = remember(mentionQuery, allPersonas) {
        if (mentionQuery != null) {
            allPersonas.filter {
                it.username.lowercase().contains(mentionQuery) ||
                it.displayName.lowercase().contains(mentionQuery)
            }.take(4)
        } else emptyList()
    }

    val assistantSuggestions = remember(activePersona, selectedTone) {
        RoleplayAssistant.generatePostSuggestions(activePersona, selectedTone)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        val isKeyboardOpen = WindowInsets.isImeVisible
        val keyboardController = LocalSoftwareKeyboardController.current

        BackHandler(enabled = true) {
            if (isKeyboardOpen) {
                keyboardController?.hide()
            } else {
                onDismiss()
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkSurface),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PersonaAvatar(
                        name = activePersona.displayName,
                        avatarPath = activePersona.avatarPath,
                        size = 42.dp,
                        borderWidth = 1.5.dp,
                        borderColor = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Posting as ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                activePersona.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NeonCyan
                            )
                        }
                        Text(
                            "@${activePersona.username}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (textContent.isNotBlank()) {
                        IconButton(
                            onClick = {
                                textContent = ""
                                onDraftChange("")
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear draft", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Content Area (Middle)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Post content input
                OutlinedTextField(
                    value = textContent,
                    onValueChange = { textContent = it },
                    placeholder = {
                        Text("What's unfolding in ${activePersona.displayName}'s world? Type @ to mention personas.")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_post_text_input"),
                    minLines = 4,
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        focusedContainerColor = DarkSurfaceElevated
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Mention Suggestions Popup
                if (suggestedPersonas.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Mention Suggestions:", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                            suggestedPersonas.forEach { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            val words = textContent.split(" ").toMutableList()
                                            if (words.isNotEmpty()) {
                                                words[words.size - 1] = "@${p.username} "
                                            }
                                            textContent = words.joinToString(" ")
                                        }
                                        .padding(vertical = 4.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PersonaAvatar(name = p.displayName, avatarPath = p.avatarPath, size = 24.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(p.displayName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("@${p.username}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Media attachment previews
                if (selectedMediaUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Attached Photos (${selectedMediaUris.size}/$maxPhotos):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedMediaUris.forEachIndexed { index, (uri, _) ->
                            Box(
                                modifier = Modifier
                                    .size(85.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(uri).build(),
                                    contentDescription = "Selected media",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { selectedMediaUris.removeAt(index) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(22.dp)
                                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Roleplay Assistant Accordion
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showAssistant = !showAssistant },
                    color = if (showAssistant) NeonViolet.copy(alpha = 0.15f) else DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (showAssistant) NeonViolet else DarkCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Offline Roleplay Assistant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NeonViolet
                            )
                        }
                        Text(
                            if (showAssistant) "Hide ▲" else "Open Suggestions ▼",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (showAssistant) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text("Select Tone:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            RoleplayAssistant.RoleplayTone.values().forEach { tone ->
                                FilterChip(
                                    selected = selectedTone == tone,
                                    onClick = { selectedTone = tone },
                                    label = { Text("${tone.icon} ${tone.label}", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonViolet.copy(alpha = 0.3f),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        assistantSuggestions.forEach { suggestion ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        textContent = if (textContent.isBlank()) suggestion.text else "$textContent\n${suggestion.text}"
                                    },
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text(suggestion.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonViolet)
                                    Text("\"${suggestion.text}\"", fontSize = 11.sp, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Docked Bottom Action Bar (Pinned Above Software Keyboard!)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(top = 6.dp, bottom = 4.dp),
                color = DarkSurface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            if (selectedMediaUris.size < maxPhotos) {
                                mediaPicker.launch("image/*")
                            }
                        },
                        enabled = selectedMediaUris.size < maxPhotos,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Photos (${selectedMediaUris.size}/$maxPhotos)", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if ((textContent.isNotBlank() || selectedMediaUris.isNotEmpty()) && !isPublishing) {
                                onPostCreated(textContent.trim(), selectedMediaUris.toList())
                            }
                        },
                        enabled = (textContent.isNotBlank() || selectedMediaUris.isNotEmpty()) && !isPublishing,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("publish_post_button")
                    ) {
                        if (isPublishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Publishing...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Publish", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
}
