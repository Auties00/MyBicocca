package it.unimib.indoornav.lab.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.unimib.indoornav.lab.format.CapabilitiesInfo
import it.unimib.indoornav.lab.format.ClockInfo
import it.unimib.indoornav.lab.format.ExperimentTemplate
import it.unimib.indoornav.lab.format.PhoneInfo
import it.unimib.indoornav.lab.format.QuickLookStatus
import it.unimib.indoornav.lab.format.RecordingEdits
import it.unimib.indoornav.lab.format.SessionEditor
import it.unimib.indoornav.lab.format.SessionHealthReport
import it.unimib.indoornav.lab.format.SessionManifest
import it.unimib.indoornav.lab.format.SetupManifestInfo
import it.unimib.indoornav.lab.service.RecordingSummary
import it.unimib.indoornav.lab.ui.theme.LabTheme
import java.io.File
import java.util.Locale

/** Shown once after every recording: the user can correct its details or delete it. Swiping it away keeps it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingSummarySheet(
    summary: RecordingSummary,
    onSave: (RecordingEdits) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        RecordingSummaryContent(summary, onSave, onDelete, onDismiss)
    }
}

@Composable
private fun RecordingSummaryContent(
    summary: RecordingSummary,
    onSave: (RecordingEdits) -> Unit,
    onDelete: () -> Unit,
    onKeep: () -> Unit,
) {
    val original = remember(summary) { SessionEditor.editsOf(summary.manifest) }
    var edits by remember(summary) { mutableStateOf(original) }
    var confirmDelete by remember { mutableStateOf(false) }
    // Without this, closing the sheet with a field focused hands the focus and keyboard to the Setup screen.
    val focusManager = LocalFocusManager.current
    val close: (() -> Unit) -> Unit = { action ->
        focusManager.clearFocus()
        action()
    }
    val preview = remember(edits) { SessionEditor.apply(summary.manifest, summary.template, edits) }
    val changed = preview != summary.manifest

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Recording finished", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            summary.sessionDir.name,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
        )

        HealthCard(summary)

        Text("Recording details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Correct anything that was wrong when you started. The recording itself is not changed.",
            style = MaterialTheme.typography.bodySmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = edits.operator,
                onValueChange = { edits = edits.copy(operator = it) },
                label = { Text("Walker") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = edits.marker,
                onValueChange = { edits = edits.copy(marker = it) },
                label = { Text("Marker") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(
            value = edits.phoneLabel,
            onValueChange = { edits = edits.copy(phoneLabel = it) },
            label = { Text("Phone ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        summary.template?.fields?.forEach { field ->
            OutlinedTextField(
                value = edits.templateFields[field.key] ?: "",
                onValueChange = { v -> edits = edits.copy(templateFields = edits.templateFields + (field.key to v)) },
                label = { Text(if (field.afterWalk) "${field.label} (fill in now)" else field.label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        OutlinedTextField(
            value = edits.notes,
            onValueChange = { edits = edits.copy(notes = it) },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Test ID", style = MaterialTheme.typography.labelSmall)
                Text(
                    preview.setup.test_id,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { confirmDelete = true }) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { close(onKeep) }) { Text(if (changed) "Discard edits" else "Keep") }
            Button(onClick = { close { onSave(edits) } }, enabled = changed) { Text("Save changes") }
        }
        Spacer(Modifier.height(16.dp))
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this recording?") },
            text = { Text("${summary.sessionDir.name} and all its files will be deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    close(onDelete)
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun HealthCard(summary: RecordingSummary) {
    val health = summary.health
    val (color, word) = when (health.status) {
        QuickLookStatus.GREEN -> Color(0xFF2E7D32) to "Looks good"
        QuickLookStatus.AMBER -> Color(0xFFED6C02) to "Usable, check the notes below"
        QuickLookStatus.RED -> Color(0xFFC62828) to "Problem"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier
                        .size(14.dp)
                        .background(color, CircleShape),
                )
                Text(word, fontWeight = FontWeight.Bold)
            }
            val minutes = (health.durationSec / 60).toInt()
            val seconds = (health.durationSec % 60).toInt()
            val facts = buildList {
                add("${minutes} min %02d s".format(Locale.US, seconds))
                add("%.1f MB".format(Locale.US, health.sizeBytes / 1_000_000.0))
                if (summary.template?.fields?.any { it.key == "route" } == true) {
                    add("${summary.checkpointMarks} checkpoint mark(s)")
                }
                if (!summary.manifest.stopped_cleanly) add("did not stop cleanly")
            }
            Text(facts.joinToString(" • "), style = MaterialTheme.typography.bodyMedium)
            val streams = listOf("acc", "gyro", "baro", "ble", "wifi").mapNotNull { key ->
                health.streamRows[key]?.let { "$key $it" }
            }
            if (streams.isNotEmpty()) {
                Text(
                    "Rows: " + streams.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
            health.findings.take(5).forEach { finding ->
                Text("• $finding", style = MaterialTheme.typography.bodySmall, color = color)
            }
        }
    }
}

private fun previewSummary(status: QuickLookStatus, findings: List<String>) = RecordingSummary(
    sessionDir = File("20261122-022524_A1_E4b"),
    template = ExperimentTemplate.E6,
    manifest = SessionManifest(
        app_version = "0.1.0",
        phone = PhoneInfo(label = "A1", model = "SM-S928B", os = "17", manufacturer = "samsung"),
        capabilities = CapabilitiesInfo(barometer = true, gyroscope = true, wifi_rtt = true, arcore = true),
        setup = SetupManifestInfo(
            operator = "LL",
            marker = "AF",
            building = "U14",
            floor = 1,
            test_id = "E6-route=R1-dir=out-pace=normal-mode=hand-low-walker=LL",
            carry_mode = "hand-low",
            recording_mode = "guided",
            template_fields = mapOf("route" to "R1", "dir" to "out", "pace" to "normal", "mode" to "hand-low", "walker" to "LL"),
            notes = "",
        ),
        clock = ClockInfo(wall_ms = 1_795_220_000_000L, elapsed_ns = 1L),
        stopped_cleanly = true,
    ),
    health = SessionHealthReport(
        status = status,
        durationSec = 121.0,
        sizeBytes = 8_400_000L,
        streamRows = mapOf("acc" to 56_000L, "gyro" to 11_200L, "baro" to 3_000L, "ble" to 811L, "wifi" to 351L),
        findings = findings,
    ),
    checkpointMarks = 9,
)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RecordingSummaryGreenPreview() {
    LabTheme {
        Surface { RecordingSummaryContent(previewSummary(QuickLookStatus.GREEN, emptyList()), {}, {}, {}) }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun RecordingSummaryAmberDarkPreview() {
    LabTheme(darkTheme = true) {
        Surface {
            RecordingSummaryContent(
                previewSummary(QuickLookStatus.AMBER, listOf("ble gap 3000 ms (>2s)", "Session did not stop cleanly")),
                {}, {}, {},
            )
        }
    }
}
