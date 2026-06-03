package it.attendance100.mybicocca.domain.model.questionnaire

// The evaluation questionnaires of a single activity, one per teaching unit / partition.
// questionnaireId and configId come from the server config and are null when the
// evaluation window is closed — units can't be compiled in that case.
data class ActivityQuestionnaires(
    val activityChoiceId: Long,
    val questionnaireId: Int?,
    val questionnaireConfigId: Int?,
    val questionnaireName: String?,
    val anonymous: Boolean,
    val units: List<QuestionnaireUnit>,
)

// One compilable questionnaire target: a teaching unit taught by a lecturer in a
// specific partition (turno). `tags` is the opaque server token that identifies this
// exact combination when starting a compilation.
data class QuestionnaireUnit(
    val teachingUnitName: String,
    val lecturerName: String?,
    val partitionName: String?,
    val completed: Boolean,
    val tags: String,
)
