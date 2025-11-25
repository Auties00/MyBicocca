package it.attendance100.mybicocca.components.uni_badge

import android.content.*
import android.hardware.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import dev.chrisbanes.haze.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.*
import kotlin.math.*

const val colorOpacity = 0.2f
const val partialColorOpacity = 0.2f

val chromaticColors = listOf(
  Color.Magenta.copy(alpha = 0.12f),
  Color.Cyan.copy(alpha = 0.12f),
  Color.Yellow.copy(alpha = 0.12f),
  Color(0xFFFFC0CB).copy(alpha = 0.12f),
  Color.Transparent
)
val partialChromaticColors = listOf(
  Color.Cyan.copy(alpha = 0.12f),
  Color.Transparent,
  Color.Magenta.copy(alpha = 0.12f)
)

@Composable
fun CreditCard(
  modifier: Modifier = Modifier,
  frontContent: @Composable (touchX: Float, touchY: Float, whiteBadge: Boolean, hazeState: HazeState) -> Unit,
  backContent: @Composable (touchX: Float, touchY: Float, whiteBadge: Boolean, hazeState: HazeState) -> Unit,
  accentColor: Color,
  isChromatic: Boolean = false,
) {
  val localDensity = LocalDensity.current
  val scope = rememberCoroutineScope()
  val haptics = rememberHapticManager()

  val rotationY = remember { Animatable(0f) }
  var rotationX by remember { mutableFloatStateOf(0f) }

  var cardWidth by remember { mutableFloatStateOf(0f) }
  var cardHeight by remember { mutableFloatStateOf(0f) }

  var touchX by remember { mutableFloatStateOf(0.5f) }
  var touchY by remember { mutableFloatStateOf(0.5f) }
  var gestureStartTime by remember { mutableStateOf(0L) }
  var totalDragX by remember { mutableFloatStateOf(0f) }
  var hasFeatheredDuringGesture by remember { mutableStateOf(false) }
  var prevAngleDuringGesture by remember { mutableFloatStateOf(((rotationY.value % 360f) + 360f) % 360f) }

  val animatedRotationX by animateFloatAsState(
    targetValue = rotationX, animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
    ), label = "rotationX"
  )
  val animatedTouchX by animateFloatAsState(
    targetValue = touchX, animationSpec = spring(
      dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow
    ), label = "touchX"
  )
  val animatedTouchY by animateFloatAsState(
    targetValue = touchY, animationSpec = spring(
      dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow
    ), label = "touchY"
  )

  val context = LocalContext.current
  val haptic = rememberHapticManager()
  val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
  val hazeState = remember { HazeState() }
  var tiltX by remember { mutableFloatStateOf(0f) }
  var tiltY by remember { mutableFloatStateOf(0f) }

  DisposableEffect(Unit) {
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

    if (sensor != null) sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)


    onDispose {
      sensorManager.unregisterListener(listener)
    }
  }

  Box(
    modifier = modifier, contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
          .wrapContentSize()
          .onGloballyPositioned { coordinates ->
            cardWidth = coordinates.size.width.toFloat()
            cardHeight = coordinates.size.height.toFloat()
          }
          .pointerInput(Unit) {
            detectDragGestures(
              onDragStart = { _ ->
                gestureStartTime = System.currentTimeMillis()
                totalDragX = 0f
                hasFeatheredDuringGesture = false
                prevAngleDuringGesture = (((rotationY.value % 360f) + 360f) % 360f)
                haptics.custom(intensity = 0.5f, durationMillis = 20)
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
                } else base
                scope.launch {
                  rotationY.animateTo(target, animationSpec = tween(600, easing = FastOutSlowInEasing))
                }
                rotationX = 0f
                touchX = 0.5f
                touchY = 0.5f
                if (!isFlick) haptics.tap()
              },
              onDragCancel = {
                val current = rotationY.value
                val base = round(current / 180f) * 180f
                scope.launch {
                  rotationY.animateTo(base, animationSpec = tween(600, easing = FastOutSlowInEasing))
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

                  // robust crossing detection: trigger feather when crossing 90° or 270° lines
                  val angleNow = (((rotationY.value % 360f) + 360f) % 360f)
                  val prev = prevAngleDuringGesture
                  val delta = (((angleNow - prev + 540f) % 360f) - 180f) // signed delta in [-180,180)

                  fun crossedThreshold(threshold: Float): Boolean {
                    if (abs(delta) < 0.0001f) return false
                    val tRel = (threshold - prev + 360f) % 360f
                    return if (delta > 0f) {
                      tRel > 0f && tRel <= delta
                    } else {
                      tRel >= 360f + delta && tRel < 360f
                    }
                  }

                  var didCross = false
                  if (crossedThreshold(90f) || crossedThreshold(270f)) {
                    didCross = true
                  }

                  if (didCross) {
                    try {
                      haptics.feather()
                    } catch (_: Exception) {
                    }
                  }

                  prevAngleDuringGesture = angleNow
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
                  rotationY.animateTo(target, animationSpec = tween(600, easing = FastOutSlowInEasing))
                }
                haptics.spring()
              })
          }
          .graphicsLayer {
            this.rotationY = rotationY.value
            this.rotationX = animatedRotationX
            cameraDistance = 12f * localDensity.density
          }, contentAlignment = Alignment.Center
    ) {
      val angle = (rotationY.value % 360 + 360) % 360
      val isBack = angle > 90f && angle < 270f

      CardFace(
        modifier = Modifier
            .wrapContentSize()
            .run {
              if (isBack) this.graphicsLayer { this.rotationY = 180f }
              else this
            }
            .hazeSource(state = hazeState),
        content = if (isBack) backContent else frontContent,
        background = accentColor,
        isChromatic = isChromatic,
        touchX = animatedTouchX + tiltX,
        touchY = animatedTouchY + tiltY,
        hazeState = hazeState,
      )
    }
  }
}

@Composable
fun CardFace(
  modifier: Modifier = Modifier,
  background: Color = Color.White,
  isChromatic: Boolean = false,
  touchX: Float = 0.5f,
  touchY: Float = 0.5f,
  hazeState: HazeState,
  content: @Composable (touchX: Float, touchY: Float, whiteBadge: Boolean, hazeState: HazeState) -> Unit,
) {
  val preferencesManager = rememberPreferencesManager()
  val whiteBadge = preferencesManager.badgeWhite
  val primaryColor = if (whiteBadge) Color.White else MaterialTheme.colorScheme.primary

  Box(modifier = modifier) {
    Card(
      modifier = Modifier
          .clip(shape = RoundedCornerShape(size = 20.dp))
          .aspectRatio(1.6111112f),
      colors = CardDefaults.cardColors(containerColor = if (isChromatic) primaryColor else background),
    ) {
      content(touchX, touchY, whiteBadge, hazeState)
    }
    // Chromatic overlay
    if (isChromatic) {
      Canvas(
        modifier = Modifier
            .matchParentSize()
            .clip(shape = RoundedCornerShape(size = 20.dp))
      ) {
        val gradientBrush = Brush.linearGradient(
          colors = chromaticColors,
          start = Offset.Zero,
          end = Offset(x = size.width * 0.75f, y = size.height * 0.75f)
        )
        drawRect(
          brush = gradientBrush, size = size, blendMode = BlendMode.Multiply
        )
        // Add secondary linear gradient for more dynamic effect
        val angle = (touchX - 0.5f) * 3.14f * 2f
        val gradientBrush2 = Brush.linearGradient(
          colors = partialChromaticColors, start = Offset(
            x = size.width * touchX - cos(angle) * size.width * 0.5f,
            y = size.height * touchY - sin(angle) * size.height * 0.5f
          ), end = Offset(
            x = size.width * touchX + cos(angle) * size.width * 0.5f,
            y = size.height * touchY + sin(angle) * size.height * 0.5f
          )
        )
        drawRect(
          brush = gradientBrush2, size = size, blendMode = BlendMode.Multiply
        )
      }
    }
  }
}