package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state

import it.attendance100.mybicocca.domain.model.elearning.course.CourseModule

// One render unit inside an expanded section: either a teacher note (a long label)
// or a run of modules under an optional sub-header (a short label).
sealed interface ContentBlock {
    data class Note(val text: String) : ContentBlock

    // `title == null` for the leading run before any label, and for runs that resume
    // after an interleaved note (the header is not repeated).
    data class Group(val title: String?, val modules: List<CourseModule>) : ContentBlock
}
