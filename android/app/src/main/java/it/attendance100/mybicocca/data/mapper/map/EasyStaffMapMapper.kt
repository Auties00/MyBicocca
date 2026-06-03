package it.attendance100.mybicocca.data.mapper.map

import it.attendance100.mybicocca.data.remote.easystaff.dto.Esse3RoomEquipment
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoom
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomDetails
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomOccupationEvent
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import it.attendance100.mybicocca.domain.model.map.RoomCode
import it.attendance100.mybicocca.domain.model.map.RoomScheduleEntry

internal fun EasyStaffRoom.toDomain(buildingCode: BuildingCode, floor: Int?): MapRoom = MapRoom(
    code = RoomCode(code),
    buildingCode = buildingCode,
    name = name,
    capacity = capacity,
    floor = floor,
)

internal fun EasyStaffRoomOccupationEvent.toDomain(): RoomScheduleEntry = RoomScheduleEntry(
    roomCode = RoomCode(roomCode),
    title = title,
    kind = eventType.takeIf { it.isNotBlank() },
    teacher = teachersList.firstOrNull()
        ?.let { "${it.name} ${it.surname}".trim() }
        ?.takeIf { it.isNotBlank() },
    start = startDateTime,
    end = endDateTime,
)

internal fun EasyStaffRoomDetails.toDomain(): MapRoomDetail = MapRoomDetail(
    floor = floor,
    isAccessible = isAccessible,
    roomType = roomType,
    interactive360Url = interactive360Link,
    address = address,
    description = description,
    equipment = equipment.map { it.label() },
)

// User-facing equipment labels stay in Italian (mirrors the source labels EasyStaff parses).
private fun Esse3RoomEquipment.label(): String = when (this) {
    Esse3RoomEquipment.FixedEquipment -> "Attrezzature fisse"
    Esse3RoomEquipment.MobileEquipment -> "Attrezzature mobili"
    Esse3RoomEquipment.TeacherDesk -> "Cattedra"
    Esse3RoomEquipment.BlackoutWindows -> "Finestre oscurabili"
    Esse3RoomEquipment.TieredSeating -> "Gradoni"
    Esse3RoomEquipment.AudioSystem -> "Impianto audio"
    Esse3RoomEquipment.StandardVideoLessonKit -> "Kit aule per videolezioni"
    Esse3RoomEquipment.FlipChart -> "Lavagna a fogli"
    Esse3RoomEquipment.Whiteboard -> "Lavagna a pennarelli"
    Esse3RoomEquipment.SlateBlackboard -> "Lavagna in ardesia"
    Esse3RoomEquipment.OverheadProjector -> "Lavagna luminosa"
    Esse3RoomEquipment.SlidingBlackboard -> "Lavagna saliscendi"
    Esse3RoomEquipment.ExaminationBeds -> "Lettini"
    Esse3RoomEquipment.LcdPlasmaMonitor -> "Monitor TV LCD/Plasma"
    Esse3RoomEquipment.TeacherPc -> "PC cattedra"
    Esse3RoomEquipment.Podium -> "Podio"
    Esse3RoomEquipment.StudentPowerSocket -> "Presa elettrica postazione studente"
    Esse3RoomEquipment.RecordingSystem -> "Registrazione"
    Esse3RoomEquipment.WiredNetwork -> "Rete fissa"
    Esse3RoomEquipment.WifiNetwork -> "Rete WiFi"
    Esse3RoomEquipment.FixedChairsWithDesks -> "Sedie fisse con banco"
    Esse3RoomEquipment.FixedChairsWithTabletArms -> "Sedie fisse con ribaltina"
    Esse3RoomEquipment.MobileChairsWithTabletArms -> "Sedie mobili con ribaltina"
    Esse3RoomEquipment.MobileChairsWithoutTabletArms -> "Sedie mobili senza ribaltina"
    Esse3RoomEquipment.AccessibleTable -> "Tavolo accessibile"
    Esse3RoomEquipment.Camera -> "Telecamera"
    Esse3RoomEquipment.VideoConference -> "Video conference"
    Esse3RoomEquipment.CeilingProjectorAndScreen -> "Videoproiettore a soffitto + telo"
    Esse3RoomEquipment.WebConference -> "Webconference"
    is Esse3RoomEquipment.Other -> name
}
