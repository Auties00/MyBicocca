package it.attendance100.mybicocca.core.os

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import kotlin.math.roundToInt

/**
 * App-wide haptic feedback entry point, reached from composables through [LocalHapticManager].
 *
 * The simple effects ([tap], [longPress], [keyboardTap]) route through the view's haptic
 * feedback constants, so they respect the system haptics setting. The richer effects compose
 * vibration primitives when the device supports them and degrade to plain waveforms or one-shot
 * buzzes otherwise; every effect is a no-op on devices without a vibrator.
 */
class HapticManager(
    context: Context,
    private val view: View?,
    private val enabled: () -> Boolean = { true }
) {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /** A feather-light tick, softer than [tap]. */
    fun feather(force: Boolean = false) =
        ifEnabled(force) { custom(intensity = 0.15f, durationMillis = 20) }

    fun tap(force: Boolean = false) =
        ifEnabled(force) { view?.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK) }

    fun longPress(force: Boolean = false) =
        ifEnabled(force) { view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) }

    fun keyboardTap(force: Boolean = false) =
        ifEnabled(force) { view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }

    /** Success: two crisp ticks; devices without the click primitive get a double-pulse waveform. */
    @RequiresApi(Build.VERSION_CODES.R)
    fun success(force: Boolean = false) = ifEnabled(force) {
        if (!hasVibrator()) return@ifEnabled

        if (arePrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)) {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.6f, 50)
                .compose()
            vibrator.vibrate(effect)
        } else {
            val timings = longArrayOf(0, 20, 50, 20)
            val amplitudes = intArrayOf(0, 150, 0, 100)
            safeWaveformVibrate(timings, amplitudes)
        }
    }

    /** Error/warning: rapid heavy thuds; devices without the thud primitive get a double buzz. */
    @RequiresApi(Build.VERSION_CODES.S)
    fun error(force: Boolean = false) = ifEnabled(force) {
        if (!hasVibrator()) return@ifEnabled

        if (arePrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_THUD)) {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f, 100)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.8f, 100)
                .compose()
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 100, 100), -1)
        }
    }

    /**
     * A spring settle: a thud (plus low tick where supported) followed by a delayed softer
     * rebound, with [scale] driving the intensity. Devices without primitives get a short buzz.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun spring(scale: Float = 1f, force: Boolean = false) = ifEnabled(force) {
        if (!hasVibrator()) return@ifEnabled

        if (arePrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_THUD)) {
            val delayMs = 200L
            val composition = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, scale)

            if (arePrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_LOW_TICK))
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, scale)

            vibrator.vibrate(composition.compose())

            view?.postDelayed({
                if (!force && !enabled()) return@postDelayed
                vibrator.vibrate(
                    VibrationEffect.startComposition()
                        .addPrimitive(
                            VibrationEffect.Composition.PRIMITIVE_THUD,
                            (scale - 0.7f).coerceAtLeast(0f) + 0.1f
                        )
                        .compose()
                )
            }, delayMs)

        } else {
            safeOneShotVibrate(40, 180)
        }
    }

    /** A wobble: three quick spins of varying strength; devices without the spin primitive get a soft pulse. */
    @RequiresApi(Build.VERSION_CODES.S)
    fun wobble(force: Boolean = false) = ifEnabled(force) {
        if (!hasVibrator()) return@ifEnabled

        if (arePrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_SPIN)) {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, 0.5f)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, 0.7f, 50)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, 0.5f, 50)
                .compose()
            vibrator.vibrate(effect)
        } else {
            safeOneShotVibrate(150, 100)
        }
    }

    /**
     * One-shot vibration with [intensity] clamped to 0..1 and mapped onto the platform amplitude
     * range; devices without amplitude control vibrate at their default strength.
     */
    fun custom(intensity: Float, durationMillis: Long, force: Boolean = false) = ifEnabled(force) {
        if (!hasVibrator()) return@ifEnabled

        val safeIntensity = intensity.coerceIn(0f, 1f)
        val amplitude = (safeIntensity * 255).roundToInt()

        safeOneShotVibrate(durationMillis, amplitude)
    }

    private inline fun ifEnabled(force: Boolean, block: () -> Unit) {
        if (force || enabled()) block()
    }

    private fun safeOneShotVibrate(duration: Long, amplitude: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effectiveAmplitude =
                if (vibrator.hasAmplitudeControl()) amplitude else VibrationEffect.DEFAULT_AMPLITUDE
            vibrator.vibrate(VibrationEffect.createOneShot(duration, effectiveAmplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun safeWaveformVibrate(timings: LongArray, amplitudes: IntArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (vibrator.hasAmplitudeControl()) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings, -1)
        }
    }

    private fun hasVibrator(): Boolean = vibrator.hasVibrator()

    /** Treats any failure from the platform query as unsupported; some implementations throw rather than return false. */
    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("WrongConstant")
    private fun arePrimitivesSupported(vararg primitiveIds: Int): Boolean {
        return try {
            vibrator.areAllPrimitivesSupported(*primitiveIds)
        } catch (_: Exception) {
            false
        }
    }
}

val LocalHapticManager = staticCompositionLocalOf<HapticManager> {
    error("No HapticManager provided")
}

/**
 * Installs a [HapticManager] for the current context and view into [LocalHapticManager].
 *
 * [enabled] is read on every haptic call (via [rememberUpdatedState]), so toggling the setting
 * takes effect immediately without recreating the manager. Must be supplied explicitly — omitting
 * it in a production subtree would install a permanently-enabled manager that shadows the outer
 * composition-local.
 */
@Composable
fun ProvideHapticManager(enabled: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val enabledState = rememberUpdatedState(enabled)

    val hapticManager = remember(context, view) {
        HapticManager(context, view, enabled = { enabledState.value })
    }

    CompositionLocalProvider(LocalHapticManager provides hapticManager) {
        content()
    }
}

/**
 * The [HapticManager] installed by the nearest [ProvideHapticManager].
 */
@Composable
fun rememberHapticManager(): HapticManager {
    // Under Compose previews a local manager is built on the spot
    if (LocalInspectionMode.current) {
        val context = LocalContext.current
        val view = LocalView.current
        return remember(context, view) { HapticManager(context, view) }
    }
    return LocalHapticManager.current
}
