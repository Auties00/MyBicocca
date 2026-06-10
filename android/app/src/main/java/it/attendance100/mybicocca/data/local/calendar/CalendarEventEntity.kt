package it.attendance100.mybicocca.data.local.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Room row of the calendar's materialized events. Only EasyStaff lessons and Esse3 exam
 * bookings are stored here; deadline, appointment and library events stay in their own
 * feature stores and are merged in at observe time.
 *
 * Composite primary key (id, career_id): every row belongs to exactly one career and all
 * queries are career-scoped. The [source] column tags the originating feed so a sync can
 * splice-replace one source's rows without touching the others, and [discriminator] picks
 * the domain subtype the row rehydrates into. Date and time columns hold ISO-8601 strings,
 * whose lexicographic order matches chronological order — the range queries and sort clauses
 * depend on that.
 *
 * Columns past [activityCode] are subtype extras: [subjectCode], [teachersCsv] and [cfu]
 * are populated for lesson rows only, [examTypeLabel] through [cancellableUntil] for exam
 * rows only; the inapplicable group is null.
 *
 * @property id Source-namespaced event id ("<source code>_<native id>").
 * @property careerId Row id of the owning career in the careers table.
 * @property source Code of the originating feed, matching the domain source codes.
 * @property discriminator Domain subtype selector; unknown values drop the row at read time.
 * @property date ISO-8601 local date (yyyy-MM-dd).
 * @property startTime ISO-8601 local time.
 * @property endTime ISO-8601 local time.
 * @property shortLabel Compact label precomputed at mapping time for dense month cells.
 * @property teachersCsv Teacher names joined with newline separators; null when empty.
 * @property bookedAt ISO-8601 local date-time of the exam booking; exam rows only.
 * @property cancellableUntil ISO-8601 local date; exam rows only.
 */
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
    @ColumnInfo(name = "activity_code") val activityCode: String?,
    @ColumnInfo(name = "subject_code") val subjectCode: String?,
    @ColumnInfo(name = "teachers_csv") val teachersCsv: String?,
    val cfu: Int?,
    @ColumnInfo(name = "exam_type_label") val examTypeLabel: String?,
    @ColumnInfo(name = "booking_position") val bookingPosition: Int?,
    @ColumnInfo(name = "booked_at") val bookedAt: String?,
    @ColumnInfo(name = "cancellable_until") val cancellableUntil: String?,
)

/**
 * Values of [CalendarEventEntity.discriminator] — one per materialized subtype; the
 * merge-at-observe sources never produce rows, so they have no discriminator.
 */
object CalendarEventDiscriminator {
    const val LESSON = "Lesson"
    const val EXAM = "Exam"
}
