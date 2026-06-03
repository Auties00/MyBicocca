package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state

import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId

sealed interface CourseDetailOneShotEvent {
    data class RefreshFailed(val cause: Throwable) : CourseDetailOneShotEvent
    data class OpenAssignment(val id: AssignmentId) : CourseDetailOneShotEvent
    data class OpenQuiz(val id: QuizId) : CourseDetailOneShotEvent
    data class OpenForum(val id: ForumId) : CourseDetailOneShotEvent
    data class OpenDiscussion(val id: DiscussionId) : CourseDetailOneShotEvent
    data class OpenModuleResource(val url: String) : CourseDetailOneShotEvent
    data class OpenVideo(val cmId: Int, val title: String) : CourseDetailOneShotEvent
}
