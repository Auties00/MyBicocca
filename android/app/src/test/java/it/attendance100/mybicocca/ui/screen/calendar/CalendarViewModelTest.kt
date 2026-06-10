package it.attendance100.mybicocca.ui.screen.calendar

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.IsCalendarMonthFreshUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.ObserveDayEventsUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.ObserveMonthEventsUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.ObserveMonthHydratedUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.PrefetchAdjacentMonthsUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.RefreshCalendarMonthUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveCoursesByActivityCodeUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.calendar.state.CalendarOneShotEvent
import it.attendance100.mybicocca.ui.screen.calendar.state.CalendarViewMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/**
 * State-machine coverage for the calendar tab ViewModel: the reference VM. Asserts the
 * NotYetLoaded -> Loaded data transition without an empty flash, the independent
 * Refreshing/Idle/Failed sync status, the fire-exactly-once RefreshFailed one-shot, and the
 * month-aligning selection actions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(101L)
    private val accountId = AccountId("acc-1")
    private val month = YearMonth.of(2026, 6)

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

    private fun lesson(id: String, date: LocalDate): CalendarEvent.Lesson = CalendarEvent.Lesson(
        id = CalendarEventId(id),
        careerId = careerId,
        date = date,
        start = LocalTime.of(9, 0),
        end = LocalTime.of(11, 0),
        title = "Algoritmi",
        shortLabel = "ASD",
        location = null,
        status = EventStatus.CONFIRMED,
        notes = null,
        activityCode = "E3101Q123",
        subjectCode = "EC1",
        teachers = listOf("Prof Bianchi"),
        cfu = 6,
    )

    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()
    private val observeMonthEvents: ObserveMonthEventsUseCase = mockk()
    private val observeDayEvents: ObserveDayEventsUseCase = mockk()
    private val observeMonthHydrated: ObserveMonthHydratedUseCase = mockk()
    private val isMonthFresh: IsCalendarMonthFreshUseCase = mockk()
    private val refreshMonth: RefreshCalendarMonthUseCase = mockk(relaxed = true)
    private val prefetchAdjacent: PrefetchAdjacentMonthsUseCase = mockk(relaxed = true)
    private val observeCoursesByActivityCode: ObserveCoursesByActivityCodeUseCase = mockk()

    private fun viewModel(
        savedState: SavedStateHandle = SavedStateHandle(),
    ): CalendarViewModel {
        every { observeActiveAccount() } returns flowOf(account)
        every { observeMonthEvents(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeDayEvents(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeMonthHydrated(any(), any()) } returns flowOf(true)
        every { observeCoursesByActivityCode(any()) } returns flowOf(emptyMap())
        coEvery { isMonthFresh(any(), any()) } returns true
        return CalendarViewModel(
            savedState = savedState,
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

    @Test
    fun `events goes NotYetLoaded then Loaded without an empty flash`() = runTest {
        val today = LocalDate.now()
        val current = YearMonth.from(today)
        every { observeActiveAccount() } returns flowOf(account)
        every { observeMonthEvents(careerId, current) } returns
            flowOf(Loadable.Loaded(listOf(lesson("lesson_1", today))))
        every { observeMonthEvents(careerId, current.minusMonths(1)) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeMonthEvents(careerId, current.plusMonths(1)) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeDayEvents(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeMonthHydrated(any(), any()) } returns flowOf(true)
        every { observeCoursesByActivityCode(any()) } returns flowOf(emptyMap())
        coEvery { isMonthFresh(any(), any()) } returns true

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

        vm.events.test {
            var current = awaitItem()
            while (current is Loadable.NotYetLoaded) {
                current = awaitItem()
            }
            val loaded = current as Loadable.Loaded
            assertThat(loaded.value).hasSize(1)
            assertThat(loaded.value).isNotEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dayEvents goes NotYetLoaded then Loaded`() = runTest {
        val today = LocalDate.now()
        every { observeActiveAccount() } returns flowOf(account)
        every { observeMonthEvents(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeMonthHydrated(any(), any()) } returns flowOf(true)
        every { observeCoursesByActivityCode(any()) } returns flowOf(emptyMap())
        coEvery { isMonthFresh(any(), any()) } returns true
        every { observeDayEvents(careerId, today) } returns
            flowOf(Loadable.Loaded(listOf(lesson("lesson_d", today))))

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

        vm.dayEvents.test {
            var current = awaitItem()
            while (current is Loadable.NotYetLoaded) {
                current = awaitItem()
            }
            val loaded = current as Loadable.Loaded
            assertThat(loaded.value).hasSize(1)
            assertThat(loaded.value).isNotEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init runs a non-forced refresh and sets sync status Idle on success`() = runTest {
        val vm = viewModel()

        coVerify { refreshMonth(careerId, YearMonth.from(LocalDate.now()), false) }
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `pullToRefresh forces a refresh and sets Refreshing then Idle on success`() = runTest {
        coEvery { isMonthFresh(any(), any()) } returns true
        val vm = viewModel()

        vm.pullToRefresh()

        coVerify { refreshMonth(careerId, YearMonth.from(LocalDate.now()), true) }
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `refresh failure sets Failed without nuking data and emits RefreshFailed exactly once`() = runTest {
        val today = LocalDate.now()
        val current = YearMonth.from(today)
        val boom = IOException("offline")
        every { observeActiveAccount() } returns flowOf(account)
        every { observeDayEvents(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeMonthHydrated(any(), any()) } returns flowOf(true)
        every { observeCoursesByActivityCode(any()) } returns flowOf(emptyMap())
        every { observeMonthEvents(careerId, current.minusMonths(1)) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeMonthEvents(careerId, current.plusMonths(1)) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeMonthEvents(careerId, current) } returns
            flowOf(Loadable.Loaded(listOf(lesson("lesson_keep", today))))
        coEvery { isMonthFresh(any(), any()) } returns false
        coEvery { refreshMonth(any(), any(), any()) } throws boom

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

        vm.oneShotEvents.test {
            val event = awaitItem()
            assertThat(event).isInstanceOf(CalendarOneShotEvent.RefreshFailed::class.java)
            assertThat((event as CalendarOneShotEvent.RefreshFailed).cause).isEqualTo(boom)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        val status = vm.syncStatus.value
        assertThat(status).isInstanceOf(SyncStatus.Failed::class.java)
        assertThat((status as SyncStatus.Failed).cause).isEqualTo(boom)

        vm.events.test {
            var item = awaitItem()
            while (item is Loadable.NotYetLoaded) {
                item = awaitItem()
            }
            val loaded = item as Loadable.Loaded
            assertThat(loaded.value).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fresh month skips the sync indicator on the non-forced init refresh`() = runTest {
        coEvery { isMonthFresh(any(), any()) } returns true
        val vm = viewModel()

        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `selectDay in a different month realigns the month`() = runTest {
        val vm = viewModel()
        val target = LocalDate.now().plusMonths(2).withDayOfMonth(15)

        vm.selectDay(target)

        assertThat(vm.selectedDay.value).isEqualTo(target)
        assertThat(vm.selectedMonth.value).isEqualTo(YearMonth.from(target))
    }

    @Test
    fun `selectDay within the current month keeps the month untouched`() = runTest {
        val vm = viewModel()
        val current = YearMonth.from(LocalDate.now())
        val sameMonthDay = current.atDay(1)

        vm.selectDay(sameMonthDay)

        assertThat(vm.selectedDay.value).isEqualTo(sameMonthDay)
        assertThat(vm.selectedMonth.value).isEqualTo(current)
    }

    @Test
    fun `weekStart derives the Monday of the selected day's week`() = runTest {
        val vm = viewModel()
        val wednesday = LocalDate.of(2026, 6, 17)

        vm.selectDay(wednesday)

        val expectedMonday = wednesday.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        assertThat(vm.weekStart.value).isEqualTo(expectedMonday)
    }

    @Test
    fun `shiftSelectedDay moves the day and snaps the month`() = runTest {
        val savedState = SavedStateHandle()
        val start = YearMonth.of(2026, 6).atEndOfMonth()
        savedState["calendar_selected_day"] = start.toString()
        savedState["calendar_year_month"] = YearMonth.of(2026, 6).toString()
        val vm = viewModel(savedState)

        vm.shiftSelectedDay(1)

        val expected = start.plusDays(1)
        assertThat(vm.selectedDay.value).isEqualTo(expected)
        assertThat(vm.selectedMonth.value).isEqualTo(YearMonth.from(expected))
    }

    @Test
    fun `shiftSelectedMonth moves the visible month`() = runTest {
        val savedState = SavedStateHandle()
        savedState["calendar_year_month"] = month.toString()
        val vm = viewModel(savedState)

        vm.shiftSelectedMonth(2)

        assertThat(vm.selectedMonth.value).isEqualTo(month.plusMonths(2))
    }

    @Test
    fun `selectViewMode updates the persisted view mode`() = runTest {
        val vm = viewModel()

        vm.selectViewMode(CalendarViewMode.WEEK)

        assertThat(vm.viewMode.value).isEqualTo(CalendarViewMode.WEEK)
    }

    @Test
    fun `openEventDetail and closeEventDetail drive the selected event id`() = runTest {
        val vm = viewModel()

        vm.openEventDetail(CalendarEventId("lesson_42"))
        assertThat(vm.selectedEventId.value).isEqualTo(CalendarEventId("lesson_42"))

        vm.closeEventDetail()
        assertThat(vm.selectedEventId.value).isNull()
    }

    @Test
    fun `no active career renders an empty Loaded list instead of staying NotYetLoaded`() = runTest {
        every { observeActiveAccount() } returns flowOf(null)
        every { observeMonthEvents(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeDayEvents(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
        every { observeMonthHydrated(any(), any()) } returns flowOf(true)
        every { observeCoursesByActivityCode(any()) } returns flowOf(emptyMap())
        coEvery { isMonthFresh(any(), any()) } returns true

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

        vm.events.test {
            assertThat(awaitItem()).isEqualTo(Loadable.Loaded(emptyList<CalendarEvent>()))
            cancelAndIgnoreRemainingEvents()
        }
    }
}
