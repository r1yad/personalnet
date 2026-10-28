package com.example.ui.screens.persona

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.components.PersonaAvatar
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPersonaScreen(
    initialPersona: PersonaEntity? = null,
    onSave: (PersonaEntity, Uri?, Uri?) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var displayName by remember { mutableStateOf(initialPersona?.displayName ?: "") }
    var username by remember { mutableStateOf(initialPersona?.username ?: "") }
    var bio by remember { mutableStateOf(initialPersona?.bio ?: "") }
    var personalityTraits by remember { mutableStateOf(initialPersona?.personalityTraits ?: "") }
    var speakingStyle by remember { mutableStateOf(initialPersona?.speakingStyle ?: "") }
    var roleplayInstructions by remember { mutableStateOf(initialPersona?.roleplayInstructions ?: "") }
    var backgroundStory by remember { mutableStateOf(initialPersona?.backgroundStory ?: "") }

    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCoverUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) selectedAvatarUri = uri
    }

    val coverPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) selectedCoverUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (initialPersona == null) "Create Persona" else "Edit Persona",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (displayName.isBlank()) {
                                errorMessage = "Please enter a display name"
                                return@Button
                            }
                            if (username.isBlank()) {
                                errorMessage = "Please enter a username"
                                return@Button
                            }
                            val cleanedUsername = username.trim().replace("@", "").replace(" ", "_")
                            val persona = (initialPersona ?: PersonaEntity(
                                id = UUID.randomUUID().toString(),
                                displayName = displayName.trim(),
                                username = cleanedUsername,
                                createdAt = System.currentTimeMillis()
                            )).copy(
                                displayName = displayName.trim(),
                                username = cleanedUsername,
                                bio = bio.trim(),
                                personalityTraits = personalityTraits.trim(),
                                speakingStyle = speakingStyle.trim(),
                                roleplayInstructions = roleplayInstructions.trim(),
                                backgroundStory = backgroundStory.trim(),
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(persona, selectedAvatarUri, selectedCoverUri)
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_persona_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Cover and Avatar Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceElevated)
                    .clickable { coverPicker.launch("image/*") }
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedCoverUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(selectedCoverUri).build(),
                        contentDescription = "Cover preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!initialPersona?.coverPath.isNullOrBlank()) {
                    val coverFile = File(context.filesDir, initialPersona!!.coverPath!!)
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(coverFile).build(),
                        contentDescription = "Cover preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = NeonViolet)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tap to add Cover Photo", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }

                // Avatar overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .clickable { avatarPicker.launch("image/*") }
                ) {
                    if (selectedAvatarUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(selectedAvatarUri).build(),
                            contentDescription = "Avatar preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(3.dp, NeonCyan, CircleShape)
                        )
                    } else {
                        PersonaAvatar(
                            name = displayName.ifBlank { "New" },
                            avatarPath = initialPersona?.avatarPath,
                            size = 72.dp,
                            borderWidth = 3.dp,
                            borderColor = NeonCyan
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(26.dp)
                            .background(NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change avatar",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Identity Inputs
            Text("Identity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NeonCyan)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name (e.g. Elena Vance)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("persona_display_name_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username handle (e.g. elena_v)") },
                prefix = { Text("@", color = NeonCyan, fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("persona_username_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Public Bio") },
                placeholder = { Text("Short introduction or public persona status") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("persona_bio_input"),
                minLines = 2,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Roleplay Instructions & Traits Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = NeonViolet)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Roleplay & Character Traits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonViolet
                )
            }
            Text(
                "These define how this persona behaves and guides the offline roleplay writing assistant.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = personalityTraits,
                onValueChange = { personalityTraits = it },
                label = { Text("Personality Traits") },
                placeholder = { Text("e.g. Sarcastic, hyper-intelligent, cautious, loyal") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonViolet)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = speakingStyle,
                onValueChange = { speakingStyle = it },
                label = { Text("Speaking Style & Dialect") },
                placeholder = { Text("e.g. Formal tone, uses archaic idioms, brief sentences") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonViolet)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = roleplayInstructions,
                onValueChange = { roleplayInstructions = it },
                label = { Text("Roleplay Instructions / Lore Rules") },
                placeholder = { Text("e.g. Never reveals secret organization; obsessed with clocks") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonViolet)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = backgroundStory,
                onValueChange = { backgroundStory = it },
                label = { Text("Background Story / History") },
                placeholder = { Text("Origin story, key events, relationships...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonViolet)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
