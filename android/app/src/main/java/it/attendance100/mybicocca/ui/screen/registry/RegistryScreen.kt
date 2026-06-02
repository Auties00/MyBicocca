package it.attendance100.mybicocca.ui.screen.registry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Work
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.exam.AcknowledgmentStatus
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.ui.screen.registry.component.ScadenzeHeader
import it.attendance100.mybicocca.ui.screen.registry.component.ServiceGroupCard
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadge
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadgeTone
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryService
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryServiceGroup
import it.attendance100.mybicocca.ui.screen.registry.state.buildRegistryDeadlines
import it.attendance100.mybicocca.ui.screen.registry.state.isUrgent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.deadlines.DeadlinesSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.deadlines.nextDeadlineLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookableExams.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExams.BookedExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.ExamResultsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import java.time.LocalDate

// Landing of the Registry (Segreterie) tab: a tappable "Scadenze" banner that opens the
// scadenzario timeline, followed by an outlined directory of every Esse3 service grouped
// by area. Live status badges (open exam calls, new results, tax position) and the
// deadline spine are both derived from the same in-memory feature streams.
@Composable
fun RegistryScreen(
    bookedExamsViewModel: BookedExamsViewModel,
    bookableExamsViewModel: BookableExamsViewModel,
    taxesViewModel: TaxesViewModel,
    examResultsViewModel: ExamResultsViewModel,
    onOpenBookedExams: () -> Unit,
    onOpenTaxes: () -> Unit,
    onOpenExamResults: () -> Unit,
    onOpenStudyPlan: () -> Unit,
    onOpenQuestionnaires: () -> Unit,
    onOpenReservations: () -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenInternships: () -> Unit,
    onOpenSelfCertificates: () -> Unit,
    onOpenDegreeAward: () -> Unit,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    // True only while this is the visible tab — see CalendarScreen for the pager-cache rationale.
    isActive: Boolean = true,
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
) {
    LaunchedEffect(isActive) { if (isActive) onProvideFilterToggle(null) }

    val bookings by bookedExamsViewModel.bookings.collectAsStateWithLifecycle()
    val examCalls by bookableExamsViewModel.examCalls.collectAsStateWithLifecycle()
    val invoices by taxesViewModel.invoices.collectAsStateWithLifecycle()
    val examResults by examResultsViewModel.results.collectAsStateWithLifecycle()

    val bookingList = bookings.valueOrNull().orEmpty()
    val examCallList = examCalls.valueOrNull().orEmpty()
    val invoiceList = invoices.valueOrNull().orEmpty()
    val resultList = examResults.valueOrNull().orEmpty()

    // Badges.
    val availableCount = examCallList.size
    val examsBadge = if (availableCount > 0) {
        RegistryBadge("$availableCount disponibili", RegistryBadgeTone.New)
    } else null

    val unviewedCount = resultList.count { it.acknowledgment == AcknowledgmentStatus.NotViewed }
    val resultsBadge = if (unviewedCount > 0) {
        RegistryBadge(if (unviewedCount == 1) "1 nuovo" else "$unviewedCount nuovi", RegistryBadgeTone.Alert)
    } else null

    val expiredCount = invoiceList.count { it.status == TaxStatus.EXPIRED }
    val pendingCount = invoiceList.count { it.status == TaxStatus.PENDING }
    val taxBadge = when {
        invoices.valueOrNull() == null -> null
        expiredCount > 0 -> RegistryBadge("In ritardo", RegistryBadgeTone.Alert)
        pendingCount > 0 -> RegistryBadge(if (pendingCount == 1) "1 da pagare" else "$pendingCount da pagare", RegistryBadgeTone.Attention)
        else -> RegistryBadge("In regola", RegistryBadgeTone.Ok)
    }

    // Deadlines (scadenzario spine + header summary).
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
            onOpenBookedExams = onOpenBookedExams,
        )
    }
    val urgentCount = deadlines.count { it.isUrgent() }
    val headerSummary = when {
        deadlines.isEmpty() -> "Nessuna scadenza imminente"
        urgentCount == 0 -> "Prossima il ${nextDeadlineLabel(deadlines.first().date)}"
        else -> {
            val urgent = if (urgentCount == 1) "1 urgente" else "$urgentCount urgenti"
            "$urgent · prossima il ${nextDeadlineLabel(deadlines.first().date)}"
        }
    }

    val groups = listOf(
        RegistryServiceGroup(
            name = "Didattica",
            caption = "Il tuo percorso accademico",
            services = listOf(
                RegistryService("study_plan", "Piano di studi", "Percorso e crediti", Icons.AutoMirrored.Outlined.MenuBook, onClick = onOpenStudyPlan),
                RegistryService("exams", "Esami", "Appelli e prenotazioni", Icons.Outlined.EditCalendar, examsBadge, onOpenBookedExams),
                RegistryService("exam_results", "Esiti esami", "Voti e accettazione", Icons.AutoMirrored.Outlined.FactCheck, resultsBadge, onOpenExamResults),
                RegistryService("attendance", "Presenze", "Frequenze e rilevazioni", Icons.Outlined.CoPresent, onClick = onOpenAttendance),
                RegistryService("questionnaires", "Questionari", "Valutazione della didattica", Icons.AutoMirrored.Outlined.Assignment, onClick = onOpenQuestionnaires),
            ),
        ),
        RegistryServiceGroup(
            name = "Tasse & documenti",
            caption = "La tua posizione amministrativa",
            services = listOf(
                RegistryService("taxes", "Tasse & agevolazioni", "Pagamenti ed esoneri", Icons.Outlined.Payments, taxBadge, onOpenTaxes),
                RegistryService("self_certificates", "Autocertificazioni", "Certificati e dichiarazioni", Icons.Outlined.Description, onClick = onOpenSelfCertificates),
                RegistryService("degree_award", "Conseguimento titolo", "Domanda di laurea", Icons.Outlined.School, onClick = onOpenDegreeAward),
            ),
        ),
        RegistryServiceGroup(
            name = "Procedure & opportunità",
            caption = "Esperienze e prenotazioni",
            services = listOf(
                RegistryService("internships", "Tirocini e stage", "Ricerca e gestione stage", Icons.Outlined.Work, onClick = onOpenInternships),
                RegistryService("reservations", "Prenotazioni", "Appuntamenti agli sportelli", Icons.Outlined.EventSeat, onClick = onOpenReservations),
            ),
        ),
    )

    var showDeadlines by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "scadenze") {
            ScadenzeHeader(
                urgentCount = urgentCount,
                summary = headerSummary,
                onClick = { showDeadlines = true },
            )
        }
        groups.forEach { group ->
            item(key = "group_${group.name}") {
                ServiceGroupCard(group = group)
            }
        }
    }

    if (showDeadlines) {
        DeadlinesSheet(
            deadlines = deadlines,
            onDismiss = { showDeadlines = false },
        )
    }
}
