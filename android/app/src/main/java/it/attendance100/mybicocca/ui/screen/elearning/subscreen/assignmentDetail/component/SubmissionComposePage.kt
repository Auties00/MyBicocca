package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionForm
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state.PickedFile
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import java.util.Locale

/**
 * The submission editor: online text (when enabled) and a file list with a device picker (when
 * enabled), over a pinned action group. "Salva bozza" persists a draft; the primary action
 * advances to the finalize confirmation (or, when drafts are disabled, is the submission
 * itself).
 *
 * Already-submitted files start in the list as kept — removing one drops it from the set
 * re-uploaded on save — and kept plus newly picked files share the assignment's file-count
 * budget. A read-only notice replaces the editor when the form is not editable.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SubmissionComposePage(
    form: SubmissionForm,
    draftText: String,
    pickedFiles: List<PickedFile>,
    keptExistingFiles: List<Assignment.AttachmentRef>,
    submitting: Boolean,
    onTextChange: (String) -> Unit,
    onAddFiles: (List<String>) -> Unit,
    onRemoveFile: (String) -> Unit,
    onRemoveExistingFile: (String?) -> Unit,
    onSaveDraft: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    if (!form.canEdit) {
        Column(modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text(
                text = "Questa consegna non è al momento modificabile.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
        return
    }

    val pickFiles = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris -> if (uris.isNotEmpty()) onAddFiles(uris.map { it.toString() }) }

    val totalFiles = keptExistingFiles.size + pickedFiles.size
    val hasContent = (form.onlineTextEnabled && draftText.isNotBlank()) ||
        (form.fileEnabled && totalFiles > 0)
    val canAddMore = totalFiles < form.maxFiles

    Column(modifier = modifier.fillMaxWidth().heightIn(max = 640.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            if (form.onlineTextEnabled) {
                SectionLabel("Testo")
                OutlinedTextField(
                    value = draftText,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    placeholder = { Text("Scrivi la tua risposta…") },
                    enabled = !submitting,
                )
            }

            if (form.fileEnabled) {
                SectionLabel("File")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    keptExistingFiles.forEach { ref ->
                        FileRow(
                            fileName = ref.fileName,
                            sizeBytes = ref.sizeBytes,
                            enabled = !submitting,
                            onRemove = { onRemoveExistingFile(ref.fileUrl) },
                        )
                    }
                    pickedFiles.forEach { file ->
                        FileRow(
                            fileName = file.fileName,
                            sizeBytes = file.sizeBytes,
                            enabled = !submitting,
                            onRemove = { onRemoveFile(file.uri) },
                        )
                    }
                }
                if (totalFiles > 0) Spacer(Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = { pickFiles.launch(arrayOf("*/*")) },
                    enabled = !submitting && canAddMore && LocalIsOnline.current,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Aggiungi file", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = constraintsHint(form),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        SubmissionActions(
            draftsEnabled = form.submissionDraftsEnabled,
            enabled = hasContent,
            submitting = submitting,
            onSaveDraft = onSaveDraft,
            onContinue = onContinue,
        )
    }
}

/**
 * Pinned editor actions: with drafts enabled, a connected tonal "Salva bozza" + brand primary
 * pair; with drafts disabled, a single full-pill "Consegna". Disabled while the editor is
 * empty, an action is in flight, or the device is offline.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SubmissionActions(
    draftsEnabled: Boolean,
    enabled: Boolean,
    submitting: Boolean,
    onSaveDraft: () -> Unit,
    onContinue: () -> Unit,
) {
    val isOnline = LocalIsOnline.current
    val scheme = MaterialTheme.colorScheme
    val primaryLabel = if (draftsEnabled) "Continua" else "Consegna"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (draftsEnabled) {
            FilledTonalButton(
                onClick = onSaveDraft,
                enabled = enabled && !submitting && isOnline,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHighest,
                    contentColor = scheme.onSurface,
                ),
            ) {
                if (submitting) {
                    LoadingIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Salva bozza", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Button(
            onClick = onContinue,
            enabled = enabled && !submitting && isOnline,
            modifier = Modifier.weight(if (draftsEnabled) 1.4f else 1f).height(56.dp),
            shape = if (draftsEnabled) ButtonGroupDefaults.connectedTrailingButtonShape else RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = scheme.primary,
                contentColor = Color.White,
            ),
        ) {
            Text(primaryLabel, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun FileRow(fileName: String, sizeBytes: Long?, enabled: Boolean, onRemove: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = scheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.InsertDriveFile,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
                sizeBytes?.let {
                    Text(
                        text = formatSize(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onRemove, enabled = enabled) {
                Icon(Icons.Outlined.Close, contentDescription = "Rimuovi", tint = scheme.onSurfaceVariant)
            }
        }
    }
}

private fun constraintsHint(form: SubmissionForm): String {
    val parts = mutableListOf<String>()
    if (form.maxFiles > 0) parts += if (form.maxFiles == 1) "1 file" else "max ${form.maxFiles} file"
    if (form.maxSizeBytes > 0) parts += "fino a ${formatSize(form.maxSizeBytes)}"
    if (form.acceptedFileTypes.isNotEmpty()) parts += form.acceptedFileTypes.joinToString(", ")
    return parts.joinToString(" · ")
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1_048_576 -> String.format(Locale.ITALIAN, "%.1f MB", bytes / 1_048_576.0)
    bytes >= 1024 -> String.format(Locale.ITALIAN, "%.0f KB", bytes / 1024.0)
    else -> "$bytes B"
}
