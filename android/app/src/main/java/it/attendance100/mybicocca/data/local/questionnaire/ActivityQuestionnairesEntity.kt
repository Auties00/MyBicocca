package it.attendance100.mybicocca.data.local.questionnaire

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one activity's VAL_DID questionnaire header, keyed by career plus the
 * activity it evaluates. Written through only on a successful live fetch and read back only
 * when the device has no network; compilation is never cached. Holds the scalar fields of
 * [it.attendance100.mybicocca.domain.model.questionnaire.ActivityQuestionnaires]; the
 * compilable units live in the child [QuestionnaireUnitEntity] table.
 *
 * @property careerId Owning career (Esse3 stuId); scopes the row so accounts never cross.
 * @property activityChoiceId Esse3 `adsceId` of the libretto row; with [careerId] the key.
 * @property questionnaireId Questionnaire template id; null when the evaluation window is
 *   closed and units cannot be compiled.
 * @property questionnaireConfigId Questionnaire configuration id; null together with
 *   [questionnaireId] when the window is closed.
 */
@Entity(tableName = "cached_activity_questionnaires", primaryKeys = ["career_id", "activity_choice_id"])
data class ActivityQuestionnairesEntity(
    @ColumnInfo("career_id") val careerId: Long,
    @ColumnInfo("activity_choice_id") val activityChoiceId: Long,
    @ColumnInfo("questionnaire_id") val questionnaireId: Int?,
    @ColumnInfo("questionnaire_config_id") val questionnaireConfigId: Int?,
    @ColumnInfo("questionnaire_name") val questionnaireName: String?,
    val anonymous: Boolean,
)
