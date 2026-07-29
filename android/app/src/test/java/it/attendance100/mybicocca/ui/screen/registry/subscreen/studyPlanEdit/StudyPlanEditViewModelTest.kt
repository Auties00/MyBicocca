package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.ChoiceConstraintUnit
import it.attendance100.mybicocca.domain.model.studyplan.EditableCourse
import it.attendance100.mybicocca.domain.model.studyplan.EditableRule
import it.attendance100.mybicocca.domain.model.studyplan.PlanApprovalType
import it.attendance100.mybicocca.domain.model.studyplan.StudyPath
import it.attendance100.mybicocca.domain.model.studyplan.StudyPathOption
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPathUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPlanDraftUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.SubmitStudyPlanUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.state.StudyPlanEditEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.state.StudyPlanEditRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

/**
 * Rule-logic and state-machine coverage for the percorso-first plan wizard: toggle rules
 * (mandatory locked, selected-elsewhere blocked, credit cap honored), schema selection with
 * per-schema draft caching, deferred rule loading, hasChanges, submit clearing drafts and
 * emitting the approval-flavoured message, and reset.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "it")
class StudyPlanEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getStudyPath: GetStudyPathUseCase = mockk()
    private val getStudyPlanDraft: GetStudyPlanDraftUseCase = mockk()
    private val submitStudyPlan: SubmitStudyPlanUseCase = mockk(relaxed = true)

    private val studentId = 101L
    private val careerId = CareerId(studentId)

    private fun request(schemaId: Long = 20L, planId: Long = 555L) = StudyPlanEditRequest(
        studentId = studentId,
        choiceRegulationId = 10L,
        schemaId = schemaId,
        planId = planId,
    )

    private fun course(
        choiceId: Long,
        code: String,
        credits: Float = 6f,
        selected: Boolean = false,
        mandatory: Boolean = false,
    ) = EditableCourse(
        choiceId = choiceId,
        code = code,
        name = "Course $code",
        credits = credits,
        courseOfStudyCode = null,
        studyPlanCode = null,
        academicYearOfferId = null,
        isSelected = selected,
        isMandatory = mandatory,
        isInitialSelected = selected,
    )

    private fun rule(
        choiceId: Long,
        courses: List<EditableCourse>,
        maxUnits: Float? = null,
        minUnits: Float? = null,
        mandatory: Boolean = false,
        optional: Boolean = false,
        unit: ChoiceConstraintUnit = ChoiceConstraintUnit.Credits,
    ) = EditableRule(
        choiceId = choiceId,
        orderNumber = 1,
        description = "Rule $choiceId",
        courseYear = 1,
        typeDescription = "scelta",
        isMandatoryRule = mandatory,
        unit = unit,
        minUnits = minUnits,
        maxUnits = maxUnits,
        isOptional = optional,
        courses = courses,
        preNote = null,
        postNote = null,
    )

    private fun pathWithOptions(vararg options: StudyPathOption): StudyPath = StudyPath(
        percorso = null,
        orientamento = null,
        profilo = null,
        partTime = null,
        choiceRegulationId = 10L,
        currentSchemaId = 20L,
        options = options.toList(),
        editingOpen = true,
        choiceAvailable = options.size > 1,
    )

    private fun option(schemaId: Long, approval: PlanApprovalType = PlanApprovalType.Manual) = StudyPathOption(
        schemaId = schemaId,
        schemaCode = "SC$schemaId",
        schemaDescription = "Schema $schemaId",
        percorso = null,
        orientamento = null,
        profilo = null,
        partTime = null,
        isCurrent = schemaId == 20L,
        approval = approval,
    )

    @Test
    fun `rules-only flow loads the request schema rules immediately`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions()
        coEvery { getStudyPlanDraft(careerId, 555L, 10L, 20L) } returns
            listOf(rule(1L, listOf(course(11L, "A"))))

        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)

        assertThat(vm.selectedSchemaId.value).isEqualTo(20L)
        assertThat(vm.rules.value).isInstanceOf(Loadable.Loaded::class.java)
        assertThat((vm.rules.value as Loadable.Loaded).value).hasSize(1)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `a percorso step defers the rules fetch and preselects nothing`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions(option(20L), option(30L))

        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)

        assertThat(vm.selectedSchemaId.value).isNull()
        assertThat(vm.rules.value).isEqualTo(Loadable.NotYetLoaded)
        coVerify(exactly = 0) { getStudyPlanDraft(any(), any(), any(), any()) }
        assertThat(vm.currentSegment.value).isEqualTo(StudyPlanEditViewModel.PATH_SEGMENT)
    }

    @Test
    fun `selectSchema then loadSelectedRules fetches the chosen schema`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions(option(20L), option(30L))
        coEvery { getStudyPlanDraft(careerId, 555L, 10L, 30L) } returns
            listOf(rule(2L, listOf(course(21L, "B"))))

        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)
        vm.selectSchema(30L)
        vm.loadSelectedRules()

        assertThat(vm.selectedSchemaId.value).isEqualTo(30L)
        assertThat((vm.rules.value as Loadable.Loaded).value).hasSize(1)
        coVerify { getStudyPlanDraft(careerId, 555L, 10L, 30L) }
    }

    @Test
    fun `flipping back to a schema restores its parked draft without a refetch`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions(option(20L), option(30L))
        coEvery { getStudyPlanDraft(careerId, 555L, 10L, 20L) } returns
            listOf(rule(1L, listOf(course(11L, "A"))))
        coEvery { getStudyPlanDraft(careerId, 555L, 10L, 30L) } returns
            listOf(rule(2L, listOf(course(21L, "B"))))

        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)
        vm.selectSchema(20L)
        vm.loadSelectedRules()
        vm.selectSchema(30L)
        vm.loadSelectedRules()
        vm.selectSchema(20L)
        vm.loadSelectedRules()

        coVerify(exactly = 1) { getStudyPlanDraft(careerId, 555L, 10L, 20L) }
        assertThat((vm.rules.value as Loadable.Loaded).value.first().choiceId).isEqualTo(1L)
    }

    @Test
    fun `toggleCourse selects a free course`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions()
        coEvery { getStudyPlanDraft(any(), any(), any(), any()) } returns
            listOf(rule(1L, listOf(course(11L, "A"))))
        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)

        vm.toggleCourse(ruleChoiceId = 1L, courseChoiceId = 11L)

        val toggled = vm.rules.value.valueOrNull()!!.first().courses.first()
        assertThat(toggled.isSelected).isTrue()
    }

    @Test
    fun `toggleCourse never flips a mandatory course`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions()
        coEvery { getStudyPlanDraft(any(), any(), any(), any()) } returns
            listOf(rule(1L, listOf(course(11L, "A", selected = true, mandatory = true)), mandatory = true))
        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)

        vm.toggleCourse(ruleChoiceId = 1L, courseChoiceId = 11L)

        assertThat(vm.rules.value.valueOrNull()!!.first().courses.first().isSelected).isTrue()
    }

    @Test
    fun `toggleCourse blocks selecting the same activity code in a second rule`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions()
        coEvery { getStudyPlanDraft(any(), any(), any(), any()) } returns listOf(
            rule(1L, listOf(course(11L, "SHARED", selected = true))),
            rule(2L, listOf(course(21L, "SHARED"))),
        )
        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)

        vm.toggleCourse(ruleChoiceId = 2L, courseChoiceId = 21L)

        val ruleTwoCourse = vm.rules.value.valueOrNull()!!.first { it.choiceId == 2L }.courses.first()
        assertThat(ruleTwoCourse.isSelected).isFalse()
    }

    @Test
    fun `toggleCourse honours the rule credit cap`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions()
        coEvery { getStudyPlanDraft(any(), any(), any(), any()) } returns listOf(
            rule(
                1L,
                listOf(
                    course(11L, "A", credits = 6f, selected = true),
                    course(12L, "B", credits = 6f),
                ),
                maxUnits = 6f,
            ),
        )
        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)

        vm.toggleCourse(ruleChoiceId = 1L, courseChoiceId = 12L)

        val second = vm.rules.value.valueOrNull()!!.first().courses.first { it.choiceId == 12L }
        assertThat(second.isSelected).isFalse()
    }

    @Test
    fun `deselecting is always allowed even at the cap`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions()
        coEvery { getStudyPlanDraft(any(), any(), any(), any()) } returns listOf(
            rule(1L, listOf(course(11L, "A", credits = 6f, selected = true)), maxUnits = 6f),
        )
        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)

        vm.toggleCourse(ruleChoiceId = 1L, courseChoiceId = 11L)

        assertThat(vm.rules.value.valueOrNull()!!.first().courses.first().isSelected).isFalse()
    }

    @Test
    fun `hasChanges is false on a freshly loaded draft`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions()
        coEvery { getStudyPlanDraft(any(), any(), any(), any()) } returns
            listOf(rule(1L, listOf(course(11L, "A"))))
        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)

        assertThat(vm.hasChanges()).isFalse()
    }

    @Test
    fun `hasChanges turns true after a selection diverges from the initial state`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions()
        coEvery { getStudyPlanDraft(any(), any(), any(), any()) } returns
            listOf(rule(1L, listOf(course(11L, "A"))))
        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)

        vm.toggleCourse(ruleChoiceId = 1L, courseChoiceId = 11L)

        assertThat(vm.hasChanges()).isTrue()
    }

    @Test
    fun `submit emits a Submitted event with the approval-flavoured message and clears the draft`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions(option(20L, PlanApprovalType.Automatic))
        coEvery { getStudyPlanDraft(careerId, 555L, 10L, 20L) } returns
            listOf(rule(1L, listOf(course(11L, "A"))))
        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)
        vm.selectSchema(20L)
        vm.loadSelectedRules()

        vm.events.test {
            vm.submit()
            val event = awaitItem()
            assertThat(event).isInstanceOf(StudyPlanEditEvent.Submitted::class.java)
            assertThat(((event as StudyPlanEditEvent.Submitted).message as it.attendance100.mybicocca.core.text.UiText.StringResource).resId)
                .isEqualTo(it.attendance100.mybicocca.R.string.studyplanedit_submitted_automatic)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { submitStudyPlan(careerId, any(), any()) }
        assertThat(vm.submitting.value).isFalse()
    }

    @Test
    fun `submit failure surfaces a friendly profile message`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions()
        coEvery { getStudyPlanDraft(any(), any(), any(), any()) } returns
            listOf(rule(1L, listOf(course(11L, "A"))))
        coEvery { submitStudyPlan(any(), any(), any()) } throws IllegalStateException("Security failed")
        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)

        vm.submit()

        assertThat((vm.submitError.value as it.attendance100.mybicocca.core.text.UiText.StringResource).resId)
            .isEqualTo(it.attendance100.mybicocca.R.string.studyplanedit_submit_error_profile)
        assertThat(vm.submitting.value).isFalse()
    }

    @Test
    fun `submit is a no-op when no rules are loaded`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions(option(20L), option(30L))
        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)

        vm.submit()

        coVerify(exactly = 0) { submitStudyPlan(any(), any(), any()) }
    }

    @Test
    fun `loadDraft failure sets Failed for the selected schema`() = runTest {
        val boom = IOException("offline")
        coEvery { getStudyPath(careerId) } returns pathWithOptions()
        coEvery { getStudyPlanDraft(careerId, 555L, 10L, 20L) } throws boom
        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)

        assertThat((vm.syncStatus.value as SyncStatus.Failed).cause).isEqualTo(boom)
    }

    @Test
    fun `setSegment records the visible wizard step`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions()
        coEvery { getStudyPlanDraft(any(), any(), any(), any()) } returns emptyList()
        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)

        vm.setSegment(7L)

        assertThat(vm.currentSegment.value).isEqualTo(7L)
    }

    @Test
    fun `reset rewinds the wizard to the percorso step and reloads the path`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions(option(20L), option(30L))
        val vm = StudyPlanEditViewModel(request(), getStudyPath, getStudyPlanDraft, submitStudyPlan)
        vm.setSegment(5L)

        vm.reset()

        assertThat(vm.currentSegment.value).isEqualTo(StudyPlanEditViewModel.PATH_SEGMENT)
        assertThat(vm.rules.value).isEqualTo(Loadable.NotYetLoaded)
        assertThat(vm.submitError.value).isNull()
    }

    @Test
    fun `a first-plan request with schemaId zero starts with no selected schema`() = runTest {
        coEvery { getStudyPath(careerId) } returns pathWithOptions(option(20L), option(30L))
        val vm = StudyPlanEditViewModel(
            request(schemaId = 0L, planId = 0L),
            getStudyPath,
            getStudyPlanDraft,
            submitStudyPlan,
        )

        assertThat(vm.selectedSchemaId.value).isNull()
    }
}
