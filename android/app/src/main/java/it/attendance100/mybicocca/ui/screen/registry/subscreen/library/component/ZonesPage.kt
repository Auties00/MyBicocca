package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.library.LibraryZone
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.theme.PreviewBgDark

/**
 * Wizard step 1: pick the seating zone. Each row carries the zone's illustration and optional
 * description; loading, error-with-retry and empty states render in place of the list.
 */
@Composable
internal fun ZonesPage(
    zones: Loadable<List<LibraryZone>>,
    zonesStatus: SyncStatus,
    onSelectZone: (LibraryZone) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (zones) {
            Loadable.NotYetLoaded -> when (zonesStatus) {
                is SyncStatus.Failed -> SheetMessage(
                    icon = Icons.Outlined.EventSeat,
                    title = stringResource(R.string.common_error_title),
                    body = stringResource(R.string.library_zones_loading_failed),
                    action = { RetryButton(onClick = onRetry) },
                )

                else -> LibraryLoading(label = stringResource(R.string.library_zones_loading))
            }

            is Loadable.Loaded -> {
                if (zones.value.isEmpty()) {
                    SheetMessage(
                        icon = Icons.Outlined.EventSeat,
                        title = stringResource(R.string.library_no_zones),
                        body = stringResource(R.string.library_no_zones_body),
                    )
                } else {
                    zones.value.forEach { zone ->
                        ZoneRow(zone = zone, onClick = { onSelectZone(zone) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoneRow(zone: LibraryZone, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    Surface(
        onClick = { haptic.tap(); onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LibraryZoneArt(
                zone = zone,
                modifier = Modifier.size(width = 64.dp, height = 48.dp),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = zone.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                zone.subdescription?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Preview(name = "Zones Loaded · Light", showBackground = true)
@Composable
private fun ZonesLoadedLightPreview() {
    LibraryPreview(dark = false) {
        ZonesPage(
            zones = Loadable.Loaded(LibraryPreviewDefaults.Zones),
            zonesStatus = SyncStatus.Idle,
            onSelectZone = {},
            onRetry = {},
        )
    }
}

@Preview(
    name = "Zones Loaded · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun ZonesLoadedDarkPreview() {
    LibraryPreview(dark = true) {
        ZonesPage(
            zones = Loadable.Loaded(LibraryPreviewDefaults.Zones),
            zonesStatus = SyncStatus.Idle,
            onSelectZone = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Zones Loading · Light", showBackground = true)
@Composable
private fun ZonesLoadingLightPreview() {
    LibraryPreview(dark = false) {
        ZonesPage(
            zones = Loadable.NotYetLoaded,
            zonesStatus = SyncStatus.Refreshing,
            onSelectZone = {},
            onRetry = {},
        )
    }
}

@Preview(
    name = "Zones Error · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun ZonesErrorDarkPreview() {
    LibraryPreview(dark = true) {
        ZonesPage(
            zones = Loadable.NotYetLoaded,
            zonesStatus = SyncStatus.Failed(RuntimeException("offline")),
            onSelectZone = {},
            onRetry = {},
        )
    }
}
