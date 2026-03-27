package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.CampusDao
import it.attendance100.mybicocca.data.datasource.campus.EasyStaffCampusDataSource
import it.attendance100.mybicocca.data.model.campus.Building
import it.attendance100.mybicocca.data.model.campus.Room
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CampusRepository @Inject constructor(
    private val dataSource: EasyStaffCampusDataSource,
    private val dao: CampusDao,
) {
    fun observeBuildings(): Flow<List<Building>> = dao.observeBuildings()

    fun observeRoomsByBuilding(buildingCode: String): Flow<List<Room>> =
        dao.observeRoomsByBuilding(buildingCode)

    fun observeAllRooms(): Flow<List<Room>> = dao.observeAllRooms()

    suspend fun refreshBuildings(): Result<Unit> = runCatching {
        val buildings = dataSource.getBuildings()
        dao.deleteAllBuildings()
        dao.upsertBuildings(buildings)
    }

    suspend fun refreshRooms(buildingCode: String): Result<Unit> = runCatching {
        val rooms = dataSource.getRooms(buildingCode)
        dao.deleteRoomsByBuilding(buildingCode)
        dao.upsertRooms(rooms)
    }
}
