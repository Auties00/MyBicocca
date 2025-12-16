package it.attendance100.mybicocca.data.repository

import androidx.lifecycle.LiveData
import it.attendance100.mybicocca.data.local.dao.CourseEventDao
import it.attendance100.mybicocca.data.local.dao.CourseScheduleDao
import it.attendance100.mybicocca.data.local.entity.CourseEvent
import it.attendance100.mybicocca.domain.datasource.CalendarDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton
import it.attendance100.mybicocca.domain.repository.CalendarRepository as ICalendarRepository

/**
 * Implements calendar contract, uses DAOs + datasource
 * Manages calendar data using Clean Architecture: Repository -> DataSource + DAO (local cache)
 *
 * Pattern:
 * - DataSource for data retrieval (mock or API)
 * - DAO for local cache and LiveData observability
 * - Offline-first: data from DB, background sync
 *
 * The DataSource is injected via Hilt:
 * - In development: MockCalendarDataSource
 * - In production: RemoteCalendarDataSource
 */
@Singleton
class CalendarRepository @Inject constructor(
    private val dataSource: CalendarDataSource,
    private val eventDao: CourseEventDao,
    private val scheduleDao: CourseScheduleDao,
) : ICalendarRepository {
    private var isInitialized = false

    /**
     * Observes events for a specific month (reactive LiveData)
     * The DAO automatically emits when data changes
     */
    override fun observeEventsForMonth(month: YearMonth): LiveData<List<CourseEvent>> {
        val startDate = month.atDay(1).atStartOfDay()
        val endDate = month.atEndOfMonth().atTime(23, 59, 59)
        return eventDao.observeEventsBetween(startDate, endDate)
    }

    /**
     * Observes events for a specific date (reactive LiveData)
     */
    override fun observeEventsForDate(date: LocalDate): LiveData<List<CourseEvent>> {
        val startDateTime = date.atStartOfDay()
        val endDateTime = date.atTime(23, 59, 59)
        return eventDao.observeEventsBetween(startDateTime, endDateTime)
    }

    /**
     * Inserisce un nuovo evento.
     */
    override suspend fun insertEvent(event: CourseEvent): Long = withContext(Dispatchers.IO) {
        eventDao.insert(event)
    }

    /**
     * Aggiorna un evento esistente.
     */
    override suspend fun updateEvent(event: CourseEvent) = withContext(Dispatchers.IO) {
        eventDao.update(event)
    }

    /**
     * Elimina un evento.
     */
    override suspend fun deleteEvent(event: CourseEvent) = withContext(Dispatchers.IO) {
        eventDao.delete(event)
    }

    override suspend fun syncData() {
        TODO("Not yet implemented")
    }

    /**
     * Sincronizza dal DataSource (mock o API) e salva in cache locale.
     *
     * Pattern offline-first:
     * 1. Recupera dati dal DataSource
     * 2. Salva nel DB locale (DAO)
     * 3. LiveData si aggiorna automaticamente
     */
    suspend fun syncFromRemote(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Chiama il DataSource (mock o API)
            val success = dataSource.syncEvents()

            if (success && !isInitialized) {
                // Carica eventi iniziali nella cache locale
                val currentMonth = YearMonth.now()
                val nextMonth = currentMonth.plusMonths(1)

                val currentEvents = dataSource.getEventsForMonth(currentMonth)
                val nextEvents = dataSource.getEventsForMonth(nextMonth)

                (currentEvents + nextEvents).forEach { event ->
                    eventDao.insert(event)
                }
                isInitialized = true
            }

            success
        } catch (e: Exception) {
            false
        }
    }
}
