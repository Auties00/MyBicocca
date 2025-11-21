package it.attendance100.mybicocca.data.entities

import androidx.room.*
import java.time.*

/**
 * Rappresenta un evento del calendario accademico
 */
@Entity(tableName = "course_events")
data class CourseEvent(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "course_name")
  val courseName: String,

  @ColumnInfo(name = "course_code")
  val courseCode: String?,

  @ColumnInfo(name = "professor")
  val professor: String?,

  @ColumnInfo(name = "room")
  val room: String?,

  @ColumnInfo(name = "building")
  val building: String?,

  @ColumnInfo(name = "start_time")
  val startTime: LocalDateTime,

  @ColumnInfo(name = "end_time")
  val endTime: LocalDateTime,

  @ColumnInfo(name = "event_type")
  val eventType: EventType = EventType.LECTURE,

  @ColumnInfo(name = "notes")
  val notes: String? = null,

  @ColumnInfo(name = "is_cancelled")
  val isCancelled: Boolean = false,

  @ColumnInfo(name = "color")
  val color: String? = null
)

/**
 * Tipo di evento accademico
 */
enum class EventType {
  LECTURE,        // Lezione
  LAB,            // Laboratorio
  EXAM,           // Esame
  OFFICE_HOURS,   // Ricevimento
  OTHER           // Altro
}

/**
 * Rappresenta un orario ricorrente del corso
 */
@Entity(tableName = "course_schedules")
data class CourseSchedule(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "course_name")
  val courseName: String,

  @ColumnInfo(name = "course_code")
  val courseCode: String?,

  @ColumnInfo(name = "professor")
  val professor: String?,

  @ColumnInfo(name = "room")
  val room: String?,

  @ColumnInfo(name = "building")
  val building: String?,

  @ColumnInfo(name = "day_of_week")
  val dayOfWeek: DayOfWeek,

  @ColumnInfo(name = "start_time")
  val startTime: LocalTime,

  @ColumnInfo(name = "end_time")
  val endTime: LocalTime,

  @ColumnInfo(name = "event_type")
  val eventType: EventType = EventType.LECTURE,

  @ColumnInfo(name = "valid_from")
  val validFrom: LocalDate,

  @ColumnInfo(name = "valid_to")
  val validTo: LocalDate,

  @ColumnInfo(name = "color")
  val color: String? = null
)
