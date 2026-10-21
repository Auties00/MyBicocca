package it.attendance100.mybicocca.data.mapper.map

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffBookingStatus
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffEventTeacher
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoom
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomShowcaseBuilding
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomShowcaseEquipment
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomShowcaseRoom
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomOccupationEvent
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomEquipment
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
    fun `room details list the available equipment as italian labels`() {
        val detail = showcaseRoom(
            fixedEquipment = listOf(
                EasyStaffRoomShowcaseEquipment(EasyStaffRoomEquipment.TeacherDesk, status = "0"),
                EasyStaffRoomShowcaseEquipment(EasyStaffRoomEquipment.Podium, status = "1"),
            ),
            mobileEquipment = listOf(
                EasyStaffRoomShowcaseEquipment(EasyStaffRoomEquipment.WifiNetwork, status = "0"),
                EasyStaffRoomShowcaseEquipment(EasyStaffRoomEquipment.Unknown("Lavagna interattiva"), status = "0"),
            ),
        ).toDomain(building())
        assertThat(detail.equipment).containsExactly(
            "Cattedra",
            "Rete WiFi",
            "Lavagna interattiva",
        ).inOrder()
    }

    @Test
    fun `room details carry the showcase scalar fields`() {
        val detail = showcaseRoom().toDomain(building())
        assertThat(detail.floor).isEqualTo(1)
        assertThat(detail.capacity).isEqualTo(120)
        assertThat(detail.isAccessible).isTrue()
        assertThat(detail.roomType).isEqualTo("Aula Magna")
        assertThat(detail.address).isEqualTo("Via")
        assertThat(detail.isInclusionValidated).isFalse()
    }

    @Test
    fun `room details leave unknown values unset`() {
        val detail = showcaseRoom(capacity = 0, typeName = "non definito", description = "Dato mancante", floor = null)
            .toDomain(building())
        assertThat(detail.capacity).isNull()
        assertThat(detail.roomType).isNull()
        assertThat(detail.description).isNull()
        assertThat(detail.floor).isNull()
    }

    @Test
    fun `room details render free-form html as lines`() {
        val detail = showcaseRoom(description = "Posti: 178<br>Coeff. 50%<ul><li>Podio</li><li> Wifi </li></ul>")
            .toDomain(building())
        assertThat(detail.description).isEqualTo("Posti: 178\nCoeff. 50%\n• Podio\n• Wifi")
    }

    @Test
    fun `floors are read in every spelling the staff uses`() {
        assertThat(listOf("  T", "PT", "piano terra", "Rialzato", "1", "Piano -1", "piano 2", "").map(::parseRoomFloor))
            .containsExactly(0, 0, 0, 0, 1, -1, 2, null).inOrder()
    }

    @Test
    fun `showcase rooms span every building`() {
        val rooms = mapOf(
            "7" to building(rooms = listOf(showcaseRoom(code = "U6-01"), showcaseRoom(code = "U6-02"))),
            "9" to building(rooms = listOf(showcaseRoom(code = "U14-01"))),
        ).showcaseRooms()
        assertThat(rooms.map { (_, room) -> room.code }).containsExactly("U6-01", "U6-02", "U14-01").inOrder()
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
        status = EasyStaffBookingStatus.Confirmed,
        eventType = eventType,
        isUniversityEvent = false,
        teachers = teachers,
    )

    private fun building(rooms: List<EasyStaffRoomShowcaseRoom> = emptyList()) = EasyStaffRoomShowcaseBuilding(
        name = "Sede Centrale",
        address = " Via ",
        rooms = rooms,
    )

    private fun showcaseRoom(
        code: String = "U6-22",
        capacity: Int? = 120,
        typeName: String? = "Aula Magna",
        description: String? = "Aula",
        floor: String? = "1",
        fixedEquipment: List<EasyStaffRoomShowcaseEquipment> = emptyList(),
        mobileEquipment: List<EasyStaffRoomShowcaseEquipment> = emptyList(),
    ) = EasyStaffRoomShowcaseRoom(
        code = code,
        name = "$code con Podio",
        description = description,
        capacity = capacity,
        typeName = typeName,
        floor = floor,
        isAccessible = true,
        isInclusionValidated = false,
        fixedEquipment = fixedEquipment,
        mobileEquipment = mobileEquipment,
    )
}
