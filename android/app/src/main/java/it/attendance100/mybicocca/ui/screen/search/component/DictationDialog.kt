package it.attendance100.mybicocca.ui.screen.search.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager

/**
 * Voice-search dialog in the M3 expressive language: a centered card with a live audio-reactive
 * mic centerpiece and the transcript as it arrives. The platform dim is disabled and a scrim is
 * drawn in-content instead, so scrim, scale and fade all enter and leave on the same transition
 * — the window itself would otherwise pop while the card animates; the window stays composed
 * until the exit transition finishes, then drops entirely. Tapping outside (or the mic/close
 * actions) accepts what was heard so far, like the IME mic.
 *
 * @param transcript partial recognition so far; blank shows a pulsing "Parla ora…" placeholder.
 * @param soundLevel normalized 0..1 input level driving the halo and knob morph.
 */
@Composable
fun DictationDialog(
    visible: Boolean,
    transcript: String,
    soundLevel: Float,
    onFinish: () -> Unit,
) {
    val haptic = rememberHapticManager()
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = visible
    if (!transitionState.currentState && !transitionState.targetState) return

    Dialog(
        onDismissRequest = onFinish,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.setDimAmount(0f) }

        val scheme = MaterialTheme.colorScheme
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visibleState = transitionState,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(scheme.scrim.copy(alpha = 0.32f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { haptic.tap(); onFinish() },
                        ),
                )
            }
            AnimatedVisibility(
                visibleState = transitionState,
                enter = fadeIn() + scaleIn(initialScale = 0.8f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )),
                exit = fadeOut() + scaleOut(targetScale = 0.9f),
            ) {
                DictationCard(
                    transcript = transcript,
                    soundLevel = soundLevel,
                    onFinish = onFinish,
                )
            }
        }
    }
}

/**
 * The close action stays brand red with explicit white content: the M3 error role shifts pink
 * in dark schemes, and onPrimary resolves to black-on-red there.
 */
@Composable
private fun DictationCard(
    transcript: String,
    soundLevel: Float,
    onFinish: () -> Unit,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = scheme.surfaceContainerHigh,
        modifier = Modifier
            .padding(horizontal = 40.dp)
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.search_voice_title),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            MicVisual(soundLevel = soundLevel, onClick = onFinish)
            Spacer(Modifier.height(20.dp))
            TranscriptText(transcript)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                FilledTonalButton(
                    onClick = { haptic.tap(); onFinish() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text(stringResource(R.string.common_close)) }
            }
        }
    }
}

/**
 * The audio-reactive centerpiece: a soft halo that breathes on its own and swells with the
 * voice, around a knob that blossoms from a circle into a scalloped cookie as the level
 * rises. The knob spins continuously — invisible while it's a circle at rest, so the
 * rotation only becomes apparent exactly when the shape does, and the mic glyph
 * counter-rotates to stay upright inside it.
 *
 * The raw level is spring-smoothed: RMS arrives in fast discrete steps, and the underdamped
 * follow turns them into an organic swell instead of a twitch. A slow breathing loop keeps
 * the halo alive through silence (and on engines that report no RMS at all).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MicVisual(
    soundLevel: Float,
    onClick: () -> Unit,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme

    val level by animateFloatAsState(
        targetValue = soundLevel,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "dictation-level",
    )

    val idle = rememberInfiniteTransition(label = "dictation-idle")
    val breath by idle.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dictation-breath",
    )
    val spin by idle.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 16_000, easing = LinearEasing)),
        label = "dictation-spin",
    )

    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided) }
    val knobShape = MorphShape(morph, level)

    Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val scale = 0.66f + 0.08f * breath + 0.26f * level
                    scaleX = scale
                    scaleY = scale
                    alpha = 0.45f + 0.4f * level
                }
                .background(scheme.primaryContainer, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(92.dp)
                .graphicsLayer {
                    rotationZ = spin
                    val scale = 1f + 0.08f * level
                    scaleX = scale
                    scaleY = scale
                }
                .clip(knobShape)
                .background(scheme.primary)
                .clickable(onClick = { haptic.tap(); onClick() }),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Mic,
                contentDescription = stringResource(R.string.search_voice_stop),
                tint = scheme.onPrimary,
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer { rotationZ = -spin },
            )
        }
    }
}

/** While nothing has been heard yet, the placeholder pulses gently to signal active listening. */
@Composable
private fun TranscriptText(transcript: String) {
    val scheme = MaterialTheme.colorScheme
    val listening = transcript.isBlank()
    val idle = rememberInfiniteTransition(label = "dictation-placeholder")
    val pulse by idle.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dictation-placeholder-alpha",
    )

    Text(
        text = if (listening) stringResource(R.string.dictation_speak_now) else transcript,
        style = MaterialTheme.typography.titleMedium,
        color = if (listening) scheme.onSurfaceVariant.copy(alpha = pulse) else scheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .animateContentSize(),
    )
}

/**
 * Scales the normalized morph path up to the knob's actual size. Constructed without remember
 * by design: the progress moves every frame while speaking, so caching buys nothing.
 */
private class MorphShape(
    private val morph: Morph,
    private val progress: Float,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val matrix = Matrix()
        matrix.scale(size.width, size.height)
        val path = morph.toPath(progress).asComposePath()
        path.transform(matrix)
        return Outline.Generic(path)
    }
}
