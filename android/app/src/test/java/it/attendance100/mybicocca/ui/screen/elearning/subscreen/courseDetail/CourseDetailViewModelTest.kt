package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.ObserveCourseAssignmentsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.RefreshCourseAssignmentsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveCompletionStatesUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveCourseDetailsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.RefreshCourseDetailsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.SetActivityCompletedUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ToggleCourseFavouriteUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObserveCourseForumsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObserveDiscussionsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.RefreshCourseForumsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.RefreshDiscussionsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.grade.ObserveCourseGradeItemsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.grade.RefreshCourseGradeItemsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.ObserveCourseQuizzesUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.RefreshCourseQuizzesUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.video.GetVideoThumbnailUrlUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.video.ObserveCourseVideoProgressUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.CourseDetailOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.CourseTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * Covers the e-learning course detail ViewModel: SavedStateHandle-backed tab/expansion selection,
 * the favourite derivation, the fan-out refresh sync status (only details-failure surfaces), the
 * continue-watching thumbnail resolution, and the module-open one-shot family.
 */
class CourseDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accountId = AccountId("acc-1")
    private val courseId = CourseId(99)

    private val account: Account = mockk(relaxed = true) {
        every { id } returns accountId
    }

    private fun enrolledCourse(favourite: Boolean): EnrolledCourse = EnrolledCourse(
        id = courseId,
        shortName = "S",
        fullName = "Course",
        displayName = "Course",
        idNumber = null,
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
        hidden = false,
    )

    private fun details(favourite: Boolean = false, withSections: Boolean = false): CourseDetails =
        CourseDetails(
            enrolled = enrolledCourse(favourite),
            sections = emptyList(),
            staff = emptyList(),
            syllabus = null,
        )

    private fun build(
        savedState: SavedStateHandle = SavedStateHandle(),
        activeAccount: Account? = account,
        detailsLoadable: Loadable<CourseDetails> = Loadable.NotYetLoaded,
        observeDetails: ObserveCourseDetailsUseCase = mockk {
            every { this@mockk.invoke(any(), any()) } returns MutableStateFlow(detailsLoadable)
        },
        refreshDetails: RefreshCourseDetailsUseCase = mockk(relaxed = true),
        setActivityCompleted: SetActivityCompletedUseCase = mockk(relaxed = true),
        toggleFavourite: ToggleCourseFavouriteUseCase = mockk(relaxed = true),
        thumbnail: GetVideoThumbnailUrlUseCase = mockk(relaxed = true),
    ): CourseDetailViewModel = CourseDetailViewModel(
        key = AppRoute.CourseDetail(courseId.value),
        savedState = savedState,
        observeActiveAccount = mockk {
            every { this@mockk.invoke() } returns MutableStateFlow<Account?>(activeAccount)
        },
        observeDetails = observeDetails,
        observeAssignments = relaxedObserveAssignments(),
        observeQuizzes = relaxedObserveQuizzes(),
        observeForums = relaxedObserveForums(),
        observeDiscussions = mockk(relaxed = true) {
            every { this@mockk.invoke(any(), any()) } returns flowOf(Loadable.NotYetLoaded)
        },
        refreshDiscussions = mockk(relaxed = true),
        observeGradeItems = relaxedObserveGrades(),
        observeCompletion = mockk(relaxed = true) {
            every { this@mockk.invoke(any(), any()) } returns flowOf(emptyMap())
        },
        refreshDetails = refreshDetails,
        refreshAssignments = mockk(relaxed = true),
        refreshQuizzes = mockk(relaxed = true),
        refreshForums = mockk(relaxed = true),
        refreshGrades = mockk(relaxed = true),
        setActivityCompleted = setActivityCompleted,
        toggleCourseFavourite = toggleFavourite,
        observeCourseVideoProgress = mockk(relaxed = true) {
            every { this@mockk.invoke(any(), any()) } returns flowOf(emptyMap())
        },
        getVideoThumbnailUrl = thumbnail,
    )

    private fun relaxedObserveAssignments(): ObserveCourseAssignmentsUseCase = mockk {
        every { this@mockk.invoke(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
    }

    private fun relaxedObserveQuizzes(): ObserveCourseQuizzesUseCase = mockk {
        every { this@mockk.invoke(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
    }

    private fun relaxedObserveForums(): ObserveCourseForumsUseCase = mockk {
        every { this@mockk.invoke(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
    }

    private fun relaxedObserveGrades(): ObserveCourseGradeItemsUseCase = mockk {
        every { this@mockk.invoke(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
    }

    @Test
    fun `selectedTab defaults to Syllabus`() = runTest {
        val vm = build()
        assertThat(vm.selectedTab.value).isEqualTo(CourseTab.Syllabus)
    }

    @Test
    fun `selectTab persists and surfaces the new tab`() = runTest {
        val vm = build()
        vm.selectTab(CourseTab.Quiz)
        assertThat(vm.selectedTab.value).isEqualTo(CourseTab.Quiz)
    }

    @Test
    fun `toggleSection adds then removes the section id`() = runTest {
        val vm = build()
        vm.toggleSection(3)
        assertThat(vm.expandedSections.value).containsExactly(3)
        vm.toggleSection(3)
        assertThat(vm.expandedSections.value).isEmpty()
    }

    @Test
    fun `toggleQuizGroup is independent of section expansion`() = runTest {
        val vm = build()
        vm.toggleQuizGroup(8)
        assertThat(vm.expandedQuizGroups.value).containsExactly(8)
        assertThat(vm.expandedSections.value).isEmpty()
    }

    @Test
    fun `isFavourite reflects the loaded details`() = runTest {
        val vm = build(detailsLoadable = Loadable.Loaded(details(favourite = true)))
        vm.isFavourite.test {
            assertThat(awaitItem()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isFavourite defaults to false while details NotYetLoaded`() = runTest {
        val vm = build(detailsLoadable = Loadable.NotYetLoaded)
        assertThat(vm.isFavourite.value).isFalse()
    }

    @Test
    fun `details refresh failure sets Failed and emits RefreshFailed once`() = runTest {
        val cause = IOException("offline")
        val refresh = mockk<RefreshCourseDetailsUseCase>()
        coEvery { refresh.invoke(accountId, courseId, any()) } throws cause
        val vm = build(refreshDetails = refresh)
        vm.oneShotEvents.test {
            val event = awaitItem()
            assertThat(event).isInstanceOf(CourseDetailOneShotEvent.RefreshFailed::class.java)
            val eventCause = (event as CourseDetailOneShotEvent.RefreshFailed).cause
            assertThat(eventCause).isInstanceOf(IOException::class.java)
            assertThat(eventCause.message).isEqualTo("offline")
            cancelAndIgnoreRemainingEvents()
        }
        val status = vm.syncStatus.value
        assertThat(status).isInstanceOf(SyncStatus.Failed::class.java)
        val statusCause = (status as SyncStatus.Failed).cause
        assertThat(statusCause).isInstanceOf(IOException::class.java)
        assertThat(statusCause.message).isEqualTo("offline")
    }

    @Test
    fun `successful refresh leaves syncStatus Idle`() = runTest {
        val refresh = mockk<RefreshCourseDetailsUseCase>()
        coEvery { refresh.invoke(accountId, courseId, any()) } returns Unit
        val vm = build(refreshDetails = refresh)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `pullToRefresh forces a details refresh`() = runTest {
        val refresh = mockk<RefreshCourseDetailsUseCase>(relaxed = true)
        val vm = build(refreshDetails = refresh)
        vm.pullToRefresh()
        coVerify { refresh.invoke(accountId, courseId, true) }
    }

    @Test
    fun `toggleFavourite writes the negated current state`() = runTest {
        val toggle = mockk<ToggleCourseFavouriteUseCase>(relaxed = true)
        val vm = build(
            detailsLoadable = Loadable.Loaded(details(favourite = false)),
            toggleFavourite = toggle,
        )
        vm.toggleFavourite()
        coVerify { toggle.invoke(accountId, courseId, true) }
    }

    @Test
    fun `onSetActivityCompleted forwards the flag`() = runTest {
        val setCompleted = mockk<SetActivityCompletedUseCase>(relaxed = true)
        val vm = build(setActivityCompleted = setCompleted)
        vm.onSetActivityCompleted(cmId = 12, completed = true)
        coVerify { setCompleted.invoke(accountId, courseId, 12, true) }
    }

    @Test
    fun `resolveContinueWatchingThumbnail with null settles on Loaded null`() = runTest {
        val vm = build()
        vm.resolveContinueWatchingThumbnail(null)
        assertThat(vm.continueWatchingThumbnailUrl.value).isEqualTo(Loadable.Loaded<String?>(null))
    }

    @Test
    fun `resolveContinueWatchingThumbnail resolves the url for a cmId`() = runTest {
        val thumbnail = mockk<GetVideoThumbnailUrlUseCase>()
        coEvery { thumbnail.invoke(5) } returns "https://poster"
        val vm = build(thumbnail = thumbnail)
        vm.resolveContinueWatchingThumbnail(5)
        assertThat(vm.continueWatchingThumbnailUrl.value)
            .isEqualTo(Loadable.Loaded<String?>("https://poster"))
    }

    @Test
    fun `resolveContinueWatchingThumbnail dedupes repeated cmIds`() = runTest {
        val thumbnail = mockk<GetVideoThumbnailUrlUseCase>()
        coEvery { thumbnail.invoke(5) } returns "https://poster"
        val vm = build(thumbnail = thumbnail)
        vm.resolveContinueWatchingThumbnail(5)
        vm.resolveContinueWatchingThumbnail(5)
        coVerify(exactly = 1) { thumbnail.invoke(5) }
    }

    @Test
    fun `emitOpenAssignment emits OpenAssignment`() = runTest {
        val vm = build()
        vm.oneShotEvents.test {
            vm.emitOpenAssignment(7)
            assertThat(awaitItem()).isEqualTo(
                CourseDetailOneShotEvent.OpenAssignment(
                    it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId(7),
                ),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emitOpenDiscussion emits OpenDiscussion with both ids`() = runTest {
        val vm = build()
        vm.oneShotEvents.test {
            vm.emitOpenDiscussion(ForumId(2), DiscussionId(3))
            assertThat(awaitItem()).isEqualTo(
                CourseDetailOneShotEvent.OpenDiscussion(ForumId(2), DiscussionId(3)),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emitOpenFile carries the file metadata`() = runTest {
        val vm = build()
        vm.oneShotEvents.test {
            vm.emitOpenFile("a.pdf", "https://file", "application/pdf", 100L, forceChooser = true)
            assertThat(awaitItem()).isEqualTo(
                CourseDetailOneShotEvent.OpenFile("a.pdf", "https://file", "application/pdf", 100L, true),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
