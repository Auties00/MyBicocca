package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.component.button.PrimaryActionButton
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage

// The outcome of a completed in-sheet action, rendered as a dedicated result page instead of a
// transient snackbar — the same cookie-icon empty-state visual as SheetMessage, with a pinned
// action group below: a brand primary (Fine / Riprova) and, for a retriable error, a tonal
// secondary (Chiudi).
sealed interface SheetOutcome {
    val title: String

    data class Success(override val title: String, val body: String? = null) : SheetOutcome
    data class Info(override val title: String, val body: String? = null) : SheetOutcome
    data class Error(override val title: String, val cause: Throwable? = null, val body: String? = null) : SheetOutcome
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SheetResultPage(
    outcome: SheetOutcome,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    // White is explicit per the brand-red rule: onPrimary flips to black-on-red in dark mode.
    val brandFg = if (dark) scheme.onPrimaryContainer else Color.White

    val icon: ImageVector
    val container: Color
    val onContainer: Color
    when (outcome) {
        is SheetOutcome.Success -> {
            icon = Icons.Rounded.CheckCircle
            container = scheme.tertiaryContainer
            onContainer = scheme.onTertiaryContainer
        }
        is SheetOutcome.Info -> {
            icon = Icons.Outlined.Info
            container = scheme.secondaryContainer
            onContainer = scheme.onSecondaryContainer
        }
        is SheetOutcome.Error -> {
            icon = Icons.Outlined.ErrorOutline
            container = scheme.errorContainer
            onContainer = scheme.onErrorContainer
        }
    }
    val body = when (outcome) {
        is SheetOutcome.Success -> outcome.body
        is SheetOutcome.Info -> outcome.body
        is SheetOutcome.Error -> outcome.body ?: outcome.cause?.friendlyMessage()
    }
    val showRetry = outcome is SheetOutcome.Error && onRetry != null

    Column(modifier = modifier.fillMaxWidth()) {
        SheetMessage(
            icon = icon,
            title = outcome.title,
            body = body,
            container = container,
            onContainer = onContainer,
        )

        if (showRetry) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FilledTonalButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = scheme.surfaceContainerHighest,
                        contentColor = scheme.onSurface,
                    ),
                ) {
                    Text("Chiudi", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { onRetry?.invoke() },
                    modifier = Modifier
                        .weight(1.4f)
                        .height(56.dp),
                    shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = brandBg, contentColor = brandFg),
                ) {
                    Text("Riprova", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            PrimaryActionButton(
                text = if (outcome is SheetOutcome.Error) "Chiudi" else "Fine",
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            )
        }
    }
}
