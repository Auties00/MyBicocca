package it.attendance100.mybicocca.domain.model.elearning.forum

import it.attendance100.mybicocca.domain.model.elearning.course.CourseId

data class Forum(
    val id: ForumId,
    val courseId: CourseId,
    val cmId: Int?,
    val name: String,
    val intro: String?,
    val type: ForumType,
    val discussionCount: Int,
    val postCount: Int,
    val canCreateDiscussions: Boolean,
    val canSubscribe: Boolean,
    val canAttachFiles: Boolean,
)
