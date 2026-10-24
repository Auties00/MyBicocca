package it.attendance100.mybicocca.data.mapper.map

import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomEquipment
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoom
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomOccupationEvent
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomShowcaseBuilding
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffRoomShowcaseRoom
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import it.attendance100.mybicocca.domain.model.map.RoomCode
import it.attendance100.mybicocca.domain.model.map.RoomScheduleEntry
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

/**
 * Maps an EasyStaff room-listing entry to the domain; the floor is not part of the listing and
 * is supplied separately, resolved from the room showcase.
 */
internal fun EasyStaffRoom.toDomain(buildingCode: BuildingCode, floor: Int?): MapRoom = MapRoom(
    code = RoomCode(code),
    buildingCode = buildingCode,
    name = name,
    capacity = capacity,
    floor = floor,
)

/**
 * Maps an occupation-grid event to a schedule entry; the teacher collapses to the first listed
 * one's full name, and blank event types become null.
 */
internal fun EasyStaffRoomOccupationEvent.toDomain(): RoomScheduleEntry = RoomScheduleEntry(
    roomCode = RoomCode(roomCode),
    title = title,
    kind = eventType.takeIf { it.isNotBlank() },
    teacher = teachers.firstOrNull()
        ?.let { "${it.name} ${it.surname}".trim() }
        ?.takeIf { it.isNotBlank() },
    start = startDateTime,
    end = endDateTime,
)

/** The rooms of a room showcase, across its buildings. */
internal fun Map<String, EasyStaffRoomShowcaseBuilding>.showcaseRooms(): List<Pair<EasyStaffRoomShowcaseBuilding, EasyStaffRoomShowcaseRoom>> =
    values.flatMap { building -> building.rooms.map { building to it } }

/**
 * Maps a showcase room to the room detail. Text is as the staff typed it, so it's cleaned up
 * here: a capacity of 0 and the "non definito" category are unknown, free-form fields are
 * rendered as plain text, and only the available equipment is listed, as user-facing labels.
 */
internal fun EasyStaffRoomShowcaseRoom.toDomain(building: EasyStaffRoomShowcaseBuilding): MapRoomDetail = MapRoomDetail(
    floor = floor?.let(::parseRoomFloor),
    capacity = capacity?.takeIf { it > 0 },
    isAccessible = isAccessible,
    accessibilityNotes = accessibilityNotes?.let(::plainText),
    isInclusionValidated = isInclusionValidated,
    roomType = typeName?.cleanText()?.takeUnless { it.isEmpty() || it == "non definito" },
    address = building.address?.cleanText()?.takeIf { it.isNotEmpty() },
    description = description?.let(::plainText),
    equipment = (fixedEquipment + mobileEquipment)
        .filter { it.status == AVAILABLE_EQUIPMENT_STATUS }
        .map { it.name.label() },
)

/**
 * Parses the floor as typed by the staff: a number, optionally prefixed by "Piano" in any case
 * ("1", "Piano -1", "piano 2"), or a spelling of the ground floor ("T", "PT", "Terra",
 * "piano terra", and "Rialzato" for the raised ground floor).
 */
internal fun parseRoomFloor(text: String): Int? {
    val floor = text.trim().lowercase().removePrefix("piano").trim()
    return when (floor) {
        "t", "pt", "terra", "rialzato" -> 0
        else -> floor.toIntOrNull()
    }
}

/**
 * Renders a free-form field, which may hold HTML, as plain text that keeps the source line
 * structure: text keeps its embedded newlines, `<br>` becomes a newline and `<ul>` lists become
 * bulleted lines. Blank text and the "Dato mancante" placeholder are no text.
 */
private fun plainText(html: String): String? {
    val body = Jsoup.parseBodyFragment(html).body()
    val rendered = buildString {
        body.childNodes().forEach { node ->
            when {
                node is TextNode -> append(node.wholeText)
                node is Element && node.tagName() == "br" -> append('\n')
                node is Element && node.tagName() == "ul" -> node.select("li").forEach { item ->
                    append("\n• ")
                    append(item.text().cleanText())
                }
                node is Element -> append(node.text().cleanText())
            }
        }
    }
    return rendered.lines()
        .joinToString("\n") { it.trim() }
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
        .takeIf { it.isNotBlank() && !it.equals("Dato mancante", ignoreCase = true) }
}

/** The text without zero-width spaces, with non-breaking spaces made plain, trimmed. */
private fun String.cleanText(): String = replace("\u200b", "").replace('\u00a0', ' ').trim()

/** The status the showcase gives available equipment, which it renders as "DISPONIBILE". */
private const val AVAILABLE_EQUIPMENT_STATUS = "0"

/** User-facing equipment labels stay in Italian, mirroring the source labels EasyStaff parses. */
private fun EasyStaffRoomEquipment.label(): String = when (this) {
    EasyStaffRoomEquipment.FixedEquipment -> "Attrezzature fisse"
    EasyStaffRoomEquipment.MobileEquipment -> "Attrezzature mobili"
    EasyStaffRoomEquipment.TeacherDesk -> "Cattedra"
    EasyStaffRoomEquipment.BlackoutWindows -> "Finestre oscurabili"
    EasyStaffRoomEquipment.TieredSeating -> "Gradoni"
    EasyStaffRoomEquipment.AudioSystem -> "Impianto audio"
    EasyStaffRoomEquipment.StandardVideoLessonKit -> "Kit aule per videolezioni"
    EasyStaffRoomEquipment.FlipChart -> "Lavagna a fogli"
    EasyStaffRoomEquipment.Whiteboard -> "Lavagna a pennarelli"
    EasyStaffRoomEquipment.SlateBlackboard -> "Lavagna in ardesia"
    EasyStaffRoomEquipment.OverheadProjector -> "Lavagna luminosa"
    EasyStaffRoomEquipment.SlidingBlackboard -> "Lavagna saliscendi"
    EasyStaffRoomEquipment.ExaminationBeds -> "Lettini"
    EasyStaffRoomEquipment.LcdPlasmaMonitor -> "Monitor TV LCD/Plasma"
    EasyStaffRoomEquipment.TeacherPc -> "PC cattedra"
    EasyStaffRoomEquipment.Podium -> "Podio"
    EasyStaffRoomEquipment.StudentPowerSocket -> "Presa elettrica postazione studente"
    EasyStaffRoomEquipment.RecordingSystem -> "Registrazione"
    EasyStaffRoomEquipment.WiredNetwork -> "Rete fissa"
    EasyStaffRoomEquipment.WifiNetwork -> "Rete WiFi"
    EasyStaffRoomEquipment.FixedChairsWithDesks -> "Sedie fisse con banco"
    EasyStaffRoomEquipment.FixedChairsWithTabletArms -> "Sedie fisse con ribaltina"
    EasyStaffRoomEquipment.MobileChairsWithTabletArms -> "Sedie mobili con ribaltina"
    EasyStaffRoomEquipment.MobileChairsWithoutTabletArms -> "Sedie mobili senza ribaltina"
    EasyStaffRoomEquipment.AccessibleTable -> "Tavolo accessibile"
    EasyStaffRoomEquipment.Camera -> "Telecamera"
    EasyStaffRoomEquipment.VideoConference -> "Video conference"
    EasyStaffRoomEquipment.CeilingProjectorAndScreen -> "Videoproiettore a soffitto + telo"
    EasyStaffRoomEquipment.WebConference -> "Webconference"
    is EasyStaffRoomEquipment.Unknown -> value
}
