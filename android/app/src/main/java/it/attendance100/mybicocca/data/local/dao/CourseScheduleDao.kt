package it.attendance100.mybicocca.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.attendance100.mybicocca.data.local.entity.CourseSchedule

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
