package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state

// Renderable form of a Moodle question, parsed out of the raw HTML the WS returns.
// Field names are the literal `q{uba}:{slot}_...` input names the save/process WS
// calls expect back; `baseFields` carries the hidden inputs (sequencecheck,
// answerformat) that MUST accompany every answer or Moodle rejects the submission
// as out-of-sequence.
sealed interface QuestionUiModel {
    val slot: Int
    val promptHtml: String
    val baseFields: Map<String, String>

    // One radio group (multichoice single, truefalse, calculated multi).
    data class SingleChoice(
        override val slot: Int,
        override val promptHtml: String,
        override val baseFields: Map<String, String>,
        val fieldName: String,
        val options: List<ChoiceOption>,
    ) : QuestionUiModel

    // Checkbox multichoice: every option posts its own `_choiceN` field as 1/0.
    data class MultiChoice(
        override val slot: Int,
        override val promptHtml: String,
        override val baseFields: Map<String, String>,
        val options: List<ChoiceOption>,
    ) : QuestionUiModel

    // Free-text line (shortanswer, numerical, calculated).
    data class TextEntry(
        override val slot: Int,
        override val promptHtml: String,
        override val baseFields: Map<String, String>,
        val fieldName: String,
        val initialValue: String,
        val numeric: Boolean,
    ) : QuestionUiModel

    data class Essay(
        override val slot: Int,
        override val promptHtml: String,
        override val baseFields: Map<String, String>,
        val fieldName: String,
        val initialValue: String,
    ) : QuestionUiModel

    // Cloze (multianswer): the question text with inline gaps, linearized into
    // text chunks interleaved with input/menu segments in document order.
    data class Cloze(
        override val slot: Int,
        override val promptHtml: String,
        override val baseFields: Map<String, String>,
        val segments: List<ClozeSegment>,
    ) : QuestionUiModel

    // Question types the app can't answer natively (drag-drop, matching, ...).
    // The formulation still renders read-only.
    data class Unsupported(
        override val slot: Int,
        override val promptHtml: String,
        override val baseFields: Map<String, String>,
        val type: String,
    ) : QuestionUiModel
}

data class ChoiceOption(
    val fieldName: String,
    val value: String,
    val labelHtml: String,
    val initiallySelected: Boolean,
    // Verdict the review HTML paints on this row, when review is allowed.
    val reviewMark: ReviewMark?,
)

sealed interface ClozeSegment {
    data class TextChunk(val html: String) : ClozeSegment
    data class TextGap(
        val fieldName: String,
        val initialValue: String,
        val reviewMark: ReviewMark?,
    ) : ClozeSegment

    data class MenuGap(
        val fieldName: String,
        val options: List<MenuOption>,
        val initialValue: String,
        val reviewMark: ReviewMark?,
    ) : ClozeSegment
}

data class MenuOption(val value: String, val label: String)

enum class ReviewMark { Correct, Incorrect }

// A question plus the review-only fragments lifted out of its HTML.
data class ParsedQuestion(
    val model: QuestionUiModel,
    val rightAnswerHtml: String?,
    val feedbackHtml: String?,
)
