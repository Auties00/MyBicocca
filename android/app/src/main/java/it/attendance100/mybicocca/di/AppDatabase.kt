package it.attendance100.mybicocca.di

import androidx.room.*
import it.attendance100.mybicocca.data.daos.*
import it.attendance100.mybicocca.data.entities.*

/**
 * Room database configuration
 * The instance is managed by Hilt (see DatabaseModule)
 */
@Database(
  entities = [CourseEvent::class, CourseSchedule::class],
  version = 1,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun courseEventDao(): CourseEventDao
  abstract fun courseScheduleDao(): CourseScheduleDao
}