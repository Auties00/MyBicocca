package it.attendance100.mybicocca.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.attendance100.mybicocca.domain.model.CalendarEvent
import java.time.LocalDateTime

@Dao
interface CourseEventDao {
    @Query("SELECT * FROM calendar_event WHERE start_time >= :startDate AND end_time <= :endDate ORDER BY start_time")
    suspend fun getEventsBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<CalendarEvent>

    @Query("SELECT * FROM calendar_event WHERE start_time >= :startDate AND end_time <= :endDate ORDER BY start_time")
    fun observeEventsBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): LiveData<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(
        events: List<CalendarEvent>
    )

    @Query("DELETE FROM calendar_event WHERE start_time >= :startDate AND end_time <= :endDate")
    suspend fun deleteEventsBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    )
}