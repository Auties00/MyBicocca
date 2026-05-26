package it.attendance100.mybicocca.ui.screen.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
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
import kotlinx.coroutines.launch

// Campus center (Piazza dell'Ateneo Nuovo). The camera target is fenced to Italy and the
// minimum zoom keeps the user from drifting out to the whole globe.
private val BICOCCA = LatLng(45.5160, 9.2120)
private val ITALY_BOUNDS = LatLngBounds(LatLng(35.40, 6.60), LatLng(47.10, 18.55))
private const val MIN_ZOOM = 5f
private const val CAMPUS_ZOOM = 15.5f
private const val BUILDING_ZOOM = 17f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
    onOpenRoom360: (String, String) -> Unit = { _, _ -> },
    viewModel: MapViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = LocalAppSnackbarController.current

    val buildings by viewModel.buildings.collectAsStateWithLifecycle()
    val selectedBuilding by viewModel.selectedBuilding.collectAsStateWithLifecycle()
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val selectedRoom by viewModel.selectedRoom.collectAsStateWithLifecycle()
    val roomDetail by viewModel.roomDetail.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()

    // The shell's "Cerca aule" box drives building search; the top-bar filter icon opens the sheet.
    LaunchedEffect(searchQuery) { viewModel.setSearch(searchQuery) }
    var showFilterSheet by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { onProvideFilterToggle { showFilterSheet = true } }

    LaunchedEffect(Unit) {
        viewModel.oneShotEvents.collect { event ->
            when (event) {
                is MapOneShotEvent.RefreshFailed ->
                    snackbar.showError("Impossibile aggiornare le aule", event.cause)
                MapOneShotEvent.LocationPermissionDenied ->
                    snackbar.showInfo("Permesso di localizzazione negato")
            }
        }
    }

    var hasLocationPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(BICOCCA, CAMPUS_ZOOM)
    }

    fun centerOnUser() {
        if (!hasLocationPermission) return
        runCatching {
            fusedLocation.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(location.latitude, location.longitude),
                                BUILDING_ZOOM,
                            ),
                        )
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) centerOnUser() else viewModel.reportLocationPermissionDenied()
    }

    LaunchedEffect(selectedBuilding) {
        selectedBuilding?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(it.point.latitude, it.point.longitude), BUILDING_ZOOM),
            )
        }
    }

    val mapStyle = rememberBicoccaMapStyle()
    val properties = remember(mapStyle, hasLocationPermission) {
        MapProperties(
            mapStyleOptions = mapStyle,
            isMyLocationEnabled = hasLocationPermission,
            latLngBoundsForCameraTarget = ITALY_BOUNDS,
            minZoomPreference = MIN_ZOOM,
        )
    }
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
        )
    }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    LaunchedEffect(selectedBuilding) {
        if (selectedBuilding != null) sheetState.partialExpand() else sheetState.hide()
    }
    // A user dragging the sheet shut clears the selection so a re-tap re-opens it.
    LaunchedEffect(sheetState.currentValue) {
        if (sheetState.currentValue == SheetValue.Hidden) viewModel.clearSelection()
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            val building = selectedBuilding
            if (building != null) {
                BuildingDetailSheet(
                    building = building,
                    rooms = rooms,
                    syncStatus = syncStatus,
                    selectedRoom = selectedRoom,
                    roomDetail = roomDetail,
                    onRoomClick = viewModel::selectRoom,
                    onOpen360 = onOpenRoom360,
                )
            } else {
                Spacer(Modifier.height(1.dp))
            }
        },
        modifier = modifier.fillMaxSize(),
    ) { _ ->
        Box(Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = properties,
                uiSettings = uiSettings,
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

            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        centerOnUser()
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            ) {
                Icon(Icons.Outlined.MyLocation, contentDescription = "Posizione attuale")
            }
        }
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

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
