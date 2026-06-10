package it.attendance100.mybicocca.ui.screen.search

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.search.DictationEvent
import it.attendance100.mybicocca.domain.model.search.SearchAction
import it.attendance100.mybicocca.domain.model.search.SearchHistoryEntry
import it.attendance100.mybicocca.domain.model.search.SearchResult
import it.attendance100.mybicocca.domain.model.search.SpeechTranscript
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.search.AddSearchHistoryEntryUseCase
import it.attendance100.mybicocca.domain.usecase.search.ClearSearchHistoryUseCase
import it.attendance100.mybicocca.domain.usecase.search.DictateUseCase
import it.attendance100.mybicocca.domain.usecase.search.GlobalSearchUseCase
import it.attendance100.mybicocca.domain.usecase.search.ObserveSearchHistoryUseCase
import it.attendance100.mybicocca.domain.usecase.search.RemoveSearchHistoryEntryUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/**
 * Coverage for the global search overlay ViewModel: history reflects the active account,
 * the query is persisted user input, submitted queries and tapped results commit to history,
 * removals and clears scope to the active account, and dictation streams transcripts straight
 * into the query while tracking the dictating flag.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accountId = AccountId("acc-1")
    private val careerId = CareerId(101L)

    private val account = Account(
        id = accountId,
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

    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()
    private val globalSearch: GlobalSearchUseCase = mockk()
    private val observeSearchHistory: ObserveSearchHistoryUseCase = mockk()
    private val addHistoryEntry: AddSearchHistoryEntryUseCase = mockk(relaxed = true)
    private val removeHistoryEntry: RemoveSearchHistoryEntryUseCase = mockk(relaxed = true)
    private val clearHistoryEntries: ClearSearchHistoryUseCase = mockk(relaxed = true)
    private val dictate: DictateUseCase = mockk()

    private fun viewModel(
        savedState: SavedStateHandle = SavedStateHandle(),
        activeAccount: Account? = account,
        history: List<SearchHistoryEntry> = emptyList(),
        results: List<SearchResult> = emptyList(),
    ): SearchViewModel {
        every { observeActiveAccount() } returns flowOf(activeAccount)
        every { observeSearchHistory(any()) } returns flowOf(history)
        every { globalSearch(any(), any(), any()) } returns flowOf(results)
        every { dictate() } returns emptyFlow()
        return SearchViewModel(
            savedState = savedState,
            observeActiveAccount = observeActiveAccount,
            globalSearch = globalSearch,
            observeSearchHistory = observeSearchHistory,
            addHistoryEntry = addHistoryEntry,
            removeHistoryEntry = removeHistoryEntry,
            clearHistoryEntries = clearHistoryEntries,
            dictate = dictate,
        )
    }

    @Test
    fun `setQuery persists the query through saved state`() = runTest {
        val vm = viewModel()

        vm.setQuery("analisi")

        assertThat(vm.query.value).isEqualTo("analisi")
    }

    @Test
    fun `history follows the active account`() = runTest {
        val entry = SearchHistoryEntry("analisi", Instant.EPOCH)
        val vm = viewModel(history = listOf(entry))

        vm.history.test {
            assertThat(awaitItem()).containsExactly(entry)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `history is empty with no active account`() = runTest {
        val vm = viewModel(activeAccount = null)

        vm.history.test {
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `submit commits the trimmed query to history`() = runTest {
        val savedState = SavedStateHandle().apply { set("search_query", "  analisi  ") }
        val vm = viewModel(savedState)

        vm.submit()

        coVerify { addHistoryEntry(accountId, "analisi") }
    }

    @Test
    fun `submit drops a blank query`() = runTest {
        val savedState = SavedStateHandle().apply { set("search_query", "   ") }
        val vm = viewModel(savedState)

        vm.submit()

        coVerify(exactly = 0) { addHistoryEntry(any(), any(), any()) }
    }

    @Test
    fun `commitPick records the query and result key pair`() = runTest {
        val savedState = SavedStateHandle().apply { set("search_query", "analisi") }
        val vm = viewModel(savedState)
        val result = SearchResult.Action(
            action = SearchAction.ChangeTheme,
            title = "Apri impostazioni",
            subtitle = null,
            score = 0.9,
        )

        vm.commitPick(result)

        coVerify { addHistoryEntry(accountId, "analisi", result.key) }
    }

    @Test
    fun `commitPick drops a blank query`() = runTest {
        val savedState = SavedStateHandle().apply { set("search_query", "") }
        val vm = viewModel(savedState)
        val result = SearchResult.CalendarDay(
            date = LocalDate.now(),
            title = "Domani",
            subtitle = null,
            score = 0.95,
        )

        vm.commitPick(result)

        coVerify(exactly = 0) { addHistoryEntry(any(), any(), any()) }
    }

    @Test
    fun `removeFromHistory removes the entry for the active account`() = runTest {
        val vm = viewModel()

        vm.removeFromHistory("analisi")

        coVerify { removeHistoryEntry(accountId, "analisi") }
    }

    @Test
    fun `clearHistory clears the active account history`() = runTest {
        val vm = viewModel()

        vm.clearHistory()

        coVerify { clearHistoryEntries(accountId) }
    }

    @Test
    fun `removeFromHistory is a no-op without an active account`() = runTest {
        val vm = viewModel(activeAccount = null)

        vm.removeFromHistory("analisi")

        coVerify(exactly = 0) { removeHistoryEntry(any(), any()) }
    }

    @Test
    fun `startDictation flips dictating and streams transcripts into the query`() = runTest {
        val events = Channel<DictationEvent>(Channel.UNLIMITED)
        val vm = viewModel()
        every { dictate() } returns events.consumeAsFlow()

        vm.startDictation()
        assertThat(vm.dictating.value).isTrue()

        events.send(DictationEvent.Transcript(SpeechTranscript("appello", isFinal = false)))
        assertThat(vm.query.value).isEqualTo("appello")

        events.send(DictationEvent.SoundLevel(0.5f))
        assertThat(vm.soundLevel.value).isEqualTo(0.5f)

        events.close()
        assertThat(vm.dictating.value).isFalse()
    }

    @Test
    fun `startDictation is ignored while already dictating`() = runTest {
        val flow = MutableSharedFlow<DictationEvent>()
        val vm = viewModel()
        every { dictate() } returns flow

        vm.startDictation()
        vm.startDictation()

        assertThat(vm.dictating.value).isTrue()
    }

    @Test
    fun `stopDictation ends the session and keeps the query`() = runTest {
        val flow = MutableSharedFlow<DictationEvent>()
        val savedState = SavedStateHandle().apply { set("search_query", "appello") }
        val vm = viewModel(savedState)
        every { dictate() } returns flow

        vm.startDictation()
        vm.stopDictation()

        assertThat(vm.dictating.value).isFalse()
        assertThat(vm.soundLevel.value).isEqualTo(0f)
        assertThat(vm.query.value).isEqualTo("appello")
    }

    @Test
    fun `reset stops dictation and blanks the query`() = runTest {
        val savedState = SavedStateHandle().apply { set("search_query", "appello") }
        val vm = viewModel(savedState)

        vm.reset()

        assertThat(vm.query.value).isEmpty()
        assertThat(vm.dictating.value).isFalse()
    }
}
