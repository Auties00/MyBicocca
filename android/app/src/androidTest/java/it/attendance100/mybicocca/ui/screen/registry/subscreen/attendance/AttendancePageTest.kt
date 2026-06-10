package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.studyplan.Semester
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.ConsumePresenceScanUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.GetPendingAttendanceCoursesUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.MarkPresenceUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.ObservePendingPresenceScanUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.ParsePresenceScanUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * UI coverage for the Presenze sheet body's pending-courses surface: the root pages through its
 * loading / empty / course-list states off the courses stream, and tapping a course row pushes its
 * overview. The screen is driven by a real [AttendanceViewModel] over MockK-faked use cases (reusing
 * the Wave 1 construction), anchored on [AttendanceTestTags], and wrapped in the shared
 * [setBicoccaContent] test environment (BicoccaTheme plus the app-wide HapticManager / snackbar /
 * device-type locals the pushed course page reads). The Rileva / QR self-mark surface is
 * camera-bound (CameraX) and out of scope.
 */
@RunWith(AndroidJUnit4::class)
class AttendancePageTest {

    @get:Rule
    val compose = createComposeRule()

    private val careerId = CareerId(101L)

    private val getPendingCourses: GetPendingAttendanceCoursesUseCase = mockk()
    private val markPresence: MarkPresenceUseCase = mockk(relaxed = true)
    private val parseScan: ParsePresenceScanUseCase = mockk(relaxed = true)
    private val observePendingPresenceScan: ObservePendingPresenceScanUseCase = mockk()
    private val consumePresenceScan: ConsumePresenceScanUseCase = mockk(relaxed = true)
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun course(name: String, code: String): CourseAttendance = CourseAttendance(
        name = name,
        code = code,
        year = StudyYear(1),
        semester = Semester.First,
        credits = 6f,
        teacherName = null,
        classroomAttendance = null,
        sessionAttendance = emptyList(),
    )

    private fun viewModel(): AttendanceViewModel {
        every { observePendingPresenceScan() } returns MutableStateFlow(null)
        return AttendanceViewModel(
            getPendingCourses, markPresence, parseScan,
            observePendingPresenceScan, consumePresenceScan, observeActiveAccount,
        )
    }

    private fun setScreen(vm: AttendanceViewModel) {
        compose.setBicoccaContent {
            AttendancePage(viewModel = vm)
        }
    }

    @Test
    fun the_root_shows_the_loading_state_while_no_career_has_resolved() {
        every { observeActiveAccount() } returns emptyFlow()

        setScreen(viewModel())

        compose.onNodeWithTag(AttendanceTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(AttendanceTestTags.ROOT_LOADING).assertIsDisplayed()
    }

    @Test
    fun the_root_shows_the_empty_state_when_no_course_is_pending() {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getPendingCourses(careerId) } returns emptyList()

        setScreen(viewModel())

        compose.onNodeWithTag(AttendanceTestTags.ROOT_EMPTY).assertIsDisplayed()
    }

    @Test
    fun the_root_lists_the_pending_courses_when_present() {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getPendingCourses(careerId) } returns listOf(course("Algoritmi", "E3101Q1"))

        setScreen(viewModel())

        compose.onNodeWithTag(AttendanceTestTags.COURSE_LIST).assertIsDisplayed()
        compose.onNodeWithTag(AttendanceTestTags.course("E3101Q1")).assertIsDisplayed()
    }

    @Test
    fun tapping_a_course_row_pushes_its_overview_page() {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getPendingCourses(careerId) } returns listOf(course("Algoritmi", "E3101Q1"))

        setScreen(viewModel())

        compose.onNodeWithTag(AttendanceTestTags.course("E3101Q1")).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(AttendanceTestTags.COURSE_PAGE).assertIsDisplayed()
    }

    private fun account(): Account = Account(
        id = AccountId("acc-1"),
        username = "mario.rossi@campus.unimib.it",
        displayName = "Mario Rossi",
        academic = AcademicIdentity(
            recordUserId = "u1",
            personId = 7L,
            fiscalCode = null,
            careers = listOf(
                Career(
                    id = careerId,
                    enrollmentTraitId = 1L,
                    programId = 2L,
                    easyStaffProgramCode = "E32",
                    academicYearEnrollmentId = 3L,
                    studentNumber = "900001",
                    description = "Informatica",
                    academicYear = 2024,
                    status = CareerStatus.ACTIVE,
                ),
            ),
            selectedCareerId = careerId,
        ),
        learning = LearningIdentity(
            lmsUserId = 11,
            lmsUsername = "mario.rossi@campus.unimib.it",
            locale = "it",
            isSiteAdmin = false,
            maxUploadFileSizeBytes = 0L,
            storageQuotaBytes = 0L,
        ),
        createdAt = Instant.EPOCH,
        lastUsedAt = Instant.EPOCH,
        lastSyncedAt = Instant.EPOCH,
    )
}
