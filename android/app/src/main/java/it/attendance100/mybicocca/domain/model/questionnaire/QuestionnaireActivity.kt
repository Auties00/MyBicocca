package it.attendance100.mybicocca.domain.model.questionnaire

// A record-book activity that has evaluation questionnaires attached.
data class QuestionnaireActivity(
    val activityChoiceId: Long,
    val activityCode: String?,
    val activityName: String,
    val credits: Float,
    val courseYear: Int,
    val attendanceYear: Int?,
    val status: QuestionnaireActivityStatus,
)

enum class QuestionnaireActivityStatus {
    Completed,
    PartiallyCompleted,
    ToCompile,
    ConfigurationError,
}
