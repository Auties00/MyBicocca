package it.attendance100.mybicocca.domain.model.questionnaire

/**
 * End-of-compilation summary the server computes before the student confirms.
 *
 * @property complete True when every mandatory question is answered and the compilation
 *   can be confirmed.
 */
data class QuestionnaireSummary(
    val complete: Boolean,
)
