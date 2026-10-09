package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoAssignment
import it.attendance100.mybicocca.demo.DemoElearning
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionFile
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionFileMetadata
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionForm
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** The assignments of [DemoElearning]; saving, submitting and removing a submission change them in memory. */
@Singleton
class DemoElearningAssignmentRepository @Inject constructor() : ElearningAssignmentRepository {

    private val assignments = MutableStateFlow(DemoElearning.allAssignments.map(::assignment))

    override fun observeForCourse(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<Assignment>>> =
        assignments.map { all -> Loadable.Loaded(all.filter { it.courseId == courseId }) }

    override fun observe(accountId: AccountId, assignmentId: AssignmentId): Flow<Loadable<Assignment>> =
        assignments.map { all ->
            all.firstOrNull { it.id == assignmentId }?.let { Loadable.Loaded(it) } ?: Loadable.NotYetLoaded
        }

    override fun observeAllForAccount(accountId: AccountId): Flow<List<Assignment>> = assignments

    override suspend fun refreshForCourse(accountId: AccountId, courseId: CourseId, force: Boolean) = Unit

    override suspend fun refreshSubmissionStatus(accountId: AccountId, assignmentId: AssignmentId) = Unit

    override suspend fun loadSubmissionForm(
        accountId: AccountId,
        courseId: CourseId,
        assignmentId: AssignmentId,
    ): SubmissionForm {
        val status = assignments.value.first { it.id == assignmentId }.submissionStatus
        val submitted = status as? SubmissionStatus.Submitted
        return SubmissionForm(
            onlineTextEnabled = true,
            fileEnabled = true,
            maxFiles = 3,
            maxSizeBytes = 20L * 1024 * 1024,
            acceptedFileTypes = listOf(".pdf", ".zip"),
            requiresSubmissionStatement = true,
            submissionStatement = "Dichiaro che questo elaborato è frutto del mio lavoro.",
            submissionDraftsEnabled = true,
            canEdit = status !is SubmissionStatus.Graded,
            canSubmit = status is SubmissionStatus.Draft,
            existingOnlineText = submitted?.onlineText,
            existingFiles = submitted?.files.orEmpty(),
        )
    }

    override suspend fun saveSubmission(
        accountId: AccountId,
        assignmentId: AssignmentId,
        onlineText: String?,
        files: List<SubmissionFile>,
        keepFiles: List<Assignment.AttachmentRef>,
    ) = setStatus(assignmentId, SubmissionStatus.Draft(savedAt = DemoClock.fixed.instant()))

    override suspend fun submitForGrading(accountId: AccountId, assignmentId: AssignmentId, acceptStatement: Boolean) =
        setStatus(
            assignmentId,
            SubmissionStatus.Submitted(submittedAt = DemoClock.fixed.instant(), files = emptyList(), onlineText = null),
        )

    override suspend fun removeSubmission(accountId: AccountId, assignmentId: AssignmentId) =
        setStatus(assignmentId, SubmissionStatus.NotSubmitted)

    override suspend fun probeSubmissionFile(uri: String): SubmissionFileMetadata =
        SubmissionFileMetadata(fileName = "consegna.pdf", mimeType = "application/pdf", sizeBytes = 482_113)

    override suspend fun readSubmissionFile(uri: String): SubmissionFile =
        SubmissionFile(fileName = "consegna.pdf", mimeType = "application/pdf", sizeBytes = 0, bytes = ByteArray(0))

    override suspend fun clearForAccount(accountId: AccountId) = Unit

    private fun setStatus(assignmentId: AssignmentId, status: SubmissionStatus) {
        assignments.update { all -> all.map { if (it.id == assignmentId) it.copy(submissionStatus = status) else it } }
    }

    private fun assignment(demo: DemoAssignment): Assignment {
        val due = demo.dueAt
        val course = demo.course
        return Assignment(
            id = AssignmentId(demo.id),
            courseId = CourseId(course.moodleCourseId),
            cmId = demo.cmId,
            name = demo.title,
            intro = "<p>Svolgi gli esercizi allegati e carica un unico file PDF con le soluzioni commentate.</p>" +
                "<p>La consegna è individuale. Sono ammesse consegne in ritardo fino a 48 ore dopo la scadenza.</p>",
            introFiles = listOf(
                Assignment.AttachmentRef(
                    fileName = "traccia.pdf",
                    fileUrl = "${DemoElearning.BASE_URL}/files/${course.shortLabel.lowercase()}/traccia.pdf",
                    mimeType = "application/pdf",
                    sizeBytes = 214_300,
                ),
            ),
            dueDate = due,
            allowSubmissionsFrom = due.minusSeconds(14 * DAY),
            cutoffDate = due.plusSeconds(2 * DAY),
            gradingDueDate = due.plusSeconds(10 * DAY),
            maxAttempts = 1,
            allowedExtensions = listOf(".pdf", ".zip"),
            allowDrafts = true,
            submissionStatus = if (demo.submitted) {
                SubmissionStatus.Submitted(
                    submittedAt = DemoClock.instant(DemoClock.now.minusDays(1)),
                    files = listOf(Assignment.AttachmentRef("consegna_rossi.pdf", null, "application/pdf", 482_113)),
                    onlineText = null,
                )
            } else {
                SubmissionStatus.NotSubmitted
            },
            pageUrl = null,
        )
    }

    private companion object {
        const val DAY = 24L * 60 * 60
    }
}
