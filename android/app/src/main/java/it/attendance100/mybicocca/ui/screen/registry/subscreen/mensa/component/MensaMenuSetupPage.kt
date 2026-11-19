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
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Restaurant
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
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuSummary
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

/** The step before the menu: which canteen, which day, lunch or dinner. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MensaMenuSetupPage(
    menus: Loadable<List<MensaMenuSummary>>,
    menusStatus: SyncStatus,
    selectedMenuId: Int?,
    selectedDate: LocalDate?,
    selectedMealTypeId: Int?,
    schedule: Loadable<Map<LocalDate, MensaDaySchedule>>,
    onSelectMenu: (Int) -> Unit,
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
    val meals = selectedDate?.let { scheduleMap[it] }?.mealTypes.orEmpty()

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
                    StepLabel(stringResource(R.string.mensa_setup_where))
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        menuList.forEach { menu ->
                            CanteenRow(
                                menu = menu,
                                selected = menu.id == selectedMenuId,
                                onClick = { haptic.tap(); onSelectMenu(menu.id) },
                            )
                        }
                    }

                    StepLabel(stringResource(R.string.mensa_setup_when))
                    DateStrip(dates = dates, selectedDate = selectedDate, onSelectDate = onSelectDate)

                    if (meals.size > 1) {
                        StepLabel(stringResource(R.string.mensa_setup_meal))
                        MealToggle(
                            mealTypes = meals.map { it.id to it.name },
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

@Composable
private fun CanteenRow(menu: MensaMenuSummary, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) scheme.primaryContainer else scheme.surfaceContainerHigh,
        contentColor = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = menu.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (menu.description.isNotBlank()) {
                    Text(
                        text = menu.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                imageVector = if (selected) Icons.Outlined.RadioButtonChecked else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun DateStrip(
    dates: List<LocalDate>,
    selectedDate: LocalDate?,
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
            selectedMenuId = 400,
            selectedDate = MensaPreviewData.today,
            selectedMealTypeId = 3,
            schedule = MensaPreviewData.schedule,
            onSelectMenu = {},
            onSelectDate = {},
            onSelectMealType = {},
            onConfirm = {},
            onRetryMenus = {},
        )
    }
}
