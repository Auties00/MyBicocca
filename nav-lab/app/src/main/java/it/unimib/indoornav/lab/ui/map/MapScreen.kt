package it.unimib.indoornav.lab.ui.map

import kotlin.math.exp
import kotlin.math.abs
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
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

// Drag distance that multiplies the zoom by e (about 2.7x) during the one-handed zoom.
private const val QUICK_ZOOM_PX_PER_E = 750f

// How much closer than the fitted view "Show on map" zooms in on a beacon.
private const val FOCUS_ZOOM = 2.5f

// How short the first tap must be, and how soon the second must follow it, for the one-handed zoom.
// Tighter than Android's double tap (400 and 300 ms), so a tap followed by a pan stays a pan.
private const val QUICK_ZOOM_TAP_MS = 200L
private const val QUICK_ZOOM_GAP_MS = 200L

/**
 * Where the plan sits on screen. In a portrait view the plan is turned a quarter clockwise, so the
 * building's long axis (x) runs down the screen and a 106 x 13 m floor fills the phone; y then runs left
 * to right. It is a turn, not a mirror: rooms keep their real left/right order.
 */
// [origin] is where the frame's (0, 0) lands on screen. Every floor shares that frame, so a view shows
// the same spot on any floor; anchoring to a floor's own outline would shift floors whose walls differ.
private data class PlanView(val rotated: Boolean, val pxPerM: Float, val origin: Offset) {
    @Suppress("UNUSED_PARAMETER")
    fun toScreen(plan: FloorPlan, x: Float, y: Float): Offset = if (rotated) {
        Offset(origin.x + y * pxPerM, origin.y + x * pxPerM)
    } else {
        Offset(origin.x + x * pxPerM, origin.y - y * pxPerM)
    }

    @Suppress("UNUSED_PARAMETER")
    fun toPlan(plan: FloorPlan, p: Offset): Offset = if (rotated) {
        Offset((p.y - origin.y) / pxPerM, (p.x - origin.x) / pxPerM)
    } else {
        Offset((p.x - origin.x) / pxPerM, (origin.y - p.y) / pxPerM)
    }

    fun zoomedAround(centroid: Offset, zoom: Float): PlanView {
        val s = (pxPerM * zoom).coerceIn(MIN_PX_PER_M, MAX_PX_PER_M)
        val k = s / pxPerM
        return copy(pxPerM = s, origin = centroid - (centroid - origin) * k)
    }

    companion object {
        /** The team's lab, where the camera goes home to. */
        const val HOME_ROOM = "1040"

        /** Fitted to the floor's width and centred on [HOME_ROOM] if this floor has it. */
        fun home(plan: FloorPlan, viewport: IntSize): PlanView {
            val fit = fitting(plan, viewport)
            val room = plan.rooms.firstOrNull { it.number == HOME_ROOM } ?: return fit
            val atZero = fit.copy(origin = Offset.Zero).toScreen(plan, room.x, room.y)
            val center = Offset(viewport.width / 2f, viewport.height / 2f)
            // Keep the fitted sideways position; only scroll along the building to the room.
            return if (fit.rotated) {
                fit.copy(origin = Offset(fit.origin.x, center.y - atZero.y))
            } else {
                fit.copy(origin = Offset(center.x - atZero.x, fit.origin.y))
            }
        }

        /** Centred on the plan point ([x], [y]), [zoom] times closer than the fitted view. */
        fun centredOn(plan: FloorPlan, viewport: IntSize, x: Float, y: Float, zoom: Float): PlanView {
            val fit = fitting(plan, viewport)
            val scaled = fit.copy(pxPerM = (fit.pxPerM * zoom).coerceIn(MIN_PX_PER_M, MAX_PX_PER_M), origin = Offset.Zero)
            val atZero = scaled.toScreen(plan, x, y)
            return scaled.copy(origin = Offset(viewport.width / 2f, viewport.height / 2f) - atZero)
        }

        fun fitting(plan: FloorPlan, viewport: IntSize): PlanView {
            val rotated = viewport.height > viewport.width
            val margin = 24f
            val (planW, planH) = if (rotated) plan.height to plan.width else plan.width to plan.height
            // Fit the short side; the long side scrolls.
            val s = ((viewport.width - 2 * margin) / planW).coerceIn(MIN_PX_PER_M, MAX_PX_PER_M)
            // Put the floor's top-left corner at the margin, or centre it vertically if it is short.
            val top = if (planH * s < viewport.height) (viewport.height - planH * s) / 2 else margin
            val origin = if (rotated) {
                Offset(margin - plan.y0 * s, top - plan.x0 * s)
            } else {
                Offset(margin - plan.x0 * s, top + plan.y1 * s)
            }
            return PlanView(rotated, s, origin)
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
    focus: MapFocusRequest,
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
    // Kept outside the detail host, which drops the map while a beacon's detail is open. Not reset on a
    // floor change: all floors share one frame, so the same view shows the same spot one floor up or down.
    var view by remember(building) { mutableStateOf<PlanView?>(null) }

    // Asked to place a beacon: close any detail, open its floor in edit mode; MapContent drops it at the centre.
    val placeTile = focus.placeBeaconId?.let { id -> tiles.firstOrNull { it.savedEntry?.id == id } }
    LaunchedEffect(placeTile?.tileKey) {
        val tile = placeTile ?: return@LaunchedEffect
        detailKey = null
        planFloorOf(tile.floor).takeIf { it in floors }?.let { floor = it }
        editSession.start()
    }

    // Asked to show a beacon: close any detail, go to its floor; MapContent centres on it once laid out.
    val focusTile = focus.beaconId?.let { id -> tiles.firstOrNull { it.savedEntry?.id == id } }
    LaunchedEffect(focusTile?.tileKey) {
        val tile = focusTile ?: return@LaunchedEffect
        detailKey = null
        planFloorOf(tile.floor).takeIf { it in floors }?.let { floor = it }
    }
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
                focusTarget = focusTile?.savedEntry?.let { e ->
                    if (e.x != null && e.y != null && planFloorOf(focusTile.floor) == floor) Offset(e.x.toFloat(), e.y.toFloat()) else null
                },
                onFocusDone = { focus.beaconId = null },
                placeKey = placeTile?.tileKey?.takeIf { planFloorOf(placeTile.floor) == floor && editSession.editing },
                onPlaceDone = { focus.placeBeaconId = null },
            )
        },
        detail = { key ->
            val tile = tiles.firstOrNull { it.tileKey == key } ?: lastKnownTiles[key]
            if (tile != null) {
                BeaconDetailOverlay(
                    tile = tile,
                    onClose = { detailKey = null },
                    onEdit = { editingTile = tile },
                    onShowOnMap = tile.savedEntry?.let { entry -> { focus.beaconId = entry.id } },
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
            onSetPositionOnMap = { id ->
                editingTile = null
                focus.placeBeaconId = id
            },
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
    focusTarget: Offset?,
    onFocusDone: () -> Unit,
    placeKey: String?,
    onPlaceDone: () -> Unit,
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

    LaunchedEffect(focusTarget, plan, viewport) {
        val target = focusTarget ?: return@LaunchedEffect
        val p = plan ?: return@LaunchedEffect
        if (viewport == IntSize.Zero) return@LaunchedEffect
        onViewChange(PlanView.centredOn(p, viewport, target.x, target.y, zoom = FOCUS_ZOOM))
        onFocusDone()
    }

    LaunchedEffect(placeKey, plan, viewport, view != null) {
        val key = placeKey ?: return@LaunchedEffect
        val p = plan ?: return@LaunchedEffect
        val v = view ?: return@LaunchedEffect
        if (viewport == IntSize.Zero) return@LaunchedEffect
        val tile = tiles.firstOrNull { it.tileKey == key } ?: return@LaunchedEffect
        val at = v.toPlan(p, Offset(viewport.width / 2f, viewport.height / 2f))
        onMove(tile, at.x.toDouble(), at.y.toDouble())
        onPlaceDone()
    }

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
            IconButton(onClick = { plan?.let { p -> if (viewport != IntSize.Zero) onViewChange(PlanView.home(p, viewport)) } }) {
                Icon(Icons.Default.MyLocation, contentDescription = "Go to room ${PlanView.HOME_ROOM}")
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
                    // One-handed zoom: tap, then hold the second tap and drag down to zoom in, up to zoom out,
                    // around the centre of the map.
                    awaitEachGesture {
                        val first = awaitFirstDown(requireUnconsumed = false)
                        // Not waitForUpOrCancellation: the tap detector below consumes the up, which that
                        // function would take as a cancellation.
                        // A first touch that moves is a pan or drag, not the first tap of the gesture.
                        val tapped: Boolean? = withTimeoutOrNull(QUICK_ZOOM_TAP_MS) {
                            var result: Boolean? = null
                            while (result == null) {
                                val changes = awaitPointerEvent().changes
                                result = when {
                                    changes.any { (it.position - first.position).getDistance() > viewConfiguration.touchSlop } -> false
                                    changes.none { it.pressed } -> true
                                    else -> null
                                }
                            }
                            result
                        }
                        if (tapped != true) return@awaitEachGesture
                        val second = withTimeoutOrNull(QUICK_ZOOM_GAP_MS) {
                            awaitFirstDown(requireUnconsumed = false)
                        } ?: return@awaitEachGesture
                        if ((second.position - first.position).getDistance() > viewConfiguration.touchSlop * 4) {
                            return@awaitEachGesture
                        }
                        val start = latestView ?: return@awaitEachGesture
                        val center = Offset(size.width / 2f, size.height / 2f)
                        var zooming = false
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == second.id } ?: break
                            if (!change.pressed) break
                            val dy = change.position.y - second.position.y
                            if (!zooming && abs(dy) > viewConfiguration.touchSlop) zooming = true
                            if (zooming) {
                                change.consume()
                                onViewChange(start.zoomedAround(center, exp(dy / QUICK_ZOOM_PX_PER_E)))
                            }
                        }
                    }
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
            // Plan y points up and screen y down, so a quarter turn plus that flip swaps the axes.
            rotate(90f, Offset.Zero)
            scale(v.pxPerM, -v.pxPerM, Offset.Zero)
        } else {
            scale(v.pxPerM, -v.pxPerM, Offset.Zero)
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
