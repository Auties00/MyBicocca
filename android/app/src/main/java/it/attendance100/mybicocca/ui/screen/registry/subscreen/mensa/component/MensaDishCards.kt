package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishArtwork
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
import androidx.compose.ui.graphics.lerp
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
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme

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
            color = dishCardColor(allergenMatch),
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
    badgeFraction: Float = 0.3f,
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
                    .size(size * badgeFraction),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = MensaAllergenIcon,
                        contentDescription = stringResource(
                            if (contains) R.string.mensa_allergen_match_contains else R.string.mensa_allergen_match_trace,
                        ),
                        modifier = Modifier.size(size * badgeFraction * 0.6f),
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        tiles.forEach { tile ->
            MensaCourseTileButton(
                tile = tile,
                selected = tile.id == selectedId,
                onClick = { onSelect(tile.id) },
            )
        }
    }
}

/** One course in the strip: a rounded square with the course's plate and its name underneath. */
@Composable
internal fun MensaCourseTileButton(
    tile: MensaCourseTile,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    Column(
        modifier = modifier
            .width(76.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { haptic.tap(); onClick() },
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

/**
 * The card's surface, nudged toward red when the dish contains one of the user's allergens and
 * toward the accent for traces. It is a blend over the theme's own surface, so it follows every
 * palette and light or dark mode.
 */
@Composable
private fun dishCardColor(allergenMatch: MensaAllergenMatch): Color {
    val scheme = MaterialTheme.colorScheme
    // Dark surfaces hide a tint, so it is stronger there.
    val strength = if (LocalDarkTheme.current) 1.6f else 1f
    return when (allergenMatch) {
        MensaAllergenMatch.CONTAINS -> lerp(scheme.surfaceContainerHigh, scheme.error, 0.10f * strength)
        MensaAllergenMatch.TRACE -> lerp(scheme.surfaceContainerHigh, scheme.tertiary, 0.08f * strength)
        MensaAllergenMatch.NONE -> scheme.surfaceContainerHigh
    }
}

@Composable
private fun PreviewCard(dish: MensaDish, match: MensaAllergenMatch, qty: Int = 0, modifier: Modifier = Modifier) {
    MensaDishPlateCard(
        dish = dish,
        art = MensaDishArtwork.forDish(dish),
        allergenMatch = match,
        cartQty = qty,
        showAdd = true,
        onClick = {},
        onAdd = {},
        onDecrement = {},
        modifier = modifier,
    )
}

@Preview(name = "Dish card", widthDp = 420, heightDp = 520)
@Composable
private fun DishCardPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PreviewCard(MensaPreviewData.plainDish, MensaAllergenMatch.NONE, modifier = Modifier.weight(1f))
                PreviewCard(MensaPreviewData.containsDish, MensaAllergenMatch.CONTAINS, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PreviewCard(MensaPreviewData.traceDish, MensaAllergenMatch.TRACE, modifier = Modifier.weight(1f))
                PreviewCard(MensaPreviewData.plainDish, MensaAllergenMatch.NONE, qty = 2, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(name = "Plate badge", widthDp = 420, heightDp = 260)
@Composable
private fun PlateBadgePreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MensaPlateWithBadge(MensaPreviewData.containsDish, MensaArt.PASTA_TOMATO, MensaAllergenMatch.CONTAINS, size = 120.dp, badgeFraction = 0.25f)
            MensaPlateWithBadge(MensaPreviewData.traceDish, MensaArt.RISOTTO, MensaAllergenMatch.TRACE, size = 120.dp, badgeFraction = 0.25f)
        }
    }
}

@Preview(name = "Course tile", widthDp = 320, heightDp = 140)
@Composable
private fun CourseTilePreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MensaCourseTileButton(MensaCourseTile(null, "Tutti", null), selected = true, onClick = {})
            MensaCourseTileButton(MensaCourseTile(1, "Primi Piatti", MensaArt.PASTA_TOMATO), selected = false, onClick = {})
            MensaCourseTileButton(MensaCourseTile(2, "Secondi Piatti", MensaArt.MEAT), selected = true, onClick = {})
        }
    }
}

@Preview(name = "Course strip", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 140)
@Composable
private fun CourseStripPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaCourseStrip(
            tiles = listOf(
                MensaCourseTile(null, "Tutti", null),
                MensaCourseTile(1, "Primi Piatti", MensaArt.PASTA_TOMATO),
                MensaCourseTile(2, "Secondi Piatti", MensaArt.MEAT),
                MensaCourseTile(14, "Piatto unico", MensaArt.RISOTTO),
                MensaCourseTile(17, "Insalatone", MensaArt.SALAD),
                MensaCourseTile(4, "Contorni", MensaArt.VEGETABLES),
            ),
            selectedId = 1,
            onSelect = {},
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
