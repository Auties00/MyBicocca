package it.attendance100.mybicocca.domain.datasource

import it.attendance100.mybicocca.data.local.entity.*
import it.attendance100.mybicocca.domain.model.*
import java.time.*

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
   * Retrieves bookable slots for appointments
   */
  suspend fun getBookableSlots(): List<BookingSlot>

    /**
     * Syncs events from the server
     */
    suspend fun syncEvents(): Boolean
}