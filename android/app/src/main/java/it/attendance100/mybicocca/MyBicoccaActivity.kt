package it.attendance100.mybicocca

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import dagger.hilt.android.AndroidEntryPoint
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.ui.navigation.AppRoot
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.LocalPipController
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.PipController
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.PipState
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import javax.inject.Inject

@AndroidEntryPoint
class MyBicoccaActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    private val pipController = object : PipController {
        private var state: PipState? = null
        override fun setActive(state: PipState?) { this.state = state }
        override fun currentState(): PipState? = state
        override fun enterPipNow(): Boolean = tryEnterPip()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            BicoccaTheme(dark = isSystemInDarkTheme()) {
                CompositionLocalProvider(LocalPipController provides pipController) {
                    AppRoot()
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        tryEnterPip()
    }

    private fun tryEnterPip(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val state = pipController.currentState() ?: return false
        if (!state.isPlaying) return false
        return runCatching {
            val ratio = Rational(
                state.aspectNumerator.coerceAtLeast(1),
                state.aspectDenominator.coerceAtLeast(1),
            )
            enterPictureInPictureMode(
                PictureInPictureParams.Builder()
                    .setAspectRatio(ratio)
                    .build()
            )
        }.getOrDefault(false)
    }
}
