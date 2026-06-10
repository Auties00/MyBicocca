package it.attendance100.mybicocca.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3Api
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3PlansApi
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PlanType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PostPlanBody
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3State3
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudyPlan
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudyPlanActivity
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudyPlanHeader
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.ChoiceConstraintUnit
import it.attendance100.mybicocca.domain.model.studyplan.EditableCourse
import it.attendance100.mybicocca.domain.model.studyplan.EditableRule
import it.attendance100.mybicocca.domain.model.studyplan.PlanApprovalType
import it.attendance100.mybicocca.domain.model.studyplan.StudyPathFacet
import it.attendance100.mybicocca.domain.model.studyplan.StudyPathOption
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlanType
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * The non-Room study-plan repository: `getStudyPlan` resolves the best header and maps the
 * in-plan activities (only `choiceFlag != 0`), `submitStudyPlan` builds the proposed-plan
 * POST body with the percorso/approval rules, and `getPlannedCoursesForActiveCareer`
 * short-circuits when the career is not on the active account.
 */
class StudyPlanRepositoryImplTest {

    private val sessionManager: SessionManager = mockk(relaxed = true)
    private val easyStaffApi: EasyStaffApi = mockk(relaxed = true)
    private val esse3Api: Esse3Api = mockk(relaxed = true)
    private val plansApi: Esse3PlansApi = mockk(relaxed = true)

    private val careerId = RepositoryTestFixtures.careerId

    private lateinit var repository: StudyPlanRepositoryImpl

    @Before
    fun setUp() {
        every { sessionManager.activeAccount } returns MutableStateFlow(RepositoryTestFixtures.account())
        coEvery { sessionManager.esse3() } returns esse3Api
        every { esse3Api.plans } returns plansApi
        repository = StudyPlanRepositoryImpl(sessionManager, easyStaffApi)
    }

    @Test
    fun `getStudyPlan returns null when there are no plan headers`() = runTest {
        coEvery { plansApi.getStudentPlanHeaders(careerId.value, any(), any(), any(), any()) } returns emptyList()

        assertThat(repository.getStudyPlan(careerId)).isNull()
    }

    @Test
    fun `getStudyPlan maps the best header and keeps only chosen activities`() = runTest {
        coEvery { plansApi.getStudentPlanHeaders(careerId.value, any(), any(), any(), any()) } returns
            listOf(
                Esse3StudyPlanHeader(
                    planId = 100,
                    planType = Esse3PlanType.Standard,
                    state = Esse3State3.Proposed,
                    stateDescription = "Proposto",
                    choiceRegulationId = 7L,
                    schemaId = 12L,
                ),
                Esse3StudyPlanHeader(
                    planId = 250,
                    planType = Esse3PlanType.Standard,
                    state = Esse3State3.Approved,
                    stateDescription = "Approvato",
                    choiceRegulationId = 8L,
                    schemaId = 13L,
                ),
            )
        coEvery { plansApi.getStudentPlan(careerId.value, 250L, any(), any(), any()) } returns
            Esse3StudyPlan(
                activity = listOf(
                    planActivity(choiceFlag = 1, id = 1L, name = "Analisi", year = 1),
                    planActivity(choiceFlag = 0, id = 2L, name = "Fisica", year = 1),
                ),
            )

        val plan = repository.getStudyPlan(careerId)

        assertThat(plan).isNotNull()
        assertThat(plan!!.planId).isEqualTo(250L)
        assertThat(plan.type).isEqualTo(StudyPlanType.Standard)
        assertThat(plan.statusDescription).isEqualTo("Approvato")
        assertThat(plan.choiceRegulationId).isEqualTo(8L)
        assertThat(plan.schemaId).isEqualTo(13L)
        assertThat(plan.courses).hasSize(1)
        assertThat(plan.courses.single().name).isEqualTo("Analisi")
        assertThat(plan.courses.single().year).isEqualTo(StudyYear(1))
    }

    @Test
    fun `getStudyPlan maps an individual plan type`() = runTest {
        coEvery { plansApi.getStudentPlanHeaders(careerId.value, any(), any(), any(), any()) } returns
            listOf(
                Esse3StudyPlanHeader(planId = 5, planType = Esse3PlanType.Individual),
            )
        coEvery { plansApi.getStudentPlan(careerId.value, 5L, any(), any(), any()) } returns Esse3StudyPlan()

        val plan = repository.getStudyPlan(careerId)

        assertThat(plan!!.type).isEqualTo(StudyPlanType.Individual)
    }

    @Test
    fun `submitStudyPlan posts a proposed standard plan replacing the valid one`() = runTest {
        val bodySlot = slot<Esse3PostPlanBody>()
        coEvery { plansApi.postStudentPlan(careerId.value, capture(bodySlot)) } returns Unit

        repository.submitStudyPlan(careerId, rules = listOf(ruleWithOneSelected()), chosenPath = null)

        val body = bodySlot.captured
        assertThat(body.type).isEqualTo("S")
        assertThat(body.state).isEqualTo("P")
        assertThat(body.cancelValidPlanFlag).isTrue()
        assertThat(body.implementationFlag).isFalse()
        assertThat(body.activity).hasSize(1)
        assertThat(body.activity.single().activityCode).isEqualTo("E3101Q123")
        assertThat(body.studyPlanChoiceCode).isNull()
        assertThat(body.choiceRegulationType).isEqualTo(0)
    }

    @Test
    fun `submitStudyPlan records the percorso only for a non-current chosen path`() = runTest {
        val bodySlot = slot<Esse3PostPlanBody>()
        coEvery { plansApi.postStudentPlan(careerId.value, capture(bodySlot)) } returns Unit

        repository.submitStudyPlan(
            careerId,
            rules = listOf(ruleWithOneSelected()),
            chosenPath = pathOption(isCurrent = false, approval = PlanApprovalType.Manual),
        )

        val body = bodySlot.captured
        assertThat(body.studyPlanChoiceCode).isEqualTo("PDS1")
        assertThat(body.choiceRegulationType).isEqualTo(1)
    }

    @Test
    fun `submitStudyPlan omits the percorso for the current chosen path`() = runTest {
        val bodySlot = slot<Esse3PostPlanBody>()
        coEvery { plansApi.postStudentPlan(careerId.value, capture(bodySlot)) } returns Unit

        repository.submitStudyPlan(
            careerId,
            rules = listOf(ruleWithOneSelected()),
            chosenPath = pathOption(isCurrent = true, approval = PlanApprovalType.AutomaticIfCompliant),
        )

        val body = bodySlot.captured
        assertThat(body.studyPlanChoiceCode).isNull()
        assertThat(body.choiceRegulationType).isEqualTo(2)
    }

    @Test
    fun `submitStudyPlan submits only selected courses`() = runTest {
        val bodySlot = slot<Esse3PostPlanBody>()
        coEvery { plansApi.postStudentPlan(careerId.value, capture(bodySlot)) } returns Unit

        val rule = EditableRule(
            choiceId = 1L,
            orderNumber = 2,
            description = "Caratterizzanti",
            courseYear = 1,
            typeDescription = "",
            isMandatoryRule = false,
            unit = ChoiceConstraintUnit.Credits,
            minUnits = null,
            maxUnits = null,
            isOptional = false,
            courses = listOf(
                editableCourse(code = "AAA", selected = true),
                editableCourse(code = "BBB", selected = false),
            ),
            preNote = null,
            postNote = null,
        )

        repository.submitStudyPlan(careerId, rules = listOf(rule), chosenPath = null)

        val body = bodySlot.captured
        assertThat(body.activity).hasSize(1)
        assertThat(body.activity.single().activityCode).isEqualTo("AAA")
        assertThat(body.activity.single().orderNumber).isEqualTo(2)
        assertThat(body.activity.single().itemId).isEqualTo(1)
    }

    @Test
    fun `getPlannedCoursesForActiveCareer is empty when the career is not on the active account`() = runTest {
        assertThat(repository.getPlannedCoursesForActiveCareer(CareerId(987654L))).isEmpty()
    }

    private fun planActivity(choiceFlag: Int, id: Long, name: String, year: Int) =
        Esse3StudyPlanActivity(
            choiceFlag = choiceFlag,
            activityChoiceId = id,
            activityTranscriptDescription = name,
            activityTranscriptCode = "COD$id",
            weight = 6f,
            courseYear = year,
        )

    private fun ruleWithOneSelected(): EditableRule = EditableRule(
        choiceId = 1L,
        orderNumber = 3,
        description = "Affini",
        courseYear = 1,
        typeDescription = "",
        isMandatoryRule = false,
        unit = ChoiceConstraintUnit.Credits,
        minUnits = null,
        maxUnits = null,
        isOptional = false,
        courses = listOf(editableCourse(code = "E3101Q123", selected = true)),
        preNote = null,
        postNote = null,
    )

    private fun editableCourse(code: String, selected: Boolean) = EditableCourse(
        choiceId = code.hashCode().toLong(),
        code = code,
        name = code,
        credits = 6f,
        courseOfStudyCode = "CDS1",
        studyPlanCode = "PDS1",
        academicYearOfferId = 2024,
        isSelected = selected,
    )

    private fun pathOption(isCurrent: Boolean, approval: PlanApprovalType) = StudyPathOption(
        schemaId = 12L,
        schemaCode = "SC1",
        schemaDescription = "Schema 1",
        percorso = StudyPathFacet(code = "PDS1", description = "Percorso 1"),
        orientamento = null,
        profilo = null,
        partTime = null,
        isCurrent = isCurrent,
        approval = approval,
    )
}
