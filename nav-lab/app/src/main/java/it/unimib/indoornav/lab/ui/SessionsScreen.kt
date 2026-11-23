package it.unimib.indoornav.lab.ui

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import it.unimib.indoornav.lab.data.SessionItem
import it.unimib.indoornav.lab.data.SessionRepository
import it.unimib.indoornav.lab.format.QuickLookStatus
import it.unimib.indoornav.lab.ui.model.ModelRenderer
import it.unimib.indoornav.lab.ui.theme.LabStatusColors
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@Composable
fun SessionsScreen(
    repository: SessionRepository,
    currentRenderer: ModelRenderer = ModelRenderer.GL,
    onOpenRendererBenchmark: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sessions by remember { mutableStateOf<List<SessionItem>>(emptyList()) }
    var surveyFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var pendingDeleteSession by remember { mutableStateOf<SessionItem?>(null) }
    var pendingDeleteSurvey by remember { mutableStateOf<File?>(null) }
    var overflowOpen by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch {
            sessions = repository.listSessions()
            surveyFiles = repository.listSurveyFiles()
        }
    }

    LaunchedEffect(Unit) {
        reload()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Recorded Sessions (${sessions.size})",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { reload() }) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Refresh")
                }
                Box {
                    IconButton(onClick = { overflowOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = overflowOpen,
                        onDismissRequest = { overflowOpen = false },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        DropdownMenuItem(
                            text = { Text("Renderer (${currentRenderer.label})") },
                            leadingIcon = {
                                Icon(Icons.Default.Speed, contentDescription = null)
                            },
                            onClick = {
                                overflowOpen = false
                                onOpenRendererBenchmark()
                            },
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(sessions, key = { it.folderName }) { item ->
                SessionCard(
                    item = item,
                    onShareZip = {
                        scope.launch {
                            val zip = repository.zipSession(item.dir)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                zip,
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/zip"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share ${item.folderName}.zip"))
                        }
                    },
                    onDelete = { pendingDeleteSession = item },
                )
            }

            if (surveyFiles.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(6.dp))
                    Text("Survey CSV Files (${surveyFiles.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                items(surveyFiles, key = { it.absolutePath }) { file ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(file.name, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                                Text(formatSize(file.length()), style = MaterialTheme.typography.bodySmall)
                            }
                            Row {
                                IconButton(onClick = {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file,
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share ${file.name}"))
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share CSV")
                                }
                                IconButton(onClick = { pendingDeleteSurvey = file }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete CSV")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDeleteSession?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDeleteSession = null },
            title = { Text("Delete session?") },
            text = { Text("Delete ${target.folderName} and all its recorded CSV files?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repository.deleteSession(target.dir)
                        pendingDeleteSession = null
                        reload()
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteSession = null }) { Text("Cancel") }
            },
        )
    }

    pendingDeleteSurvey?.let { file ->
        AlertDialog(
            onDismissRequest = { pendingDeleteSurvey = null },
            title = { Text("Delete survey file?") },
            text = { Text("Delete ${file.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    file.delete()
                    pendingDeleteSurvey = null
                    reload()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteSurvey = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SessionCard(
    item: SessionItem,
    onShareZip: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val statusColor = when (item.health.status) {
        QuickLookStatus.GREEN -> LabStatusColors.Green
        QuickLookStatus.AMBER -> LabStatusColors.Amber
        QuickLookStatus.RED -> LabStatusColors.Red
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(statusColor),
                    )
                    Text(
                        item.folderName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Row {
                    IconButton(onClick = onShareZip) {
                        Icon(Icons.Default.Share, contentDescription = "Share zip")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete session")
                    }
                }
            }

            val mode = item.manifest?.setup?.recording_mode ?: "unknown"
            val carry = item.manifest?.setup?.carry_mode ?: "?"
            Text(
                "Mode: $mode ($carry) • Duration: %.1fs • Size: ${formatSize(item.health.sizeBytes)}".format(
                    Locale.US,
                    item.health.durationSec,
                ),
                style = MaterialTheme.typography.bodySmall,
            )

            val summaryStreams = item.health.streamRows.entries
                .take(6)
                .joinToString("  ") { "${it.key}:${it.value}" }
            if (summaryStreams.isNotEmpty()) {
                Text(summaryStreams, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            }

            if (item.health.findings.isNotEmpty() || item.health.streamRows.size > 6) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide details" else "Details (${item.health.findings.size} notice(s))")
                }
            }

            if (expanded) {
                item.health.streamRows.entries.forEach { (k, v) ->
                    Text("$k: $v rows", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                }
                item.health.findings.forEach { finding ->
                    Text("• $finding", style = MaterialTheme.typography.bodySmall, color = statusColor)
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L -> "%.1f KB".format(Locale.US, bytes / 1024.0)
        else -> "%.2f MB".format(Locale.US, bytes / (1024.0 * 1024.0))
    }
}
