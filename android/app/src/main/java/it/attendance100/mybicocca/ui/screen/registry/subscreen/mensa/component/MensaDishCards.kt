package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenMatch
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaArt

/** Top-down illustration of a dish. It is decorative, so it carries no description. */
@Composable
internal fun MensaPlate(
    art: MensaArt,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(art.drawable),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .clearAndSetSemantics { },
    )
}

private val PlateSize = 92.dp
private val PlateOverlap = PlateSize / 2
private val AddButtonSize = 52.dp

/**
 * A dish card whose plate sits half outside the card's top edge. The card is a plain surface and
 * the plate a sibling drawn after it, so nothing clips the part that overflows. The add button
 * sits in a notch cut into the card's bottom-right corner, and an allergen badge on the plate's
 * top-right corner flags dishes that matter to the user.
 */
@Composable
internal fun MensaDishPlateCard(
    dish: MensaDish,
    art: MensaArt,
    allergenMatch: MensaAllergenMatch,
    cartQty: Int,
    showAdd: Boolean,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val cardShape = remember(showAdd) { NotchedCardShape(notch = if (showAdd) 60.dp else 0.dp) }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PlateOverlap),
            shape = cardShape,
            color = scheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier
                    .clickable { haptic.tap(); onClick() }
                    .padding(start = 14.dp, end = 14.dp, top = PlateOverlap + 10.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = dish.name.toDishDisplayName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
                Column(
                    modifier = Modifier.heightIn(min = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    dish.caloricLabel?.let { label ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColorOrNull(dish.caloricColorHex) ?: scheme.primary),
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    dish.dietaryBadges.firstOrNull()?.let { badge ->
                        Text(
                            text = badge.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.tertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (showAdd) Spacer(Modifier.height(AddButtonSize - 36.dp))
            }
        }

        MensaPlateWithBadge(
            dish = dish,
            art = art,
            allergenMatch = allergenMatch,
            size = PlateSize,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        if (showAdd) {
            if (cartQty > 0) {
                Surface(
                    shape = CircleShape,
                    color = scheme.surfaceContainerHighest,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 70.dp, bottom = 12.dp)
                        .size(30.dp)
                        .clickable { haptic.tap(); onDecrement() },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = scheme.primary,
                contentColor = scheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(AddButtonSize)
                    .clickable { haptic.tap(); onAdd() },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (cartQty > 0) {
                        Text(
                            text = "$cartQty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.mensa_dish_add_takeaway),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

/** A dish plate with the allergen badge on its top-right corner; the plate is the shared element. */
@Composable
internal fun MensaPlateWithBadge(
    dish: MensaDish,
    art: MensaArt,
    allergenMatch: MensaAllergenMatch,
    size: Dp,
    modifier: Modifier = Modifier,
    sharedElement: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = modifier.size(size)) {
        MensaPlate(art = art, size = size, modifier = Modifier.mensaPlateElement(dish.id, sharedElement))
        if (allergenMatch != MensaAllergenMatch.NONE) {
            val contains = allergenMatch == MensaAllergenMatch.CONTAINS
            Surface(
                shape = CircleShape,
                color = if (contains) scheme.error else scheme.tertiary,
                contentColor = if (contains) scheme.onError else scheme.onTertiary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(size * 0.3f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = MensaAllergenIcon,
                        contentDescription = stringResource(
                            if (contains) R.string.mensa_allergen_match_contains else R.string.mensa_allergen_match_trace,
                        ),
                        modifier = Modifier.size(size * 0.18f),
                    )
                }
            }
        }
    }
}

/** A rounded card with a rounded notch cut from its bottom-right corner, for the add button. */
private class NotchedCardShape(private val notch: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = with(density) { 28.dp.toPx() }
        val w = size.width
        val h = size.height
        if (notch <= 0.dp) return Outline.Rounded(RoundRect(0f, 0f, w, h, CornerRadius(r)))
        val n = with(density) { notch.toPx() }
        val f = with(density) { 12.dp.toPx() }
        val c = with(density) { 16.dp.toPx() }
        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(w - r, 0f)
            quadraticTo(w, 0f, w, r)
            lineTo(w, h - n - f)
            quadraticTo(w, h - n, w - f, h - n)
            lineTo(w - n + c, h - n)
            quadraticTo(w - n, h - n, w - n, h - n + c)
            lineTo(w - n, h - f)
            quadraticTo(w - n, h, w - n - f, h)
            lineTo(r, h)
            quadraticTo(0f, h, 0f, h - r)
            lineTo(0f, r)
            quadraticTo(0f, 0f, r, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

/** Entry of the course filter strip. A `null` [id] is the "all courses" tile. */
internal data class MensaCourseTile(
    val id: Int?,
    val label: String,
    val art: MensaArt?,
)

/** Row of rounded square tiles, one per course, to filter a dish list down to a single course. */
@Composable
internal fun MensaCourseStrip(
    tiles: List<MensaCourseTile>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        tiles.forEach { tile ->
            val selected = tile.id == selectedId
            Column(
                modifier = Modifier
                    .width(76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { haptic.tap(); onSelect(tile.id) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) scheme.primaryContainer else scheme.surfaceContainerHigh,
                    modifier = Modifier.size(64.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (tile.art != null) {
                            MensaPlate(art = tile.art, size = 48.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Restaurant,
                                contentDescription = null,
                                tint = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }
                }
                Text(
                    text = tile.label.toCourseTileLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) scheme.onSurface else scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Lays [dishes] out in two columns, leaving room above each card for its overflowing plate. */
@Composable
internal fun MensaDishGrid(
    dishes: List<MensaDish>,
    modifier: Modifier = Modifier,
    content: @Composable (MensaDish, Modifier) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        dishes.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                pair.forEach { dish -> content(dish, Modifier.weight(1f)) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** Turns the operator's ALL-CAPS dish names into sentence case, keeping the usual codes upper case. */
internal fun String.toDishDisplayName(): String {
    val trimmed = trim()
    if (trimmed.any { it.isLowerCase() }) return trimmed
    return trimmed.lowercase()
        .replaceFirstChar { it.uppercase() }
        .replace(CODES) { it.value.uppercase() }
}

private val CODES = Regex("\\b(s/g|s/glut|sg|igp|dop|u12|u6|u3)\\b")

/** Shortens the operator's course names ("Primi Piatti", "Pane e Prodotti da forno") to fit a tile. */
private fun String.toCourseTileLabel(): String =
    replace(" Piatti", "").substringBefore(",").substringBefore(" e ").trim()
