package it.attendance100.mybicocca.domain.repository

import androidx.lifecycle.LiveData
import it.attendance100.mybicocca.domain.model.CourseEvent
import it.attendance100.mybicocca.domain.model.CourseEventSelector

interface CalendarRepository {
    fun observeEvents(filter: CourseEventSelector): LiveData<List<CourseEvent>>
    suspend fun syncEvents()
}