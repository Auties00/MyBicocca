package it.attendance100.mybicocca.domain.repository

import androidx.lifecycle.*
import it.attendance100.mybicocca.domain.model.*

interface CalendarRepository {
    fun observeEvents(filter: CourseEventSelector): LiveData<List<CourseEvent>>
    suspend fun syncEvents()

	suspend fun insertEvent(event: CourseEvent): Long
	suspend fun updateEvent(event: CourseEvent)
	suspend fun deleteEvent(event: CourseEvent)
}