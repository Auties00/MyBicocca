package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumGroup
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.ComposerTarget
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.PendingAttachment
import it.attendance100.mybicocca.ui.theme.LocalIsOnline

/**
 * The shared composer page body: a subject field (new discussion / edit / reply), a multi-line
 * message field, optional file attachments, and a brand submit button pinned at the bottom whose
 * label follows the target (Pubblica / Rispondi / Salva). New discussions in group-mode forums
 * get a group picker above the fields. When editing, the post's existing attachments are kept
 * and listed read-only for context; only newly added files are removable. Submitting requires a
 * message (and a subject where one is required) and an online connection.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ForumComposer(
    target: ComposerTarget,
    subject: String,
    onSubjectChange: (String) -> Unit,
    message: String,
    onMessageChange: (String) -> Unit,
    attachments: List<PendingAttachment>,
    onAddAttachment: () -> Unit,
    onRemoveAttachment: (PendingAttachment) -> Unit,
    groups: List<ForumGroup>,
    selectedGroupId: Int?,
    onSelectGroup: (Int?) -> Unit,
    submitting: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else Color.White

    val canAttach = when (target) {
        is ComposerTarget.NewDiscussion -> target.canAttach
        is ComposerTarget.Reply -> target.canAttach
        is ComposerTarget.Edit -> target.canAttach
    }
    val existingAttachments = (target as? ComposerTarget.Edit)?.existingAttachments.orEmpty()
    val submitLabel = when (target) {
        is ComposerTarget.NewDiscussion -> "Pubblica"
        is ComposerTarget.Reply -> "Rispondi"
        is ComposerTarget.Edit -> "Salva"
    }
    val subjectRequired = target is ComposerTarget.NewDiscussion || target is ComposerTarget.Edit
    val canSubmit = !submitting && message.isNotBlank() && (!subjectRequired || subject.isNotBlank()) &&
            LocalIsOnline.current

    Column(modifier = modifier.fillMaxWidth().imePadding()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .heightIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (target is ComposerTarget.NewDiscussion && groups.isNotEmpty()) {
                GroupSelector(groups = groups, selectedGroupId = selectedGroupId, onSelect = onSelectGroup)
            }
            OutlinedTextField(
                value = subject,
                onValueChange = onSubjectChange,
                label = { Text("Oggetto") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                label = { Text("Messaggio") },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
            )

            existingAttachments.forEach { attachment ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = scheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Outlined.AttachFile, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Text(attachment.fileName, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                    }
                }
            }

            if (canAttach) {
                attachments.forEach { attachment ->
                    PendingAttachmentRow(attachment = attachment, onRemove = { onRemoveAttachment(attachment) })
                }
                TextButton(onClick = onAddAttachment) {
                    Icon(Icons.Outlined.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Allega file", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Button(
            onClick = onSubmit,
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = brandBg,
                contentColor = brandFg,
                disabledContainerColor = brandBg.copy(alpha = 0.5f),
                disabledContentColor = brandFg.copy(alpha = 0.8f),
            ),
        ) {
            if (submitting) {
                LoadingIndicator(modifier = Modifier.size(24.dp), color = brandFg)
            } else {
                Text(submitLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Group target picker for group-mode forums, a dropdown styled as a rounded field. Shown only
 * when the user belongs to at least one course group; the chosen group scopes the new
 * discussion's visibility.
 */
@Composable
private fun GroupSelector(groups: List<ForumGroup>, selectedGroupId: Int?, onSelect: (Int?) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    val selectedName = groups.firstOrNull { it.id == selectedGroupId }?.name ?: groups.first().name
    Box {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = scheme.surfaceContainerHigh,
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Outlined.Group, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Gruppo", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    Text(selectedName, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
                Icon(Icons.Outlined.UnfoldMore, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            groups.forEach { group ->
                DropdownMenuItem(
                    text = { Text(group.name) },
                    onClick = { expanded = false; onSelect(group.id) },
                )
            }
        }
    }
}

@Composable
private fun PendingAttachmentRow(attachment: PendingAttachment, onRemove: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = scheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.AttachFile,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = attachment.fileName,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Rimuovi",
                tint = scheme.onSurfaceVariant,
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClick = onRemove)
                    .padding(8.dp),
            )
        }
    }
}
