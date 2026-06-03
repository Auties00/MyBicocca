package it.attendance100.mybicocca

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator.ofFloat
import android.animation.ObjectAnimator.ofInt
import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.settings.SecuritySettingsStore
import it.attendance100.mybicocca.ui.navigation.AppRoot
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.LocalPipController
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.PipController
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.PipState
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.util.ProvideHapticManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

// FragmentActivity (a ComponentActivity subclass) is required by androidx.biometric's
// BiometricPrompt; Compose setContent, edge-to-edge, splash, PiP, and Hilt are unaffected.
@AndroidEntryPoint
class MyBicoccaActivity : FragmentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var securitySettingsStore: SecuritySettingsStore

    private val pipController = object : PipController {
        private var state: PipState? = null
        override fun setActive(state: PipState?) {
            this.state = state
        }

        override fun currentState(): PipState? = state
        override fun enterPipNow(): Boolean = tryEnterPip()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Animate the splash screen to transition to the app's content
        splashScreen.setOnExitAnimationListener { provider -> provider.animateExitAndRemove() }

        enableEdgeToEdge()

        // Mark the window secure (hide from recents/app-switcher + block screenshots) only while the
        // lock is active and the user opted in. Off by default, so screenshots stay allowed.
        lifecycleScope.launch {
            combine(
                securitySettingsStore.appLockEnabled,
                securitySettingsStore.secureScreenEnabled,
            ) { lockEnabled, secure -> lockEnabled && secure }
                .distinctUntilChanged()
                .collect { secure ->
                    if (secure) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
        }

        setContent {
            BicoccaTheme(dark = isSystemInDarkTheme()) {
                CompositionLocalProvider(LocalPipController provides pipController) {
                    ProvideHapticManager {
                        AppRoot()
                    }
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

// Animates the splash screen exit
private fun SplashScreenViewProvider.animateExitAndRemove() {
    val iconDuration = 700L   // icon scale + fade
    val chromeDuration = 500L // background fade + branding slide

    val animators = buildList {
        val splashIconEndScale = 10f
        add(
            ofFloat(
                iconView,
                View.SCALE_X,
                1f,
                splashIconEndScale
            ).lasting(iconDuration)
        ) // The icon zooms out
        add(ofFloat(iconView, View.SCALE_Y, 1f, splashIconEndScale).lasting(iconDuration))
        add(
            ofFloat(
                iconView,
                View.ALPHA,
                1f,
                0f
            ).lasting(iconDuration)
        )                   // The icon fades out
        add(
            ofInt(
                view.background,
                "alpha",
                255,
                0
            ).lasting(chromeDuration)
        )          // The background fades out
        addAll(brandingExitAnimators(chromeDuration))                                                          // The branding image slides down and fades out
    }

    AnimatorSet().apply {
        interpolator = AccelerateInterpolator()
        playTogether(animators)
        doOnEnd { remove() }
        start()
    }
}

@SuppressLint("DiscouragedApi")
private fun SplashScreenViewProvider.brandingExitAnimators(durationMs: Long): List<Animator> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return emptyList()

    val brandingId = view.resources.getIdentifier(
        "splashscreen_branding_view",
        "id",
        "android"
    ) // Identifier not available in api

    val brandingView =
        brandingId.takeIf { it != 0 }?.let { view.findViewById<View>(it) } ?: return emptyList()

    val slideDownwardDistance = brandingView.height.toFloat() + 100f // 100px downwards
    return listOf(
        ofFloat(brandingView, View.ALPHA, 1f, 0f).lasting(durationMs),
        ofFloat(brandingView, View.TRANSLATION_Y, 0f, slideDownwardDistance).lasting(durationMs),
    )
}

// Sets an animator's duration inline so groups of animators can run at different speeds within one AnimatorSet
private fun Animator.lasting(durationMs: Long): Animator = apply { duration = durationMs }
