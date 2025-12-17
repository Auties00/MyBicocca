package it.attendance100.mybicocca.di

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.attendance100.mybicocca.data.local.dao.CourseEventDao
import it.attendance100.mybicocca.data.local.dao.CourseScheduleDao
import it.attendance100.mybicocca.data.local.dao.UserDao
import it.attendance100.mybicocca.data.local.entity.CareerStatsEntity
import it.attendance100.mybicocca.data.local.entity.CourseEvent
import it.attendance100.mybicocca.data.local.entity.CourseSchedule
import it.attendance100.mybicocca.data.local.entity.EventType
import it.attendance100.mybicocca.data.local.entity.UserEntity
import it.attendance100.mybicocca.domain.model.Exam
import it.attendance100.mybicocca.domain.model.GradePoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseEventDao(): CourseEventDao
    abstract fun courseScheduleDao(): CourseScheduleDao
    abstract fun userDao(): UserDao
}

private class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }

    @TypeConverter
    fun fromDayOfWeek(value: DayOfWeek?): Int? {
        return value?.value
    }

    @TypeConverter
    fun toDayOfWeek(value: Int?): DayOfWeek? {
        return value?.let { DayOfWeek.of(it) }
    }

    @TypeConverter
    fun fromEventType(value: EventType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toEventType(value: String?): EventType? {
        return value?.let { EventType.valueOf(it) }
    }

    @TypeConverter
    fun fromFloatList(value: List<Float>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toFloatList(value: String?): List<Float>? {
        if (value == null) return null
        val type = object : TypeToken<List<Float>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromGradePointList(value: List<GradePoint>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toGradePointList(value: String?): List<GradePoint>? {
        if (value == null) return null
        val type = object : TypeToken<List<GradePoint>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromExamList(value: List<Exam>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toExamList(value: String?): List<Exam>? {
        if (value == null) return null
        val type = object : TypeToken<List<Exam>>() {}.type
        return gson.fromJson(value, type)
    }
}