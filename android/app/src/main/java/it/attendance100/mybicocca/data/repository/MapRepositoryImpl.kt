package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.local.campus.CampusGeometrySource
import it.attendance100.mybicocca.data.local.map.MapBuildingDao
import it.attendance100.mybicocca.data.local.map.MapRoomDao
import it.attendance100.mybicocca.data.local.map.MapRoomSyncStateDao
import it.attendance100.mybicocca.data.local.map.MapRoomSyncStateEntity
import it.attendance100.mybicocca.data.mapper.map.toDomain
import it.attendance100.mybicocca.data.mapper.map.toEntity
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffBuilding
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoom
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import it.attendance100.mybicocca.domain.repository.MapRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapRepositoryImpl @Inject constructor(
    private val easyStaffApi: EasyStaffApi,
    private val campusGeometry: CampusGeometrySource,
    private val buildingDao: MapBuildingDao,
    private val roomDao: MapRoomDao,
    private val syncStateDao: MapRoomSyncStateDao,
    private val stalePolicy: StalePolicy,
) : MapRepository {

    override fun observeBuildings(): Flow<Loadable<List<MapBuilding>>> =
        buildingDao.observeAll()
            .map { rows -> rows.map { it.toDomain() } }
            .map<List<MapBuilding>, Loadable<List<MapBuilding>>> { Loadable.Loaded(it) }
            .flowOn(Dispatchers.Default)

    override fun observeRooms(buildingCode: BuildingCode): Flow<Loadable<List<MapRoom>>> =
        roomDao.observeForBuilding(buildingCode.value)
            .map { rows -> rows.map { it.toDomain() } }
            .map<List<MapRoom>, Loadable<List<MapRoom>>> { Loadable.Loaded(it) }
            .flowOn(Dispatchers.Default)

    override suspend fun refreshBuildings() {
        // The asset is static — seed once and leave it; nothing remote to re-pull.
        if (buildingDao.count() > 0) return
        buildingDao.upsertAll(campusGeometry.loadBuildings().map { it.toEntity() })
    }

    override suspend fun refreshRooms(buildingCode: BuildingCode) {
        if (!isRoomsStale(buildingCode)) return
        val building = EasyStaffBuilding(code = buildingCode.value, name = "")
        val rooms = easyStaffApi.buildings.getRooms(building)
            .filter { it.code != EasyStaffRoom.FILTER_ALLOW_ALL.code }
            .map { it.toDomain(buildingCode) }
        roomDao.replaceForBuilding(buildingCode.value, rooms.map { it.toEntity() })
        syncStateDao.upsertState(MapRoomSyncStateEntity(buildingCode.value, now()))
    }

    override suspend fun loadRoomDetail(room: MapRoom): MapRoomDetail? {
        val building = EasyStaffBuilding(code = room.buildingCode.value, name = "")
        val easyRoom = EasyStaffRoom(code = room.code.value, name = room.name)
        // Querying one room at a time makes the returned showcase card unambiguously this room's
        // — EasyStaff's room detail DTO carries no room identity of its own.
        return easyStaffApi.buildings.getRoomDetails(listOf(building), listOf(easyRoom))
            .firstOrNull()
            ?.toDomain()
    }

    private suspend fun isRoomsStale(buildingCode: BuildingCode): Boolean {
        val state = syncStateDao.getState(buildingCode.value) ?: return true
        return now() - state.lastRefreshedAtMs > stalePolicy.ttlFor(MAP_ROOMS_SOURCE)
    }

    private fun now(): Long = System.currentTimeMillis()

    private companion object {
        const val MAP_ROOMS_SOURCE = "map_rooms"
    }
}
