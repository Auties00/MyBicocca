package it.attendance100.mybicocca.domain.model.elearning.course

data class CourseDetails(
    val enrolled: EnrolledCourse,
    val sections: List<CourseSection>,
    val staff: List<CourseStaffMember>,
    val syllabus: CourseSyllabusPointer?,
)
