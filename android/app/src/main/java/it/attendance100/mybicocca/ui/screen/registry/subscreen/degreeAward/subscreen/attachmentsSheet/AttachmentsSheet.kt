package it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.subscreen.attachmentsSheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.degreeaward.ThesisAttachment
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadgeTone
import it.attendance100.mybicocca.ui.screen.registry.theme.registryBadgeTone

// Allegati tesi: shows the attachments already on the thesis with their approval state and
// antiplagio reference. The actual deposit of the final PDF + antiplagio runs through the
// secretariat's frk-allegati blob flow, which we hand off to the Esse3 portal.
@Composable
fun AttachmentsSheet(
    attachments: List<ThesisAttachment>,
    onOpenPortal: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) { _, _ ->
        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 680.dp)) {
            Text(
                text = "Allegati tesi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 8.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (attachments.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = scheme.surfaceContainer,
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.UploadFile, null, tint = scheme.onSurfaceVariant)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Nessun allegato caricato. Carica l'elaborato definitivo e i dati antiplagio dal portale di Ateneo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    attachments.forEach { AttachmentRow(it) }
                }
            }

            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(22.dp)) {
                Button(
                    onClick = onOpenPortal,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.primary,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Carica sul portale", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AttachmentRow(attachment: ThesisAttachment) {
    val scheme = MaterialTheme.colorScheme
    val tone = when (attachment.stateCode?.uppercase()) {
        "A" -> registryBadgeTone(RegistryBadgeTone.Ok)
        "R" -> registryBadgeTone(RegistryBadgeTone.Alert)
        else -> registryBadgeTone(RegistryBadgeTone.Neutral)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Description, null, tint = scheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = attachment.title ?: attachment.fileName ?: "Allegato",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                attachment.antiplagiarismLink?.let {
                    Text("Antiplagio collegato", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
            }
            attachment.stateLabel?.let { label ->
                Surface(color = tone.container, contentColor = tone.onContainer, shape = RoundedCornerShape(10.dp)) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
