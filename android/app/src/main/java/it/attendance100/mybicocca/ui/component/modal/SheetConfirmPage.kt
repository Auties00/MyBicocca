package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * In-sheet destructive-confirm page, used where a popup AlertDialog would otherwise appear:
 * body copy over the connected expressive action pair. The emphasized action is always the
 * brand primary trailing on the right; by default that's the safe way out ([keepLabel]), with
 * [confirmIsPrimary] the committing action takes the slot instead and the safe way out becomes
 * the neutral tonal leading left.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SheetConfirmPage(
    body: String,
    onConfirm: () -> Unit,
    onKeep: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = "Conferma",
    keepLabel: String = "Annulla",
    confirmIsPrimary: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary
    val (secondaryLabel, onSecondary) = if (confirmIsPrimary) keepLabel to onKeep else confirmLabel to onConfirm
    val (primaryLabel, onPrimary) = if (confirmIsPrimary) confirmLabel to onConfirm else keepLabel to onKeep

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FilledTonalButton(
                onClick = onSecondary,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHighest,
                    contentColor = scheme.onSurface,
                ),
            ) {
                Text(secondaryLabel, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onPrimary,
                modifier = Modifier
                    .weight(1.4f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = brandBg, contentColor = brandFg),
            ) {
                Text(primaryLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
