package it.attendance100.mybicocca.ui.screen.registry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import it.attendance100.mybicocca.ui.navigation.LocalAppNavigator
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.scene.sheetHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.BookedExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.deadlines.DeadlinesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.deadlines.deadlinesHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.ExamResultsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.StudyPlanViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel

/**
 * The Segreterie landing's own sheets: the scadenzario, built from the same shell-scoped feature
 * ViewModels as the Scadenze banner so both read one deadline spine. Each entry routes to its
 * owning service sheet through the navigator.
 */
fun EntryProviderScope<NavKey>.registrySheetEntries(
    bookedExamsViewModel: BookedExamsViewModel,
    bookableExamsViewModel: BookableExamsViewModel,
    taxesViewModel: TaxesViewModel,
    examResultsViewModel: ExamResultsViewModel,
    studyPlanViewModel: StudyPlanViewModel,
) {
    entry<SheetRoute.Deadlines>(
        metadata = sheetHeader<SheetRoute.Deadlines> {
            val state = rememberRegistryDeadlines(
                bookedExamsViewModel = bookedExamsViewModel,
                bookableExamsViewModel = bookableExamsViewModel,
                taxesViewModel = taxesViewModel,
                examResultsViewModel = examResultsViewModel,
                studyPlanViewModel = studyPlanViewModel,
                onOpenExamResults = {},
                onOpenTaxes = {},
                onOpenBookedExams = {},
            )
            deadlinesHeader(deadlines = state.deadlines, loading = state.loading)
        },
    ) {
        val navigator = LocalAppNavigator.current
        val state = rememberRegistryDeadlines(
            bookedExamsViewModel = bookedExamsViewModel,
            bookableExamsViewModel = bookableExamsViewModel,
            taxesViewModel = taxesViewModel,
            examResultsViewModel = examResultsViewModel,
            studyPlanViewModel = studyPlanViewModel,
            onOpenExamResults = { navigator?.navigate(SheetRoute.ExamResults) },
            onOpenTaxes = { navigator?.navigate(SheetRoute.Taxes) },
            onOpenBookedExams = { navigator?.navigate(SheetRoute.Appelli) },
        )
        DeadlinesPage(
            deadlines = state.deadlines,
            loading = state.loading,
            failure = state.failure,
            onRetry = state.retry,
        )
    }
}
