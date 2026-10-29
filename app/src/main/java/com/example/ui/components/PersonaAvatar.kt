package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import java.io.File
import kotlin.math.abs

@Composable
fun PersonaAvatar(
    name: String,
    avatarPath: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = MaterialTheme.colorScheme.primary
) {
    val context = LocalContext.current
    val avatarFile = remember(avatarPath) {
        if (!avatarPath.isNullOrBlank()) {
            if (avatarPath.startsWith("/")) File(avatarPath)
            else File(context.filesDir, avatarPath)
        } else null
    }

    val initials = remember(name) {
        name.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifBlank { "?" }
    }

    val gradientBrush = remember(name) {
        val hash = abs(name.hashCode())
        val colors = listOf(
            listOf(Color(0xFF00E5FF), Color(0xFF0072FF)),
            listOf(Color(0xFFB388FF), Color(0xFF651FFF)),
            listOf(Color(0xFFFF4081), Color(0xFFC51162)),
            listOf(Color(0xFF00E676), Color(0xFF00B0FF)),
            listOf(Color(0xFFFFAB00), Color(0xFFFF3D00))
        )
        val palette = colors[hash % colors.size]
        Brush.linearGradient(palette)
    }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, borderColor, CircleShape)
                } else Modifier
            )
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (avatarFile != null && avatarFile.exists()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(avatarFile)
                    .crossfade(true)
                    .allowHardware(true)
                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                    .build(),
                contentDescription = "$name avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .background(gradientBrush),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.42f).sp
                )
            }
        }
    }
}
