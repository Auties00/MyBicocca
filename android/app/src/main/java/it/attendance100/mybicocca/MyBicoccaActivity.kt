package it.attendance100.mybicocca

import android.app.PictureInPictureParams
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntRect
import androidx.core.graphics.createBitmap
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.doOnPreDraw
import dagger.hilt.android.AndroidEntryPoint
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.ui.navigation.AppRoot
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.LocalPipController
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.PipController
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.PipState
import it.attendance100.mybicocca.ui.screen.splash.SplashIconSnapshot
import it.attendance100.mybicocca.ui.screen.splash.SplashRevealController
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import javax.inject.Inject

@AndroidEntryPoint
class MyBicoccaActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    private val revealController = SplashRevealController()

    private val pipController = object : PipController {
        private var state: PipState? = null
        override fun setActive(state: PipState?) { this.state = state }
        override fun currentState(): PipState? = state
        override fun enterPipNow(): Boolean = tryEnterPip()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Capture the platform-rendered icon and let Compose draw one frame with those exact pixels
        // before removing the native splash view.
        splashScreen.setOnExitAnimationListener { provider ->
            revealController.beginHandoff(provider.iconView.captureSplashIconSnapshot())
            window.decorView.post {
                window.decorView.doOnPreDraw {
                    provider.remove()
                    revealController.startReveal()
                }
                window.decorView.postInvalidateOnAnimation()
            }
        }

        setContent {
            BicoccaTheme(dark = isSystemInDarkTheme()) {
                CompositionLocalProvider(LocalPipController provides pipController) {
                    AppRoot(
                        reveal = revealController
                    )
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

    private fun View.captureSplashIconSnapshot(): SplashIconSnapshot? {
        if (width <= 0 || height <= 0) return null

        val location = IntArray(2)
        getLocationInWindow(location)
        return runCatching {
            val bitmap = createBitmap(width, height)
            draw(Canvas(bitmap))
            SplashIconSnapshot(
                image = bitmap.asImageBitmap(),
                boundsInWindow = IntRect(
                    left = location[0],
                    top = location[1],
                    right = location[0] + width,
                    bottom = location[1] + height,
                ),
                scaleX = scaleX.validScale(),
                scaleY = scaleY.validScale(),
                alpha = alpha.coerceIn(0f, 1f),
                pivotXFraction = (pivotX / width).coerceIn(0f, 1f),
                pivotYFraction = (pivotY / height).coerceIn(0f, 1f),
            )
        }.getOrNull()
    }

    private fun Float.validScale(): Float =
        if (isFinite() && this > 0f) this else 1f

}
