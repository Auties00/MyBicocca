package it.attendance100.mybicocca.data.repository

import androidx.lifecycle.*
import it.attendance100.mybicocca.data.daos.*
import it.attendance100.mybicocca.data.datasources.calendar.*
import it.attendance100.mybicocca.data.entities.*
import kotlinx.coroutines.*
import java.time.*
import javax.inject.*
import it.attendance100.mybicocca.domain.contracts.CalendarRepository as ICalendarRepository

/**
 * Repository per gestire i dati del calendario.
 * Architettura Clean: Repository → DataSource + DAO (cache locale).
 *
 * Pattern:
 * - DataSource per recupero dati (mock o API)
 * - DAO per cache locale e osservabilità LiveData
 * - Offline-first: dati dal DB, sync in background
 *
 * Il DataSource viene iniettato tramite Hilt:
 * - In sviluppo: MockCalendarDataSource
 * - In produzione: RemoteCalendarDataSource
 */
@Singleton
class CalendarRepository @Inject constructor(
  private val dataSource: CalendarDataSource,
  private val eventDao: CourseEventDao,
  private val scheduleDao: CourseScheduleDao,
) : ICalendarRepository {
    private var isInitialized = false

    /**
     * Osserva eventi per un mese specifico (LiveData reattivo).
     * Il DAO emette automaticamente quando i dati cambiano.
     */
    override fun observeEventsForMonth(month: YearMonth): LiveData<List<CourseEvent>> {
        val startDate = month.atDay(1).atStartOfDay()
        val endDate = month.atEndOfMonth().atTime(23, 59, 59)
        return eventDao.observeEventsBetween(startDate, endDate)
    }

    /**
     * Osserva eventi per una data specifica (LiveData reattivo).
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
                val events = dataSource.getEventsForMonth(currentMonth)
                events.forEach { event ->
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
