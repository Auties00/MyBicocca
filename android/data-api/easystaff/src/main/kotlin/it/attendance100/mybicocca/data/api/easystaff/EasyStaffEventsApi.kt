package it.attendance100.mybicocca.data.api.easystaff

import io.ktor.client.*
import it.attendance100.mybicocca.data.dto.easystaff.*
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffBookingStatus
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffEventSearchResults
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffEventType
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffScheduledEvent
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalTime.of

/**
 * API for event search operations (Ricerca eventi).
 *
 * Provides access to:
 * - Universal event search across all types
 * - Filtering by building, room, event type, status, date range
 * - Keyword search
 *
 * This is the most flexible search interface, allowing queries across
 * all event types (lessons, exams, seminars, etc.) with various filters.
 */
class EasyStaffEventsApi(
    client: HttpClient,
    json: Json
) : EasyStaffAbstractApi(client, json) {
    companion object {
        private const val EVENTS_VIEW = "bookings"
    }

    /**
     * Gets the available search options for events.
     *
     * This includes available buildings, rooms, and event types.
     *
     * @param language The language for labels
     * @return The available search options
     */
    suspend fun getSearchOptions(language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN): EasyStaffEventSearchOptions {
        val doc = executeGet(
            AGENDA_WEB_API, mapOf(
                "view" to EVENTS_VIEW,
                "include" to "bookings",
                "_lang" to language.code
            )
        )

        // Parse buildings
        val buildings = doc.select("select[name*=sede] option").mapNotNull { option ->
            option.attr("value")
                .takeIf { it.isNotBlank() }
                ?.let {
                    EasyStaffBuilding(
                        code = it,
                        name = option.text().trim()
                    )
                }
        }

        // Parse rooms
        val rooms = doc.select("select[name*=aula] option").mapNotNull { option ->
            option.attr("value")
                .takeIf { it.isNotBlank() }
                ?.let {
                    EasyStaffRoom(
                        code = it,
                        name = option.text().trim(),
                        buildingCode = "",
                        buildingName = ""
                    )
                }
        }

        // Parse event types
        val eventTypes = doc.select("select[name=tipo] option")
            .mapNotNull { option ->
                val id = option.attr("value").toIntOrNull() ?: return@mapNotNull null
                EasyStaffEventType.fromId(id)
            }
            .distinct()
            .ifEmpty { EasyStaffEventType.entries.toList() }

        return EasyStaffEventSearchOptions(
            buildings = buildings,
            rooms = rooms,
            eventTypes = eventTypes
        )
    }

    /**
     * Gets all events for today.
     *
     * @param buildings Optional building filter
     * @param eventTypes Optional event type filter
     * @param language The language for labels
     * @return The search results
     */
    suspend fun getTodayEvents(
        buildings: List<String> = emptyList(),
        eventTypes: List<EasyStaffEventType> = emptyList(),
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffEventSearchResults {
        val today = LocalDate.now()
        return searchEvents(
            EasyStaffEventSearchQuery(
                buildings = buildings,
                startDate = today,
                endDate = today,
                eventTypes = eventTypes
            ),
            language
        )
    }

    /**
     * Gets all events for this week.
     *
     * @param buildings Optional building filter
     * @param eventTypes Optional event type filter
     * @param language The language for labels
     * @return The search results
     */
    suspend fun getWeekEvents(
        buildings: List<String> = emptyList(),
        eventTypes: List<EasyStaffEventType> = emptyList(),
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffEventSearchResults {
        val today = LocalDate.now()
        val weekStart = today.with(java.time.DayOfWeek.MONDAY)
        val weekEnd = weekStart.plusDays(6)

        return searchEvents(
            EasyStaffEventSearchQuery(
                buildings = buildings,
                startDate = weekStart,
                endDate = weekEnd,
                eventTypes = eventTypes
            ),
            language
        )
    }

    /**
     * Searches for events by keyword.
     *
     * @param keyword The search keyword
     * @param startDate The start date for the search range
     * @param endDate The end date for the search range
     * @param language The language for labels
     * @return The search results
     */
    suspend fun searchByKeyword(
        keyword: String,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate = LocalDate.now().plusMonths(1),
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffEventSearchResults {
        return searchEvents(
            EasyStaffEventSearchQuery(
                startDate = startDate,
                endDate = endDate,
                keyword = keyword
            ),
            language
        )
    }

    /**
     * Gets events of a specific type.
     *
     * @param eventType The event type to filter by
     * @param startDate The start date for the search range
     * @param endDate The end date for the search range
     * @param language The language for labels
     * @return The search results
     */
    suspend fun getEventsByType(
        eventType: EasyStaffEventType,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate = LocalDate.now().plusMonths(1),
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffEventSearchResults {
        return searchEvents(
            EasyStaffEventSearchQuery(
                startDate = startDate,
                endDate = endDate,
                eventTypes = listOf(eventType)
            ),
            language
        )
    }

    /**
     * Searches for events matching the given criteria.
     *
     * @param query The search parameters
     * @param language The language for labels
     * @return The search results
     */
    suspend fun searchEvents(
        query: EasyStaffEventSearchQuery,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffEventSearchResults {
        val params = buildMap {
            put("view", EVENTS_VIEW)
            put("include", "bookings")
            put("datefrom", formatDate(query.startDate))
            put("dateto", formatDate(query.endDate))
            put("_lang", language.code)

            // Add building filters
            if (query.buildings.isNotEmpty()) {
                put("sede", query.buildings.joinToString(","))
            }

            // Add room filter (if single room)
            if (query.rooms.size == 1) {
                put("aula", query.rooms.first())
            }

            // Add event type filters
            if (query.eventTypes.isNotEmpty()) {
                put("tipo", query.eventTypes.first().id.toString())
            }

            // Add status filter
            query.status?.let { put("stato", it.value) }

            // Add keyword
            query.keyword?.let { put("search", it) }

            // Add day of week filter
            if (query.daysOfWeek.isNotEmpty()) {
                put("giorno_settimana", query.daysOfWeek.first().value)
            }

            // Add time filters
            query.startTime?.let { put("dalle_ore", formatTime(it)) }
            query.endTime?.let { put("alle_ore", formatTime(it)) }
        }
        val doc = executeGet(AGENDA_WEB_API, params)

        val searchSummary = doc.selectFirst(".dati-ricerca, .search-summary, .risultati-ricerca")
            ?.cleanText()
            ?: ""

        val table = doc.selectFirst("table.table-bordered, table.detail_table, #tableEventi")
            ?: return EasyStaffEventSearchResults(
                events = listOf(),
                searchSummary = searchSummary
            )

        val headers = table.select("thead th, thead td")
            .map { it.cleanText().lowercase() }

        val events = table.select("tbody tr").mapNotNull { row ->
            val cells = row.select("td")
            if (cells.isEmpty()) return@mapNotNull null

            // Map cells to headers
            val cellMap = if (headers.isNotEmpty()) {
                headers.zip(cells).toMap()
            } else {
                emptyMap()
            }

            // Extract title
            val title = findCellValue(cellMap, "titolo", "descrizione", "evento", "title")
                ?: cells.getOrNull(0)?.cleanText()
                ?: return@mapNotNull null

            // Extract room
            val roomText = findCellValue(cellMap, "aula", "room", "sede")
                ?: cells.getOrNull(1)?.cleanText()
            val (room, building) = parseRoomAndBuilding(roomText)

            // Extract date
            val dateText = findCellValue(cellMap, "data", "date")
                ?: cells.getOrNull(2)?.cleanText()
            val date = dateText?.let { parseDate(it) } ?: return@mapNotNull null

            // Extract time
            val timeText = findCellValue(cellMap, "orari", "ora", "time")
                ?: cells.getOrNull(3)?.cleanText()
            val (startTime, endTime) = parseTimeRange(timeText)

            // Extract event type
            val eventTypeCell = findCell(cellMap, "tipo", "type")
            val eventTypeText = eventTypeCell?.cleanText()
            val eventType = if (eventTypeText != null) {
                EasyStaffEventType.fromItalianName(eventTypeText)
            } else {
                EasyStaffEventType.OTHER
            }

            // Extract organizers
            val organizerText = findCellValue(cellMap, "utilizzatori", "docente", "organizer")
                ?: cells.getOrNull(5)?.cleanText()
            val organizers = organizerText?.split(",", ";")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()

            // Extract status
            val statusText = findCellValue(cellMap, "stato", "status")
                ?: cells.lastOrNull()?.cleanText()
            val status =
                statusText?.let { EasyStaffBookingStatus.fromItalian(it) } ?: EasyStaffBookingStatus.CONFIRMED

            EasyStaffScheduledEvent(
                title = title,
                date = date,
                startTime = startTime ?: of(0, 0),
                endTime = endTime ?: of(23, 59),
                room = room,
                building = building,
                eventType = eventType,
                organizers = organizers,
                status = status
            )
        }

        return EasyStaffEventSearchResults(
            events = events.sortedWith(compareBy({ it.date }, { it.startTime })),
            searchSummary = searchSummary
        )
    }
}
