package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.CampusDao
import it.attendance100.mybicocca.data.datasource.campus.EasyStaffCampusDataSource
import it.attendance100.mybicocca.data.datasource.campus.EasyStaffEventsDataSource
import it.attendance100.mybicocca.data.model.campus.Building
import it.attendance100.mybicocca.data.model.campus.CampusEvent
import it.attendance100.mybicocca.data.model.campus.CampusEventType
import it.attendance100.mybicocca.data.model.campus.Room
import it.attendance100.mybicocca.data.model.campus.RoomDetails
import it.attendance100.mybicocca.data.model.campus.RoomOccupationEvent
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CampusRepository @Inject constructor(
    private val campusDataSource: EasyStaffCampusDataSource,
    private val eventsDataSource: EasyStaffEventsDataSource,
    private val dao: CampusDao,
) {
    fun observeBuildings(): Flow<List<Building>> = dao.observeBuildings()

    fun observeRoomsByBuilding(buildingCode: String): Flow<List<Room>> =
        dao.observeRoomsByBuilding(buildingCode)

    fun observeAllRooms(): Flow<List<Room>> = dao.observeAllRooms()

    suspend fun refreshBuildings(): Result<Unit> = runCatching {
        val buildings = campusDataSource.getBuildings()
        dao.deleteAllBuildings()
        dao.upsertBuildings(buildings)
    }

    suspend fun refreshRooms(buildingCode: String): Result<Unit> = runCatching {
        val rooms = campusDataSource.getRooms(buildingCode)
        dao.deleteRoomsByBuilding(buildingCode)
        dao.upsertRooms(rooms)
    }

    suspend fun getRoomDetails(buildingCode: String): Result<List<RoomDetails>> = runCatching {
        campusDataSource.getRoomDetails(buildingCode)
    }

    suspend fun getBuildingOccupation(
        buildingCode: String,
        date: LocalDate = LocalDate.now(),
    ): Result<List<RoomOccupationEvent>> = runCatching {
        eventsDataSource.getBuildingOccupation(buildingCode, date)
    }

    suspend fun getTodayEvents(buildingCode: String): Result<List<CampusEvent>> = runCatching {
        eventsDataSource.getTodayEvents(buildingCode)
    }

    suspend fun getEventTypes(): Result<List<CampusEventType>> = runCatching {
        eventsDataSource.getEventTypes()
    }
}
