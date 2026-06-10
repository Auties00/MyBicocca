package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state

/**
 * One-shot results of forum actions, surfaced by the sheet as an in-sheet result page (failures)
 * or a lightweight confirmation (info). Navigation outcomes (e.g. opening a freshly created
 * thread) are handled inside the ViewModel and don't travel through here.
 */
sealed interface ForumSheetEvent {
    data class Failed(val title: String, val cause: Throwable) : ForumSheetEvent
    data class Info(val title: String) : ForumSheetEvent
}
