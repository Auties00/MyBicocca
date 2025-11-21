package it.attendance100.mybicocca.data.datasources.calendar

import it.attendance100.mybicocca.data.entities.*
import java.time.*

/**
 * Interfaccia per le sorgenti dati del calendario.
 * Permette di astrarre la provenienza dei dati (mock, API, ecc.)
 */
interface CalendarDataSource {
    /**
     * Recupera eventi per un mese specifico.
     */
    suspend fun getEventsForMonth(month: YearMonth): List<CourseEvent>

    /**
     * Recupera eventi per una data specifica.
     */
    suspend fun getEventsForDate(date: LocalDate): List<CourseEvent>

    /**
     * Sincronizza gli eventi dal server.
     */
    suspend fun syncEvents(): Boolean
}
