package it.attendance100.mybicocca.data.local.exam

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one bookable/future exam call in a career's booking calendar. Written through
 * only on a successful live fetch and read back only when the device has no network; the Prenota
 * action stays disabled offline. Mirrors [it.attendance100.mybicocca.domain.model.exam.ExamCall],
 * flattening the nested president ([presidentId]/[presidentName]/[presidentSurname]) and
 * enrollment window ([windowOpensAt]/[windowClosesAt]); dates and times are ISO-8601 strings.
 *
 * @property careerId Owning career (Esse3 stuId); scopes the row so accounts never cross.
 * @property courseOfStudyId Together with [activityId] and [callId], the call's key.
 * @property cacheOrder Server list position, preserved so the offline list reads back in the
 *   same order the API returned.
 */
@Entity(tableName = "cached_exam_call", primaryKeys = ["career_id", "course_of_study_id", "activity_id", "call_id"])
data class ExamCallEntity(
    @ColumnInfo("career_id") val careerId: Long,
    @ColumnInfo("course_of_study_id") val courseOfStudyId: Long,
    @ColumnInfo("activity_id") val activityId: Long,
    @ColumnInfo("call_id") val callId: Int,
    @ColumnInfo("cache_order") val cacheOrder: Int,
    @ColumnInfo("exam_call_id") val examCallId: Long?,
    @ColumnInfo("activity_choice_id") val activityChoiceId: Long?,
    @ColumnInfo("activity_code") val activityCode: String?,
    @ColumnInfo("activity_description") val activityDescription: String?,
    @ColumnInfo("course_of_study_description") val courseOfStudyDescription: String?,
    @ColumnInfo("call_description") val callDescription: String?,
    @ColumnInfo("call_date") val callDate: String?,
    @ColumnInfo("call_time") val callTime: String?,
    @ColumnInfo("window_opens_at") val windowOpensAt: String?,
    @ColumnInfo("window_closes_at") val windowClosesAt: String?,
    @ColumnInfo("enrolled_number") val enrolledNumber: Int?,
    val state: String?,
    @ColumnInfo("state_description") val stateDescription: String?,
    @ColumnInfo("call_type") val callType: String,
    @ColumnInfo("exam_type") val examType: String,
    @ColumnInfo("is_reserved") val isReserved: Boolean,
    @ColumnInfo("mat_id") val matId: Long?,
    val notes: String?,
    @ColumnInfo("president_id") val presidentId: Long?,
    @ColumnInfo("president_name") val presidentName: String?,
    @ColumnInfo("president_surname") val presidentSurname: String?,
    @ColumnInfo("booking_type_description") val bookingTypeDescription: String?,
)
