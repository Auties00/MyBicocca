package it.attendance100.mybicocca.domain.model.elearning.forum

/**
 * A course group the user belongs to, sourced from the core_group_get_course_user_groups web
 * service and offered as a target when starting a discussion in a group-mode forum. An empty
 * group list means the forum is not group-restricted for this user, so the discussion is posted
 * without a group.
 *
 * @property id Moodle group id, submitted as the new discussion's group.
 * @property name Group display name.
 */
data class ForumGroup(
    val id: Int,
    val name: String,
)
