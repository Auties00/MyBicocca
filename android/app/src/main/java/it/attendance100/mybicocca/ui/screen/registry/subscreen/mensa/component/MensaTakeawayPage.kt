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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.Preview
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.button.ConnectedButtonPair
import it.attendance100.mybicocca.ui.component.button.PairButtonLabel
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.mensa.MensaCartPricing
import it.attendance100.mybicocca.domain.model.mensa.MensaPlace
import it.attendance100.mybicocca.domain.model.mensa.MensaTakeawayService
import it.attendance100.mybicocca.domain.model.mensa.timeLabel
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
import androidx.compose.material.icons.outlined.Delete

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MensaTakeawayPage(
    services: Loadable<List<MensaTakeawayService>>,
    servicesStatus: SyncStatus,
    selectedMenuId: Int?,
    selectedPlace: MensaPlace?,
    selectedDate: LocalDate?,
    selectedMealTypeId: Int?,
    selectedPickupLocationId: Int?,
    selectedTimeSlotId: Int?,
    cartDishes: Map<Int, MensaCartEntry>,
    cartPricing: Loadable<MensaCartPricing>,
    orderNote: String,
    submitting: Boolean,
    pricingError: String?,
    walletBalance: Double?,
    onSelectTimeSlot: (Int) -> Unit,
    onIncrementDish: (Int) -> Unit,
    onDecrementDish: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onBrowseDishes: () -> Unit,
    onSubmitOrder: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    singlePick: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    val serviceList = services.valueOrNull().orEmpty()
    // What is ordered was chosen on the way to the menu; only the time slot is left to pick here.
    val currentService = serviceList.firstOrNull { it.menuId == selectedMenuId }
    val dayMeals = selectedDate?.let { currentService?.scheduleByDate?.get(it) }.orEmpty()
    val activeMeal = dayMeals.firstOrNull { it.id == selectedMealTypeId }
    val slots = activeMeal?.timeSlots.orEmpty()
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
                    SectionLabel(stringResource(R.string.mensa_takeaway_pickup_summary))
                    val pickupName = currentService?.pickupLocations?.firstOrNull { it.id == selectedPickupLocationId }?.name
                    // The full date, never "tomorrow": an order placed around midnight must say which day it is for.
                    val dayLabel = selectedDate?.format(DateTimeFormatter.ofPattern("EEEE d MMMM", locale))
                        ?.replaceFirstChar { it.titlecase(locale) }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = scheme.surfaceContainerHigh,
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            SummaryLine(stringResource(R.string.mensa_takeaway_location), selectedPlace?.displayName ?: pickupName)
                            SummaryLine(stringResource(R.string.mensa_takeaway_date), dayLabel)
                            SummaryLine(stringResource(R.string.mensa_takeaway_meal_type), activeMeal?.name)
                            if (slots.size == 1) {
                                SummaryLine(stringResource(R.string.mensa_takeaway_slot), slots.first().timeLabel)
                            }
                        }
                    }

                    // Several slots (the bars' half hours) are a real choice; one slot is just the time.
                    if (slots.size > 1) {
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
                                                text = slot.timeLabel,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                                color = if (slot.bookable) {
                                                    if (selected) scheme.onPrimaryContainer else scheme.onSurface
                                                } else {
                                                    scheme.onSurfaceVariant
                                                },
                                            )
                                            if (!slot.bookable) {
                                                Text(
                                                    text = stringResource(R.string.mensa_takeaway_slot_full),
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
                                FilledTonalButton(onClick = { haptic.tap(); onBrowseDishes() }, enabled = !submitting) {
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
                                        if (singlePick) {
                                            FilledTonalIconButton(
                                                onClick = { haptic.tap(); onDecrementDish(entry.dish.id) },
                                                modifier = Modifier.size(36.dp),
                                            ) {
                                                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.mensa_quantity_remove), modifier = Modifier.size(18.dp))
                                            }
                                        } else Row(
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

                    (cartPricing as? Loadable.Loaded)?.value?.takeIf { currentService?.viewPrices != false }?.let { pricing ->
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

                // Wallet-paid services refuse an order the balance cannot cover, so it is said here first.
                val total = (cartPricing as? Loadable.Loaded)?.value?.total
                val walletShort = currentService?.walletPayment == true && walletBalance != null && total != null && total > walletBalance
                val problem = pricingError ?: if (walletShort) stringResource(R.string.mensa_takeaway_wallet_short, walletBalance ?: 0.0) else null
                problem?.let { MensaOrderProblem(text = it, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp)) }

                val canSubmit = problem == null &&
                    LocalIsOnline.current &&
                    !submitting &&
                    currentService != null &&
                    selectedDate != null &&
                    activeMeal != null &&
                    selectedTimeSlotId != null &&
                    cartDishes.isNotEmpty()

                ConnectedButtonPair(
                    secondary = { PairButtonLabel(stringResource(R.string.appointments_back)) },
                    onSecondary = onBack,
                    secondaryEnabled = !submitting,
                    primary = {
                        if (submitting) {
                            LoadingIndicator(modifier = Modifier.size(30.dp), color = brandFg)
                        } else {
                            PairButtonLabel(stringResource(R.string.mensa_takeaway_confirm))
                        }
                    },
                    onPrimary = onSubmitOrder,
                    primaryEnabled = canSubmit,
                    primaryColors = ButtonDefaults.buttonColors(containerColor = brandBg, contentColor = brandFg),
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
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

@Composable
private fun TakeawayPreviewPage(service: MensaTakeawayService, place: MensaPlace, walletBalance: Double?, pricingError: String? = null) {
    MensaTakeawayPage(
        services = Loadable.Loaded(listOf(service)),
        servicesStatus = SyncStatus.Idle,
        selectedMenuId = service.menuId,
        selectedPlace = place,
        selectedDate = MensaPreviewData.today,
        selectedMealTypeId = 3,
        selectedPickupLocationId = place.pickupLocation(service)?.id,
        selectedTimeSlotId = service.scheduleByDate.values.first().first().timeSlots.first().id,
        cartDishes = MensaPreviewOrders.cart,
        cartPricing = Loadable.Loaded(MensaPreviewOrders.pricing),
        orderNote = "",
        submitting = false,
        pricingError = pricingError,
        walletBalance = walletBalance,
        onSelectTimeSlot = {},
        onIncrementDish = {},
        onDecrementDish = {},
        onNoteChange = {},
        onBrowseDishes = {},
        onSubmitOrder = {},
        onBack = {},
        onRetry = {},
    )
}

@Preview(name = "Order · canteen", widthDp = MENSA_PREVIEW_WIDTH, heightDp = MENSA_PREVIEW_HEIGHT)
@Composable
private fun TakeawayCanteenPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) { TakeawayPreviewPage(MensaPreviewOrders.canteenService, MensaPlace.CANTEEN_U12, walletBalance = 12.4) }
}

@Preview(name = "Order · bar, wallet short", widthDp = MENSA_PREVIEW_WIDTH, heightDp = MENSA_PREVIEW_HEIGHT)
@Composable
private fun TakeawayBarPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) { TakeawayPreviewPage(MensaPreviewOrders.barService, MensaPlace.BAR_U12, walletBalance = 4.0) }
}
