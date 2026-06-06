package it.attendance100.mybicocca.ui.navigation

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.search.SearchDestination

// Domain -> navigation mapping for unified-search hits. Tabs return null here and resolve
// via toShellTab() instead, since they are pager pages rather than back-stack routes.
fun SearchDestination.toAppRoute(activeCareerId: CareerId?): AppRoute? = when (this) {
    SearchDestination.TabCalendar,
    SearchDestination.TabElearning,
    SearchDestination.TabMap,
    SearchDestination.TabRegistry,
        -> null

    SearchDestination.Profile -> AppRoute.Profile
    SearchDestination.Settings -> AppRoute.Settings
    SearchDestination.Taxes -> AppRoute.Taxes
    SearchDestination.ExamResults -> AppRoute.ExamResults
    SearchDestination.BookedExams -> AppRoute.BookedExams
    SearchDestination.StudyPlan -> AppRoute.StudyPlan
    SearchDestination.Attendance -> AppRoute.Attendance
    SearchDestination.Internships -> AppRoute.Internships
    SearchDestination.Questionnaires -> AppRoute.Questionnaires
    SearchDestination.DegreeAward -> AppRoute.DegreeAward
    SearchDestination.Reservations -> AppRoute.Reservations
    SearchDestination.Messaging -> AppRoute.Messaging
}

fun SearchDestination.toShellTab(): ShellTab? = when (this) {
    SearchDestination.TabCalendar -> ShellTab.Calendar
    SearchDestination.TabElearning -> ShellTab.Elearning
    SearchDestination.TabMap -> ShellTab.Map
    SearchDestination.TabRegistry -> ShellTab.Registry
    else -> null
}
