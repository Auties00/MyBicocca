package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/**
 * Per-tile look for the syllabus staff grid: the tile background plus the organic shape and
 * color pair of the initials avatar, cycled by position so neighbouring tiles never match.
 */
data class StaffGridVariant(
    val tileBg: Color,
    val avatarShape: Shape,
    val avatarBg: Color,
    val avatarFg: Color,
)
