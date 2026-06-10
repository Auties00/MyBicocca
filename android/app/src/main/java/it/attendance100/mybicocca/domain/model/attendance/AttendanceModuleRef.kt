package it.attendance100.mybicocca.domain.model.attendance

/**
 * Reference to a Moodle mod_attendance activity within a course, addressed through the
 * tool_mobile_get_content bridge. Identifies where open sessions are looked up and marked.
 *
 * @property courseId Moodle course id the activity belongs to.
 * @property courseModuleId Course-module id (cmid) of the attendance activity.
 * @property name Display name of the activity.
 */
data class AttendanceModuleRef(
    val courseId: Int,
    val courseModuleId: Int,
    val name: String,
)
