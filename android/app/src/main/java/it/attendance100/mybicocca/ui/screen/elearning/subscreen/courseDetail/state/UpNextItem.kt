package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state

import java.time.Instant

data class UpNextItem(
    val title: String,
    val subtitle: String?,
    val dueAt: Instant,
    val onClick: () -> Unit,
)
