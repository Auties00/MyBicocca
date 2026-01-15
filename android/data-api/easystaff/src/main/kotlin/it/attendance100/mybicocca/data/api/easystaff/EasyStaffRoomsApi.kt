package it.attendance100.mybicocca.data.api.easystaff

import io.ktor.client.*
import it.attendance100.mybicocca.data.api.cleanText
import it.attendance100.mybicocca.data.dto.easystaff.*
import kotlinx.serialization.json.Json
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime

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
class EasyStaffRoomsApi(
    client: HttpClient,
    json: Json
) : EasyStaffAbstractApi(client, json) {

    companion object {
        private const val ROOMS_VIEW = "rooms"
        private const val SHOWCASE_VIEW = "vetrina_aule"

        // Time grid defaults
        private val GRID_START_TIME = LocalTime.of(8, 0)
        private val GRID_END_TIME = LocalTime.of(20, 0)
    }

    /**
     * Gets the list of available buildings.
     *
     * @return The available buildings
     */
    suspend fun getBuildings(): List<EasyStaffBuilding> {
        val responseBody = executeGetText("/PortaleStudentiUnimib/combo.php", mapOf(
            "sw" to "rooms_",
            "_" to System.currentTimeMillis().toString()
        ))

        val jsonString = extractJsonFromJsVariable(responseBody, "elenco_sedi")
            ?: return emptyList()
        return json.decodeFromString(jsonString)
    }

    /**
     * Gets the rooms available in a building.
     *
     * @param buildingCode The building code
     * @param language The language for labels
     * @return The available rooms
     */
    suspend fun getRoomsInBuilding(
        buildingCode: String,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffRoom> {
        val doc = executeGet(AGENDA_WEB_API, mapOf(
            "view" to ROOMS_VIEW,
            "include" to "rooms",
            "sede" to buildingCode,
            "_lang" to language.code
        ))

        // Find the building name
        val buildingName = doc.select("#sede option[value=$buildingCode], select[name=sede] option[value=$buildingCode]")
            .firstOrNull()?.text()?.trim() ?: buildingCode

        return doc.select("#aula option, select[name=aula] option")
            .mapNotNull { option ->
                val code = option.attr("value")
                if (code.isBlank() || code == "all") return@mapNotNull null
                EasyStaffRoom(
                    code = code,
                    name = option.text().trim(),
                    buildingCode = buildingCode,
                    buildingName = buildingName
                )
            }
    }

    /**
     * Gets the daily occupation schedule for a building.
     *
     * @param query The search parameters
     * @param language The language for labels
     * @return The building's daily occupation schedule
     */
    suspend fun getBuildingOccupation(
        query: EasyStaffRoomOccupationQuery,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffBuildingDailyOccupation {
        val params = buildMap {
            put("view", ROOMS_VIEW)
            put("include", "rooms")
            put("sede", query.buildingCode)
            put("date", formatDate(query.date))
            put("_lang", language.code)
            query.roomCode?.let { put("aula", it) }
        }

        val doc = executeGet(AGENDA_WEB_API, params)

        // Parse building info
        val buildingName = doc.select("#sede option[selected], select[name=sede] option[selected]")
            .firstOrNull()?.text()?.trim() ?: query.buildingCode
        val building = EasyStaffBuilding(query.buildingCode, buildingName)

        // Parse rooms from the occupation grid
        val rooms = mutableListOf<EasyStaffRoomDailyOccupation>()

        // The occupation grid typically shows rooms as rows and time slots as columns
        val gridRows = doc.select(".riga-aula, .room-row, tr[data-aula]")

        for (row in gridRows) {
            val roomCode = row.attr("data-aula").ifBlank {
                row.selectFirst(".nome-aula, .room-name, td:first-child")?.cleanText()
            } ?: continue

            val roomName = row.selectFirst(".nome-aula, .room-name")?.cleanText() ?: roomCode

            val room = EasyStaffRoom(
                code = roomCode,
                name = roomName,
                buildingCode = query.buildingCode,
                buildingName = buildingName
            )

            val timeSlots = parseRoomTimeSlots(row)

            rooms.add(EasyStaffRoomDailyOccupation(
                room = room,
                building = building,
                date = query.date,
                timeSlots = timeSlots,
                gridStartTime = GRID_START_TIME,
                gridEndTime = GRID_END_TIME
            ))
        }

        // If no grid rows found, try alternative parsing
        if (rooms.isEmpty()) {
            val occupationTable = doc.selectFirst("table.occupation-grid, table.schedule-grid, #tableOccupazione")
            if (occupationTable != null) {
                rooms.addAll(parseOccupationTable(occupationTable, building, query.date))
            }
        }

        return EasyStaffBuildingDailyOccupation(
            building = building,
            date = query.date,
            rooms = rooms,
            gridStartTime = GRID_START_TIME,
            gridEndTime = GRID_END_TIME
        )
    }

    /**
     * Gets the daily occupation schedule for a specific room.
     *
     * @param buildingCode The building code
     * @param roomCode The room code
     * @param date The date to query
     * @param language The language for labels
     * @return The room's daily occupation schedule
     */
    suspend fun getRoomOccupation(
        buildingCode: String,
        roomCode: String,
        date: LocalDate,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffRoomDailyOccupation {
        val query = EasyStaffRoomOccupationQuery(
            buildingCode = buildingCode,
            roomCode = roomCode,
            date = date
        )

        val buildingOccupation = getBuildingOccupation(query, language)

        return buildingOccupation.rooms.find { it.room.code == roomCode }
            ?: EasyStaffRoomDailyOccupation(
                room = EasyStaffRoom(roomCode, roomCode, buildingCode, buildingOccupation.building.name),
                building = buildingOccupation.building,
                date = date,
                timeSlots = emptyList(),
                gridStartTime = GRID_START_TIME,
                gridEndTime = GRID_END_TIME
            )
    }

    /**
     * Gets detailed room information from the room showcase.
     *
     * @param buildingCode The building code (optional, returns all if null)
     * @param language The language for labels
     * @return The room showcase results
     */
    suspend fun getRoomShowcase(
        buildingCode: String? = null,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): RoomShowcaseResults {
        val params = buildMap {
            put("view", SHOWCASE_VIEW)
            put("include", "vetrina_aule")
            put("_lang", language.code)
            buildingCode?.let { put("sede", it) }
        }

        val doc = executeGet(AGENDA_WEB_API, params)

        // Parse building info if filtering
        val building = if (buildingCode != null) {
            val buildingName = doc.select("#sede option[selected], select[name=sede] option[selected]")
                .firstOrNull()?.text()?.trim() ?: buildingCode
            EasyStaffBuilding(buildingCode, buildingName)
        } else null

        // Parse room cards
        val rooms = mutableListOf<EasyStaffRoomShowcaseDetails>()

        val roomCards = doc.select(".room-card, .aula-card, .vetrina-item, .box-aula")

        for (card in roomCards) {
            val roomCode = card.attr("data-aula").ifBlank {
                card.selectFirst(".room-code, .codice-aula")?.cleanText()
            } ?: continue

            val roomName = card.selectFirst(".room-name, .nome-aula, h4, h5")?.cleanText() ?: roomCode

            // Parse building for this room
            val roomBuildingCode = card.attr("data-sede").ifBlank { buildingCode ?: "" }
            val roomBuildingName = card.selectFirst(".building-name, .nome-edificio")?.cleanText()
                ?: building?.name ?: roomBuildingCode
            val roomBuilding = EasyStaffBuilding(roomBuildingCode, roomBuildingName)

            // Parse capacity
            val capacityText = card.selectFirst(".capacity, .capienza, [data-capacity]")?.cleanText()
            val capacity = capacityText?.filter { it.isDigit() }?.toIntOrNull()

            val examCapacityText = card.selectFirst(".exam-capacity, .capienza-esame")?.cleanText()
            val examCapacity = examCapacityText?.filter { it.isDigit() }?.toIntOrNull()

            // Parse floor
            val floor = card.selectFirst(".floor, .piano")?.cleanText()

            // Parse accessibility
            val isAccessible = card.selectFirst(".accessible, .accessibile, [data-accessible]") != null
                    || card.text().contains("accessibile", ignoreCase = true)

            // Parse equipment
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

            // Parse other equipment
            val otherEquipment = card.select(".equipment-item, .dotazione li")
                .map { it.cleanText() }
                .filter { it.isNotBlank() }

            // Parse notes
            val notes = card.selectFirst(".notes, .note")?.cleanText()

            // Parse URLs
            val imageUrl = card.selectFirst("img")?.attr("src")?.ifBlank { null }
            val mapsUrl = card.selectFirst("a[href*=maps], a[href*=google]")?.attr("href")?.ifBlank { null }

            rooms.add(EasyStaffRoomShowcaseDetails(
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
            ))
        }

        return RoomShowcaseResults(
            rooms = rooms,
            building = building
        )
    }

    /**
     * Gets detailed information for a specific room.
     *
     * @param buildingCode The building code
     * @param roomCode The room code
     * @param language The language for labels
     * @return The room details, or null if not found
     */
    suspend fun getRoomDetails(
        buildingCode: String,
        roomCode: String,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffRoomShowcaseDetails? {
        val results = getRoomShowcase(buildingCode, language)
        return results.rooms.find { it.code == roomCode }
    }

    /**
     * Parses time slots from a room row in the occupation grid.
     */
    private fun parseRoomTimeSlots(
        row: Element
    ): List<EasyStaffRoomTimeSlot> {
        val slots = mutableListOf<EasyStaffRoomTimeSlot>()

        // Parse event cells
        val eventCells = row.select(".cella-evento, .event-cell, td[data-time]")

        for (cell in eventCells) {
            val timeAttr = cell.attr("data-time").ifBlank {
                cell.attr("data-ora")
            }

            val startTime = if (timeAttr.isNotBlank()) {
                parseTime(timeAttr)
            } else null

            // Default to 30-minute slots if no end time
            val endTime = startTime?.plusMinutes(30)

            // Check if cell has an event
            val hasEvent = cell.hasClass("occupata") ||
                    cell.hasClass("occupied") ||
                    cell.selectFirst(".evento, .event") != null

            val event = if (hasEvent) {
                val title = cell.selectFirst(".titolo-evento, .event-title")?.cleanText()
                    ?: cell.attr("title").ifBlank { "Evento" }

                val eventTypeText = cell.attr("data-tipo").ifBlank {
                    cell.selectFirst(".tipo-evento")?.cleanText()
                }
                val eventType = eventTypeText?.let { EasyStaffEventType.fromItalianName(it) } ?: EasyStaffEventType.OTHER

                val organizer = cell.selectFirst(".organizzatore, .docente")?.cleanText()
                val studyProgram = cell.selectFirst(".corso, .cdl")?.cleanText()

                EasyStaffRoomOccupationEvent(
                    title = title,
                    startTime = startTime ?: GRID_START_TIME,
                    endTime = endTime ?: GRID_END_TIME,
                    eventType = eventType,
                    organizer = organizer,
                    studyProgram = studyProgram
                )
            } else null

            if (startTime != null && endTime != null) {
                slots.add(EasyStaffRoomTimeSlot(
                    startTime = startTime,
                    endTime = endTime,
                    event = event
                ))
            }
        }

        return slots.sortedBy { it.startTime }
    }

    /**
     * Parses room occupation from a table format.
     */
    private fun parseOccupationTable(
        table: org.jsoup.nodes.Element,
        building: EasyStaffBuilding,
        date: LocalDate
    ): List<EasyStaffRoomDailyOccupation> {
        val rooms = mutableListOf<EasyStaffRoomDailyOccupation>()

        // Parse header for time slots
        val timeHeaders = table.select("thead th, thead td")
            .drop(1) // Skip room name column
            .mapNotNull { th ->
                parseTime(th.cleanText())
            }

        // Parse each row as a room
        val rows = table.select("tbody tr")

        for (row in rows) {
            val cells = row.select("td")
            if (cells.isEmpty()) continue

            val roomCell = cells.first() ?: continue
            val roomName = roomCell.cleanText()
            val roomCode = roomCell.attr("data-aula").ifBlank { roomName }

            val room = EasyStaffRoom(
                code = roomCode,
                name = roomName,
                buildingCode = building.code,
                buildingName = building.name
            )

            val slots = cells.drop(1).mapIndexedNotNull { index, cell ->
                val startTime = timeHeaders.getOrNull(index) ?: return@mapIndexedNotNull null
                val endTime = timeHeaders.getOrNull(index + 1) ?: startTime.plusMinutes(30)

                val hasEvent = cell.hasClass("occupata") || cell.text().isNotBlank()

                val event = if (hasEvent && cell.text().isNotBlank()) {
                    EasyStaffRoomOccupationEvent(
                        title = cell.cleanText(),
                        startTime = startTime,
                        endTime = endTime,
                        eventType = EasyStaffEventType.OTHER,
                        organizer = null,
                        studyProgram = null
                    )
                } else null

                EasyStaffRoomTimeSlot(
                    startTime = startTime,
                    endTime = endTime,
                    event = event
                )
            }

            rooms.add(EasyStaffRoomDailyOccupation(
                room = room,
                building = building,
                date = date,
                timeSlots = slots,
                gridStartTime = GRID_START_TIME,
                gridEndTime = GRID_END_TIME
            ))
        }

        return rooms
    }
}
