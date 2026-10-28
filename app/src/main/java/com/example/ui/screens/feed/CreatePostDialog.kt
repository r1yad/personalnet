package com.example.ui.screens.feed

import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostDialog(
    activePersona: PersonaEntity,
    allPersonas: List<PersonaEntity>,
    onPostCreated: (String, List<Pair<Uri, String?>>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var textContent by remember { mutableStateOf("") }
    val selectedMediaUris = remember { mutableStateListOf<Pair<Uri, String?>>() }
    var selectedTone by remember { mutableStateOf(RoleplayAssistant.RoleplayTone.CASUAL) }
    var showAssistant by remember { mutableStateOf(false) }

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
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

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                maxLines = 8,
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
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedMediaUris.forEachIndexed { index, (uri, _) ->
                        Box(
                            modifier = Modifier
                                .size(90.dp)
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
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Roleplay Assistant Accordion / Toggle
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
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(18.dp))
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
                        fontSize = 12.sp,
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
                        .padding(12.dp)
                ) {
                    Text("Select Roleplay Tone:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))

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
                    Text("Tap to insert in-character prompt:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))

                    assistantSuggestions.forEach { suggestion ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    textContent = if (textContent.isBlank()) suggestion.text else "$textContent\n${suggestion.text}"
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(suggestion.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonViolet)
                                Text("\"${suggestion.text}\"", fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Bar: Attach Media, Character counter, Publish Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { mediaPicker.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = NeonCyan),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Media (${selectedMediaUris.size})", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        if (textContent.isNotBlank() || selectedMediaUris.isNotEmpty()) {
                            onPostCreated(textContent.trim(), selectedMediaUris.toList())
                            onDismiss()
                        }
                    },
                    enabled = textContent.isNotBlank() || selectedMediaUris.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("publish_post_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publish", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
