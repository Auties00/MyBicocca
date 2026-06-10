package it.attendance100.mybicocca.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.ElearningSession
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.assignment.AssignmentDao
import it.attendance100.mybicocca.data.local.elearning.assignment.AssignmentEntity
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.local.file.ContentUriReader
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningApi
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAssignment
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseAssignments
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetAssignmentsResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetSubmissionStatusResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningUpload
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningUploadedFile
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionFile
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Behaviour coverage for the assignment repository: the TTL-gated, deduplicated course refresh
 * that fans a submission-status fetch out per assignment and degrades a failed one to
 * not-submitted, plus the submission write path — kept files re-downloaded and re-uploaded with
 * the new ones into a single draft area, no draft id when there is nothing to upload, and the
 * remove call carrying the active account's Moodle user id.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ElearningAssignmentRepositoryImplTest {

    private val accountId = AccountId("acc-1")
    private val courseId = CourseId(42)
    private val assignmentId = AssignmentId(500)
    private val lmsUserId = 7
    private val account = elearningRepoTestAccount(accountId, lmsUserId)
    private val token = "x".repeat(32)

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val assignmentDao = mockk<AssignmentDao>(relaxed = true)
    private val syncStateDao = mockk<ElearningSyncStateDao>(relaxed = true)
    private val contentUriReader = mockk<ContentUriReader>(relaxed = true)
    private val elearningApi = mockk<ElearningApi>(relaxed = true)
    private val stalePolicy = StalePolicy(defaultTtlMs = 60_000L)

    private fun newRepository(scope: CoroutineScope): ElearningAssignmentRepositoryImpl {
        every { sessionManager.activeAccount } returns MutableStateFlow(account)
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, token)
        coEvery { syncStateDao.getState(any(), any(), any()) } returns null
        return ElearningAssignmentRepositoryImpl(
            sessionManager, assignmentDao, syncStateDao, stalePolicy, contentUriReader, scope,
        )
    }

    private fun assignmentEntity(id: Int) = AssignmentEntity(
        accountId = accountId.value,
        assignmentId = id,
        courseId = courseId.value,
        cmId = 9,
        name = "Compito $id",
        intro = null,
        introFilesJson = null,
        dueDateMs = null,
        allowSubmissionsFromMs = null,
        cutoffDateMs = null,
        gradingDueDateMs = null,
        maxAttempts = null,
        allowedExtensionsCsv = null,
        allowDrafts = true,
        submissionStatusJson = "{}",
        submissionConfigJson = null,
    )

    @Test
    fun `observeForCourse maps rows to Loaded`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { assignmentDao.observeForCourse(accountId.value, courseId.value) } returns
            flowOf(listOf(assignmentEntity(500)))

        repository.observeForCourse(accountId, courseId).test {
            val loaded = awaitItem() as Loadable.Loaded
            assertThat(loaded.value).hasSize(1)
            assertThat(loaded.value.first().id).isEqualTo(AssignmentId(500))
            cancelAndIgnoreRemainingEvents()
        }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `observe maps a missing row to NotYetLoaded`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { assignmentDao.observe(accountId.value, assignmentId.value) } returns flowOf(null)

        repository.observe(accountId, assignmentId).test {
            assertThat(awaitItem()).isEqualTo(Loadable.NotYetLoaded)
            cancelAndIgnoreRemainingEvents()
        }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `refreshForCourse writes assignments through and stamps`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.assignments.getAssignmentsForCourse(token, courseId.value) } returns
            ElearningGetAssignmentsResponse(
                courses = listOf(
                    ElearningCourseAssignments(
                        id = courseId.value,
                        assignments = listOf(
                            ElearningAssignment(id = 500, courseId = courseId.value, name = "Compito A"),
                        ),
                    ),
                ),
            )
        coEvery { elearningApi.assignments.getSubmissionStatus(token, 500, null, null) } returns
            ElearningGetSubmissionStatusResponse()

        repository.refreshForCourse(accountId, courseId, force = false)

        val rows = slot<List<AssignmentEntity>>()
        coVerify { assignmentDao.replaceForCourse(accountId.value, courseId.value, capture(rows)) }
        assertThat(rows.captured.first().assignmentId).isEqualTo(500)
        coVerify { syncStateDao.upsertState(match { it.scope == ElearningSyncScope.COURSE_ASSIGNMENTS }) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `refreshForCourse degrades a failed status fetch to not-submitted without failing`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.assignments.getAssignmentsForCourse(token, courseId.value) } returns
            ElearningGetAssignmentsResponse(
                courses = listOf(
                    ElearningCourseAssignments(
                        id = courseId.value,
                        assignments = listOf(
                            ElearningAssignment(id = 500, courseId = courseId.value, name = "Compito A"),
                        ),
                    ),
                ),
            )
        coEvery { elearningApi.assignments.getSubmissionStatus(token, 500, null, null) } throws RuntimeException("boom")

        repository.refreshForCourse(accountId, courseId, force = false)

        coVerify { assignmentDao.replaceForCourse(accountId.value, courseId.value, any()) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `refreshForCourse skips network while fresh and not forced`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery {
            syncStateDao.getState(accountId.value, ElearningSyncScope.COURSE_ASSIGNMENTS, courseId.value.toLong())
        } returns ElearningSyncStateEntity(
            accountId = accountId.value,
            scope = ElearningSyncScope.COURSE_ASSIGNMENTS,
            scopeId = courseId.value.toLong(),
            lastRefreshedAtMs = System.currentTimeMillis(),
        )

        repository.refreshForCourse(accountId, courseId, force = false)

        coVerify(exactly = 0) { elearningApi.assignments.getAssignmentsForCourse(any(), any()) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `saveSubmission re-uploads kept files plus new ones into one draft area`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { assignmentDao.observe(accountId.value, assignmentId.value) } returns flowOf(assignmentEntity(500))
        coEvery { elearningApi.files.downloadFileBytes(token, "https://kept/file.pdf") } returns byteArrayOf(1, 2)
        coEvery { elearningApi.files.uploadToDraftArea(token, any()) } returns listOf(ElearningUploadedFile(itemId = 808))
        coEvery { elearningApi.assignments.getSubmissionStatus(token, assignmentId.value, null, null) } returns
            ElearningGetSubmissionStatusResponse()

        repository.saveSubmission(
            accountId = accountId,
            assignmentId = assignmentId,
            onlineText = "ciao",
            files = listOf(SubmissionFile("new.pdf", "application/pdf", 3L, byteArrayOf(9))),
            keepFiles = listOf(
                Assignment.AttachmentRef("file.pdf", "https://kept/file.pdf", "application/pdf", 2L),
            ),
        )

        val uploads = slot<List<ElearningUpload>>()
        coVerify { elearningApi.files.uploadToDraftArea(token, capture(uploads)) }
        assertThat(uploads.captured.map { it.fileName }).containsExactly("file.pdf", "new.pdf").inOrder()
        coVerify {
            elearningApi.assignments.saveSubmission(
                wsToken = token,
                assignmentId = assignmentId.value,
                onlineText = "ciao",
                filesDraftItemId = 808,
            )
        }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `saveSubmission with nothing to upload sends a null draft id`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { assignmentDao.observe(accountId.value, assignmentId.value) } returns flowOf(assignmentEntity(500))
        coEvery { elearningApi.assignments.getSubmissionStatus(token, assignmentId.value, null, null) } returns
            ElearningGetSubmissionStatusResponse()

        repository.saveSubmission(
            accountId = accountId,
            assignmentId = assignmentId,
            onlineText = "solo testo",
            files = emptyList(),
            keepFiles = emptyList(),
        )

        coVerify(exactly = 0) { elearningApi.files.uploadToDraftArea(any(), any()) }
        coVerify {
            elearningApi.assignments.saveSubmission(
                wsToken = token,
                assignmentId = assignmentId.value,
                onlineText = "solo testo",
                filesDraftItemId = null,
            )
        }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `removeSubmission passes the active account's Moodle user id`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { assignmentDao.observe(accountId.value, assignmentId.value) } returns flowOf(assignmentEntity(500))
        coEvery { elearningApi.assignments.getSubmissionStatus(token, assignmentId.value, null, null) } returns
            ElearningGetSubmissionStatusResponse()

        repository.removeSubmission(accountId, assignmentId)

        coVerify { elearningApi.assignments.removeSubmission(token, assignmentId.value, lmsUserId) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `submitForGrading forwards the accept-statement flag and re-syncs the status`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { assignmentDao.observe(accountId.value, assignmentId.value) } returns flowOf(assignmentEntity(500))
        coEvery { elearningApi.assignments.getSubmissionStatus(token, assignmentId.value, null, null) } returns
            ElearningGetSubmissionStatusResponse()

        repository.submitForGrading(accountId, assignmentId, acceptStatement = true)

        coVerify { elearningApi.assignments.submitForGrading(token, assignmentId.value, true) }
        coVerify { assignmentDao.upsert(any()) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `probeSubmissionFile delegates to the content uri reader`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)

        repository.probeSubmissionFile("content://doc/1")

        coVerify { contentUriReader.probe("content://doc/1") }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `clearForAccount delegates to the DAO`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)

        repository.clearForAccount(accountId)

        coVerify { assignmentDao.deleteForAccount(accountId.value) }
        scope.coroutineContext[Job]?.cancel()
    }
}
