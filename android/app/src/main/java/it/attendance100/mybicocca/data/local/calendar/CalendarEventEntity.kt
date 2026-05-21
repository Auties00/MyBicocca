package it.attendance100.mybicocca.data.local.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "calendar_events",
    primaryKeys = ["id", "career_id"],
    indices = [
        Index("career_id", "date"),
        Index("career_id", "source", "date"),
        Index("career_id", "date", "start_time"),
    ],
)
data class CalendarEventEntity(
    val id: String,
    @ColumnInfo(name = "career_id") val careerId: Long,
    val source: String,
    val discriminator: String,
    val date: String,
    @ColumnInfo(name = "start_time") val startTime: String,
    @ColumnInfo(name = "end_time") val endTime: String,
    val title: String,
    @ColumnInfo(name = "short_label") val shortLabel: String?,
    val room: String?,
    val building: String?,
    @ColumnInfo(name = "maps_url") val mapsUrl: String?,
    val status: String,
    val notes: String?,
    @ColumnInfo(name = "subject_code") val subjectCode: String?,
    @ColumnInfo(name = "teachers_csv") val teachersCsv: String?,
    val cfu: Int?,
    @ColumnInfo(name = "examiners_csv") val examinersCsv: String?,
    @ColumnInfo(name = "exam_type_label") val examTypeLabel: String?,
)

object CalendarEventDiscriminator {
    const val LESSON = "Lesson"
    const val EXAM = "Exam"
}
