package it.attendance100.mybicocca.ui.screen.registry

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.ui.component.modal.SheetPage
import it.attendance100.mybicocca.ui.navigation.PopWhenMissing
import it.attendance100.mybicocca.ui.navigation.requireAppNavigator
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.sheetHeader
import it.attendance100.mybicocca.ui.navigation.sheetHeaderInPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.AppelliPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.BookedExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.AppointmentsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.AppointmentsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.AttendancePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.AttendanceViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.certificates.CertificatesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.certificates.CertificatesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.deadlines.DeadlinesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.deadlines.deadlinesHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.EnrollmentsTimelinePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.EnrollmentsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.enrollmentsHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.subscreen.yearDetail.EnrollmentDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.subscreen.yearDetail.enrollmentDetailHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.ExamResultsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.ExamResultsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.isee.IseeDeclarationsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.isee.IseeDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.isee.iseeDetailHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.isee.iseeHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.QuestionnairesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.QuestionnairesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds.RefundDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds.RefundsListPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds.RefundsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds.refundDetailHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds.refundKey
import it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds.refundsHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.StudyPlanPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.StudyPlanViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.taxesHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.TitleDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.TitlesListPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.TitlesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.titleDetailHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.titlesHeader

/**
 * The Segreterie services, each a sheet over the tab. Their ViewModels are shell-scoped: a list
 * and its detail pages are separate entries that must share one owner, the landing derives its
 * badges and deadline spine from the same fetches, and those fetches start on shell load.
 *
 * Detail pages resolve their item from the live list and close themselves when it disappears
 * ([PopWhenMissing]), e.g. after a career switch.
 */
fun EntryProviderScope<NavKey>.registrySheetEntries(
    bookedExamsViewModel: BookedExamsViewModel,
    bookableExamsViewModel: BookableExamsViewModel,
    taxesViewModel: TaxesViewModel,
    examResultsViewModel: ExamResultsViewModel,
    studyPlanViewModel: StudyPlanViewModel,
    enrollmentsViewModel: EnrollmentsViewModel,
    titlesViewModel: TitlesViewModel,
    certificatesViewModel: CertificatesViewModel,
    refundsViewModel: RefundsViewModel,
    attendanceViewModel: AttendanceViewModel,
    questionnairesViewModel: QuestionnairesViewModel,
    appointmentsViewModel: AppointmentsViewModel,
    libraryViewModel: LibraryViewModel,
) {
    entry<SheetRoute.Enrollments>(
        metadata = sheetHeader<SheetRoute.Enrollments> {
            val history by enrollmentsViewModel.history.collectAsStateWithLifecycle()
            enrollmentsHeader(history.valueOrNull())
        },
    ) {
        val navigator = requireAppNavigator()
        EnrollmentsTimelinePage(
            viewModel = enrollmentsViewModel,
            onOpenDetail = { id -> navigator.navigate(SheetRoute.EnrollmentDetail(id.value)) },
        )
    }
    entry<SheetRoute.EnrollmentDetail>(
        metadata = sheetHeader<SheetRoute.EnrollmentDetail> { route ->
            val history by enrollmentsViewModel.history.collectAsStateWithLifecycle()
            enrollmentDetailHeader(history.valueOrNull()?.years?.firstOrNull { it.id.value == route.enrollmentId })
        },
    ) { route ->
        val history by enrollmentsViewModel.history.collectAsStateWithLifecycle()
        val enrollment = history.valueOrNull()?.years?.firstOrNull { it.id.value == route.enrollmentId }
        PopWhenMissing(loaded = history.valueOrNull() != null, missing = enrollment == null)
        if (enrollment != null) EnrollmentDetailPage(enrollment = enrollment)
    }

    entry<SheetRoute.Titles>(
        metadata = sheetHeader<SheetRoute.Titles> {
            val titles by titlesViewModel.titles.collectAsStateWithLifecycle()
            titlesHeader(titles.valueOrNull())
        },
    ) {
        val navigator = requireAppNavigator()
        TitlesListPage(
            viewModel = titlesViewModel,
            onOpenDetail = { id -> navigator.navigate(SheetRoute.TitleDetail(id)) },
        )
    }
    entry<SheetRoute.TitleDetail>(
        metadata = sheetHeader<SheetRoute.TitleDetail> { route ->
            val titles by titlesViewModel.titles.collectAsStateWithLifecycle()
            titleDetailHeader(titles.valueOrNull()?.firstOrNull { it.id == route.titleId })
        },
    ) { route ->
        val titles by titlesViewModel.titles.collectAsStateWithLifecycle()
        val title = titles.valueOrNull()?.firstOrNull { it.id == route.titleId }
        PopWhenMissing(loaded = titles.valueOrNull() != null, missing = title == null)
        if (title != null) TitleDetailPage(title = title)
    }

    entry<SheetRoute.Refunds>(
        metadata = sheetHeader<SheetRoute.Refunds> {
            val refunds by refundsViewModel.refunds.collectAsStateWithLifecycle()
            refundsHeader(refunds.valueOrNull())
        },
    ) {
        val navigator = requireAppNavigator()
        RefundsListPage(
            viewModel = refundsViewModel,
            onOpenDetail = { key -> navigator.navigate(SheetRoute.RefundDetail(key)) },
        )
    }
    entry<SheetRoute.RefundDetail>(
        metadata = sheetHeader<SheetRoute.RefundDetail> { route ->
            val refunds by refundsViewModel.refunds.collectAsStateWithLifecycle()
            refundDetailHeader(refunds.valueOrNull()?.firstOrNull { it.refundKey() == route.refundKey })
        },
    ) { route ->
        val refunds by refundsViewModel.refunds.collectAsStateWithLifecycle()
        val refund = refunds.valueOrNull()?.firstOrNull { it.refundKey() == route.refundKey }
        PopWhenMissing(loaded = refunds.valueOrNull() != null, missing = refund == null)
        if (refund != null) RefundDetailPage(refund = refund)
    }

    entry<SheetRoute.Isee>(
        metadata = sheetHeader<SheetRoute.Isee> {
            val declarations by taxesViewModel.isee.collectAsStateWithLifecycle()
            iseeHeader(
                declarations.valueOrNull()?.filter { it.isee != null && it.academicYearEnrollmentId != null },
            )
        },
    ) {
        val navigator = requireAppNavigator()
        IseeDeclarationsPage(
            viewModel = taxesViewModel,
            onOpenDetail = { year -> navigator.navigate(SheetRoute.IseeDetail(year)) },
        )
    }
    entry<SheetRoute.IseeDetail>(
        metadata = sheetHeader<SheetRoute.IseeDetail> { route ->
            val declarations by taxesViewModel.isee.collectAsStateWithLifecycle()
            iseeDetailHeader(declarations.valueOrNull()?.firstOrNull { it.academicYearEnrollmentId == route.year })
        },
    ) { route ->
        val declarations by taxesViewModel.isee.collectAsStateWithLifecycle()
        val declaration = declarations.valueOrNull()?.firstOrNull { it.academicYearEnrollmentId == route.year }
        PopWhenMissing(loaded = declarations.valueOrNull() != null, missing = declaration == null)
        if (declaration != null) IseeDetailPage(declaration = declaration)
    }

    entry<SheetRoute.Taxes>(
        metadata = sheetHeader<SheetRoute.Taxes> {
            val invoices by taxesViewModel.invoices.collectAsStateWithLifecycle()
            taxesHeader(invoices.valueOrNull())
        },
    ) { TaxesPage(viewModel = taxesViewModel) }

    entry<SheetRoute.Certificates>(metadata = sheetHeaderInPage()) {
        CertificatesPage(viewModel = certificatesViewModel)
    }
    entry<SheetRoute.ExamResults>(metadata = sheetHeaderInPage()) {
        ExamResultsPage(viewModel = examResultsViewModel)
    }
    entry<SheetRoute.Attendance>(metadata = sheetHeaderInPage()) {
        AttendancePage(viewModel = attendanceViewModel)
    }
    entry<SheetRoute.Appelli>(metadata = sheetHeaderInPage()) {
        AppelliPage(bookableViewModel = bookableExamsViewModel, viewModel = bookedExamsViewModel)
    }
    entry<SheetRoute.StudyPlan>(metadata = sheetHeaderInPage()) {
        StudyPlanPage(viewModel = studyPlanViewModel)
    }
    entry<SheetRoute.Questionnaires>(metadata = sheetHeaderInPage()) {
        QuestionnairesPage(viewModel = questionnairesViewModel)
    }
    entry<SheetRoute.Appointments>(metadata = sheetHeaderInPage()) {
        val navigator = requireAppNavigator()
        AppointmentsPage(
            viewModel = appointmentsViewModel,
            onOpenPdf = { path, name ->
                navigator.navigate(AppRoute.FileViewer(fileName = name, localPath = path, mimeType = "application/pdf"))
            },
        )
    }
    entry<SheetRoute.Library>(metadata = sheetHeaderInPage()) {
        LibraryPage(viewModel = libraryViewModel)
    }

    // The scadenzario reads the same deadline spine as the landing's Scadenze banner. Its header
    // is drawn in the page so the spine is derived once for header and list.
    entry<SheetRoute.Deadlines>(metadata = sheetHeaderInPage()) {
        val navigator = requireAppNavigator()
        val state = rememberRegistryDeadlines(
            bookedExamsViewModel = bookedExamsViewModel,
            bookableExamsViewModel = bookableExamsViewModel,
            taxesViewModel = taxesViewModel,
            examResultsViewModel = examResultsViewModel,
            studyPlanViewModel = studyPlanViewModel,
            onOpenExamResults = { navigator.navigate(SheetRoute.ExamResults) },
            onOpenTaxes = { navigator.navigate(SheetRoute.Taxes) },
            onOpenBookedExams = { navigator.navigate(SheetRoute.Appelli) },
        )
        SheetPage(header = deadlinesHeader(deadlines = state.deadlines, loading = state.loading)) {
            DeadlinesPage(
                deadlines = state.deadlines,
                loading = state.loading,
                failure = state.failure,
                onRetry = state.retry,
            )
        }
    }
}
