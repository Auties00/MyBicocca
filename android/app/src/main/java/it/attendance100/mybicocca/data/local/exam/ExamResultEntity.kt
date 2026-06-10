package it.attendance100.mybicocca.data.local.exam

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one published exam outcome in a career's results list. Written through only on
 * a successful live fetch and read back only when the device has no network; acknowledging an
 * outcome stays disabled offline. Mirrors
 * [it.attendance100.mybicocca.domain.model.exam.ExamResult], with the sealed grade encoded as a
 * [gradeKind]/[gradeValue] pair, the acknowledgment status as its enum name, and dates as
 * ISO-8601 strings.
 *
 * @property careerId Owning career (Esse3 stuId); scopes the row so accounts never cross.
 * @property cacheOrder Server list position, preserved so the offline list reads back in order.
 * @property gradeKind Discriminator of the sealed grade (see [BookedExamEntity.gradeKind]).
 */
@Entity(tableName = "cached_exam_result", primaryKeys = ["career_id", "course_of_study_id", "activity_id", "call_id"])
data class ExamResultEntity(
    @ColumnInfo("career_id") val careerId: Long,
    @ColumnInfo("course_of_study_id") val courseOfStudyId: Long,
    @ColumnInfo("activity_id") val activityId: Long,
    @ColumnInfo("call_id") val callId: Int,
    @ColumnInfo("cache_order") val cacheOrder: Int,
    @ColumnInfo("application_list_id") val applicationListId: Long?,
    @ColumnInfo("publication_id") val publicationId: Long?,
    @ColumnInfo("activity_description") val activityDescription: String?,
    @ColumnInfo("exam_date_time") val examDateTime: String?,
    @ColumnInfo("grade_kind") val gradeKind: String,
    @ColumnInfo("grade_value") val gradeValue: Int?,
    val acknowledgment: String,
    @ColumnInfo("published_note") val publishedNote: String?,
    @ColumnInfo("acknowledgment_deadline") val acknowledgmentDeadline: String?,
)
