package it.attendance100.mybicocca.ui.screen.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.map
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.map.BuildingCategory
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import it.attendance100.mybicocca.domain.usecase.map.LoadRoomDetailUseCase
import it.attendance100.mybicocca.domain.usecase.map.ObserveBuildingsUseCase
import it.attendance100.mybicocca.domain.usecase.map.ObserveRoomsUseCase
import it.attendance100.mybicocca.domain.usecase.map.RefreshBuildingsUseCase
import it.attendance100.mybicocca.domain.usecase.map.RefreshRoomsUseCase
import it.attendance100.mybicocca.ui.screen.map.state.MapOneShotEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MapViewModel @Inject constructor(
    observeBuildings: ObserveBuildingsUseCase,
    private val observeRooms: ObserveRoomsUseCase,
    private val refreshBuildings: RefreshBuildingsUseCase,
    private val refreshRooms: RefreshRoomsUseCase,
    private val loadRoomDetail: LoadRoomDetailUseCase,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _categoryFilter = MutableStateFlow<Set<BuildingCategory>>(emptySet())
    val categoryFilter: StateFlow<Set<BuildingCategory>> = _categoryFilter.asStateFlow()

    private val allBuildings: StateFlow<Loadable<List<MapBuilding>>> =
        observeBuildings()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    // Pins to render: the full set narrowed by the search box and the category filter.
    val buildings: StateFlow<Loadable<List<MapBuilding>>> =
        combine(allBuildings, _searchQuery, _categoryFilter) { loadable, query, categories ->
            loadable.map { list -> list.filter { it.matches(query, categories) } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    private val _selectedBuildingCode = MutableStateFlow<BuildingCode?>(null)

    val selectedBuilding: StateFlow<MapBuilding?> =
        combine(allBuildings, _selectedBuildingCode) { loadable, code ->
            code?.let { c -> loadable.valueOrNull()?.firstOrNull { it.code == c } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), null)

    val rooms: StateFlow<Loadable<List<MapRoom>>> =
        _selectedBuildingCode.flatMapLatest { code ->
            if (code == null) flowOf(Loadable.Loaded(emptyList())) else observeRooms(code)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    private val _selectedRoom = MutableStateFlow<MapRoom?>(null)
    val selectedRoom: StateFlow<MapRoom?> = _selectedRoom.asStateFlow()

    // Rich, per-room detail fetched on demand when a room is opened (not cached).
    val roomDetail: StateFlow<Loadable<MapRoomDetail?>> =
        _selectedRoom.flatMapLatest { room ->
            if (room == null) {
                flowOf(Loadable.Loaded(null) as Loadable<MapRoomDetail?>)
            } else {
                flow {
                    emit(Loadable.NotYetLoaded)
                    emit(Loadable.Loaded(runCatching { loadRoomDetail(room) }.getOrNull()))
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.Loaded(null))

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val oneShotChannel = Channel<MapOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<MapOneShotEvent> = oneShotChannel.receiveAsFlow()

    init {
        viewModelScope.launch { runCatching { refreshBuildings() } }
    }

    fun setSearch(query: String) {
        _searchQuery.value = query
    }

    fun toggleCategory(category: BuildingCategory) {
        _categoryFilter.update { if (category in it) it - category else it + category }
    }

    fun clearCategories() {
        _categoryFilter.value = emptySet()
    }

    fun selectBuilding(code: BuildingCode) {
        _selectedRoom.value = null
        _selectedBuildingCode.value = code
        refreshRoomsFor(code)
    }

    fun clearSelection() {
        _selectedBuildingCode.value = null
        _selectedRoom.value = null
    }

    fun selectRoom(room: MapRoom) {
        _selectedRoom.value = if (_selectedRoom.value == room) null else room
    }

    fun retryRooms() {
        _selectedBuildingCode.value?.let(::refreshRoomsFor)
    }

    private fun refreshRoomsFor(code: BuildingCode) {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { refreshRooms(code) }
                .onSuccess { _syncStatus.value = SyncStatus.Idle }
                .onFailure {
                    _syncStatus.value = SyncStatus.Failed(it)
                    oneShotChannel.trySend(MapOneShotEvent.RefreshFailed(it))
                }
        }
    }

    fun reportLocationPermissionDenied() {
        oneShotChannel.trySend(MapOneShotEvent.LocationPermissionDenied)
    }

    private companion object {
        const val STATE_KEEP_ALIVE_MS = 5_000L
    }
}

private fun MapBuilding.matches(query: String, categories: Set<BuildingCategory>): Boolean {
    if (categories.isNotEmpty() && category !in categories) return false
    if (query.isBlank()) return true
    val needle = query.trim().lowercase()
    return name.lowercase().contains(needle) ||
        code.value.lowercase().contains(needle) ||
        (address?.lowercase()?.contains(needle) == true)
}
