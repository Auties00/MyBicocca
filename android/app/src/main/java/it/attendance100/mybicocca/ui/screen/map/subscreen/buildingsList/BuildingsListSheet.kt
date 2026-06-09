package it.attendance100.mybicocca.ui.screen.map.subscreen.buildingsList

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import it.attendance100.mybicocca.domain.model.map.RoomScheduleEntry
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.screen.map.component.label
import it.attendance100.mybicocca.ui.screen.map.ext.buildingDisplayName
import it.attendance100.mybicocca.ui.screen.map.ext.openBuildingInMaps
import it.attendance100.mybicocca.ui.screen.map.ext.splitLegacyAlias
import it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail.BuildingPageBody
import it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail.RoomDetailPage

// Edifici sheet: a single modal hosting a pinned morphing header over a three-level body
// pager (list -> building -> room). "Informazioni" pushes the building page in place; the
// header itself never swaps, it morphs (back button slides in, title/subtitle crossfade).
@Composable
fun BuildingsListSheet(
    buildings: List<MapBuilding>,
    detailBuilding: MapBuilding?,
    rooms: Loadable<List<MapRoom>>,
    daySchedule: Loadable<Map<String, List<RoomScheduleEntry>>?>,
    syncStatus: SyncStatus,
    selectedRoom: MapRoom?,
    roomDetail: Loadable<MapRoomDetail?>,
    onShowInfo: (BuildingCode) -> Unit,
    onRoomClick: (MapRoom) -> Unit,
    onCloseRoom: () -> Unit,
    onOpen360: (String, String) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    onRetryRooms: (() -> Unit)? = null,
) {
    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        sizeDuration = 500,
    ) { _, _ ->
        val context = LocalContext.current
        val scheduleMap = (daySchedule as? Loadable.Loaded)?.value
        val detailTitle = detailBuilding?.let { buildingDisplayName(it) }

        // System back walks the pager up one level before dismissing the sheet.
        BackHandler(enabled = detailBuilding != null) {
            if (selectedRoom != null) onCloseRoom() else onBack()
        }

        Column {
            SheetPagerHeader(
                depth = when {
                    detailBuilding == null -> 0
                    selectedRoom == null -> 1
                    else -> 2
                },
                title = when {
                    detailBuilding == null -> "Edifici"
                    selectedRoom == null -> detailTitle.orEmpty()
                    else -> selectedRoom.name
                },
                subtitle = when {
                    detailBuilding == null -> if (buildings.size == 1) "1 sede" else "${buildings.size} sedi"
                    selectedRoom == null ->
                        detailBuilding.address ?: detailBuilding.city ?: detailBuilding.category.label
                    else -> detailTitle
                },
                onBack = when {
                    detailBuilding == null -> null
                    selectedRoom == null -> onBack
                    else -> onCloseRoom
                },
                onSubtitleClick = if (detailBuilding != null && selectedRoom == null) {
                    { context.openBuildingInMaps(detailBuilding) }
                } else {
                    null
                },
            )
            AnimatedContent(
                targetState = detailBuilding to selectedRoom,
                transitionSpec = {
                    sheetPageTransform(forward = pageDepth(targetState) >= pageDepth(initialState))
                },
                contentKey = { (building, room) -> room?.code?.value ?: building?.code?.value ?: "list" },
                label = "buildings_sheet_pages",
            ) { (building, room) ->
                when {
                    building == null -> BuildingsListPage(
                        buildings = buildings,
                        onShowInfo = onShowInfo,
                    )

                    room == null -> BuildingPageBody(
                        rooms = rooms,
                        daySchedule = daySchedule,
                        syncStatus = syncStatus,
                        onRoomClick = onRoomClick,
                        onRetry = onRetryRooms,
                    )

                    else -> RoomDetailPage(
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
}

private fun pageDepth(page: Pair<MapBuilding?, MapRoom?>): Int = when {
    page.first == null -> 0
    page.second == null -> 1
    else -> 2
}

// Headerless list body: the sheet's morphing header carries the "Edifici / n sedi" text.
@Composable
private fun BuildingsListPage(
    buildings: List<MapBuilding>,
    onShowInfo: (BuildingCode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandedCode by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        itemsIndexed(buildings, key = { _, building -> building.code.value }) { index, building ->
            BuildingRow(
                building = building,
                isFirst = index == 0,
                isLast = index == buildings.lastIndex,
                expanded = expandedCode == building.code.value,
                onToggle = {
                    expandedCode = if (expandedCode == building.code.value) null else building.code.value
                },
                onShowInfo = { onShowInfo(building.code) },
            )
        }
    }
}

// Segmented M3E group: 28dp corners cap the group's ends, 6dp where rows touch; the expanded
// row morphs to a fully rounded standalone card and rises one tonal step.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BuildingRow(
    building: MapBuilding,
    isFirst: Boolean,
    isLast: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    onShowInfo: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val transition = updateTransition(expanded, label = "building_row")
    val topCorner by transition.animateDp(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "top_corner",
    ) { if (it || isFirst) 28.dp else 6.dp }
    val bottomCorner by transition.animateDp(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "bottom_corner",
    ) { if (it || isLast) 28.dp else 6.dp }
    val containerColor by transition.animateColor(
        transitionSpec = { motion.defaultEffectsSpec() },
        label = "container_color",
    ) { if (it) scheme.surfaceContainerHigh else scheme.surfaceContainer }
    val arrowRotation by transition.animateFloat(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "arrow",
    ) { if (it) 180f else 0f }
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    val (name, legacyAlias) = remember(building.name) { splitLegacyAlias(building.name) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
        shape = RoundedCornerShape(
            topStart = topCorner,
            topEnd = topCorner,
            bottomStart = bottomCorner,
            bottomEnd = bottomCorner,
        ),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(shape = CircleShape, color = scheme.primaryContainer) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (legacyAlias != null) {
                                Text(
                                    text = legacyAlias,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = scheme.onPrimaryContainer,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Apartment,
                                    contentDescription = null,
                                    tint = scheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                        )
                        Text(
                            text = building.city ?: building.category.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = if (expanded) "Comprimi" else "Espandi",
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.rotate(arrowRotation),
                )
            }

            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    building.address?.let { address ->
                        DetailRow(icon = Icons.Outlined.Place, value = address)
                    }
                    DetailRow(icon = Icons.Outlined.Tag, value = "Codice ${building.code.value}")
                    ActionRow(building = building, onShowInfo = onShowInfo)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    value: String,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = scheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
    }
}

// Connected button pair, same scheme as the calendar event detail's action row. Directions
// leads on the tonal; the primary Info trails on the brand fill.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActionRow(
    building: MapBuilding,
    onShowInfo: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val infoBg = if (dark) scheme.primaryContainer else scheme.primary
    val infoFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Directions button (secondary, leads)
        FilledTonalButton(
            onClick = { context.openBuildingInMaps(building) },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = ButtonGroupDefaults.connectedLeadingButtonShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.surfaceContainerHighest,
                contentColor = scheme.onSurface,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Indicazioni", fontWeight = FontWeight.SemiBold)
        }

        // Info button (primary, trails)
        Button(
            onClick = onShowInfo,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = infoBg,
                contentColor = infoFg,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Informazioni", fontWeight = FontWeight.SemiBold)
        }
    }
}
