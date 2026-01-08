package it.attendance100.mybicocca.data.api.easystaff

import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffRoomOccupationQuery
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EasyStaffRoomsApiTest : EasyStaffTestBase() {
    companion object {
        private val MOCK_DATE = LocalDate.now()
    }

    @Test
    suspend fun getBuildings() {
        val buildings = api.rooms.getBuildings()
        assertNotNull(buildings)
        assertTrue(buildings.isNotEmpty())
    }

    @Test
    suspend fun getRoomsInBuilding() {
        val buildings = api.rooms.getBuildings()
        if (buildings.isEmpty()) return

        val building = buildings.first()
        val rooms = api.rooms.getRoomsInBuilding(building.code)
        assertNotNull(rooms)
    }

    @Test
    suspend fun getBuildingOccupation() {
        val buildings = api.rooms.getBuildings()
        if (buildings.isEmpty()) return

        val building = buildings.first()
        val query = EasyStaffRoomOccupationQuery(
            buildingCode = building.code,
            date = MOCK_DATE
        )

        val occupation = api.rooms.getBuildingOccupation(query)
        assertNotNull(occupation.building)
        assertNotNull(occupation.date)
        assertNotNull(occupation.rooms)
    }

    @Test
    suspend fun getRoomOccupation() {
        val buildings = api.rooms.getBuildings()
        if (buildings.isEmpty()) return

        val building = buildings.first()
        val rooms = api.rooms.getRoomsInBuilding(building.code)
        if (rooms.isEmpty()) return

        val room = rooms.first()
        val occupation = api.rooms.getRoomOccupation(
            buildingCode = building.code,
            roomCode = room.code,
            date = MOCK_DATE
        )
        assertNotNull(occupation.room)
        assertNotNull(occupation.building)
        assertNotNull(occupation.date)
        assertNotNull(occupation.timeSlots)
    }

    @Test
    suspend fun getRoomShowcase() {
        val results = api.rooms.getRoomShowcase()
        assertNotNull(results.rooms)
    }

    @Test
    suspend fun getRoomShowcaseByBuilding() {
        val buildings = api.rooms.getBuildings()
        if (buildings.isEmpty()) return

        val building = buildings.first()
        val results = api.rooms.getRoomShowcase(building.code)
        assertNotNull(results.rooms)
    }

    @Test
    suspend fun getRoomDetails() {
        val buildings = api.rooms.getBuildings()
        if (buildings.isEmpty()) return

        val building = buildings.first()
        val rooms = api.rooms.getRoomsInBuilding(building.code)
        if (rooms.isEmpty()) return

        val room = rooms.first()
        val details = api.rooms.getRoomDetails(building.code, room.code)
        if (details == null) return

        assertNotNull(details.code)
        assertNotNull(details.name)
        assertNotNull(details.building)
    }
}
