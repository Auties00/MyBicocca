package it.attendance100.mybicocca.ui.screen.elearning

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourseGroup
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveAvailableStudyYearsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveCourseFilterUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveEnrolledCoursesUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveFilteredCoursesUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.RefreshEnrolledCoursesUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.SetCourseFilterUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.SetCourseHiddenUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ToggleCourseFavouriteUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import io.mockk.coEvery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * State and behaviour coverage for the e-learning tab home. The screen is driven by a real
 * [ElearningViewModel] over MockK-faked use cases — the same construction the Wave 1
 * `ElearningViewModelTest` uses — stubbed per test to render the cold-cache loading view, the
 * sync-failure view, the empty/filtered-empty view, and the loaded course grid. Behaviour tests
 * tap a tagged filter toggle and verify it delegates to the filter use case. Anchored on
 * [ElearningTestTags] and rendered via `setBicoccaContent`, which installs the app-wide
 * CompositionLocals (haptics, snackbar controller, device type) and the production theme.
 *
 * The add-course FAB opens a sheet that self-creates a `hiltViewModel`, so the FAB is asserted as
 * present and clickable but never actually tapped (no Hilt in a unit test).
 */
@RunWith(AndroidJUnit4::class)
class ElearningScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val accountId = AccountId("acc-1")

    private val account: Account = mockk(relaxed = true) {
        every { id } returns accountId
    }

    private fun course(
        id: Int,
        idNumber: String? = "2526-1-E1234",
        favourite: Boolean = false,
        hidden: Boolean = false,
    ): EnrolledCourse = EnrolledCourse(
        id = CourseId(id),
        shortName = "S$id",
        fullName = "Course $id",
        displayName = "Course $id",
        idNumber = idNumber,
        summary = null,
        courseImageUrl = null,
        format = null,
        language = null,
        categoryId = null,
        progress = null,
        completed = false,
        completionEnabled = false,
        startDate = null,
        endDate = null,
        lastAccessDate = null,
        isFavourite = favourite,
        hidden = hidden,
    )

    private fun observeActiveAccount(value: Account? = account): ObserveActiveAccountUseCase =
        mockk { every { this@mockk.invoke() } returns MutableStateFlow<Account?>(value) }

    private fun observeCourseFilter(filter: CourseFilter = CourseFilter.All): ObserveCourseFilterUseCase =
        mockk { every { this@mockk.invoke() } returns flowOf(filter) }

    private fun observeEnrolledCourses(
        loadable: Loadable<List<EnrolledCourse>> = Loadable.NotYetLoaded,
    ): ObserveEnrolledCoursesUseCase =
        mockk { every { this@mockk.invoke(any()) } returns MutableStateFlow(loadable) }

    private fun observeFilteredCourses(
        loadable: Loadable<List<EnrolledCourseGroup>> = Loadable.NotYetLoaded,
    ): ObserveFilteredCoursesUseCase =
        mockk { every { this@mockk.invoke(any()) } returns MutableStateFlow(loadable) }

    private fun observeAvailableStudyYears(
        years: List<StudyYear> = emptyList(),
    ): ObserveAvailableStudyYearsUseCase =
        mockk { every { this@mockk.invoke(any()) } returns MutableStateFlow(years) }

    private val setFilter = mockk<SetCourseFilterUseCase>(relaxed = true)

    private fun build(
        activeAccount: ObserveActiveAccountUseCase = observeActiveAccount(),
        courseFilter: ObserveCourseFilterUseCase = observeCourseFilter(),
        enrolled: ObserveEnrolledCoursesUseCase = observeEnrolledCourses(),
        filtered: ObserveFilteredCoursesUseCase = observeFilteredCourses(),
        studyYears: ObserveAvailableStudyYearsUseCase = observeAvailableStudyYears(),
        refresh: RefreshEnrolledCoursesUseCase = mockk(relaxed = true),
    ): ElearningViewModel = ElearningViewModel(
        observeActiveAccount = activeAccount,
        observeCourseFilter = courseFilter,
        observeEnrolledCourses = enrolled,
        observeFilteredCourses = filtered,
        observeAvailableStudyYears = studyYears,
        refreshEnrolledCourses = refresh,
        setCourseFilter = setFilter,
        toggleCourseFavourite = mockk(relaxed = true),
        setCourseHidden = mockk(relaxed = true),
    )

    private fun setScreen(viewModel: ElearningViewModel) {
        compose.setBicoccaContent {
            ElearningScreen(viewModel = viewModel)
        }
    }

    @Test
    fun cold_cache_renders_the_loading_state() {
        val refresh = mockk<RefreshEnrolledCoursesUseCase>()
        coEvery { refresh.invoke(accountId, false) } returns Unit
        val vm = build(
            enrolled = observeEnrolledCourses(Loadable.NotYetLoaded),
            refresh = refresh,
        )
        setScreen(vm)

        compose.onNodeWithTag(ElearningTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(ElearningTestTags.STATE_LOADING).assertIsDisplayed()
    }

    @Test
    fun cold_cache_refresh_failure_renders_the_error_state() {
        val refresh = mockk<RefreshEnrolledCoursesUseCase>()
        coEvery { refresh.invoke(accountId, false) } throws IOException("offline")
        val vm = build(
            enrolled = observeEnrolledCourses(Loadable.Loaded(emptyList())),
            refresh = refresh,
        )
        setScreen(vm)

        compose.onNodeWithTag(ElearningTestTags.STATE_ERROR).assertIsDisplayed()
    }

    @Test
    fun empty_cache_after_a_successful_refresh_renders_the_empty_state() {
        val refresh = mockk<RefreshEnrolledCoursesUseCase>()
        coEvery { refresh.invoke(accountId, false) } returns Unit
        val vm = build(
            enrolled = observeEnrolledCourses(Loadable.Loaded(emptyList())),
            filtered = observeFilteredCourses(Loadable.Loaded(emptyList())),
            refresh = refresh,
        )
        setScreen(vm)

        compose.onNodeWithTag(ElearningTestTags.STATE_EMPTY).assertIsDisplayed()
        compose.onNodeWithTag(ElearningTestTags.FILTER_BAR).assertIsDisplayed()
    }

    @Test
    fun loaded_courses_render_the_content_grid_with_a_keyed_row_and_the_FAB() {
        val group = EnrolledCourseGroup(editions = listOf(course(1)))
        val refresh = mockk<RefreshEnrolledCoursesUseCase>()
        coEvery { refresh.invoke(accountId, false) } returns Unit
        val vm = build(
            enrolled = observeEnrolledCourses(Loadable.Loaded(listOf(course(1)))),
            filtered = observeFilteredCourses(Loadable.Loaded(listOf(group))),
            refresh = refresh,
        )
        setScreen(vm)

        compose.onNodeWithTag(ElearningTestTags.STATE_CONTENT).assertIsDisplayed()
        compose.onNodeWithTag(ElearningTestTags.courseItem(1)).assertIsDisplayed()
        compose.onNodeWithTag(ElearningTestTags.ADD_COURSE_FAB).assertHasClickAction()
    }

    @Test
    fun tapping_the_Preferiti_filter_delegates_to_the_filter_use_case() {
        val group = EnrolledCourseGroup(editions = listOf(course(1)))
        val refresh = mockk<RefreshEnrolledCoursesUseCase>()
        coEvery { refresh.invoke(accountId, false) } returns Unit
        val vm = build(
            enrolled = observeEnrolledCourses(Loadable.Loaded(listOf(course(1)))),
            filtered = observeFilteredCourses(Loadable.Loaded(listOf(group))),
            refresh = refresh,
        )
        setScreen(vm)

        compose.onNodeWithTag(ElearningTestTags.filterOption("favourites")).performClick()
        compose.waitForIdle()

        coVerify { setFilter(CourseFilter.Favourites) }
    }
}
