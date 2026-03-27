package it.attendance100.mybicocca.data.datasource.calendar

import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventSource
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3CalendarDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    suspend fun getAppointmentEvents(
        range: ClosedRange<LocalDate>,
    ): List<CalendarEvent> = withContext(ioDispatcher) {
        val personId = authTokenStore.esse3PersonId
        if (personId == -1L) return@withContext emptyList()

        val calendarTypes = esse3Api.appointmentsCalendar.getCalendarList(
            calendarContext = "STUDENTE",
        )

        calendarTypes.flatMap { calType ->
            val typeCode = calType.calendarCallTypeCode ?: return@flatMap emptyList()
            val shifts = esse3Api.appointmentsCalendar.getShiftsList(
                calendarTypeCode = typeCode,
            )
            shifts.mapNotNull { shift ->
                val shiftId = shift.calendarShiftId ?: return@mapNotNull null
                val date = shift.day?.let { parseDate(it) } ?: return@mapNotNull null
                if (date !in range) return@mapNotNull null

                CalendarEvent(
                    id = "ESSE3_${shiftId}",
                    title = shift.calendarCallDescription
                        ?: calType.calendarCallTypeDescription
                        ?: "",
                    date = date,
                    startTime = shift.shiftStartDate?.let { parseTime(it) },
                    endTime = shift.shiftEndDate?.let { parseTime(it) },
                    location = shift.siteDescription,
                    source = EventSource.ESSE3,
                )
            }
        }
    }

    private fun parseDate(value: String): LocalDate? = runCatching {
        LocalDate.parse(value.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
    }.recoverCatching {
        LocalDate.parse(value.take(10), dateFormatter)
    }.getOrNull()

    private fun parseTime(value: String): LocalTime? = runCatching {
        if (value.length >= 16) LocalTime.parse(value.substring(11, 16))
        else null
    }.getOrNull()
}
