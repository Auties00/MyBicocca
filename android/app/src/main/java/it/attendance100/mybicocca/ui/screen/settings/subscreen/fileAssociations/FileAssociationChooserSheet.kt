package it.attendance100.mybicocca.ui.screen.settings.subscreen.fileAssociations

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
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.data.local.settings.FileOpenChoice
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.FileKind
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.openChooser.openChooserIcon
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.openChooser.openChooserLabel

// The settings sibling of FileOpenChooserSheet: a fileless chooser that sets the default for
// a whole file kind. Same centered-hero language, but three picks instead of two — "In app",
// "Altra app", and "Chiedi ogni volta" (which clears the saved default). The current default is
// shown filled; [current] == null means no default is saved yet.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileAssociationChooserSheet(
    kind: FileKind,
    current: FileOpenChoice?,
    onSelect: (FileOpenChoice?) -> Unit,
    onDismiss: () -> Unit,
) {
    PredictiveModalBottomSheet(onDismiss = onDismiss) { _, _ ->
        Column(modifier = Modifier.fillMaxWidth()) {
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
                    text = kind.openChooserLabel(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Scegli come aprire i file di questo tipo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ChoiceButton(
                    label = "In app",
                    icon = Icons.Outlined.OpenInFull,
                    selected = current == FileOpenChoice.InApp,
                    shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                    onClick = { onSelect(FileOpenChoice.InApp) },
                    modifier = Modifier.weight(1.4f),
                )
                ChoiceButton(
                    label = "Altra app",
                    icon = Icons.AutoMirrored.Outlined.OpenInNew,
                    selected = current == FileOpenChoice.External,
                    shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                    onClick = { onSelect(FileOpenChoice.External) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(8.dp))

            ChoiceButton(
                label = "Chiedi ogni volta",
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                selected = current == null,
                shape = MaterialTheme.shapes.extraLarge,
                onClick = { onSelect(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
            )
        }
    }
}

// One pick. The active default is brand-filled (white content, like the in-app hand-off) and
// carries a check; the rest are tonal.
@Composable
private fun ChoiceButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        )
    } else {
        ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    }
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = shape,
        colors = colors,
    ) {
        Icon(
            imageVector = if (selected) Icons.Outlined.Check else icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}
