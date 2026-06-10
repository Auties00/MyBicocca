package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state

import it.attendance100.mybicocca.domain.model.elearning.course.CourseModule

/**
 * The continue-watching pick with its navigation anchors: the card's display model plus the
 * backing module to open on resume and the owning section id to expand and scroll to for
 * "go to lesson".
 */
data class PickedContinueWatching(
    val playable: ContinuePlayable,
    val module: CourseModule,
    val sectionId: Int,
)
