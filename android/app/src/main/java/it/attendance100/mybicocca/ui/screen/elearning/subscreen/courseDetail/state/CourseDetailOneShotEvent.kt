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
    // mod/url links: opens the link sheet on the resolved external target (open / copy).
    data class OpenLink(val title: String, val url: String) : CourseDetailOneShotEvent
    data class OpenVideo(val cmId: Int, val title: String) : CourseDetailOneShotEvent
    // A file open. forceChooser is set by a long-press, to re-show the in-app/external chooser
    // even when a choice was already remembered.
    data class OpenFile(
        val fileName: String,
        val fileUrl: String,
        val mimeType: String?,
        val sizeBytes: Long?,
        val forceChooser: Boolean = false,
    ) : CourseDetailOneShotEvent
    // Folders (and multi-file resources) open a picker sheet before the viewer.
    data class OpenFolder(val cmId: Int) : CourseDetailOneShotEvent
}
