package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
 * One dish at a time, in a horizontal carousel that keeps the neighbouring plates in view. The
 * dishes run course after course, so swiping past a course's last dish lands on the next course;
 * the course strip above follows the swipe and jumps when tapped.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
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
) {
    val dishes = request.dishes
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = request.startIndex) { dishes.size }
    val current = dishes[pagerState.currentPage.coerceIn(dishes.indices)]
    val density = LocalDensity.current
    val pageHeight = (LocalConfiguration.current.screenHeightDp * 0.78f).dp.coerceAtMost(700.dp)

    // The side plates slide in from the edges once, when the carousel opens.
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reveal.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) }

    LaunchedEffect(pagerState.currentPage) {
        val page = pagerState.currentPage
        for (index in (page - 1)..(page + 1)) dishes.getOrNull(index)?.let { onRequestDetail(it.id) }
    }

    val tiles = remember(dishes) {
        dishes.filter { it.courseId != null }
            .distinctBy { it.courseId }
            .map { MensaCourseTile(it.courseId, it.courseName.orEmpty(), MensaDishArtwork.forCourse(it.courseId)) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(pageHeight)
            .testTag(MensaTestTags.DISH_PAGER),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MensaCourseStrip(
            tiles = tiles,
            selectedId = current.courseId,
            onSelect = { courseId ->
                val target = dishes.indexOfFirst { it.courseId == courseId }
                if (target >= 0) scope.launch { pagerState.animateScrollToPage(target) }
            },
        )

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
                        sharedElement = page == pagerState.currentPage,
                    )
                }
            }
        }

        AnimatedContent(
            targetState = current,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "dish info",
            modifier = Modifier.weight(1f),
        ) { dish ->
            DishInfo(
                dish = dish,
                state = details[dish.id],
                onRetry = { onRetryDetail(dish.id) },
            )
        }

        AddToOrderBar(
            dish = current,
            quantity = cartDishes[current.id]?.quantity ?: 0,
            onAdd = { onAdd(current) },
            onDecrement = { onDecrement(current.id) },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DishInfo(
    dish: MensaDish,
    state: MensaDishDetailState?,
    onRetry: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = dish.name.toDishDisplayName(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        if (dish.dietaryBadges.isNotEmpty() || dish.caloricLabel != null) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                dish.caloricLabel?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                dish.dietaryBadges.forEach { AssistChip(onClick = {}, label = { Text(it.label) }) }
            }
        }

        when (state) {
            is MensaDishDetailState.Loaded -> {
                NutritionStats(state.detail)
                MensaDishInfoSections(info = state.detail)
            }

            MensaDishDetailState.Failed -> Column(
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

            else -> Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                SheetLoadingIndicator(
                    label = stringResource(R.string.mensa_loading_dish),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
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
