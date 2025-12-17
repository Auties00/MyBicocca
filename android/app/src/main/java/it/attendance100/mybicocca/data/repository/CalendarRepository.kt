package it.attendance100.mybicocca.data.repository

import androidx.lifecycle.LiveData
import it.attendance100.mybicocca.domain.model.CourseEvent
import java.time.LocalDate
import java.time.YearMonth
import it.attendance100.mybicocca.domain.repository.CalendarRepository as ICalendarRepository

class CalendarRepository : ICalendarRepository {
    override fun observeEventsForMonth(month: YearMonth): LiveData<List<CourseEvent>> {
        TODO("Not yet implemented")
    }

    override fun observeEventsForDate(date: LocalDate): LiveData<List<CourseEvent>> {
        TODO("Not yet implemented")
    }

    override suspend fun syncEvents() {
        TODO("Not yet implemented")
    }
}