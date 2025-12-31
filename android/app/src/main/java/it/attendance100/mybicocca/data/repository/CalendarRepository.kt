package it.attendance100.mybicocca.data.repository

import androidx.lifecycle.LiveData
import it.attendance100.mybicocca.data.api.bicoccapp.BicoccappApi
import it.attendance100.mybicocca.di.AppDatabase
import it.attendance100.mybicocca.domain.model.CalendarEvent
import it.attendance100.mybicocca.domain.model.CalendarEventTeacher
import it.attendance100.mybicocca.domain.model.CalendarEventType
import it.attendance100.mybicocca.domain.model.CourseEventSelector
import it.attendance100.mybicocca.manager.StorageManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import it.attendance100.mybicocca.domain.repository.CalendarRepository as ICalendarRepository

class CalendarRepository @Inject constructor(
	private val api: BicoccappApi,
	private val database: AppDatabase,
    private val storage: StorageManager
) : ICalendarRepository {
    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    }

	override fun observeEvents(filter: CourseEventSelector): LiveData<List<CalendarEvent>> {
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

    override suspend fun syncEvents(startDate: LocalDate?): Boolean {
        val enrollmentId = storage.authEnrollmentId ?: return false
	    val response = api.calendar.getCalendar(enrollmentId, startDate?.format(DateTimeFormatter.ISO_DATE))
        if (!response.isSuccessful) return false
        val calendar = response.body() ?: return false

        val domainEvents = mutableListOf<CalendarEvent>()

        var calendarStartDate: LocalDate? = null
        var calendarEndDate: LocalDate? = null

        calendar.days.forEach { day ->
            val date = day.date.let {
                LocalDate.parse(it, dateFormatter)
            } ?: return@forEach

            if(calendarStartDate == null || date < calendarStartDate) {
                calendarStartDate = date
            }

            if(calendarEndDate == null || date > calendarEndDate) {
                calendarEndDate = date
            }

            day.events.forEach { eventDto ->
                val courseCode = eventDto.courseCode ?: return@forEach

                val courseName = eventDto.courseName

                val (startStr, endStr) = (eventDto.time ?: "00-00").split("-", limit = 2).map {
                        section -> section.trim()
                }


                val startDateTime = date.atTime(LocalTime.parse(startStr))
                val endDateTime = date.atTime(LocalTime.parse(endStr))

                val teachers = eventDto.teachers.mapNotNull {
                    val code = it.code ?: return@mapNotNull null
                    val fullName = it.fullName ?: return@mapNotNull null
                    val email = it.email ?: return@mapNotNull null

                    CalendarEventTeacher(
                        code = code,
                        fullName = fullName,
                        email = email
                    )
                }

                val room = eventDto.room

                val building = eventDto.courseCode
                    .split("-", limit = 2)
                    .first()

                domainEvents.add(
                    CalendarEvent(
                        id = courseCode,
                        name = courseName,
                        teachers = teachers,
                        room = room,
                        building = building,
                        startTime = startDateTime,
                        endTime = endDateTime,
                        type = CalendarEventType.LECTURE,
                        notes = null,
                        isCancelled = eventDto.canceled == "1",
                        color = null, // Calendar events might not have color, or check DTO
                    ),
                )
            }

            day.appeals.forEach { appealDto ->
                val code = appealDto.activityAppealId.toString()

                val dateTime = date.atTime(LocalTime.parse(appealDto.time))

                domainEvents.add(
                    CalendarEvent(
                        id = code,
                        name = appealDto.appealDescription,
                        teachers = emptyList(),
                        room = null,
                        building = null,
                        startTime = dateTime,
                        endTime = dateTime,
                        type = CalendarEventType.EXAM,
                        notes = "Status: ${appealDto.status}",
                        isCancelled = false,
                        color = "#FF5252", // Red for exams
                    ),
                )
            }
        }

        if(calendarStartDate != null && calendarEndDate != null) {
            database.courseEventDao()
                .deleteEventsBetween(calendarStartDate.atStartOfDay(), calendarEndDate.atStartOfDay())
        }

        database.courseEventDao()
            .insertEvents(domainEvents)

        return true
    }
}