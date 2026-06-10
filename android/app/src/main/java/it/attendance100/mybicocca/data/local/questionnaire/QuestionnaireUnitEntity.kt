package it.attendance100.mybicocca.data.local.questionnaire

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one compilable teaching-unit target belonging to a cached
 * [ActivityQuestionnairesEntity]. Written through and read back with its parent under the same
 * no-network policy; the [tags] token is preserved so the offline list keeps the same shape the
 * server returned, even though compilation itself stays disabled offline. Mirrors
 * [it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireUnit].
 *
 * @property careerId Owning career (Esse3 stuId); with [activityChoiceId] scopes the unit to its
 *   parent header row.
 * @property activityChoiceId Esse3 `adsceId` of the libretto row the parent evaluates.
 * @property unitOrder Position within the parent's unit list, preserved so the offline list reads
 *   back in the same order the API returned; part of the key.
 * @property tags Opaque Esse3 `tagsValdid` token identifying the unit/lecturer/partition combination.
 */
@Entity(
    tableName = "cached_questionnaire_unit",
    primaryKeys = ["career_id", "activity_choice_id", "unit_order"],
)
data class QuestionnaireUnitEntity(
    @ColumnInfo("career_id") val careerId: Long,
    @ColumnInfo("activity_choice_id") val activityChoiceId: Long,
    @ColumnInfo("unit_order") val unitOrder: Int,
    @ColumnInfo("teaching_unit_name") val teachingUnitName: String,
    @ColumnInfo("lecturer_name") val lecturerName: String?,
    @ColumnInfo("partition_name") val partitionName: String?,
    val completed: Boolean,
    val tags: String,
)
