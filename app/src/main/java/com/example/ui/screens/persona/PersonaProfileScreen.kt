package com.example.ui.screens.persona

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.PersonaEntity
import com.example.data.local.model.PostWithDetails
import com.example.ui.components.PersonaAvatar
import com.example.ui.screens.feed.PostCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LikeRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaProfileScreen(
    persona: PersonaEntity,
    posts: List<PostWithDetails>,
    totalLikesReceived: Int,
    isActivePersona: Boolean,
    onSetActivePersona: () -> Unit,
    onEditPersona: () -> Unit,
    onPostClick: (String) -> Unit,
    onLikePost: (String) -> Unit,
    onOpenMedia: (String) -> Unit,
    onMentionClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val coverFile = remember(persona.coverPath) {
        if (!persona.coverPath.isNullOrBlank()) {
            if (persona.coverPath.startsWith("/")) File(persona.coverPath)
            else File(context.filesDir, persona.coverPath)
        } else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(persona.displayName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditPersona) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit persona")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            // Header: Cover Photo & Avatar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    if (coverFile != null && coverFile.exists()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(coverFile).build(),
                            contentDescription = "Cover photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(DarkSurfaceElevated)
                        )
                    }

                    // Avatar overlapping bottom of cover
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp)
                    ) {
                        PersonaAvatar(
                            name = persona.displayName,
                            avatarPath = persona.avatarPath,
                            size = 80.dp,
                            borderWidth = 3.dp,
                            borderColor = if (isActivePersona) NeonCyan else DarkSurface
                        )
                    }

                    // Action button on top right of profile info
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 4.dp)
                    ) {
                        if (isActivePersona) {
                            Surface(
                                color = NeonCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Active Identity", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        } else {
                            Button(
                                onClick = onSetActivePersona,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Switch to Persona", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Persona details
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = persona.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${persona.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (persona.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = persona.bio,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${posts.size}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NeonCyan)
                            Text("Posts", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(DarkCardBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = LikeRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$totalLikesReceived", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = LikeRed)
                            }
                            Text("Likes Received", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tab selector
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DarkSurface,
                        contentColor = NeonCyan,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = NeonCyan
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Posts (${posts.size})", fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Roleplay Dossier", fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }

            // Content based on tab
            if (selectedTab == 0) {
                if (posts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No posts published yet by @${persona.username}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(posts, key = { it.post.id }) { postDetails ->
                        PostCard(
                            postWithDetails = postDetails,
                            onPostClick = { onPostClick(postDetails.post.id) },
                            onLikeClick = { onLikePost(postDetails.post.id) },
                            onProfileClick = { /* already here */ },
                            onMediaClick = onOpenMedia,
                            onMentionClick = onMentionClick,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            } else {
                // Roleplay Dossier Tab
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (persona.personalityTraits.isNotBlank()) {
                            DossierCard(
                                title = "Personality Traits",
                                content = persona.personalityTraits,
                                icon = Icons.Default.Psychology,
                                tint = NeonViolet
                            )
                        }

                        if (persona.speakingStyle.isNotBlank()) {
                            DossierCard(
                                title = "Speaking Style & Mannerisms",
                                content = persona.speakingStyle,
                                icon = Icons.Default.Forum,
                                tint = NeonCyan
                            )
                        }

                        if (persona.roleplayInstructions.isNotBlank()) {
                            DossierCard(
                                title = "Roleplay Lore Rules & Guidelines",
                                content = persona.roleplayInstructions,
                                icon = Icons.Default.AutoAwesome,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (persona.backgroundStory.isNotBlank()) {
                            DossierCard(
                                title = "Background History & Lore",
                                content = persona.backgroundStory,
                                icon = Icons.Default.AutoAwesome,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        if (persona.personalityTraits.isBlank() &&
                            persona.speakingStyle.isBlank() &&
                            persona.roleplayInstructions.isBlank() &&
                            persona.backgroundStory.isBlank()
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("No roleplay lore defined yet", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Add speaking styles, traits, and background lore to empower the offline roleplay writing assistant.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(onClick = onEditPersona) {
                                        Text("Edit Character Dossier")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DossierCard(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, color = tint, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
