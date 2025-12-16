package it.attendance100.mybicocca.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import it.attendance100.mybicocca.data.local.entity.CourseEvent
import java.time.LocalDateTime

/**
 * Data Access Object for calendar events
 */
@Dao
interface CourseEventDao {
    // Methods for DataSource with LocalDateTime
    @Query("SELECT * FROM course_events WHERE start_time >= :startDateTime AND end_time <= :endDateTime ORDER BY start_time")
    suspend fun getEventsBetween(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): List<CourseEvent>

    @Query("SELECT * FROM course_events WHERE start_time >= :startDateTime AND end_time <= :endDateTime ORDER BY start_time")
    fun observeEventsBetween(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): LiveData<List<CourseEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CourseEvent): Long

    @Update
    suspend fun update(event: CourseEvent)

    @Delete
    suspend fun delete(event: CourseEvent)
}