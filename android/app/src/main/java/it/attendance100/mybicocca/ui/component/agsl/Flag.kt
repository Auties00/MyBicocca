package it.attendance100.mybicocca.ui.component.agsl

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.delay
import org.intellij.lang.annotations.Language

@Language("AGSL")
const val WavyShader = """
    uniform float2 resolution;
    uniform float phase;
    uniform float amplitudeRatio;
    uniform shader contents;

    half4 main(in float2 fragCoord) {
        float normalizedX = fragCoord.x / resolution.x;
        float maxAmplitude = resolution.y * amplitudeRatio;
        float currentAmplitude = maxAmplitude * normalizedX;
        float frequency = (6.28318 / resolution.x) * 1.5;
        float yOffset = sin(fragCoord.x * frequency - phase) * currentAmplitude;
        // Sampling outside the content bounds intentionally returns transparent,
        // so the waving edge reveals cleanly instead of smearing the edge row.
        float2 displacedCoord = float2(fragCoord.x, fragCoord.y - yOffset);

        return contents.eval(displacedCoord);
    }
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DeformingFlagBox(
    modifier: Modifier = Modifier,
    amplitudeRatio: Float = 0.1f,
    isWaving: Boolean = false,
    flag: @Composable BoxScope.() -> Unit
) {
    // The phase always runs, but when amplitude is 0, the flag appears perfectly still
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Smoothly transition the amplitude so the flag doesn't "snap" when stopping/starting
    val animatedAmplitude by animateFloatAsState(
        targetValue = if (isWaving) amplitudeRatio else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "amplitudeSmooth"
    )

    var size by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val shader = remember { RuntimeShader(WavyShader) }

    Box(
        modifier = modifier
            .onSizeChanged {
                size = androidx.compose.ui.geometry.Size(it.width.toFloat(), it.height.toFloat())
            }
            .graphicsLayer {
                // Only run the shader while there is something to animate
                if (size.width > 0f && size.height > 0f && animatedAmplitude > 0f) {
                    shader.setFloatUniform("resolution", size.width, size.height)
                    shader.setFloatUniform("phase", phase)

                    // Feed the animated amplitude to the shader
                    shader.setFloatUniform("amplitudeRatio", animatedAmplitude)

                    renderEffect = RenderEffect.createRuntimeShaderEffect(
                        shader, "contents"
                    ).asComposeRenderEffect()
                } else {
                    renderEffect = null
                }
            }
    ) {
        flag()
    }
}

@Composable
fun WavingSelectionWrapper(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (isWaving: Boolean) -> Unit
) {
    var isWaving by remember { mutableStateOf(false) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            isWaving = true
            delay(1000L)
            isWaving = false
        } else {
            isWaving = false
        }
    }

    Box(modifier = modifier) {
        content(isWaving)
    }
}