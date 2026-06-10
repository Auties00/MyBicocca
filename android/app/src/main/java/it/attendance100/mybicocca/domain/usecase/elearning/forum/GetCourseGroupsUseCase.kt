package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumGroup
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import javax.inject.Inject

/**
 * Fetches the user's groups in a course to populate the group picker when starting a discussion
 * in a group-mode forum. An empty result means the forum is not group-restricted for this user.
 * Live call, nothing cached; throws on network failure.
 */
class GetCourseGroupsUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    suspend operator fun invoke(accountId: AccountId, courseId: CourseId): List<ForumGroup> =
        repository.getCourseGroups(accountId, courseId)
}
