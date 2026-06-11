package it.attendance100.mybicocca.ui.screen.registry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.Grading
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.ui.screen.registry.component.RegistryServiceSection
import it.attendance100.mybicocca.ui.screen.registry.component.ScadenzeHeader
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryService
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryServiceGroup
import it.attendance100.mybicocca.ui.screen.registry.state.buildRegistryDeadlines
import it.attendance100.mybicocca.ui.screen.registry.state.isUrgent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.BookedExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.deadlines.DeadlinesSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.deadlines.nextDeadlineLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.ExamResultsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import it.attendance100.mybicocca.ui.screen.registry.theme.serviceAccents
import java.time.LocalDate

/**
 * Landing of the Registry (Segreterie) tab: a pinned "Scadenze" banner over a scrollable
 * directory of Esse3 services grouped into connected segmented cards, one icon-chip accent
 * hue per group (see [serviceAccents]). Tapping the banner opens the scadenzario timeline
 * sheet; tapping a row routes to the owning service sheet.
 *
 * The banner summary and the deadline spine derive from the in-memory streams of four
 * feature ViewModels (bookings, exam calls, invoices, exam results). The spine counts as
 * loading until every stream has delivered — a partial merge would read as "fewer
 * deadlines" rather than "still loading" — and the summary line covers the loading,
 * failure, empty and urgent-count states. The banner stays pinned above the directory;
 * only the sections scroll. [isActive] is true only while this is the visible tab — see
 * CalendarScreen for the pager-cache rationale.
 */
@Composable
fun RegistryScreen(
    bookedExamsViewModel: BookedExamsViewModel,
    bookableExamsViewModel: BookableExamsViewModel,
    taxesViewModel: TaxesViewModel,
    examResultsViewModel: ExamResultsViewModel,
    onOpenAppelli: () -> Unit,
    onOpenTaxes: () -> Unit,
    onOpenIsee: () -> Unit,
    onOpenRefunds: () -> Unit,
    onOpenExamResults: () -> Unit,
    onOpenStudyPlan: () -> Unit,
    onOpenQuestionnaires: () -> Unit,
    onOpenAppointments: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenEnrollments: () -> Unit,
    onOpenTitles: () -> Unit,
    onOpenCertificates: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
) {
    LaunchedEffect(isActive) { if (isActive) onProvideFilterToggle(null) }

    val bookings by bookedExamsViewModel.bookings.collectAsStateWithLifecycle()
    val examCalls by bookableExamsViewModel.examCalls.collectAsStateWithLifecycle()
    val invoices by taxesViewModel.invoices.collectAsStateWithLifecycle()
    val examResults by examResultsViewModel.results.collectAsStateWithLifecycle()
    val bookingsSync by bookedExamsViewModel.syncStatus.collectAsStateWithLifecycle()
    val examCallsSync by bookableExamsViewModel.syncStatus.collectAsStateWithLifecycle()
    val invoicesSync by taxesViewModel.syncStatus.collectAsStateWithLifecycle()
    val examResultsSync by examResultsViewModel.syncStatus.collectAsStateWithLifecycle()

    val bookingList = bookings.valueOrNull().orEmpty()
    val examCallList = examCalls.valueOrNull().orEmpty()
    val invoiceList = invoices.valueOrNull().orEmpty()
    val resultList = examResults.valueOrNull().orEmpty()

    val deadlinesLoading = listOf(bookings, examCalls, invoices, examResults)
        .any { it is Loadable.NotYetLoaded }
    val deadlinesFailure = listOf(bookingsSync, examCallsSync, invoicesSync, examResultsSync)
        .firstNotNullOfOrNull { (it as? SyncStatus.Failed)?.cause }

    val today = remember { LocalDate.now() }

    val deadlines = remember(resultList, invoiceList, bookingList, examCallList) {
        buildRegistryDeadlines(
            today = today,
            examResults = resultList,
            invoices = invoiceList,
            bookings = bookingList,
            examCalls = examCallList,
            onOpenExamResults = onOpenExamResults,
            onOpenTaxes = onOpenTaxes,
            onOpenBookedExams = onOpenAppelli,
        )
    }
    val urgentCount = deadlines.count { it.isUrgent() }
    val headerSummary = when {
        deadlinesLoading && deadlinesFailure != null -> stringResource(R.string.registry_sync_failed)
        deadlinesLoading -> stringResource(R.string.common_loading)
        deadlines.isEmpty() -> stringResource(R.string.registry_no_deadlines)
        urgentCount == 0 -> stringResource(
            R.string.registry_next_deadline,
            nextDeadlineLabel(deadlines.first().date)
        )
        else -> {
            val urgent =
                if (urgentCount == 1) stringResource(R.string.registry_one_urgent) else stringResource(
                    R.string.registry_multiple_urgent,
                    urgentCount
                )
            "$urgent · " + stringResource(
                R.string.registry_next_deadline,
                nextDeadlineLabel(deadlines.first().date)
            )
        }
    }

    val sections = listOf(
        RegistryServiceGroup(
            name = stringResource(R.string.registry_teaching),
            caption = stringResource(R.string.registry_teaching_caption),
            services = listOf(
                RegistryService(
                    "study_plan",
                    stringResource(R.string.registry_study_plan),
                    stringResource(R.string.registry_study_plan_desc),
                    Icons.Outlined.AccountTree,
                    onClick = onOpenStudyPlan
                ),
                RegistryService(
                    "attendance",
                    stringResource(R.string.registry_attendance),
                    stringResource(R.string.registry_attendance_desc),
                    Icons.Outlined.CoPresent,
                    onClick = onOpenAttendance
                ),
                RegistryService(
                    "exam_results",
                    stringResource(R.string.registry_exam_results),
                    stringResource(R.string.registry_exam_results_desc),
                    Icons.AutoMirrored.Outlined.Grading,
                    onClick = onOpenExamResults
                ),
                RegistryService(
                    "questionnaires",
                    stringResource(R.string.registry_questionnaires),
                    stringResource(R.string.registry_questionnaires_desc),
                    Icons.AutoMirrored.Outlined.FactCheck,
                    onClick = onOpenQuestionnaires
                ),
            ),
        ),
        RegistryServiceGroup(
            name = stringResource(R.string.registry_bookings),
            caption = stringResource(R.string.registry_bookings_caption),
            services = listOf(
                RegistryService(
                    "appelli",
                    stringResource(R.string.appelli_title),
                    stringResource(R.string.appelli_desc),
                    Icons.Outlined.EventAvailable,
                    onClick = onOpenAppelli
                ),
                RegistryService(
                    "appointments",
                    stringResource(R.string.appointments_title),
                    stringResource(R.string.appointments_desc),
                    Icons.Outlined.SupportAgent,
                    onClick = onOpenAppointments
                ),
                RegistryService(
                    "library",
                    stringResource(R.string.registry_library),
                    stringResource(R.string.registry_library_desc),
                    Icons.Outlined.LocalLibrary,
                    onClick = onOpenLibrary
                ),
            ),
        ),
        RegistryServiceGroup(
            name = stringResource(R.string.registry_documents),
            caption = stringResource(R.string.registry_documents_caption),
            services = listOf(
                RegistryService(
                    "enrollments",
                    stringResource(R.string.registry_enrollments),
                    stringResource(R.string.registry_enrollments_desc),
                    Icons.Outlined.School,
                    onClick = onOpenEnrollments
                ),
                RegistryService(
                    "titles",
                    stringResource(R.string.registry_titles),
                    stringResource(R.string.registry_titles_desc),
                    Icons.Outlined.WorkspacePremium,
                    onClick = onOpenTitles
                ),
                RegistryService(
                    "certificates",
                    stringResource(R.string.registry_certificates),
                    stringResource(R.string.registry_certificates_desc),
                    Icons.Outlined.Description,
                    onClick = onOpenCertificates
                ),
            ),
        ),
        RegistryServiceGroup(
            name = stringResource(R.string.registry_taxes),
            caption = stringResource(R.string.registry_taxes_caption),
            services = listOf(
                RegistryService(
                    "taxes",
                    stringResource(R.string.registry_fees),
                    stringResource(R.string.registry_fees_desc),
                    Icons.Outlined.Payments,
                    onClick = onOpenTaxes
                ),
                RegistryService(
                    "isee",
                    stringResource(R.string.registry_isee),
                    stringResource(R.string.registry_isee_desc),
                    Icons.Outlined.Savings,
                    onClick = onOpenIsee
                ),
                RegistryService(
                    "refunds",
                    stringResource(R.string.registry_refunds),
                    stringResource(R.string.registry_refunds_desc),
                    Icons.Outlined.CurrencyExchange,
                    onClick = onOpenRefunds
                ),
            ),
        ),
    )

    var showDeadlines by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().testTag(RegistryTestTags.ROOT)) {
        ScadenzeHeader(
            summary = headerSummary,
            onClick = { showDeadlines = true },
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
                .testTag(RegistryTestTags.SCADENZE_HEADER),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag(RegistryTestTags.SERVICES)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            val accents = serviceAccents()
            sections.forEachIndexed { index, group ->
                val accent = accents[index % accents.size]
                RegistryServiceSection(
                    group = group,
                    accentContainer = accent.container,
                    accentOnContainer = accent.onContainer,
                    tileTag = { service -> RegistryTestTags.service(service.id) },
                )
            }
        }
    }

    if (showDeadlines) {
        DeadlinesSheet(
            deadlines = deadlines,
            loading = deadlinesLoading,
            failure = deadlinesFailure,
            onRetry = {
                if (bookingsSync is SyncStatus.Failed) bookedExamsViewModel.refresh()
                if (examCallsSync is SyncStatus.Failed) bookableExamsViewModel.refresh()
                if (invoicesSync is SyncStatus.Failed) taxesViewModel.refresh()
                if (examResultsSync is SyncStatus.Failed) examResultsViewModel.refresh()
            },
            onDismiss = { showDeadlines = false },
        )
    }
}
