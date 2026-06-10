package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.directory.SegmentedIconChip
import it.attendance100.mybicocca.ui.component.directory.SegmentedTile
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.AppointmentsTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.state.AppointmentDirectorySection
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.ext.toDirectorySections

/**
 * First "Prenota" page: the desk directory folded into its macro sections, one segmented tile
 * each with an accent icon chip and a chevron; shows the loading/error status body until the
 * directory lands.
 */
@Composable
internal fun SectionsPage(
    services: Loadable<List<AppointmentService>>,
    syncStatus: SyncStatus,
    onRetry: () -> Unit,
    onOpenSection: (AppointmentDirectorySection) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (services) {
        Loadable.NotYetLoaded -> {
            val statusTag = if (syncStatus is SyncStatus.Failed) {
                AppointmentsTestTags.SECTIONS_ERROR
            } else {
                AppointmentsTestTags.SECTIONS_LOADING
            }
            SheetStatusBody(syncStatus = syncStatus, onRetry = onRetry, modifier = modifier.testTag(statusTag))
        }

        is Loadable.Loaded -> {
            val sections = remember(services.value) { services.value.toDirectorySections() }
            val scheme = MaterialTheme.colorScheme
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(max = 720.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .testTag(AppointmentsTestTags.SECTIONS_LIST),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                sections.forEachIndexed { index, section ->
                    val (container, onContainer) = sectionAccent(index)
                    SegmentedTile(
                        isFirst = index == 0,
                        isLast = index == sections.lastIndex,
                        title = section.name,
                        subtitle = section.caption,
                        onClick = { onOpenSection(section) },
                        modifier = Modifier.testTag(AppointmentsTestTags.section(section.name)),
                        leading = {
                            SegmentedIconChip(
                                icon = sectionIcon(section.name),
                                container = container,
                                onContainer = onContainer,
                            )
                        },
                        trailing = {
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(22.dp),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun sectionAccent(index: Int): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    val scheme = MaterialTheme.colorScheme
    return when (index % 3) {
        0 -> scheme.primaryContainer to scheme.onPrimaryContainer
        1 -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        else -> scheme.secondaryContainer to scheme.onSecondaryContainer
    }
}

private fun sectionIcon(name: String): ImageVector = when {
    name.startsWith("Carriere", ignoreCase = true) -> Icons.Outlined.School
    name.startsWith("Didattica", ignoreCase = true) -> Icons.AutoMirrored.Outlined.MenuBook
    name.startsWith("Ritiri", ignoreCase = true) -> Icons.Outlined.Inventory2
    else -> Icons.Outlined.SupportAgent
}

/** Centered in-sheet status body: an expressive loading indicator, or a failure message with a retry button. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SheetStatusBody(
    syncStatus: SyncStatus,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (syncStatus) {
            is SyncStatus.Failed -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Caricamento non riuscito.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                RetryButton(onClick = onRetry)
            }

            else -> LoadingIndicator(modifier = Modifier.size(56.dp))
        }
    }
}
