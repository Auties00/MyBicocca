package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
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

@Composable
internal fun MensaReservationsPage(
    reservations: Loadable<List<MensaReservation>>,
    status: SyncStatus,
    onOpenReservation: (MensaReservation) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val list = reservations.valueOrNull().orEmpty()
    val scheme = MaterialTheme.colorScheme
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

            list.isEmpty() -> EmptyState(
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                title = stringResource(R.string.mensa_orders_empty_title),
                body = stringResource(R.string.mensa_orders_empty_body),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
            )

            else -> Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                list.forEach { reservation ->
                    val statusColor = parseHexColorOrNull(reservation.statusColorHex) ?: scheme.primary
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = scheme.surfaceContainer,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = { haptic.tap(); onOpenReservation(reservation) })
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = reservation.canteenName ?: reservation.menuName ?: reservation.code,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = statusColor.copy(alpha = 0.16f),
                                ) {
                                    Text(
                                        text = reservation.statusDescription.ifBlank { reservation.code },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    )
                                }
                            }
                            Text(
                                text = listOfNotNull(
                                    reservation.rawDate.takeIf { it.isNotBlank() },
                                    reservation.shiftName,
                                    reservation.mealTypeName,
                                ).joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                            )
                            if (reservation.dishes.isNotEmpty()) {
                                Text(
                                    text = reservation.dishes.joinToString(", ") { it.name.toDishDisplayName() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MensaReservationDetailPage(
    reservation: MensaReservation,
    isCancelling: Boolean,
    onCancel: (MensaReservation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
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

        if (reservation.deletable && reservation.active) {
            Spacer(Modifier.height(10.dp))
            FilledTonalButton(
                onClick = { haptic.tap(); onCancel(reservation) },
                enabled = !isCancelling && LocalIsOnline.current,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.errorContainer,
                    contentColor = scheme.onErrorContainer,
                ),
            ) {
                if (isCancelling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = scheme.onErrorContainer,
                    )
                } else {
                    Icon(Icons.Outlined.Cancel, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.mensa_order_cancel), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Preview(name = "My orders", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 520)
@Composable
private fun ReservationsPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaReservationsPage(
            reservations = Loadable.Loaded(listOf(MensaPreviewOrders.reservation)),
            status = SyncStatus.Idle,
            onOpenReservation = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Order detail", widthDp = MENSA_PREVIEW_WIDTH, heightDp = MENSA_PREVIEW_HEIGHT)
@Composable
private fun ReservationDetailPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaReservationDetailPage(reservation = MensaPreviewOrders.reservation, isCancelling = false, onCancel = {})
    }
}
