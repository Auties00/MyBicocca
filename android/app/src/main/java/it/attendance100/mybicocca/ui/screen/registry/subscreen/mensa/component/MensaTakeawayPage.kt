package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.mensa.MensaCartPricing
import it.attendance100.mybicocca.domain.model.mensa.MensaTakeawayService
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaCartEntry
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val TakeawayDateFormat: DateTimeFormatter
    @Composable
    @ReadOnlyComposable
    get() = DateTimeFormatter.ofPattern("EEE d MMM", currentLocale())

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MensaTakeawayPage(
    services: Loadable<List<MensaTakeawayService>>,
    servicesStatus: SyncStatus,
    selectedServiceId: Int?,
    selectedDate: LocalDate?,
    selectedMealTypeId: Int?,
    selectedPickupLocationId: Int?,
    selectedTimeSlotId: Int?,
    cartDishes: Map<Int, MensaCartEntry>,
    cartPricing: Loadable<MensaCartPricing>,
    orderNote: String,
    submitting: Boolean,
    onSelectService: (Int) -> Unit,
    onToggleFavorite: (MensaTakeawayService) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onSelectMealType: (Int) -> Unit,
    onSelectLocation: (Int) -> Unit,
    onSelectTimeSlot: (Int) -> Unit,
    onIncrementDish: (Int) -> Unit,
    onDecrementDish: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onBrowseDishes: () -> Unit,
    onSubmitOrder: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    val serviceList = services.valueOrNull().orEmpty()
    val currentService = serviceList.firstOrNull { it.id == selectedServiceId } ?: serviceList.firstOrNull()
    val dates = remember(currentService) { currentService?.scheduleByDate?.keys?.sorted().orEmpty() }
    val dayMeals = selectedDate?.let { currentService?.scheduleByDate?.get(it) }.orEmpty()
    val activeMeal = dayMeals.firstOrNull { it.id == selectedMealTypeId } ?: dayMeals.firstOrNull()
    val slots = activeMeal?.timeSlots.orEmpty()
    val today = remember { LocalDate.now(AppClock.clock) }
    val tomorrow = remember(today) { today.plusDays(1) }
    val dateFormat = TakeawayDateFormat
    val locale = currentLocale()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .testTag(MensaTestTags.TAKEAWAY_ORDER_PAGE),
    ) {
        when {
            serviceList.isEmpty() && servicesStatus is SyncStatus.Failed -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = stringResource(R.string.mensa_services_load_failed),
                body = stringResource(servicesStatus.cause.friendlyMessage()),
                action = { RetryButton(onClick = onRetry) },
            )

            services !is Loadable.Loaded && serviceList.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
            ) {
                SheetLoadingIndicator(
                    label = stringResource(R.string.mensa_loading_services),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (serviceList.isNotEmpty()) {
                        SectionLabel(stringResource(R.string.mensa_takeaway_service))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            serviceList.forEach { service ->
                                FilterChip(
                                    selected = service.id == currentService?.id,
                                    onClick = { haptic.tap(); onSelectService(service.id) },
                                    label = { Text(service.name) },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { haptic.tap(); onToggleFavorite(service) },
                                            modifier = Modifier.size(20.dp),
                                        ) {
                                            Icon(
                                                imageVector = if (service.isFavorite) Icons.Rounded.Star else Icons.Outlined.StarOutline,
                                                contentDescription = null,
                                                tint = if (service.isFavorite) scheme.primary else scheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }

                    if (currentService != null && currentService.pickupLocations.isNotEmpty()) {
                        SectionLabel(stringResource(R.string.mensa_takeaway_location))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            currentService.pickupLocations.forEach { loc ->
                                FilterChip(
                                    selected = loc.id == selectedPickupLocationId,
                                    onClick = { haptic.tap(); onSelectLocation(loc.id) },
                                    label = { Text(loc.name) },
                                )
                            }
                        }
                    }

                    if (dates.isNotEmpty()) {
                        SectionLabel(stringResource(R.string.mensa_takeaway_date))
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
                                    else -> date.format(dateFormat).replaceFirstChar { it.titlecase(locale) }
                                }
                                FilterChip(
                                    selected = date == selectedDate,
                                    onClick = { haptic.tap(); onSelectDate(date) },
                                    label = { Text(label) },
                                )
                            }
                        }
                    }

                    if (dayMeals.size > 1 && activeMeal != null) {
                        SectionLabel(stringResource(R.string.mensa_takeaway_meal_type))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            dayMeals.forEach { meal ->
                                FilterChip(
                                    selected = meal.id == activeMeal.id,
                                    onClick = { haptic.tap(); onSelectMealType(meal.id) },
                                    label = { Text(meal.name) },
                                )
                            }
                        }
                    }

                    if (slots.isNotEmpty()) {
                        SectionLabel(stringResource(R.string.mensa_takeaway_slot))
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            slots.forEach { slot ->
                                val selected = slot.id == selectedTimeSlotId
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (selected) scheme.primaryContainer else scheme.surfaceContainer,
                                    onClick = {
                                        if (slot.bookable) {
                                            haptic.tap()
                                            onSelectTimeSlot(slot.id)
                                        }
                                    },
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column {
                                            Text(
                                                text = slot.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                                color = if (slot.bookable) {
                                                    if (selected) scheme.onPrimaryContainer else scheme.onSurface
                                                } else {
                                                    scheme.onSurfaceVariant
                                                },
                                            )
                                            slot.affluencyCapacity?.let { cap ->
                                                val count = slot.reservationsCount ?: 0
                                                Text(
                                                    text = if (slot.bookable) "$count / $cap" else stringResource(R.string.mensa_takeaway_slot_full),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = scheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = null,
                                                tint = scheme.onPrimaryContainer,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SectionLabel(stringResource(R.string.mensa_takeaway_cart))
                    if (cartDishes.isEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = scheme.surfaceContainer,
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.mensa_takeaway_cart_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant,
                                )
                                FilledTonalButton(onClick = { haptic.tap(); onBrowseDishes() }) {
                                    Icon(Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(stringResource(R.string.mensa_takeaway_browse_dishes))
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            cartDishes.values.forEach { entry ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = scheme.surfaceContainer,
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = entry.dish.name.toDishDisplayName(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            FilledTonalIconButton(
                                                onClick = { haptic.tap(); onDecrementDish(entry.dish.id) },
                                                modifier = Modifier.size(32.dp),
                                            ) {
                                                Icon(Icons.Outlined.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                            Text(
                                                text = "${entry.quantity}",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            FilledTonalIconButton(
                                                onClick = { haptic.tap(); onIncrementDish(entry.dish.id) },
                                                modifier = Modifier.size(32.dp),
                                            ) {
                                                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    (cartPricing as? Loadable.Loaded)?.value?.let { pricing ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = scheme.surfaceContainerHigh,
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = stringResource(R.string.mensa_takeaway_price_total),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = "${pricing.currencySymbol} ${"%.2f".format(pricing.total)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.primary,
                                    )
                                }
                                pricing.cfpSum?.let { sum ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Eco,
                                            contentDescription = null,
                                            tint = scheme.tertiary,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Text(
                                            text = listOfNotNull("$sum ${pricing.cfpUnit.orEmpty()}".trim(), pricing.cfpEquivalentPhrase)
                                                .joinToString(" · "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = scheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (currentService?.userRequestsEnabled == true) {
                        OutlinedTextField(
                            value = orderNote,
                            onValueChange = onNoteChange,
                            enabled = !submitting,
                            label = { Text(stringResource(R.string.mensa_takeaway_note)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(16.dp),
                        )
                    }
                }

                currentService?.let { service ->
                    Text(
                        text = stringResource(
                            if (service.walletPayment) R.string.mensa_takeaway_pay_wallet else R.string.mensa_takeaway_pay_at_till,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp),
                    )
                }

                val canSubmit = LocalIsOnline.current &&
                    !submitting &&
                    currentService != null &&
                    selectedDate != null &&
                    activeMeal != null &&
                    selectedTimeSlotId != null &&
                    cartDishes.isNotEmpty()

                Button(
                    onClick = { haptic.tap(); onSubmitOrder() },
                    enabled = canSubmit,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = ButtonDefaults.MediumContainerHeight)
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandBg, contentColor = brandFg),
                ) {
                    if (submitting) {
                        LoadingIndicator(modifier = Modifier.size(30.dp), color = brandFg)
                    } else {
                        Text(
                            text = stringResource(R.string.mensa_takeaway_confirm),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp),
    )
}
