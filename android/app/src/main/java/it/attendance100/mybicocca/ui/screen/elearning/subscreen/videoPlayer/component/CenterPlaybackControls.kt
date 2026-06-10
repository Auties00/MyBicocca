package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.component

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState

/**
 * The transport cluster floating at the centre of the video: a large play/pause toggle flanked by
 * smaller skip-previous / skip-next buttons, each drawn as a translucent black circle with a white
 * icon. Skip buttons grey out when there is no adjacent playlist video, and every press reports
 * user activity so the chrome's auto-hide timer restarts.
 */
@OptIn(UnstableApi::class)
@Composable
fun CenterPlaybackControls(
    player: Player,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    onUserActivity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playPauseState = rememberPlayPauseButtonState(player)
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
    ) {
        CircularControl(
            icon = Icons.Outlined.SkipPrevious,
            contentDescription = "Video precedente",
            enabled = onPrevious != null,
            size = SIDE_SIZE,
            iconSize = SIDE_ICON,
            onClick = {
                onPrevious?.invoke()
                onUserActivity()
            },
        )
        CircularControl(
            icon = if (playPauseState.showPlay) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
            contentDescription = if (playPauseState.showPlay) "Play" else "Pausa",
            enabled = playPauseState.isEnabled,
            size = CENTER_SIZE,
            iconSize = CENTER_ICON,
            onClick = {
                playPauseState.onClick()
                onUserActivity()
            },
        )
        CircularControl(
            icon = Icons.Outlined.SkipNext,
            contentDescription = "Video successivo",
            enabled = onNext != null,
            size = SIDE_SIZE,
            iconSize = SIDE_ICON,
            onClick = {
                onNext?.invoke()
                onUserActivity()
            },
        )
    }
}

@Composable
private fun CircularControl(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    val bg = if (enabled) Color.Black.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.3f)
    val tint = if (enabled) Color.White else Color.White.copy(alpha = 0.4f)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

private val CENTER_SIZE = 68.dp
private val CENTER_ICON = 40.dp
private val SIDE_SIZE = 56.dp
private val SIDE_ICON = 32.dp
