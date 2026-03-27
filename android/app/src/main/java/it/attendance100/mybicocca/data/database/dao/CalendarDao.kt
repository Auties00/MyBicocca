package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventSource
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events WHERE date >= :start AND date <= :end ORDER BY date, startTime")
    fun observeInRange(start: LocalDate, end: LocalDate): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE date >= :start ORDER BY date, startTime")
    fun observeFrom(start: LocalDate): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE source = :source ORDER BY date, startTime")
    fun observeBySource(source: EventSource): Flow<List<CalendarEvent>>

    @Upsert
    suspend fun upsertAll(events: List<CalendarEvent>)

    @Query("DELETE FROM calendar_events WHERE source = :source AND date >= :start AND date <= :end")
    suspend fun deleteBySourceAndRange(source: EventSource, start: LocalDate, end: LocalDate)
}
