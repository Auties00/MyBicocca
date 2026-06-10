package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state

/**
 * In-progress edits for one question on the current page. [freeText] belongs either to a
 * FreeText question's single option or to a selected option flagged requiresFreeText.
 */
data class QuestionAnswerState(
    val selectedOptionIds: Set<Long> = emptySet(),
    val freeText: String = "",
)
