package it.attendance100.mybicocca.data.local.exam

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one row of a career's booked-exams list (Esse3 prenotazioni). Exam booking
 * is live-first, so this table is written through only on a successful fetch and read back only
 * when the device has no network; booking and cancellation stay disabled offline. The row mirrors
 * the [it.attendance100.mybicocca.domain.model.exam.BookedExam] fields one-to-one, with the
 * sealed grade encoded as a [gradeKind]/[gradeValue] pair and dates stored as ISO-8601 strings.
 *
 * @property careerId Owning career (Esse3 stuId); scopes the row so accounts never cross.
 * @property courseOfStudyId Together with [activityId] and [callId], the booked call's key.
 * @property cacheOrder Server list position, preserved so the offline list reads back in the
 *   same order the API returned (most-recent first).
 * @property gradeKind Discriminator of the sealed grade: `numeric`, `passed`, `not_passed`,
 *   `withdrew`, `absent` or `unknown`.
 * @property gradeValue The mark when [gradeKind] is `numeric`, otherwise null.
 */
@Entity(tableName = "cached_booked_exam", primaryKeys = ["career_id", "course_of_study_id", "activity_id", "call_id"])
data class BookedExamEntity(
    @ColumnInfo("career_id") val careerId: Long,
    @ColumnInfo("course_of_study_id") val courseOfStudyId: Long,
    @ColumnInfo("activity_id") val activityId: Long,
    @ColumnInfo("call_id") val callId: Int,
    @ColumnInfo("cache_order") val cacheOrder: Int,
    @ColumnInfo("application_list_id") val applicationListId: Long?,
    @ColumnInfo("student_id") val studentId: Long?,
    @ColumnInfo("activity_choice_id") val activityChoiceId: Long?,
    @ColumnInfo("activity_description") val activityDescription: String?,
    @ColumnInfo("exam_call_description") val examCallDescription: String?,
    @ColumnInfo("exam_type") val examType: String,
    @ColumnInfo("call_type") val callType: String,
    @ColumnInfo("exam_date_time") val examDateTime: String?,
    @ColumnInfo("classroom_description") val classroomDescription: String?,
    @ColumnInfo("building_description") val buildingDescription: String?,
    val credits: Float?,
    @ColumnInfo("exam_mode_description") val examModeDescription: String?,
    val position: Int?,
    @ColumnInfo("total_bookings") val totalBookings: Int? = null,
    @ColumnInfo("booking_date") val bookingDate: String?,
    @ColumnInfo("cancellable_until") val cancellableUntil: String?,
    @ColumnInfo("student_note") val studentNote: String?,
    @ColumnInfo("grade_kind") val gradeKind: String,
    @ColumnInfo("grade_value") val gradeValue: Int?,
    @ColumnInfo("outcome_published") val outcomePublished: Boolean,
    @ColumnInfo("published_note") val publishedNote: String?,
)
