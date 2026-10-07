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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.settings.FileOpenChoice
import it.attendance100.mybicocca.ui.component.directory.SegmentedIconChip
import it.attendance100.mybicocca.ui.component.directory.SegmentedTile
import it.attendance100.mybicocca.ui.component.file.FileKind
import it.attendance100.mybicocca.ui.component.file.openChooserIcon
import it.attendance100.mybicocca.ui.component.file.openChooserLabel
import it.attendance100.mybicocca.ui.navigation.FileOpenPreferenceViewModel
import it.attendance100.mybicocca.ui.navigation.LocalAppNavigator
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute

/**
 * The in-app-capable kinds the user can set a default for; kinds that always hand off to
 * another app (Office, unknown) are excluded. Order mirrors the file-type survey: documents
 * first.
 */
private val FILE_ASSOCIATION_KINDS = listOf(
    FileKind.Pdf,
    FileKind.Image,
    FileKind.Video,
    FileKind.Audio,
    FileKind.Html,
    FileKind.Text,
    FileKind.Zip,
)

/** The association kind whose [FileKind.preferenceKey] is [preferenceKey], if it is one. */
fun fileAssociationKind(preferenceKey: String): FileKind? =
    FILE_ASSOCIATION_KINDS.firstOrNull { it.preferenceKey == preferenceKey }

/**
 * The "Apertura file" settings page ([SheetRoute.FileAssociations]): a connected segmented card
 * with one row per supported file kind — its icon in a primary chip, its name, and the current
 * default ("Apri in app" / "Apri con altra app" / "Chiedi ogni volta") as the subtitle. Tapping
 * a row pushes a [FileAssociationChooserPage] inside this sheet
 * ([SheetRoute.FileAssociationChooser]) to set the default to In app / Altra app or reset it to
 * ask every time; a pick is persisted immediately through the store flow and the chooser page
 * pops. Backed by the same store as the open chooser shown when a file is actually opened, so a
 * change here takes effect the next time a file of that kind opens.
 */
@Composable
fun FileAssociationsPage(
    viewModel: FileOpenPreferenceViewModel = hiltViewModel(),
) {
    val haptic = rememberHapticManager()
    val navigator = LocalAppNavigator.current
    val choices by viewModel.choices.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_file_opening_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.settings_file_opening_subtitle),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            FILE_ASSOCIATION_KINDS.forEachIndexed { index, kind ->
                SegmentedTile(
                    isFirst = index == 0,
                    isLast = index == FILE_ASSOCIATION_KINDS.lastIndex,
                    title = stringResource(kind.openChooserLabel()),
                    subtitle = choices[kind.preferenceKey].associationLabel(),
                    onClick = {
                        haptic.tap()
                        kind.preferenceKey?.let {
                            navigator?.navigate(SheetRoute.FileAssociationChooser(it))
                        }
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

@Composable
private fun FileOpenChoice?.associationLabel(): String =
    when (this) {
        FileOpenChoice.InApp -> stringResource(R.string.settings_file_association_open_in_app)
        FileOpenChoice.External -> stringResource(R.string.settings_file_association_open_external)
        null -> stringResource(R.string.settings_file_association_ask)
}
