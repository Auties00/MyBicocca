package it.attendance100.mybicocca.ui.screen.map

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.map.component.BuildingMarker
import it.attendance100.mybicocca.ui.screen.map.component.MapFilterSheet
import it.attendance100.mybicocca.ui.screen.map.component.rememberBicoccaMapStyle
import it.attendance100.mybicocca.ui.screen.map.ext.splitLegacyAlias
import it.attendance100.mybicocca.ui.screen.map.state.MapOneShotEvent
import it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail.BuildingDetailSheet
import it.attendance100.mybicocca.ui.screen.map.subscreen.buildingsList.BuildingsListSheet
import kotlin.math.cos
import kotlin.math.pow

// Campus center (Piazza dell'Ateneo Nuovo). minZoom keeps the user from drifting out to the globe.
private val BICOCCA = LatLng(45.5160, 9.2120)

private const val MIN_ZOOM = 5f
private const val CAMPUS_ZOOM = 15.5f
private const val BUILDING_ZOOM = 17f

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    // True only while this is the visible tab — see CalendarScreen for the pager-cache rationale.
    isActive: Boolean = true,
    // The shell's content insets (top = floating app bar + status bar, bottom = nav bar). The map
    // renders edge-to-edge behind the top bar, so we feed these to the map's contentPadding.
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

    // The top-bar filter icon opens the category sheet; building search lives in the
    // unified app-bar search.
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

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(BICOCCA, CAMPUS_ZOOM)
    }

    // Selections made from inside the buildings list sheet stay in that sheet's pager — the
    // standalone detail modal (and its camera centering) only reacts to pin taps.
    val detailModalBuilding = if (showBuildingsList) null else selectedBuilding

    // Only the floating top bar goes through the map's contentPadding. The open detail sheet is
    // deliberately NOT fed in as bottom padding: Google Maps anchors its logo above the bottom
    // padding, so the logo would jump up every time the sheet opens. The sheet's cover is instead
    // compensated by shifting the camera target (below).
    val topInsetPx = with(density) { contentInsets.calculateTopPadding().roundToPx() }
    val bottomBarPx = with(density) { contentInsets.calculateBottomPadding().roundToPx() }
    var sheetHeightPx by remember { mutableIntStateOf(0) }
    val sheetCoverPx = if (detailModalBuilding != null) (sheetHeightPx - bottomBarPx).coerceAtLeast(0) else 0
    val mapContentPadding = remember(topInsetPx, density) {
        with(density) { PaddingValues(top = topInsetPx.toDp()) }
    }

    // Center the selected building in the area the sheet doesn't cover — wait until the sheet has
    // been measured. The target is moved south by half the covered height so the building lands
    // above the sheet without bottom contentPadding (which would also move the Google logo).
    LaunchedEffect(detailModalBuilding, sheetHeightPx) {
        val building = detailModalBuilding ?: return@LaunchedEffect
        if (sheetHeightPx == 0) return@LaunchedEffect
        // Mercator latitude degrees per screen pixel at this latitude/zoom — the world is 256dp
        // wide at zoom 0, so the pixel world width scales with density and 2^zoom.
        val worldWidthPx = 256.0 * density.density * 2.0.pow(BUILDING_ZOOM.toDouble())
        val latitudePerPixel = 360.0 * cos(Math.toRadians(building.point.latitude)) / worldWidthPx
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    building.point.latitude - sheetCoverPx / 2.0 * latitudePerPixel,
                    building.point.longitude,
                ),
                BUILDING_ZOOM,
            ),
        )
    }

    val mapStyle = rememberBicoccaMapStyle()
    val properties = remember(mapStyle) {
        MapProperties(
            mapStyleOptions = mapStyle,
            isMyLocationEnabled = false,
            minZoomPreference = MIN_ZOOM,
        )
    }
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            compassEnabled = false,
            tiltGesturesEnabled = false,
            myLocationButtonEnabled = false,
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings,
            contentPadding = mapContentPadding,
            onMapClick = { viewModel.clearSelection() },
        ) {
            val pins = (buildings as? Loadable.Loaded)?.value.orEmpty()
            pins.forEach { building ->
                key(building.code.value) {
                    val markerState = remember {
                        MarkerState(LatLng(building.point.latitude, building.point.longitude))
                    }
                    val isSelected = building.code == selectedBuilding?.code
                    // The marker is a rasterized bitmap: animating the selection outside and
                    // keying the rasterizer on the animated value re-renders it every frame,
                    // so the pin grows smoothly instead of snapping between the two sizes.
                    val selectionProgress by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                        label = "pin_selection",
                    )
                    MarkerComposable(
                        building.code.value,
                        selectionProgress,
                        state = markerState,
                        title = building.name,
                        onClick = {
                            viewModel.selectBuilding(building.code)
                            true
                        },
                    ) {
                        BuildingMarker(
                            selectionProgress = selectionProgress,
                            code = remember(building.name) { splitLegacyAlias(building.name).second },
                        )
                    }
                }
            }
        }

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
        val modalState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelection() },
            sheetState = modalState,
            // No scrim: the sheet coexists with the map (the camera centers the selected building
            // above it), so dimming the map behind it would defeat the point.
            scrimColor = Color.Transparent,
        ) {
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
