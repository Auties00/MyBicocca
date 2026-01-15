package it.attendance100.mybicocca.data.api.easystaff

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Main entry point for the EasyStaff/Agenda Web API.
 *
 * This class provides access to all Agenda Web functionality:
 * - Lesson schedules (Orario delle lezioni)
 * - Exam calendar (Calendario esami)
 * - Room occupation and information (Occupazione/Vetrina aule)
 * - Event search (Ricerca eventi)
 *
 * The Agenda Web platform (powered by EasyStaff) is the scheduling system
 * for Università degli Studi di Milano-Bicocca. It provides:
 * - Public access to schedules (no authentication required for basic queries)
 * - Multi-language support (Italian, English, Spanish, German, French)
 * - Multiple search modes (by program, teacher, subject)
 * - Various display options (weekly agenda, basic schedule, compact view)
 *
 * @param httpClientConfig Optional configuration block for the underlying HTTP client.
 * @see EasyStaffScheduleApi for lesson schedule operations
 * @see EasyStaffExamCalendarApi for exam calendar operations
 * @see EasyStaffRoomsApi for room operations
 * @see EasyStaffEventsApi for event search operations
 * @see EasyStaffAttendanceApi for attendance operations
 */
class EasyStaffApi(httpClientConfig: HttpClientConfig<*>.() -> Unit = {}) : AutoCloseable {

    companion object {
        /**
         * HTTP timeout in milliseconds.
         */
        private const val TIMEOUT = 30_000L
    }

    /**
     * Json instance.
     */
    private val json: Json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    /**
     * Shared HTTP client for all API requests.
     *
     * Configured with:
     * - Cookie storage for session management (if authenticated)
     * - Extended timeouts for slow university servers
     * - Automatic redirect following
     */
    private val client = HttpClient {
        httpClientConfig()
        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT
            connectTimeoutMillis = TIMEOUT
            socketTimeoutMillis = TIMEOUT
        }
        install(ContentNegotiation) {
            json(json, contentType = ContentType.Application.Json)
            json(json, contentType = ContentType.Text.Html)
        }
        followRedirects = true
    }

    /**
     * API for lesson schedule operations (Orario delle lezioni).
     *
     * Provides access to:
     * - Lesson schedules by study program, teacher, or subject
     * - Available teaching areas and study programs
     * - Weekly schedule grid with lesson details
     * - Teacher listings
     */
    val schedule: EasyStaffScheduleApi = EasyStaffScheduleApi(client, json)

    /**
     * API for exam calendar operations (Calendario esami).
     *
     * Provides access to:
     * - Exam schedules by study program, teacher, or subject
     * - Date range queries for upcoming exams
     * - Exam details including room, teachers, and type
     */
    val exams: EasyStaffExamCalendarApi = EasyStaffExamCalendarApi(client, json)

    /**
     * API for room operations (Occupazione delle aule & Vetrina aule).
     *
     * Provides access to:
     * - Room occupation schedules (daily grid view)
     * - Room showcase (detailed room information)
     * - Building and room listings
     * - Room equipment and accessibility information
     */
    val rooms: EasyStaffRoomsApi = EasyStaffRoomsApi(client, json)

    /**
     * API for event search operations (Ricerca eventi).
     *
     * Provides access to:
     * - Universal event search across all types
     * - Filtering by building, room, event type, status, date range
     * - Keyword search
     * - Today/week convenience methods
     */
    val events: EasyStaffEventsApi = EasyStaffEventsApi(client, json)


    /**
     * API for attendance operations (Obbligo di Presenza).
     *
     * Provides access to:
     * - Attendance history
     * - Attendance verification
     */
    val attendance: EasyStaffAttendanceApi = EasyStaffAttendanceApi(client, json)

    /**
     * Closes the underlying HTTP client and releases resources.
     *
     * After calling this method, the API instance should not be used.
     * All API calls will fail after closing.
     *
     * This method is idempotent - calling it multiple times has no effect.
     */
    override fun close() {
        client.close()
    }
}
