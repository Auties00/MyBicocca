package it.attendance100.mybicocca

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator.ofFloat
import android.animation.ObjectAnimator.ofInt
import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.Intent
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
import it.attendance100.mybicocca.core.os.LocalPipController
import it.attendance100.mybicocca.core.os.PipController
import it.attendance100.mybicocca.core.os.PipState
import it.attendance100.mybicocca.core.os.ProvideHapticManager
import it.attendance100.mybicocca.core.os.getDeviceType
import it.attendance100.mybicocca.data.update.UpdateChecker
import it.attendance100.mybicocca.domain.model.library.LibraryActionKind
import it.attendance100.mybicocca.domain.model.library.LibraryDeepLinkAction
import it.attendance100.mybicocca.domain.model.settings.AppTheme
import it.attendance100.mybicocca.domain.model.settings.ThemeMode
import it.attendance100.mybicocca.domain.usecase.attendance.SubmitPresenceScanUseCase
import it.attendance100.mybicocca.domain.usecase.library.SubmitLibraryActionUseCase
import it.attendance100.mybicocca.domain.usecase.security.ObserveAppLockEnabledUseCase
import it.attendance100.mybicocca.domain.usecase.security.ObserveSecureScreenUseCase
import it.attendance100.mybicocca.domain.usecase.settings.ObserveAppThemeUseCase
import it.attendance100.mybicocca.domain.usecase.settings.ObserveThemeModeUseCase
import it.attendance100.mybicocca.ui.navigation.AppRoot
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.LocalAppTheme
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The single activity hosting the whole Compose app behind [AppRoot].
 *
 * It owns the pieces that must live at the window level: resolving the appearance preferences
 * into the active theme, flagging the window secure while the app lock is enabled and the user
 * opted in, animating the OS splash screen away, picture-in-picture for media playback, and
 * translating externally-opened deep links (mod_attendance QR scans, Affluences reservation
 * email links) into their domain hand-offs.
 *
 * It extends [AppCompatActivity] for two reasons: androidx.biometric's BiometricPrompt requires
 * a FragmentActivity, and AppCompatDelegate.setApplicationLocales only applies in-app locales to
 * AppCompat activities on pre-Android-13 — with a plain FragmentActivity the language switch
 * there only took effect on the next launch.
 */
@AndroidEntryPoint
class MyBicoccaActivity : AppCompatActivity() {

    @Inject
    lateinit var observeThemeMode: ObserveThemeModeUseCase

    @Inject
    lateinit var observeAppTheme: ObserveAppThemeUseCase

    @Inject
    lateinit var observeAppLockEnabled: ObserveAppLockEnabledUseCase

    @Inject
    lateinit var observeSecureScreen: ObserveSecureScreenUseCase

    @Inject
    lateinit var submitPresenceScan: SubmitPresenceScanUseCase

    @Inject
    lateinit var submitLibraryAction: SubmitLibraryActionUseCase

    // Drives the once-a-day update check; started in onCreate.
    @Inject
    lateinit var updateChecker: UpdateChecker

    private val pipController = object : PipController {
        private var state: PipState? = null
        val inPip = mutableStateOf(false)
        override fun setActive(state: PipState?) {
            this.state = state
        }

        override fun currentState(): PipState? = state

        /** The explicit PiP button works for any file; auto-enter (onUserLeaveHint) needs playing media. */
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

        captureAttendanceDeepLink(intent)
        captureLibraryDeepLink(intent)

        updateChecker.start()

        splashScreen.setOnExitAnimationListener { provider -> provider.animateExitAndRemove() }

        enableEdgeToEdge()

        lifecycleScope.launch {
            combine(
                observeAppLockEnabled(),
                observeSecureScreen(),
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
            val themeMode by observeThemeMode().collectAsStateWithLifecycle(ThemeMode.System)
            val appTheme by observeAppTheme().collectAsStateWithLifecycle(AppTheme.Default)
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
                CompositionLocalProvider(
                    LocalDeviceType provides deviceType,
                    LocalAppTheme provides appTheme,
                    LocalDarkTheme provides dark,
                ) {
                    CompositionLocalProvider(LocalPipController provides pipController) {
                        ProvideHapticManager {
                            AppRoot()
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        captureAttendanceDeepLink(intent)
        captureLibraryDeepLink(intent)
    }

    /**
     * A mod_attendance QR opened from outside resolves to this activity; the raw link is handed
     * to the Presenze flow, which runs the marking flow when its screen comes up.
     */
    private fun captureAttendanceDeepLink(intent: Intent) {
        if (intent.action != Intent.ACTION_VIEW) return
        val uri = intent.data ?: return
        if (uri.host == "elearning.unimib.it" && uri.path?.contains("mod/attendance/attendance.php") == true) {
            submitPresenceScan(uri.toString())
        }
    }

    /**
     * An Affluences reservation cancellation email link opened from outside resolves here; the
     * reservationToken is parsed out and handed to the Biblioteca flow.
     */
    private fun captureLibraryDeepLink(intent: Intent) {
        if (intent.action != Intent.ACTION_VIEW) return
        val uri = intent.data ?: return
        if (uri.host?.contains("affluences.com") != true) return
        val token = uri.getQueryParameter("reservationToken") ?: return
        if (!uri.path.orEmpty().contains("/reservation/cancel")) return
        submitLibraryAction(LibraryDeepLinkAction(LibraryActionKind.Cancel, token))
    }

    /** Auto-enters picture-in-picture only for playing media (video/audio), never for static files. */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        tryEnterPip(requirePlaying = true)
    }

    /**
     * Enters picture-in-picture using the content aspect ratio when known (media); a portrait
     * default otherwise, so a document or image still pops out at a sensible size.
     */
    private fun tryEnterPip(requirePlaying: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false

        val state = pipController.currentState()
        if (requirePlaying && state?.isPlaying != true) return false

        return runCatching {
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

/**
 * Animates the OS splash screen away: the icon zooms out and fades while the background and
 * branding image fade on a faster track, then the splash view is removed.
 */
private fun SplashScreenViewProvider.animateExitAndRemove() {
    val iconDuration = 700L
    val chromeDuration = 500L

    val animators = buildList {
        val splashIconEndScale = 10f
        add(ofFloat(iconView, View.SCALE_X, 1f, splashIconEndScale).lasting(iconDuration))
        add(ofFloat(iconView, View.SCALE_Y, 1f, splashIconEndScale).lasting(iconDuration))
        add(ofFloat(iconView, View.ALPHA, 1f, 0f).lasting(iconDuration))
        add(ofInt(view.background, "alpha", 255, 0).lasting(chromeDuration))
        addAll(brandingExitAnimators(chromeDuration))
    }

    AnimatorSet().apply {
        interpolator = AccelerateInterpolator()
        playTogether(animators)
        doOnEnd { remove() }
        start()
    }
}

/**
 * Slide-down + fade animators for the splash branding image. The branding view has no public id,
 * so it is looked up by the OS resource name; on devices where it cannot be resolved (or before
 * Android 12, where there is no branding view) no animators are produced.
 */
@SuppressLint("DiscouragedApi")
private fun SplashScreenViewProvider.brandingExitAnimators(durationMs: Long): List<Animator> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return emptyList()

    val brandingId = view.resources.getIdentifier(
        "splashscreen_branding_view",
        "id",
        "android"
    )

    val brandingView =
        brandingId.takeIf { it != 0 }?.let { view.findViewById<View>(it) } ?: return emptyList()

    val slideDownwardDistance = brandingView.height.toFloat() + 100f
    return listOf(
        ofFloat(brandingView, View.ALPHA, 1f, 0f).lasting(durationMs),
        ofFloat(brandingView, View.TRANSLATION_Y, 0f, slideDownwardDistance).lasting(durationMs),
    )
}

/** Sets an animator's duration inline so groups of animators can run at different speeds within one AnimatorSet. */
private fun Animator.lasting(durationMs: Long): Animator = apply { duration = durationMs }
