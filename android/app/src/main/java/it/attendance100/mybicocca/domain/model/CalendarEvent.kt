package it.attendance100.mybicocca.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "calendar_event")
data class CalendarEvent(
    @PrimaryKey
    @ColumnInfo(name = "code")
    val id: String,

    @ColumnInfo(name = "name")
	val name: String?,

    @ColumnInfo(name = "teachers")
	val teachers: List<CalendarEventTeacher>,

    @ColumnInfo(name = "room")
	val room: String?,

    @ColumnInfo(name = "building")
	val building: String?,

    @ColumnInfo(name = "start_time")
    val startTime: LocalDateTime,

    @ColumnInfo(name = "end_time")
    val endTime: LocalDateTime,

    @ColumnInfo(name = "type")
	val type: CalendarEventType = CalendarEventType.LECTURE,

    @ColumnInfo(name = "notes")
	val notes: String? = null,

    @ColumnInfo(name = "cancelled")
	val isCancelled: Boolean = false,

    @ColumnInfo(name = "color")
	val color: String? = null,
)