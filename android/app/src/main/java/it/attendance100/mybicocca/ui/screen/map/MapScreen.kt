package it.attendance100.mybicocca.ui.screen.map

import android.graphics.PointF
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.google.gson.JsonPrimitive
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.settings.AppTheme
import it.attendance100.mybicocca.ui.screen.map.component.BuildingPin
import it.attendance100.mybicocca.ui.screen.map.ext.splitLegacyAlias
import it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail.BuildingDetailSheet
import it.attendance100.mybicocca.ui.screen.map.subscreen.buildingsList.BuildingsListSheet
import it.attendance100.mybicocca.ui.screen.map.subscreen.mapFilter.MapFilterSheet
import it.attendance100.mybicocca.ui.screen.map.theme.MapPalette
import it.attendance100.mybicocca.ui.screen.map.theme.applyBicoccaPalette
import it.attendance100.mybicocca.ui.screen.map.theme.hidePois
import it.attendance100.mybicocca.ui.screen.map.theme.resolveBicoccaStyleJson
import it.attendance100.mybicocca.ui.theme.LocalAppTheme
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.layers.Property
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.tan
import android.graphics.Color as AndroidColor

/** Campus center (Piazza dell'Ateneo Nuovo): the initial camera target. */
private val BICOCCA = LatLng(45.5160, 9.2120)

/**
 * Coverage of the bundled extract — the exact `--bbox` passed to `pmtiles extract`, also
 * recorded in basemap.pmtiles' v3 header. Must stay in sync with the asset whenever it is
 * re-extracted.
 */
private const val EXTRACT_WEST = 9.189
private const val EXTRACT_EAST = 9.653
private const val EXTRACT_SOUTH = 45.491
private const val EXTRACT_NORTH = 45.710

/**
 * Normalized web-mercator world coordinates (0..1, y grows southward) — the space the camera
 * bounds are fit in, so "half a viewport" is an exact span at any latitude. [mercatorY],
 * [longitudeFromMercatorX] and [latitudeFromMercatorY] share the same convention.
 */
private fun mercatorX(longitude: Double): Double = (longitude + 180.0) / 360.0

private fun longitudeFromMercatorX(x: Double): Double = x * 360.0 - 180.0

private fun mercatorY(latitude: Double): Double {
    val rad = Math.toRadians(latitude)
    return (1.0 - ln(tan(PI / 4 + rad / 2)) / PI) / 2.0
}

private fun latitudeFromMercatorY(y: Double): Double =
    Math.toDegrees(2.0 * atan(exp(PI * (1.0 - 2.0 * y))) - PI / 2.0)

private val EXTRACT_X_MIN = mercatorX(EXTRACT_WEST)
private val EXTRACT_X_MAX = mercatorX(EXTRACT_EAST)
private val EXTRACT_Y_MIN = mercatorY(EXTRACT_NORTH)
private val EXTRACT_Y_MAX = mercatorY(EXTRACT_SOUTH)

/**
 * The extract's bbox inset so that constraining the camera TARGET to it keeps the VISIBLE
 * viewport on tiled ground. Insets are in normalized-mercator units; the vertical pair is
 * asymmetric because the visible strip excludes the area behind the floating bar. If the
 * viewport outgrows the extract on an axis (transiently possible mid-resize — the min-zoom
 * floor prevents it in steady state), that axis pins to the midpoint of its inset range.
 */
private fun viewportFitBounds(
    westInset: Double,
    eastInset: Double,
    northInset: Double,
    southInset: Double,
): LatLngBounds {
    val xMin = EXTRACT_X_MIN + westInset
    val xMax = EXTRACT_X_MAX - eastInset
    val xMid = (EXTRACT_X_MIN + EXTRACT_X_MAX) / 2
    val yMin = EXTRACT_Y_MIN + northInset
    val yMax = EXTRACT_Y_MAX - southInset
    val yMid = (yMin + yMax) / 2
    return LatLngBounds.from(
        latitudeFromMercatorY(if (yMin <= yMax) yMin else yMid),
        longitudeFromMercatorX(if (xMin <= xMax) xMax else xMid),
        latitudeFromMercatorY(if (yMin <= yMax) yMax else yMid),
        longitudeFromMercatorX(if (xMin <= xMax) xMin else xMid),
    )
}

/**
 * Physical px spanned by the full mercator world at the CURRENT zoom, measured through the
 * live projection (two probe points half a view apart). Self-consistent with whatever
 * pixel/density conventions the renderer applies internally — unlike camera padding or a
 * 512·2^zoom constant, it cannot drift from what is actually on screen. Requires bearing/tilt
 * locked (they are).
 */
private fun MapLibreMap.worldPixelsAtCurrentZoom(): Double {
    val x = width / 2f
    val north = projection.fromScreenLocation(PointF(x, height * 0.25f))
    val south = projection.fromScreenLocation(PointF(x, height * 0.75f))
    return height * 0.5 / (mercatorY(south.latitude) - mercatorY(north.latitude))
}

/**
 * Latitude the camera target must aim at so that [latitude] lands on screen row [focalY]: the
 * target itself always projects to the view's vertical center, so it is offset by the
 * mercator span between the center and [focalY]. [targetLongitudeFor] is the horizontal twin.
 */
private fun MapLibreMap.targetLatitudeFor(latitude: Double, focalY: Double, worldPx: Double): Double =
    latitudeFromMercatorY(mercatorY(latitude) + (height / 2.0 - focalY) / worldPx)

private fun MapLibreMap.targetLongitudeFor(longitude: Double, focalX: Double, worldPx: Double): Double =
    longitudeFromMercatorX(mercatorX(longitude) + (width / 2.0 - focalX) / worldPx)

/**
 * Absolute zoom floor, kept city-level rather than the globe because the offline extract
 * covers the Milan area only; map bring-up raises the effective floor further, to the zoom
 * where the visible strip would outgrow the extract and vertical bounds fitting would have
 * no solution.
 */
private const val MIN_ZOOM = 10.0
private const val CAMPUS_ZOOM = 15.5
private const val BUILDING_ZOOM = 17.0
private const val MAX_ZOOM = 19.0

/**
 * A uniform margin inset on every side of the visible viewport before the building is centered
 * in it, so the pin never sits flush against the app bar / sheet edge. Symmetric, so it leaves
 * the center untouched in the normal case; it only bites when an axis is nearly fully covered.
 */
private val VIEWPORT_PADDING = 16.dp

/**
 * The map tab: the campus rendered edge-to-edge on MapLibre from the offline Protomaps pmtiles
 * extract, one tappable pin per building from the bundled catalog, and a "Visualizza edifici"
 * FAB in the bottom-end corner. Tapping a pin selects its building and opens a scrimless
 * detail modal over the live map — dimming the map would defeat the camera framing the
 * building above the sheet — while tapping bare map clears the selection. The FAB opens the
 * buildings list sheet; selections made inside it stay in that sheet's pager, so the
 * standalone detail modal (and its camera centering) reacts to pin taps only. The shell's
 * filter affordance opens the category filter sheet, registered via [onProvideFilterToggle].
 * Room refresh failures surface as in-sheet error states in the building detail / buildings
 * list sheets (driven by sync status plus a retry action), never as a transient snackbar over
 * the map.
 *
 * Theme reactivity: the brand theme paints the base map with its exact hand-tuned hexes, every
 * other palette (Material You, Oceano, Bosco) derives map colors from its M3 scheme, and the
 * live style is recolored in place — no reload — so theme switches are instant while POI icons
 * keep Protomaps' own colors. Pin colors come from the M3 scheme (primary head, white text),
 * not the map palette: each building registers two bitmaps (idle + selected) and a symbol, so
 * reflecting the selection just swaps each symbol's icon.
 *
 * Camera framing uses no camera padding anywhere — its internal px/dp conversion is unreliable
 * in this SDK — and no animateCamera flights that a later update would have to interrupt: all
 * framing is plain camera targets offset with the mercator world-pixel scale calibrated
 * through the live projection ([worldPixelsAtCurrentZoom]). Bring-up centers the campus in the
 * strip below the floating bar and floors the zoom where the visible strip (full width ×
 * between-bar height) would outgrow the extract. Pan bounds keep the VISIBLE map inside the
 * extract, not just the camera target: MapLibre's bounds API clamps the center only, which
 * leaves half a viewport free to slide past the bbox (the void shows mostly vertically —
 * screens are tall and the extract is short, while horizontally the bbox-intersecting tiles
 * overshoot far enough to hide it). The target bounds are re-inset on every zoom change via
 * [viewportFitBounds], vertically asymmetric so panning stops where the data edge meets the
 * app bar's bottom (north) or the view's bottom (south). A zoom-out tightens the bounds under
 * the camera: ongoing gestures self-correct on their next frame, but one that ends outside
 * gets an explicit ease back in.
 *
 * Sheet choreography: the tapped building is framed in the visible viewport — the part of the
 * map with nothing drawn over it: the status bar + floating app bar cap the top, the detail
 * sheet caps the bottom, side insets cap the width, and a uniform [VIEWPORT_PADDING] margin
 * insets all four (the FAB is ignored) — in map-local px, where (0,0) is both the map's and
 * the window's top-left because the map is edge to edge. How much of the map the sheet covers
 * is its surface height minus the slice hanging below the map view (the bottom inset): the
 * modal owns its whole surface (no default drag handle, no auto inset padding; the nav-bar
 * spacer is part of the measured column), so ONE node measures the surface from its top down
 * to the screen bottom and, a height being window-agnostic, no cross-window position math is
 * needed. That node is keyed on the building code so an in-place A->B selection swap recreates
 * it and onSizeChanged is guaranteed to re-fire even when B lays out at A's exact height — a
 * bare state reset could otherwise wait forever on a callback that never comes — and each
 * pick's framing waits for the sheet's first size report so it never runs against a stale
 * cover. The drive is ONE per-frame moveCamera fed by smooth inputs, so the camera never
 * snaps: an entrance spring (0..1) on the sheet's own motion curve — rather than a
 * fixed-duration fly that would finish out of step with the modal — interpolates zoom and pan
 * from the current camera to the framed target, and past 1 the same loop keeps re-deriving the
 * target from the live cover, riding the sheet's animateContentSize (rooms landing, room-page
 * swaps) frame for frame so the building stays centered in the strip the sheet leaves. The
 * framed target rescales the calibrated world-pixel span by 2^Δzoom — mercator scale is
 * exactly 2^zoom — so the px offset from the view center to the focal point converts to a
 * camera target with no padding/unit guesswork. The focal point drops below the strip center
 * by the selected pin's head-center offset so the head — what reads as "the pin" — is what
 * lands centered, clamped back into the strip when it is too short to fit it; if the margin
 * over-shrinks an axis, the focal point pins to that axis's near edge rather than inverting.
 *
 * @param isActive true only while this is the visible tab — see CalendarScreen for the
 *   pager-cache rationale; the filter-sheet opener is (re)registered on each activation.
 * @param contentInsets the shell's content insets: top = floating app bar + status bar, bottom
 *   = nav bar. The map renders edge-to-edge behind the top bar, so the user-visible viewport
 *   is the map view minus the top inset — every camera framing computation centers content in
 *   that strip. The shell ends the map view at the nav bar's TOP edge, so the bottom inset is
 *   the gap between the map view's bottom and the screen bottom, i.e. the slice of the detail
 *   sheet that hangs BELOW the map and so covers none of it.
 */
@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    contentInsets: PaddingValues = PaddingValues(),
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
    viewModel: MapViewModel = hiltViewModel(
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current

    val startInsetPx = with(density) { contentInsets.calculateStartPadding(layoutDirection).roundToPx() }
    val endInsetPx = with(density) { contentInsets.calculateEndPadding(layoutDirection).roundToPx() }
    val topInsetPx = with(density) { contentInsets.calculateTopPadding().roundToPx() }
    val bottomBarPx = with(density) { contentInsets.calculateBottomPadding().roundToPx() }
    val viewportPaddingPx = with(density) { VIEWPORT_PADDING.roundToPx() }
    val pinHeadCenterOffsetPx = BuildingPin.selectedHeadCenterOffset(density.density)

    val buildings by viewModel.buildings.collectAsStateWithLifecycle()
    val allBuildings by viewModel.allBuildings.collectAsStateWithLifecycle()
    val selectedBuilding by viewModel.selectedBuilding.collectAsStateWithLifecycle()
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val selectedRoom by viewModel.selectedRoom.collectAsStateWithLifecycle()
    val roomDetail by viewModel.roomDetail.collectAsStateWithLifecycle()
    val daySchedule by viewModel.daySchedule.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showBuildingsList by remember { mutableStateOf(false) }
    LaunchedEffect(isActive) { if (isActive) onProvideFilterToggle { showFilterSheet = true } }

    val appTheme = LocalAppTheme.current
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val palette = remember(appTheme, scheme, dark) {
        when (appTheme) {
            AppTheme.Default -> if (dark) MapPalette.BicoccaDark else MapPalette.BicoccaLight
            else -> MapPalette.fromColorScheme(scheme, dark)
        }
    }
    val pinIdleContainer = scheme.primary.toArgb()
    val pinSelContainer = scheme.primaryContainer.toArgb()
    val pinBorder = scheme.surface.toArgb()
    val pinText = AndroidColor.WHITE

    val mapView = rememberMapViewWithLifecycle()
    var libreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var mapStyle by remember { mutableStateOf<Style?>(null) }
    var symbolManager by remember { mutableStateOf<SymbolManager?>(null) }
    val symbols = remember { mutableMapOf<String, Symbol>() }

    LaunchedEffect(mapView) {
        val map = suspendCancellableCoroutine<MapLibreMap> { cont ->
            mapView.getMapAsync { cont.resume(it) }
        }
        map.uiSettings.apply {
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isCompassEnabled = false
            isLogoEnabled = false
            isAttributionEnabled = false
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(BICOCCA, CAMPUS_ZOOM))
        val campusWorldPx = map.worldPixelsAtCurrentZoom()
        val visibleWidthPx = (map.width - startInsetPx - endInsetPx).coerceAtLeast(1f).toDouble()
        val visibleHeightPx = (map.height - topInsetPx).coerceAtLeast(1f).toDouble()

        val campusZoom = map.cameraPosition.zoom
        map.setMinZoomPreference(
            maxOf(
                MIN_ZOOM,
                campusZoom + log2(visibleWidthPx / (campusWorldPx * (EXTRACT_X_MAX - EXTRACT_X_MIN))),
                campusZoom + log2(visibleHeightPx / (campusWorldPx * (EXTRACT_Y_MAX - EXTRACT_Y_MIN))),
            ),
        )
        map.setMaxZoomPreference(MAX_ZOOM)

        val initialFocalX = (startInsetPx + map.width - endInsetPx) / 2.0
        val initialFocalY = (topInsetPx + map.height) / 2.0
        map.moveCamera(
            CameraUpdateFactory.newLatLng(
                LatLng(
                    map.targetLatitudeFor(BICOCCA.latitude, initialFocalY, campusWorldPx),
                    map.targetLongitudeFor(BICOCCA.longitude, initialFocalX, campusWorldPx),
                ),
            ),
        )

        var fitZoom = Double.NaN
        var fitBounds: LatLngBounds? = null
        fun refitCameraBounds() {
            val zoom = map.cameraPosition.zoom
            if (zoom == fitZoom) return
            fitZoom = zoom
            val worldPx = map.worldPixelsAtCurrentZoom()
            val bounds = viewportFitBounds(
                westInset = (map.width / 2.0 - startInsetPx) / worldPx,
                eastInset = (map.width / 2.0 - endInsetPx) / worldPx,
                northInset = (map.height / 2.0 - topInsetPx) / worldPx,
                southInset = map.height / 2.0 / worldPx,
            )
            fitBounds = bounds
            map.setLatLngBoundsForCameraTarget(bounds)
        }
        map.addOnCameraMoveListener { refitCameraBounds() }
        map.addOnCameraIdleListener {
            refitCameraBounds()
            val bounds = fitBounds ?: return@addOnCameraIdleListener
            val target = map.cameraPosition.target ?: return@addOnCameraIdleListener
            val easedLat = target.latitude.coerceIn(bounds.latitudeSouth, bounds.latitudeNorth)
            val easedLon = target.longitude.coerceIn(bounds.longitudeWest, bounds.longitudeEast)
            if (easedLat != target.latitude || easedLon != target.longitude) {
                map.animateCamera(CameraUpdateFactory.newLatLng(LatLng(easedLat, easedLon)))
            }
        }
        refitCameraBounds()

        val styleJson = resolveBicoccaStyleJson(context)
        val style = suspendCancellableCoroutine<Style> { cont ->
            map.setStyle(Style.Builder().fromJson(styleJson)) { cont.resume(it) }
        }
        style.hidePois()
        val sm = SymbolManager(mapView, map, style).apply {
            iconAllowOverlap = true
            iconIgnorePlacement = true
        }
        sm.addClickListener { symbol ->
            symbol.data?.asString?.let { viewModel.selectBuilding(BuildingCode(it)) }
            true
        }
        map.addOnMapClickListener {
            viewModel.clearSelection()
            false
        }
        libreMap = map
        mapStyle = style
        symbolManager = sm
    }

    LaunchedEffect(palette, mapStyle) { mapStyle?.applyBicoccaPalette(palette) }

    LaunchedEffect(buildings, symbolManager, mapStyle, pinIdleContainer, pinSelContainer, pinBorder) {
        val sm = symbolManager ?: return@LaunchedEffect
        val style = mapStyle ?: return@LaunchedEffect
        val pins = (buildings as? Loadable.Loaded)?.value.orEmpty()
        sm.deleteAll()
        symbols.clear()
        val d = density.density
        pins.forEach { building ->
            val codeValue = building.code.value
            val pinCode = splitLegacyAlias(building.name).second ?: codeValue
            val idleKey = "pin_${codeValue}_idle"
            val selKey = "pin_${codeValue}_sel"
            style.addImage(idleKey, BuildingPin.render(d, pinCode, pinIdleContainer, pinBorder, pinText, selected = false))
            style.addImage(selKey, BuildingPin.render(d, pinCode, pinSelContainer, pinBorder, pinText, selected = true))
            val isSelected = building.code == selectedBuilding?.code
            val symbol = sm.create(
                SymbolOptions()
                    .withLatLng(LatLng(building.point.latitude, building.point.longitude))
                    .withIconImage(if (isSelected) selKey else idleKey)
                    .withIconAnchor(Property.ICON_ANCHOR_BOTTOM)
                    .withData(JsonPrimitive(codeValue)),
            )
            symbols[codeValue] = symbol
        }
    }

    LaunchedEffect(selectedBuilding, symbolManager) {
        val sm = symbolManager ?: return@LaunchedEffect
        if (symbols.isEmpty()) return@LaunchedEffect
        val selectedValue = selectedBuilding?.code?.value
        symbols.forEach { (codeValue, symbol) ->
            symbol.iconImage = if (codeValue == selectedValue) "pin_${codeValue}_sel" else "pin_${codeValue}_idle"
        }
        sm.update(symbols.values.toList())
    }

    val detailModalBuilding = if (showBuildingsList) null else selectedBuilding

    var sheetSurfaceHeightPx by remember { mutableIntStateOf(0) }
    val modalState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val entranceSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()

    LaunchedEffect(detailModalBuilding, libreMap) {
        val map = libreMap ?: return@LaunchedEffect
        val building = detailModalBuilding ?: return@LaunchedEffect

        sheetSurfaceHeightPx = 0

        fun coverOrNull(): Int? =
            if (sheetSurfaceHeightPx <= 0) null else (sheetSurfaceHeightPx - bottomBarPx).coerceAtLeast(0)

        fun framedTarget(coverPx: Int, zoom: Double): LatLng {
            val left = startInsetPx + viewportPaddingPx
            val right = map.width - endInsetPx - viewportPaddingPx
            val top = topInsetPx + viewportPaddingPx
            val bottom = map.height - coverPx - viewportPaddingPx
            val focalX = ((left + right) / 2.0).coerceAtLeast(left.toDouble())
            val focalY = ((top + bottom) / 2.0 + pinHeadCenterOffsetPx)
                .coerceIn(top.toDouble(), maxOf(top.toDouble(), bottom.toDouble()))
            val worldPx = map.worldPixelsAtCurrentZoom() * 2.0.pow(zoom - map.cameraPosition.zoom)
            return LatLng(
                map.targetLatitudeFor(building.point.latitude, focalY, worldPx),
                map.targetLongitudeFor(building.point.longitude, focalX, worldPx),
            )
        }

        snapshotFlow { coverOrNull() }.filterNotNull().first()

        val startZoom = map.cameraPosition.zoom
        val startTarget = map.cameraPosition.target
            ?: LatLng(building.point.latitude, building.point.longitude)
        val entrance = Animatable(0f)
        launch { entrance.animateTo(1f, entranceSpec) }

        snapshotFlow { entrance.value to (coverOrNull() ?: 0) }
            .collect { (progress, cover) ->
                val p = progress.toDouble()
                val framed = framedTarget(cover, BUILDING_ZOOM)
                val target = LatLng(
                    startTarget.latitude + (framed.latitude - startTarget.latitude) * p,
                    startTarget.longitude + (framed.longitude - startTarget.longitude) * p,
                )
                val zoom = startZoom + (BUILDING_ZOOM - startZoom) * p
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(target, zoom))
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
        )

        ExtendedFloatingActionButton(
            onClick = { showBuildingsList = true },
            icon = { Icon(Icons.Outlined.Apartment, contentDescription = null) },
            text = { Text(stringResource(R.string.map_view_buildings)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }

    if (detailModalBuilding != null) {
        it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet(
            onDismiss = { viewModel.clearSelection() },
            scrimColor = Color.Transparent,
            dragHandle = null,
            contentWindowInsets = { WindowInsets(0) },
        ) { _, _ ->
            val navBottomPx = WindowInsets.navigationBars.getBottom(density)
            key(detailModalBuilding.code) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { sheetSurfaceHeightPx = it.height },
                ) {
                    BottomSheetDefaults.DragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
                    BuildingDetailSheet(
                        building = detailModalBuilding,
                        rooms = rooms,
                        daySchedule = daySchedule,
                        syncStatus = syncStatus,
                        selectedRoom = selectedRoom,
                        roomDetail = roomDetail,
                        onRoomClick = viewModel::selectRoom,
                        onCloseRoom = viewModel::clearRoomSelection,
                        onRetryRooms = viewModel::retryRooms,
                    )
                    Spacer(modifier = Modifier.height(with(density) { navBottomPx.toDp() }))
                }
            }
        }
    }

    if (showBuildingsList) {
        BuildingsListSheet(
            buildings = (allBuildings as? Loadable.Loaded)?.value.orEmpty(),
            detailBuilding = selectedBuilding,
            rooms = rooms,
            daySchedule = daySchedule,
            syncStatus = syncStatus,
            selectedRoom = selectedRoom,
            roomDetail = roomDetail,
            onShowInfo = viewModel::selectBuilding,
            onRoomClick = viewModel::selectRoom,
            onCloseRoom = viewModel::clearRoomSelection,
            onBack = viewModel::clearSelection,
            onDismiss = {
                showBuildingsList = false
                viewModel.clearSelection()
            },
            onRetryRooms = viewModel::retryRooms,
        )
    }

    if (showFilterSheet) {
        MapFilterSheet(
            selected = categoryFilter,
            onToggle = viewModel::toggleCategory,
            onClear = viewModel::clearCategories,
            onDismiss = { showFilterSheet = false },
        )
    }
}

/**
 * MapLibre's MapView is an Android View with a manual lifecycle, bound here to the
 * composition: create, catch up to the current lifecycle state (addObserver does not replay
 * past events, so a View added while already RESUMED must be started/resumed eagerly), then
 * forward future events.
 */
@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply { onCreate(null) }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStart()
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onResume()
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onStop()
            mapView.onDestroy()
        }
    }
    return mapView
}
