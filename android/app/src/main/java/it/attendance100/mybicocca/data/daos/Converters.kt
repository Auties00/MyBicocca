package it.attendance100.mybicocca.data.daos

import androidx.room.*
import com.google.gson.*
import com.google.gson.reflect.*
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.domain.model.*
import java.time.*

/**
 * Type converters per Room Database
 * Necessari per gestire i tipi di data LocalDateTime, LocalDate, LocalTime, DayOfWeek
 */
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