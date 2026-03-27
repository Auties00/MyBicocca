package it.attendance100.mybicocca.data.datasource.campus

import it.attendance100.mybicocca.data.api.easystaff.EasyStaffApi
import it.attendance100.mybicocca.data.model.campus.Building
import it.attendance100.mybicocca.data.model.campus.Room
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EasyStaffCampusDataSource @Inject constructor(
    private val easyStaffApi: EasyStaffApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getBuildings(): List<Building> = withContext(ioDispatcher) {
        easyStaffApi.buildings.getBuildings()
            .filter { it.code != "all" && it.code != "senza_aula" }
            .map { building ->
                Building(
                    code = building.code,
                    name = building.name,
                )
            }
    }

    suspend fun getRooms(buildingCode: String): List<Room> = withContext(ioDispatcher) {
        val buildings = easyStaffApi.buildings.getBuildings()
        val building = buildings.firstOrNull { it.code == buildingCode } ?: return@withContext emptyList()

        easyStaffApi.buildings.getRooms(building)
            .filter { it.code != "all" }
            .map { room ->
                Room(
                    code = room.code,
                    buildingCode = buildingCode,
                    name = room.name,
                    capacity = room.capacity,
                )
            }
    }
}
