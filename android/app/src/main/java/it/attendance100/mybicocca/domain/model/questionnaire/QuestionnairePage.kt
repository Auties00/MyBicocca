package it.attendance100.mybicocca.domain.model.questionnaire

/**
 * One page of a VAL_DID questionnaire, rendered by the compilation sub-screen. Page
 * navigation is server-driven: the next/previous page depends on the answers saved so
 * far (branching), so pages are always fetched, never assumed.
 *
 * @property id Server page id; pages with a negative id are end markers.
 * @property isEnd True for the server's end-of-questionnaire marker page.
 * @property paragraphs Question groups in display order.
 */
data class QuestionnairePage(
    val id: Long,
    val isEnd: Boolean,
    val paragraphs: List<QuestionnaireParagraph>,
)

/**
 * A titled group of questions inside a questionnaire page.
 *
 * @property id Server id of the paragraph.
 * @property title Heading shown above the group, when authored.
 * @property questions The paragraph's questions in display order.
 */
data class QuestionnaireParagraph(
    val id: Long,
    val title: String?,
    val questions: List<QuestionnaireQuestion>,
)

/**
 * One question of a questionnaire page.
 *
 * @property id Server id of the question, echoed back when saving answers.
 * @property text The question prompt.
 * @property note Auxiliary note authored alongside the prompt.
 * @property mandatory True when the question must be answered before confirming.
 * @property kind How the question is answered and rendered.
 * @property options The answer options, empty for pure free-text questions.
 * @property savedOptionIds Option ids already saved on the server for this page
 *   (present when revisiting).
 * @property savedFreeText Free text already saved on the server for this page.
 */
data class QuestionnaireQuestion(
    val id: Long,
    val text: String,
    val note: String?,
    val mandatory: Boolean,
    val kind: QuestionnaireQuestionKind,
    val options: List<QuestionnaireOption>,
    val savedOptionIds: Set<Long>,
    val savedFreeText: String?,
)

/**
 * How a questionnaire question is answered, decoded from Esse3 format codes. Unknown
 * formats degrade to [SingleChoice] or [FreeText] so every question stays answerable.
 */
sealed interface QuestionnaireQuestionKind {
    data object SingleChoice : QuestionnaireQuestionKind

    /**
     * Ordered single choice (e.g. agreement 1..10) — same wire format as [SingleChoice]
     * but rendered as a scale.
     */
    data object Scale : QuestionnaireQuestionKind

    /**
     * Multiple selection.
     *
     * @property maxSelections Upper bound on picks; null when the server sets none.
     */
    data class MultiChoice(val maxSelections: Int?) : QuestionnaireQuestionKind

    data object FreeText : QuestionnaireQuestionKind
}

/**
 * One answer option of a questionnaire question.
 *
 * @property id Server id of the option, sent back as the chosen answer.
 * @property text Label shown to the student.
 * @property requiresFreeText True for options like "Altro" that carry an attached
 *   free-text field.
 */
data class QuestionnaireOption(
    val id: Long,
    val text: String,
    val requiresFreeText: Boolean,
)
