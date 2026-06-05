package it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.state

import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.theme.EnrollmentBadgeTone

// A compact status chip shown on a timeline node (fuori corso, part-time, sospesa, …).
data class EnrollmentBadge(
    val label: String,
    val tone: EnrollmentBadgeTone,
)
