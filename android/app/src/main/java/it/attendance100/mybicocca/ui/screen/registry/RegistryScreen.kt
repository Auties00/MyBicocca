package it.attendance100.mybicocca.ui.screen.registry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Grading
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.WorkspacePremium
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
import it.attendance100.mybicocca.ui.screen.registry.component.RegistryServiceSection
import it.attendance100.mybicocca.ui.screen.registry.component.ScadenzeHeader
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryService
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryServiceGroup
import it.attendance100.mybicocca.ui.screen.registry.state.buildRegistryDeadlines
import it.attendance100.mybicocca.ui.screen.registry.state.isUrgent
import it.attendance100.mybicocca.ui.screen.registry.theme.serviceAccents
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.BookedExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.deadlines.DeadlinesSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.deadlines.nextDeadlineLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.ExamResultsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import java.time.LocalDate

// Landing of the Registry (Segreterie) tab: a pinned "Scadenze" banner that opens the
// scadenzario timeline, over a directory of services grouped into connected segmented
// cards. Live status badges (open exam calls, new outcomes, tax position) and the
// deadline spine are both derived from the same in-memory feature streams.
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

    val today = remember { LocalDate.now() }

    // Deadlines (scadenzario spine + header summary).
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
        deadlines.isEmpty() -> "Nessuna scadenza imminente"
        urgentCount == 0 -> "Prossima il ${nextDeadlineLabel(deadlines.first().date)}"
        else -> {
            val urgent = if (urgentCount == 1) "1 urgente" else "$urgentCount urgenti"
            "$urgent · prossima il ${nextDeadlineLabel(deadlines.first().date)}"
        }
    }

    // One section per directory group; each gets its own icon-chip accent (see serviceAccents()).
    val sections = listOf(
        RegistryServiceGroup(
            name = "Didattica",
            caption = "Percorso, presenze ed esiti",
            services = listOf(
                RegistryService("study_plan", "Percorso", "Percorso e piano di studi", Icons.Outlined.AccountTree, onClick = onOpenStudyPlan),
                RegistryService("attendance", "Presenze", "Frequenze e rilevazioni", Icons.Outlined.CoPresent, onClick = onOpenAttendance),
                RegistryService("exam_results", "Esiti", "Accetta o rifiuta i voti", Icons.Outlined.Grading, onClick = onOpenExamResults),
                RegistryService("questionnaires", "Questionari", "Valutazione didattica", Icons.AutoMirrored.Outlined.FactCheck, onClick = onOpenQuestionnaires),
            ),
        ),
        RegistryServiceGroup(
            name = "Prenotazioni",
            caption = "Appelli, sportelli e posti studio",
            services = listOf(
                RegistryService("appelli", "Appelli", "Esami prenotati e prenotazione", Icons.Outlined.EventAvailable, onClick = onOpenAppelli),
                RegistryService("appointments", "Segreterie", "Prenota uno sportello in segreteria", Icons.Outlined.SupportAgent, onClick = onOpenAppointments),
                RegistryService("library", "Biblioteca", "Prenota un posto in biblioteca", Icons.Outlined.LocalLibrary, onClick = onOpenLibrary),
            ),
        ),
        RegistryServiceGroup(
            name = "Documenti",
            caption = "Iscrizioni, titoli e certificati",
            services = listOf(
                RegistryService("enrollments", "Iscrizioni", "Storico iscrizioni e rinnovo", Icons.Outlined.School, onClick = onOpenEnrollments),
                RegistryService("titles", "Titoli", "Titoli di studio conseguiti", Icons.Outlined.WorkspacePremium, onClick = onOpenTitles),
                RegistryService("certificates", "Certificati", "Autocertificazioni in PDF", Icons.Outlined.Description, onClick = onOpenCertificates),
            ),
        ),
        RegistryServiceGroup(
            name = "Tasse & agevolazioni",
            caption = "La tua posizione amministrativa",
            services = listOf(
                RegistryService("taxes", "Tasse", "Pagamenti e fatture", Icons.Outlined.Payments, onClick = onOpenTaxes),
                RegistryService("isee", "ISEE", "Dichiarazioni e fasce", Icons.Outlined.Savings, onClick = onOpenIsee),
                RegistryService("refunds", "Rimborsi", "Importi e mandati", Icons.Outlined.CurrencyExchange, onClick = onOpenRefunds),
            ),
        ),
        RegistryServiceGroup(
            name = "Opportunità",
            caption = "Esperienze e nuove occasioni",
            services = listOf(
                RegistryService("internships", "Tirocini e stage", "Ricerca e gestione stage", Icons.Outlined.Work, onClick = {}),
                RegistryService("opportunities", "Opportunità", "Bandi, borse e mobilità", Icons.Outlined.Explore, onClick = {}),
                RegistryService("admissions", "Ammissioni", "Concorsi e graduatorie", Icons.Outlined.HowToReg, onClick = {}),
            ),
        ),
    )

    var showDeadlines by remember { mutableStateOf(false) }

    // The Scadenze banner stays pinned above the directory; only the sections scroll.
    Column(modifier = modifier.fillMaxSize()) {
        ScadenzeHeader(
            summary = headerSummary,
            onClick = { showDeadlines = true },
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                )
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
