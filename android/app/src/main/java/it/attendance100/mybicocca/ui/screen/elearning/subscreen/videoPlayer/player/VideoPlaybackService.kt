package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.ExperimentalApi
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Foreground media-session service that owns the ExoPlayer for course videos. Hosting the player
 * here, rather than in the screen, is what lets audio keep playing when the app is backgrounded
 * and gives the system media notification its session (queue, prev/next, play/pause); the screen
 * talks to it through a [androidx.media3.session.MediaController].
 *
 * The player requests audio focus with movie-content media attributes, pauses when audio becomes
 * noisy (e.g. headphones unplugged), and enables dynamic scheduling for a more efficient core
 * playback loop. The session and player live for the service's lifetime and are released in
 * [onDestroy].
 */
@OptIn(UnstableApi::class)
class VideoPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    @OptIn(ExperimentalApi::class)
    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true,
            )
            .setHandleAudioBecomingNoisy(true)
            .experimentalSetDynamicSchedulingEnabled(true)
            .build()

        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
