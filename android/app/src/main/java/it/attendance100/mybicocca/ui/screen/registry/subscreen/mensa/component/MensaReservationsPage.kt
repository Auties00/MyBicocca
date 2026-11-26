package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.foundation.background
import it.attendance100.mybicocca.ui.component.modal.SheetMotion
import it.attendance100.mybicocca.ui.component.modal.LocalSheetBackScrub
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Dp
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.mensa.isUpcoming
import it.attendance100.mybicocca.ui.component.input.SegmentedSwitch
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishArtwork
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.mensaDishDrawable
import java.time.LocalDate
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.button.ConnectedButtonPair
import it.attendance100.mybicocca.ui.component.button.ConnectedButtonPairDefaults
import it.attendance100.mybicocca.ui.component.button.PairButtonLabel
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.mensa.MensaReservation
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.card.DetailFactCard
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import java.net.URLEncoder
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

private val ReservationDateFormat: DateTimeFormatter
    @Composable
    @ReadOnlyComposable
    get() = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", currentLocale())

/** The two pages of the bookings screen, in pager order. */
internal enum class MensaOrdersTab { ACTIVE, HISTORY }

/** How many plates a booking shows before the rest become a count. */
private const val MAX_CARD_PLATES = 4

/**
 * The student's take-away bookings on two pages, swiped between or picked with the switch at the
 * bottom: those still to pick up, and the history. [pagerState] is the caller's, so the sheet
 * header can follow the page.
 */
@Composable
internal fun MensaReservationsPage(
    pagerState: PagerState,
    reservations: Loadable<List<MensaReservation>>,
    status: SyncStatus,
    pickupPoints: Map<Int, String>,
    onOpenReservation: (MensaReservation) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val list = reservations.valueOrNull().orEmpty()
    val today = LocalDate.now(AppClock.clock)
    val (active, past) = remember(list, today) {
        val (upcoming, rest) = list.partition { it.isUpcoming(today) }
        upcoming.sortedBy { it.date } to rest.sortedByDescending { it.date }
    }
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticManager()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .testTag(MensaTestTags.RESERVATIONS_PAGE),
    ) {
        when {
            list.isEmpty() && status is SyncStatus.Failed -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = stringResource(R.string.mensa_reservations_load_failed),
                body = stringResource(status.cause.friendlyMessage()),
                action = { RetryButton(onClick = onRetry) },
            )

            reservations !is Loadable.Loaded && list.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
            ) {
                SheetLoadingIndicator(
                    label = stringResource(R.string.mensa_loading_reservations),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            else -> {
                HorizontalPager(
                    state = pagerState,
                    flingBehavior = PagerDefaults.flingBehavior(
                        state = pagerState,
                        snapAnimationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                    ),
                    overscrollEffect = null,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                ) { page ->
                    when (MensaOrdersTab.entries[page]) {
                        MensaOrdersTab.ACTIVE -> ActiveOrders(active, today, pickupPoints, onOpenReservation)
                        MensaOrdersTab.HISTORY -> PastOrders(past, pickupPoints, onOpenReservation)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(100)),
                ) {
                    SegmentedSwitch(
                        options = MensaOrdersTab.entries,
                        selected = MensaOrdersTab.entries[pagerState.targetPage],
                        onSelected = { selected ->
                            haptic.tap()
                            scope.launch { pagerState.animateScrollToPage(selected.ordinal) }
                        },
                        label = {
                            stringResource(
                                if (it == MensaOrdersTab.ACTIVE) R.string.mensa_orders_tab_active else R.string.mensa_orders_tab_history,
                            )
                        },
                        borderColor = Color.White.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

/** The bookings still to pick up, soonest first, each with its bag, as on the hub. */
@Composable
private fun ActiveOrders(
    orders: List<MensaReservation>,
    today: LocalDate,
    pickupPoints: Map<Int, String>,
    onOpen: (MensaReservation) -> Unit,
) {
    if (orders.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.ShoppingBag,
            title = stringResource(R.string.mensa_orders_active_empty_title),
            body = stringResource(R.string.mensa_orders_active_empty_body),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp),
        )
        return
    }
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        orders.forEach { order ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = scheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { haptic.tap(); onOpen(order) }
                        .padding(start = 10.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MensaBookingBag(reservationId = order.id, width = 84.dp)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = mensaBookingDay(order, today),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = listOfNotNull(
                                order.shiftName?.takeIf { it.isNotBlank() } ?: order.mealTypeName,
                                pickupPoints[order.id],
                            ).joinToString("\n"),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                        OrderPlates(order, plateSize = 30.dp, modifier = Modifier.padding(top = 4.dp))
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = scheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** Past bookings, newest first under the month they were for: what was taken, when, and how it ended. */
@Composable
private fun PastOrders(
    orders: List<MensaReservation>,
    pickupPoints: Map<Int, String>,
    onOpen: (MensaReservation) -> Unit,
) {
    if (orders.isEmpty()) {
        EmptyState(
            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
            title = stringResource(R.string.mensa_orders_history_empty_title),
            body = stringResource(R.string.mensa_orders_history_empty_body),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp),
        )
        return
    }
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val locale = currentLocale()
    val months = remember(orders, locale) {
        orders.groupBy { order ->
            order.date?.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))?.replaceFirstChar { it.titlecase(locale) }.orEmpty()
        }
    }
    val dayFormat = remember(locale) { DateTimeFormatter.ofPattern("EEE d MMM", locale) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        months.forEach { (month, monthOrders) ->
            if (month.isNotEmpty()) {
                Text(
                    text = month,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp),
                )
            }
            monthOrders.forEach { order ->
                val statusColor = parseHexColorOrNull(order.statusColorHex) ?: scheme.primary
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = scheme.surfaceContainer,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { haptic.tap(); onOpen(order) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = order.date?.format(dayFormat)?.replaceFirstChar { it.titlecase(locale) } ?: order.rawDate,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            OrderPlates(order, plateSize = 26.dp)
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (order.statusDescription.isNotBlank()) {
                                Surface(shape = RoundedCornerShape(10.dp), color = statusColor.copy(alpha = 0.16f)) {
                                    Text(
                                        text = order.statusDescription.replaceFirstChar { it.titlecase(locale) },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    )
                                }
                            }
                            pickupPoints[order.id]?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** The booking's dishes as a row of overlapping plates, the ones past [MAX_CARD_PLATES] as a count. */
@Composable
private fun OrderPlates(order: MensaReservation, plateSize: Dp, modifier: Modifier = Modifier) {
    if (order.dishes.isEmpty()) return
    val shown = order.dishes.take(MAX_CARD_PLATES)
    val step = plateSize * 0.72f
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(width = step * (shown.size - 1) + plateSize, height = plateSize)) {
            shown.forEachIndexed { index, dish ->
                val art = remember(dish) { MensaDishArtwork.forDish(dish) }
                MensaPlate(
                    art = art,
                    size = plateSize,
                    drawable = mensaDishDrawable(dish.id, art.drawable),
                    modifier = Modifier.offset(x = step * index),
                )
            }
        }
        val more = order.dishes.size - shown.size
        if (more > 0) {
            Text(
                text = "+$more",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

/** When booking [booking] is for: today, tomorrow, or its weekday and date. */
@Composable
internal fun mensaBookingDay(booking: MensaReservation, today: LocalDate): String {
    val locale = currentLocale()
    return when (booking.date) {
        today -> stringResource(R.string.mensa_today)
        today.plusDays(1) -> stringResource(R.string.mensa_tomorrow)
        null -> booking.rawDate
        else -> booking.date.format(DateTimeFormatter.ofPattern("EEE d MMM", locale)).replaceFirstChar { it.titlecase(locale) }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MensaReservationDetailPage(
    reservation: MensaReservation,
    pickupPoint: String?,
    isCancelling: Boolean,
    onCancel: (MensaReservation) -> Unit,
    modifier: Modifier = Modifier,
    onShowOnMap: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val locale = currentLocale()
    val formattedDate = reservation.date?.format(ReservationDateFormat)?.replaceFirstChar { it.titlecase(locale) }
        ?: reservation.rawDate

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
            .testTag(MensaTestTags.RESERVATION_DETAIL_PAGE),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MensaBookingBagHero(reservationId = reservation.id, dishes = reservation.dishes)
            // Everything under the bag leaves together, so the page it opens onto never shows through it.
            Column(
                modifier = Modifier.leavingBelowBag(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.mensa_order_barcode_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF444444),
                        )
                        val encoded = URLEncoder.encode(reservation.barcodeValue, "UTF-8")
                        AsyncImage(
                            model = "https://unibicocca.compasscloud.it/api/utilities/barcode/render?data=$encoded&width=480&height=140",
                            contentDescription = reservation.barcodeValue,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCode2,
                                contentDescription = null,
                                tint = Color(0xFF1A1A1A),
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = reservation.barcodeValue,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A),
                            )
                        }
                    }
                }

                pickupPoint?.let { place ->
                    DetailFactCard(
                        icon = Icons.Outlined.Storefront,
                        label = stringResource(R.string.mensa_order_pickup_label),
                        value = place,
                    )
                }
                if (reservation.code.isNotBlank()) {
                    DetailFactCard(
                        icon = Icons.Outlined.ConfirmationNumber,
                        label = stringResource(R.string.mensa_order_code_label),
                        value = reservation.code,
                        mono = true,
                    )
                }
                DetailFactCard(
                    icon = Icons.Outlined.Restaurant,
                    label = stringResource(R.string.mensa_order_canteen_label),
                    value = listOfNotNull(reservation.canteenName, reservation.menuName, reservation.deliveryAddress).joinToString(" · "),
                )
                DetailFactCard(
                    icon = Icons.Outlined.CalendarMonth,
                    label = stringResource(R.string.mensa_order_date_label),
                    value = formattedDate,
                )
                reservation.shiftName?.let { shift ->
                    DetailFactCard(
                        icon = Icons.Outlined.Schedule,
                        label = stringResource(R.string.mensa_order_shift_label),
                        value = listOfNotNull(shift, reservation.mealTypeName).joinToString(" · "),
                    )
                }
                if (reservation.dishes.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = scheme.surfaceContainer,
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.mensa_order_dishes_label),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurfaceVariant,
                            )
                            reservation.dishes.forEach { dish ->
                                Text(
                                    text = "• ${dish.name.toDishDisplayName()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }

        val cancellable = reservation.deletable && reservation.active
        val cancel: @Composable RowScope.() -> Unit = {
            if (isCancelling) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = scheme.onErrorContainer)
            } else {
                PairButtonLabel(stringResource(R.string.mensa_order_cancel), Icons.Outlined.Cancel)
            }
        }
        val map: @Composable RowScope.() -> Unit = { PairButtonLabel(stringResource(R.string.mensa_order_show_on_map), Icons.Outlined.Place) }
        when {
            cancellable -> ConnectedButtonPair(
                secondary = cancel,
                onSecondary = { onCancel(reservation) },
                secondaryEnabled = !isCancelling && LocalIsOnline.current,
                secondaryColors = ConnectedButtonPairDefaults.destructiveColors(),
                primary = onShowOnMap?.let { map },
                onPrimary = { onShowOnMap?.invoke() },
                modifier = Modifier.padding(top = 10.dp).leavingBelowBag(),
            )
            onShowOnMap != null -> ConnectedButtonPair(
                secondary = map,
                onSecondary = onShowOnMap,
                secondaryColors = ConnectedButtonPairDefaults.primaryColors(),
                modifier = Modifier.padding(top = 10.dp).leavingBelowBag(),
            )
        }
    }
}

@Preview(name = "My orders, to pick up", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 520)
@Composable
private fun ReservationsPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaReservationsPage(
            pagerState = rememberPagerState { MensaOrdersTab.entries.size },
            reservations = Loadable.Loaded(listOf(MensaPreviewOrders.reservation)),
            status = SyncStatus.Idle,
            pickupPoints = mapOf(MensaPreviewOrders.reservation.id to "Mensa U12"),
            onOpenReservation = {},
            onRetry = {},
        )
    }
}

@Preview(name = "My orders, history", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 520)
@Composable
private fun ReservationsHistoryPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        val past = MensaPreviewOrders.reservation.copy(
            date = LocalDate.of(2026, 6, 5),
            active = false,
            deletable = false,
            provided = true,
            statusDescription = "ritirata",
            statusColorHex = "#42a5f5",
        )
        MensaReservationsPage(
            pagerState = rememberPagerState(initialPage = MensaOrdersTab.HISTORY.ordinal) { MensaOrdersTab.entries.size },
            reservations = Loadable.Loaded(listOf(past)),
            status = SyncStatus.Idle,
            pickupPoints = mapOf(past.id to "Mensa U12"),
            onOpenReservation = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Order detail", widthDp = MENSA_PREVIEW_WIDTH, heightDp = MENSA_PREVIEW_HEIGHT)
@Composable
private fun ReservationDetailPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaReservationDetailPage(reservation = MensaPreviewOrders.reservation, pickupPoint = "Mensa U12", isCancelling = false, onCancel = {}, onShowOnMap = {})
    }
}

/**
 * Leaving a booking, what is under its bag slides down out of the sheet and fades: with a back
 * gesture while the bag takes the dishes back, following the finger; otherwise with the page change.
 */
@Composable
private fun Modifier.leavingBelowBag(): Modifier {
    val visibility = LocalMensaShared.current?.visibility ?: return this
    // Scrubbed, it is gone by the time the bag sets off; otherwise it leaves with the page change,
    // so the page coming in fades up behind it rather than after it.
    val scrubbed = LocalSheetBackScrub.current()
    val slideMs = if (scrubbed) MENSA_BAG_RETURN_MS else SheetMotion.PAGE_MS
    val fadeMs = if (scrubbed) MENSA_BAG_RETURN_MS else SheetMotion.PAGE_MS * 2 / 3
    return with(visibility) {
        this@leavingBelowBag.animateEnterExit(
            enter = EnterTransition.None,
            exit = slideOutVertically(tween(slideMs, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(fadeMs)),
        )
    }
}
