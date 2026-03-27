package it.attendance100.mybicocca.util

import android.content.*
import android.os.*
import android.view.*
import androidx.annotation.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import kotlin.math.*

/**
 * Haptic Manager for MyBicoca
 */
class HapticManager(private val context: Context, private val view: View?) {

  private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    vibratorManager.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
  }

  // Standard Hapcitc Feedback

  fun feather() {
	  custom(intensity = 0.15f, durationMillis = 20)
  }

  fun tap() {
    view?.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
  }

  fun longPress() {
    view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
  }

  fun keyboardTap() {
    view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
  }

  // Functional Feedback

  /**
   * Success: Two crisp ticks
   * [Legacy: A double click waveform]
   */
  fun success() {
    if (!hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && arePrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)) {
      // Rich feedback
      val effect = VibrationEffect.startComposition()
          .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f)
          .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.6f, 50) // 50ms delay
          .compose()
      vibrator.vibrate(effect)
    } else {
      // Fallback waveform: Wait 0ms, Vibrate 20ms, Wait 50ms, Vibrate 20ms
      val timings = longArrayOf(0, 20, 50, 20)
      val amplitudes = intArrayOf(0, 150, 0, 100) // Fallback amplitudes if supported
      safeWaveformVibrate(timings, amplitudes)
    }
  }

  /**
   * Error/Warning: multiple rapid thuds
   * [Legacy: A jarring buzz]
   */
  fun error() {
    if (!hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && arePrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_THUD)) {
      val effect = VibrationEffect.startComposition()
          .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f)
          .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f, 100)
          .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.8f, 100)
          .compose()
      vibrator.vibrate(effect)
    } else {
      // Fallback: simple double vibration: Wait 0ms, Vibrate 50ms, Wait 100ms, Vibrate 100ms
      @Suppress("DEPRECATION")
      vibrator.vibrate(longArrayOf(0, 50, 100, 100), -1)
    }
  }

  // Custom Feedback

  /**
   * Spring
   */
  fun spring(scale: Float = 1f) {
    if (!hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && arePrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_THUD)) {
      val delayMs = 200L
      val composition = VibrationEffect.startComposition()
          .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, scale)

      if (arePrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_LOW_TICK))
        composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, scale)

      vibrator.vibrate(composition.compose())

      view?.postDelayed({
        vibrator.vibrate(
          VibrationEffect.startComposition()
              .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, (scale - 0.7f).coerceAtLeast(0f) + 0.1f)
              .compose()
        )
      }, delayMs)

    } else {
      // Fallback: A single short buzz
      safeOneShotVibrate(40, 180)
    }
  }

  /**
   * Wobble
   */
  fun wobble() {
    if (!hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && arePrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_SPIN)) {
      val effect = VibrationEffect.startComposition()
          .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, 0.5f)
          .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, 0.7f, 50)
          .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, 0.5f, 50)
          .compose()
      vibrator.vibrate(effect)
    } else {
      // Fallback: A single soft pulse.
      safeOneShotVibrate(150, 100)
    }
  }

  /**
   * Custom single vibration.
   * @param intensity Float 0.0..1.0
   * @param durationMillis Duration in ms
   */
  fun custom(intensity: Float, durationMillis: Long) {
    if (!hasVibrator()) return

    val safeIntensity = intensity.coerceIn(0f, 1f)
    val amplitude = (safeIntensity * 255).roundToInt()

    safeOneShotVibrate(durationMillis, amplitude)
  }

  // Internals

  private fun safeOneShotVibrate(duration: Long, amplitude: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val effectiveAmplitude = if (vibrator.hasAmplitudeControl()) amplitude else VibrationEffect.DEFAULT_AMPLITUDE
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
        // If no amplitude control, just use timings
        vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
      }
    } else {
      @Suppress("DEPRECATION")
      vibrator.vibrate(timings, -1)
    }
  }

  private fun hasVibrator(): Boolean = vibrator.hasVibrator()

  @RequiresApi(Build.VERSION_CODES.R)
  private fun arePrimitivesSupported(vararg primitiveIds: Int): Boolean {
    // Safe check for primitive support
    return try {
      vibrator.areAllPrimitivesSupported(*primitiveIds)
    } catch (e: Exception) {
      false
    }
  }
}

// Compose Boilerplate

val LocalHapticManager = staticCompositionLocalOf<HapticManager> {
  error("No HapticManager provided")
}

@Composable
fun ProvideHapticManager(content: @Composable () -> Unit) {
  val context = LocalContext.current
  val view = LocalView.current

  // Create the manager once and remember it
  val hapticManager = remember(context, view) {
    HapticManager(context, view)
  }

  CompositionLocalProvider(LocalHapticManager provides hapticManager) {
    content()
  }
}

@Composable
fun rememberHapticManager(): HapticManager {
  return LocalHapticManager.current
}
