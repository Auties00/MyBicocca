package it.attendance100.mybicocca.ui.screen.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.ui.component.modal.rememberLastNonNull
import it.attendance100.mybicocca.ui.navigation.AppNavigator
import it.attendance100.mybicocca.ui.navigation.LocalDestination
import it.attendance100.mybicocca.ui.navigation.PopWhenMissing
import it.attendance100.mybicocca.ui.navigation.rememberPopSelf
import it.attendance100.mybicocca.ui.navigation.requireAppNavigator
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.sheetHeader
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.coursePicker.CourseEditionPickerPage
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.coursePicker.courseEditionPickerHeader
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.eventDetail.EventDetailPage
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.eventDetail.eventDetailHeader

/**
 * The calendar's sheets: an event's detail and the course-edition picker it can open. Both read
 * the shell-scoped [calendarViewModel] the tab renders from. [examBookingTotals] holds the seat
 * totals per exam event; [onExamEventShown] lazily fetches the total of an exam once its detail
 * shows.
 */
fun EntryProviderScope<NavKey>.calendarSheetEntries(
    calendarViewModel: CalendarViewModel,
    examBookingTotals: State<Map<CalendarEventId, Int>>,
    onExamEventShown: (CalendarEvent.Exam) -> Unit,
) {
    entry<SheetRoute.CalendarEvent>(
        metadata = sheetHeader<SheetRoute.CalendarEvent> { route ->
            eventDetailHeader(rememberLastNonNull(liveEvent(calendarViewModel, route.eventId)))
        },
    ) { route ->
        val navigator = requireAppNavigator()
        val monthEvents by calendarViewModel.events.collectAsStateWithLifecycle()
        val coursesByActivityCode by calendarViewModel.coursesByActivityCode.collectAsStateWithLifecycle()
        val liveEvent = liveEvent(calendarViewModel, route.eventId)
        PopWhenMissing(loaded = monthEvents.valueOrNull() != null, missing = liveEvent == null, onlyAfterSeen = true)
        // Keep showing the event while it closes (or while a sheet stacked above removed it, e.g.
        // a cancelled booking) instead of blanking the page.
        val event = rememberLastNonNull(liveEvent) ?: return@entry
        LaunchedEffect(event.id) { (event as? CalendarEvent.Exam)?.let(onExamEventShown) }
        val closeEvent = rememberPopSelf()
        EventDetailPage(
            event = event,
            examTotalBookings = (event as? CalendarEvent.Exam)?.let { examBookingTotals.value[it.id] },
            elearningCourses = event.activityCode?.let(coursesByActivityCode::get).orEmpty(),
            onOpenCourse = { course ->
                closeEvent()
                navigator.navigate(AppRoute.CourseDetail(course.value))
            },
            onOpenAssignment = { assignmentId, courseId ->
                closeEvent()
                navigator.navigate(SheetRoute.AssignmentDetail(assignmentId, courseId))
            },
            onOpenReservation = navigator::openReservation,
        )
    }
    entry<SheetRoute.CourseEditionPicker>(
        metadata = sheetHeader<SheetRoute.CourseEditionPicker> { route ->
            val coursesByActivityCode by calendarViewModel.coursesByActivityCode.collectAsStateWithLifecycle()
            courseEditionPickerHeader(count = coursesByActivityCode[route.activityCode].orEmpty().size)
        },
    ) { route ->
        val navigator = requireAppNavigator()
        val self = checkNotNull(LocalDestination.current)
        val coursesByActivityCode by calendarViewModel.coursesByActivityCode.collectAsStateWithLifecycle()
        CourseEditionPickerPage(
            courses = coursesByActivityCode[route.activityCode].orEmpty(),
            onPick = { courseId ->
                // Leave the whole sheet the picker belongs to (the event sheet when it was opened
                // from there).
                navigator.dismissSheetOf(self.id)
                navigator.navigate(AppRoute.CourseDetail(courseId.value))
            },
        )
    }
}

/**
 * "Vai alla prenotazione" on a calendar event: the sheet managing the booking (appelli,
 * appuntamenti, biblioteca) opens STACKED over whatever is showing, so dismissing it lands back
 * on the event.
 */
fun AppNavigator.openReservation(event: CalendarEvent) {
    when (event) {
        is CalendarEvent.Exam -> navigate(SheetRoute.Appelli)
        is CalendarEvent.Appointment -> navigate(SheetRoute.Appointments)
        is CalendarEvent.LibraryReservation -> navigate(SheetRoute.Library)
        else -> Unit
    }
}

/** The event [eventId] from the month list, or from the selected day's list. */
@Composable
private fun liveEvent(calendarViewModel: CalendarViewModel, eventId: String): CalendarEvent? {
    val monthEvents by calendarViewModel.events.collectAsStateWithLifecycle()
    val dayEvents by calendarViewModel.dayEvents.collectAsStateWithLifecycle()
    return monthEvents.valueOrNull()?.firstOrNull { it.id.value == eventId }
        ?: dayEvents.valueOrNull()?.firstOrNull { it.id.value == eventId }
}
