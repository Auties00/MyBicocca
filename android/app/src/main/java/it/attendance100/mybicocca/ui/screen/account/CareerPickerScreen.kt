package it.attendance100.mybicocca.ui.screen.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.isSelectable

@Composable
fun CareerPickerScreen(
    account: Account,
    onPicked: (CareerId) -> Unit,
) {
    val selectable = account.academic.careers.filter { it.status.isSelectable }
    val history = account.academic.careers.filterNot { it.status.isSelectable }

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
        ) {
            item {
                Text("Choose a career", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = "Hi ${account.displayName}. You have multiple programs — pick the one to use as your default.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
            }

            if (selectable.isNotEmpty()) {
                item {
                    SectionLabel("Active")
                }
                items(selectable, key = { it.id.value }) { career ->
                    CareerCard(career = career, onClick = { onPicked(career.id) })
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (history.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    SectionLabel("History")
                }
                items(history, key = { it.id.value }) { career ->
                    CareerCard(career = career, onClick = null)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
internal fun CareerCard(
    career: Career,
    onClick: (() -> Unit)?,
    selected: Boolean = false,
) {
    val container = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        onClick == null -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = career.description.ifEmpty { "Career #${career.id.value}" },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Matricola ${career.matricola} · AY ${career.academicYear}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = career.status.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
