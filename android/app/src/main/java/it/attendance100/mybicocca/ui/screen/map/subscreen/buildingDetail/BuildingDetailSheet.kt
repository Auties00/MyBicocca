package it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import it.attendance100.mybicocca.domain.model.map.RoomScheduleEntry
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.shimmer.ShimmerCircle
import it.attendance100.mybicocca.ui.screen.map.component.label
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.screen.map.ext.buildingDisplayName
import it.attendance100.mybicocca.ui.screen.map.ext.openBuildingInMaps
import it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail.state.RoomStatus
import it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail.state.roomStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val StatusTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

// Building detail content: a pinned morphing header over a two-level body pager
// (room list <-> room detail). Hosted both by the standalone pin modal and as a page
// inside the buildings list sheet (then onBack != null at the building level too).
@Composable
fun BuildingDetailSheet(
    building: MapBuilding,
    rooms: Loadable<List<MapRoom>>,
    daySchedule: Loadable<Map<String, List<RoomScheduleEntry>>?>,
    syncStatus: SyncStatus,
    selectedRoom: MapRoom?,
    roomDetail: Loadable<MapRoomDetail?>,
    onRoomClick: (MapRoom) -> Unit,
    onCloseRoom: () -> Unit,
    onOpen360: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onRetryRooms: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val scheduleMap = (daySchedule as? Loadable.Loaded)?.value
    val buildingTitle = remember(building.name) { buildingDisplayName(building) }

    // System back pops the room page before closing/dismissing anything above it.
    BackHandler(enabled = selectedRoom != null, onBack = onCloseRoom)

    Column(modifier = modifier.fillMaxWidth()) {
        SheetPagerHeader(
            depth = if (selectedRoom == null) 0 else 1,
            title = selectedRoom?.name ?: buildingTitle,
            subtitle = if (selectedRoom == null) {
                building.address ?: building.city ?: building.category.label
            } else {
                buildingTitle
            },
            onBack = if (selectedRoom != null) onCloseRoom else onBack,
            onSubtitleClick = if (selectedRoom == null) {
                { context.openBuildingInMaps(building) }
            } else {
                null
            },
        )
        AnimatedContent(
            targetState = selectedRoom,
            transitionSpec = { sheetPageTransform(forward = targetState != null) },
            contentKey = { it?.code?.value },
            label = "building_detail_pages",
        ) { room ->
            if (room == null) {
                BuildingPageBody(
                    rooms = rooms,
                    daySchedule = daySchedule,
                    syncStatus = syncStatus,
                    onRoomClick = onRoomClick,
                    onDirections = { context.openBuildingInMaps(building) },
                    onRetry = onRetryRooms,
                )
            } else {
                RoomDetailPage(
                    room = room,
                    detail = roomDetail,
                    todayEntries = scheduleMap?.get(room.code.value)
                        ?: if (scheduleMap != null) emptyList() else null,
                    onOpen360 = onOpen360,
                )
            }
        }
    }
}

// Headerless room list: the hosting sheet provides the morphing header above the pager.
// Rooms are split into one section per floor, each rendered as a two-column segmented grid.
// A non-null onDirections pins an "Indicazioni" button below the (height-capped) grid, so it
// stays reachable no matter how long the room list is.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BuildingPageBody(
    rooms: Loadable<List<MapRoom>>,
    daySchedule: Loadable<Map<String, List<RoomScheduleEntry>>?>,
    syncStatus: SyncStatus,
    onRoomClick: (MapRoom) -> Unit,
    modifier: Modifier = Modifier,
    onDirections: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
) {
    // Snapshot "now" each time the schedule lands so free/busy is computed against fresh data.
    val now = remember(daySchedule) { LocalDateTime.now() }
    val scheduleMap = (daySchedule as? Loadable.Loaded)?.value
    val roomList = (rooms as? Loadable.Loaded)?.value
    val failure = syncStatus as? SyncStatus.Failed

    val statusLoading = daySchedule is Loadable.NotYetLoaded
    // The shimmer drives an infinite animation — only materialize it while its placeholders
    // are actually on screen, so an open sheet doesn't keep an always-on animation running.
    val shimmerInstance = if (statusLoading) rememberShimmer(shimmerBounds = ShimmerBounds.Window) else null

    // Hold the loading state for a beat so quick fetches don't flash it; cached rooms skip it.
    val showLoading = rememberMinDurationLoading(
        loading = rooms is Loadable.NotYetLoaded ||
            (roomList.isNullOrEmpty() && syncStatus is SyncStatus.Refreshing),
    )

    // Floors ascending, rooms without a floor grouped last.
    val floorSections = remember(roomList) {
        roomList.orEmpty()
            .groupBy { it.floor }
            .toList()
            .sortedWith(compareBy(nullsLast()) { it.first })
    }

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    // The sheet may only grow/shrink vertically as content lands — animate the height change
    // here instead of letting the modal snap to the new size.
    Column(modifier = modifier.fillMaxWidth().animateContentSize(animationSpec = sizeSpec)) {
        when {
            roomList.isNullOrEmpty() && failure != null -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = "Caricamento aule non riuscito",
                body = failure.cause.friendlyMessage(),
                action = onRetry?.let { { RetryButton(onClick = it) } },
            )

            showLoading -> SheetLoadingIndicator(label = "Caricamento aule…")

            roomList.isNullOrEmpty() -> Text(
                text = "Nessuna aula disponibile",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = if (onDirections != null) 12.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                floorSections.forEach { (floor, floorRooms) ->
                    item(
                        key = "floor_${floor ?: "none"}",
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        FloorHeader(floor = floor, roomCount = floorRooms.size)
                    }
                    itemsIndexed(
                        items = floorRooms,
                        key = { _, room -> room.code.value },
                        span = { index, _ ->
                            // An odd trailing room stretches across the full row, which also
                            // caps both bottom corners of the section cleanly.
                            if (floorRooms.size % 2 == 1 && index == floorRooms.lastIndex) {
                                GridItemSpan(maxLineSpan)
                            } else {
                                GridItemSpan(1)
                            }
                        },
                    ) { index, room ->
                        val entries = scheduleMap?.get(room.code.value)
                            ?: if (scheduleMap != null) emptyList() else null
                        RoomCell(
                            room = room,
                            status = roomStatus(entries, now),
                            statusLoading = statusLoading,
                            shimmer = shimmerInstance,
                            sectionIndex = index,
                            sectionSize = floorRooms.size,
                            onClick = { onRoomClick(room) },
                        )
                    }
                }
            }
        }

        if (onDirections != null) {
            DirectionsButton(
                onClick = onDirections,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            )
        }
    }
}

// Same scheme as the buildings list's directions action: brand fill in light, primary
// container in dark.
@Composable
private fun DirectionsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (dark) scheme.primaryContainer else scheme.primary,
            contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
        ),
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = "Indicazioni",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun FloorHeader(floor: Int?, roomCount: Int) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 14.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = when {
                floor == null -> "Altri spazi"
                floor == 0 -> "Piano terra"
                else -> "Piano $floor"
            },
            style = MaterialTheme.typography.titleSmallEmphasized,
        )
        Text(
            text = if (roomCount == 1) "1 aula" else "$roomCount aule",
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant,
        )
    }
}

// Grid flavor of the segmented M3E group language used across the map sheets: 28dp corners cap
// the section's outer corners, 6dp where cells touch. Tapping pushes the room page.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RoomCell(
    room: MapRoom,
    status: RoomStatus,
    statusLoading: Boolean,
    shimmer: Shimmer?,
    sectionIndex: Int,
    sectionSize: Int,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val statusColor = when (status) {
        is RoomStatus.Free -> scheme.tertiary
        is RoomStatus.Busy -> scheme.error
        RoomStatus.Unknown -> scheme.outline
    }
    val statusLabel = when (status) {
        is RoomStatus.Free ->
            status.until?.let { "Libera fino alle ${it.format(StatusTimeFormat)}" } ?: "Libera"

        is RoomStatus.Busy -> "Occupata fino alle ${status.until.format(StatusTimeFormat)}"

        RoomStatus.Unknown -> null
    }

    // First/last grid row of the section get the 28dp outer caps; an odd trailing cell spans
    // the full row (see the span lambda in the caller), so "last row start" is itself.
    val lastRowStart = sectionSize - (if (sectionSize % 2 == 0) 2 else 1)
    val shape = RoundedCornerShape(
        topStart = if (sectionIndex == 0) 28.dp else 6.dp,
        topEnd = if (sectionIndex == 1 || sectionSize == 1) 28.dp else 6.dp,
        bottomStart = if (sectionIndex == lastRowStart) 28.dp else 6.dp,
        bottomEnd = if (sectionIndex == sectionSize - 1) 28.dp else 6.dp,
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = scheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                room.capacity?.let { capacity ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = scheme.onSurfaceVariant,
                        )
                        // Fixed min width + end alignment keeps the icons in a column across
                        // cells regardless of the count's digit count.
                        Text(
                            text = "$capacity",
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.widthIn(min = 20.dp),
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Free/busy dot; a shimmer stands in while the day's occupation grid loads.
                if (statusLoading && shimmer != null) {
                    ShimmerCircle(size = 8.dp, modifier = Modifier.shimmer(shimmer))
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor),
                    )
                }
                Text(
                    text = when {
                        statusLoading -> "Verifica disponibilità…"
                        statusLabel != null -> statusLabel
                        else -> "Disponibilità sconosciuta"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
