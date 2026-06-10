package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.component

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberProgressStateWithTickCount

/**
 * The bottom scrub bar, sitting on a transparent-to-black gradient: a course-accent slider over
 * the full video duration with monospaced elapsed/total time labels underneath. The slider keeps
 * the default thumb but draws its own track to drop the end-of-track stop dot. Progress tracking
 * is event-driven (it pauses with the player rather than busy-polling), and dragging the thumb
 * seeks the video in real time so the picture follows the scrub. Releasing the thumb counts as
 * user activity for the chrome's auto-hide timer.
 */
@OptIn(UnstableApi::class)
@Composable
fun CustomPlayerControls(
    player: Player,
    accentColor: Color,
    onUserActivity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sliderProgress = rememberProgressStateWithTickCount(player, totalTickCount = SLIDER_TICK_COUNT)

    val sliderState = rememberSliderState(valueRange = 0f..1f)

    LaunchedEffect(sliderState) {
        snapshotFlow { sliderProgress.currentPositionProgress to sliderState.isDragging }
            .collect { (progress, dragging) ->
                if (!dragging) sliderState.value = progress
            }
    }

    LaunchedEffect(sliderState, sliderProgress) {
        snapshotFlow { sliderState.isDragging to sliderState.value }
            .collect { (dragging, value) ->
                if (dragging) sliderProgress.updateCurrentPositionProgress(value)
            }
    }

    SideEffect {
        sliderState.onValueChangeFinished = {
            sliderProgress.updateCurrentPositionProgress(sliderState.value)
            onUserActivity()
        }
    }

    val displayedFraction = if (sliderState.isDragging) sliderState.value else sliderProgress.currentPositionProgress
    val displayedPositionMs = sliderProgress.progressToPosition(displayedFraction)
    val durationMs = sliderProgress.progressToPosition(1f)

    val sliderColors = SliderDefaults.colors(
        thumbColor = accentColor,
        activeTrackColor = accentColor,
        activeTickColor = Color.White,
        inactiveTrackColor = Color.White.copy(alpha = 0.28f),
        inactiveTickColor = Color.White.copy(alpha = 0.6f),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.65f),
                    ),
                ),
            )
            .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Slider(
            state = sliderState,
            enabled = sliderProgress.changingProgressEnabled,
            colors = sliderColors,
            track = { state ->
                SliderDefaults.Track(
                    sliderState = state,
                    colors = sliderColors,
                    enabled = sliderProgress.changingProgressEnabled,
                    drawStopIndicator = null,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatTime(displayedPositionMs),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = formatTime(durationMs),
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

private const val SLIDER_TICK_COUNT = 1_000

private fun formatTime(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSec = ms / 1_000L
    val h = totalSec / 3_600L
    val m = (totalSec % 3_600L) / 60L
    val s = totalSec % 60L
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
