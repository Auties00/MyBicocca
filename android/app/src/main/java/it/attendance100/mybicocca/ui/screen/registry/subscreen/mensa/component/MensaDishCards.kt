package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import it.attendance100.mybicocca.domain.model.mensa.mensaDishNameWithoutPlaceNote
import it.attendance100.mybicocca.domain.model.mensa.MensaDishAvailability
import androidx.compose.ui.draw.alpha
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenMatch
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaArt
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishArtwork
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishDrawings
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags

/**
 * Whether a dish can be ordered where the user is ordering from. Provided by the Mensa page, which
 * knows the place and the menu; previews and screens outside an order leave every dish orderable.
 */
internal val LocalMensaDishAvailability = staticCompositionLocalOf<(MensaDish) -> MensaDishAvailability> {
    { MensaDishAvailability.Orderable }
}

/** Why a dish cannot be ordered here, or null when it can. */
@Composable
internal fun MensaDishAvailability.reason(): String? = when (this) {
    MensaDishAvailability.Orderable -> null
    is MensaDishAvailability.OnlyAt -> stringResource(R.string.mensa_dish_only_at, building)
    MensaDishAvailability.NotForTakeaway -> stringResource(R.string.mensa_dish_not_for_takeaway)
}

/** Top-down illustration of a dish. It is decorative, so it carries no description. */
@Composable
internal fun MensaPlate(
    art: MensaArt,
    size: Dp,
    modifier: Modifier = Modifier,
    @DrawableRes drawable: Int = art.drawable,
) {
    Image(
        painter = painterResource(drawable),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .clearAndSetSemantics { },
    )
}

/**
 * Salad, pasta and dessert overlapping, the pasta raised and larger in the middle: the canteen's
 * art on the sign-in page and the menu card. Everything scales with [middleSize].
 */
@Composable
internal fun MensaPlateTrio(middleSize: Dp, modifier: Modifier = Modifier) {
    val scale = middleSize / 100.dp
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy((-20).dp * scale)) {
        listOf(MensaArt.SALAD, MensaArt.PASTA_TOMATO, MensaArt.DESSERT).forEachIndexed { index, art ->
            val middle = index == 1
            MensaPlate(
                art = art,
                size = if (middle) middleSize else middleSize * 0.84f,
                modifier = Modifier.offset(y = if (middle) 0.dp else 12.dp * scale),
            )
        }
    }
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
    onEditQuantity: () -> Unit = {},
    isFavourite: Boolean = false,
    onToggleFavourite: (() -> Unit)? = null,
    singlePick: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    // A dish that cannot be ordered here stays on the menu, greyed, with the reason instead of the button.
    val unavailable = if (showAdd) LocalMensaDishAvailability.current(dish).reason() else null
    val canAdd = showAdd && unavailable == null
    // Pressing the order button swells it, and the notch in the card opens up to make room, as if
    // the button were pushing into the card.
    val addPress = remember { MutableInteractionSource() }
    val pressed by addPress.collectIsPressedAsState()
    val dragged by addPress.collectIsDraggedAsState()
    val impact by animateFloatAsState(
        targetValue = if (canAdd && (pressed || dragged)) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "add impact",
    )
    val cardShape = NotchedCardShape(notch = if (canAdd) 60.dp + 8.dp * impact else 0.dp)
    // The press is taken by the whole card, plate included, and drawn on the card itself.
    val press = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .alpha(if (unavailable != null) 0.5f else 1f)
            .clickable(interactionSource = press, indication = null) { haptic.tap(); onClick() },
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = PlateOverlap),
            shape = cardShape,
            color = dishCardColor(allergenMatch),
        ) {
            Column(
                modifier = Modifier
                    .indication(press, ripple())
                    .padding(start = 14.dp, end = 14.dp, top = PlateOverlap + 10.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = dish.name.toDishDisplayName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
                // Calories and the dietary label share a line; a row's shorter cards are stretched to
                // its tallest, so this stays right under the name whatever its length.
                Row(
                    modifier = if (showAdd) Modifier.heightIn(min = 36.dp) else Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                }
                if (unavailable != null) {
                    Text(
                        text = unavailable,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else if (showAdd) {
                    Spacer(Modifier.height(AddButtonSize - 36.dp))
                }
            }
        }

        MensaPlateWithBadge(
            dish = dish,
            art = art,
            allergenMatch = allergenMatch,
            size = PlateSize,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        if (canAdd) {
            MensaQuantityStepper(
                quantity = cartQty,
                onAdd = onAdd,
                onDecrement = onDecrement,
                onEdit = onEditQuantity,
                modifier = Modifier.align(Alignment.BottomEnd),
                size = AddButtonSize + 7.dp * impact,
                interactionSource = addPress,
                singlePick = singlePick,
            )
        }

        if (onToggleFavourite != null) {
            // In the card's own corner, beside the plate rather than on it.
            MensaFavouriteButton(
                isFavourite = isFavourite,
                onToggle = onToggleFavourite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = PlateOverlap + 2.dp, end = 2.dp),
            )
        }
    }
}

/**
 * The heart that adds a dish to the favourites or takes it off. It pops when it fills and
 * shrinks back when it empties, with a haptic for each.
 */
@Composable
internal fun MensaFavouriteButton(
    isFavourite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val scale = remember { Animatable(1f) }
    var toggled by remember { mutableIntStateOf(0) }
    LaunchedEffect(toggled) {
        if (toggled == 0) return@LaunchedEffect
        if (isFavourite) {
            scale.animateTo(1.35f, tween(110, easing = FastOutSlowInEasing))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        } else {
            scale.animateTo(0.75f, tween(90))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium))
        }
    }
    IconButton(
        onClick = {
            if (isFavourite) haptic.tap() else haptic.spring()
            onToggle()
            toggled++
        },
        modifier = modifier.testTag(MensaTestTags.FAVOURITE_TOGGLE),
    ) {
        Crossfade(targetState = isFavourite, animationSpec = tween(160), label = "favourite") { filled ->
            Icon(
                imageVector = if (filled) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(if (filled) R.string.mensa_favourite_remove else R.string.mensa_favourite_add),
                tint = if (filled) scheme.primary else scheme.onSurfaceVariant,
                modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value },
            )
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
    portionLabel: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = modifier.size(size)) {
        MensaPlate(
            art = art,
            size = size,
            modifier = Modifier.mensaPlateElement(dish.id, sharedElement),
            drawable = MensaDishDrawings.of(dish.id) ?: art.drawable,
        )
        if (portionLabel != null) {
            Surface(
                shape = CircleShape,
                color = scheme.surfaceContainerHighest,
                contentColor = scheme.onSurface,
                modifier = Modifier.align(Alignment.BottomStart),
            ) {
                Text(
                    text = portionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                )
            }
        }
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
        val f = with(density) { 16.dp.toPx() }
        val c = with(density) { 26.dp.toPx() }
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
            .padding(horizontal = 20.dp)
            .padding(end = 30.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
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
            .padding(end = if (selected) 10.dp else 0.dp)
            .width(76.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
            .clickable { haptic.tap(); onClick() }
            .padding(bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (selected) scheme.primaryContainer else scheme.surfaceContainerHigh,
            modifier = Modifier.height(70.dp).width(64.dp).padding(top = 6.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
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
    val trimmed = mensaDishNameWithoutPlaceNote(this)
    // Already in mixed case, or all lowercase ("bibita"): only the first letter needs a capital.
    if (trimmed.any { it.isLowerCase() }) return trimmed.replaceFirstChar { it.uppercase() }
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
