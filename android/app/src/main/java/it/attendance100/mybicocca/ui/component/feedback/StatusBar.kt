package it.attendance100.mybicocca.ui.component.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Inline banner that surfaces connectivity loss or a dismissible sync error without
// nuking stale content underneath. Offline takes precedence over error.
@Composable
fun StatusBar(
    isOnline: Boolean,
    errorMessage: String?,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showOffline = !isOnline
    val showError = isOnline && errorMessage != null

    AnimatedVisibility(
        visible = showOffline || showError,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier,
    ) {
        val backgroundColor = if (showOffline) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.errorContainer
        }
        val contentColor = if (showOffline) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onErrorContainer
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = backgroundColor,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = if (showOffline) Icons.Outlined.WifiOff else Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor,
                )
                Text(
                    text = if (showOffline) "Sei offline" else errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor,
                    modifier = Modifier.weight(1f),
                )
                if (showError) {
                    IconButton(
                        onClick = onDismissError,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Chiudi",
                            modifier = Modifier.size(16.dp),
                            tint = contentColor,
                        )
                    }
                }
            }
        }
    }
}
