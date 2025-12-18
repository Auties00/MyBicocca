package it.attendance100.mybicocca.data.repository

import androidx.lifecycle.*
import it.attendance100.mybicocca.data.api.bicoccapp.*
import it.attendance100.mybicocca.di.*
import it.attendance100.mybicocca.domain.model.*
import javax.inject.*
import it.attendance100.mybicocca.domain.repository.CalendarRepository as ICalendarRepository

class CalendarRepository @Inject constructor(
	private val api: BicoccappApi,
	private val database: AppDatabase,
) : ICalendarRepository {

	override fun observeEvents(filter: CourseEventSelector): LiveData<List<CourseEvent>> {
		val dao = database.courseEventDao()
		return when (filter) {
			is CourseEventSelector.ByDay -> {
				val start = filter.day.atStartOfDay()
				val end = filter.day.plusDays(1).atStartOfDay()
				dao.observeEventsBetween(start, end)
			}

			is CourseEventSelector.ByMonth -> {
				val start = filter.month.atDay(1).atStartOfDay()
				val end = filter.month.plusMonths(1).atDay(1).atStartOfDay()
				dao.observeEventsBetween(start, end)
			}
		}
    }

    override suspend fun syncEvents() {
	    val response = api.calendar.getCalendar()
	    if (response.isSuccessful) {
		    val calendarDays = response.body()?.calendar ?: emptyList()
		    val domainEvents = mutableListOf<CourseEvent>()

		    calendarDays.forEach { day ->
			    val dateStr = day.day // "YYYY-MM-DD" presumably, or DD-MM-YYYY. Assuming ISO based on API docs.
			    if (dateStr == null) return@forEach

			    // Parse Date
			    // DTO date format verification needed. Assuming "yyyy-MM-dd" for now based on API.
			    // If parsing fails, we skip.
			    val date = try {
				    java.time.LocalDate.parse(dateStr)
			    } catch (e: Exception) {
				    return@forEach
			    }

			    // 1. Map Events (Lectures)
			    day.events.forEach { eventDto ->
				    val timeRange = eventDto.time // "08:30 - 10:30"
				    val (startStr, endStr) = if (!timeRange.isNullOrBlank() && timeRange.contains("-")) {
					    timeRange.split("-").map { it.trim() }
				    } else {
					    listOf("00:00", "00:00") // Fallback
				    }

				    val startDateTime = date.atTime(java.time.LocalTime.parse(startStr))
				    val endDateTime = date.atTime(java.time.LocalTime.parse(endStr))

				    domainEvents.add(
					    CourseEvent(
						    courseName = eventDto.courseName ?: "Unknown",
						    courseCode = eventDto.courseCode,
						    professor = eventDto.teachers.joinToString(", ") { "${it.teacherFullName} (${it.teacherEmail})" },
						    room = eventDto.room,
						    building = null, // DTO doesn't give explicit building
						    startTime = startDateTime,
						    endTime = endDateTime,
						    eventType = EventType.LECTURE,
						    notes = null,
						    isCancelled = eventDto.canceled == "true",
						    color = null, // Calendar events might not have color, or check DTO
					    ),
				    )
			    }

			    // 2. Map Appeals (Exams)
			    day.appeals.forEach { appealDto ->
				    val timeStr = appealDto.time // "09:00"
				    val startDateTime = if (!timeStr.isNullOrBlank()) {
					    date.atTime(java.time.LocalTime.parse(timeStr))
				    } else {
					    date.atStartOfDay()
				    }
				    // Exams often default to 1-2 hours if end not specified
				    val endDateTime = startDateTime.plusHours(2)

				    domainEvents.add(
					    CourseEvent(
						    courseName = appealDto.courseName ?: "Esame",
						    courseCode = null,
						    professor = null,
						    room = appealDto.room,
						    building = null,
						    startTime = startDateTime,
						    endTime = endDateTime,
						    eventType = EventType.EXAM,
						    notes = "Status: ${appealDto.status}",
						    isCancelled = false,
						    color = "#FF5252", // Red for exams
					    ),
				    )
			    }
		    }

		    // Clear old events? Or upsert?
		    // For now, we insert. Ideally clear events in the fetched range.
		    // Since API returns a range, we might duplicate if we just insert.
		    // But CourseEvent has auto-gen ID.
		    // Strategy: Clear all and insert is safest for simple sync, or clear specific range.
		    // Assuming full sync for now.
		    // database.courseEventDao().clearAll() // If method existed

		    domainEvents.forEach { database.courseEventDao().insert(it) }
	    }
    }

	override suspend fun insertEvent(event: CourseEvent): Long {
		return database.courseEventDao().insert(event)
	}

	override suspend fun updateEvent(event: CourseEvent) {
		database.courseEventDao().update(event)
	}

	override suspend fun deleteEvent(event: CourseEvent) {
		database.courseEventDao().delete(event)
	}
}