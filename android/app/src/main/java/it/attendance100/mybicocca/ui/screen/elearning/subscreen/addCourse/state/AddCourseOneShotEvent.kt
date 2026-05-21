package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state

import it.attendance100.mybicocca.domain.model.elearning.course.CourseId

sealed interface AddCourseOneShotEvent {
    data class EnrolFailed(val courseId: CourseId, val cause: Throwable) : AddCourseOneShotEvent
    data class EnrolSucceeded(val courseId: CourseId, val courseName: String) : AddCourseOneShotEvent
    data object RequireSignIn : AddCourseOneShotEvent
}
