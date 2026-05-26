package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    fun observeBuildings(): Flow<Loadable<List<MapBuilding>>>

    fun observeRooms(buildingCode: BuildingCode): Flow<Loadable<List<MapRoom>>>

    // Seeds the bundled campus geometry into the local store. Throws on failure.
    suspend fun refreshBuildings()

    // Pulls the building's room list from EasyStaff into the local store. Throws on failure.
    suspend fun refreshRooms(buildingCode: BuildingCode)

    // On-demand rich detail for a single room (floor, accessibility, 360° view). Throws on failure.
    suspend fun loadRoomDetail(room: MapRoom): MapRoomDetail?
}
