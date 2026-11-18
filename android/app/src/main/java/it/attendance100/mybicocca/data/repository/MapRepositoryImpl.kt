package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.local.map.BuildingCatalogSource
import it.attendance100.mybicocca.data.local.map.MapBuildingDao
import it.attendance100.mybicocca.data.local.map.MapRoomDao
import it.attendance100.mybicocca.data.local.map.MapRoomSyncStateDao
import it.attendance100.mybicocca.data.local.map.MapRoomSyncStateEntity
import it.attendance100.mybicocca.data.mapper.map.parseRoomFloor
import it.attendance100.mybicocca.data.mapper.map.showcaseRooms
import it.attendance100.mybicocca.data.mapper.map.toDomain
import it.attendance100.mybicocca.data.mapper.map.toEntity
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffBookingStatus
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import it.attendance100.mybicocca.domain.model.map.RoomScheduleEntry
import it.attendance100.mybicocca.domain.repository.MapRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Map repository over the bundled building catalog (Room-seeded) and EasyStaff (rooms synced to
 * Room per building; room detail and day schedules fetched live).
 *
 * The EasyStaff client parses responses on the caller's dispatcher (Jsoup over a multi-hundred-
 * KB showcase page, JSON decoding of a full day of events), and these calls run in
 * viewModelScope — the main thread, right while the detail modal animates in. Every remote call
 * therefore hops to Dispatchers.Default for the fetch+parse+map pipeline; network suspension
 * releases the worker, so only the parsing occupies it.
 */
@Singleton
class MapRepositoryImpl @Inject constructor(
    private val easyStaffApi: EasyStaffApi,
    private val buildingCatalog: BuildingCatalogSource,
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

    override fun observeAllRooms(): Flow<List<MapRoom>> =
        roomDao.observeAll()
            .map { rows -> rows.map { it.toDomain() } }
            .flowOn(Dispatchers.Default)

    /**
     * Buildings come entirely from the bundled catalog — no remote call. The catalog is
     * re-seeded on every launch so shipped edits to buildings.json take effect, and the write is
     * guarded so a read failure never wipes an already-populated table.
     */
    override suspend fun refreshBuildings() {
        val catalog = runCatching { buildingCatalog.load() }.getOrDefault(emptyList())
        if (catalog.isNotEmpty()) buildingDao.upsertAll(catalog.map { it.toEntity() })
    }

    /**
     * Replaces the building's cached rooms from EasyStaff, skipping the fetch while the cached
     * copy is fresh per the stale policy. One bulk showcase call resolves every room's floor up
     * front — each showcase room carries its own code, so the mapping is unambiguous — and runs
     * concurrently with the independent rooms call; floor resolution is best-effort, so a parse
     * failure costs floors only.
     */
    override suspend fun refreshRooms(buildingCode: BuildingCode) {
        if (!isRoomsStale(buildingCode)) return
        val rooms = withContext(Dispatchers.Default) {
            val floorByRoomCodeDeferred = async {
                runCatching {
                    easyStaffApi.buildings.getRoomDetails(buildings = listOf(buildingCode.value))
                        .showcaseRooms()
                        .mapNotNull { (_, room) -> room.code?.trim()?.let { it to room.floor?.let(::parseRoomFloor) } }
                        .toMap()
                }.getOrDefault(emptyMap())
            }
            val remoteRooms = easyStaffApi.buildings.getRooms()[buildingCode.value].orEmpty()
            val floorByRoomCode = floorByRoomCodeDeferred.await()
            remoteRooms
                .filter { it.code != ALL_ROOMS }
                .map { it.toDomain(buildingCode, floorByRoomCode[it.code]) }
        }
        roomDao.replaceForBuilding(buildingCode.value, rooms.map { it.toEntity() })
        syncStateDao.upsertState(MapRoomSyncStateEntity(buildingCode.value, now()))
    }

    /** Queries the showcase for this room alone. */
    override suspend fun loadRoomDetail(room: MapRoom): MapRoomDetail? {
        return withContext(Dispatchers.Default) {
            easyStaffApi.buildings.getRoomDetails(buildings = listOf(room.buildingCode.value), rooms = listOf(room.code.value))
                .showcaseRooms()
                .firstOrNull()
                ?.let { (building, showcaseRoom) -> showcaseRoom.toDomain(building) }
        }
    }

    override suspend fun loadDaySchedule(buildingCode: BuildingCode, date: LocalDate): List<RoomScheduleEntry> {
        return withContext(Dispatchers.Default) {
            easyStaffApi.buildings.getBuildingOccupation(buildings = listOf(buildingCode.value), date = date)
                .filter { it.status == EasyStaffBookingStatus.Confirmed }
                .map { it.toDomain() }
        }
    }

    private suspend fun isRoomsStale(buildingCode: BuildingCode): Boolean {
        val state = syncStateDao.getState(buildingCode.value) ?: return true
        return now() - state.lastRefreshedAtMs > stalePolicy.ttlFor(MAP_ROOMS_SOURCE)
    }

    private fun now(): Long = System.currentTimeMillis()

    private companion object {
        const val MAP_ROOMS_SOURCE = "map_rooms"

        /** The code of the "every room" entry the room listing starts with. */
        const val ALL_ROOMS = "all"
    }
}
