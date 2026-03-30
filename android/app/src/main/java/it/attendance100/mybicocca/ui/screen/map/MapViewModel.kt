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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val campusRepository: CampusRepository,
) : ViewModel() {

    private companion object { const val STALE_THRESHOLD_MS = 5 * 60 * 1000L }

    val buildings: StateFlow<List<Building>> = campusRepository.observeBuildings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var lastRefreshMillis = 0L

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            campusRepository.refreshBuildings()
                .onFailure { _error.value = it.localizedMessage }
            lastRefreshMillis = System.currentTimeMillis()
            _isRefreshing.value = false
        }
    }

    fun refreshIfStale() {
        if (System.currentTimeMillis() - lastRefreshMillis > STALE_THRESHOLD_MS) {
            refresh()
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
            _error.value = null

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
                .onFailure { _error.value = it.localizedMessage }

            detailsDeferred.await()
                .onSuccess { _roomDetails.value = it }
                .onFailure { _error.value = it.localizedMessage }

            occupationDeferred.await()
                .onSuccess { _occupation.value = it }
                .onFailure { _error.value = it.localizedMessage }

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
