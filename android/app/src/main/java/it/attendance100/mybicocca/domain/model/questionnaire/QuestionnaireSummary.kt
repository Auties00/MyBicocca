package it.attendance100.mybicocca.domain.model.questionnaire

data class QuestionnaireSummary(
    // True when every mandatory question is answered and the compilation can be confirmed.
    val complete: Boolean,
)
