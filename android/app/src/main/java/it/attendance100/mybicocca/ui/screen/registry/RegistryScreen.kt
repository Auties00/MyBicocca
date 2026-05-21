package it.attendance100.mybicocca.ui.screen.registry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.material.icons.outlined.Grading
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.screen.registry.component.RegistryGridTile
import it.attendance100.mybicocca.ui.screen.registry.component.RegistryHeroLayout
import it.attendance100.mybicocca.ui.screen.registry.component.RegistryHeroTile
import it.attendance100.mybicocca.ui.screen.registry.component.RegistrySectionHeader
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryAccentShape
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryCategory
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryDashboardTile

@Composable
fun RegistryScreen(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
    onNavigateToBooking: () -> Unit = {},
    onNavigateToBooked: () -> Unit = {},
    onNavigateToTaxes: () -> Unit = {},
    onNavigateToIsee: () -> Unit = {},
    onNavigateToSelfCertificates: () -> Unit = {},
    onNavigateToExamResults: () -> Unit = {},
    onNavigateToStudyPlan: () -> Unit = {},
    onNavigateToQuestionnaires: () -> Unit = {},
    onNavigateToReservations: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToInternships: () -> Unit = {},
) {
    LaunchedEffect(Unit) { onProvideFilterToggle(null) }

    val tiles = remember(
        onNavigateToBooking,
        onNavigateToBooked,
        onNavigateToTaxes,
        onNavigateToIsee,
        onNavigateToSelfCertificates,
        onNavigateToExamResults,
        onNavigateToStudyPlan,
        onNavigateToQuestionnaires,
        onNavigateToReservations,
        onNavigateToAttendance,
        onNavigateToInternships,
    ) {
        listOf(
            RegistryDashboardTile(
                id = "booking",
                title = "Prenotazione Esami",
                icon = Icons.Filled.Event,
                shape = RegistryAccentShape.Sunny,
                category = RegistryCategory.Exams,
                onClick = onNavigateToBooking,
            ),
            RegistryDashboardTile(
                id = "booked",
                title = "Esami Prenotati",
                icon = Icons.Filled.EventAvailable,
                shape = RegistryAccentShape.Cookie9,
                category = RegistryCategory.Exams,
                onClick = onNavigateToBooked,
            ),
            RegistryDashboardTile(
                id = "exam_results",
                title = "Bacheca Esiti",
                icon = Icons.Outlined.Grading,
                shape = RegistryAccentShape.Burst,
                category = RegistryCategory.Exams,
                onClick = onNavigateToExamResults,
            ),
            RegistryDashboardTile(
                id = "study_plan",
                title = "Piano di Studi",
                icon = Icons.Filled.Book,
                shape = RegistryAccentShape.Clover4,
                category = RegistryCategory.Teaching,
                onClick = onNavigateToStudyPlan,
            ),
            RegistryDashboardTile(
                id = "questionnaires",
                title = "Questionari",
                icon = Icons.Outlined.RateReview,
                shape = RegistryAccentShape.Pentagon,
                category = RegistryCategory.Teaching,
                onClick = onNavigateToQuestionnaires,
            ),
            RegistryDashboardTile(
                id = "taxes",
                title = "Tasse",
                icon = Icons.Outlined.Euro,
                shape = RegistryAccentShape.Cookie6,
                category = RegistryCategory.Administration,
                onClick = onNavigateToTaxes,
            ),
            RegistryDashboardTile(
                id = "isee",
                title = "ISEE",
                icon = Icons.Outlined.AccountBalance,
                shape = RegistryAccentShape.Diamond,
                category = RegistryCategory.Administration,
                onClick = onNavigateToIsee,
            ),
            RegistryDashboardTile(
                id = "self_certificates",
                title = "Autocertificazioni",
                icon = Icons.Outlined.Description,
                shape = RegistryAccentShape.Pill,
                category = RegistryCategory.Administration,
                onClick = onNavigateToSelfCertificates,
            ),
            RegistryDashboardTile(
                id = "reservations",
                title = "Prenotazioni",
                icon = Icons.Filled.AutoStories,
                shape = RegistryAccentShape.Cookie12,
                category = RegistryCategory.Agenda,
                onClick = onNavigateToReservations,
            ),
            RegistryDashboardTile(
                id = "attendance",
                title = "Presenze",
                icon = Icons.Outlined.HowToReg,
                shape = RegistryAccentShape.SoftBurst,
                category = RegistryCategory.Agenda,
                onClick = onNavigateToAttendance,
            ),
            RegistryDashboardTile(
                id = "internships",
                title = "Stage",
                icon = Icons.Filled.WorkHistory,
                shape = RegistryAccentShape.Flower,
                category = RegistryCategory.Internships,
                onClick = onNavigateToInternships,
            ),
        )
    }

    val sections = remember(tiles) {
        RegistryCategory.entries
            .filter { it != RegistryCategory.All }
            .mapNotNull { category ->
                val items = tiles.filter { it.category == category }
                if (items.isEmpty()) null else category to items
            }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp),
    ) {
        sections.forEach { (category, items) ->
            item(key = "section_${category.name}") {
                RegistrySection(category = category, items = items)
            }
        }
    }
}

@Composable
private fun RegistrySection(
    category: RegistryCategory,
    items: List<RegistryDashboardTile>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RegistrySectionHeader(category = category)
        when (items.size) {
            0 -> Unit
            1 -> RegistryHeroTile(
                tile = items[0],
                layout = RegistryHeroLayout.Banner,
                modifier = Modifier.fillMaxWidth().height(150.dp),
            )
            else -> BentoRow(items = items)
        }
    }
}

@Composable
private fun BentoRow(items: List<RegistryDashboardTile>) {
    val big = items.first()
    val smalls = items.drop(1).take(2)
    Row(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RegistryHeroTile(
            tile = big,
            layout = RegistryHeroLayout.Portrait,
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight(),
        )
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            smalls.forEach { tile ->
                RegistryGridTile(
                    tile = tile,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
        }
    }
}
