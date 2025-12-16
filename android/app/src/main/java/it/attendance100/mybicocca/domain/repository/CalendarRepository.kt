package it.attendance100.mybicocca.domain.repository

import androidx.lifecycle.LiveData
import it.attendance100.mybicocca.data.local.entity.CourseEvent
import java.time.LocalDate
import java.time.YearMonth

/**
 * Calendar operations contract
 * Defines methods for observing and managing calendar events
 */
interface CalendarRepository {
    fun observeEventsForMonth(month: YearMonth): LiveData<List<CourseEvent>>
    fun observeEventsForDate(date: LocalDate): LiveData<List<CourseEvent>>
    suspend fun insertEvent(event: CourseEvent): Long
    suspend fun updateEvent(event: CourseEvent)
    suspend fun deleteEvent(event: CourseEvent)
    suspend fun syncData()
}