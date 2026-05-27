package it.attendance100.mybicocca.ui.screen.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import it.attendance100.mybicocca.ui.screen.map.state.MapOneShotEvent
import it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail.BuildingDetailSheet
import it.attendance100.mybicocca.ui.screen.map.subscreen.buildingsList.BuildingsListSheet

// Campus center (Piazza dell'Ateneo Nuovo). minZoom keeps the user from drifting out to the globe.
private val BICOCCA = LatLng(45.5160, 9.2120)

private const val MIN_ZOOM = 5f
private const val CAMPUS_ZOOM = 15.5f
private const val BUILDING_ZOOM = 17f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
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
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()

    // The shell's "Cerca aule" box drives building search; the top-bar filter icon opens the sheet.
    LaunchedEffect(searchQuery) { viewModel.setSearch(searchQuery) }
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

    // Map contentPadding so the projected center sits in the area the chrome doesn't cover: the
    // floating top bar up top, and the open detail sheet at the bottom (its height minus the nav
    // bar the sheet also overlaps). This is what makes "center on building" land above the sheet.
    val topInsetPx = with(density) { contentInsets.calculateTopPadding().roundToPx() }
    val bottomBarPx = with(density) { contentInsets.calculateBottomPadding().roundToPx() }
    var sheetHeightPx by remember { mutableIntStateOf(0) }
    val sheetCoverPx = if (selectedBuilding != null) (sheetHeightPx - bottomBarPx).coerceAtLeast(0) else 0
    val mapContentPadding = remember(topInsetPx, sheetCoverPx, density) {
        with(density) { PaddingValues(top = topInsetPx.toDp(), bottom = sheetCoverPx.toDp()) }
    }

    // Center the selected building in the visible area — wait until the sheet has been measured so
    // the contentPadding offset is in effect first.
    LaunchedEffect(selectedBuilding, sheetHeightPx, topInsetPx) {
        val building = selectedBuilding ?: return@LaunchedEffect
        if (sheetHeightPx == 0) return@LaunchedEffect
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(building.point.latitude, building.point.longitude),
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
                    MarkerComposable(
                        building.code.value,
                        isSelected,
                        state = markerState,
                        title = building.name,
                        onClick = {
                            viewModel.selectBuilding(building.code)
                            true
                        },
                    ) {
                        BuildingMarker(category = building.category, selected = isSelected)
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

    val building = selectedBuilding
    if (building != null) {
        val modalState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelection() },
            sheetState = modalState,
        ) {
            BuildingDetailSheet(
                building = building,
                rooms = rooms,
                syncStatus = syncStatus,
                selectedRoom = selectedRoom,
                roomDetail = roomDetail,
                onRoomClick = viewModel::selectRoom,
                onOpen360 = onOpenRoom360,
                modifier = Modifier.onSizeChanged { sheetHeightPx = it.height },
            )
        }
    }

    if (showBuildingsList) {
        BuildingsListSheet(
            buildings = (allBuildings as? Loadable.Loaded)?.value.orEmpty(),
            onShowOnMap = { code ->
                showBuildingsList = false
                viewModel.selectBuilding(code)
            },
            onDismiss = { showBuildingsList = false },
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
