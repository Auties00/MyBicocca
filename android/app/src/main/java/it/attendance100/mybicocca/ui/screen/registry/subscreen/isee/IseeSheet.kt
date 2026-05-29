package it.attendance100.mybicocca.ui.screen.registry.subscreen.isee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.tax.IseeDeclaration
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.formatEuro
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.taxFriendlyMessage

@Composable
fun IseeSheet(
    viewModel: TaxesViewModel,
    onDismiss: () -> Unit,
) {
    val iseeState by viewModel.isee.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sizeDuration = 500,
    ) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp)
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "ISEE",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))

            when (val snapshot = iseeState) {
                Loadable.NotYetLoaded -> when (val status = syncStatus) {
                    is SyncStatus.Failed -> StatusBlock(
                        message = status.cause.taxFriendlyMessage(),
                        action = { FilledTonalButton(onClick = viewModel::refresh) { Text("Riprova") } },
                    )
                    else -> StatusBlock { CircularProgressIndicator() }
                }

                is Loadable.Loaded -> {
                    val declarations = snapshot.value
                        .filter { it.isee != null }
                        .sortedByDescending { it.academicYearEnrollmentId ?: 0L }
                    if (declarations.isEmpty()) {
                        StatusBlock(message = "Non risultano dichiarazioni ISEE per la tua carriera.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            declarations.forEach { IseeCard(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBlock(
    message: String? = null,
    action: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (content != null) {
            content()
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (action != null) {
                    Spacer(Modifier.height(16.dp))
                    action()
                }
            }
        }
    }
}

@Composable
private fun IseeCard(declaration: IseeDeclaration) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            declaration.academicYearEnrollmentId?.let { year ->
                Text(
                    text = "Anno accademico $year/${(year + 1) % 100}",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = declaration.isee?.let { formatEuro(it) } ?: "—",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            declaration.iseeThreshold?.let { DetailRow("Soglia agevolazioni", formatEuro(it)) }
            declaration.courseDescription?.let { DetailRow("Corso", it) }
            declaration.exemptionDescription?.takeIf { it.isNotBlank() }?.let { DetailRow("Esonero", it) }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
