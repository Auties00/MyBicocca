package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * Identifier of a Moodle course on the e-learning platform.
 *
 * @property value The numeric course id assigned by Moodle, used as the `courseid`
 * parameter of every course-scoped web-service call.
 */
@JvmInline
value class CourseId(val value: Int)
