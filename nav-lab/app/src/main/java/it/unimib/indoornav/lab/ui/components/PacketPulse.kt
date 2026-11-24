package it.unimib.indoornav.lab.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import it.unimib.indoornav.lab.LabApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * 1 the moment a packet from one of [trackerKeys] arrives, fading back to 0, like a receive LED.
 * Stays 0 outside the app (previews) or when [trackerKeys] is empty.
 */
@Composable
fun rememberPacketPulse(trackerKeys: Set<String>): Animatable<Float, AnimationVector1D> {
    val packets = (LocalContext.current.applicationContext as? LabApp)?.surveyManager?.packets
    val reduceMotion = rememberReduceMotion()
    val pulse = remember { Animatable(0f) }
    LaunchedEffect(packets, trackerKeys, reduceMotion) {
        if (packets == null || trackerKeys.isEmpty()) return@LaunchedEffect
        packets.filter { it in trackerKeys }.collect {
            launch {
                pulse.snapTo(1f)
                if (reduceMotion) {
                    delay(300)
                    pulse.snapTo(0f)
                } else {
                    pulse.animateTo(0f, tween(durationMillis = 500, delayMillis = 120, easing = LinearEasing))
                }
            }
        }
    }
    return pulse
}
