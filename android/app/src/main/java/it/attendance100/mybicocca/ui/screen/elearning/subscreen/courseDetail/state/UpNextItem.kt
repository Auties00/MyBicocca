package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state

import java.time.Instant

/**
 * Display model for an upcoming-deadline callout: what is due, when, and the action that opens
 * the underlying activity.
 */
data class UpNextItem(
    val title: String,
    val subtitle: String?,
    val dueAt: Instant,
    val onClick: () -> Unit,
)
