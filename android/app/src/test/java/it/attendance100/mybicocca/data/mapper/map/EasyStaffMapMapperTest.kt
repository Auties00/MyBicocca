package it.attendance100.mybicocca.data.mapper.map

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffBookingStatus
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffEventTeacher
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoom
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomDetails
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomOccupationEvent
import it.attendance100.mybicocca.data.remote.easystaff.dto.Esse3RoomEquipment
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.RoomCode
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Covers the EasyStaff room/occupation/showcase mapping: the floor is injected separately into
 * a room listing, an occupation event collapses to its first teacher's full name and nulls a
 * blank event type, and showcase equipment renders as the Italian user-facing labels.
 */
class EasyStaffMapMapperTest {

    @Test
    fun `room listing entry takes the supplied building code and floor`() {
        val room = EasyStaffRoom(code = "U6-22", name = "U6-22", capacity = 80)
            .toDomain(BuildingCode("U06"), floor = 2)
        assertThat(room.code).isEqualTo(RoomCode("U6-22"))
        assertThat(room.buildingCode).isEqualTo(BuildingCode("U06"))
        assertThat(room.capacity).isEqualTo(80)
        assertThat(room.floor).isEqualTo(2)
    }

    @Test
    fun `room listing entry accepts a null floor`() {
        val room = EasyStaffRoom(code = "U6-22", name = "U6-22").toDomain(BuildingCode("U06"), floor = null)
        assertThat(room.floor).isNull()
        assertThat(room.capacity).isNull()
    }

    @Test
    fun `occupation event collapses to the first teacher full name`() {
        val entry = occupationEvent(
            teachers = listOf(
                EasyStaffEventTeacher(name = "Mario", surname = "Rossi", email = null, code = null),
                EasyStaffEventTeacher(name = "Anna", surname = "Verdi", email = null, code = null),
            ),
        ).toDomain()
        assertThat(entry.teacher).isEqualTo("Mario Rossi")
    }

    @Test
    fun `occupation event has no teacher when the list is empty`() {
        assertThat(occupationEvent(teachers = emptyList()).toDomain().teacher).isNull()
    }

    @Test
    fun `occupation event nulls a blank event type`() {
        assertThat(occupationEvent(eventType = "").toDomain().kind).isNull()
        assertThat(occupationEvent(eventType = "Lezione").toDomain().kind).isEqualTo("Lezione")
    }

    @Test
    fun `occupation event carries title room code and the wall-clock times`() {
        val entry = occupationEvent().toDomain()
        assertThat(entry.title).isEqualTo("Analisi 1")
        assertThat(entry.roomCode).isEqualTo(RoomCode("U6-22"))
        assertThat(entry.start).isEqualTo(LocalDateTime.of(2024, 6, 15, 9, 0))
        assertThat(entry.end).isEqualTo(LocalDateTime.of(2024, 6, 15, 11, 0))
    }

    @Test
    fun `room details map equipment to italian labels`() {
        val detail = roomDetails(
            equipment = listOf(
                Esse3RoomEquipment.TeacherDesk,
                Esse3RoomEquipment.WifiNetwork,
                Esse3RoomEquipment.Other("Lavagna interattiva"),
            ),
        ).toDomain()
        assertThat(detail.equipment).containsExactly(
            "Cattedra",
            "Rete WiFi",
            "Lavagna interattiva",
        ).inOrder()
    }

    @Test
    fun `room details carry the showcase scalar fields`() {
        val detail = roomDetails().toDomain()
        assertThat(detail.floor).isEqualTo(1)
        assertThat(detail.capacity).isEqualTo(120)
        assertThat(detail.isAccessible).isTrue()
        assertThat(detail.roomType).isEqualTo("Aula Magna")
        assertThat(detail.isInclusionValidated).isFalse()
    }

    private fun occupationEvent(
        eventType: String = "Lezione",
        teachers: List<EasyStaffEventTeacher> = emptyList(),
    ) = EasyStaffRoomOccupationEvent(
        id = "1",
        title = "Analisi 1",
        date = LocalDate.of(2024, 6, 15),
        startDateTime = LocalDateTime.of(2024, 6, 15, 9, 0),
        endDateTime = LocalDateTime.of(2024, 6, 15, 11, 0),
        roomName = "U6-22",
        roomCode = "U6-22",
        buildingName = "Edificio U6",
        buildingCode = "U06",
        facultyId = "1",
        status = EasyStaffBookingStatus.CONFIRMED,
        eventType = eventType,
        isUniversityEvent = false,
        teachersList = teachers,
    )

    private fun roomDetails(equipment: List<Esse3RoomEquipment> = emptyList()) = EasyStaffRoomDetails(
        roomCode = "U6-22",
        roomName = "U6-22 con Podio",
        name = "Sede Centrale",
        address = "Via",
        googleMapsLink = null,
        interactive360Link = null,
        description = "Aula",
        capacity = 120,
        roomType = "Aula Magna",
        floor = 1,
        isAccessible = true,
        accessibilityNotes = null,
        isInclusionValidated = false,
        equipment = equipment,
    )
}
