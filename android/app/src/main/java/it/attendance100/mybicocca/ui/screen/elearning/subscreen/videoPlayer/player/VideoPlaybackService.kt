package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player

import android.app.PendingIntent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.ExperimentalApi
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.notification.NotificationChannelId

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

        // media3 still builds the notification and binds its controls to the session; this only
        // moves it off the library's own "Now playing" channel onto the one in our registry.
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelId(NotificationChannelId.MEDIA_PLAYBACK.id)
                .setChannelName(R.string.notification_channel_media_playback_name)
                .build()
        )

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

        val session = MediaSession.Builder(this, player)
        // What a tap on the media notification opens. Without it the tap does nothing, which is
        // most noticeable in picture-in-picture: relaunching the activity is what expands it.
        packageManager.getLaunchIntentForPackage(packageName)?.let { launch ->
            session.setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    launch,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            )
        }
        mediaSession = session.build()
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
