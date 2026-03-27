package it.attendance100.mybicocca.data.datasource.calendar

import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventSource
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningCalendarDataSource @Inject constructor(
    private val elearningApi: ElearningApi,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getActionEvents(
        range: ClosedRange<LocalDate>,
    ): List<CalendarEvent> = withContext(ioDispatcher) {
        val wsToken = authTokenStore.elearningWsToken ?: return@withContext emptyList()

        val zone = ZoneId.systemDefault()
        val timesortFrom = range.start.atStartOfDay(zone).toEpochSecond()
        val timesortTo = range.endInclusive.plusDays(1).atStartOfDay(zone).toEpochSecond()

        val response = elearningApi.calendar.getActionEventsByTimesort(
            wsToken = wsToken,
            timesortFrom = timesortFrom,
            timesortTo = timesortTo,
            limitNum = 200,
        )

        response.events.map { event ->
            val startInstant = Instant.ofEpochSecond(event.timeStart)
            val startZoned = startInstant.atZone(zone)
            val endInstant = if (event.timeDuration > 0) {
                Instant.ofEpochSecond(event.timeStart + event.timeDuration)
            } else null
            val endZoned = endInstant?.atZone(zone)

            CalendarEvent(
                id = "ELEARNING_${event.id}",
                title = event.name,
                date = startZoned.toLocalDate(),
                startTime = startZoned.toLocalTime(),
                endTime = endZoned?.toLocalTime(),
                location = event.location?.takeIf { it.isNotBlank() },
                source = EventSource.ELEARNING,
                isOverdue = event.overdue ?: false,
                actionUrl = event.action?.url ?: event.url,
                courseId = event.course?.id,
            )
        }.filter { it.date in range }
    }
}
