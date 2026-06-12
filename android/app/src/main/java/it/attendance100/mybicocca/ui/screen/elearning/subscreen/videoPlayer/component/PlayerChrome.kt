package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R

/**
 * The player's top bar, laid over the video on a black-to-transparent gradient: back button,
 * video title with its section as subtitle, then picture-in-picture, quality and playlist
 * actions. PiP and quality stay disabled until a video is actually loaded; the playlist is
 * always reachable.
 */
@Composable
fun PlayerChrome(
    title: String,
    subtitle: String?,
    videoLoaded: Boolean,
    onBack: () -> Unit,
    onOpenQuality: () -> Unit,
    onOpenPlaylist: () -> Unit,
    onEnterPip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.65f),
                        Color.Transparent,
                    ),
                ),
            )
            .padding(top = 12.dp, bottom = 24.dp, start = 8.dp, end = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = Color.White,
                )
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            val disabledTint = Color.White.copy(alpha = 0.35f)
            IconButton(onClick = onEnterPip, enabled = videoLoaded) {
                Icon(
                    imageVector = Icons.Outlined.PictureInPictureAlt,
                    contentDescription = stringResource(R.string.elearning_video_picture_in_picture),
                    tint = if (videoLoaded) Color.White else disabledTint,
                )
            }
            IconButton(onClick = onOpenQuality, enabled = videoLoaded) {
                Icon(
                    imageVector = Icons.Outlined.HighQuality,
                    contentDescription = stringResource(R.string.elearning_video_quality),
                    tint = if (videoLoaded) Color.White else disabledTint,
                )
            }
            IconButton(onClick = onOpenPlaylist) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.PlaylistPlay,
                    contentDescription = stringResource(R.string.elearning_video_playlist),
                    tint = Color.White,
                )
            }
        }
    }
}
