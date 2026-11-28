package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.domain.model.library.LibraryZone
import it.attendance100.mybicocca.domain.model.library.LibraryZoneColor
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.theme.zoneSwatch
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import it.attendance100.mybicocca.ui.theme.PreviewBgDark

/**
 * Wizard step 3: compact zone header over the list of free seats — each row shows the zone swatch
 * and a power-outlet badge — plus a pinned auto-assign footer button.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SeatsPage(
    zone: LibraryZone?,
    libraryName: String,
    seats: List<LibrarySeat>,
    seatsStatus: SyncStatus,
    onSelectSeat: (LibrarySeat) -> Unit,
    onAutoSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val zoneColor = zone?.color ?: LibraryZoneColor.Other

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp),
    ) {
        if (zone != null) {
            ZoneHeader(
                zone = zone,
                libraryName = libraryName,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 12.dp),
            )
        }

        if (seats.isEmpty() && seatsStatus is SyncStatus.Refreshing) {
            LibraryLoading(label = stringResource(R.string.common_loading))
            return
        }

        if (seats.isEmpty()) {
            SheetMessage(
                icon = Icons.Outlined.EventSeat,
                title = stringResource(R.string.library_no_free_seats),
                body = stringResource(R.string.library_no_free_seats_body),
            )
            return
        }

        val haptic = rememberHapticManager()

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            seats.forEach { seat ->
                SeatRow(
                    seat = seat,
                    zoneColor = zoneColor,
                    onClick = { haptic.tap(); onSelectSeat(seat) },
                )
            }
            Spacer(Modifier.size(4.dp))
        }

        val dark = LocalDarkTheme.current
        Button(
            onClick = { haptic.tap(); onAutoSelect() },
            enabled = LocalIsOnline.current,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ButtonDefaults.MediumContainerHeight)
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
            contentPadding = ButtonDefaults.MediumContentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (dark) scheme.primaryContainer else scheme.primary,
                contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
            ),
        ) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.library_auto_select),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SeatRow(seat: LibrarySeat, zoneColor: LibraryZoneColor, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val swatch = zoneSwatch(zoneColor)
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(swatch.container),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Chair,
                    contentDescription = null,
                    tint = swatch.accent,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = seat.shortName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (seat.hasPowerOutlet) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Bolt,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(R.string.library_power_outlet),
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                } else {
                    seat.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
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

@Preview(name = "Seats Loaded · Light", showBackground = true)
@Composable
private fun SeatsLoadedLightPreview() {
    LibraryPreview(dark = false) {
        SeatsPage(
            zone = LibraryPreviewDefaults.Zones.first(),
            libraryName = LibraryPreviewDefaults.Libraries.first().name,
            seats = LibraryPreviewDefaults.Seats,
            seatsStatus = SyncStatus.Idle,
            onSelectSeat = {},
            onAutoSelect = {},
        )
    }
}

@Preview(
    name = "Seats Loaded · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun SeatsLoadedDarkPreview() {
    LibraryPreview(dark = true) {
        SeatsPage(
            zone = LibraryPreviewDefaults.Zones.first(),
            libraryName = LibraryPreviewDefaults.Libraries.first().name,
            seats = LibraryPreviewDefaults.Seats,
            seatsStatus = SyncStatus.Idle,
            onSelectSeat = {},
            onAutoSelect = {},
        )
    }
}

@Preview(name = "Seats Loading · Light", showBackground = true)
@Composable
private fun SeatsLoadingLightPreview() {
    LibraryPreview(dark = false) {
        SeatsPage(
            zone = LibraryPreviewDefaults.Zones[1],
            libraryName = LibraryPreviewDefaults.Libraries.first().name,
            seats = emptyList(),
            seatsStatus = SyncStatus.Refreshing,
            onSelectSeat = {},
            onAutoSelect = {},
        )
    }
}

@Preview(
    name = "Seats Empty · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun SeatsEmptyDarkPreview() {
    LibraryPreview(dark = true) {
        SeatsPage(
            zone = LibraryPreviewDefaults.Zones[1],
            libraryName = LibraryPreviewDefaults.Libraries.first().name,
            seats = emptyList(),
            seatsStatus = SyncStatus.Idle,
            onSelectSeat = {},
            onAutoSelect = {},
        )
    }
}
