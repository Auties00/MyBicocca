package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.ui.text.style.TextOverflow
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MENSA_MAX_CART_QUANTITY
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val MENSA_MAX_QUANTITY = MENSA_MAX_CART_QUANTITY

/** Drag distance that changes the quantity by one. */
private val StepDistance = 30.dp

/**
 * The card's order button. Empty it is a plus that adds the dish. With dishes in the order it
 * shows the quantity as a small wheel: dragging down brings the next number in from above and
 * adds one, dragging up takes one away, and a tap opens the quantity sheet. The first time a dish
 * is added the number nudges down for a moment, so the gesture can be discovered.
 */
@Composable
internal fun MensaQuantityStepper(
    quantity: Int,
    onAdd: () -> Unit,
    onDecrement: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val scope = rememberCoroutineScope()
    val stepPx = with(LocalDensity.current) { StepDistance.toPx() }

    // The number on show moves with the finger, so it is tracked here and caught up with the real
    // quantity when that arrives a frame later.
    var shown by remember(quantity) { mutableIntStateOf(quantity) }
    var offset by remember { mutableFloatStateOf(0f) }
    var settling by remember { mutableStateOf<Job?>(null) }

    fun settle(spec: suspend () -> Unit) {
        settling?.cancel()
        settling = scope.launch { spec() }
    }

    // The nudge only plays when the dish has just been added, not when a card scrolls into view
    // that already has a quantity.
    var previous by remember { mutableIntStateOf(quantity) }
    LaunchedEffect(quantity) {
        val justAdded = previous == 0 && quantity == 1
        previous = quantity
        if (justAdded) {
            delay(450)
            animate(0f, stepPx * 0.5f, animationSpec = tween(260, easing = FastOutSlowInEasing)) { v, _ -> offset = v }
            animate(offset, 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) { v, _ -> offset = v }
        }
    }

    val dragState = rememberDraggableState { delta ->
        settling?.cancel()
        offset += delta
        // Down adds, up removes: the new number follows the finger in from the side it came from.
        while (offset >= stepPx && shown < MENSA_MAX_QUANTITY) {
            offset -= stepPx
            shown += 1
            onAdd()
            haptic.feather()
        }
        while (offset <= -stepPx && shown > 0) {
            offset += stepPx
            shown -= 1
            onDecrement()
            haptic.feather()
        }
        offset = offset.coerceIn(-stepPx, stepPx)
    }

    val increase = stringResource(R.string.mensa_quantity_increase)
    val decrease = stringResource(R.string.mensa_quantity_decrease)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = scheme.primary,
        contentColor = scheme.onPrimary,
        modifier = modifier
            .size(size)
            .then(
                if (quantity > 0) {
                    Modifier
                        .semantics {
                            stateDescription = "$quantity"
                            customActions = listOf(
                                CustomAccessibilityAction(increase) { onAdd(); true },
                                CustomAccessibilityAction(decrease) { onDecrement(); true },
                            )
                        }
                        .draggable(
                            state = dragState,
                            orientation = Orientation.Vertical,
                            onDragStopped = { settle { animate(offset, 0f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { v, _ -> offset = v } } },
                        )
                } else {
                    Modifier
                },
            )
            .clickable { haptic.tap(); if (quantity > 0) onEdit() else onAdd() },
    ) {
        if (quantity > 0) {
            Box(modifier = Modifier.clipToBounds(), contentAlignment = Alignment.Center) {
                // Neighbours sit one step away, so a number that has just changed lands where its
                // predecessor was.
                val progress = (abs(offset) / stepPx).coerceIn(0f, 1f)
                WheelNumber(shown, offset, 1f - progress * 0.75f)
                if (offset > 0f && shown < MENSA_MAX_QUANTITY) WheelNumber(shown + 1, offset - stepPx, progress)
                if (offset < 0f && shown > 0) WheelNumber(shown - 1, offset + stepPx, progress)
            }
        } else {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.mensa_dish_add_takeaway),
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun WheelNumber(value: Int, offsetPx: Float, alpha: Float) {
    Text(
        text = "$value",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.graphicsLayer {
            translationY = offsetPx
            this.alpha = alpha
        },
    )
}

/** The rounding where two controls of the sheet meet; the same on the buttons and the text box. */
private val InnerCorner = CornerSize(6.dp)
private val OuterCorner = CornerSize(50)

private val LeadingShape = RoundedCornerShape(topStart = OuterCorner, bottomStart = OuterCorner, topEnd = InnerCorner, bottomEnd = InnerCorner)
private val TrailingShape = RoundedCornerShape(topStart = InnerCorner, bottomStart = InnerCorner, topEnd = OuterCorner, bottomEnd = OuterCorner)
private val MiddleShape = RoundedCornerShape(InnerCorner)

/**
 * Sets a dish's quantity exactly: minus and plus either side of a number box, all one row. Typing a
 * number applies it as it is typed; a quantity of zero, or the remove button, takes the dish out of
 * the order.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MensaQuantitySheet(
    dishName: String,
    quantity: Int,
    onSetQuantity: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()
    // The dish leaving the order (here or from outside) closes the sheet.
    LaunchedEffect(quantity) { if (quantity <= 0) onDismiss() }
    var text by remember { mutableStateOf(quantity.toString()) }
    LaunchedEffect(quantity) { if (text.toIntOrNull() != quantity && quantity > 0) text = quantity.toString() }

    PredictiveModalBottomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(R.string.mensa_quantity_sheet_title, dishName),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            // Pressing a button grows it toward the middle of the row; the box is pushed and shrunk
            // toward the far side and the opposite button gives up a little room too.
            val minusSource = remember { MutableInteractionSource() }
            val plusSource = remember { MutableInteractionSource() }
            val minusDown by minusSource.collectIsPressedAsState()
            val plusDown by plusSource.collectIsPressedAsState()
            val grow = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            val minusWeight by animateFloatAsState(if (minusDown) 1.6f else if (plusDown) 0.8f else 1f, grow, label = "minus weight")
            val plusWeight by animateFloatAsState(if (plusDown) 1.6f else if (minusDown) 0.8f else 1f, grow, label = "plus weight")
            val boxWeight by animateFloatAsState(if (minusDown || plusDown) 0.85f else 1.1f, grow, label = "box weight")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FilledTonalButton(
                    onClick = { haptic.tap(); onSetQuantity((quantity - 1).coerceAtLeast(1)) },
                    enabled = quantity > 1,
                    shape = LeadingShape,
                    interactionSource = minusSource,
                    modifier = Modifier.weight(minusWeight).height(StepHeight),
                ) {
                    Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.mensa_quantity_decrease))
                }
                OutlinedTextField(
                    value = text,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(2)
                        text = digits
                        digits.toIntOrNull()?.takeIf { it in 1..MENSA_MAX_QUANTITY }?.let(onSetQuantity)
                    },
                    singleLine = true,
                    shape = MiddleShape,
                    textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(boxWeight).height(StepHeight),
                )
                FilledTonalButton(
                    onClick = { haptic.tap(); onSetQuantity((quantity + 1).coerceAtMost(MENSA_MAX_QUANTITY)) },
                    enabled = quantity < MENSA_MAX_QUANTITY,
                    shape = TrailingShape,
                    interactionSource = plusSource,
                    modifier = Modifier.weight(plusWeight).height(StepHeight),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.mensa_quantity_increase))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Same pairing as the map's building sheet: a quiet action beside the brand one.
                FilledTonalButton(
                    onClick = { haptic.tap(); onSetQuantity(0) },
                    shape = LeadingShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = scheme.surfaceContainerHighest,
                        contentColor = scheme.onSurface,
                    ),
                    modifier = Modifier.weight(1f).height(ActionHeight),
                ) {
                    Icon(Icons.Outlined.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(
                        text = stringResource(R.string.mensa_quantity_remove),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Button(
                    onClick = { haptic.tap(); if (text.toIntOrNull() == 0) onSetQuantity(0) else onDismiss() },
                    shape = TrailingShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (dark) scheme.primaryContainer else scheme.primary,
                        contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
                    ),
                    modifier = Modifier.weight(1f).height(ActionHeight),
                ) {
                    Text(
                        text = stringResource(R.string.mensa_quantity_done),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private val StepHeight = 60.dp
private val ActionHeight = 52.dp

@Preview(name = "Quantity stepper", widthDp = 320, heightDp = 120)
@Composable
private fun QuantityStepperPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MensaQuantityStepper(quantity = 0, onAdd = {}, onDecrement = {}, onEdit = {})
            MensaQuantityStepper(quantity = 2, onAdd = {}, onDecrement = {}, onEdit = {})
            MensaQuantityStepper(quantity = 12, onAdd = {}, onDecrement = {}, onEdit = {})
        }
    }
}
