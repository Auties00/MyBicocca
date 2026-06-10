package it.attendance100.mybicocca.domain.model.questionnaire

/**
 * One answer the student gives on a questionnaire page. Even pure free-text questions
 * carry an [optionId]: Esse3 models the text field itself as an answer option, with the
 * typed text in [freeText].
 *
 * @property questionId Id of the question being answered.
 * @property optionId Id of the chosen answer option (or of the text-field option for
 *   free-text questions).
 * @property freeText Text typed by the student; empty for plain choice answers, and the
 *   save call always sends it because Esse3 rejects answers without a response body.
 */
data class QuestionnaireAnswer(
    val questionId: Long,
    val optionId: Long,
    val freeText: String = "",
)
