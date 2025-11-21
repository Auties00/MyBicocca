package it.attendance100.mybicocca.data.daos

import androidx.lifecycle.*
import androidx.room.*
import it.attendance100.mybicocca.data.entities.*
import java.time.*

/**
 * Data Access Object for calendar events
 */
@Dao
interface CourseEventDao {
  // Methods for DataSource with LocalDateTime
  @Query("SELECT * FROM course_events WHERE start_time >= :startDateTime AND end_time <= :endDateTime ORDER BY start_time")
  suspend fun getEventsBetween(startDateTime: LocalDateTime, endDateTime: LocalDateTime): List<CourseEvent>
  
  @Query("SELECT * FROM course_events WHERE start_time >= :startDateTime AND end_time <= :endDateTime ORDER BY start_time")
  fun observeEventsBetween(startDateTime: LocalDateTime, endDateTime: LocalDateTime): LiveData<List<CourseEvent>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(event: CourseEvent): Long
  
  @Update
  suspend fun update(event: CourseEvent)
  
  @Delete
  suspend fun delete(event: CourseEvent)
}

/**
 * Data Access Object for recurring schedules
 */
@Dao
interface CourseScheduleDao {
  @Query("SELECT * FROM course_schedules ORDER BY day_of_week, start_time")
  suspend fun getAllSchedulesList(): List<CourseSchedule>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(schedule: CourseSchedule): Long
}
