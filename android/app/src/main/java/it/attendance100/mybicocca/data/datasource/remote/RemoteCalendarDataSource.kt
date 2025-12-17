package it.attendance100.mybicocca.data.datasource.remote

import it.attendance100.mybicocca.data.mapper.toCourseEvents
import it.attendance100.mybicocca.data.remote.api.bicoccapp.BicoccappCalendarApi
import it.attendance100.mybicocca.data.local.entity.CourseEvent
import it.attendance100.mybicocca.domain.datasource.CalendarDataSource
import it.attendance100.mybicocca.domain.model.BookingSlot
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class RemoteCalendarDataSource @Inject constructor(
  private val api: BicoccappCalendarApi,
) : CalendarDataSource {

  override suspend fun getEventsForMonth(month: YearMonth): List<CourseEvent> {
    val dateStr = month.atDay(1).format(DateTimeFormatter.ISO_DATE)
    val response = api.getCalendar(date = dateStr)
    if (response.isSuccessful && response.body() != null) {
      val calendarList = response.body()!!.calendar
      return calendarList.toCourseEvents()
    }
    return emptyList()
  }

  override suspend fun getEventsForDate(date: LocalDate): List<CourseEvent> {
    val dateStr = date.format(DateTimeFormatter.ISO_DATE)
    val response = api.getCalendar(date = dateStr)
    if (response.isSuccessful && response.body() != null) {
      val calendarList = response.body()!!.calendar
      return calendarList.toCourseEvents()
    }
    return emptyList()
  }

  override suspend fun syncEvents(): Boolean {
    val response = api.getCalendar()
    return response.isSuccessful
  }

  override suspend fun getBookableSlots(): List<BookingSlot> {
    // BicoccappCalendarApi doesn't seem to have booking slots for appointments.
    // Esse3ExamsApi has getCalendarAppointments.
    // This DataSource might need to use Esse3ExamsApi or returns empty if not supported here.
    return emptyList()
  }
}
