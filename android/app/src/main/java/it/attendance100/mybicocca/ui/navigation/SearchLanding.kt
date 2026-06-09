package it.attendance100.mybicocca.ui.navigation

import it.attendance100.mybicocca.domain.model.search.SearchDestination

// How a unified-search hit materializes in the shell: a pager tab, a back-stack sub-page,
// or a shell-hosted modal sheet that opens over its owning tab.
sealed interface SearchLanding {
    data class Tab(val tab: ShellTab) : SearchLanding
    data class SubPage(val route: AppRoute) : SearchLanding
    data class Sheet(val sheet: ShellSheet, val hostTab: ShellTab) : SearchLanding
}

// Search-reachable modal sheets hosted by the shell as state flags, not back-stack routes.
enum class ShellSheet { StudyPlan, Attendance, Enrollments, Certificates, Titles, BookedExams, ExamResults, Questionnaires, Appointments, Taxes }

fun SearchDestination.toLanding(): SearchLanding = when (this) {
    SearchDestination.TabCalendar -> SearchLanding.Tab(ShellTab.Calendar)
    SearchDestination.TabElearning -> SearchLanding.Tab(ShellTab.Elearning)
    SearchDestination.TabMap -> SearchLanding.Tab(ShellTab.Map)
    SearchDestination.TabRegistry -> SearchLanding.Tab(ShellTab.Registry)

    SearchDestination.Profile -> SearchLanding.SubPage(AppRoute.Profile)
    SearchDestination.Settings -> SearchLanding.SubPage(AppRoute.Settings)
    SearchDestination.Taxes -> SearchLanding.Sheet(ShellSheet.Taxes, ShellTab.Registry)
    SearchDestination.Appointments -> SearchLanding.Sheet(ShellSheet.Appointments, ShellTab.Registry)
    SearchDestination.Messaging -> SearchLanding.SubPage(AppRoute.Messaging)

    SearchDestination.StudyPlan -> SearchLanding.Sheet(ShellSheet.StudyPlan, ShellTab.Registry)
    SearchDestination.Attendance -> SearchLanding.Sheet(ShellSheet.Attendance, ShellTab.Registry)
    SearchDestination.Enrollments -> SearchLanding.Sheet(ShellSheet.Enrollments, ShellTab.Registry)
    SearchDestination.Certificates -> SearchLanding.Sheet(ShellSheet.Certificates, ShellTab.Registry)
    SearchDestination.Titles -> SearchLanding.Sheet(ShellSheet.Titles, ShellTab.Registry)
    SearchDestination.BookedExams -> SearchLanding.Sheet(ShellSheet.BookedExams, ShellTab.Registry)
    SearchDestination.ExamResults -> SearchLanding.Sheet(ShellSheet.ExamResults, ShellTab.Registry)
    SearchDestination.Questionnaires -> SearchLanding.Sheet(ShellSheet.Questionnaires, ShellTab.Registry)
}
