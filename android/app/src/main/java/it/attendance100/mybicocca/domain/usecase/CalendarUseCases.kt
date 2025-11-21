package it.attendance100.mybicocca.domain.usecase

import androidx.lifecycle.LiveData
import it.attendance100.mybicocca.model.CourseEvent
import it.attendance100.mybicocca.repository.CalendarRepository
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * Raccoglie tutti gli Use Cases per il Calendario in un unico file.
 * Approccio pragmatico per evitare troppi file per operazioni CRUD semplici.
 * 
 * Quando aggiungere nuovi Use Cases:
 * - Se la logica è complessa (es. calcoli business, validazioni)
 * - Se combinano multiple operazioni repository
 * - Per operazioni riutilizzabili in più punti
 */

/**
 * Ottiene eventi per una data specifica come LiveData (osservabile).
 */
class GetEventsForDateUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    operator fun invoke(date: LocalDate): LiveData<List<CourseEvent>> {
        return repository.observeEventsForDate(date)
    }
}

/**
 * Ottiene eventi per un mese specifico come LiveData (osservabile).
 */
class GetEventsForMonthUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    operator fun invoke(month: YearMonth): LiveData<List<CourseEvent>> {
        return repository.observeEventsForMonth(month)
    }
}

/**
 * Sincronizza il calendario dal server remoto (o mock data).
 * Ritorna true se sync ha successo, false altrimenti.
 */
class SyncCalendarUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.syncFromRemote()
    }
}

/**
 * Inserisce un nuovo evento nel calendario.
 */
class InsertEventUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    suspend operator fun invoke(event: CourseEvent): Long {
        return repository.insertEvent(event)
    }
}

/**
 * Aggiorna un evento esistente.
 */
class UpdateEventUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    suspend operator fun invoke(event: CourseEvent) {
        repository.updateEvent(event)
    }
}

/**
 * Elimina un evento dal calendario.
 */
class DeleteEventUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    suspend operator fun invoke(event: CourseEvent) {
        repository.deleteEvent(event)
    }
}
