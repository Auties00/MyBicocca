package it.unimib.indoornav.lab.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unimib.indoornav.lab.format.ExperimentTemplate
import it.unimib.indoornav.lab.service.PendingAfterWalkPrompt
import it.unimib.indoornav.lab.service.ProtocolPhase
import it.unimib.indoornav.lab.service.RecordingUiState
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveRecordingScreen(
    state: RecordingUiState,
    onCheckpoint: () -> Unit,
    onUndoMark: () -> Unit,
    onEventMark: (kind: String, label: String) -> Unit,
    onFinish: () -> Unit,
    onToggleTouchLock: (Boolean) -> Unit,
    onToggleAllowScreenOff: (Boolean) -> Unit,
) {
    var showNoteDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            formatElapsed(state.elapsedSec),
                            style = MaterialTheme.typography.displaySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(state.recordingMode.uppercase(), style = MaterialTheme.typography.labelLarge)
                            Text(state.carryMode, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text(state.testId, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)

                    if (state.protocolStatus.message.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                state.protocolStatus.message,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    if (state.twentyMinuteWarning) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100))
                                Text(
                                    "20 minutes elapsed — BLE scan restarted; finish walk soon.",
                                    color = Color(0xFFE65100),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = { onToggleTouchLock(true) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Lock Touch")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text("Allow screen off", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(6.dp))
                    Switch(
                        checked = state.allowScreenOff,
                        onCheckedChange = onToggleAllowScreenOff,
                    )
                }
            }

            val showCheckpoint = !(state.recordingMode == "free" &&
                state.templateCode in ExperimentTemplate.STAND_TEMPLATE_CODES)
            if (showCheckpoint) {
                Button(
                    onClick = onCheckpoint,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CHECKPOINT", style = MaterialTheme.typography.labelLarge)
                        Text(state.nextCheckpointLabel, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    state.lastMark?.let { "Last: ${it.kind} ${it.label} (${it.source})" } ?: "No marks yet",
                    style = MaterialTheme.typography.bodySmall,
                )
                TextButton(
                    onClick = onUndoMark,
                    enabled = state.lastMark != null,
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Undo last mark")
                }
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (showCheckpoint) "Events (Vol- = Checkpoint, Hold Vol+ 2s = Finish)"
                        else "Events (Hold Vol+ 2s = Finish)",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(onClick = { onEventMark("stopped", "") }) { Text("Stopped") }
                        OutlinedButton(onClick = { onEventMark("resumed", "") }) { Text("Resumed") }
                        OutlinedButton(onClick = { onEventMark("stairs_start", "") }) { Text("Stairs start") }
                        OutlinedButton(onClick = { onEventMark("stairs_end", "") }) { Text("Stairs end") }
                        OutlinedButton(onClick = { onEventMark("lift_start", "") }) { Text("Lift start") }
                        OutlinedButton(onClick = { onEventMark("lift_end", "") }) { Text("Lift end") }
                        OutlinedButton(onClick = { onEventMark("note", "dropped_phone") }) { Text("Dropped phone") }
                        OutlinedButton(onClick = { showNoteDialog = true }) { Text("Note") }
                    }
                }
            }

            val isFinishing = state.protocolStatus.phase == ProtocolPhase.END_STILL ||
                state.protocolStatus.phase == ProtocolPhase.END_SHAKE
            Button(
                onClick = onFinish,
                enabled = !isFinishing,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        isFinishing -> "FINISHING PROTOCOL..."
                        state.recordingMode == "guided" || state.recordingMode == "marker_only" -> "FINISH (GUIDED END)"
                        else -> "STOP RECORDING"
                    },
                    fontWeight = FontWeight.Bold,
                )
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Streams", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    state.streamCounters.forEach { counter ->
                        val isEventLike = counter.streamKey in setOf("marks", "events", "steps", "sensor_accuracy", "rtt", "gnss")
                        val silentAlert = counter.active && !isEventLike && counter.silentMs > 3_000L
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                counter.streamKey,
                                fontFamily = FontFamily.Monospace,
                                color = if (silentAlert) MaterialTheme.colorScheme.error else Color.Unspecified,
                                fontWeight = if (silentAlert) FontWeight.Bold else FontWeight.Normal,
                            )
                            Text(
                                "${counter.rows} rows (%.1f/s)".format(Locale.US, counter.rowsPerSec),
                                fontFamily = FontFamily.Monospace,
                                color = if (silentAlert) MaterialTheme.colorScheme.error else Color.Unspecified,
                            )
                        }
                    }
                }
            }
        }

        if (state.touchLocked) {
            TouchLockOverlay(
                elapsedSec = state.elapsedSec,
                onUnlock = { onToggleTouchLock(false) },
            )
        }
    }

    if (showNoteDialog) {
        var noteText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Add Note Mark") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note label (e.g. door opened)") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEventMark("note", noteText.trim().ifEmpty { "note" })
                    showNoteDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun TouchLockOverlay(
    elapsedSec: Long,
    onUnlock: () -> Unit,
) {
    var twoFingerDown by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(twoFingerDown) {
        if (!twoFingerDown) {
            progress = 0f
            return@LaunchedEffect
        }
        val stepMs = 40L
        val totalMs = 1200L
        var elapsed = 0L
        while (twoFingerDown && elapsed < totalMs) {
            delay(stepMs)
            elapsed += stepMs
            progress = (elapsed.toFloat() / totalMs).coerceIn(0f, 1f)
        }
        if (twoFingerDown && elapsed >= totalMs) {
            onUnlock()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pressedCount = event.changes.count { it.pressed }
                        twoFingerDown = pressedCount >= 2
                        if (event.type == PointerEventType.Press || event.type == PointerEventType.Move) {
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
            Text(
                formatElapsed(elapsedSec),
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                "Touch locked — hold TWO fingers to unlock",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.titleMedium,
            )
            if (twoFingerDown) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(0.7f),
                )
            }
        }
    }
}

@Composable
fun AfterWalkFieldsDialog(
    prompt: PendingAfterWalkPrompt,
    onSave: (Map<String, String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val afterFields = prompt.template.fields.filter { it.afterWalk }
    var values by remember {
        mutableStateOf(
            afterFields.associate { it.key to (prompt.currentFields[it.key] ?: "") },
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post-Walk Fields (${prompt.template.code})") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter values collected at the end of the walk:")
                afterFields.forEach { field ->
                    OutlinedTextField(
                        value = values[field.key] ?: "",
                        onValueChange = { v ->
                            values = values.toMutableMap().apply { put(field.key, v) }
                        },
                        label = { Text(field.label) },
                        singleLine = true,
                        keyboardOptions = if (field.key == "steps") {
                            KeyboardOptions(keyboardType = KeyboardType.Number)
                        } else {
                            KeyboardOptions.Default
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(values) }) { Text("Update Manifest") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Skip") }
        },
    )
}

private fun formatElapsed(sec: Long): String {
    val m = sec / 60
    val s = sec % 60
    return "%02d:%02d".format(m, s)
}
