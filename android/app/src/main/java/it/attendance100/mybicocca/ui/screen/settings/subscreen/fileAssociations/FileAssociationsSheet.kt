package it.attendance100.mybicocca.ui.screen.settings.subscreen.fileAssociations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.data.local.settings.FileOpenChoice
import it.attendance100.mybicocca.ui.component.directory.SegmentedIconChip
import it.attendance100.mybicocca.ui.component.directory.SegmentedTile
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.FileOpenPreferenceViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.FileKind
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.openChooser.openChooserIcon
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.openChooser.openChooserLabel

// The in-app-capable kinds the user can set a default for; the rest (Office, unknown) always
// hand off, so they're not listed. Order mirrors the file-type survey: documents first.
private val FILE_ASSOCIATION_KINDS = listOf(
    FileKind.Pdf,
    FileKind.Image,
    FileKind.Video,
    FileKind.Audio,
    FileKind.Html,
    FileKind.Text,
    FileKind.Zip,
)

// "Apertura file" settings: one row per file kind showing its current default. Tapping a row
// opens the generic chooser (FileAssociationChooserSheet) to set it to In app / Altra app, or
// reset it to "Chiedi ogni volta". Backed by the same store as the real open chooser, so a
// change here takes effect the next time a file of that kind is opened.
@Suppress("AssignedValueIsNeverRead")
@Composable
fun FileAssociationsSheet(onDismiss: () -> Unit) {
    val haptic = rememberHapticManager()
    val viewModel: FileOpenPreferenceViewModel =
        hiltViewModel(checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null)
    val choices by viewModel.choices.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<FileKind?>(null) }

    PredictiveModalBottomSheet(onDismiss = onDismiss) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = "Apertura file",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "App predefinite per tipo di file",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                FILE_ASSOCIATION_KINDS.forEachIndexed { index, kind ->
                    SegmentedTile(
                        isFirst = index == 0,
                        isLast = index == FILE_ASSOCIATION_KINDS.lastIndex,
                        title = kind.openChooserLabel(),
                        subtitle = choices[kind.preferenceKey].associationLabel(),
                        onClick = {
                            haptic.tap()
                            editing = kind
                        },
                        leading = {
                            SegmentedIconChip(
                                icon = kind.openChooserIcon(),
                                container = MaterialTheme.colorScheme.primaryContainer,
                                onContainer = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        },
                        trailing = {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(22.dp),
                            )
                        },
                    )
                }
            }
        }
    }

    // Stacked over the list (same pattern the shell uses to show the chooser over a detail
    // sheet). Picking applies immediately via the store flow and closes the chooser.
    editing?.let { kind ->
        FileAssociationChooserSheet(
            kind = kind,
            current = choices[kind.preferenceKey],
            onSelect = { choice ->
                val key = kind.preferenceKey
                if (key != null) {
                    if (choice == null) viewModel.forget(key) else viewModel.remember(key, choice)
                }
                editing = null
            },
            onDismiss = { editing = null },
        )
    }
}

private fun FileOpenChoice?.associationLabel(): String = when (this) {
    FileOpenChoice.InApp -> "Apri in app"
    FileOpenChoice.External -> "Apri con altra app"
    null -> "Chiedi ogni volta"
}
