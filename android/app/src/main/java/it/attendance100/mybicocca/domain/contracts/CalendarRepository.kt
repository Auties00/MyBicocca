package it.attendance100.mybicocca.domain.contracts

import androidx.lifecycle.*
import it.attendance100.mybicocca.data.entities.*
import java.time.*

interface CalendarRepository {
  fun observeEventsForMonth(month: YearMonth): LiveData<List<CourseEvent>>
  fun observeEventsForDate(date: LocalDate): LiveData<List<CourseEvent>>
  suspend fun insertEvent(event: CourseEvent): Long
  suspend fun updateEvent(event: CourseEvent)
  suspend fun deleteEvent(event: CourseEvent)
  suspend fun syncData()
}