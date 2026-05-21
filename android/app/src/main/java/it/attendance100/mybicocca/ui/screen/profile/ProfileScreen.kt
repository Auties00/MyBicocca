package it.attendance100.mybicocca.ui.screen.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.ui.screen.profile.component.BadgeCard
import it.attendance100.mybicocca.ui.screen.profile.component.ProgressStatCard
import it.attendance100.mybicocca.ui.screen.profile.component.StatCard
import it.attendance100.mybicocca.ui.screen.profile.subscreen.hypotheticalGrade.HypotheticalGradeSheet

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onOpenTranscript: (Long) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    val career by viewModel.activeCareer.collectAsStateWithLifecycle()
    val photoFile by viewModel.photoFile.collectAsStateWithLifecycle()
    val statsLoadable by viewModel.stats.collectAsStateWithLifecycle()
    val rollupLoadable by viewModel.gradeRollup.collectAsStateWithLifecycle()

    val stats = statsLoadable.valueOrNull()
    val rollup = rollupLoadable.valueOrNull()

    var showCalculator by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BadgeCard(
            account = account,
            career = career,
            photoFile = photoFile,
        )

        SectionTitle("Statistiche")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Media Aritmetica",
                value = if (statsLoadable is Loadable.Loaded) stats?.arithmeticAverage?.let { "%.2f".format(it) } ?: "—" else null,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                title = "Media Ponderata",
                value = if (statsLoadable is Loadable.Loaded) stats?.weightedAverage?.let { "%.2f".format(it) } ?: "—" else null,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Esami Sostenuti",
                value = if (statsLoadable is Loadable.Loaded) stats?.passedExamCount?.toString() ?: "—" else null,
                modifier = Modifier.weight(1f),
            )
            ProgressStatCard(
                title = "CFU Acquisiti",
                current = stats?.passedCredits,
                total = stats?.totalCreditsRequired,
                modifier = Modifier.weight(1f),
            )
        }

        OutlinedButton(
            onClick = { showCalculator = true },
            enabled = rollup != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Calcola Media")
        }

        career?.let { c ->
            TranscriptTile(
                onClick = { onOpenTranscript(c.id.value) },
            )
        }
    }

    if (showCalculator) {
        HypotheticalGradeSheet(
            rollup = rollup,
            currentArithmetic = stats?.arithmeticAverage,
            currentWeighted = stats?.weightedAverage,
            onDismiss = { showCalculator = false },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun TranscriptTile(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Libretto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Esami sostenuti, in sospeso e grafico voti",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
