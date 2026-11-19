package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenMatch
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import it.attendance100.mybicocca.domain.model.mensa.MensaDaySchedule
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.domain.model.mensa.allergenMatch
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaCartEntry
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishArtwork
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import java.time.LocalDate

/** The dishes of the chosen canteen, day and meal: a course strip above a grid of cards. */
@Composable
internal fun MensaMenuPage(
    selectedDate: LocalDate?,
    selectedMealTypeId: Int?,
    schedule: Loadable<Map<LocalDate, MensaDaySchedule>>,
    scheduleStatus: SyncStatus,
    cartDishes: Map<Int, MensaCartEntry>,
    allergenPreferences: MensaAllergenPreferences,
    onOpenDishes: (dishes: List<MensaDish>, startIndex: Int) -> Unit,
    onAddDishToCart: (MensaDish) -> Unit,
    onDecrementDish: (Int) -> Unit,
    onOpenTakeaway: () -> Unit,
    onRetrySchedule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheduleMap = schedule.valueOrNull().orEmpty()
    val availableMeals = selectedDate?.let { scheduleMap[it] }?.mealTypes.orEmpty()
    val activeMeal = availableMeals.firstOrNull { it.id == selectedMealTypeId } ?: availableMeals.firstOrNull()
    val cartCount = cartDishes.values.sumOf { it.quantity }
    var courseFilter by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .testTag(MensaTestTags.MENU_PAGE),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                scheduleMap.isEmpty() && scheduleStatus is SyncStatus.Failed -> SheetMessage(
                    icon = Icons.Outlined.CloudOff,
                    title = stringResource(R.string.mensa_schedule_load_failed),
                    body = stringResource(scheduleStatus.cause.friendlyMessage()),
                    action = { RetryButton(onClick = onRetrySchedule) },
                )

                schedule is Loadable.NotYetLoaded -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                ) {
                    SheetLoadingIndicator(
                        label = stringResource(R.string.mensa_loading_schedule),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                activeMeal == null || activeMeal.courses.all { it.dishes.isEmpty() } -> EmptyState(
                    icon = Icons.Outlined.Restaurant,
                    title = stringResource(R.string.mensa_no_dishes_for_day),
                    body = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                )

                else -> {
                    val courses = activeMeal.courses.filter { it.dishes.isNotEmpty() }
                    val activeFilter = courseFilter?.takeIf { id -> courses.any { it.id == id } }
                    val allLabel = stringResource(R.string.mensa_course_all)
                    val tiles = remember(courses, allLabel) {
                        listOf(MensaCourseTile(null, allLabel, null)) +
                            courses.map { MensaCourseTile(it.id, it.name, MensaDishArtwork.forCourse(it.id)) }
                    }
                    // The carousel walks every shown dish in menu order, so swiping past a course's last
                    // dish lands on the next course.
                    val shownDishes = remember(courses, allergenPreferences) {
                        courses.flatMap { course ->
                            course.dishes.filterNot {
                                allergenPreferences.hideMatchingDishes &&
                                    it.allergenMatch(allergenPreferences) != MensaAllergenMatch.NONE
                            }
                        }
                    }
                    MensaCourseStrip(
                        tiles = tiles,
                        selectedId = activeFilter,
                        onSelect = { courseFilter = it },
                    )
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        courses
                            .filter { activeFilter == null || it.id == activeFilter }
                            .forEach { course ->
                                val visible = course.dishes.filter { dish -> shownDishes.any { it.id == dish.id } }
                                if (visible.isNotEmpty()) {
                                    Text(
                                        text = course.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(start = 4.dp, top = 6.dp),
                                    )
                                    MensaDishGrid(dishes = visible) { dish, cardModifier ->
                                        MensaDishPlateCard(
                                            dish = dish,
                                            art = MensaDishArtwork.forDish(dish),
                                            allergenMatch = dish.allergenMatch(allergenPreferences),
                                            cartQty = cartDishes[dish.id]?.quantity ?: 0,
                                            showAdd = true,
                                            onClick = { onOpenDishes(shownDishes, shownDishes.indexOfFirst { it.id == dish.id }) },
                                            onAdd = { onAddDishToCart(dish) },
                                            onDecrement = { onDecrementDish(dish.id) },
                                            modifier = cardModifier,
                                        )
                                    }
                                }
                            }
                    }
                }
            }
        }

        if (cartCount > 0) {
            CartFooterBar(cartCount = cartCount, onOpenTakeaway = onOpenTakeaway)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CartFooterBar(
    cartCount: Int,
    onOpenTakeaway: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()
    val bg = if (dark) scheme.primaryContainer else scheme.primary
    val fg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Button(
        onClick = { haptic.tap(); onOpenTakeaway() },
        shapes = ButtonDefaults.shapes(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp)
            .heightIn(min = ButtonDefaults.MediumContainerHeight),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
    ) {
        Icon(Icons.Outlined.ShoppingBag, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = pluralStringResource(R.plurals.mensa_cart_footer, cartCount, cartCount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

internal fun parseHexColorOrNull(hex: String?): Color? {
    val cleaned = hex?.trim()?.removePrefix("#")?.takeIf { it.length == 6 || it.length == 8 } ?: return null
    val value = cleaned.toLongOrNull(16) ?: return null
    val argb = if (cleaned.length == 6) value or 0xFF000000L else value
    return Color(argb)
}
