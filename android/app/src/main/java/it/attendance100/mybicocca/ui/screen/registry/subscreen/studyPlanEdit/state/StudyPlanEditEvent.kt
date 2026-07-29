package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.state

import it.attendance100.mybicocca.core.text.UiText


/** One-shot effects of the plan-compiler wizard, consumed once and never replayed. */
sealed interface StudyPlanEditEvent {
    /** The plan was accepted; [message] reflects the schema's approval flavour (automatic vs manual). */
    data class Submitted(val message: UiText) : StudyPlanEditEvent
}
