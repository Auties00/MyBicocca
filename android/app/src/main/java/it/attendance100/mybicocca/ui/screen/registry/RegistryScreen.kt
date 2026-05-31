package it.attendance100.mybicocca.ui.screen.registry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.ui.screen.registry.component.RegistryGridTile
import it.attendance100.mybicocca.ui.screen.registry.component.RegistryHeroLayout
import it.attendance100.mybicocca.ui.screen.registry.component.RegistryHeroTile
import it.attendance100.mybicocca.ui.screen.registry.component.RegistrySectionHeader
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryAccentShape
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryCategory
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryDashboardTile
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryTileStatus
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookableExams.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExams.BookedExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel

// Landing of the Registry (Segreterie) tab: a dashboard of cards that open the individual
// services as sub-pages. Esami and Tasse are surfaced as live summary hero tiles (booked
// count / tax status); every other service is a categorized grid tile.
@Composable
fun RegistryScreen(
    bookedExamsViewModel: BookedExamsViewModel,
    bookableExamsViewModel: BookableExamsViewModel,
    taxesViewModel: TaxesViewModel,
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

    val bookedCount = bookings.valueOrNull()?.size
    val availableCount = examCalls.valueOrNull()?.size
    val examSubtitle = when {
        bookedCount == null -> "Gestisci le iscrizioni agli appelli"
        availableCount != null && availableCount > 0 -> "$bookedCount prenotati · $availableCount disponibili"
        else -> "$bookedCount appelli prenotati"
    }

    val invoiceList = invoices.valueOrNull()
    val expiredCount = invoiceList?.count { it.status == TaxStatus.EXPIRED } ?: 0
    val pendingCount = invoiceList?.count { it.status == TaxStatus.PENDING } ?: 0
    val taxSubtitle = when {
        invoiceList == null -> "Visualizza e paga le tasse"
        expiredCount > 0 -> if (expiredCount == 1) "1 pagamento in ritardo" else "$expiredCount pagamenti in ritardo"
        pendingCount > 0 -> if (pendingCount == 1) "1 tassa da pagare" else "$pendingCount tasse da pagare"
        else -> "Tutto in regola"
    }
    val taxStatus = if (expiredCount > 0) RegistryTileStatus.Warning else RegistryTileStatus.Normal

    val examsHero = RegistryDashboardTile(
        id = "exams",
        title = "Appelli",
        subtitle = examSubtitle,
        icon = Icons.Outlined.EventAvailable,
        shape = RegistryAccentShape.Sunny,
        category = RegistryCategory.Exams,
        onClick = onOpenBookedExams,
        mirrored = false,
    )
    val taxesHero = RegistryDashboardTile(
        id = "taxes",
        title = "Tasse",
        subtitle = taxSubtitle,
        status = taxStatus,
        icon = Icons.Outlined.Euro,
        shape = RegistryAccentShape.Cookie6,
        category = RegistryCategory.Taxes,
        onClick = onOpenTaxes,
        mirrored = true,
    )

//    val examServices = listOf(
//        RegistryDashboardTile(
//            id = "exam_results",
//            title = "Bacheca Esiti",
//            subtitle = null,
//            icon = Icons.Outlined.Assessment,
//            shape = RegistryAccentShape.Burst,
//            category = RegistryCategory.Exams,
//            onClick = onOpenExamResults,
//        ),
//    )
    val teachingServices = listOf(
        RegistryDashboardTile(
            id = "study_plan",
            title = "Piano di Studi",
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            shape = RegistryAccentShape.Clover4,
            category = RegistryCategory.Teaching,
            onClick = onOpenStudyPlan,
        ),
        RegistryDashboardTile(
            id = "questionnaires",
            title = "Questionari",
            icon = Icons.AutoMirrored.Outlined.Assignment,
            shape = RegistryAccentShape.Flower,
            category = RegistryCategory.Teaching,
            onClick = onOpenQuestionnaires,
        ),
        RegistryDashboardTile(
            id = "degree_award",
            title = "Conseguimento Titolo",
            icon = Icons.Outlined.School,
            shape = RegistryAccentShape.Pentagon,
            category = RegistryCategory.Teaching,
            onClick = onOpenDegreeAward,
        ),
        RegistryDashboardTile(
            id = "internships",
            title = "Stage",
            icon = Icons.Outlined.WorkOutline,
            shape = RegistryAccentShape.Diamond,
            category = RegistryCategory.Teaching,
            onClick = onOpenInternships,
        ),
    )
    val documentServices = listOf(
        RegistryDashboardTile(
            id = "self_certificates",
            title = "Autocertificazioni",
            icon = Icons.Outlined.Description,
            shape = RegistryAccentShape.Cookie9,
            category = RegistryCategory.Documents,
            onClick = onOpenSelfCertificates,
        ),
    )
    val agendaServices = listOf(
        RegistryDashboardTile(
            id = "reservations",
            title = "Prenotazioni",
            icon = Icons.Outlined.EventSeat,
            shape = RegistryAccentShape.Cookie12,
            category = RegistryCategory.Agenda,
            onClick = onOpenReservations,
        ),
        RegistryDashboardTile(
            id = "attendance",
            title = "Presenze",
            icon = Icons.Outlined.HowToReg,
            shape = RegistryAccentShape.SoftBurst,
            category = RegistryCategory.Agenda,
            onClick = onOpenAttendance,
        ),
    )

    // hero = full-width live-summary card that heads the section; when present it stands
    // in for the inverted lead tile. invertLead applies the accent-on-card treatment to
    // the first grid tile of sections that have no hero.
    data class Section(
        val category: RegistryCategory,
        val hero: RegistryDashboardTile?,
        val tiles: List<RegistryDashboardTile>,
        val invertLead: Boolean,
    )

    val sections = listOf(
        Section(RegistryCategory.Exams, examsHero, emptyList(), invertLead = false),
        Section(RegistryCategory.Taxes, taxesHero, emptyList(), invertLead = false),
        Section(RegistryCategory.Teaching, null, teachingServices, invertLead = true),
        Section(RegistryCategory.Documents, null, documentServices, invertLead = true),
        Section(RegistryCategory.Agenda, null, agendaServices, invertLead = true),
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        sections.forEach { section ->
            item(span = { GridItemSpan(maxLineSpan) }, key = "header_${section.category}") {
                RegistrySectionHeader(
                    category = section.category,
                    modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
                )
            }
            if (section.hero != null) {
                item(span = { GridItemSpan(maxLineSpan) }, key = section.hero.id) {
                    RegistryHeroTile(
                        tile = section.hero,
                        layout = RegistryHeroLayout.Banner,
                        modifier = Modifier.height(150.dp),
                    )
                }
            }
            section.tiles.forEachIndexed { index, tile ->
                val singleItemWide = section.invertLead && section.tiles.size == 1 && index == 0
                item(
                    key = tile.id,
                    span = {
                        if (singleItemWide) {
                            GridItemSpan(maxLineSpan)
                        } else {
                            GridItemSpan(1)
                        }
                    },
                ) {
                    if (singleItemWide) {
                        RegistryGridTile(
                            tile = tile,
                            inverted = true,
                            wide = true,
                            modifier = Modifier.height(124.dp),
                        )
                    } else if (section.invertLead && index == 0) {
                        RegistryHeroTile(
                            tile = tile,
                            layout = RegistryHeroLayout.Portrait,
                            modifier = Modifier.height(200.dp),
                        )
                    } else RegistryGridTile(
                        tile = tile,
                        inverted = false,
                        modifier = Modifier.height(124.dp),
                    )
                }
            }
        }
    }
}
