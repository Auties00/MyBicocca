package it.attendance100.mybicocca.domain.model.questionnaire

// Even pure free-text questions carry an optionId: Esse3 models the text field itself
// as an answer option, with the typed text in freeText.
data class QuestionnaireAnswer(
    val questionId: Long,
    val optionId: Long,
    val freeText: String = "",
)
