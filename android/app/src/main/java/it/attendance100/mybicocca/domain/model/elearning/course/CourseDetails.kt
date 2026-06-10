package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * Everything the course detail screen renders for a single Moodle course, assembled
 * from the enrolled-course record, the course contents web service, and the public
 * course page.
 *
 * @property enrolled The course's entry in the student's enrolled-course list.
 * @property sections The course content tree, in page order.
 * @property staff Teachers, tutors and assistants listed on the public course page.
 * @property syllabus The official course sheet ("scheda"), or null when the public
 * page exposes none.
 */
data class CourseDetails(
    val enrolled: EnrolledCourse,
    val sections: List<CourseSection>,
    val staff: List<CourseStaffMember>,
    val syllabus: CourseSyllabusPointer?,
)
