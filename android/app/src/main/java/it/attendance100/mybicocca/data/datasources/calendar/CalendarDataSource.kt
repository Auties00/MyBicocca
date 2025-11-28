package it.attendance100.mybicocca.data.datasources.calendar

import it.attendance100.mybicocca.data.entities.*
import java.time.*

/**
 * Abstraction for calendar data retrieval
 * Allows abstracting data origin (mock, API, etc)
 */
interface CalendarDataSource {
    /**
     * Retrieves events for a specific month
     */
    suspend fun getEventsForMonth(month: YearMonth): List<CourseEvent>

    /**
     * Retrieves events for a specific date
     */
    suspend fun getEventsForDate(date: LocalDate): List<CourseEvent>

    /**
     * Syncs events from the server
     */
    suspend fun syncEvents(): Boolean
}
