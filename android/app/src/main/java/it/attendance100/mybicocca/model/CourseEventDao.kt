package it.attendance100.mybicocca.model

import androidx.lifecycle.*
import androidx.room.*
import java.time.*

/**
 * Data Access Object per gli eventi del calendario
 */
@Dao
interface CourseEventDao {
  // Metodi per DataSource con LocalDateTime
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
 * Data Access Object per gli orari ricorrenti
 */
@Dao
interface CourseScheduleDao {
  @Query("SELECT * FROM course_schedules ORDER BY day_of_week, start_time")
  suspend fun getAllSchedulesList(): List<CourseSchedule>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(schedule: CourseSchedule): Long
}
