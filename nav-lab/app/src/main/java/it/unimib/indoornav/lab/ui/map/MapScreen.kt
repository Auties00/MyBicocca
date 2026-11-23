package it.unimib.indoornav.lab.ui.map

import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.Size
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unimib.indoornav.lab.data.SetupConfig
import it.unimib.indoornav.lab.inventory.BeaconGrouping
import it.unimib.indoornav.lab.inventory.BeaconInventoryRepository
import it.unimib.indoornav.lab.inventory.ResolvedBeaconTile
import it.unimib.indoornav.lab.survey.SurveyManager
import it.unimib.indoornav.lab.survey.SurveyUiState
import it.unimib.indoornav.lab.ui.beacons.BeaconDetailHost
import it.unimib.indoornav.lab.ui.beacons.BeaconDetailOverlay
import it.unimib.indoornav.lab.ui.beacons.BeaconHero
import it.unimib.indoornav.lab.ui.beacons.SaveBeaconBottomSheet
import it.unimib.indoornav.lab.ui.components.OrganicShapes
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private val MARKER_SIZE = 56.dp
private const val MIN_PX_PER_M = 4f
private const val MAX_PX_PER_M = 400f

/**
 * Where the plan sits on screen. In a portrait view the building's long axis (x) runs down the screen,
 * so a 106 x 13 m floor fills the phone instead of being a thin strip.
 */
private data class PlanView(val rotated: Boolean, val pxPerM: Float, val origin: Offset) {
    fun toScreen(plan: FloorPlan, x: Float, y: Float): Offset = if (rotated) {
        Offset(origin.x + (plan.y1 - y) * pxPerM, origin.y + (x - plan.x0) * pxPerM)
    } else {
        Offset(origin.x + (x - plan.x0) * pxPerM, origin.y + (plan.y1 - y) * pxPerM)
    }

    fun toPlan(plan: FloorPlan, p: Offset): Offset = if (rotated) {
        Offset(plan.x0 + (p.y - origin.y) / pxPerM, plan.y1 - (p.x - origin.x) / pxPerM)
    } else {
        Offset(plan.x0 + (p.x - origin.x) / pxPerM, plan.y1 - (p.y - origin.y) / pxPerM)
    }

    fun zoomedAround(centroid: Offset, zoom: Float): PlanView {
        val s = (pxPerM * zoom).coerceIn(MIN_PX_PER_M, MAX_PX_PER_M)
        val k = s / pxPerM
        return copy(pxPerM = s, origin = centroid - (centroid - origin) * k)
    }

    companion object {
        fun fitting(plan: FloorPlan, viewport: IntSize): PlanView {
            val rotated = viewport.height > viewport.width
            val margin = 24f
            val (planW, planH) = if (rotated) plan.height to plan.width else plan.width to plan.height
            // Fit the short side; the long side scrolls.
            val s = ((viewport.width - 2 * margin) / planW).coerceIn(MIN_PX_PER_M, MAX_PX_PER_M)
            return PlanView(rotated, s, Offset(margin, margin)).let { v ->
                if (planH * s < viewport.height) v.copy(origin = Offset(margin, (viewport.height - planH * s) / 2)) else v
            }
        }
    }
}

@Composable
fun MapScreen(
    surveyState: SurveyUiState,
    config: SetupConfig,
    isRecording: Boolean,
    surveyManager: SurveyManager,
    inventoryRepository: BeaconInventoryRepository,
    editSession: MapEditSession,
    onRequestLeaveEdit: () -> Unit,
) {
    val context = LocalContext.current
    val savedEntries by inventoryRepository.entries.collectAsState()
    val latestIsRecording by rememberUpdatedState(isRecording)
    LaunchedEffect(isRecording) { surveyManager.setInventoryTabActive(!isRecording, isRecording) }
    DisposableEffect(Unit) { onDispose { surveyManager.setInventoryTabActive(false, latestIsRecording) } }

    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            nowMs = System.currentTimeMillis()
        }
    }
    val liveGrouped = remember(surveyState.allBeacons) { BeaconGrouping.groupByPhysicalBeacon(surveyState.allBeacons) }
    val tiles = remember(savedEntries, liveGrouped, nowMs) {
        BeaconGrouping.resolveInventoryTiles(savedEntries, liveGrouped, nowMs)
    }

    val building = config.building.ifBlank { "U14" }
    val floors = remember(building) { FloorPlan.floorsOf(context, building) }
    var floor by rememberSaveable { mutableStateOf(config.floor.toString()) }
    if (floors.isNotEmpty() && floor !in floors) floor = floors.first()
    val plan = remember(building, floor) { FloorPlan.load(context, building, floor) }

    var placingKey by remember { mutableStateOf<String?>(null) }
    var detailKey by remember { mutableStateOf<String?>(null) }
    var editingTile by remember { mutableStateOf<ResolvedBeaconTile?>(null) }
    // Kept outside the detail host, which drops the map while a beacon's detail is open.
    var view by remember(building, floor) { mutableStateOf<PlanView?>(null) }
    val lastKnownTiles = remember { mutableMapOf<String, ResolvedBeaconTile>() }
    remember(tiles) { tiles.forEach { lastKnownTiles[it.tileKey] = it } }

    BeaconDetailHost(
        detailKey = detailKey,
        onClose = { detailKey = null },
        list = {
            MapContent(
                plan = plan,
                floors = floors,
                floor = floor,
                onFloorChange = { floor = it },
                tiles = tiles.filter { it.building.trim() == building },
                view = view,
                onViewChange = { view = it },
                editMode = editSession.editing,
                moves = editSession.moves,
                onStartEdit = {
                    editSession.start()
                    placingKey = null
                },
                onSaveEdit = {
                    editSession.save(inventoryRepository)
                    placingKey = null
                },
                onCancelEdit = {
                    editSession.discard()
                    placingKey = null
                },
                placingKey = placingKey,
                onPlacingKeyChange = { placingKey = it },
                onOpen = { detailKey = it.tileKey },
                onMove = { tile, x, y -> tile.savedEntry?.let { editSession.move(it.id, x, y) } },
            )
        },
        detail = { key ->
            val tile = tiles.firstOrNull { it.tileKey == key } ?: lastKnownTiles[key]
            if (tile != null) {
                BeaconDetailOverlay(
                    tile = tile,
                    onClose = { detailKey = null },
                    onEdit = { editingTile = tile },
                    onRemove = {
                        detailKey = null
                        tile.savedEntry?.let { inventoryRepository.remove(it.id) }
                    },
                )
            }
        },
    )

    // Back while editing asks what to do with the moves instead of dropping them.
    BackHandler(enabled = editSession.editing && detailKey == null) { onRequestLeaveEdit() }

    editingTile?.let { tile ->
        SaveBeaconBottomSheet(
            tile = tile,
            defaultBuilding = config.building,
            defaultFloor = config.floor,
            onSave = { entry ->
                inventoryRepository.upsert(entry)
                editingTile = null
            },
            onRemove = { id ->
                detailKey = null
                inventoryRepository.remove(id)
                editingTile = null
            },
            onDismiss = { editingTile = null },
        )
    }
}

@Composable
private fun MapContent(
    plan: FloorPlan?,
    floors: List<String>,
    floor: String,
    onFloorChange: (String) -> Unit,
    tiles: List<ResolvedBeaconTile>,
    view: PlanView?,
    onViewChange: (PlanView) -> Unit,
    editMode: Boolean,
    moves: Map<String, Pair<Double, Double>>,
    onStartEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    placingKey: String?,
    onPlacingKeyChange: (String?) -> Unit,
    onOpen: (ResolvedBeaconTile) -> Unit,
    onMove: (ResolvedBeaconTile, Double, Double) -> Unit,
) {
    val density = LocalDensity.current
    val markerPx = with(density) { MARKER_SIZE.toPx() }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    val onFloor = tiles.filter { planFloorOf(it.floor) == floor }
    // Where each beacon is shown: its staged move if it has one, else its saved position.
    fun positionOf(tile: ResolvedBeaconTile): Pair<Double, Double>? {
        val entry = tile.savedEntry ?: return null
        moves[entry.id]?.let { return it }
        val x = entry.x ?: return null
        val y = entry.y ?: return null
        return x to y
    }
    val placed = onFloor.filter { positionOf(it) != null }
    val unplaced = onFloor.filter { it.savedEntry != null && positionOf(it) == null }
    // The beacon being dragged, and where the finger grabbed it relative to its centre, so it keeps that
    // offset instead of jumping onto the finger.
    var draggingKey by remember { mutableStateOf<String?>(null) }
    var dragCenter by remember { mutableStateOf(Offset.Zero) }
    var grabOffset by remember { mutableStateOf(Offset.Zero) }
    val latestView by rememberUpdatedState(view)
    val latestPlacing by rememberUpdatedState(placingKey)
    val latestPlaced by rememberUpdatedState(placed)
    val latestPositionOf by rememberUpdatedState(::positionOf)

    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 4.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Map", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "${plan?.building ?: "No plan"} · ${placed.size} placed" +
                        if (unplaced.isNotEmpty()) " · ${unplaced.size} to place" else "",
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                )
            }
            IconButton(onClick = { plan?.let { p -> if (viewport != IntSize.Zero) onViewChange(PlanView.fitting(p, viewport)) } }) {
                Icon(Icons.Default.MyLocation, contentDescription = "Fit the floor")
            }
            if (editMode) {
                TextButton(onClick = onCancelEdit) { Text("Cancel") }
                Button(onClick = onSaveEdit, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(if (moves.isEmpty()) " Done" else " Save ${moves.size}")
                }
            } else {
                FilterChip(
                    selected = false,
                    onClick = onStartEdit,
                    label = { Text("Edit positions") },
                    leadingIcon = {
                        Icon(Icons.Default.EditLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            floors.forEach { f ->
                FilterChip(selected = f == floor, onClick = { onFloorChange(f) }, label = { Text("Floor $f") })
            }
        }
        if (editMode) {
            EditTray(unplaced = unplaced, placingKey = placingKey, onPlacingKeyChange = onPlacingKeyChange)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .onSizeChanged {
                    viewport = it
                    if (view == null && plan != null && it != IntSize.Zero) onViewChange(PlanView.fitting(plan, it))
                }
                .pointerInput(plan) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val v = latestView ?: return@detectTransformGestures
                        onViewChange(v.zoomedAround(centroid, zoom).let { it.copy(origin = it.origin + pan) })
                    }
                }
                .pointerInput(plan, editMode) {
                    if (!editMode) return@pointerInput
                    detectDragGesturesAfterLongPress(
                        onDragStart = { touch ->
                            val v = latestView ?: return@detectDragGesturesAfterLongPress
                            val p = plan ?: return@detectDragGesturesAfterLongPress
                            // The marker's body sits just below its anchor point; grab the nearest within reach.
                            val hit = latestPlaced.mapNotNull { tile ->
                                val (x, y) = latestPositionOf(tile) ?: return@mapNotNull null
                                val c = v.toScreen(p, x.toFloat(), y.toFloat())
                                tile to c
                            }.minByOrNull { (_, c) -> (c - touch).getDistance() }
                                ?.takeIf { (_, c) -> (c - touch).getDistance() < markerPx * 0.9f }
                            if (hit != null) {
                                draggingKey = hit.first.tileKey
                                dragCenter = hit.second
                                grabOffset = touch - hit.second
                            }
                        },
                        onDrag = { change, _ ->
                            if (draggingKey != null) {
                                change.consume()
                                dragCenter = change.position - grabOffset
                            }
                        },
                        onDragEnd = {
                            val key = draggingKey
                            val v = latestView
                            if (key != null && v != null && plan != null) {
                                val at = v.toPlan(plan, dragCenter)
                                latestPlaced.firstOrNull { it.tileKey == key }?.let {
                                    onMove(it, at.x.toDouble(), at.y.toDouble())
                                }
                            }
                            draggingKey = null
                        },
                        onDragCancel = { draggingKey = null },
                    )
                }
                .pointerInput(plan) {
                    detectTapGestures { tap ->
                        val v = latestView ?: return@detectTapGestures
                        val p = plan ?: return@detectTapGestures
                        val key = latestPlacing ?: return@detectTapGestures
                        val tile = (tiles.firstOrNull { it.tileKey == key }) ?: return@detectTapGestures
                        val at = v.toPlan(p, tap)
                        onMove(tile, at.x.toDouble(), at.y.toDouble())
                        onPlacingKeyChange(null)
                    }
                },
        ) {
            if (plan == null) {
                Text("No plan for this floor.", modifier = Modifier.align(Alignment.Center), color = colors.onSurfaceVariant)
                return@Box
            }
            val v = view ?: return@Box
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawPlan(plan, v, walls = colors.onSurface.copy(alpha = 0.75f), openings = colors.outline, stairs = colors.tertiary)
                drawRooms(plan, v, colors.onSurfaceVariant.copy(alpha = 0.6f))
                // A dotted outline marks where a moved beacon is saved, until the move is saved or discarded.
                placed.forEach { tile ->
                    val entry = tile.savedEntry ?: return@forEach
                    val moved = entry.id in moves || tile.tileKey == draggingKey
                    val ox = entry.x ?: return@forEach
                    val oy = entry.y ?: return@forEach
                    if (moved) {
                        drawDottedHalo(v.toScreen(plan, ox.toFloat(), oy.toFloat()), markerPx * 0.86f, colors.onSurfaceVariant)
                    }
                }
            }
            placed.forEach { tile ->
                val (px, py) = positionOf(tile) ?: return@forEach
                val center = if (tile.tileKey == draggingKey) dragCenter else v.toScreen(plan, px.toFloat(), py.toFloat())
                // Skip markers far off screen; the map can hold a whole building.
                if (center.x < -markerPx || center.y < -markerPx * 2 ||
                    center.x > viewport.width + markerPx || center.y > viewport.height + markerPx
                ) {
                    return@forEach
                }
                MapMarker(
                    tile = tile,
                    editMode = editMode,
                    modifier = Modifier
                        .offset { IntOffset((center.x - markerPx / 2).roundToInt(), (center.y - markerPx * 0.8f).roundToInt()) }
                        .pointerInput(tile.tileKey, editMode) {
                            if (!editMode) detectTapGestures { onOpen(tile) }
                        },
                )
            }
            if (editMode) {
                Surface(
                    color = colors.surfaceContainerHigh,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                ) {
                    Text(
                        text = placingKey?.let { key ->
                            "Tap the map to place ${tiles.firstOrNull { it.tileKey == key }?.displayLabel ?: "the beacon"}"
                        } ?: "Long-press a beacon and drag it to move it",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EditTray(unplaced: List<ResolvedBeaconTile>, placingKey: String?, onPlacingKeyChange: (String?) -> Unit) {
    if (unplaced.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("To place:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        unplaced.forEach { tile ->
            FilterChip(
                selected = tile.tileKey == placingKey,
                onClick = { onPlacingKeyChange(if (tile.tileKey == placingKey) null else tile.tileKey) },
                label = { Text(tile.displayLabel) },
            )
        }
    }
}

/** A beacon on the map: its code above its halo and model, which fly into the detail screen when tapped. */
@Composable
private fun MapMarker(tile: ResolvedBeaconTile, editMode: Boolean, modifier: Modifier) {
    val subLabel = listOf(tile.building, tile.floor).filter { it.isNotBlank() }.joinToString(" - ")
    Column(modifier = modifier.width(MARKER_SIZE), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = tile.alphanumericCode.ifBlank { tile.displayLabel },
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier
                .background(
                    if (editMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    RoundedCornerShape(6.dp),
                )
                .padding(horizontal = 4.dp),
        )
        BeaconHero(
            tileKey = tile.tileKey,
            label = tile.displayLabel,
            subLabel = subLabel,
            online = tile.isOnline,
            stale = tile.isStale,
            cardShape = OrganicShapes.cardShapeFor(tile.tileKey),
            inDetail = false,
            size = MARKER_SIZE,
        )
    }
}

private fun DrawScope.drawDottedHalo(center: Offset, size: Float, color: Color) {
    val outline = OrganicShapes.SmoothCookie8.createOutline(Size(size, size), layoutDirection, this)
    val path = (outline as? androidx.compose.ui.graphics.Outline.Generic)?.path ?: return
    translate(center.x - size / 2, center.y - size / 2) {
        drawPath(path, color, style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))))
    }
}

private fun DrawScope.drawPlan(plan: FloorPlan, v: PlanView, walls: Color, openings: Color, stairs: Color) {
    withTransform({
        translate(v.origin.x, v.origin.y)
        if (v.rotated) {
            rotate(90f, Offset.Zero)
            scale(v.pxPerM, v.pxPerM, Offset.Zero)
            translate(-plan.x0, -plan.y1)
        } else {
            scale(v.pxPerM, -v.pxPerM, Offset.Zero)
            translate(-plan.x0, -plan.y1)
        }
    }) {
        val hair = 1.2f / v.pxPerM
        drawPath(plan.openings, openings, style = Stroke(width = hair))
        drawPath(plan.stairs, stairs, style = Stroke(width = hair))
        drawPath(plan.walls, walls, style = Stroke(width = 1.6f / v.pxPerM))
    }
}

private fun DrawScope.drawRooms(plan: FloorPlan, v: PlanView, color: Color) {
    // Room numbers only once they are legible.
    if (v.pxPerM < 18f) return
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgb()
        textSize = min(max(v.pxPerM * 0.45f, 22f), 40f)
        textAlign = android.graphics.Paint.Align.CENTER
    }
    plan.rooms.forEach { room ->
        val p = v.toScreen(plan, room.x, room.y)
        if (p.x in -50f..size.width + 50f && p.y in -50f..size.height + 50f) {
            drawContext.canvas.nativeCanvas.drawText(room.number, p.x, p.y, paint)
        }
    }
}
