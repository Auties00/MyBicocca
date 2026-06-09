package it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.state

import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadgeTone

// A compact status chip shown on a timeline node (fuori corso, part-time, sospesa, …).
data class EnrollmentBadge(
    val label: String,
    val tone: RegistryBadgeTone,
)
