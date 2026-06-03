package it.attendance100.mybicocca.domain.model.questionnaire

// Everything needed to start one compilation: the activity, the server-side
// questionnaire config and the unit tags. Built from ActivityQuestionnaires + the
// chosen QuestionnaireUnit; plain values so it can travel through navigation args.
data class QuestionnaireTarget(
    val activityChoiceId: Long,
    val questionnaireId: Int,
    val questionnaireConfigId: Int,
    val tags: String,
)
