package it.attendance100.mybicocca.data.api.easystaff

import io.ktor.client.*
import it.attendance100.mybicocca.data.api.cleanText
import it.attendance100.mybicocca.data.dto.easystaff.*
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffRoom
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.lang.IllegalStateException
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
                "sw" to "rooms_",
                "_" to System.currentTimeMillis().toString()
            )
        )
        val jsonString = extractJsonFromJsVariable(responseBody, "elenco_sedi")
            ?: throw IllegalStateException("Missing 'elenco_sedi' field")
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
                "sw" to "rooms_",
                "_lang" to language.code,
                "_" to System.currentTimeMillis().toString()
            )
        )

        val jsonString = extractJsonFromJsVariable(responseBody, "elenco_aule")
            ?: throw IllegalStateException("Missing 'elenco_aule' field")

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
     * Gets the daily occupation schedule for a specific room.
     *
     * @param building The building
     * @param room The room in the building
     * @param date The date to query
     * @param language The language for labels
     * @return The room's daily occupation schedule
     */
    suspend fun getRoomOccupation(
        building: EasyStaffBuilding,
        room: EasyStaffRoom,
        date: LocalDate,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffRoomOccupationEvent> {
        val buildingOccupation = getBuildingOccupation(
            building = building,
            date = date,
            room = room,
            language = language
        )
        return buildingOccupation.filter { it.roomCode == room.code }
    }

    /**
     * Gets detailed room information from the room showcase.
     *
     * @param building The building (optional, returns all buildings if null)
     * @param room The room in the building (optional, returns all rooms if null)
     * @param language The language for labels
     * @return The room showcase results
     */
    suspend fun getRoomShowcase(
        building: EasyStaffBuilding? = null,
        room: EasyStaffRoom? = null,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffRoomDetails> {
        val params = buildMap {
            put("view", "vetrina_aule")
            put("include", "vetrina_aule")
            put("_lang", language.code)
            building?.let { put("sede", it.code) }
        }

        val doc = executeGetHtml(AGENDA_WEB_ENDPOINT, params)

        val rooms = mutableListOf<EasyStaffRoomDetails>()

        val roomCards = doc.select(".room-card, .aula-card, .vetrina-item, .box-aula")

        for (card in roomCards) {
            val roomCode = card.attr("data-aula").ifBlank {
                card.selectFirst(".room-code, .codice-aula")?.cleanText()
            } ?: continue
            if(room != null && room.code != roomCode) {
                continue
            }

            val roomName = card.selectFirst(".room-name, .nome-aula, h4, h5")?.cleanText() ?: roomCode

            val roomBuildingCode = card.attr("data-sede").ifBlank { building?.code ?: "" }
            val roomBuildingName = card.selectFirst(".building-name, .nome-edificio")?.cleanText()
                ?: building?.name ?: roomBuildingCode
            val roomBuilding = EasyStaffBuilding(roomBuildingCode, roomBuildingName)

            val capacityText = card.selectFirst(".capacity, .capienza, [data-capacity]")?.cleanText()
            val capacity = capacityText?.filter { it.isDigit() }?.toIntOrNull()

            val examCapacityText = card.selectFirst(".exam-capacity, .capienza-esame")?.cleanText()
            val examCapacity = examCapacityText?.filter { it.isDigit() }?.toIntOrNull()

            val floor = card.selectFirst(".floor, .piano")?.cleanText()

            val isAccessible = card.selectFirst(".accessible, .accessibile, [data-accessible]") != null
                    || card.text().contains("accessibile", ignoreCase = true)

            val hasVideo = card.selectFirst(".has-video, [data-video]") != null
                    || card.text().contains("video", ignoreCase = true)
            val hasMicrophone = card.selectFirst(".has-mic, [data-microphone]") != null
                    || card.text().contains("microfono", ignoreCase = true)
            val hasProjector = card.selectFirst(".has-projector, [data-projector]") != null
                    || card.text().contains("proiettore", ignoreCase = true)
                    || card.text().contains("videoproiettore", ignoreCase = true)
            val hasComputer = card.selectFirst(".has-computer, .has-podio, [data-computer]") != null
                    || card.text().contains("podio", ignoreCase = true)
                    || card.text().contains("computer", ignoreCase = true)
            val hasWhiteboard = card.text().contains("lavagna bianca", ignoreCase = true)
                    || card.text().contains("whiteboard", ignoreCase = true)
            val hasBlackboard = card.text().contains("lavagna", ignoreCase = true)
                    && !card.text().contains("lavagna bianca", ignoreCase = true)

            val otherEquipment = card.select(".equipment-item, .dotazione li")
                .map { it.cleanText() }
                .filter { it.isNotBlank() }

            val notes = card.selectFirst(".notes, .note")?.cleanText()

            val imageUrl = card.selectFirst("img")?.attr("src")?.ifBlank { null }
            val mapsUrl = card.selectFirst("a[href*=maps], a[href*=google]")?.attr("href")?.ifBlank { null }

            rooms.add(
                EasyStaffRoomDetails(
                    code = roomCode,
                    name = roomName,
                    building = roomBuilding,
                    capacity = capacity,
                    examCapacity = examCapacity,
                    floor = floor,
                    isAccessible = isAccessible,
                    hasVideo = hasVideo,
                    hasMicrophone = hasMicrophone,
                    hasProjector = hasProjector,
                    hasComputer = hasComputer,
                    hasWhiteboard = hasWhiteboard,
                    hasBlackboard = hasBlackboard,
                    otherEquipment = otherEquipment,
                    notes = notes,
                    imageUrl = imageUrl,
                    mapsUrl = mapsUrl
                )
            )
        }

        return rooms
    }
}
