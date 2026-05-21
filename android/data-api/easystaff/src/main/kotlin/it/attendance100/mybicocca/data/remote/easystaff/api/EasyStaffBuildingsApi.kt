package it.attendance100.mybicocca.data.remote.easystaff.api

import io.ktor.client.*
import it.attendance100.mybicocca.data.remote.common.exception.HtmlParsingException
import it.attendance100.mybicocca.data.remote.common.util.cleanText
import it.attendance100.mybicocca.data.remote.easystaff.dto.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.time.LocalDate

/**
 * API for room operations (Occupazione delle aule & Vetrina aule).
 *
 * Provides access to:
 * - Room occupation schedules (daily grid view)
 * - Room showcase (detailed room information)
 * - Building and room listings
 *
 * The room occupation view shows a daily schedule grid for all rooms
 * in a building, with events color-coded by type.
 *
 * The room showcase provides detailed information about rooms including
 * capacity, equipment, accessibility, and location.
 */
class EasyStaffBuildingsApi(
    client: HttpClient,
    json: Json
) : EasyStaffAbstractApi(client, json) {

    companion object {
        private const val ROOM_OCCUPATION_ENDPOINT = "/PortaleStudentiUnimib/rooms_call.php"
    }

    /**
     * Gets the list of available buildings.
     *
     * @return The available buildings
     */
    suspend fun getBuildings(): List<EasyStaffBuilding> {
        val responseBody = executeGetText(
            COMBO_ENDPOINT,
            mapOf(
                "sw" to listOf("rooms_"),
                "_" to listOf(System.currentTimeMillis().toString())
            )
        )
        val jsonString = extractJsonFromJsVariable(responseBody, "elenco_sedi")
            ?: throw HtmlParsingException("Missing 'elenco_sedi' field")
        return json.decodeFromString(jsonString)
    }

    /**
     * Gets the rooms available in a building.
     *
     * @param building The building
     * @param language The language for labels
     * @return The available rooms with capacity information
     */
    suspend fun getRooms(
        building: EasyStaffBuilding,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffRoom> {
        val responseBody = executeGetText(
            COMBO_ENDPOINT,
            mapOf(
                "sw" to listOf("rooms_"),
                "_lang" to listOf(language.code),
                "_" to listOf(System.currentTimeMillis().toString())
            )
        )

        val jsonString = extractJsonFromJsVariable(responseBody, "elenco_aule")
            ?: throw HtmlParsingException("Missing 'elenco_aule' field")

        val roomsArray = json.parseToJsonElement(jsonString)
            .jsonObject[building.code]
            ?.jsonArray
            ?: return emptyList()

        return json.decodeFromJsonElement(roomsArray)
    }

    /**
     * Gets the daily occupation schedule for a building.
     *
     * @param building The building
     * @param date The date to query
     * @param room Optional room filter (null for all rooms in building)
     * @param language The language for labels
     * @return The building's daily occupation schedule
     */
    suspend fun getBuildingOccupation(
        building: EasyStaffBuilding,
        date: LocalDate,
        room: EasyStaffRoom? = null,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffRoomOccupationEvent> {
        val params = buildMap {
            put("view", listOf("rooms"))
            put("include", listOf("rooms"))
            put("sede[]", listOf(building.code))
            put("date", listOf(formatDate(date)))
            put("_lang", listOf(language.code))
            room?.let { put("aula[]", listOf(it.code)) }
            put("list", listOf(""))
            put("week_grid_type", listOf("-1"))
            put("ar_codes_", listOf(""))
            put("ar_select_", listOf(""))
            put("col_cells", listOf("0"))
            put("empty_box", listOf("0"))
            put("only_grid", listOf("0"))
            put("highlighted_date", listOf("0"))
            put("all_events", listOf("0"))
        }

        val response = executePostForm<EasyStaffRoomOccupationResponse>(ROOM_OCCUPATION_ENDPOINT, params)
        return response.events
    }

    /**
     * Gets detailed room information from the room showcase.
     *
     * @param buildings The buildings
     * @param rooms The rooms (optional, returns data for all rooms if empty)
     * @param language The language for labels
     * @throws IllegalArgumentException if the buildings are empty
     * @return The room showcase results
     */
    suspend fun getRoomDetails(
        buildings: List<EasyStaffBuilding>,
        rooms: List<EasyStaffRoom> = emptyList(),
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffRoomDetails> {
        if(buildings.isEmpty()) {
            throw IllegalArgumentException("Buildings cannot be empty")
        }

        val params = buildMap {
            put("form-type", listOf("vetrina_aule"))
            put("view", listOf("vetrina_aule"))
            put("include", listOf("vetrina_aule"))
            put("sede[]", buildings.map { it.code })
            if(rooms.isNotEmpty()) {
                put("aula[]", rooms.map { it.code })
            }
            put("list", listOf(""))
            put("week_grid_type", listOf("-1"))
            put("ar_codes_", listOf(""))
            put("ar_select_", listOf(""))
            put("col_cells", listOf("0"))
            put("empty_box", listOf("1"))
            put("only_grid", listOf("0"))
            put("highlighted_date", listOf("0"))
            put("all_events", listOf("0"))
            put("_lang", listOf(language.code))

        }
        val doc = executeGetHtml(AGENDA_WEB_ENDPOINT, params)

        return doc.select(".attendance-section")
            .mapNotNull { parseRoomDetails(doc, it)  }
    }

    private fun parseRoomDetails(doc: Document, card: Element): EasyStaffRoomDetails? {
        val headerToNodeMap = card.select(".custom-color-bold")
            .associate { it.text().cleanText() to it.siblingNodes().lastOrNull()}

        fun extractText(label: String): String? {
            val node = headerToNodeMap[label]
            return if(node is TextNode) {
                node.text()
                    .cleanText()
                    .takeIf { !it.equals("Dato mancante", ignoreCase = true) }
            } else {
                null
            }
        }

        fun extractLink(label: String): String? {
            val node = headerToNodeMap[label]
            return if(node != null && node.hasAttr("href")) {
                node.attr("href")
            } else {
                null
            }
        }

        val locationName = extractText("Sede")
            ?: return null

        val address = extractText("Indirizzo")

        val googleMapsLink = extractLink("Google Maps")
            ?.let { parseRoomMapsLink(doc, it) }

        val interactive360Link = extractLink("Immagine interattiva 360°")

        val description = extractText("Descrizione")

        val capacity = extractText("Capacità")
            ?.let { parseRoomCapacity(it) }

        val roomType = extractText("Tipo")
            ?.takeIf { it != "non definito" }

        val floor = extractText("Piano")
            ?.let { parseRoomFloor(it) }

        val accessibility = extractText("Accessibile")
        val isAccessible = parseRoomAccessibility(accessibility)

        val equipment = extractLink("Attrezzature")
            ?.let { parseRoomEquipment(card, it) }
            ?: emptyList()

        return EasyStaffRoomDetails(
            name = locationName,
            address = address,
            googleMapsLink = googleMapsLink,
            interactive360Link = interactive360Link,
            description = description,
            capacity = capacity,
            roomType = roomType,
            floor = floor,
            isAccessible = isAccessible,
            equipment = equipment
        )
    }

    private fun parseRoomMapsLink(doc: Document, selector: String): String? {
        val iframe = doc.selectFirst(selector)
            ?.selectFirst("iframe")
        return if (iframe != null && iframe.hasAttr("src")) {
            iframe.attr("src")
        } else {
            null
        }
    }

    private fun parseRoomCapacity(string: String): Int? {
        return string.removeSuffix(" posti").toIntOrNull()
    }

    private fun parseRoomFloor(string: String): Int? {
        return if (string.equals("Terra", ignoreCase = true) || string.equals("T", ignoreCase = true) || string.equals("PT", ignoreCase = true)) {
            0
        } else {
            string.toIntOrNull()
        }
    }

    private fun parseRoomAccessibility(accessibility: String?): Boolean {
        return accessibility != null && accessibility.equals("sì", ignoreCase = true)
    }

    private fun parseRoomEquipment(card: Element, selector: String): List<Esse3RoomEquipment> {
        return card.selectFirst(selector)
            ?.select(".contenuto span")
            ?.mapNotNull { parseRoomEquipmentEntry(it) }
            ?: emptyList()
    }

    private fun parseRoomEquipmentEntry(span: Element): Esse3RoomEquipment? {
        val name = span.text().trim()
        return if (span.nextElementSibling()?.text()?.contains("DISPONIBILE", ignoreCase = true) == true) {
            when (name) {
                "Attrezzature fisse" -> Esse3RoomEquipment.FixedEquipment
                "Attrezzature mobili" -> Esse3RoomEquipment.MobileEquipment
                "Cattedra" -> Esse3RoomEquipment.TeacherDesk
                "Finestre oscurabili" -> Esse3RoomEquipment.BlackoutWindows
                "Gradoni" -> Esse3RoomEquipment.TieredSeating
                "Impianto audio" -> Esse3RoomEquipment.AudioSystem
                "Kit aule standard per videolezioni" -> Esse3RoomEquipment.StandardVideoLessonKit
                "Lavagna a fogli" -> Esse3RoomEquipment.FlipChart
                "Lavagna a pennarelli" -> Esse3RoomEquipment.Whiteboard
                "Lavagna in ardesia" -> Esse3RoomEquipment.SlateBlackboard
                "Lavagna luminosa" -> Esse3RoomEquipment.OverheadProjector
                "Lavagna saliscendi" -> Esse3RoomEquipment.SlidingBlackboard
                "Lettini" -> Esse3RoomEquipment.ExaminationBeds
                "Monitor-Tv lcd-plasma" -> Esse3RoomEquipment.LcdPlasmaMonitor
                "PC cattedra" -> Esse3RoomEquipment.TeacherPc
                "Podio" -> Esse3RoomEquipment.Podium
                "Presa elettrica su postazione studente" -> Esse3RoomEquipment.StudentPowerSocket
                "Registrazione" -> Esse3RoomEquipment.RecordingSystem
                "Rete fissa" -> Esse3RoomEquipment.WiredNetwork
                "Rete wifi" -> Esse3RoomEquipment.WifiNetwork
                "Sedie Fisse con Banco" -> Esse3RoomEquipment.FixedChairsWithDesks
                "Sedie fisse con ribaltina" -> Esse3RoomEquipment.FixedChairsWithTabletArms
                "Sedie mobili con ribaltina" -> Esse3RoomEquipment.MobileChairsWithTabletArms
                "Sedie mobili senza ribaltina" -> Esse3RoomEquipment.MobileChairsWithoutTabletArms
                "Tavolo Accessibile (disabilità)" -> Esse3RoomEquipment.AccessibleTable
                "Telecamera" -> Esse3RoomEquipment.Camera
                "Video-Call conference" -> Esse3RoomEquipment.VideoConference
                "Videoproiettore soffitto+Telo" -> Esse3RoomEquipment.CeilingProjectorAndScreen
                "Webconference" -> Esse3RoomEquipment.WebConference
                else -> Esse3RoomEquipment.Other(name)
            }
        } else {
            null
        }
    }
}
