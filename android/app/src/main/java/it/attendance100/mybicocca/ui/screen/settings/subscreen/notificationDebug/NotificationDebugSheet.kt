package it.attendance100.mybicocca.ui.screen.settings.subscreen.notificationDebug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.notification.NotificationChannelId
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet

/**
 * Debug-only screen that fires one of every notification shape on demand.
 *
 * Strings are hardcoded English on purpose: it never ships to a user, and putting throwaway copy
 * through the translation files would only make them harder to read.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationDebugSheet(
    onDismiss: () -> Unit,
    viewModel: NotificationDebugViewModel = hiltViewModel(),
) {
    val lastResult by viewModel.lastResult.collectAsStateWithLifecycle()

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        sizeDuration = 500,
    ) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Notifications debug",
                style = MaterialTheme.typography.titleLargeEmphasized,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Fires specs through AppNotifier. Progress is throttled to one post per " +
                        "second per slot, so rapid taps on the same progress button are dropped " +
                        "by design.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            NotificationChannelId.entries.forEach { channel ->
                Text(
                    text = "${channel.name}: ${if (viewModel.canNotify(channel)) "can notify" else "BLOCKED"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = "Live Update chip: ${if (viewModel.canPromoteOngoing()) "promotable" else "NOT promoted"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(onClick = viewModel::updateAvailable) { Text("Update available") }
                FilledTonalButton(onClick = viewModel::readyToInstall) { Text("Ready to install") }
                FilledTonalButton(onClick = { viewModel.progress(0) }) { Text("Progress 0%") }
                FilledTonalButton(onClick = { viewModel.progress(45) }) { Text("Progress 45%") }
                FilledTonalButton(onClick = { viewModel.progress(100) }) { Text("Progress 100%") }
                FilledTonalButton(onClick = viewModel::indeterminateProgress) { Text("Indeterminate") }
                FilledTonalButton(onClick = viewModel::grouped) { Text("Grouped pair") }
                OutlinedButton(onClick = viewModel::cancelProgress) { Text("Cancel progress") }
                OutlinedButton(onClick = viewModel::cancelAll) { Text("Cancel all") }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = lastResult,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
