package it.attendance100.mybicocca.data.calendar

import it.attendance100.mybicocca.model.CourseEvent
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementazione remota del CalendarDataSource.
 * Si connetterà all'API reale quando disponibile.
 * 
 * TODO: Implementare quando l'API sarà pronta
 * - Iniettare Retrofit ApiService
 * - Mappare DTO → Entity
 * - Gestire errori di rete
 */
@Singleton
class RemoteCalendarDataSource @Inject constructor(
    // TODO: @Inject private val calendarApiService: CalendarApiService
) : CalendarDataSource {

    override suspend fun getEventsForMonth(month: YearMonth): List<CourseEvent> {
        // TODO: Implementare chiamata API
        // val response = calendarApiService.getEventsForMonth(month)
        // return response.map { it.toEntity() }
        throw NotImplementedError("RemoteCalendarDataSource non ancora implementato - usa MockCalendarDataSource")
    }

    override suspend fun getEventsForDate(date: LocalDate): List<CourseEvent> {
        // TODO: Implementare chiamata API
        throw NotImplementedError("RemoteCalendarDataSource non ancora implementato - usa MockCalendarDataSource")
    }

    override suspend fun syncEvents(): Boolean {
        // TODO: Implementare sincronizzazione
        throw NotImplementedError("RemoteCalendarDataSource non ancora implementato - usa MockCalendarDataSource")
    }
}
