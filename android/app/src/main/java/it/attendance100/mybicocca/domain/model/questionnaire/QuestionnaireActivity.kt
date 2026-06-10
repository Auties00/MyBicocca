package it.attendance100.mybicocca.domain.model.questionnaire

/**
 * A libretto activity that has VAL_DID evaluation questionnaires attached, listed by the
 * questionnaires sub-screen in the registry tab so the student can pick which course to
 * evaluate.
 *
 * @property activityChoiceId Esse3 `adsceId` of the libretto row.
 * @property activityCode University activity code, e.g. "E3101Q123".
 * @property activityName Name of the teaching activity.
 * @property credits CFU weight of the activity.
 * @property courseYear Year of the course the activity sits in.
 * @property attendanceYear Academic year the student attended the activity.
 * @property status Aggregate compilation state across the activity's units.
 */
data class QuestionnaireActivity(
    val activityChoiceId: Long,
    val activityCode: String?,
    val activityName: String,
    val credits: Float,
    val courseYear: Int,
    val attendanceYear: Int?,
    val status: QuestionnaireActivityStatus,
)

/**
 * Aggregate compilation state Esse3 reports per activity (`linkState`): 1 = [Completed],
 * 2 = [PartiallyCompleted], 3 = [ToCompile], 4 = [ConfigurationError] (server-side
 * misconfiguration, not compilable).
 */
enum class QuestionnaireActivityStatus {
    Completed,
    PartiallyCompleted,
    ToCompile,
    ConfigurationError,
}
