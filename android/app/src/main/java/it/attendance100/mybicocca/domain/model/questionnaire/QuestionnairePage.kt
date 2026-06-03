package it.attendance100.mybicocca.domain.model.questionnaire

// One page of a questionnaire. Navigation is server-driven: the next/previous page
// depends on the answers saved so far (branching), so pages are always fetched, never
// assumed. A page with isEnd=true is the server's end-of-questionnaire marker.
data class QuestionnairePage(
    val id: Long,
    val isEnd: Boolean,
    val paragraphs: List<QuestionnaireParagraph>,
)

data class QuestionnaireParagraph(
    val id: Long,
    val title: String?,
    val questions: List<QuestionnaireQuestion>,
)

data class QuestionnaireQuestion(
    val id: Long,
    val text: String,
    val note: String?,
    val mandatory: Boolean,
    val kind: QuestionnaireQuestionKind,
    val options: List<QuestionnaireOption>,
    // Answers already saved on the server for this page (present when revisiting).
    val savedOptionIds: Set<Long>,
    val savedFreeText: String?,
)

sealed interface QuestionnaireQuestionKind {
    data object SingleChoice : QuestionnaireQuestionKind

    // Ordered single choice (e.g. agreement 1..10) — same wire format as SingleChoice
    // but rendered as a scale.
    data object Scale : QuestionnaireQuestionKind

    data class MultiChoice(val maxSelections: Int?) : QuestionnaireQuestionKind

    data object FreeText : QuestionnaireQuestionKind
}

data class QuestionnaireOption(
    val id: Long,
    val text: String,
    // True for options like "Altro" that carry an attached free-text field.
    val requiresFreeText: Boolean,
)
