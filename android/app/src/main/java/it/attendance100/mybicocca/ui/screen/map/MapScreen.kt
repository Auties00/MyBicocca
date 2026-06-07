package it.attendance100.mybicocca.ui.screen.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.google.gson.JsonPrimitive
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.map.component.BuildingPin
import it.attendance100.mybicocca.ui.screen.map.component.MapFilterSheet
import it.attendance100.mybicocca.ui.screen.map.ext.splitLegacyAlias
import it.attendance100.mybicocca.ui.screen.map.state.MapOneShotEvent
import it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail.BuildingDetailSheet
import it.attendance100.mybicocca.ui.screen.map.subscreen.buildingsList.BuildingsListSheet
import it.attendance100.mybicocca.ui.screen.map.theme.MapPalette
import it.attendance100.mybicocca.ui.screen.map.theme.applyBicoccaPalette
import it.attendance100.mybicocca.ui.screen.map.theme.hidePois
import it.attendance100.mybicocca.ui.screen.map.theme.resolveBicoccaStyleJson
import it.attendance100.mybicocca.ui.theme.AppTheme
import it.attendance100.mybicocca.ui.theme.LocalAppTheme
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import kotlinx.coroutines.suspendCancellableCoroutine
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdate
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.layers.Property
import kotlin.coroutines.resume
import kotlin.math.cos
import kotlin.math.pow
import android.graphics.Color as AndroidColor

// Campus center (Piazza dell'Ateneo Nuovo). The offline pmtiles extract covers the Milan area, so
// minZoom is kept city-level rather than the globe — you can't pan off the bundled tiles.
private val BICOCCA = LatLng(45.5160, 9.2120)

private const val MIN_ZOOM = 10.0
private const val CAMPUS_ZOOM = 15.5
private const val BUILDING_ZOOM = 17.0
private const val MAX_ZOOM = 19.0

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    // True only while this is the visible tab — see CalendarScreen for the pager-cache rationale.
    isActive: Boolean = true,
    // The shell's content insets (top = floating app bar + status bar, bottom = nav bar). The map
    // renders edge-to-edge behind the top bar, so we feed the top inset to the map's padding.
    contentInsets: PaddingValues = PaddingValues(),
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
    onOpenRoom360: (String, String) -> Unit = { _, _ -> },
    viewModel: MapViewModel = hiltViewModel(
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val snackbar = LocalAppSnackbarController.current

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

    LaunchedEffect(Unit) {
        viewModel.oneShotEvents.collect { event ->
            when (event) {
                is MapOneShotEvent.RefreshFailed ->
                    snackbar.showError("Impossibile aggiornare le aule", event.cause)
            }
        }
    }

    // Base-map palette: the brand uses its exact hand-tuned hexes; every other palette (Material
    // You, Oceano, Bosco) derives map colors from its M3 scheme so the map tracks the theme.
    val appTheme = LocalAppTheme.current
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val palette = remember(appTheme, scheme, dark) {
        when (appTheme) {
            AppTheme.Default -> if (dark) MapPalette.BicoccaDark else MapPalette.BicoccaLight
            else -> MapPalette.fromColorScheme(scheme, dark)
        }
    }
    // Building-pin colors come from the M3 scheme (not the map palette) — primary head, white text.
    val pinIdleContainer = scheme.primary.toArgb()
    val pinSelContainer = scheme.primaryContainer.toArgb()
    val pinBorder = scheme.surface.toArgb()
    val pinText = AndroidColor.WHITE

    val mapView = rememberMapViewWithLifecycle()
    var libreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var mapStyle by remember { mutableStateOf<Style?>(null) }
    var symbolManager by remember { mutableStateOf<SymbolManager?>(null) }
    val symbols = remember { mutableMapOf<String, Symbol>() }

    // One-time map bring-up: await the map + style, configure gestures/zoom, install the symbol
    // layer and the click handlers.
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
        map.setMinZoomPreference(MIN_ZOOM)
        map.setMaxZoomPreference(MAX_ZOOM)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(BICOCCA, CAMPUS_ZOOM))

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

    // Recolor the live style to the active palette — in place, no reload, so theme changes are
    // instant. POI icons keep Protomaps' own colors.
    LaunchedEffect(palette, mapStyle) { mapStyle?.applyBicoccaPalette(palette) }

    // Top inset goes to the map padding (keeps the camera target below the floating bar). The
    // detail sheet is NOT fed as bottom padding — the camera is shifted instead (below).
    val topInsetPx = with(density) { contentInsets.calculateTopPadding().roundToPx() }
    val bottomBarPx = with(density) { contentInsets.calculateBottomPadding().roundToPx() }
    LaunchedEffect(libreMap, topInsetPx) { libreMap?.setPadding(0, topInsetPx, 0, 0) }

    // Build/refresh the pin symbols whenever the pin set or the pin colors (theme) change. Each
    // building registers two bitmaps (idle + selected) and a symbol; selection just swaps the icon.
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

    // Reflect the current selection by swapping each symbol's icon (idle/selected).
    LaunchedEffect(selectedBuilding, symbolManager) {
        val sm = symbolManager ?: return@LaunchedEffect
        if (symbols.isEmpty()) return@LaunchedEffect
        val selectedValue = selectedBuilding?.code?.value
        symbols.forEach { (codeValue, symbol) ->
            symbol.iconImage = if (codeValue == selectedValue) "pin_${codeValue}_sel" else "pin_${codeValue}_idle"
        }
        sm.update(symbols.values.toList())
    }

    // Selections made from inside the buildings list sheet stay in that sheet's pager — the
    // standalone detail modal (and its camera centering) only reacts to pin taps.
    val detailModalBuilding = if (showBuildingsList) null else selectedBuilding
    var sheetHeightPx by remember { mutableIntStateOf(0) }

    // Center the selected building in the area the sheet doesn't cover. The target is moved south
    // by half the covered height so the building lands above the sheet without bottom padding. The
    // full lat/lng centering runs ONCE per selection; later sheet resizes only nudge vertically by
    // the height delta, so the map never shifts horizontally when the modal content loads.
    LaunchedEffect(detailModalBuilding, libreMap) {
        val map = libreMap ?: return@LaunchedEffect
        val building = detailModalBuilding ?: return@LaunchedEffect
        // Mercator latitude degrees per screen pixel at this latitude/zoom (256dp world at zoom 0).
        val worldWidthPx = 256.0 * density.density * 2.0.pow(BUILDING_ZOOM)
        val latitudePerPixel = 360.0 * cos(Math.toRadians(building.point.latitude)) / worldWidthPx
        var lastCoverPx = -1

        suspend fun animate(update: CameraUpdate) = suspendCancellableCoroutine<Unit> { cont ->
            map.animateCamera(update, object : MapLibreMap.CancelableCallback {
                override fun onFinish() { if (cont.isActive) cont.resume(Unit) }
                override fun onCancel() { if (cont.isActive) cont.resume(Unit) }
            })
        }

        snapshotFlow { (sheetHeightPx - bottomBarPx).coerceAtLeast(0) }.collect { coverPx ->
            when {
                coverPx == 0 -> Unit
                lastCoverPx < 0 -> {
                    lastCoverPx = coverPx
                    animate(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                building.point.latitude - coverPx / 2.0 * latitudePerPixel,
                                building.point.longitude,
                            ),
                            BUILDING_ZOOM,
                        ),
                    )
                }
                coverPx != lastCoverPx -> {
                    // Re-center on the building shifted for the new cover. Longitude is held fixed,
                    // so the map never drifts horizontally when the sheet content resizes; zoom is
                    // left untouched (newLatLng, not newLatLngZoom).
                    lastCoverPx = coverPx
                    animate(
                        CameraUpdateFactory.newLatLng(
                            LatLng(
                                building.point.latitude - coverPx / 2.0 * latitudePerPixel,
                                building.point.longitude,
                            ),
                        ),
                    )
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        ExtendedFloatingActionButton(
            onClick = { showBuildingsList = true },
            icon = { Icon(Icons.Outlined.Apartment, contentDescription = null) },
            text = { Text("Visualizza edifici") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }

    if (detailModalBuilding != null) {
        it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet(
            onDismiss = { viewModel.clearSelection() },
            // No scrim: the camera centers the building in the area the sheet doesn't cover, so
            // dimming the map behind it would defeat the point.
            scrimColor = Color.Transparent,
        ) { _, _ ->
            BuildingDetailSheet(
                building = detailModalBuilding,
                rooms = rooms,
                daySchedule = daySchedule,
                syncStatus = syncStatus,
                selectedRoom = selectedRoom,
                roomDetail = roomDetail,
                onRoomClick = viewModel::selectRoom,
                onCloseRoom = viewModel::clearRoomSelection,
                onOpen360 = onOpenRoom360,
                modifier = Modifier.onSizeChanged { sheetHeightPx = it.height },
            )
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
            onOpen360 = onOpenRoom360,
            onBack = viewModel::clearSelection,
            onDismiss = {
                showBuildingsList = false
                viewModel.clearSelection()
            },
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

// MapLibre's MapView is an Android View with a manual lifecycle. Bind it to the composition: create
// + catch up to the current lifecycle state (addObserver does not replay past events, so a View
// added while already RESUMED must be started/resumed eagerly), then forward future events.
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
