package it.attendance100.mybicocca.domain.repository

import androidx.lifecycle.LiveData
import it.attendance100.mybicocca.domain.model.CalendarEvent
import it.attendance100.mybicocca.domain.model.CourseEventSelector
import java.time.LocalDate

interface CalendarRepository {
    fun observeEvents(filter: CourseEventSelector): LiveData<List<CalendarEvent>>
    suspend fun syncEvents(startDate: LocalDate? = null): Boolean
}