package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.VideoPlaybackService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service binding and string resolution helper for video playback, encapsulating Android [Context]
 * usage so [VideoPlayerViewModel] does not hold framework Context references.
 */
@Singleton
class MediaControllerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun createControllerFuture(): ListenableFuture<MediaController> {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, VideoPlaybackService::class.java),
        )
        return MediaController.Builder(context, sessionToken).buildAsync()
    }

    fun getString(resId: Int): String {
        return context.getString(resId)
    }

    fun getString(resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}
