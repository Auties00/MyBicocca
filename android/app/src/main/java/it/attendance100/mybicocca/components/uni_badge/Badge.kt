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
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import kotlin.math.*

const val colorOpacity = 0.1f
const val partialColorOpacity = 0.2f

val chromaticColors = listOf(
  Color.White.copy(alpha = colorOpacity * 4),
  Color.White.copy(alpha = colorOpacity / 2),
  Color.Transparent,
  Color.White.copy(alpha = colorOpacity / 2),
  Color.White.copy(alpha = colorOpacity),
)
val partialChromaticColors = listOf(
  Color.Black.copy(alpha = partialColorOpacity),
  Color.Transparent,
  Color.White.copy(alpha = partialColorOpacity)
)

@Composable
fun CreditCard(
  modifier: Modifier = Modifier,
  frontContent: @Composable () -> Unit,
  backContent: @Composable () -> Unit,
  accentColor: Color,
  isChromatic: Boolean = false,
) {
  val localDensity = LocalDensity.current
  var isFlipped by rememberSaveable { mutableStateOf(false) }
  var rotationX by remember { mutableFloatStateOf(0f) }
  var rotationY by remember { mutableFloatStateOf(0f) }
  var cardWidth by remember { mutableFloatStateOf(0f) }
  var cardHeight by remember { mutableFloatStateOf(0f) }
  var touchX by remember { mutableFloatStateOf(0.5f) }
  var touchY by remember { mutableFloatStateOf(0.5f) }
  val flipRotation by animateFloatAsState(
    targetValue = if (isFlipped) 180f else 0f,
    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
    label = "flipRotation"
  )
  val animatedRotationX by animateFloatAsState(
    targetValue = rotationX, animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
    ), label = "rotationX"
  )
  val animatedRotationY by animateFloatAsState(
    targetValue = rotationY, animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
    ), label = "rotationY"
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
  val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
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

        tiltY = -(pitch).coerceIn(-1f, 1f)
        tiltX = (roll).coerceIn(-1f, 1f)
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
              onDragEnd = {
                rotationX = 0f
                rotationY = 0f
                touchX = 0.5f
                touchY = 0.5f
              },
              onDragCancel = {
                rotationX = 0f
                rotationY = 0f
                touchX = 0.5f
                touchY = 0.5f
              },
            ) { change, dragAmount ->
              change.consume()
              val currentTouchX = change.position.x
              val currentTouchY = change.position.y
              val normalizedX = (currentTouchX - cardWidth / 2f) / (cardWidth / 2f)
              val normalizedY = (currentTouchY - cardHeight / 2f) / (cardHeight / 2f)
              val maxRotation = 10f
              rotationX = -normalizedY * maxRotation
              rotationY = normalizedX * maxRotation
              touchX = (currentTouchX / cardWidth).coerceIn(0f, 1f)
              touchY = (currentTouchY / cardHeight).coerceIn(0f, 1f)
            }
          }
          .pointerInput(Unit) {
            detectTapGestures(
              onTap = {
                isFlipped = !isFlipped
              })
          }
          .graphicsLayer {
            this.rotationY = flipRotation + animatedRotationY
            this.rotationX = animatedRotationX
            cameraDistance = 12f * localDensity.density
          }, contentAlignment = Alignment.Center
    ) {
      CardFace(
        modifier = Modifier.wrapContentSize().run {
          if (flipRotation > 90f) this.graphicsLayer { this.rotationY = 180f }
          else this
        },
        content = if (flipRotation <= 90f) frontContent else backContent,
        background = accentColor,
        isChromatic = isChromatic,
        touchX = animatedTouchX + tiltX,
        touchY = animatedTouchY + tiltY
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
  content: @Composable () -> Unit,
) {
  val primaryColor = MaterialTheme.colorScheme.primary

  Box(modifier = modifier) {
    Card(
      modifier = Modifier
          .clip(shape = RoundedCornerShape(size = 20.dp))
          .aspectRatio(1.6111112f),
      colors = CardDefaults.cardColors(containerColor = if (isChromatic) primaryColor else background),
    ) {
      content()
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