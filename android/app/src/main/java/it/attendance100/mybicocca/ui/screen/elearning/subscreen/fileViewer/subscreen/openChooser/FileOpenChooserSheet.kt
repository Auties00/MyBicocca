package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.openChooser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.settings.FileOpenChoice
import it.attendance100.mybicocca.ui.component.file.FileKind
import it.attendance100.mybicocca.ui.component.file.openChooserIcon
import it.attendance100.mybicocca.ui.component.file.openChooserLabel
import java.util.Locale

/**
 * Asks whether to open an in-app-capable file inside the app or hand it to an external app,
 * with an optional "remember this" switch so the choice sticks for that file type (a long-press
 * on a file re-shows the chooser, as the switch's helper text explains).
 *
 * Follows the app's hand-off-sheet language (LinkSheet / OfficeOpenSheet): a centered hero
 * shape with the per-kind icon, the file name, a kind + size chip, the remember toggle, and a
 * pinned connected button pair — brand-filled "In app" leading with explicit white content
 * (a theme-reactive onPrimary would flip dark in dark mode), tonal "Altra app" trailing.
 *
 * This is sheet CONTENT, not a sheet: it renders as a back-stack page (the FileOpenChooser
 * sheet route) inside whatever sheet container the scene strategy provides — a sub-page of an
 * already-open sheet, or its own standalone one.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileOpenChooserContent(
    fileName: String,
    sizeBytes: Long?,
    kind: FileKind,
    onChoose: (choice: FileOpenChoice, remember: Boolean) -> Unit,
) {
    var rememberChoice by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(MaterialShapes.Cookie9Sided.toShape())
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = kind.openChooserIcon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(52.dp),
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Text(
                        text = listOfNotNull(
                            stringResource(kind.openChooserLabel()),
                            formatSize(sizeBytes)
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
                Spacer(Modifier.height(20.dp))
                Surface(
                    onClick = { rememberChoice = !rememberChoice },
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ricorda la scelta",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "Tieni premuto un file per cambiare di nuovo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Switch(checked = rememberChoice, onCheckedChange = { rememberChoice = it })
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Button(
                    onClick = { onChoose(FileOpenChoice.InApp, rememberChoice) },
                    modifier = Modifier
                        .weight(1.4f)
                        .height(56.dp),
                    shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.Outlined.OpenInFull, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("In app", fontWeight = FontWeight.SemiBold)
                }
                FilledTonalButton(
                    onClick = { onChoose(FileOpenChoice.External, rememberChoice) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Altra app", fontWeight = FontWeight.SemiBold)
                }
            }
    }
}

private fun formatSize(bytes: Long?): String? = when {
    bytes == null || bytes <= 0 -> null
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.ITALIAN, "%.1f MB", bytes / (1024.0 * 1024.0))
}
