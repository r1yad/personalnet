package com.example.ui.screens.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.ExportSummary
import com.example.data.backup.ImportSummary
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
    mediaQuality: String,
    onSetMediaQuality: (String) -> Unit,
    onExportBackup: (Uri) -> Unit,
    onImportBackup: (Uri, Boolean) -> Unit, // uri, isRestoreMode
    isProcessing: Boolean,
    exportResult: ExportSummary?,
    importResult: ImportSummary?,
    onClearResults: () -> Unit
) {
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImportMode by remember { mutableStateOf("MERGE") } // "MERGE" or "RESTORE"

    // SAF Launchers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            onExportBackup(uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            if (selectedImportMode == "RESTORE") {
                pendingImportUri = uri
                showRestoreConfirmDialog = true
            } else {
                onImportBackup(uri, false)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status or progress
            if (isProcessing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Processing backup operation... Please wait", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Export Result Card
            exportResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.success) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                    ),
                    border = BorderStroke(1.dp, if (result.success) SuccessGreen else MaterialTheme.colorScheme.error)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (result.success) SuccessGreen else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (result.success) "Export Succeeded!" else "Export Failed",
                                fontWeight = FontWeight.Bold,
                                color = if (result.success) SuccessGreen else MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(result.message, fontSize = 13.sp)
                        if (result.success) {
                            Text(
                                "Saved ${result.totalPersonas} personas, ${result.totalPosts} posts, and ${result.totalMedia} media files.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Import Result Card
            importResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.success) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                    ),
                    border = BorderStroke(1.dp, if (result.success) SuccessGreen else MaterialTheme.colorScheme.error)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (result.success) SuccessGreen else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (result.success) "Import Summary" else "Import Failed",
                                fontWeight = FontWeight.Bold,
                                color = if (result.success) SuccessGreen else MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(result.message, fontSize = 13.sp)
                        if (result.success) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Personas Added: ${result.personasAdded}, Updated: ${result.personasUpdated}", fontSize = 12.sp)
                            Text("• Feed Posts Added: ${result.postsAdded}", fontSize = 12.sp)
                            Text("• Comments Added: ${result.commentsAdded}", fontSize = 12.sp)
                            Text("• Conversations Added: ${result.conversationsAdded}, Messages: ${result.messagesAdded}", fontSize = 12.sp)
                            Text("• Media Files Extracted: ${result.mediaFilesExtracted}", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Export Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(NeonCyan.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Export Network Archive", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Packages all personas, posts, chats & media into a .zip", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "Creates a versioned backup archive containing JSON databases and all compressed media files. Use Android Storage Access Framework to save wherever you choose.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val defaultFilename = "personanet_backup_${System.currentTimeMillis()}.zip"
                            exportLauncher.launch(defaultFilename)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_backup_button")
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export All Data (.ZIP)", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Import & Merge Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(NeonViolet.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Merge, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Import & Recovery", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Merge or restore a previously saved .zip backup", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode Selection: Merge vs Restore
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedImportMode == "MERGE",
                            onClick = { selectedImportMode = "MERGE" }
                        )
                        Column(modifier = Modifier.clickable { selectedImportMode = "MERGE" }) {
                            Text("Safe Merge (Recommended)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "Combines imported personas, posts and chats without overwriting existing local data.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedImportMode == "RESTORE",
                            onClick = { selectedImportMode = "RESTORE" }
                        )
                        Column(modifier = Modifier.clickable { selectedImportMode = "RESTORE" }) {
                            Text("Full Restore (Replace All)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WarningAmber)
                            Text(
                                "Wipes local database and replaces with archive contents. Requires confirmation.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            importLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedImportMode == "MERGE") NeonViolet else WarningAmber,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_backup_button")
                    ) {
                        Icon(
                            imageVector = if (selectedImportMode == "MERGE") Icons.Default.Merge else Icons.Default.Restore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedImportMode == "MERGE") "Select Backup to Merge" else "Select Backup to Restore",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Media Quality Preferences
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HighQuality, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Media Quality & Compression", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Controls how photos and videos are stored in app-private storage:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    listOf(
                        "STANDARD" to "Standard (WebP 85%, downscaled to max 1600px - Fast & compact)",
                        "HIGH" to "High Quality (WebP 95%, max 2560px)",
                        "ORIGINAL" to "Preserve Originals (Uncompressed copy)"
                    ).forEach { (qualityKey, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSetMediaQuality(qualityKey) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = mediaQuality == qualityKey,
                                onClick = { onSetMediaQuality(qualityKey) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Offline Architecture Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, DarkCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "PersonaNet operates 100% offline. No servers, trackers, or cloud logins. All personas, posts, chats, and media remain strictly on your device.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Confirmation dialog for Destructive Restore
    if (showRestoreConfirmDialog && pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pendingImportUri = null
            },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber) },
            title = { Text("Confirm Full Restore") },
            text = {
                Text("Warning: Full restore will replace all existing personas, posts, comments, and direct messages with the backup archive data. Are you sure you want to proceed?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        pendingImportUri?.let { onImportBackup(it, true) }
                        pendingImportUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Proceed with Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRestoreConfirmDialog = false
                    pendingImportUri = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
