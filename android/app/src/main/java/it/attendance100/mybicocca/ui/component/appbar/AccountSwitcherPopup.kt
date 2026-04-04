package it.attendance100.mybicocca.ui.component.appbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import it.attendance100.mybicocca.data.model.career.Career
import it.attendance100.mybicocca.data.model.user.User

/**
 * Progress-driven account switcher overlay, centered on screen.
 * [progress] drives every visual property: 0 = fully hidden, 1 = fully visible.
 * The profile avatar is hoisted out — a 72 dp placeholder reports its center
 * via [onAvatarTargetPositioned] so the parent can render a flying avatar.
 */
@Composable
fun AccountSwitcherPopup(
    progress: Float,
    user: User?,
    career: Career?,
    onAvatarTargetPositioned: (Offset) -> Unit,
    onDismiss: () -> Unit,
    onProfileClick: () -> Unit,
    onAddAccount: () -> Unit,
    onManageAccounts: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    if (progress <= 0f) return

    val contentAlpha = ((progress - 0.3f) / 0.7f).coerceIn(0f, 1f)

    // Scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f * progress))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onDismiss() },
    )

    // Card — centered
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val scale = 0.92f + 0.08f * progress
                    scaleX = scale
                    scaleY = scale
                    alpha = progress
                },
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = lerp(0.dp, 8.dp, progress),
        ) {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                // Header: close button + app title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Chiudi",
                        )
                    }
                    AppTitle()
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Profile card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onProfileClick() },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Avatar placeholder — reports center for flying avatar
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .onGloballyPositioned { coords ->
                                    val pos = coords.positionInRoot()
                                    val s = coords.size
                                    onAvatarTargetPositioned(
                                        Offset(
                                            pos.x + s.width / 2f,
                                            pos.y + s.height / 2f,
                                        ),
                                    )
                                },
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier.graphicsLayer { alpha = contentAlpha },
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = user?.fullName ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            if (career?.matricola != null) {
                                Text(
                                    text = career.matricola,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            if (user?.email != null) {
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                // Menu items — staggered fade
                Column(modifier = Modifier.graphicsLayer { alpha = contentAlpha }) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    AccountMenuItem(
                        icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                        label = "Aggiungi un altro account",
                        onClick = onAddAccount,
                    )

                    AccountMenuItem(
                        icon = { Icon(Icons.Outlined.ManageAccounts, contentDescription = null) },
                        label = "Gestisci gli account",
                        onClick = onManageAccounts,
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    AccountMenuItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = "Impostazioni",
                        onClick = onSettingsClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountMenuItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
