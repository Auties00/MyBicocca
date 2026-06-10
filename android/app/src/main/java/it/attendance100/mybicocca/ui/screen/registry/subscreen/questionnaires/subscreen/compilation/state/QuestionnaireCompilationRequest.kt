package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state

/**
 * Everything the compiler needs to start the server-side session, carried over from the
 * units page so the activity's questionnaire config is never re-fetched. The display
 * fields feed the hosting sheet's pinned header.
 */
data class QuestionnaireCompilationRequest(
    val activityChoiceId: Long,
    val questionnaireId: Int,
    val questionnaireConfigId: Int,
    val anonymous: Boolean,
    val tags: String,
    val activityName: String,
    val lecturerName: String? = null,
    val partitionName: String? = null,
)
