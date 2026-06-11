package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.button.PrimaryActionButton
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage

/**
 * Terminal result page for an in-sheet action: the same cookie-icon empty-state visual as
 * SheetMessage, tinted by the outcome's severity, with a pinned action group below — a brand
 * primary ("Fine", or "Riprova" for a retriable error) and, when retry is offered, a tonal
 * "Chiudi" as the connected leading half of the pair. An Error without a body falls back to
 * the friendly message derived from its cause.
 *
 * Brand-red fills keep explicit white content in the light scheme (onPrimary resolves to
 * black-on-red in the dark scheme), and the retry button holds the brand fill while disabled
 * mid-retry instead of dimming under its spinner.
 *
 * @param retryInProgress while true the "Riprova" button shows a spinner and both actions
 * disable — the caller is performing (or deliberately holding a loading beat over) the retry.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SheetResultPage(
    outcome: SheetOutcome,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    retryInProgress: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
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
                    enabled = !retryInProgress,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = scheme.surfaceContainerHighest,
                        contentColor = scheme.onSurface,
                    ),
                ) {
                    Text(stringResource(R.string.common_close), fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { onRetry.invoke() },
                    enabled = !retryInProgress,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(56.dp),
                    shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = brandBg,
                        contentColor = brandFg,
                        disabledContainerColor = brandBg,
                        disabledContentColor = brandFg,
                    ),
                ) {
                    if (retryInProgress) {
                        LoadingIndicator(modifier = Modifier.size(24.dp), color = brandFg)
                    } else {
                        Text(
                            stringResource(R.string.common_retry),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            PrimaryActionButton(
                text = if (outcome is SheetOutcome.Error) stringResource(R.string.common_close) else stringResource(
                    R.string.common_done
                ),
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            )
        }
    }
}
