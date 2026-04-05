package it.attendance100.mybicocca.ui.screen.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.ui.component.map.BuildingDetailSheet
import it.attendance100.mybicocca.ui.component.map.CampusMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateTo360: (url: String, roomName: String) -> Unit = { _, _ -> },
    searchQuery: String = "",
    viewModel: MapViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val buildings by viewModel.buildings.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val selectedBuilding by viewModel.selectedBuilding.collectAsStateWithLifecycle()
    val buildingRooms by viewModel.buildingRooms.collectAsStateWithLifecycle()
    val todayEvents by viewModel.todayEvents.collectAsStateWithLifecycle()
    val roomDetails by viewModel.roomDetails.collectAsStateWithLifecycle()
    val occupation by viewModel.occupation.collectAsStateWithLifecycle()
    val isLoadingDetail by viewModel.isLoadingDetail.collectAsStateWithLifecycle()

    val filteredBuildings = remember(buildings, searchQuery) {
        if (searchQuery.isBlank()) buildings
        else buildings.filter { it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true) }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(Modifier.fillMaxSize()) {
            CampusMap(
                buildings = filteredBuildings,
                selectedBuilding = selectedBuilding,
                onBuildingClick = { building -> viewModel.selectBuilding(building) },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    // Building detail bottom sheet
    if (selectedBuilding != null) {
        BuildingDetailSheet(
            building = selectedBuilding!!,
            rooms = buildingRooms,
            todayEvents = todayEvents,
            roomDetails = roomDetails,
            occupation = occupation,
            isLoading = isLoadingDetail,
            onDismiss = { viewModel.selectBuilding(null) },
            onNavigateTo360 = onNavigateTo360,
        )
    }
}
