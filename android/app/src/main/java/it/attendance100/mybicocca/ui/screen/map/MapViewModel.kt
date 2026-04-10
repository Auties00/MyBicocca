package it.attendance100.mybicocca.ui.screen.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.campus.Building
import it.attendance100.mybicocca.data.model.campus.CampusEvent
import it.attendance100.mybicocca.data.model.campus.Room
import it.attendance100.mybicocca.data.model.campus.RoomDetails
import it.attendance100.mybicocca.data.model.campus.RoomOccupationEvent
import it.attendance100.mybicocca.data.repository.CampusRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import it.attendance100.mybicocca.data.sync.ResourceSyncManager
import it.attendance100.mybicocca.data.sync.SyncKeys
import it.attendance100.mybicocca.data.sync.SyncPolicies
import it.attendance100.mybicocca.data.sync.SyncUiState
import it.attendance100.mybicocca.util.NetworkMonitor
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val campusRepository: CampusRepository,
    private val resourceSyncManager: ResourceSyncManager,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    val buildings: StateFlow<List<Building>> = campusRepository.observeBuildings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val syncState: StateFlow<SyncUiState> = resourceSyncManager.observe(SyncKeys.CAMPUS_BUILDINGS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState())

    val isRefreshing: StateFlow<Boolean> = syncState
        .map { it.isRefreshing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _selectedBuilding = MutableStateFlow<Building?>(null)
    val selectedBuilding: StateFlow<Building?> = _selectedBuilding.asStateFlow()

    private val _buildingRooms = MutableStateFlow<List<Room>>(emptyList())
    val buildingRooms: StateFlow<List<Room>> = _buildingRooms.asStateFlow()

    private val _todayEvents = MutableStateFlow<List<CampusEvent>>(emptyList())
    val todayEvents: StateFlow<List<CampusEvent>> = _todayEvents.asStateFlow()

    private val _roomDetails = MutableStateFlow<List<RoomDetails>>(emptyList())
    val roomDetails: StateFlow<List<RoomDetails>> = _roomDetails.asStateFlow()

    private val _occupation = MutableStateFlow<List<RoomOccupationEvent>>(emptyList())
    val occupation: StateFlow<List<RoomOccupationEvent>> = _occupation.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _detailError = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = combine(syncState, _detailError) { sync, detail ->
        detail ?: sync.errorMessage
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        refreshIfStale()
    }

    fun refresh() {
        viewModelScope.launch {
            _detailError.value = null
            resourceSyncManager.refresh(SyncKeys.CAMPUS_BUILDINGS, SyncPolicies.Static) {
                campusRepository.refreshBuildings()
            }
        }
    }

    fun clearError() {
        _detailError.value = null
        resourceSyncManager.clearError(SyncKeys.CAMPUS_BUILDINGS)
    }

    fun refreshIfStale() {
        viewModelScope.launch {
            resourceSyncManager.refreshIfStale(SyncKeys.CAMPUS_BUILDINGS, SyncPolicies.Static) {
                campusRepository.refreshBuildings()
            }
        }
    }

    fun selectBuilding(building: Building?) {
        _selectedBuilding.value = building
        if (building != null) {
            loadBuildingDetail(building.code)
        } else {
            clearBuildingDetail()
        }
    }

    private fun loadBuildingDetail(buildingCode: String) {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            _detailError.value = null

            val roomsDeferred = async {
                campusRepository.refreshRooms(buildingCode)
            }
            val eventsDeferred = async {
                campusRepository.getTodayEvents(buildingCode)
            }
            val detailsDeferred = async {
                campusRepository.getRoomDetails(buildingCode)
            }
            val occupationDeferred = async {
                campusRepository.getBuildingOccupation(buildingCode)
            }

            roomsDeferred.await()
            _buildingRooms.value = campusRepository.observeRoomsByBuilding(buildingCode).first()

            eventsDeferred.await()
                .onSuccess { _todayEvents.value = it }
                .onFailure { _detailError.value = it.localizedMessage }

            detailsDeferred.await()
                .onSuccess { _roomDetails.value = it }
                .onFailure { _detailError.value = it.localizedMessage }

            occupationDeferred.await()
                .onSuccess { _occupation.value = it }
                .onFailure { _detailError.value = it.localizedMessage }

            _isLoadingDetail.value = false
        }
    }

    private fun clearBuildingDetail() {
        _buildingRooms.value = emptyList()
        _todayEvents.value = emptyList()
        _roomDetails.value = emptyList()
        _occupation.value = emptyList()
    }
}
