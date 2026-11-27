package it.unimib.indoornav.lab.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unimib.indoornav.lab.ui.theme.LabTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun <T> SegmentedSwitch(
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable (T, Color) -> Unit)? = null,
    borderColor: Color = Color.Unspecified,
) {
    require(options.isNotEmpty()) { "SegmentedSwitch needs at least one option" }

    val cornerRadius = 100.dp
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val currentSelected by rememberUpdatedState(selected)
    val currentOnSelected by rememberUpdatedState(onSelected)
    val resolvedBorder = if (borderColor == Color.Unspecified) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    } else {
        borderColor
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, resolvedBorder, RoundedCornerShape(cornerRadius))
            .padding(4.dp),
    ) {
        val count = options.size
        val segmentWidth = maxWidth / count
        val segmentWidthPx = with(LocalDensity.current) { segmentWidth.toPx() }
        val maxOffsetPx = segmentWidthPx * (count - 1)

        val selectedIndex = options.indexOf(selected).coerceAtLeast(0)

        val offsetXAnimatable = remember { Animatable(selectedIndex * segmentWidthPx) }
        var rawDragX by remember { mutableFloatStateOf(selectedIndex * segmentWidthPx) }
        var currentIndex by remember { mutableIntStateOf(selectedIndex) }
        var isDragging by remember { mutableStateOf(false) }

        LaunchedEffect(selectedIndex, segmentWidthPx) {
            if (!isDragging) {
                offsetXAnimatable.animateTo(selectedIndex * segmentWidthPx, tween(280))
            }
        }

        val dragModifier = Modifier.pointerInput(segmentWidthPx, count) {
            detectHorizontalDragGestures(
                onDragStart = {
                    isDragging = true
                    rawDragX = offsetXAnimatable.value
                    currentIndex = (rawDragX / segmentWidthPx).roundToInt().coerceIn(0, count - 1)
                },
                onDragEnd = {
                    isDragging = false
                    val target = currentIndex
                    coroutineScope.launch {
                        offsetXAnimatable.animateTo(target * segmentWidthPx, tween(200))
                        val option = options[target]
                        if (option != currentSelected) currentOnSelected(option)
                    }
                },
                onDragCancel = {
                    isDragging = false
                    coroutineScope.launch {
                        val idx = options.indexOf(currentSelected).coerceAtLeast(0)
                        offsetXAnimatable.animateTo(idx * segmentWidthPx, tween(200))
                    }
                },
            ) { change, dragAmount ->
                change.consume()
                rawDragX = (rawDragX + dragAmount).coerceIn(0f, maxOffsetPx)
                coroutineScope.launch { offsetXAnimatable.snapTo(rawDragX) }

                val newIndex = (rawDragX / segmentWidthPx).roundToInt().coerceIn(0, count - 1)
                if (newIndex != currentIndex) {
                    currentIndex = newIndex
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val option = options[newIndex]
                    if (option != currentSelected) currentOnSelected(option)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(dragModifier),
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetXAnimatable.value.roundToInt(), 0) }
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(MaterialTheme.colorScheme.primaryContainer),
            )

            Row(modifier = Modifier.fillMaxSize()) {
                options.forEach { option ->
                    val contentColor = if (option == selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        color = Color.Transparent,
                        shape = RoundedCornerShape(cornerRadius),
                        onClick = {
                            currentOnSelected(option)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            if (leadingIcon != null) {
                                leadingIcon(option, contentColor)
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(
                                text = label(option),
                                color = contentColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SegmentedSwitchPreview() {
    LabTheme {
        Surface {
            var selected by remember { mutableStateOf("List View") }
            SegmentedSwitch(
                options = listOf("List View", "Grid View"),
                selected = selected,
                onSelected = { selected = it },
                label = { it },
                leadingIcon = { option, tint ->
                    Icon(
                        imageVector = if (option == "List View") {
                            Icons.AutoMirrored.Filled.FormatListBulleted
                        } else {
                            Icons.Default.GridView
                        },
                        contentDescription = null,
                        tint = tint,
                    )
                },
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
