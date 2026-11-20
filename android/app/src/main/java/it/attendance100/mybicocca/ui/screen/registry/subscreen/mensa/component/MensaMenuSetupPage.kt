package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.runtime.Composable
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.mensa.MensaDaySchedule
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuIdentity
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuSummary
import it.attendance100.mybicocca.domain.model.mensa.MensaPlace
import it.attendance100.mybicocca.domain.model.mensa.identity
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaMenuCard
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DateChipFormatter: DateTimeFormatter
    @Composable
    @ReadOnlyComposable
    get() = DateTimeFormatter.ofPattern("EEE d MMM", currentLocale())

/** The step before the menu: which day, and lunch or dinner where the place serves both. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MensaMenuSetupPage(
    menus: Loadable<List<MensaMenuSummary>>,
    menusStatus: SyncStatus,
    selectedPlace: MensaPlace?,
    selectedMenuId: Int?,
    selectedDate: LocalDate?,
    selectedMealTypeId: Int?,
    schedule: Loadable<Map<LocalDate, MensaDaySchedule>>,
    bookableDates: Set<LocalDate>,
    onChangePlace: () -> Unit,
    onChangeMenu: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onSelectMealType: (Int) -> Unit,
    onConfirm: () -> Unit,
    onRetryMenus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    val menuList = menus.valueOrNull().orEmpty()
    val scheduleMap = schedule.valueOrNull().orEmpty()
    val currentMenu = menuList.firstOrNull { it.id == selectedMenuId }
    val dates = remember(scheduleMap, currentMenu) {
        if (scheduleMap.isNotEmpty()) scheduleMap.keys.sorted() else currentMenu?.activeDays.orEmpty()
    }
    // Until the day's schedule arrives the menu's own meals stand in, so a canteen that serves
    // dinner shows both buttons straight away instead of growing them a moment later.
    val dayMeals = selectedDate?.let { scheduleMap[it] }?.mealTypes?.map { Triple(it.id, it.name, it.order) }
    val allMeals = dayMeals ?: currentMenu?.mealTypes?.map { Triple(it.id, it.name, it.order) }.orEmpty()
    val meals = selectedPlace?.mealsOf(allMeals, { it.third }) ?: allMeals

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .testTag(MensaTestTags.MENU_SETUP_PAGE),
    ) {
        when {
            menuList.isEmpty() && menusStatus is SyncStatus.Failed -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = stringResource(R.string.mensa_menus_load_failed),
                body = stringResource(menusStatus.cause.friendlyMessage()),
                action = { RetryButton(onClick = onRetryMenus) },
            )

            menus is Loadable.NotYetLoaded && menuList.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
            ) {
                SheetLoadingIndicator(
                    label = stringResource(R.string.mensa_loading_menus),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            menuList.isEmpty() -> EmptyState(
                icon = Icons.Outlined.Restaurant,
                title = stringResource(R.string.mensa_no_menus),
                body = "",
                modifier = Modifier.padding(24.dp),
            )

            else -> {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    selectedPlace?.let { place ->
                        ChosenRow(
                            identity = place.identity(),
                            label = stringResource(R.string.mensa_setup_place),
                            name = place.displayName,
                            tag = MensaTestTags.MENU_SETUP_CHANGE_PLACE,
                            onChange = { haptic.tap(); onChangePlace() },
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                    if (selectedPlace?.hasMenuChoice != false) {
                        currentMenu?.let { menu ->
                            ChosenRow(
                                identity = menu.identity(),
                                label = stringResource(R.string.mensa_setup_menu),
                                name = menu.identity().displayName,
                                tag = MensaTestTags.MENU_SETUP_CHANGE,
                                onChange = { haptic.tap(); onChangeMenu() },
                                modifier = Modifier.padding(horizontal = 20.dp),
                            )
                        }
                    }

                    StepLabel(stringResource(R.string.mensa_setup_when))
                    DateStrip(
                        dates = dates,
                        selectedDate = selectedDate,
                        bookableDates = bookableDates,
                        onSelectDate = onSelectDate,
                    )
                    if (bookableDates.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.mensa_setup_bookable_hint),
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    }

                    if (meals.size > 1) {
                        StepLabel(stringResource(R.string.mensa_setup_meal))
                        MealToggle(
                            mealTypes = meals.map { it.first to it.second },
                            selectedMealTypeId = selectedMealTypeId,
                            onSelectMealType = onSelectMealType,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }

                Button(
                    onClick = { haptic.tap(); onConfirm() },
                    enabled = selectedMenuId != null && selectedDate != null && schedule is Loadable.Loaded,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp)
                        .heightIn(min = ButtonDefaults.MediumContainerHeight)
                        .testTag(MensaTestTags.MENU_SETUP_CONFIRM),
                    colors = ButtonDefaults.buttonColors(containerColor = brandBg, contentColor = brandFg),
                ) {
                    Text(
                        text = stringResource(R.string.mensa_setup_confirm),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp),
    )
}

/** A choice made in an earlier step, with a way back to change it. */
@Composable
private fun ChosenRow(
    identity: MensaMenuIdentity,
    label: String,
    name: String,
    tag: String,
    onChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = scheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onChange)
                .testTag(tag)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MensaMenuCard(identity, Modifier.height(64.dp), showLabels = false, corner = 0.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(onClick = onChange) { Text(stringResource(R.string.mensa_setup_change)) }
        }
    }
}

@Composable
private fun DateStrip(
    dates: List<LocalDate>,
    selectedDate: LocalDate?,
    bookableDates: Set<LocalDate>,
    onSelectDate: (LocalDate) -> Unit,
) {
    val haptic = rememberHapticManager()
    val today = remember { LocalDate.now(AppClock.clock) }
    val tomorrow = remember(today) { today.plusDays(1) }
    val formatter = DateChipFormatter
    val locale = currentLocale()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        dates.forEach { date ->
            val label = when (date) {
                today -> stringResource(R.string.mensa_today)
                tomorrow -> stringResource(R.string.mensa_tomorrow)
                else -> date.format(formatter).replaceFirstChar { it.titlecase(locale) }
            }
            FilterChip(
                selected = date == selectedDate,
                onClick = { haptic.tap(); onSelectDate(date) },
                label = { Text(label) },
                leadingIcon = if (date in bookableDates) {
                    { Icon(Icons.Outlined.ShoppingBag, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else {
                    null
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MealToggle(
    mealTypes: List<Pair<Int, String>>,
    selectedMealTypeId: Int?,
    onSelectMealType: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val activeBg = if (dark) scheme.primaryContainer else scheme.primary
    val activeFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        mealTypes.forEachIndexed { index, (id, name) ->
            val shape = when {
                mealTypes.size == 1 -> RoundedCornerShape(24.dp)
                index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShape
                index == mealTypes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShape
                else -> RoundedCornerShape(8.dp)
            }
            if (id == selectedMealTypeId) {
                Button(
                    onClick = { haptic.tap(); onSelectMealType(id) },
                    modifier = Modifier.weight(1f),
                    shape = shape,
                    colors = ButtonDefaults.buttonColors(containerColor = activeBg, contentColor = activeFg),
                ) {
                    Text(name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            } else {
                FilledTonalButton(
                    onClick = { haptic.tap(); onSelectMealType(id) },
                    modifier = Modifier.weight(1f),
                    shape = shape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = scheme.surfaceContainerHighest,
                        contentColor = scheme.onSurface,
                    ),
                ) {
                    Text(name, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Preview(name = "Menu setup", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 640)
@Composable
private fun MenuSetupPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaMenuSetupPage(
            menus = Loadable.Loaded(MensaPreviewData.menus),
            menusStatus = SyncStatus.Idle,
            selectedPlace = MensaPlace.CANTEEN_U12,
            selectedMenuId = 400,
            selectedDate = MensaPreviewData.today,
            selectedMealTypeId = 3,
            schedule = MensaPreviewData.schedule,
            bookableDates = setOf(MensaPreviewData.today),
            onChangePlace = {},
            onChangeMenu = {},
            onSelectDate = {},
            onSelectMealType = {},
            onConfirm = {},
            onRetryMenus = {},
        )
    }
}
