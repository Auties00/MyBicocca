package it.unimib.indoornav.lab.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import it.unimib.indoornav.lab.LabApp
import it.unimib.indoornav.lab.ui.theme.LabStatusColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * "1.0 pkt/s" whose number blinks red, like an RX LED, each time a packet from one of [trackerKeys] arrives.
 * [packets] defaults to the live survey stream; previews pass none and show a still dot.
 */
@Composable
fun PacketRateText(
    packetsPerSec: Double,
    trackerKeys: Set<String>,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: Int = 11,
    mono: Boolean = false,
    suffix: String = "",
    packets: SharedFlow<String>? = (LocalContext.current.applicationContext as? LabApp)?.surveyManager?.packets,
) {
    val reduceMotion = rememberReduceMotion()
    val pulse = remember { Animatable(0f) }

    LaunchedEffect(packets, trackerKeys) {
        packets ?: return@LaunchedEffect
        packets.filter { it in trackerKeys }.collect {
            launch {
                pulse.snapTo(1f)
                if (reduceMotion) {
                    delay(300)
                    pulse.snapTo(0f)
                } else {
                    pulse.animateTo(0f, tween(durationMillis = 450, delayMillis = 150, easing = LinearEasing))
                }
            }
        }
    }

    val style = TextStyle(
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
        color = color,
    )
    Row(modifier) {
        Text(
            "%.1f".format(Locale.US, packetsPerSec),
            style = style.copy(color = lerp(color, LabStatusColors.Red, pulse.value)),
            maxLines = 1,
        )
        Text(" pkt/s$suffix", style = style, maxLines = 1)
    }
}

@Preview
@Composable
private fun PacketRateTextPreview() {
    MaterialTheme {
        PacketRateText(3.0, emptySet(), MaterialTheme.colorScheme.onSurface, packets = null)
    }
}
