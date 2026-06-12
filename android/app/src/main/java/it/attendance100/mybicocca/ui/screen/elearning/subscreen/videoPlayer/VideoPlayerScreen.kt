package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.LocalPipController
import it.attendance100.mybicocca.core.os.PipState
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.component.CenterPlaybackControls
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.component.CustomPlayerControls
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.component.PlayerChrome
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.component.PlaylistSidePanel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.subscreen.qualityPicker.QualityPickerSheet
import it.attendance100.mybicocca.ui.screen.elearning.theme.LocalCourseAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.theme.ProvideCourseAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.theme.accentFor
import kotlinx.coroutines.delay

/**
 * Full-screen player for a course's Kaltura videos, forced into sensor landscape with the system
 * bars hidden for the whole stay (orientation and bar behaviour are restored on exit).
 *
 * The video renders on a black canvas; a tap toggles the chrome overlay, which auto-hides after a
 * few seconds of playback, and a double tap seeks ten seconds backwards/forwards depending on the
 * screen half. The chrome is a top gradient bar (back, title, PiP, quality, playlist), centred
 * skip-previous / play-pause / skip-next transport, and a bottom scrub bar tinted with the course
 * accent. A right-hand side panel lists the course playlist, and a bottom sheet picks the stream
 * quality. While composed it registers the active video with the app's PiP controller so the
 * system can shrink it to a picture-in-picture window.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    courseId: Int,
    cmId: Int,
    onBack: () -> Unit,
    viewModel: VideoPlayerViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    ProvideCourseAccentPalette {
        VideoPlayerScreenContent(
            courseId = courseId,
            cmId = cmId,
            onBack = onBack,
            viewModel = viewModel
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayerScreenContent(
    courseId: Int,
    cmId: Int,
    onBack: () -> Unit,
    viewModel: VideoPlayerViewModel,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val accentColor = LocalCourseAccentPalette.current.accentFor(CourseId(courseId))

    ForceLandscapeImmersive(activity)

    val player by viewModel.player.collectAsStateWithLifecycle()
    val stream by viewModel.currentStream.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val playlist by viewModel.playlist.collectAsStateWithLifecycle()
    val currentCmId by viewModel.currentCmId.collectAsStateWithLifecycle()

    var qualityOpen by remember { mutableStateOf(false) }
    var playlistOpen by remember { mutableStateOf(false) }
    var chromeVisible by remember { mutableStateOf(true) }

    val currentPlayer = player
    val isPlaying = rememberPlayerIsPlaying(currentPlayer)

    LaunchedEffect(chromeVisible, isPlaying, playlistOpen, qualityOpen) {
        if (chromeVisible && isPlaying && !playlistOpen && !qualityOpen) {
            delay(CHROME_HIDE_AFTER_MS)
            chromeVisible = false
        }
    }

    val pipController = LocalPipController.current
    val activeStream = stream.valueOrNull()
    LaunchedEffect(currentPlayer, activeStream, isPlaying) {
        if (currentPlayer == null || activeStream == null) {
            pipController.setActive(null)
            return@LaunchedEffect
        }
        pipController.setActive(
            PipState(
                aspectNumerator = 16,
                aspectDenominator = 9,
                isPlaying = isPlaying
            )
        )
    }
    DisposableEffect(Unit) {
        onDispose { pipController.setActive(null) }
    }

    val currentIdx = playlist.indexOfFirst { it.cmId == currentCmId }
    val currentItem = playlist.getOrNull(currentIdx)
    val previousCmId = playlist.getOrNull(currentIdx - 1)?.cmId
    val nextCmId = playlist.getOrNull(currentIdx + 1)?.cmId

    val playerBuffering = rememberPlayerIsBuffering(currentPlayer)
    val showBufferingIndicator = currentPlayer == null ||
            stream is Loadable.NotYetLoaded ||
            syncStatus is SyncStatus.Refreshing ||
            playerBuffering

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(currentPlayer) {
                detectTapGestures(
                    onTap = { chromeVisible = !chromeVisible },
                    onDoubleTap = { offset ->
                        val p = currentPlayer ?: return@detectTapGestures
                        val newPos = if (offset.x < size.width / 2f) {
                            (p.currentPosition - DOUBLE_TAP_SEEK_MS).coerceAtLeast(0L)
                        } else {
                            p.currentPosition + DOUBLE_TAP_SEEK_MS
                        }
                        p.seekTo(newPos)
                    },
                )
            },
    ) {
        VideoSurface(
            player = currentPlayer,
            syncStatus = syncStatus,
            showBufferingIndicator = showBufferingIndicator,
            onRetry = viewModel::retry,
        )

        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.matchParentSize(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                PlayerChrome(
                    title = currentItem?.title ?: stringResource(R.string.elearning_video_course_video),
                    subtitle = currentItem?.sectionName,
                    videoLoaded = activeStream != null,
                    onBack = onBack,
                    onOpenQuality = { qualityOpen = true },
                    onOpenPlaylist = { playlistOpen = true },
                    onEnterPip = { pipController.enterPipNow() },
                    modifier = Modifier.align(Alignment.TopCenter),
                )
                if (currentPlayer != null) {
                    if (!showBufferingIndicator) {
                        CenterPlaybackControls(
                            player = currentPlayer,
                            onPrevious = previousCmId?.let { id -> { viewModel.selectVideo(id) } },
                            onNext = nextCmId?.let { id -> { viewModel.selectVideo(id) } },
                            onUserActivity = { chromeVisible = true },
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    CustomPlayerControls(
                        player = currentPlayer,
                        accentColor = accentColor,
                        onUserActivity = { chromeVisible = true },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }

        PlaylistSidePanel(
            visible = playlistOpen,
            items = playlist,
            onSelect = { id ->
                viewModel.selectVideo(id)
                playlistOpen = false
            },
            onDismiss = { playlistOpen = false },
        )

        if (qualityOpen && activeStream != null) {
            QualityPickerSheet(
                variants = activeStream.variants,
                selected = null,
                onSelect = viewModel::selectQuality,
                onDismiss = { qualityOpen = false },
            )
        }
    }
}

/**
 * The video surface plus its overlay states: a black cover while the surface has no frame to
 * show, a centred white spinner while resolving or buffering, and an error message with a retry
 * button when the stream could not be loaded.
 */
@OptIn(UnstableApi::class)
@Composable
private fun VideoSurface(
    player: Player?,
    syncStatus: SyncStatus,
    showBufferingIndicator: Boolean,
    onRetry: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (player != null) {
            val presentation = rememberPresentationState(player)
            PlayerSurface(
                player = player,
                surfaceType = SURFACE_TYPE_SURFACE_VIEW,
                modifier = Modifier.resizeWithContentScale(
                    ContentScale.Fit,
                    presentation.videoSizeDp
                ),
            )
            if (presentation.coverSurface) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            }
        }

        when {
            syncStatus is SyncStatus.Failed -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.elearning_video_load_failed),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    RetryButton(onClick = onRetry)
                }
            }

            showBufferingIndicator -> {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            else -> Unit
        }
    }
}

/**
 * [Player.isPlaying] is not snapshot state, so it is bridged into Compose through a player
 * listener that keeps a local state in sync.
 */
@Composable
private fun rememberPlayerIsPlaying(player: Player?): Boolean {
    var isPlaying by remember(player) { mutableStateOf(player?.isPlaying == true) }
    DisposableEffect(player) {
        val p = player
        if (p == null) {
            onDispose {}
        } else {
            isPlaying = p.isPlaying
            val listener = object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
            }
            p.addListener(listener)
            onDispose { p.removeListener(listener) }
        }
    }
    return isPlaying
}

/** Listener-backed bridge of the player's buffering state into Compose snapshot state. */
@Composable
private fun rememberPlayerIsBuffering(player: Player?): Boolean {
    var buffering by remember(player) {
        mutableStateOf(player?.playbackState == Player.STATE_BUFFERING)
    }
    DisposableEffect(player) {
        val p = player
        if (p == null) {
            onDispose {}
        } else {
            buffering = p.playbackState == Player.STATE_BUFFERING
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    buffering = playbackState == Player.STATE_BUFFERING
                }
            }
            p.addListener(listener)
            onDispose { p.removeListener(listener) }
        }
    }
    return buffering
}

/**
 * Holds the activity in sensor landscape with the system bars hidden (revealable by swipe) while
 * in composition, restoring the previous orientation and bar behaviour on dispose.
 */
@Composable
private fun ForceLandscapeImmersive(activity: Activity?) {
    DisposableEffect(activity) {
        if (activity == null) {
            onDispose {}
        } else {
            val window = activity.window
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            val previousOrientation = activity.requestedOrientation
            val previousBehavior = controller.systemBarsBehavior
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
            onDispose {
                activity.requestedOrientation = previousOrientation
                controller.systemBarsBehavior = previousBehavior
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private const val CHROME_HIDE_AFTER_MS = 3_000L
private const val DOUBLE_TAP_SEEK_MS = 10_000L
