package it.attendance100.mybicocca.ui.screen.calendar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.calendar.EventLocation
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.IsCalendarMonthFreshUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.ObserveDayEventsUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.ObserveMonthEventsUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.ObserveMonthHydratedUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.PrefetchAdjacentMonthsUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.RefreshCalendarMonthUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveCoursesByActivityCodeUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import it.attendance100.mybicocca.ui.screen.calendar.state.CalendarViewMode
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.eventDetail.EventDetailContent
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.eventDetail.EventDetailSheet
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Compose-level coverage for the calendar tab. The screen is driven by a real
 * [CalendarViewModel] over MockK-faked use cases (the same construction as
 * [CalendarViewModelTest]), so each data/sync state can be staged and the resulting state
 * marker asserted; the event detail body is exercised directly as the stateless composable
 * [EventDetailContent] it is, sidestepping the modal-sheet window so the action click lands
 * synchronously. Tests anchor on [CalendarTestTags] and render under [setBicoccaContent], which
 * installs the app-wide CompositionLocals the screen reads (haptics, snackbar, device type)
 * over the production theme.
 *
 * The pull-to-refresh spinner pinned during the initial fetch is owned by `PullToRefreshBox`
 * and carries no tag seam, so the initial-loading case is asserted through its observable
 * surface — the content branch renders the empty calendar while no error replaces it.
 */
@RunWith(AndroidJUnit4::class)
class CalendarScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val careerId = CareerId(101L)

    private val account = Account(
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

    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()
    private val observeMonthEvents: ObserveMonthEventsUseCase = mockk()
    private val observeDayEvents: ObserveDayEventsUseCase = mockk()
    private val observeMonthHydrated: ObserveMonthHydratedUseCase = mockk()
    private val isMonthFresh: IsCalendarMonthFreshUseCase = mockk()
    private val refreshMonth: RefreshCalendarMonthUseCase = mockk(relaxed = true)
    private val prefetchAdjacent: PrefetchAdjacentMonthsUseCase = mockk(relaxed = true)
    private val observeCoursesByActivityCode: ObserveCoursesByActivityCodeUseCase = mockk()

    private fun viewModel(): CalendarViewModel {
        every { observeActiveAccount() } returns flowOf(account)
        every { observeMonthEvents(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeDayEvents(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeMonthHydrated(any(), any()) } returns flowOf(true)
        every { observeCoursesByActivityCode(any()) } returns flowOf(emptyMap())
        coEvery { isMonthFresh(any(), any()) } returns true
        return CalendarViewModel(
            savedState = SavedStateHandle(),
            observeActiveAccount = observeActiveAccount,
            observeMonthEvents = observeMonthEvents,
            observeDayEvents = observeDayEvents,
            observeMonthHydrated = observeMonthHydrated,
            isMonthFresh = isMonthFresh,
            refreshMonth = refreshMonth,
            prefetchAdjacent = prefetchAdjacent,
            observeCoursesByActivityCode = observeCoursesByActivityCode,
        )
    }

    private fun setScreen(vm: CalendarViewModel) {
        compose.setBicoccaContent {
            CalendarScreen(
                isActive = false,
                bottomNavBarPadding = PaddingValues(),
                viewModel = vm,
            )
        }
    }

    private fun lesson(
        id: String,
        date: LocalDate = LocalDate.now(),
        status: EventStatus = EventStatus.CONFIRMED,
        teachers: List<String> = listOf("Prof Bianchi"),
        location: EventLocation? = EventLocation(room = "U6-15", building = "U6"),
    ): CalendarEvent.Lesson = CalendarEvent.Lesson(
        id = CalendarEventId(id),
        careerId = careerId,
        date = date,
        start = LocalTime.of(9, 0),
        end = LocalTime.of(11, 0),
        title = "Algoritmi e Strutture Dati",
        shortLabel = "ASD",
        location = location,
        status = status,
        notes = null,
        activityCode = "E3101Q123",
        subjectCode = "EC1",
        teachers = teachers,
        cfu = 6,
    )

    private fun course(id: Int): EnrolledCourse = mockk(relaxed = true) {
        every { this@mockk.id } returns CourseId(id)
    }

    @Test
    fun loaded_calendar_shows_the_content_branch_and_no_error() {
        setScreen(viewModel())
        compose.waitForIdle()

        compose.onNodeWithTag(CalendarTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(CalendarTestTags.STATE_CONTENT).assertIsDisplayed()
        assertThat(
            compose.onAllNodesWithTag(CalendarTestTags.STATE_ERROR).fetchSemanticsNodes(),
        ).isEmpty()
    }

    @Test
    fun initial_fetch_keeps_the_content_branch_with_no_error_while_the_month_is_not_yet_hydrated() {
        every { observeActiveAccount() } returns flowOf(account)
        every { observeMonthEvents(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeDayEvents(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeMonthHydrated(any(), any()) } returns flowOf(false)
        every { observeCoursesByActivityCode(any()) } returns flowOf(emptyMap())
        coEvery { isMonthFresh(any(), any()) } returns false
        val vm = CalendarViewModel(
            savedState = SavedStateHandle(),
            observeActiveAccount = observeActiveAccount,
            observeMonthEvents = observeMonthEvents,
            observeDayEvents = observeDayEvents,
            observeMonthHydrated = observeMonthHydrated,
            isMonthFresh = isMonthFresh,
            refreshMonth = refreshMonth,
            prefetchAdjacent = prefetchAdjacent,
            observeCoursesByActivityCode = observeCoursesByActivityCode,
        )

        setScreen(vm)
        compose.waitForIdle()

        assertThat(vm.initialLoading.value).isTrue()
        compose.onNodeWithTag(CalendarTestTags.STATE_CONTENT).assertIsDisplayed()
        assertThat(
            compose.onAllNodesWithTag(CalendarTestTags.STATE_ERROR).fetchSemanticsNodes(),
        ).isEmpty()
    }

    @Test
    fun a_failed_sync_replaces_the_calendar_with_the_error_state() {
        every { observeActiveAccount() } returns flowOf(account)
        every { observeMonthEvents(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeDayEvents(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeMonthHydrated(any(), any()) } returns flowOf(true)
        every { observeCoursesByActivityCode(any()) } returns flowOf(emptyMap())
        coEvery { isMonthFresh(any(), any()) } returns false
        coEvery { refreshMonth(any(), any(), any()) } throws IOException("offline")
        val vm = CalendarViewModel(
            savedState = SavedStateHandle(),
            observeActiveAccount = observeActiveAccount,
            observeMonthEvents = observeMonthEvents,
            observeDayEvents = observeDayEvents,
            observeMonthHydrated = observeMonthHydrated,
            isMonthFresh = isMonthFresh,
            refreshMonth = refreshMonth,
            prefetchAdjacent = prefetchAdjacent,
            observeCoursesByActivityCode = observeCoursesByActivityCode,
        )

        setScreen(vm)
        compose.waitForIdle()

        compose.onNodeWithTag(CalendarTestTags.STATE_ERROR).assertIsDisplayed()
        assertThat(
            compose.onAllNodesWithTag(CalendarTestTags.STATE_CONTENT).fetchSemanticsNodes(),
        ).isEmpty()
    }

    @Test
    fun tapping_the_week_segment_switches_the_view_mode() {
        val vm = viewModel()
        setScreen(vm)
        compose.waitForIdle()

        compose.onNodeWithTag(CalendarTestTags.segment(CalendarViewMode.WEEK)).performClick()
        compose.waitForIdle()

        assertThat(vm.viewMode.value).isEqualTo(CalendarViewMode.WEEK)
        compose.onNodeWithTag(CalendarTestTags.STATE_CONTENT).assertIsDisplayed()
    }

    @Test
    fun event_detail_sheet_renders_the_event_title_and_kind_label() {
        compose.setBicoccaContent {
            EventDetailSheet(
                event = lesson("lesson_1"),
                elearningCourses = emptyList(),
                onOpenCourse = mockk(relaxed = true),
                onOpenAssignment = mockk(relaxed = true),
                onOpenReservation = mockk(relaxed = true),
                onDismiss = mockk(relaxed = true),
            )
        }
        compose.waitForIdle()

        compose.onNodeWithTag(CalendarTestTags.EVENT_TITLE).assertIsDisplayed()
        compose.onNodeWithTag(CalendarTestTags.EVENT_ACTIVITY_LABEL).assertIsDisplayed()
        compose.onNodeWithTag(CalendarTestTags.EVENT_CONTENT).assertIsDisplayed()
    }

    @Test
    fun tapping_the_lesson_primary_action_opens_the_matching_elearning_course() {
        val onOpenCourse: (CourseId) -> Unit = mockk(relaxed = true)
        compose.setBicoccaContent {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                EventDetailContent(
                    event = lesson("lesson_2"),
                    elearningCourses = listOf(course(42)),
                    onOpenCourse = onOpenCourse,
                    onOpenAssignment = mockk(relaxed = true),
                    onOpenReservation = mockk(relaxed = true),
                )
            }
        }

        compose.waitUntil(timeoutMillis = 5000) {
            compose.onAllNodesWithTag(CalendarTestTags.EVENT_PRIMARY_ACTION)
                .fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNodeWithTag(CalendarTestTags.EVENT_PRIMARY_ACTION).performScrollTo().performClick()
        compose.waitForIdle()

        verify { onOpenCourse(CourseId(42)) }
    }
}
