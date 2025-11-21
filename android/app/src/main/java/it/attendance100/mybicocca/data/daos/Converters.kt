package it.attendance100.mybicocca.data.daos

import androidx.room.*
import it.attendance100.mybicocca.data.entities.*
import java.time.*

/**
 * Type converters per Room Database
 * Necessari per gestire i tipi di data LocalDateTime, LocalDate, LocalTime, DayOfWeek
 */
class Converters {
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
}