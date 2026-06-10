package it.attendance100.mybicocca.domain.model.questionnaire

import it.attendance100.mybicocca.domain.model.career.CareerId

/**
 * Identifies one in-flight questionnaire compilation. Esse3 never resumes drafts: every
 * start call creates a fresh compilation instance, so this lives only as long as the
 * compilation ViewModel driving the flow — abandoning it has no server-visible effect.
 *
 * @property careerId Career the compilation belongs to; its value is the `stuId` the
 *   questionnaire endpoints address.
 * @property activityChoiceId Esse3 `adsceId` of the activity being evaluated.
 * @property questionnaireId Id of the questionnaire template being compiled.
 * @property questionnaireConfigId Id of the questionnaire configuration being compiled.
 * @property compilationId Server id of this compilation instance, echoed on every page
 *   call.
 * @property userCompilationId Server id of the student's participation in this
 *   compilation, echoed on summary/confirm calls.
 */
data class QuestionnaireSession(
    val careerId: CareerId,
    val activityChoiceId: Long,
    val questionnaireId: Int,
    val questionnaireConfigId: Int,
    val compilationId: Long,
    val userCompilationId: Long,
)

/**
 * Result of starting a compilation: the session identifiers plus the first page the
 * server hands back, so the compilation screen can render without a second round-trip.
 *
 * @property session Identifiers of the freshly created compilation.
 * @property firstPage The first page to render.
 */
data class QuestionnaireCompilationStart(
    val session: QuestionnaireSession,
    val firstPage: QuestionnairePage,
)
