package it.attendance100.mybicocca.domain.model.questionnaire

import it.attendance100.mybicocca.domain.model.career.CareerId

// Identifies one in-flight compilation. Esse3 never resumes drafts: every start call
// creates a fresh compilation instance, so this lives only as long as the ViewModel
// driving the flow — abandoning it has no server-visible effect.
data class QuestionnaireSession(
    val careerId: CareerId,
    val activityChoiceId: Long,
    val questionnaireId: Int,
    val questionnaireConfigId: Int,
    val compilationId: Long,
    val userCompilationId: Long,
)

data class QuestionnaireCompilationStart(
    val session: QuestionnaireSession,
    val firstPage: QuestionnairePage,
)
