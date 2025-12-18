package it.attendance100.mybicocca.di

import androidx.room.*
import com.google.gson.*
import com.google.gson.reflect.*
import it.attendance100.mybicocca.data.dao.*
import it.attendance100.mybicocca.domain.model.*
import java.time.*

/**
 * Room database configuration
 * The instance is managed by Hilt (see DatabaseModule)
 */
@Database(
	entities = [
		CourseEvent::class,
		CourseSchedule::class,
		User::class,
		CareerStats::class,
		ElearningCourse::class,
		Teacher::class,
		MapLocation::class,
		Tax::class,
		Alert::class,
		Internship::class,
		Questionnaire::class,
		Conversation::class,
		Message::class,
	],
	version = 3,
	exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseEventDao(): CourseEventDao
    abstract fun courseScheduleDao(): CourseScheduleDao
    abstract fun userDao(): UserDao
	abstract fun campusDao(): CampusDao
	abstract fun elearningDao(): ElearningDao
	abstract fun registryDao(): RegistryDao
}

class Converters {
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
	fun fromExamList(value: List<Exam>?): String? {
		return gson.toJson(value)
	}


	@TypeConverter
	fun toExamList(value: String?): List<Exam>? {
		if (value == null) return null
		val type = object : TypeToken<List<Exam>>() {}.type
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
	fun fromStringList(value: List<String>?): String? {
		return gson.toJson(value)
	}


	@TypeConverter
	fun toStringList(value: String?): List<String>? {
		if (value == null) return null
		val type = object : TypeToken<List<String>>() {}.type
		return gson.fromJson(value, type)
	}


	@TypeConverter
	fun fromInstant(value: Instant?): Long? {
		return value?.toEpochMilli()
	}


	@TypeConverter
	fun toInstant(value: Long?): Instant? {
		return value?.let { Instant.ofEpochMilli(it) }
	}
}