package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireAnswer
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireCompilationStart
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireOption
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnairePage
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireParagraph
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireQuestion
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireQuestionKind
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSession
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSummary
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.ConfirmQuestionnaireUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.GetNextQuestionnairePageUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.GetPreviousQuestionnairePageUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.GetQuestionnaireSummaryUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.SaveQuestionnairePageUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.StartQuestionnaireUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionnaireCompilationEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionnaireCompilationRequest
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionnaireCompilationStep
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

/**
 * State-machine coverage for the server-driven questionnaire compiler: the start ->
 * page -> summary -> confirm pager (eventCompId session held in-VM), the -1 end-marker
 * driving the summary, empty save skipping, mandatory-gate on forward navigation,
 * multi-choice cap and toggle-to-clear, back-from-summary cache restore, and confirm
 * gated on completeness.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QuestionnaireCompilationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(101L)

    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()
    private val startQuestionnaire: StartQuestionnaireUseCase = mockk()
    private val savePage: SaveQuestionnairePageUseCase = mockk(relaxed = true)
    private val getNextPage: GetNextQuestionnairePageUseCase = mockk()
    private val getPreviousPage: GetPreviousQuestionnairePageUseCase = mockk()
    private val getSummary: GetQuestionnaireSummaryUseCase = mockk()
    private val confirmQuestionnaire: ConfirmQuestionnaireUseCase = mockk(relaxed = true)

    private val session = QuestionnaireSession(
        careerId = careerId,
        activityChoiceId = 7L,
        questionnaireId = 1,
        questionnaireConfigId = 2,
        compilationId = 900L,
        userCompilationId = 901L,
    )

    private fun request() = QuestionnaireCompilationRequest(
        activityChoiceId = 7L,
        questionnaireId = 1,
        questionnaireConfigId = 2,
        anonymous = true,
        tags = "tag",
        activityName = "Analisi",
        lecturerName = "Prof Bianchi",
        partitionName = null,
    )

    private fun singleChoiceQuestion(
        id: Long,
        mandatory: Boolean = true,
        kind: QuestionnaireQuestionKind = QuestionnaireQuestionKind.SingleChoice,
        options: List<QuestionnaireOption> = listOf(
            QuestionnaireOption(id = id * 10 + 1, text = "Sì", requiresFreeText = false),
            QuestionnaireOption(id = id * 10 + 2, text = "No", requiresFreeText = false),
        ),
    ) = QuestionnaireQuestion(
        id = id,
        text = "Question $id",
        note = null,
        mandatory = mandatory,
        kind = kind,
        options = options,
        savedOptionIds = emptySet(),
        savedFreeText = null,
    )

    private fun page(id: Long, isEnd: Boolean = false, questions: List<QuestionnaireQuestion> = emptyList()) =
        QuestionnairePage(
            id = id,
            isEnd = isEnd,
            paragraphs = listOf(QuestionnaireParagraph(id = id, title = null, questions = questions)),
        )

    private fun viewModel(firstPage: QuestionnairePage): QuestionnaireCompilationViewModel {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { startQuestionnaire(any(), any()) } returns
            QuestionnaireCompilationStart(session, firstPage)
        return QuestionnaireCompilationViewModel(
            key = request(),
            observeActiveAccount = observeActiveAccount,
            startQuestionnaire = startQuestionnaire,
            savePage = savePage,
            getNextPage = getNextPage,
            getPreviousPage = getPreviousPage,
            getSummary = getSummary,
            confirmQuestionnaire = confirmQuestionnaire,
        )
    }

    @Test
    fun `start lands on the first page at index zero`() = runTest {
        val vm = viewModel(page(1L, questions = listOf(singleChoiceQuestion(100L))))

        val step = vm.step.value
        assertThat(step).isInstanceOf(QuestionnaireCompilationStep.Page::class.java)
        assertThat((step as QuestionnaireCompilationStep.Page).index).isEqualTo(0)
        assertThat(step.page.id).isEqualTo(1L)
    }

    @Test
    fun `a start failure parks on StartFailed and retryStart re-attempts`() = runTest {
        val boom = IOException("offline")
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { startQuestionnaire(any(), any()) } throws boom

        val vm = QuestionnaireCompilationViewModel(
            request(), observeActiveAccount, startQuestionnaire, savePage,
            getNextPage, getPreviousPage, getSummary, confirmQuestionnaire,
        )

        assertThat(vm.step.value).isInstanceOf(QuestionnaireCompilationStep.StartFailed::class.java)
        assertThat((vm.step.value as QuestionnaireCompilationStep.StartFailed).cause).isEqualTo(boom)

        coEvery { startQuestionnaire(any(), any()) } returns
            QuestionnaireCompilationStart(session, page(1L, questions = listOf(singleChoiceQuestion(100L))))
        vm.retryStart()

        assertThat(vm.step.value).isInstanceOf(QuestionnaireCompilationStep.Page::class.java)
    }

    @Test
    fun `next blocks and emits MissingAnswers when a mandatory question is unanswered`() = runTest {
        val vm = viewModel(page(1L, questions = listOf(singleChoiceQuestion(100L, mandatory = true))))

        vm.events.test {
            vm.next()
            assertThat(awaitItem()).isEqualTo(QuestionnaireCompilationEvent.MissingAnswers)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.invalidQuestionIds.value).containsExactly(100L)
        assertThat(vm.step.value).isInstanceOf(QuestionnaireCompilationStep.Page::class.java)
        coVerify(exactly = 0) { getNextPage(any(), any()) }
    }

    @Test
    fun `answering then next advances to the server's following page`() = runTest {
        val q = singleChoiceQuestion(100L)
        val vm = viewModel(page(1L, questions = listOf(q)))
        coEvery { getNextPage(session, 1L) } returns page(2L, questions = listOf(singleChoiceQuestion(200L)))

        vm.selectOption(q, q.options.first())
        vm.next()

        val step = vm.step.value
        assertThat(step).isInstanceOf(QuestionnaireCompilationStep.Page::class.java)
        assertThat((step as QuestionnaireCompilationStep.Page).index).isEqualTo(1)
        assertThat(step.page.id).isEqualTo(2L)
        coVerify { savePage(session, 1L, any()) }
    }

    @Test
    fun `an end-marker next page transitions to the summary`() = runTest {
        val q = singleChoiceQuestion(100L)
        val vm = viewModel(page(1L, questions = listOf(q)))
        coEvery { getNextPage(session, 1L) } returns page(-1L, isEnd = true)
        coEvery { getSummary(session) } returns QuestionnaireSummary(complete = true)

        vm.selectOption(q, q.options.first())
        vm.next()

        val step = vm.step.value
        assertThat(step).isInstanceOf(QuestionnaireCompilationStep.Summary::class.java)
        assertThat((step as QuestionnaireCompilationStep.Summary).complete).isTrue()
    }

    @Test
    fun `save is skipped when the page has no answers to send`() = runTest {
        val q = singleChoiceQuestion(100L, mandatory = false)
        val vm = viewModel(page(1L, questions = listOf(q)))
        coEvery { getNextPage(session, 1L) } returns page(2L, questions = listOf(singleChoiceQuestion(200L)))

        vm.next()

        coVerify(exactly = 0) { savePage(any(), any(), any()) }
    }

    @Test
    fun `the save payload carries the selected option with empty free text`() = runTest {
        val q = singleChoiceQuestion(100L)
        val vm = viewModel(page(1L, questions = listOf(q)))
        coEvery { getNextPage(session, 1L) } returns page(2L, questions = listOf(singleChoiceQuestion(200L)))
        val captured = slot<List<QuestionnaireAnswer>>()
        coEvery { savePage(session, 1L, capture(captured)) } returns Unit

        vm.selectOption(q, q.options.first())
        vm.next()

        assertThat(captured.captured).hasSize(1)
        val answer = captured.captured.first()
        assertThat(answer.questionId).isEqualTo(100L)
        assertThat(answer.optionId).isEqualTo(q.options.first().id)
        assertThat(answer.freeText).isEmpty()
    }

    @Test
    fun `multi-choice respects the maxSelections cap`() = runTest {
        val q = QuestionnaireQuestion(
            id = 100L,
            text = "pick two",
            note = null,
            mandatory = false,
            kind = QuestionnaireQuestionKind.MultiChoice(maxSelections = 2),
            options = listOf(
                QuestionnaireOption(1L, "A", false),
                QuestionnaireOption(2L, "B", false),
                QuestionnaireOption(3L, "C", false),
            ),
            savedOptionIds = emptySet(),
            savedFreeText = null,
        )
        val vm = viewModel(page(1L, questions = listOf(q)))

        vm.selectOption(q, q.options[0])
        vm.selectOption(q, q.options[1])
        vm.selectOption(q, q.options[2])

        assertThat(vm.answers.value[100L]!!.selectedOptionIds).containsExactly(1L, 2L)
    }

    @Test
    fun `re-tapping a single-choice option clears it`() = runTest {
        val q = singleChoiceQuestion(100L)
        val vm = viewModel(page(1L, questions = listOf(q)))

        vm.selectOption(q, q.options.first())
        vm.selectOption(q, q.options.first())

        assertThat(vm.answers.value[100L]!!.selectedOptionIds).isEmpty()
    }

    @Test
    fun `setFreeText clears the invalid flag for that question`() = runTest {
        val q = singleChoiceQuestion(100L, mandatory = true)
        val vm = viewModel(page(1L, questions = listOf(q)))
        vm.next()
        assertThat(vm.invalidQuestionIds.value).containsExactly(100L)

        vm.setFreeText(100L, "anything")

        assertThat(vm.invalidQuestionIds.value).isEmpty()
    }

    @Test
    fun `back from the summary restores the cached last page`() = runTest {
        val q = singleChoiceQuestion(100L)
        val vm = viewModel(page(1L, questions = listOf(q)))
        coEvery { getNextPage(session, 1L) } returns page(-1L, isEnd = true)
        coEvery { getSummary(session) } returns QuestionnaireSummary(complete = true)
        vm.selectOption(q, q.options.first())
        vm.next()

        vm.back()

        val step = vm.step.value
        assertThat(step).isInstanceOf(QuestionnaireCompilationStep.Page::class.java)
        assertThat((step as QuestionnaireCompilationStep.Page).page.id).isEqualTo(1L)
        coVerify(exactly = 0) { getPreviousPage(any(), any()) }
    }

    @Test
    fun `back on the first page does nothing`() = runTest {
        val vm = viewModel(page(1L, questions = listOf(singleChoiceQuestion(100L))))

        vm.back()

        assertThat((vm.step.value as QuestionnaireCompilationStep.Page).index).isEqualTo(0)
        coVerify(exactly = 0) { getPreviousPage(any(), any()) }
    }

    @Test
    fun `confirm is rejected while the summary is incomplete`() = runTest {
        val q = singleChoiceQuestion(100L, mandatory = false)
        val vm = viewModel(page(1L, questions = listOf(q)))
        coEvery { getNextPage(session, 1L) } returns page(-1L, isEnd = true)
        coEvery { getSummary(session) } returns QuestionnaireSummary(complete = false)
        vm.next()

        vm.confirm()

        coVerify(exactly = 0) { confirmQuestionnaire(any()) }
    }

    @Test
    fun `confirm on a complete summary fires the Confirmed event`() = runTest {
        val q = singleChoiceQuestion(100L)
        val vm = viewModel(page(1L, questions = listOf(q)))
        coEvery { getNextPage(session, 1L) } returns page(-1L, isEnd = true)
        coEvery { getSummary(session) } returns QuestionnaireSummary(complete = true)
        vm.selectOption(q, q.options.first())
        vm.next()

        vm.events.test {
            vm.confirm()
            assertThat(awaitItem()).isEqualTo(QuestionnaireCompilationEvent.Confirmed)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { confirmQuestionnaire(session) }
    }

    @Test
    fun `enterPage seeds edits from the server's saved answers`() = runTest {
        val q = singleChoiceQuestion(100L).copy(savedOptionIds = setOf(1001L), savedFreeText = "prior")
        val vm = viewModel(page(1L, questions = listOf(q)))

        val state = vm.answers.value[100L]!!
        assertThat(state.selectedOptionIds).containsExactly(1001L)
        assertThat(state.freeText).isEqualTo("prior")
    }

    @Test
    fun `request display facets are exposed verbatim`() = runTest {
        val vm = viewModel(page(1L, questions = listOf(singleChoiceQuestion(100L))))

        assertThat(vm.activityName).isEqualTo("Analisi")
        assertThat(vm.lecturerName).isEqualTo("Prof Bianchi")
        assertThat(vm.anonymous).isTrue()
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
