package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state

import it.attendance100.mybicocca.domain.model.elearning.course.CourseModule

data class PickedContinueWatching(
    val playable: ContinuePlayable,
    val module: CourseModule,
    val sectionId: Int,
)
