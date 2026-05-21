package it.attendance100.mybicocca.data.remote.easystaff.api

import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomDetails
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomOccupationEvent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EasyStaffBuildingsApiTest : EasyStaffTestBase() {
    companion object {
        private val MOCK_DATE = LocalDate.now()
    }

    @Test
    suspend fun getBuildings() {
        val buildings = api.buildings.getBuildings()
        assertNotNull(buildings)
        assertTrue(buildings.isNotEmpty(), "Buildings list should not be empty")

        // Verify each building has valid properties
        buildings.forEach { building ->
            assertTrue(building.code.isNotBlank(), "Building code should not be blank")
            assertTrue(building.name.isNotBlank(), "Building name should not be blank")
        }

        // Verify building codes are unique
        val buildingCodes = buildings.map { it.code }
        assertEquals(buildingCodes.size, buildingCodes.toSet().size, "Building codes should be unique")
    }

    @Test
    suspend fun getRoomsInBuilding() {
        val buildings = api.buildings.getBuildings()
        assertTrue(buildings.isNotEmpty(), "Buildings list should not be empty")

        val building = buildings.first()
        val rooms = api.buildings.getRooms(building)
        assertNotNull(rooms)
        assertTrue(rooms.isNotEmpty(), "Rooms list should not be empty for building ${building.code}")

        // Verify each room has valid properties
        rooms.forEach { room ->
            assertTrue(room.code.isNotBlank(), "Room code should not be blank")
            assertTrue(room.name.isNotBlank(), "Room name should not be blank")

            // Verify capacity is present and reasonable (most rooms have capacity)
            if (room.capacity != null) {
                assertTrue(room.capacity > 0, "Room capacity should be positive, got ${room.capacity}")
                assertTrue(room.capacity < 2000, "Room capacity should be reasonable, got ${room.capacity}")
            }
        }

        // Verify room codes are unique within a building
        val roomCodes = rooms.map { it.code }
        assertEquals(roomCodes.size, roomCodes.toSet().size, "Room codes should be unique within a building")
    }

    @Test
    suspend fun getRoomsInBuildingHasCapacity() {
        val buildings = api.buildings.getBuildings()
        assertTrue(buildings.isNotEmpty(), "Buildings list should not be empty")

        val building = buildings.first()
        val rooms = api.buildings.getRooms(building)
        assertTrue(rooms.isNotEmpty(), "Rooms list should not be empty")

        // Verify that at least some rooms have capacity data
        val roomsWithCapacity = rooms.filter { it.capacity != null }
        assertTrue(
            roomsWithCapacity.isNotEmpty(),
            "At least some rooms should have capacity data"
        )
    }

    @Test
    suspend fun getBuildingOccupation() {
        val buildings = api.buildings.getBuildings()
        assertTrue(buildings.isNotEmpty(), "Buildings list should not be empty")

        val building = buildings.first()
        val events = api.buildings.getBuildingOccupation(
            building = building,
            date = MOCK_DATE
        )

        assertNotNull(events)

        // Verify each event in the building occupation
        events.forEach { event ->
            validateEvent(event)
        }
    }

    @Test
    suspend fun getRoomDetailsByBuildings() {
        val buildings = api.buildings.getBuildings()
        assertNotNull(buildings)
        assertTrue(buildings.isNotEmpty(), "Buildings list should not be empty")

        val roomsDetails = api.buildings.getRoomDetails(buildings)
        assertNotNull(roomsDetails)
        assertTrue(roomsDetails.isNotEmpty(), "Rooms details list should not be empty")

        // Verify room showcase details if any rooms are returned
        roomsDetails.forEach { roomDetails ->
            validateRoomDetails(roomDetails)
        }
    }

    @Test
    suspend fun getRoomDetailsByRooms() {
        val buildings = api.buildings.getBuildings()
        assertNotNull(buildings)
        assertTrue(buildings.isNotEmpty(), "Buildings list should not be empty")
        val building = buildings.first()

        val rooms = api.buildings.getRooms(building)
        assertNotNull(rooms)
        assertTrue(rooms.isNotEmpty(), "Rooms list should not be empty")

        val roomsDetails = api.buildings.getRoomDetails(listOf(building))
        assertNotNull(roomsDetails)
        assertTrue(roomsDetails.isNotEmpty(), "Rooms details list should not be empty")

        // Verify room showcase details if any rooms are returned
        roomsDetails.forEach { roomDetails ->
            validateRoomDetails(roomDetails)
        }
    }

    private fun validateRoomDetails(roomDetails: EasyStaffRoomDetails) {
        assertTrue(roomDetails.name.isNotBlank(), "Room name should not be blank")

        // Verify capacity is reasonable if present
        roomDetails.capacity?.let { capacity ->
            assertTrue(capacity > 0, "Capacity should be positive")
            assertTrue(capacity < 2000, "Capacity should be reasonable")
        }
    }

    private fun validateEvent(event: EasyStaffRoomOccupationEvent) {
        // Verify basic event properties
        assertTrue(event.id.isNotBlank(), "Event ID should not be blank")
        assertTrue(event.title.isNotBlank(), "Event title should not be blank")

        // Verify date and time
        assertNotNull(event.date, "Event date should not be null")
        assertNotNull(event.startDateTime, "Event start time should not be null")
        assertNotNull(event.endDateTime, "Event end time should not be null")
        assertTrue(
            event.startDateTime.isBefore(event.endDateTime),
            "Event start time should be before end time"
        )

        // Verify room information
        assertTrue(event.roomName.isNotBlank(), "Room name should not be blank")
        assertTrue(event.roomCode.isNotBlank(), "Room code should not be blank")

        // Verify building information
        assertTrue(event.buildingName.isNotBlank(), "Building name should not be blank")
        assertTrue(event.buildingCode.isNotBlank(), "Building code should not be blank")

        // Verify event type (string label in room occupation events)
        assertTrue(event.eventType.isNotBlank(), "Event type should not be blank")
    }
}
