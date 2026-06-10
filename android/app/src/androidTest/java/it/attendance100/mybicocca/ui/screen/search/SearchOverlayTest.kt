package it.attendance100.mybicocca.ui.screen.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.search.SearchAction
import it.attendance100.mybicocca.domain.model.search.SearchDestination
import it.attendance100.mybicocca.domain.model.search.SearchHistoryEntry
import it.attendance100.mybicocca.domain.model.search.SearchResult
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.search.AddSearchHistoryEntryUseCase
import it.attendance100.mybicocca.domain.usecase.search.ClearSearchHistoryUseCase
import it.attendance100.mybicocca.domain.usecase.search.DictateUseCase
import it.attendance100.mybicocca.domain.usecase.search.GlobalSearchUseCase
import it.attendance100.mybicocca.domain.usecase.search.ObserveSearchHistoryUseCase
import it.attendance100.mybicocca.domain.usecase.search.RemoveSearchHistoryEntryUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * State and behaviour coverage for the global search overlay. The overlay renders four mutually
 * exclusive branches keyed on the query and result list: recent-search history, an empty-history
 * state, a no-matches state, and the ranked results group (with an optional hero top-hit). The
 * search text field lives in the shell app bar rather than in the overlay, so the query is driven
 * through the ViewModel's saved state; tapping a result invokes the [SearchOverlay.onOpenResult]
 * navigation hook, verified through a spy. The overlay is driven by a real [SearchViewModel] over
 * faked use cases (the construction mirrors `SearchViewModelTest`), anchored on
 * [SearchOverlayTestTags], and wrapped in [setBicoccaContent] so the app-wide locals the shell
 * normally provides (haptics, snackbar controller, device type) are installed.
 *
 * The overlay's branches are full-viewport `LazyColumn`s and `fillParentMaxSize` empty boxes; the
 * container/full-bleed nodes report `isDisplayed = false` under Robolectric's headless renderer even
 * though the branch renders (its row/card children are laid out and clickable — see the tap tests).
 * State coverage therefore anchors the displayed assertion on a finitely-sized child (a history row,
 * a result row) where one exists, and verifies the mutually exclusive branch through marker presence
 * and the sibling markers' absence rather than the flaky container geometry.
 */
@RunWith(AndroidJUnit4::class)
class SearchOverlayTest {

    @get:Rule
    val compose = createComposeRule()

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
        query: String = "",
        history: List<SearchHistoryEntry> = emptyList(),
        results: List<SearchResult> = emptyList(),
    ): SearchViewModel {
        every { observeActiveAccount() } returns flowOf(account)
        every { observeSearchHistory(any()) } returns flowOf(history)
        every { globalSearch(any(), any(), any()) } returns flowOf(results)
        every { dictate() } returns emptyFlow()
        return SearchViewModel(
            savedState = SavedStateHandle().apply { set("search_query", query) },
            observeActiveAccount = observeActiveAccount,
            globalSearch = globalSearch,
            observeSearchHistory = observeSearchHistory,
            addHistoryEntry = addHistoryEntry,
            removeHistoryEntry = removeHistoryEntry,
            clearHistoryEntries = clearHistoryEntries,
            dictate = dictate,
        )
    }

    private fun setOverlay(
        vm: SearchViewModel,
        onOpenResult: (SearchResult) -> Unit = mockk(relaxed = true),
    ) {
        compose.setBicoccaContent {
            SearchOverlay(
                viewModel = vm,
                progress = 1f,
                subPageProgress = 0f,
                topInset = 0.dp,
                onOpenResult = onOpenResult,
            )
        }
    }

    /**
     * Asserts a state-marker node is in the tree. The branch markers (the `LazyColumn` carrying
     * [SearchOverlayTestTags.HISTORY_LIST]/[SearchOverlayTestTags.RESULTS_LIST] and the
     * `fillParentMaxSize` empty boxes) sit on non-clickable nodes inside the overlay root, whose
     * no-op `clickable` (swallowing taps for the tab underneath) makes the root a `mergeDescendants`
     * boundary that absorbs every non-merging descendant into a single merged node. The container
     * markers therefore only exist on the unmerged tree, so the branch is verified there through node
     * presence; a finitely-sized child (row/card) carries the displayed assertion where one exists.
     */
    private fun assertMarkerPresent(tag: String) {
        assertThat(compose.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes())
            .isNotEmpty()
    }

    /** Asserts a state-marker node is not in the (unmerged) tree, proving the branch is mutually exclusive. */
    private fun assertMarkerAbsent(tag: String) {
        assertThat(compose.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes())
            .isEmpty()
    }

    @Test
    fun a_blank_query_with_history_shows_the_recent_searches() {
        val vm = viewModel(history = listOf(SearchHistoryEntry("analisi", Instant.EPOCH)))
        setOverlay(vm)
        compose.waitForIdle()

        compose.onNodeWithTag(SearchOverlayTestTags.ROOT).assertIsDisplayed()
        assertMarkerPresent(SearchOverlayTestTags.HISTORY_LIST)
        assertMarkerAbsent(SearchOverlayTestTags.HISTORY_EMPTY)
        compose.onNodeWithTag(SearchOverlayTestTags.historyItem("analisi"), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun a_blank_query_with_no_history_shows_the_empty_history_state() {
        val vm = viewModel(history = emptyList())
        setOverlay(vm)
        compose.waitForIdle()

        assertMarkerPresent(SearchOverlayTestTags.HISTORY_LIST)
        assertMarkerPresent(SearchOverlayTestTags.HISTORY_EMPTY)
        assertMarkerAbsent(SearchOverlayTestTags.NO_RESULTS)
        assertMarkerAbsent(SearchOverlayTestTags.RESULTS_LIST)
    }

    @Test
    fun a_non_blank_query_with_no_matches_shows_the_no_results_state() {
        val vm = viewModel(query = "zzzz", results = emptyList())
        setOverlay(vm)
        compose.waitForIdle()

        assertMarkerPresent(SearchOverlayTestTags.RESULTS_LIST)
        assertMarkerPresent(SearchOverlayTestTags.NO_RESULTS)
        assertMarkerAbsent(SearchOverlayTestTags.HISTORY_EMPTY)
        assertMarkerAbsent(SearchOverlayTestTags.HISTORY_LIST)
    }

    @Test
    fun a_non_blank_query_with_matches_shows_the_ranked_result_rows() {
        val result = SearchResult.Destination(
            destination = SearchDestination.TabCalendar,
            title = "Calendario",
            subtitle = null,
            score = 0.4,
        )
        val vm = viewModel(query = "cale", results = listOf(result))
        setOverlay(vm)
        compose.waitForIdle()

        assertMarkerPresent(SearchOverlayTestTags.RESULTS_LIST)
        assertMarkerAbsent(SearchOverlayTestTags.NO_RESULTS)
        compose.onNodeWithTag(SearchOverlayTestTags.resultItem(result.key)).assertIsDisplayed()
    }

    @Test
    fun tapping_a_result_row_invokes_the_open_result_hook() {
        val onOpenResult: (SearchResult) -> Unit = mockk(relaxed = true)
        val result = SearchResult.Destination(
            destination = SearchDestination.TabCalendar,
            title = "Calendario",
            subtitle = null,
            score = 0.4,
        )
        val vm = viewModel(query = "cale", results = listOf(result))
        setOverlay(vm, onOpenResult)
        compose.waitForIdle()

        compose.onNodeWithTag(SearchOverlayTestTags.resultItem(result.key)).performClick()
        compose.waitForIdle()

        verify { onOpenResult(result) }
    }

    @Test
    fun tapping_the_hero_top_hit_invokes_the_open_result_hook() {
        val onOpenResult: (SearchResult) -> Unit = mockk(relaxed = true)
        val top = SearchResult.Action(
            action = SearchAction.ChangeTheme,
            title = "Cambia tema",
            subtitle = null,
            score = 0.95,
        )
        val vm = viewModel(query = "tema", results = listOf(top))
        setOverlay(vm, onOpenResult)
        compose.waitForIdle()

        compose.onNodeWithTag(SearchOverlayTestTags.TOP_HIT).assertIsDisplayed()
        compose.onNodeWithTag(SearchOverlayTestTags.TOP_HIT).performClick()
        compose.waitForIdle()

        verify { onOpenResult(top) }
    }
}
