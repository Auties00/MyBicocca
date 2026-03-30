package it.attendance100.mybicocca.data.datasource.campus

import it.attendance100.mybicocca.data.api.easystaff.EasyStaffApi
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffBookingStatus
import it.attendance100.mybicocca.data.model.campus.CampusEvent
import it.attendance100.mybicocca.data.model.campus.CampusEventType
import it.attendance100.mybicocca.data.model.campus.RoomOccupationEvent
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EasyStaffEventsDataSource @Inject constructor(
    private val easyStaffApi: EasyStaffApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getTodayEvents(buildingCode: String): List<CampusEvent> =
        withContext(ioDispatcher) {
            val buildings = easyStaffApi.buildings.getBuildings()
            val building = buildings.firstOrNull { it.code == buildingCode }
                ?: return@withContext emptyList()

            easyStaffApi.events.getTodayEvents(buildings = listOf(building))
                .filter { it.status == EasyStaffBookingStatus.CONFIRMED }
                .map { event ->
                    CampusEvent(
                        id = event.id,
                        title = event.title,
                        date = event.date,
                        startDateTime = event.startDateTime,
                        endDateTime = event.endDateTime,
                        roomName = event.roomName,
                        eventType = event.eventType,
                        isPast = event.isPast,
                        teachers = event.teachersList.map { "${it.name} ${it.surname}" },
                    )
                }
        }

    suspend fun getBuildingOccupation(
        buildingCode: String,
        date: LocalDate = LocalDate.now(),
    ): List<RoomOccupationEvent> = withContext(ioDispatcher) {
        val buildings = easyStaffApi.buildings.getBuildings()
        val building = buildings.firstOrNull { it.code == buildingCode }
            ?: return@withContext emptyList()

        easyStaffApi.buildings.getBuildingOccupation(building, date)
            .filter { it.status == EasyStaffBookingStatus.CONFIRMED }
            .map { event ->
                RoomOccupationEvent(
                    id = event.id,
                    title = event.title,
                    startDateTime = event.startDateTime,
                    endDateTime = event.endDateTime,
                    roomName = event.roomName,
                    roomCode = event.roomCode,
                    buildingName = event.buildingName,
                    buildingCode = event.buildingCode,
                    eventType = event.eventType,
                    teachers = event.teachersList.map { "${it.name} ${it.surname}" },
                )
            }
    }

    suspend fun getEventTypes(): List<CampusEventType> = withContext(ioDispatcher) {
        easyStaffApi.events.getEventTypes()
            .map { CampusEventType(code = it.code, name = it.name) }
    }
}
