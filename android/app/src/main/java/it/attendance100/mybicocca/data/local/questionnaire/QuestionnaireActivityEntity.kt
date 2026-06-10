package it.attendance100.mybicocca.data.local.questionnaire

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one row of a career's VAL_DID questionnaire-activities list. The
 * questionnaire lists are live-first, so this table is written through only on a successful
 * fetch and read back only when the device has no network; the compilation flow stays
 * disabled offline and is never cached. The row mirrors the
 * [it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity] fields
 * one-to-one, with the aggregate status encoded by its enum name.
 *
 * @property careerId Owning career (Esse3 stuId); scopes the row so accounts never cross.
 * @property activityChoiceId Esse3 `adsceId` of the libretto row; with [careerId] the key.
 * @property cacheOrder Server list position, preserved so the offline list reads back in the
 *   same order the API returned.
 * @property status Aggregate compilation state by enum name; an unknown value reads back as
 *   [it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus.ConfigurationError].
 */
@Entity(tableName = "cached_questionnaire_activity", primaryKeys = ["career_id", "activity_choice_id"])
data class QuestionnaireActivityEntity(
    @ColumnInfo("career_id") val careerId: Long,
    @ColumnInfo("activity_choice_id") val activityChoiceId: Long,
    @ColumnInfo("cache_order") val cacheOrder: Int,
    @ColumnInfo("activity_code") val activityCode: String?,
    @ColumnInfo("activity_name") val activityName: String,
    val credits: Float,
    @ColumnInfo("course_year") val courseYear: Int,
    @ColumnInfo("attendance_year") val attendanceYear: Int?,
    val status: String,
)
