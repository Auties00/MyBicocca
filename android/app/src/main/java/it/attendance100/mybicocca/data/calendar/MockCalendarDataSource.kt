package it.attendance100.mybicocca.data.calendar

import it.attendance100.mybicocca.model.CourseEvent
import it.attendance100.mybicocca.utils.CalendarTestData
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementazione mock del CalendarDataSource.
 * Fornisce dati di test per lo sviluppo.
 */
@Singleton
class MockCalendarDataSource @Inject constructor() : CalendarDataSource {

    private var mockData: List<CourseEvent>? = null

    override suspend fun getEventsForMonth(month: YearMonth): List<CourseEvent> {
        // Simula latenza di rete
        delay(300)
        
        ensureMockDataLoaded()
        
        val startDate = month.atDay(1)
        val endDate = month.atEndOfMonth()
        
        return mockData?.filter { event ->
            val eventDate = event.startTime.toLocalDate()
            !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate)
        } ?: emptyList()
    }

    override suspend fun getEventsForDate(date: LocalDate): List<CourseEvent> {
        // Simula latenza di rete
        delay(300)
        
        ensureMockDataLoaded()
        
        return mockData?.filter { event ->
            event.startTime.toLocalDate() == date
        } ?: emptyList()
    }

    override suspend fun syncEvents(): Boolean {
        // Simula sincronizzazione dal server
        delay(1000)
        ensureMockDataLoaded()
        return true
    }

    private fun ensureMockDataLoaded() {
        if (mockData == null) {
            mockData = CalendarTestData.getSampleEvents()
        }
    }
}
