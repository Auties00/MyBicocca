package it.attendance100.mybicocca.ui.navigation

import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.RoomCode
import it.attendance100.mybicocca.domain.model.search.SearchAction
import it.attendance100.mybicocca.domain.model.search.SearchDestination
import it.attendance100.mybicocca.domain.model.search.SearchResult
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.route.ShellTab
import java.time.LocalDate

/**
 * One step of a guided search navigation. The shell executes a plan's steps in order with a pause
 * between them, so every transition plays out visibly — tab first, then the page sliding in, then
 * the sheet rising over it. The journey teaches the user where the destination lives.
 */
sealed interface SearchNavStep {
    data class SwitchTab(val tab: ShellTab) : SearchNavStep
    data class PushPage(val route: AppRoute) : SearchNavStep
    data class PushSheet(val route: SheetRoute) : SearchNavStep

    data object OpenAccountSwitcher : SearchNavStep

    /**
     * A screen-side effect that has no route: select a calendar day, focus a map room, pop the
     * add-course sheet. The lambda captures the owning ViewModel.
     */
    data class Run(val action: () -> Unit) : SearchNavStep
}

/** The ViewModel calls a plan may need; supplied by [MainShell], where they are all hoisted. */
class SearchNavHooks(
    val selectCalendarDay: (LocalDate) -> Unit,
    val openCalendarEvent: (CalendarEventId) -> Unit,
    val selectBuilding: (BuildingCode) -> Unit,
    val selectRoom: (BuildingCode, RoomCode) -> Unit,
    val requestAddCourse: () -> Unit,
    val requestHypotheticalCalculator: () -> Unit,
)

/**
 * Builds the guided step plan that brings a tapped search hit on screen. Transcript (libretto)
 * hits land on the Profile page, which renders the libretto.
 */
fun SearchResult.toNavPlan(hooks: SearchNavHooks): List<SearchNavStep> = when (this) {
    is SearchResult.Action -> action.toNavPlan(hooks)
    is SearchResult.Destination -> destination.toNavPlan()

    is SearchResult.Course -> listOf(
        SearchNavStep.SwitchTab(ShellTab.Elearning),
        SearchNavStep.PushPage(AppRoute.CourseDetail(courseId.value)),
    )

    is SearchResult.Assignment -> listOf(
        SearchNavStep.SwitchTab(ShellTab.Elearning),
        SearchNavStep.PushPage(AppRoute.CourseDetail(courseId.value)),
        SearchNavStep.PushSheet(SheetRoute.AssignmentDetail(assignmentId.value, courseId.value)),
    )

    is SearchResult.Quiz -> listOf(
        SearchNavStep.SwitchTab(ShellTab.Elearning),
        SearchNavStep.PushPage(AppRoute.CourseDetail(courseId.value)),
        SearchNavStep.PushSheet(SheetRoute.QuizDetail(quizId.value, courseId.value)),
    )

    is SearchResult.CalendarEntry -> listOf(
        SearchNavStep.SwitchTab(ShellTab.Calendar),
        SearchNavStep.Run { hooks.selectCalendarDay(date) },
        SearchNavStep.Run { hooks.openCalendarEvent(eventId) },
    )

    is SearchResult.CalendarDay -> listOf(
        SearchNavStep.SwitchTab(ShellTab.Calendar),
        SearchNavStep.Run { hooks.selectCalendarDay(date) },
    )

    is SearchResult.Building -> listOf(
        SearchNavStep.SwitchTab(ShellTab.Map),
        SearchNavStep.Run { hooks.selectBuilding(code) },
    )

    is SearchResult.Room -> listOf(
        SearchNavStep.SwitchTab(ShellTab.Map),
        SearchNavStep.Run { hooks.selectRoom(buildingCode, roomCode) },
    )

    is SearchResult.TranscriptEntry -> listOf(
        SearchNavStep.PushPage(AppRoute.Profile),
    )
}

/**
 * Builds the guided step plan for a static destination hit. Every per-section settings
 * destination lands on the one settings page, which hosts all of its sections inline.
 */
fun SearchDestination.toNavPlan(): List<SearchNavStep> = when (this) {
    SearchDestination.TabCalendar -> listOf(SearchNavStep.SwitchTab(ShellTab.Calendar))
    SearchDestination.TabElearning -> listOf(SearchNavStep.SwitchTab(ShellTab.Elearning))
    SearchDestination.TabMap -> listOf(SearchNavStep.SwitchTab(ShellTab.Map))
    SearchDestination.TabRegistry -> listOf(SearchNavStep.SwitchTab(ShellTab.Registry))

    SearchDestination.Profile -> listOf(SearchNavStep.PushPage(AppRoute.Profile))

    SearchDestination.Taxes -> registrySheet(SheetRoute.Taxes)
    SearchDestination.ExamResults -> registrySheet(SheetRoute.ExamResults)
    SearchDestination.BookedExams -> registrySheet(SheetRoute.Appelli)
    SearchDestination.StudyPlan -> registrySheet(SheetRoute.StudyPlan)
    SearchDestination.Attendance -> registrySheet(SheetRoute.Attendance)
    SearchDestination.Questionnaires -> registrySheet(SheetRoute.Questionnaires)
    SearchDestination.Appointments -> registrySheet(SheetRoute.Appointments)
    SearchDestination.Enrollments -> registrySheet(SheetRoute.Enrollments)
    SearchDestination.Titles -> registrySheet(SheetRoute.Titles)
    SearchDestination.Certificates -> registrySheet(SheetRoute.Certificates)
    SearchDestination.Library -> registrySheet(SheetRoute.Library)
    SearchDestination.Refunds -> registrySheet(SheetRoute.Refunds)
    SearchDestination.Isee -> registrySheet(SheetRoute.Isee)

    SearchDestination.Settings,
    SearchDestination.SettingsAppearance,
    SearchDestination.SettingsSecurity,
    SearchDestination.SettingsLanguage,
    SearchDestination.SettingsFileAssociations,
    SearchDestination.SettingsLicenses,
    SearchDestination.SettingsAppInfo,
    -> listOf(SearchNavStep.PushPage(AppRoute.Settings))
}

private fun SearchAction.toNavPlan(hooks: SearchNavHooks): List<SearchNavStep> = when (this) {
    SearchAction.BookExam -> registrySheet(SheetRoute.Appelli)
    SearchAction.MarkPresence -> registrySheet(SheetRoute.Attendance)
    SearchAction.ReserveLibrarySeat -> registrySheet(SheetRoute.Library)
    SearchAction.BookAppointment -> registrySheet(SheetRoute.Appointments)
    SearchAction.PayTaxes -> registrySheet(SheetRoute.Taxes)
    SearchAction.CompileQuestionnaire -> registrySheet(SheetRoute.Questionnaires)
    SearchAction.EditStudyPlan -> registrySheet(SheetRoute.StudyPlan)

    SearchAction.AddCourse -> listOf(
        SearchNavStep.SwitchTab(ShellTab.Elearning),
        SearchNavStep.Run(hooks.requestAddCourse),
    )

    SearchAction.HypotheticalAverage -> listOf(
        SearchNavStep.PushPage(AppRoute.Profile),
        SearchNavStep.Run(hooks.requestHypotheticalCalculator),
    )

    SearchAction.ChangeTheme -> listOf(SearchNavStep.PushPage(AppRoute.Settings))
    SearchAction.ChangeLanguage -> listOf(SearchNavStep.PushPage(AppRoute.Settings))
    SearchAction.AddAccount -> listOf(SearchNavStep.OpenAccountSwitcher)
}

private fun registrySheet(route: SheetRoute): List<SearchNavStep> = listOf(
    SearchNavStep.SwitchTab(ShellTab.Registry),
    SearchNavStep.PushSheet(route),
)
