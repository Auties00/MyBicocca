package it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.state

import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadgeTone

/**
 * A compact status descriptor for an enrollment year (fuori corso, part-time, sospesa,
 * …), rendered as a toned chip.
 */
data class EnrollmentBadge(
    val label: String,
    val tone: RegistryBadgeTone,
)
