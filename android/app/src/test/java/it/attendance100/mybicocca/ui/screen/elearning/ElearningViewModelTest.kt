package it.attendance100.mybicocca.ui.screen.elearning

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourseGroup
import it.attendance100.mybicocca.domain.model.elearning.deadline.Deadline
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
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
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.elearning.state.ElearningOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.state.InitialFetchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

/**
 * Covers the e-learning tab ViewModel: Loadable/SyncStatus/InitialFetch derivation, the auto and
 * pull-to-refresh flows, the one-shot navigation channel, and the reveal-course logic.
 */
class ElearningViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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

    private fun build(
        activeAccount: ObserveActiveAccountUseCase = observeActiveAccount(),
        courseFilter: ObserveCourseFilterUseCase = observeCourseFilter(),
        enrolled: ObserveEnrolledCoursesUseCase = observeEnrolledCourses(),
        filtered: ObserveFilteredCoursesUseCase = observeFilteredCourses(),
        studyYears: ObserveAvailableStudyYearsUseCase = observeAvailableStudyYears(),
        refresh: RefreshEnrolledCoursesUseCase = mockk(relaxed = true),
        setFilter: SetCourseFilterUseCase = mockk(relaxed = true),
        toggleFavourite: ToggleCourseFavouriteUseCase = mockk(relaxed = true),
        setHidden: SetCourseHiddenUseCase = mockk(relaxed = true),
    ): ElearningViewModel = ElearningViewModel(
        observeActiveAccount = activeAccount,
        observeCourseFilter = courseFilter,
        observeEnrolledCourses = enrolled,
        observeFilteredCourses = filtered,
        observeAvailableStudyYears = studyYears,
        refreshEnrolledCourses = refresh,
        setCourseFilter = setFilter,
        toggleCourseFavourite = toggleFavourite,
        setCourseHidden = setHidden,
    )

    @Test
    fun `null account keeps visibleCourses NotYetLoaded`() = runTest {
        val vm = build(activeAccount = observeActiveAccount(value = null))
        assertThat(vm.visibleCourses.value).isEqualTo(Loadable.NotYetLoaded)
    }

    @Test
    fun `loaded groups surface on visibleCourses`() = runTest {
        val group = EnrolledCourseGroup(editions = listOf(course(1)))
        val vm = build(filtered = observeFilteredCourses(Loadable.Loaded(listOf(group))))
        val value = vm.visibleCourses.value
        assertThat(value).isInstanceOf(Loadable.Loaded::class.java)
        assertThat((value as Loadable.Loaded).value).containsExactly(group)
    }

    @Test
    fun `successful auto refresh leaves syncStatus Idle`() = runTest {
        val refresh = mockk<RefreshEnrolledCoursesUseCase>()
        coEvery { refresh.invoke(accountId, false) } returns Unit
        val vm = build(refresh = refresh)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
        coVerify { refresh.invoke(accountId, false) }
    }

    @Test
    fun `failed refresh sets SyncStatus Failed without clearing data`() = runTest {
        val group = EnrolledCourseGroup(editions = listOf(course(1)))
        val cause = IOException("offline")
        val refresh = mockk<RefreshEnrolledCoursesUseCase>()
        coEvery { refresh.invoke(accountId, false) } throws cause
        val vm = build(
            filtered = observeFilteredCourses(Loadable.Loaded(listOf(group))),
            refresh = refresh,
        )
        val status = vm.syncStatus.value
        assertThat(status).isInstanceOf(SyncStatus.Failed::class.java)
        assertThat((status as SyncStatus.Failed).cause).isEqualTo(cause)
        assertThat((vm.visibleCourses.value as Loadable.Loaded).value).containsExactly(group)
    }

    @Test
    fun `initialFetch settles once raw cache has a course`() = runTest {
        val vm = build(enrolled = observeEnrolledCourses(Loadable.Loaded(listOf(course(1)))))
        assertThat(vm.initialFetch.value).isEqualTo(InitialFetchState.Settled)
    }

    @Test
    fun `initialFetch settles to Settled when cache loaded empty and refresh idle`() = runTest {
        val refresh = mockk<RefreshEnrolledCoursesUseCase>()
        coEvery { refresh.invoke(accountId, false) } returns Unit
        val vm = build(
            enrolled = observeEnrolledCourses(Loadable.Loaded(emptyList())),
            refresh = refresh,
        )
        assertThat(vm.initialFetch.value).isEqualTo(InitialFetchState.Settled)
    }

    @Test
    fun `initialFetch reports Failed when cache empty and refresh failed`() = runTest {
        val cause = IOException("cold")
        val refresh = mockk<RefreshEnrolledCoursesUseCase>()
        coEvery { refresh.invoke(accountId, false) } throws cause
        val vm = build(
            enrolled = observeEnrolledCourses(Loadable.Loaded(emptyList())),
            refresh = refresh,
        )
        val state = vm.initialFetch.value
        assertThat(state).isInstanceOf(InitialFetchState.Failed::class.java)
        assertThat((state as InitialFetchState.Failed).cause).isEqualTo(cause)
    }

    @Test
    fun `initialFetch stays InProgress while cache NotYetLoaded`() = runTest {
        val vm = build(
            activeAccount = observeActiveAccount(value = null),
            refresh = mockk(relaxed = true),
        )
        assertThat(vm.initialFetch.value).isEqualTo(InitialFetchState.InProgress)
    }

    @Test
    fun `openCourse emits OpenCourse one-shot exactly once`() = runTest {
        val vm = build()
        vm.oneShotEvents.test {
            vm.openCourse(CourseId(7))
            assertThat(awaitItem()).isEqualTo(ElearningOneShotEvent.OpenCourse(CourseId(7)))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `requestAddCourse emits OpenAddCourse`() = runTest {
        val vm = build()
        vm.oneShotEvents.test {
            vm.requestAddCourse()
            assertThat(awaitItem()).isEqualTo(ElearningOneShotEvent.OpenAddCourse)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `openDeadline routes assignment to OpenAssignment`() = runTest {
        val vm = build()
        val deadline = Deadline.Assignment(
            id = AssignmentId(11),
            courseId = CourseId(3),
            title = "Compito",
            dueAt = Instant.EPOCH,
        )
        vm.oneShotEvents.test {
            vm.openDeadline(deadline)
            assertThat(awaitItem()).isEqualTo(
                ElearningOneShotEvent.OpenAssignment(CourseId(3), AssignmentId(11)),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `openDeadline routes quiz to OpenQuiz`() = runTest {
        val vm = build()
        val deadline = Deadline.Quiz(
            id = QuizId(22),
            courseId = CourseId(4),
            title = "Quiz",
            dueAt = Instant.EPOCH,
        )
        vm.oneShotEvents.test {
            vm.openDeadline(deadline)
            assertThat(awaitItem()).isEqualTo(ElearningOneShotEvent.OpenQuiz(CourseId(4), QuizId(22)))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pullToRefresh forces a refresh`() = runTest {
        val refresh = mockk<RefreshEnrolledCoursesUseCase>(relaxed = true)
        val vm = build(refresh = refresh)
        vm.pullToRefresh()
        coVerify { refresh.invoke(accountId, true) }
    }

    @Test
    fun `setFilter delegates to use case`() = runTest {
        val setFilter = mockk<SetCourseFilterUseCase>(relaxed = true)
        val vm = build(setFilter = setFilter)
        vm.setFilter(CourseFilter.Favourites)
        coVerify { setFilter.invoke(CourseFilter.Favourites) }
    }

    @Test
    fun `toggleFavourite passes active account and flag through`() = runTest {
        val toggle = mockk<ToggleCourseFavouriteUseCase>(relaxed = true)
        val vm = build(toggleFavourite = toggle)
        vm.toggleFavourite(CourseId(5), favourite = true)
        coVerify { toggle.invoke(accountId, CourseId(5), true) }
    }

    @Test
    fun `toggleGroupFavourite fans out to every edition`() = runTest {
        val toggle = mockk<ToggleCourseFavouriteUseCase>(relaxed = true)
        val group = EnrolledCourseGroup(editions = listOf(course(1), course(2)))
        val vm = build(toggleFavourite = toggle)
        vm.toggleGroupFavourite(group, favourite = false)
        coVerify { toggle.invoke(accountId, CourseId(1), false) }
        coVerify { toggle.invoke(accountId, CourseId(2), false) }
    }

    @Test
    fun `setHidden passes active account and flag through`() = runTest {
        val setHidden = mockk<SetCourseHiddenUseCase>(relaxed = true)
        val vm = build(setHidden = setHidden)
        vm.setHidden(CourseId(9), hidden = true)
        coVerify { setHidden.invoke(accountId, CourseId(9), true) }
    }

    @Test
    fun `revealEnrolledCourse drops a hiding filter to All and signals scroll`() = runTest {
        val setFilter = mockk<SetCourseFilterUseCase>(relaxed = true)
        val target = course(42, idNumber = "2526-2-E9999")
        val vm = build(
            courseFilter = observeCourseFilter(CourseFilter.ByYear(StudyYear(1))),
            enrolled = observeEnrolledCourses(Loadable.Loaded(listOf(target))),
            setFilter = setFilter,
        )
        vm.revealCourse.test {
            vm.revealEnrolledCourse(CourseId(42))
            assertThat(awaitItem()).isEqualTo(CourseId(42))
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { setFilter.invoke(CourseFilter.All) }
    }

    @Test
    fun `revealEnrolledCourse keeps matching filter untouched`() = runTest {
        val setFilter = mockk<SetCourseFilterUseCase>(relaxed = true)
        val target = course(42, idNumber = "2526-1-E1111")
        val vm = build(
            courseFilter = observeCourseFilter(CourseFilter.ByYear(StudyYear(1))),
            enrolled = observeEnrolledCourses(Loadable.Loaded(listOf(target))),
            setFilter = setFilter,
        )
        vm.revealCourse.test {
            vm.revealEnrolledCourse(CourseId(42))
            assertThat(awaitItem()).isEqualTo(CourseId(42))
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { setFilter.invoke(CourseFilter.All) }
    }

    @Test
    fun `availableStudyYears reflects the use case stream`() = runTest {
        val years = listOf(StudyYear(1), StudyYear(2))
        val vm = build(studyYears = observeAvailableStudyYears(years))
        vm.availableStudyYears.test {
            assertThat(awaitItem()).containsExactly(StudyYear(1), StudyYear(2)).inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
