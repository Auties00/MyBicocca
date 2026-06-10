@file:Suppress("AssignedValueIsNeverRead")

package it.attendance100.mybicocca.ui.screen.profile.component

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import it.attendance100.mybicocca.ui.theme.BadgeCardColorDark
import it.attendance100.mybicocca.ui.theme.BadgeCardColorLight
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

private val chromaticColors = listOf(
    Color.Magenta.copy(alpha = 0.12f),
    Color.Cyan.copy(alpha = 0.12f),
    Color.Yellow.copy(alpha = 0.12f),
    Color(0xFFFFC0CB).copy(alpha = 0.12f),
    Color.Transparent,
)
private val partialChromaticColors = listOf(
    Color.Cyan.copy(alpha = 0.12f),
    Color.Transparent,
    Color.Magenta.copy(alpha = 0.12f),
)

/**
 * Card geometry shared with layout code that must reserve or position the card's
 * footprint, so it derives the same proportions the face itself is drawn with.
 */
const val CreditCardAspectRatio = 1.6111112f
val CreditCardHorizontalInset = 16.dp

/** Vertical gap reserved between the bottom of the top bar and the top of the student card. */
val ProfileCardTopGap = 20.dp

/**
 * Height the card occupies when laid out full width with [horizontalInset] on each side,
 * derived from the screen width so space can be reserved before the card is placed.
 */
@Composable
fun creditCardHeight(horizontalInset: Dp = CreditCardHorizontalInset): Dp {
    val widthDp = LocalConfiguration.current.screenWidthDp.dp
    return ((widthDp - horizontalInset * 2) / CreditCardAspectRatio).coerceAtLeast(0.dp)
}

/**
 * Draggable, tappable 3D flip card. Horizontal drags accumulate rotation freely; a flick
 * (a short, fast drag) or a half-card-width drag commits a 180-degree turn, anything less
 * snaps back to the nearest face, and a tap flips toward the touched half. A
 * rotation-vector sensor adds a subtle parallax offset layered on top of the touch-driven
 * one so the face art shifts as the device moves.
 */
@Composable
fun CreditCard(
    modifier: Modifier = Modifier,
    frontContent: @Composable (touchX: Float, touchY: Float, whiteBadge: Boolean, hazeState: HazeState) -> Unit,
    backContent: @Composable (touchX: Float, touchY: Float, whiteBadge: Boolean, hazeState: HazeState) -> Unit,
    accentColor: Color,
    isChromatic: Boolean = false,
    whiteBadge: Boolean = false,
    enabled: Boolean = true,
    sensorsEnabled: Boolean = true,
) {
    val localDensity = LocalDensity.current
    val scope = rememberCoroutineScope()

    val rotationY = remember { Animatable(0f) }
    var rotationX by remember { mutableFloatStateOf(0f) }

    var cardWidth by remember { mutableFloatStateOf(0f) }
    var cardHeight by remember { mutableFloatStateOf(0f) }

    var touchX by remember { mutableFloatStateOf(0.5f) }
    var touchY by remember { mutableFloatStateOf(0.5f) }
    var gestureStartTime by remember { mutableLongStateOf(0L) }
    var totalDragX by remember { mutableFloatStateOf(0f) }

    val animatedRotationX by animateFloatAsState(
        targetValue = rotationX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "rotationX",
    )
    val animatedTouchX by animateFloatAsState(
        targetValue = touchX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "touchX",
    )
    val animatedTouchY by animateFloatAsState(
        targetValue = touchY,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "touchY",
    )

    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val hazeState = remember { HazeState() }
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }

    if (sensorsEnabled) DisposableEffect(Unit) {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)

                val pitch = orientation[1]
                val roll = orientation[2]

                tiltY = -(pitch / 2.5f).coerceIn(-1f, 1f)
                tiltX = (roll / 2.5f).coerceIn(-1f, 1f)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensor != null) {
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val interactionModifier = Modifier
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    gestureStartTime = System.currentTimeMillis()
                    totalDragX = 0f
                },
                onDragEnd = {
                    val current = rotationY.value
                    val duration = (System.currentTimeMillis() - gestureStartTime)
                    val isFlick = duration in 0..<100

                    val thresholdPx = if (isFlick) cardWidth * 0.05f else cardWidth * 0.5f
                    val absTotal = abs(totalDragX)

                    val base = round(current / 180f) * 180f

                    val target = if (isFlick && cardWidth > 0 && absTotal > thresholdPx) {
                        val dir = if (totalDragX > 0f) 1f else -1f
                        base + 180f * dir
                    } else {
                        base
                    }
                    scope.launch {
                        rotationY.animateTo(
                            target,
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
                    }
                    rotationX = 0f
                    touchX = 0.5f
                    touchY = 0.5f
                },
                onDragCancel = {
                    val current = rotationY.value
                    val base = round(current / 180f) * 180f
                    scope.launch {
                        rotationY.animateTo(
                            base,
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
                    }
                    rotationX = 0f
                    touchX = 0.5f
                    touchY = 0.5f
                },
            ) { change, dragAmount ->
                change.consume()
                val currentTouchX = change.position.x
                val currentTouchY = change.position.y

                if (cardWidth > 0 && cardHeight > 0) {
                    touchX = (currentTouchX / cardWidth).coerceIn(0f, 1f)
                    touchY = (currentTouchY / cardHeight).coerceIn(0f, 1f)

                    val normalizedY = (currentTouchY - cardHeight / 2f) / (cardHeight / 2f)
                    rotationX = -normalizedY * 10f

                    totalDragX += dragAmount.x
                    val rotationDelta = (dragAmount.x / cardWidth) * 180f
                    scope.launch {
                        rotationY.snapTo(rotationY.value + rotationDelta)
                    }
                }
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    val current = rotationY.value
                    val isLeft = if (cardWidth > 0) offset.x < cardWidth / 2 else false
                    val direction = if (isLeft) -1f else 1f
                    val target = (round(current / 180f) + direction) * 180f

                    scope.launch {
                        rotationY.animateTo(
                            target,
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
                    }
                },
            )
        }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .onGloballyPositioned { coordinates ->
                    cardWidth = coordinates.size.width.toFloat()
                    cardHeight = coordinates.size.height.toFloat()
                }
                .then(if (enabled) interactionModifier else Modifier)
                .graphicsLayer {
                    this.rotationY = rotationY.value
                    this.rotationX = animatedRotationX
                    cameraDistance = 12f * localDensity.density
                },
            contentAlignment = Alignment.Center,
        ) {
            val angle = (rotationY.value % 360 + 360) % 360
            val isBack = angle > 90f && angle < 270f

            CardFace(
                modifier = Modifier
                    .wrapContentSize()
                    .run {
                        if (isBack) this.graphicsLayer { this.rotationY = 180f } else this
                    }
                    .hazeSource(state = hazeState),
                content = if (isBack) backContent else frontContent,
                background = accentColor,
                isChromatic = isChromatic,
                whiteBadge = whiteBadge,
                touchX = animatedTouchX + tiltX,
                touchY = animatedTouchY + tiltY,
                hazeState = hazeState,
            )
        }
    }
}

/**
 * One face of the flip card: the content card plus, in the chromatic finish, two
 * multiply-blended iridescent gradients that track the touch point. The chromatic face
 * fills with the dedicated badge red — deep in dark theme, bright in light — rather than
 * the Material primary, so it reads as the physical university ID card in both modes.
 */
@Composable
private fun CardFace(
    modifier: Modifier = Modifier,
    background: Color = Color.White,
    isChromatic: Boolean = false,
    whiteBadge: Boolean = false,
    touchX: Float = 0.5f,
    touchY: Float = 0.5f,
    hazeState: HazeState,
    content: @Composable (touchX: Float, touchY: Float, whiteBadge: Boolean, hazeState: HazeState) -> Unit,
) {
    val primaryColor = when {
        whiteBadge -> Color.White
        isSystemInDarkTheme() -> BadgeCardColorDark
        else -> BadgeCardColorLight
    }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .aspectRatio(CreditCardAspectRatio),
            colors = CardDefaults.cardColors(containerColor = if (isChromatic) primaryColor else background),
        ) {
            content(touchX, touchY, whiteBadge, hazeState)
        }
        if (isChromatic) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape = RoundedCornerShape(size = 20.dp)),
            ) {
                val gradientBrush = Brush.linearGradient(
                    colors = chromaticColors,
                    start = Offset.Zero,
                    end = Offset(x = size.width * 0.75f, y = size.height * 0.75f),
                )
                drawRect(brush = gradientBrush, size = size, blendMode = BlendMode.Multiply)

                val angle = (touchX - 0.5f) * 3.14f * 2f
                val gradientBrush2 = Brush.linearGradient(
                    colors = partialChromaticColors,
                    start = Offset(
                        x = size.width * touchX - cos(angle) * size.width * 0.5f,
                        y = size.height * touchY - sin(angle) * size.height * 0.5f,
                    ),
                    end = Offset(
                        x = size.width * touchX + cos(angle) * size.width * 0.5f,
                        y = size.height * touchY + sin(angle) * size.height * 0.5f,
                    ),
                )
                drawRect(brush = gradientBrush2, size = size, blendMode = BlendMode.Multiply)
            }
        }
    }
}
