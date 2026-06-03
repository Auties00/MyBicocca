package it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
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
import it.attendance100.mybicocca.ui.component.shimmer.ShimmerCircle
import it.attendance100.mybicocca.ui.screen.map.component.label
import it.attendance100.mybicocca.ui.screen.map.component.SheetPagerHeader
import it.attendance100.mybicocca.ui.screen.map.component.sheetPageTransform
import it.attendance100.mybicocca.ui.screen.map.ext.buildingDisplayName
import it.attendance100.mybicocca.ui.screen.map.ext.openBuildingInMaps
import it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail.state.RoomStatus
import it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail.state.roomStatus
import java.time.LocalDateTime

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
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BuildingPageBody(
    rooms: Loadable<List<MapRoom>>,
    daySchedule: Loadable<Map<String, List<RoomScheduleEntry>>?>,
    syncStatus: SyncStatus,
    onRoomClick: (MapRoom) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Snapshot "now" each time the schedule lands so free/busy is computed against fresh data.
    val now = remember(daySchedule) { LocalDateTime.now() }
    val scheduleMap = (daySchedule as? Loadable.Loaded)?.value
    val roomList = (rooms as? Loadable.Loaded)?.value

    val statusLoading = daySchedule is Loadable.NotYetLoaded
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        when {
            rooms is Loadable.NotYetLoaded || (roomList.isNullOrEmpty() && syncStatus is SyncStatus.Refreshing) -> {
                item(key = "rooms_loading") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator()
                    }
                }
            }

            roomList.isNullOrEmpty() -> {
                item(key = "rooms_empty") {
                    Text(
                        text = "Nessuna aula disponibile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
            }

            else -> {
                itemsIndexed(roomList, key = { _, room -> room.code.value }) { index, room ->
                    val entries = scheduleMap?.get(room.code.value)
                        ?: if (scheduleMap != null) emptyList() else null
                    RoomRow(
                        room = room,
                        status = roomStatus(entries, now),
                        statusLoading = statusLoading,
                        shimmer = shimmerInstance,
                        isFirst = index == 0,
                        isLast = index == roomList.lastIndex,
                        onClick = { onRoomClick(room) },
                    )
                }
            }
        }
    }
}

// Segmented M3E group, same language as the buildings list: 28dp corners cap the group's
// ends, 6dp where rows touch. Tapping pushes the room page rather than expanding inline.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RoomRow(
    room: MapRoom,
    status: RoomStatus,
    statusLoading: Boolean,
    shimmer: Shimmer,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val statusColor = when (status) {
        is RoomStatus.Free -> scheme.tertiary
        is RoomStatus.Busy -> scheme.error
        RoomStatus.Unknown -> scheme.outline
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = if (isFirst) 28.dp else 6.dp,
            topEnd = if (isFirst) 28.dp else 6.dp,
            bottomStart = if (isLast) 28.dp else 6.dp,
            bottomEnd = if (isLast) 28.dp else 6.dp,
        ),
        color = scheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Free/busy dot rides the Piano subtitle; a shimmer stands in while
                    // the day's occupation grid is loading.
                    if (statusLoading) {
                        ShimmerCircle(size = 8.dp, modifier = Modifier.shimmer(shimmer))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor),
                        )
                    }
                    room.floor?.let { floor ->
                        Text(
                            text = if (floor == 0) "Piano terra" else "Piano $floor",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            room.capacity?.let { capacity ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = scheme.onSurfaceVariant,
                    )
                    // Fixed min width + end alignment keeps the icons in a column across rows
                    // regardless of the count's digit count.
                    Text(
                        text = "$capacity",
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.widthIn(min = 28.dp),
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
            )
        }
    }
}
