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
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalView
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import it.attendance100.mybicocca.core.os.LocalDeviceType
import it.attendance100.mybicocca.core.os.ProvideHapticManager
import it.attendance100.mybicocca.core.os.getDeviceType
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.settings.AppearanceSettingsStore
import it.attendance100.mybicocca.data.local.settings.SecuritySettingsStore
import it.attendance100.mybicocca.data.local.settings.ThemeMode
import it.attendance100.mybicocca.ui.navigation.AppRoot
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.LocalPipController
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.PipController
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.PipState
import it.attendance100.mybicocca.ui.theme.AppTheme
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

// AppCompatActivity for two reasons: it extends FragmentActivity (required by
// androidx.biometric's BiometricPrompt), and AppCompatDelegate.setApplicationLocales only
// applies in-app locales to AppCompat activities on pre-Android-13 — with a plain
// FragmentActivity the language switch there only took effect on the next launch.
// Compose setContent, edge-to-edge, splash, PiP, and Hilt are unaffected.
@AndroidEntryPoint
class MyBicoccaActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var securitySettingsStore: SecuritySettingsStore

    @Inject
    lateinit var appearanceSettingsStore: AppearanceSettingsStore

    private val pipController = object : PipController {
        private var state: PipState? = null
        val inPip = mutableStateOf(false)
        override fun setActive(state: PipState?) {
            this.state = state
        }

        override fun currentState(): PipState? = state
        // The button works for any file; auto-enter (onUserLeaveHint) needs playing media.
        override fun enterPipNow(): Boolean = tryEnterPip(requirePlaying = false)
        override val isInPip get() = inPip
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        pipController.inPip.value = isInPictureInPictureMode
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
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
            val themeMode by appearanceSettingsStore.themeMode.collectAsStateWithLifecycle(ThemeMode.System)
            val appTheme by appearanceSettingsStore.appTheme.collectAsStateWithLifecycle(AppTheme.Default)
            val dark = when (themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
            val view = LocalView.current
            LaunchedEffect(dark) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }


            val windowSizeClass = calculateWindowSizeClass(this)
            val deviceType = getDeviceType(windowSizeClass.widthSizeClass)

            BicoccaTheme(dark = dark, appTheme = appTheme) {
                CompositionLocalProvider(LocalDeviceType provides deviceType) {
                    CompositionLocalProvider(LocalPipController provides pipController) {
                        ProvideHapticManager {
                            AppRoot()
                        }
                    }
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Auto-enter only for playing media (video/audio), never for static files.
        tryEnterPip(requirePlaying = true)
    }

    private fun tryEnterPip(requirePlaying: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false

        val state = pipController.currentState()
        if (requirePlaying && state?.isPlaying != true) return false

        return runCatching {
            // Use the content aspect when known (media); a portrait default otherwise so a
            // document/image still pops out at a sensible size.
            val ratio = if (state != null) {
                Rational(
                    state.aspectNumerator.coerceAtLeast(1),
                    state.aspectDenominator.coerceAtLeast(1),
                )
            } else {
                Rational(2, 3)
            }
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
