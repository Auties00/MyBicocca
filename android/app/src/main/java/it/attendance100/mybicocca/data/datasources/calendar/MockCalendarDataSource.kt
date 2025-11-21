package it.attendance100.mybicocca.data.datasources.calendar

import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.data.mocks.*
import kotlinx.coroutines.*
import java.time.*
import javax.inject.*

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
          mockData = CalendarMockData.getSampleEvents()
        }
    }
}
