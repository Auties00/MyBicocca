package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.state

/** One-shot effects of the percorso sheet, consumed once and never replayed. */
sealed interface StudyPlanEvent {
    /** A fetched plan-print PDF, handed off to an external viewer under [fileName]. */
    data class OpenPdf(val bytes: ByteArray, val fileName: String) : StudyPlanEvent
    data class ShowMessage(val message: String) : StudyPlanEvent
}
