package it.attendance100.mybicocca.di

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.attendance100.mybicocca.data.dao.CampusDao
import it.attendance100.mybicocca.data.dao.CourseEventDao
import it.attendance100.mybicocca.data.dao.ElearningDao
import it.attendance100.mybicocca.data.dao.RegistryDao
import it.attendance100.mybicocca.data.dao.UserDao
import it.attendance100.mybicocca.domain.model.Alert
import it.attendance100.mybicocca.domain.model.CalendarEvent
import it.attendance100.mybicocca.domain.model.CalendarEventTeacher
import it.attendance100.mybicocca.domain.model.CalendarEventType
import it.attendance100.mybicocca.domain.model.CareerStats
import it.attendance100.mybicocca.domain.model.Conversation
import it.attendance100.mybicocca.domain.model.ElearningCourse
import it.attendance100.mybicocca.domain.model.Exam
import it.attendance100.mybicocca.domain.model.GradePoint
import it.attendance100.mybicocca.domain.model.Internship
import it.attendance100.mybicocca.domain.model.MapLocation
import it.attendance100.mybicocca.domain.model.Message
import it.attendance100.mybicocca.domain.model.Questionnaire
import it.attendance100.mybicocca.domain.model.Tax
import it.attendance100.mybicocca.domain.model.Teacher
import it.attendance100.mybicocca.domain.model.TeacherBuilding
import it.attendance100.mybicocca.domain.model.User
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Room database configuration
 * The instance is managed by Hilt (see DatabaseModule)
 */
@Database(
    entities = [
        Alert::class,
        CalendarEvent::class,
        CalendarEventTeacher::class,
        CareerStats::class,
        Conversation::class,
        ElearningCourse::class,
        Internship::class,
        MapLocation::class,
        Message::class,
        Questionnaire::class,
        Tax::class,
        Teacher::class,
        TeacherBuilding::class,
        User::class
    ],
	version = 3,
	exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseEventDao(): CourseEventDao
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
	fun fromEventType(value: CalendarEventType?): String? {
		return value?.name
	}


	@TypeConverter
	fun toEventType(value: String?): CalendarEventType? {
		return value?.let { CalendarEventType.valueOf(it) }
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
    fun fromTeacherList(value: List<Teacher>?): String? {
        return gson.toJson(value)
    }


    @TypeConverter
    fun toTeacherList(value: String?): List<Teacher>? {
        if (value == null) return null
        val type = object : TypeToken<List<Teacher>>() {}.type
        return gson.fromJson(value, type)
    }

    
    @TypeConverter
    fun fromCalendarEventTeacherList(value: List<CalendarEventTeacher>?): String? {
        return gson.toJson(value)
    }


    @TypeConverter
    fun toCalendarEventTeacherList(value: String?): List<CalendarEventTeacher>? {
        if (value == null) return null
        val type = object : TypeToken<List<CalendarEventTeacher>>() {}.type
        return gson.fromJson(value, type)
    }  
    
    @TypeConverter
    fun fromTeacherBuildingList(value: List<TeacherBuilding>?): String? {
        return gson.toJson(value)
    }


    @TypeConverter
    fun toTeacherBuildingList(value: String?): List<TeacherBuilding>? {
        if (value == null) return null
        val type = object : TypeToken<List<TeacherBuilding>>() {}.type
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