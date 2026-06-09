package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class ViewerAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

// Adaptive bottom action bar used by all file viewers. The layout scales with action count:
//   1 action  → large extended FAB with label, centered
//   2 actions → full-width connected button pair (secondary=tonal leading, primary=filled trailing)
//   3+ actions → pill of icon buttons on the left + large square FAB on the right
//
// [primary] is the dominant action (Download / Save to gallery / Open With).
// [secondary] are supporting actions (Share, theme toggle, PiP, …).
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxScope.ViewerBottomBar(
    primary: ViewerAction,
    vararg secondary: ViewerAction,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .align(Alignment.BottomCenter)
    ) {
        when (secondary.size) {
            0 -> ExtendedFloatingActionButton(
                onClick = primary.onClick,
                icon = { Icon(primary.icon, contentDescription = null) },
                text = { Text(primary.label) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            1 -> Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FilledTonalButton(
                    onClick = secondary[0].onClick,
                    enabled = secondary[0].enabled,
                    shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                ) {
                    Icon(
                        secondary[0].icon,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(secondary[0].label)
                }
                Button(
                    onClick = primary.onClick,
                    enabled = primary.enabled,
                    shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                ) {
                    Icon(
                        primary.icon,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(primary.label)
                }
            }

            else -> Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Row(
                        modifier = Modifier
                            .padding(4.dp)
                            .padding(horizontal = 4.dp)
                    ) {
                        secondary.forEach { action ->
                            IconButton(onClick = action.onClick, enabled = action.enabled) {
                                Icon(action.icon, contentDescription = action.label)
                            }
                        }
                    }
                }
                FloatingActionButton(
                    onClick = primary.onClick,
                    modifier = Modifier.size(65.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(primary.icon, contentDescription = primary.label)
                }
            }
        }
    }
}
