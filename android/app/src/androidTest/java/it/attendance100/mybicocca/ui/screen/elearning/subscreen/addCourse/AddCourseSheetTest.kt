package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogCourse
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogNode
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogSection
import it.attendance100.mybicocca.domain.model.elearning.catalog.ElearningCatalog
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.catalog.LoadElearningCatalogUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.EnrolIntoCourseUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveEnrolledCoursesUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.CatalogStackEntry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * State and behaviour coverage for the add-course catalog browser. The sheet is driven by a real
 * [AddCourseViewModel] over MockK-faked use cases — the same construction the Wave 1
 * `AddCourseViewModelTest` uses. The root catalog landing is stubbed to its loading, error and
 * content states; the enrol behaviour pre-seeds the browse stack so an inside level lists an
 * enrollable course, then taps its enrol affordance and verifies the enrol use case fires and the
 * row advances out of the idle state. Anchored on [AddCourseTestTags] and wrapped in the shared
 * `setBicoccaContent` harness, which installs the app-wide CompositionLocals (HapticManager,
 * AppSnackbar controller, DeviceType) the sheet reads alongside the production theme. The sheet
 * receives an explicit ViewModel, bypassing its `hiltViewModel()` default.
 *
 * No `MainDispatcherRule`: like [AppLockScreenTest], this drives a real ViewModel through the
 * Compose tree, so `Dispatchers.Main` must stay the Robolectric main looper that the Compose test
 * clock pumps. Swapping it for a `TestDispatcher` decouples the `viewModelScope.launch` enrol
 * coroutine from `waitForIdle()`, so the enrol use-case call is never flushed before the verify.
 * Letting the looper drive both keeps a tap's resulting coroutine ordered behind `waitForIdle()`.
 */
@RunWith(AndroidJUnit4::class)
class AddCourseSheetTest {

    @get:Rule
    val compose = createComposeRule()

    private val accountId = AccountId("acc-1")

    private val account: Account = mockk(relaxed = true) {
        every { id } returns accountId
    }

    private val catalogCourse = CatalogCourse(
        id = CourseId(5),
        name = "Algebra Lineare",
        code = "2526-1-E1805",
        url = "https://elearning.unimib.it/course/view.php?id=5",
    )

    private val catalogNode = CatalogNode(
        id = "node-1",
        name = "Primo anno",
        url = null,
        children = emptyList(),
        courses = listOf(catalogCourse),
    )

    private val catalog = ElearningCatalog(
        sections = listOf(CatalogSection(name = "Catalogo Corsi", nodes = listOf(catalogNode))),
    )

    private val stackEntry = CatalogStackEntry(
        node = catalogNode,
        areaTileId = "node-1",
        accent = Color(0xFF0D733C),
    )

    private fun observeActiveAccount(value: Account? = account): ObserveActiveAccountUseCase =
        mockk { every { this@mockk.invoke() } returns MutableStateFlow<Account?>(value) }

    private fun observeEnrolledCourses(
        loadable: Loadable<List<EnrolledCourse>> = Loadable.Loaded(emptyList()),
    ): ObserveEnrolledCoursesUseCase =
        mockk { every { this@mockk.invoke(any()) } returns flowOf(loadable) }

    private fun loadCatalogUseCase(): LoadElearningCatalogUseCase {
        val useCase = mockk<LoadElearningCatalogUseCase>()
        coEvery { useCase.invoke() } returns catalog
        return useCase
    }

    private fun build(
        loadCatalog: LoadElearningCatalogUseCase = loadCatalogUseCase(),
        enrol: EnrolIntoCourseUseCase = mockk(relaxed = true),
    ): AddCourseViewModel = AddCourseViewModel(
        observeActiveAccount = observeActiveAccount(),
        observeEnrolledCourses = observeEnrolledCourses(),
        loadCatalog = loadCatalog,
        enrolIntoCourse = enrol,
    )

    private fun setSheet(viewModel: AddCourseViewModel) {
        compose.setBicoccaContent {
            AddCourseSheet(
                onDismiss = {},
                onEnrolFailed = {},
                onEnrolSucceeded = { _, _ -> },
                onRequireSignIn = {},
                viewModel = viewModel,
            )
        }
    }

    @Test
    fun root_shows_the_loading_state_while_the_catalog_load_is_in_flight() {
        val gate = CompletableDeferred<ElearningCatalog>()
        val loader = mockk<LoadElearningCatalogUseCase>()
        coEvery { loader.invoke() } coAnswers { gate.await() }
        val vm = build(loadCatalog = loader)
        setSheet(vm)

        compose.onNodeWithTag(AddCourseTestTags.ROOT_STATE_LOADING).assertIsDisplayed()
    }

    @Test
    fun root_shows_the_error_state_with_a_retry_when_the_catalog_load_fails() {
        val loader = mockk<LoadElearningCatalogUseCase>()
        coEvery { loader.invoke() } throws java.io.IOException("missing index")
        val vm = build(loadCatalog = loader)
        setSheet(vm)

        compose.onNodeWithTag(AddCourseTestTags.ROOT_STATE_ERROR).assertIsDisplayed()
        compose.onNodeWithTag(AddCourseTestTags.RETRY_BUTTON).assertExists()
    }

    @Test
    fun root_shows_the_area_grid_once_the_catalog_loads() {
        val vm = build()
        setSheet(vm)

        compose.onNodeWithTag(AddCourseTestTags.ROOT_STATE_CONTENT).assertIsDisplayed()
    }

    @Test
    fun an_inside_level_lists_an_enrollable_course_row() {
        val vm = build()
        vm.open(stackEntry)
        setSheet(vm)

        compose.onNodeWithTag(AddCourseTestTags.courseRow(5)).assertIsDisplayed()
    }

    @Test
    fun tapping_enrol_fires_the_enrol_use_case() {
        val inFlight = CompletableDeferred<Unit>()
        val enrol = mockk<EnrolIntoCourseUseCase>()
        coEvery { enrol.invoke(accountId, CourseId(5), any()) } coAnswers { inFlight.await() }
        val vm = build(enrol = enrol)
        vm.open(stackEntry)
        setSheet(vm)

        compose.waitUntil(timeoutMillis = 5000) {
            compose.onAllNodesWithTag(AddCourseTestTags.enrolButton(5))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithTag(AddCourseTestTags.enrolButton(5)).performClick()
        compose.waitForIdle()

        coVerify { enrol.invoke(accountId, CourseId(5), any()) }
    }
}
