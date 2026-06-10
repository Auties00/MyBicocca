package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan

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
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.studyplan.StudyPath
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlan
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlanCourse
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlanType
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPathUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPlanPrintUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPlanUseCase
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * UI coverage for the Percorso e piano sheet body: the root pages through its loading / empty /
 * year-directory states off the plan and path streams, and tapping a year row pushes that year's
 * course page. The screen is driven by a real [StudyPlanViewModel] over MockK-faked use cases
 * (reusing the Wave 1 construction), anchored on [StudyPlanTestTags], and wrapped in the production
 * [BicoccaTheme]. The Modifica entry is never tapped — it mounts a hiltViewModel child the unit-test
 * harness cannot create.
 */
@RunWith(AndroidJUnit4::class)
class StudyPlanPageTest {

    @get:Rule
    val compose = createComposeRule()

    private val careerId = CareerId(101L)

    private val getStudyPlan: GetStudyPlanUseCase = mockk()
    private val getStudyPath: GetStudyPathUseCase = mockk()
    private val getStudyPlanPrint: GetStudyPlanPrintUseCase = mockk(relaxed = true)
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun course(id: Long, year: Int) = StudyPlanCourse(
        id = id,
        name = "Analisi $id",
        code = "E3101Q$id",
        credits = 6f,
        year = StudyYear(year),
    )

    private fun plan(courses: List<StudyPlanCourse>) = StudyPlan(
        planId = 555L,
        studentId = 101L,
        type = StudyPlanType.Standard,
        statusDescription = "Approvato",
        lastUpdated = null,
        choiceRegulationId = 10L,
        schemaId = 20L,
        courses = courses,
    )

    private fun path() = StudyPath(
        percorso = null,
        orientamento = null,
        profilo = null,
        partTime = null,
        choiceRegulationId = 10L,
        currentSchemaId = 20L,
        options = emptyList(),
        editingOpen = false,
        choiceAvailable = false,
    )

    private fun viewModel(): StudyPlanViewModel =
        StudyPlanViewModel(getStudyPlan, getStudyPath, getStudyPlanPrint, observeActiveAccount)

    private fun setScreen(vm: StudyPlanViewModel) {
        compose.setContent {
            BicoccaTheme(dark = false) {
                StudyPlanPage(viewModel = vm)
            }
        }
    }

    @Test
    fun the_root_shows_the_loading_state_while_no_career_has_resolved() {
        every { observeActiveAccount() } returns emptyFlow()

        setScreen(viewModel())

        compose.onNodeWithTag(StudyPlanTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(StudyPlanTestTags.ROOT_LOADING).assertIsDisplayed()
    }

    @Test
    fun the_root_shows_the_empty_state_when_the_career_has_no_plan() {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getStudyPlan(careerId) } returns null
        coEvery { getStudyPath(careerId) } returns path()

        setScreen(viewModel())

        compose.onNodeWithTag(StudyPlanTestTags.ROOT_EMPTY).assertIsDisplayed()
    }

    @Test
    fun the_root_lists_the_years_when_a_plan_is_loaded() {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getStudyPlan(careerId) } returns plan(listOf(course(1L, 1), course(2L, 2)))
        coEvery { getStudyPath(careerId) } returns path()

        setScreen(viewModel())

        compose.onNodeWithTag(StudyPlanTestTags.YEAR_LIST).assertIsDisplayed()
        compose.onNodeWithTag(StudyPlanTestTags.yearRow(1)).assertIsDisplayed()
    }

    @Test
    fun tapping_a_year_row_pushes_that_year_s_course_page() {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getStudyPlan(careerId) } returns plan(listOf(course(1L, 1)))
        coEvery { getStudyPath(careerId) } returns path()

        setScreen(viewModel())

        compose.onNodeWithTag(StudyPlanTestTags.yearRow(1)).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(StudyPlanTestTags.YEAR_COURSES).assertIsDisplayed()
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
