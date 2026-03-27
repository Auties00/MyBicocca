package it.attendance100.mybicocca.ui.component.calendar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.withTimeoutOrNull
import it.attendance100.mybicocca.ui.component.calendar.CalendarConfig

/**
 * State holder per il pinch-to-zoom.
 * Condiviso tra DayTimelineView e WeekGridView.
 */
@Stable
class PinchZoomState(
    private val baseContentHeightPx: Float,
    private val maxZoom: Float,
    private val minZoomRange: ClosedFloatingPointRange<Float>,
    private val hapticFeedback: HapticFeedback
) {
    var targetZoom by mutableFloatStateOf(CalendarConfig.Zoom.DEFAULT_ZOOM)
        internal set

    /**
     * L'altezza del container, aggiornata tramite onSizeChanged.
     */
    var containerHeight by mutableFloatStateOf(0f)
        internal set

    private var hasHitMinZoom by mutableStateOf(false)
    private var hasHitMaxZoom by mutableStateOf(false)

    /**
     * Calcola il minZoom dinamicamente in base al containerHeight.
     */
    val minZoom: Float
        get() = if (containerHeight > 0) {
            (containerHeight / baseContentHeightPx).coerceIn(minZoomRange.start, minZoomRange.endInclusive)
        } else {
            0.5f
        }

    /**
     * Indica se siamo in modalità compatta (zoom al minimo).
     */
    val isCompactMode: Boolean
        get() = targetZoom <= minZoom * 1.1f

    /**
     * Aggiorna l'altezza del container.
     */
    fun updateContainerHeight(height: Float) {
        containerHeight = height
    }

    /**
     * Resetta i flag haptic quando inizia un nuovo gesto.
     */
    internal fun resetHapticFlags() {
        hasHitMinZoom = false
        hasHitMaxZoom = false
    }

    /**
     * Aggiorna lo zoom con haptic feedback ai limiti.
     */
    internal fun updateZoom(rawZoom: Float) {
        val newZoom = rawZoom.coerceIn(minZoom, maxZoom)

        // Haptic feedback quando si raggiunge il limite minimo
        if (rawZoom <= minZoom && !hasHitMinZoom) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            hasHitMinZoom = true
        } else if (rawZoom > minZoom) {
            hasHitMinZoom = false
        }

        // Haptic feedback quando si raggiunge il limite massimo
        if (rawZoom >= maxZoom && !hasHitMaxZoom) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            hasHitMaxZoom = true
        } else if (rawZoom < maxZoom) {
            hasHitMaxZoom = false
        }

        targetZoom = newZoom
    }
}

/**
 * Crea e ricorda uno state per il pinch-to-zoom.
 *
 * @param baseContentHeight L'altezza base del contenuto (senza zoom)
 * @param maxZoom Il livello massimo di zoom consentito
 * @param minZoomRange Il range per coerceIn del minZoom calcolato
 */
@Composable
fun rememberPinchZoomState(
    baseContentHeight: Dp,
    maxZoom: Float = CalendarConfig.Zoom.MAX_ZOOM,
    minZoomRange: ClosedFloatingPointRange<Float> = 0.3f..1f
): PinchZoomState {
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val baseContentHeightPx = with(density) { baseContentHeight.toPx() }

    return remember(baseContentHeightPx, maxZoom, minZoomRange, hapticFeedback) {
        PinchZoomState(
            baseContentHeightPx = baseContentHeightPx,
            maxZoom = maxZoom,
            minZoomRange = minZoomRange,
            hapticFeedback = hapticFeedback
        )
    }
}

/**
 * Anima il livello di zoom corrente.
 */
@Composable
fun PinchZoomState.animatedZoomLevel(
    animationDuration: Int = 150
): Float {
    val zoomLevel by animateFloatAsState(
        targetValue = targetZoom,
        animationSpec = tween(durationMillis = animationDuration),
        label = "zoomAnimation"
    )
    return zoomLevel
}

/**
 * Modifier completo che gestisce sia onSizeChanged che pinchToZoom.
 * Usare questo modifier per configurare automaticamente containerHeight e gesture.
 */
fun Modifier.zoomableContainer(
    zoomState: PinchZoomState
): Modifier = this
    .onSizeChanged { size ->
        zoomState.updateContainerHeight(size.height.toFloat())
    }
    .pinchToZoom(zoomState)

/**
 * Modifier per gestire solo il pinch-to-zoom gesture.
 * Usare zoomableContainer se si vuole anche il tracking automatico di containerHeight.
 */
fun Modifier.pinchToZoom(
    zoomState: PinchZoomState
): Modifier = this.pointerInput(zoomState.minZoom) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var secondPointer: PointerId? = null
        var initialSpan = 0f

        withTimeoutOrNull(100L) {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val pointers = event.changes.filter { it.pressed }

                if (pointers.size >= 2) {
                    secondPointer = pointers[1].id
                    val p1 = pointers[0].position
                    val p2 = pointers[1].position
                    initialSpan = (p1 - p2).getDistance()
                    zoomState.resetHapticFlags()
                    break
                }

                if (pointers.isEmpty()) {
                    break
                }
            }
        }

        if (secondPointer != null && initialSpan > 0f) {
            val initialZoom = zoomState.targetZoom

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val pointers = event.changes.filter { it.pressed }

                if (pointers.size < 2) break

                val currentSpan = (pointers[0].position - pointers[1].position).getDistance()
                val scaleFactor = currentSpan / initialSpan
                val rawZoom = initialZoom * scaleFactor

                zoomState.updateZoom(rawZoom)
                event.changes.forEach { it.consume() }
            }
        }
    }
}
