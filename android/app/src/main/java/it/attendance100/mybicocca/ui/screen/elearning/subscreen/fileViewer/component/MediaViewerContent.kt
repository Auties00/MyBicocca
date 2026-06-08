package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton
import androidx.media3.ui.compose.material3.buttons.SeekBackButton
import androidx.media3.ui.compose.material3.buttons.SeekForwardButton
import androidx.media3.ui.compose.material3.indicator.PositionAndDurationText
import androidx.media3.ui.compose.material3.indicator.ProgressSlider
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.LocalPipController
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.PipState

// mp4/audio resources play straight off the tokenized pluginfile URL — the endpoint
// honors Range requests, so seeking works without downloading the file first. Local
// playback (zip-extracted media) goes through the same path with a file uri.
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun MediaViewerContent(
    mediaUri: String,
    isVideo: Boolean,
    fileName: String,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    LaunchedEffect(mediaUri) {
        player.setMediaItem(MediaItem.fromUri(mediaUri))
        player.prepare()
        player.playWhenReady = true
    }
    // Course material isn't background-listening content: pause when the screen leaves
    // the foreground, release with the composition.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) player.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }

    // Picture-in-Picture: publish play state + aspect so leaving the app (or the chrome's PiP
    // button) pops the video out, like the lesson player. In PiP the controls hide.
    val pipController = LocalPipController.current
    val inPip by pipController.isInPip
    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var aspectN by remember { mutableIntStateOf(16) }
    var aspectD by remember { mutableIntStateOf(9) }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    aspectN = videoSize.width
                    aspectD = videoSize.height
                }
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
    // Both video and audio register for auto-PiP-on-leave; audio just uses the default aspect.
    LaunchedEffect(isPlaying, aspectN, aspectD) {
        pipController.setActive(PipState(aspectN, aspectD, isPlaying))
    }
    DisposableEffect(Unit) {
        onDispose { pipController.setActive(null) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(if (isVideo) Color.Black else MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            if (isVideo) {
                val presentation = rememberPresentationState(player)
                PlayerSurface(
                    player = player,
                    surfaceType = SURFACE_TYPE_SURFACE_VIEW,
                    modifier = Modifier.resizeWithContentScale(ContentScale.Fit, presentation.videoSizeDp),
                )
                if (presentation.coverSurface) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(96.dp),
                    )
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp),
                    )
                }
            }
        }
        if (!inPip) {
            MediaControls(
                player = player,
                onPip = { pipController.enterPipNow() },
                onShare = onShare
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun MediaControls(player: ExoPlayer, onPip: () -> Unit, onShare: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        ProgressSlider(player = player, modifier = Modifier.fillMaxWidth())
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SeekBackButton(player = player)
            PlayPauseButton(player = player)
            SeekForwardButton(player = player)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                PositionAndDurationText(player = player)
            }
            IconButton(onClick = onPip) {
                Icon(Icons.Outlined.PictureInPictureAlt, contentDescription = "Picture-in-picture")
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Outlined.Share, contentDescription = "Condividi")
            }
        }
    }
}
