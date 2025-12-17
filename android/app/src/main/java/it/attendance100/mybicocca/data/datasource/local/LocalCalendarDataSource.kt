package it.attendance100.mybicocca.data.datasource.local

import it.attendance100.mybicocca.data.local.dao.*
import it.attendance100.mybicocca.data.local.entity.*
import java.time.*
import javax.inject.*

class LocalCalendarDataSource @Inject constructor(
  private val eventDao: CourseEventDao,
  private val scheduleDao: CourseScheduleDao,
) {
  fun observeEventsBetween(start: LocalDateTime, end: LocalDateTime): androidx.lifecycle.LiveData<List<CourseEvent>> {
    return eventDao.observeEventsBetween(start, end)
  }

  suspend fun insertEvent(event: CourseEvent) = eventDao.insert(event)

  suspend fun deleteEvent(event: CourseEvent) = eventDao.delete(event)

  suspend fun updateEvent(event: CourseEvent) = eventDao.update(event)
}
