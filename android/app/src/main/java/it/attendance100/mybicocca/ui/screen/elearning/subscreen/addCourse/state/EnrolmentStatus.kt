package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state

/** Per-course self-enrolment state rendered by a catalog row's enrol button. */
enum class EnrolmentStatus {
    Idle,
    InProgress,
    Enrolled,
    Failed,
}
