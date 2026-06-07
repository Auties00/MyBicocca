package it.attendance100.mybicocca.ui.screen.registry

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.Grading
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.ui.screen.registry.component.RegistryServiceSection
import it.attendance100.mybicocca.ui.screen.registry.component.ScadenzeHeader
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryService
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryServiceGroup
import it.attendance100.mybicocca.ui.screen.registry.state.buildRegistryDeadlines
import it.attendance100.mybicocca.ui.screen.registry.state.isUrgent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookableExams.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExams.BookedExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.deadlines.DeadlinesSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.deadlines.nextDeadlineLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.ExamResultsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import java.time.LocalDate

// Landing of the Registry (Segreterie) tab: a pinned "Scadenze" banner that opens the
// scadenzario timeline, over a directory of services grouped into connected segmented
// cards. Live status badges (open exam calls, new outcomes, tax position) and the
// deadline spine are both derived from the same in-memory feature streams.
@Suppress("AssignedValueIsNeverRead")
@Composable
fun RegistryScreen(
    bookedExamsViewModel: BookedExamsViewModel,
    bookableExamsViewModel: BookableExamsViewModel,
    taxesViewModel: TaxesViewModel,
    examResultsViewModel: ExamResultsViewModel,
    onOpenBookedExams: () -> Unit,
    onOpenBookableExams: () -> Unit,
    onOpenTaxes: () -> Unit,
    onOpenIsee: () -> Unit,
    onOpenRefunds: () -> Unit,
    onOpenExamResults: () -> Unit,
    onOpenStudyPlan: () -> Unit,
    onOpenQuestionnaires: () -> Unit,
    onOpenProcedures: () -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenInternships: () -> Unit,
    onOpenDegreeAward: () -> Unit,
    modifier: Modifier = Modifier,
    // True only while this is the visible tab — see CalendarScreen for the pager-cache rationale.
    isActive: Boolean = true,
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
) {
    LaunchedEffect(isActive) { if (isActive) onProvideFilterToggle(null) }

    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    // External university pages open in an in-app browser (Custom Tab) instead of
    // kicking the user out to the system browser.
    val openInAppBrowser: (String) -> Unit = remember(context) {
        { url ->
            CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, url.toUri())
        }
    }

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

    // (group, iconChip container, iconChip onContainer) per section.
    val sections = listOf(
        Triple(
            RegistryServiceGroup(
                name = "Didattica",
                caption = "Il tuo percorso accademico",
                services = listOf(
                    RegistryService(
                        "study_plan",
                        "Percorso e piano",
                        "Percorso, orientamento, piano e crediti",
                        Icons.Outlined.AccountTree,
                        onClick = onOpenStudyPlan
                    ),
                    RegistryService(
                        "attendance",
                        "Presenze",
                        "Frequenze e rilevazioni",
                        Icons.Outlined.CoPresent,
                        onClick = onOpenAttendance
                    ),
                    RegistryService(
                        "questionnaires",
                        "Questionari",
                        "Valutazione didattica",
                        Icons.AutoMirrored.Outlined.FactCheck,
                        onClick = onOpenQuestionnaires
                    ),
                    RegistryService(
                        "degree_award",
                        "Conseguimento titolo",
                        "Domanda di laurea e tesi",
                        Icons.Outlined.School,
                        onClick = onOpenDegreeAward
                    ),
                ),
            ),
            scheme.primaryContainer, scheme.onPrimaryContainer,
        ),
        Triple(
            RegistryServiceGroup(
                name = "Esami",
                caption = "Appelli, prenotazioni ed esiti",
                services = listOf(
                    RegistryService(
                        "booked_exams",
                        "Prenotazioni",
                        "I tuoi esami prenotati",
                        Icons.Outlined.EventAvailable,
                        onClick = onOpenBookedExams
                    ),
                    RegistryService(
                        "bookable_exams",
                        "Prenota esame",
                        "Appelli disponibili",
                        Icons.Outlined.EditCalendar,
                        onClick = onOpenBookableExams
                    ),
                    RegistryService(
                        "exam_results",
                        "Esiti",
                        "Accetta o rifiuta esiti",
                        Icons.AutoMirrored.Outlined.Grading,
                        onClick = onOpenExamResults
                    ),
                ),
            ),
            scheme.secondaryContainer, scheme.onSecondaryContainer,
        ),
        Triple(
            RegistryServiceGroup(
                name = "Tasse & agevolazioni",
                caption = "La tua posizione amministrativa",
                services = listOf(
                    RegistryService(
                        "taxes",
                        "Tasse",
                        "Pagamenti e fatture",
                        Icons.Outlined.Payments,
                        onClick = onOpenTaxes
                    ),
                    RegistryService(
                        "isee",
                        "ISEE",
                        "Dichiarazioni e fasce",
                        Icons.Outlined.Savings,
                        onClick = onOpenIsee
                    ),
                    RegistryService(
                        "refunds",
                        "Rimborsi",
                        "Importi e mandati",
                        Icons.Outlined.CurrencyExchange,
                        onClick = onOpenRefunds
                    ),
                ),
            ),
            scheme.tertiaryContainer, scheme.onTertiaryContainer,
        ),
        Triple(
            RegistryServiceGroup(
                name = "Procedure & opportunità",
                caption = "Esperienze e nuove occasioni",
                services = listOf(
                    RegistryService(
                        "internships",
                        "Tirocini e stage",
                        "Ricerca e gestione stage",
                        Icons.Outlined.Work,
                        onClick = onOpenInternships
                    ),
                    RegistryService(
                        "requests",
                        "Domande e procedure",
                        "Appuntamenti e istanze",
                        Icons.AutoMirrored.Outlined.Assignment,
                        onClick = onOpenProcedures
                    ),
                    RegistryService(
                        "opportunities",
                        "Opportunità",
                        "Bandi, borse e mobilità",
                        Icons.Outlined.Explore,
                        onClick = {}),
                    RegistryService(
                        "admissions",
                        "Ammissioni",
                        "Concorsi e graduatorie",
                        Icons.Outlined.HowToReg,
                        onClick = {}),
                ),
            ),
            scheme.secondaryContainer, scheme.onSecondaryContainer,
        ),
        Triple(
            RegistryServiceGroup(
                name = "Ateneo",
                caption = "Comunicazioni e vita universitaria",
                services = listOf(
                    RegistryService(
                        "news",
                        "Notizie",
                        "News ed eventi dall'ateneo",
                        Icons.Outlined.Newspaper,
                        external = true,
                        onClick = { openInAppBrowser("https://www.unimib.it/news") }),
                    RegistryService(
                        "elections",
                        "Elezioni studentesche",
                        "Rappresentanza",
                        Icons.Outlined.HowToVote,
                        external = true,
                        onClick = { openInAppBrowser("https://unimib-electors-prod.gea.esse3.cineca.it/app/select-event") }),
                ),
            ),
            scheme.primaryContainer, scheme.onPrimaryContainer,
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
        Box(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                sections.forEach { (group, container, onContainer) ->
                    RegistryServiceSection(
                        group = group,
                        accentContainer = container,
                        accentOnContainer = onContainer,
                    )
                }
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
