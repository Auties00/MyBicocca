package it.attendance100.mybicocca.di

import androidx.room.Database
import androidx.room.RoomDatabase
import it.attendance100.mybicocca.data.local.dao.CourseEventDao
import it.attendance100.mybicocca.data.local.dao.CourseScheduleDao
import it.attendance100.mybicocca.data.local.dao.UserDao
import it.attendance100.mybicocca.data.local.entity.CareerStatsEntity
import it.attendance100.mybicocca.data.local.entity.CourseEvent
import it.attendance100.mybicocca.data.local.entity.CourseSchedule
import it.attendance100.mybicocca.data.local.entity.UserEntity

/**
 * Room database configuration
 * The instance is managed by Hilt (see DatabaseModule)
 */
@Database(
    entities = [
        CourseEvent::class,
        CourseSchedule::class,
        UserEntity::class,
        CareerStatsEntity::class,
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseEventDao(): CourseEventDao
    abstract fun courseScheduleDao(): CourseScheduleDao
    abstract fun userDao(): UserDao
}