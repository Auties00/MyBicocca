package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.domain.model.mensa.MensaDishDetail
import it.attendance100.mybicocca.domain.model.mensa.MensaNutritionRow
import it.attendance100.mybicocca.domain.model.mensa.allergenMatch
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaCartEntry
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishArtwork
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaDishDetailState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaDishPagerRequest
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

private val PlateSize = 196.dp

/**
 * The dish carousel on its own page (opened from the canteen catalogue): the course strip above the
 * carousel body. Opened from the menu, the strip belongs to the menu page instead, see
 * [MensaMenuPage], so that it stays put when the carousel opens.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MensaDishPagerPage(
    request: MensaDishPagerRequest,
    details: Map<Int, MensaDishDetailState>,
    cartDishes: Map<Int, MensaCartEntry>,
    allergenPreferences: MensaAllergenPreferences,
    onRequestDetail: (Int) -> Unit,
    onRetryDetail: (Int) -> Unit,
    onAdd: (MensaDish) -> Unit,
    onDecrement: (Int) -> Unit,
    modifier: Modifier = Modifier,
    canOrder: Boolean = true,
) {
    val dishes = request.dishes
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = request.startIndex) { dishes.size }
    val current = dishes[pagerState.currentPage.coerceIn(dishes.indices)]
    val tiles = remember(dishes) { courseTiles(dishes) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MensaCourseStrip(
            tiles = tiles,
            selectedId = current.courseId,
            onSelect = { courseId ->
                val target = dishes.indexOfFirst { it.courseId == courseId }
                if (target >= 0) scope.launch { pagerState.animateScrollToPage(target, animationSpec = mensaScrollSpec()) }
            },
        )
        MensaDishCarousel(
            request = request,
            pagerState = pagerState,
            details = details,
            cartDishes = cartDishes,
            allergenPreferences = allergenPreferences,
            onRequestDetail = onRequestDetail,
            onRetryDetail = onRetryDetail,
            onAdd = onAdd,
            onDecrement = onDecrement,
            modifier = Modifier.weight(1f),
            canOrder = canOrder,
        )
    }
}

/** One tile per course, in the order the dishes run. */
internal fun courseTiles(dishes: List<MensaDish>): List<MensaCourseTile> =
    dishes.filter { it.courseId != null }
        .distinctBy { it.courseId }
        .map { MensaCourseTile(it.courseId, it.courseName.orEmpty(), MensaDishArtwork.forCourse(it.courseId)) }

/**
 * One dish at a time, in a horizontal carousel that keeps the neighbouring plates in view. The
 * dishes run course after course, so swiping past a course's last dish lands on the next course.
 * The plates and each dish's title, notes and details are two pagers moved as one, so the whole page
 * follows the finger; the state is owned by the caller, which also drives it from its course strip.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MensaDishCarousel(
    request: MensaDishPagerRequest,
    pagerState: PagerState,
    details: Map<Int, MensaDishDetailState>,
    cartDishes: Map<Int, MensaCartEntry>,
    allergenPreferences: MensaAllergenPreferences,
    onRequestDetail: (Int) -> Unit,
    onRetryDetail: (Int) -> Unit,
    onAdd: (MensaDish) -> Unit,
    onDecrement: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onFocusMenuRow: (Int) -> Unit = {},
    canOrder: Boolean = true,
) {
    val dishes = request.dishes
    val current = dishes[pagerState.currentPage.coerceIn(dishes.indices)]
    val density = LocalDensity.current

    // The side plates slide in from the edges once, when the carousel opens.
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reveal.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) }

    LaunchedEffect(pagerState.currentPage) {
        val page = pagerState.currentPage
        for (index in (page - 1)..(page + 1)) dishes.getOrNull(index)?.let { onRequestDetail(it.id) }
    }

    // The menu underneath follows the carousel, so when the way back starts the dish being shown
    // already has its card in view for the plate to land on.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            request.menuRows?.getOrNull(page)?.let(onFocusMenuRow)
        }
    }

    // A light tick each time the swipe lands on another dish.
    val haptic = rememberHapticManager()
    LaunchedEffect(pagerState) {
        var last = pagerState.currentPage
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page != last) haptic.feather()
            last = page
        }
    }

    // The info pager mirrors the plates' position frame by frame; only the plates' pager reads touches.
    val infoState = rememberPagerState(initialPage = pagerState.currentPage) { dishes.size }
    LaunchedEffect(pagerState, infoState) {
        snapshotFlow { pagerState.currentPage to pagerState.currentPageOffsetFraction }
            .collect { (page, fraction) -> infoState.scrollToPage(page, fraction.coerceIn(-0.5f, 0.5f)) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(MensaTestTags.DISH_PAGER),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val pageWidth = PlateSize + 36.dp
            val sidePadding = ((maxWidth - pageWidth) / 2).coerceAtLeast(0.dp)
            val slideDistance = with(density) { 140.dp.toPx() }
            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fixed(pageWidth),
                contentPadding = PaddingValues(horizontal = sidePadding),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PlateSize + 20.dp),
            ) { page ->
                val dish = dishes[page]
                val distance = abs(pagerState.currentPage - page + pagerState.currentPageOffsetFraction).coerceIn(0f, 1f)
                val isStart = page == request.startIndex
                val direction = if (page < request.startIndex) -1f else 1f
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val scale = lerp(1f, 0.62f, distance)
                            scaleX = scale
                            scaleY = scale
                            alpha = lerp(1f, 0.55f, distance) * if (isStart) 1f else reveal.value
                            translationX = if (isStart) 0f else (1f - reveal.value) * direction * slideDistance
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    MensaPlateWithBadge(
                        dish = dish,
                        art = MensaDishArtwork.forDish(dish),
                        allergenMatch = dish.allergenMatch(allergenPreferences),
                        size = PlateSize,
                        badgeFraction = 0.25f,
                        sharedElement = page == pagerState.currentPage,
                        portionLabel = (details[dish.id] as? MensaDishDetailState.Loaded)
                            ?.detail?.netQuantityGrams?.takeIf { it > 0 }
                            ?.let { "${formatStat(it)}g" },
                    )
                }
            }
        }

        // The whole area swipes between dishes, not just the plates, and runs edge to edge; each
        // page keeps its own side padding and scrolls vertically under its pinned title. A page
        // shrinks and sinks as it leaves, like the plates above it.
        val layoutDirection = LocalLayoutDirection.current
        val sink = with(density) { 28.dp.toPx() }
        HorizontalPager(
            state = infoState,
            userScrollEnabled = false,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .weight(1f)
                .scrollable(
                    state = pagerState,
                    orientation = Orientation.Horizontal,
                    flingBehavior = PagerDefaults.flingBehavior(pagerState),
                    reverseDirection = ScrollableDefaults.reverseDirection(layoutDirection, Orientation.Horizontal, reverseScrolling = false),
                ),
        ) { page ->
            val dish = dishes[page]
            DishInfoPage(
                dish = dish,
                state = details[dish.id],
                allergenPreferences = allergenPreferences,
                onRetry = { onRetryDetail(dish.id) },
                modifier = Modifier.graphicsLayer {
                    val away = abs(pagerState.currentPage - page + pagerState.currentPageOffsetFraction).coerceIn(0f, 1f)
                    val scale = lerp(1f, 0.9f, away)
                    scaleX = scale
                    scaleY = scale
                    translationY = away * sink
                    alpha = lerp(1f, 0.55f, away)
                },
            )
        }

        if (canOrder) {
            AddToOrderBar(
                dish = current,
                quantity = cartDishes[current.id]?.quantity ?: 0,
                onAdd = { onAdd(current) },
                onDecrement = { onDecrement(current.id) },
            )
        }
    }
}

/**
 * One dish's page: the title pinned at the top, and under it what scrolls — the user's own
 * allergens first, then the numbers, the description and a way into everything else.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DishInfoPage(
    dish: MensaDish,
    state: MensaDishDetailState?,
    allergenPreferences: MensaAllergenPreferences,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val scroll = rememberScrollState()
    val haptic = rememberHapticManager()
    val divider by animateFloatAsState(if (scroll.value > 0) 1f else 0f, label = "title divider")
    var showDetails by remember { mutableStateOf(false) }
    val loaded = (state as? MensaDishDetailState.Loaded)?.detail

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Column(
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = dish.name.toDishDisplayName(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            // The calories are in the row below, so only the dietary badges are chips.
            if (dish.dietaryBadges.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dish.dietaryBadges.forEach { AssistChip(onClick = {}, label = { Text(it.label) }) }
                }
            }
        }
        HorizontalDivider(modifier = Modifier.graphicsLayer { alpha = divider })
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MensaDishFlags(dish = dish, info = loaded, allergenPreferences = allergenPreferences)
            when {
                loaded != null -> {
                    NutritionStats(loaded)
                    MensaDishDescription(loaded)
                    FilledTonalButton(
                        onClick = { haptic.tap(); showDetails = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            text = stringResource(R.string.mensa_dish_details_button),
                            modifier = Modifier.padding(start = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                state is MensaDishDetailState.Failed -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = scheme.onSurfaceVariant)
                    Text(
                        text = stringResource(R.string.mensa_dish_load_failed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    RetryButton(onClick = onRetry)
                }

                else -> Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    SheetLoadingIndicator(
                        label = stringResource(R.string.mensa_loading_dish),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }

    if (showDetails && loaded != null) {
        MensaDishDetailsSheet(
            dishName = dish.name.toDishDisplayName(),
            info = loaded,
            allergenPreferences = allergenPreferences,
            onDismiss = { showDetails = false },
        )
    }
}

/** Calories, protein, fat and energy at a glance, when the canteen publishes them for the dish. */
@Composable
private fun NutritionStats(detail: MensaDishDetail) {
    if (!detail.displayNutrition || detail.nutritionRows.isEmpty()) return
    val energy = detail.nutrient("energ", "calor")
    val protein = detail.nutrient("protein")
    val fat = detail.nutrient("grassi", "lipid", "fat")
    val stats = listOfNotNull(
        energy?.portionValue?.let { R.string.mensa_stat_calories to "${formatStat(it)} kcal" },
        protein?.portionValue?.let { R.string.mensa_stat_protein to "${formatStat(it)} g" },
        fat?.portionValue?.let { R.string.mensa_stat_fat to "${formatStat(it)} g" },
        energy?.portionKjValue?.let { R.string.mensa_stat_energy to "~${formatStat(it)} kJ" },
    )
    if (stats.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        stats.forEach { (label, value) ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun MensaDishDetail.nutrient(vararg names: String): MensaNutritionRow? =
    nutritionRows.firstOrNull { row -> names.any { row.name.contains(it, ignoreCase = true) } }

private fun formatStat(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AddToOrderBar(
    dish: MensaDish,
    quantity: Int,
    onAdd: () -> Unit,
    onDecrement: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()
    val bg = if (dark) scheme.primaryContainer else scheme.primary
    val fg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    if (quantity == 0) {
        Button(
            onClick = { haptic.tap(); onAdd() },
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 4.dp)
                .height(ButtonDefaults.MediumContainerHeight),
            colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
        ) {
            Icon(Icons.Outlined.AddShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                text = "  " + stringResource(R.string.mensa_dish_add_takeaway),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    } else {
        Surface(
            shape = RoundedCornerShape(50),
            color = bg,
            contentColor = fg,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 4.dp)
                .height(ButtonDefaults.MediumContainerHeight),
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StepButton(Icons.Outlined.Remove, fg) { haptic.tap(); onDecrement() }
                Text(
                    text = "$quantity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                StepButton(Icons.Outlined.Add, fg) { haptic.tap(); onAdd() }
            }
        }
    }
}

@Composable
private fun StepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    androidx.compose.material3.IconButton(onClick = onClick) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
    }
}

@Preview(name = "Dish carousel", widthDp = MENSA_PREVIEW_WIDTH, heightDp = MENSA_PREVIEW_HEIGHT)
@Composable
private fun DishCarouselPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaDishPagerPage(
            request = MensaDishPagerRequest(MensaPreviewData.dishes, startIndex = 1),
            details = mapOf(MensaPreviewData.containsDish.id to MensaDishDetailState.Loaded(MensaPreviewData.detail)),
            cartDishes = emptyMap(),
            allergenPreferences = MensaPreviewData.glutenSelected,
            onRequestDetail = {},
            onRetryDetail = {},
            onAdd = {},
            onDecrement = {},
        )
    }
}

@Preview(name = "Dish carousel · loading", widthDp = MENSA_PREVIEW_WIDTH, heightDp = MENSA_PREVIEW_HEIGHT)
@Composable
private fun DishCarouselLoadingPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaDishPagerPage(
            request = MensaDishPagerRequest(MensaPreviewData.dishes, startIndex = 0),
            details = emptyMap(),
            cartDishes = mapOf(MensaPreviewData.plainDish.id to MensaCartEntry(MensaPreviewData.plainDish, 2)),
            allergenPreferences = MensaPreviewData.glutenSelected,
            onRequestDetail = {},
            onRetryDetail = {},
            onAdd = {},
            onDecrement = {},
        )
    }
}
