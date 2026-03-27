package it.attendance100.mybicocca.data.api.easystaff

import io.ktor.client.*
import it.attendance100.mybicocca.data.common.exception.HtmlParsingException
import it.attendance100.mybicocca.data.dto.easystaff.*
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

/**
 * API for event search operations (Ricerca eventi).
 *
 * Provides access to:
 * - Universal event search across all types
 * - Filtering by building, room, event type, status, date range
 * - Keyword search
 */
class EasyStaffEventsApi(
    client: HttpClient,
    json: Json
) : EasyStaffAbstractApi(client, json) {
    companion object {
        private const val BOOKINGS_ENDPOINT = "/PortaleStudentiUnimib/bookings_call.php"

        private val DEFAULT_BUILDINGS = listOf(EasyStaffBuilding.FILTER_ALLOW_ALL, EasyStaffBuilding.FILTER_ALLOW_NO_ROOM)
    }

    /**
     * Gets the list of available event types.
     *
     * @param language The language for labels
     * @return The available event types
     */
    suspend fun getEventTypes(
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN,
    ): List<EasyStaffEventType> {
        val response = executeGetText(
            COMBO_ENDPOINT,
            mapOf(
                "sw" to listOf("rooms_"),
                "_lang" to listOf(language.code)
            )
        )
        val jsonString = extractJsonFromJsVariable(response, "elenco_tipi")
            ?: throw HtmlParsingException("Missing 'elenco_tipi' field")
        return json.decodeFromString(jsonString)
    }

    /**
     * Gets all events for today.
     *
     * @param buildings Optional building filter
     * @param eventTypes Optional event type filter
     * @param language The language for labels
     * @param keyword Optional keyword search
     * @return The search results
     */
    suspend fun getTodayEvents(
        buildings: List<EasyStaffBuilding> = emptyList(),
        eventTypes: List<EasyStaffEventType> = emptyList(),
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN,
        keyword: String? = null
    ): List<EasyStaffEvent> {
        val today = LocalDate.now()
        return getEvents(
            buildings = buildings,
            startDate = today,
            endDate = today,
            eventTypes = eventTypes,
            language = language,
            keyword = keyword
        )
    }

    /**
     * Gets all events for this week.
     *
     * @param buildings Optional building filter
     * @param eventTypes Optional event type filter
     * @param language The language for labels
     * @param keyword Optional keyword search
     * @return The search results
     */
    suspend fun getWeekEvents(
        buildings: List<EasyStaffBuilding> = emptyList(),
        eventTypes: List<EasyStaffEventType> = emptyList(),
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN,
        keyword: String? = null
    ): List<EasyStaffEvent> {
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        return getEvents(
            buildings = buildings,
            startDate = weekStart,
            endDate = weekEnd,
            eventTypes = eventTypes,
            language = language,
            keyword = keyword
        )
    }

    /**
     * Gets all events for this month.
     *
     * @param buildings Optional building filter
     * @param eventTypes Optional event type filter
     * @param language The language for labels
     * @param keyword Optional keyword search
     * @return The search results
     */
    suspend fun getMonthEvents(
        buildings: List<EasyStaffBuilding> = emptyList(),
        eventTypes: List<EasyStaffEventType> = emptyList(),
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN,
        keyword: String? = null,
    ): List<EasyStaffEvent> {
        val today = LocalDate.now()
        val monthStart = today.with(TemporalAdjusters.firstDayOfMonth())
        val monthEnd = today.with(TemporalAdjusters.lastDayOfMonth())
        return getEvents(
            buildings = buildings,
            startDate = monthStart,
            endDate = monthEnd,
            eventTypes = eventTypes,
            language = language,
            keyword = keyword
        )
    }

    /**
     * Searches for events matching the given criteria.
     *
     * @param startDate The start date for the search range
     * @param endDate The end date for the search range
     * @param buildings Buildings to search (empty for all)
     * @param rooms Rooms to search (empty for all)
     * @param eventTypes Event types to filter by (empty for all)
     * @param status Booking status filter (null for all)
     * @param keyword Optional keyword search
     * @param daysOfWeek Days of week to filter by (empty for all)
     * @param startTime Optional start time filter
     * @param endTime Optional end time filter
     * @param language The language for labels
     * @return The search results
     */
    suspend fun getEvents(
        startDate: LocalDate,
        endDate: LocalDate,
        buildings: List<EasyStaffBuilding> = emptyList(),
        rooms: List<EasyStaffRoom> = emptyList(),
        eventTypes: List<EasyStaffEventType> = emptyList(),
        status: EasyStaffBookingStatus? = null,
        keyword: String? = null,
        daysOfWeek: List<EasyStaffDayOfWeek> = emptyList(),
        startTime: LocalTime? = null,
        endTime: LocalTime? = null,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffEvent> {
        val params = buildMap {
            put("view", listOf("bookings"))
            put("form-type", listOf("bookings"))
            put("include", listOf("bookings"))
            put("mode", listOf("bookings"))

            put("sede[]", buildings.ifEmpty { DEFAULT_BUILDINGS }.map { it.code })

            if (rooms.isNotEmpty()) {
                put("aula[]", rooms.map { it.code })
            }

            put("tipo", listOf(eventTypes.firstOrNull()?.code?.toString() ?: ""))
            put("stato", listOf(status?.value ?: ""))
            put("ricerca", listOf(keyword ?: ""))

            put("datefrom", listOf(formatDate(startDate)))
            put("dateto", listOf(formatDate(endDate)))

            put("giorno_settimana[]", daysOfWeek.map { it.value }.ifEmpty { listOf("") })

            put("start_time", listOf(startTime?.let { formatTime(it) } ?: ""))
            put("end_time", listOf(endTime?.let { formatTime(it) } ?: ""))

            put("_lang", listOf(language.code))
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

        val response = executePostForm<EasyStaffEventsResponse>(BOOKINGS_ENDPOINT, params)
        return response.events
    }
}
