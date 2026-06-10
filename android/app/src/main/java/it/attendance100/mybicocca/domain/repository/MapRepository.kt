package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import it.attendance100.mybicocca.domain.model.map.RoomScheduleEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Campus map data for the map tab.
 *
 * Buildings and rooms are Room-backed: buildings are seeded from the static bundled catalog,
 * rooms are synced per building from EasyStaff. Room detail and day schedules are network-only —
 * volatile live EasyStaff data, never cached.
 */
interface MapRepository {
    /** Streams the cached campus buildings, hot from Room. */
    fun observeBuildings(): Flow<Loadable<List<MapBuilding>>>

    /** Streams a building's cached rooms, hot from Room. */
    fun observeRooms(buildingCode: BuildingCode): Flow<Loadable<List<MapRoom>>>

    /**
     * Streams every cached room across buildings, for the unified search index. Rooms hydrate
     * as buildings are visited, so coverage grows with use.
     */
    fun observeAllRooms(): Flow<List<MapRoom>>

    /**
     * Seeds the bundled campus geometry into the local store; a failed catalog read leaves
     * already-seeded rows untouched.
     */
    suspend fun refreshBuildings()

    /**
     * Pulls the building's room list from EasyStaff into the local store; the fetch is skipped
     * while the cached copy is still fresh. Throws on failure.
     */
    suspend fun refreshRooms(buildingCode: BuildingCode)

    /**
     * On-demand rich detail for a single room (floor, capacity, accessibility, equipment).
     * Returns `null` when EasyStaff has no showcase card for the room. Throws on failure.
     */
    suspend fun loadRoomDetail(room: MapRoom): MapRoomDetail?

    /**
     * The day's confirmed occupation slots for every room in the building — volatile live data
     * fetched when a building is opened, never cached. Throws on failure.
     */
    suspend fun loadDaySchedule(buildingCode: BuildingCode, date: LocalDate): List<RoomScheduleEntry>
}
