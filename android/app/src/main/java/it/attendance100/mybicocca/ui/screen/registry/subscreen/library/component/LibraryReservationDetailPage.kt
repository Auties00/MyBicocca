package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.QrCodeScanner
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.DEFAULT_LIBRARY_PRESENCE_WINDOW_MINUTES
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.isCheckInOpenAt
import it.attendance100.mybicocca.domain.model.library.isEndedAt
import it.attendance100.mybicocca.domain.model.library.mapBuildingCode
import it.attendance100.mybicocca.domain.model.library.zoneColor
import it.attendance100.mybicocca.ui.component.button.ConnectedButtonPair
import it.attendance100.mybicocca.ui.component.button.ConnectedButtonPairDefaults
import it.attendance100.mybicocca.ui.component.button.PairButtonLabel
import it.attendance100.mybicocca.ui.component.directory.segmentedShape
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.bookingDayLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.formattedSeatLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.reservationBuildingLine
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.zoneDisplayName
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")
private val TicketInk = Color(0xFF1A1617)
private val TicketMutedInk = Color(0xFF5C4B4E)

private data class BookingFactEntry(
    val icon: ImageVector,
    val label: String,
    val value: String,
)

/**
 * Detail page for a single synced booking: centered zone illustration (shared element with Home
 * and Bookings cards), white monospace code ticket when a code exists, connected fact rows,
 * optional "Mostra sulla mappa" action, and pinned "Cancella" / "Verifica presenza" buttons for
 * non-ended bookings. Ended bookings are view-only with no pinned actions.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LibraryReservationDetailPage(
    reservation: LibraryReservation,
    isCancelling: Boolean,
    onShowOnMap: (String) -> Unit,
    onVerifyPresence: () -> Unit,
    onCancel: (LibraryReservation) -> Unit,
    modifier: Modifier = Modifier,
    presenceWindowMinutes: Int = DEFAULT_LIBRARY_PRESENCE_WINDOW_MINUTES,
    now: LocalDateTime = LocalDateTime.now(AppClock.clock),
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()

    val ended = reservation.isEndedAt(now)
    val canCancel = !ended && reservation.cancellationToken != null
    val windowStart = reservation.start.minusMinutes(presenceWindowMinutes.toLong())
    val tooEarly = !ended && now.isBefore(windowStart)
    val canVerify = !ended && reservation.isCheckInOpenAt(now, presenceWindowMinutes)
    val presenceHint = when {
        tooEarly -> stringResource(
            R.string.library_verification_opens,
            windowStart.format(TimeFormat),
            presenceWindowMinutes,
        )
        else -> stringResource(R.string.library_qr_or_code)
    }

    val buildingLine = reservationBuildingLine(reservation)
    val libraryValue = listOfNotNull(reservation.libraryName, buildingLine).joinToString(" · ")
    val seatValue = listOfNotNull(
        reservation.formattedSeatLabel(),
        zoneDisplayName(reservation.zoneColor()),
    ).joinToString(" · ")
    val whenValue = "${bookingDayLabel(reservation.start.toLocalDate(), today = now.toLocalDate())} · " +
        "${reservation.start.format(TimeFormat)}–${reservation.end.format(TimeFormat)}"
    val buildingCode = reservation.mapBuildingCode()

    val facts = buildList {
        add(BookingFactEntry(Icons.Outlined.AccountBalance, stringResource(R.string.library_library), libraryValue))
        add(BookingFactEntry(Icons.Outlined.Chair, stringResource(R.string.library_seat), seatValue))
        add(BookingFactEntry(Icons.Outlined.Schedule, stringResource(R.string.library_when), whenValue))
        reservation.note?.takeIf { it.isNotBlank() }?.let { note ->
            add(BookingFactEntry(Icons.AutoMirrored.Outlined.Notes, stringResource(R.string.library_note), note))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .testTag(LibraryTestTags.RESERVATION_DETAIL_PAGE),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(reservation.zoneColor().artDrawable()),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .librarySharedElement(key = libraryBookingArtSharedKey(reservation.reservationId))
                        .size(
                            width = if (reservation.zoneColor().isChair) 200.dp else 176.dp,
                            height = if (reservation.zoneColor().isChair) 150.dp else 132.dp,
                        ),
                )
            }

            reservation.reservationCode?.takeIf { it.isNotBlank() }?.let { code ->
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    contentColor = TicketInk,
                    border = if (!dark) BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.55f)) else null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.library_booking_code),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = TicketMutedInk,
                        )
                        Text(
                            text = code,
                            style = MaterialTheme.typography.headlineLarge,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp,
                            color = TicketInk,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                facts.forEachIndexed { index, fact ->
                    BookingFactRow(
                        fact = fact,
                        isFirst = index == 0,
                        isLast = index == facts.lastIndex,
                    )
                }
            }

            if (buildingCode != null) {
                FilledTonalButton(
                    onClick = {
                        haptic.tap()
                        onShowOnMap(buildingCode)
                    },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag(LibraryTestTags.RESERVATION_DETAIL_MAP),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = scheme.surfaceContainerHighest,
                        contentColor = scheme.onSurface,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.library_show_on_map),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
        }

        if (!ended) {
            Text(
                text = presenceHint,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 10.dp),
            )
            BookingActionRow(
                canVerify = canVerify,
                canCancel = canCancel,
                isCancelling = isCancelling,
                onVerifyPresence = onVerifyPresence,
                onCancel = { onCancel(reservation) },
            )
        }
    }
}

@Composable
private fun BookingFactRow(
    fact: BookingFactEntry,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = segmentedShape(isFirst = isFirst, isLast = isLast),
        color = scheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(scheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = fact.icon,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = fact.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = fact.value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun BookingActionRow(
    canVerify: Boolean,
    canCancel: Boolean,
    isCancelling: Boolean,
    onVerifyPresence: () -> Unit,
    onCancel: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val cancelLabel = stringResource(R.string.library_cancel)
    val verifyLabel = stringResource(R.string.library_verify_presence)

    ConnectedButtonPair(
        secondary = {
            Row(
                modifier = Modifier.testTag(LibraryTestTags.RESERVATION_DETAIL_CANCEL),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isCancelling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = scheme.onErrorContainer,
                    )
                } else {
                    PairButtonLabel(
                        text = cancelLabel,
                        icon = Icons.Outlined.Cancel,
                    )
                }
            }
        },
        onSecondary = onCancel,
        primary = {
            Row(
                modifier = Modifier.testTag(LibraryTestTags.RESERVATION_DETAIL_VERIFY),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PairButtonLabel(
                    text = verifyLabel,
                    icon = Icons.Outlined.QrCodeScanner,
                )
            }
        },
        onPrimary = onVerifyPresence,
        secondaryEnabled = canCancel && !isCancelling && LocalIsOnline.current,
        primaryEnabled = canVerify && !isCancelling,
        secondaryColors = ConnectedButtonPairDefaults.destructiveColors(),
    )
}

@Preview(name = "ReservationDetail Active · Light", showBackground = true)
@Composable
private fun ReservationDetailActiveLightPreview() {
    val now = LocalDateTime.of(LibraryPreviewDefaults.Today, LibraryPreviewDefaults.NowTime)
    LibraryPreview(dark = false) {
        LibraryReservationDetailPage(
            reservation = LibraryPreviewDefaults.LiveReservation,
            isCancelling = false,
            onShowOnMap = {},
            onVerifyPresence = {},
            onCancel = {},
            now = now,
        )
    }
}

@Preview(
    name = "ReservationDetail Active · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun ReservationDetailActiveDarkPreview() {
    val now = LocalDateTime.of(LibraryPreviewDefaults.Today, LibraryPreviewDefaults.NowTime)
    LibraryPreview(dark = true) {
        LibraryReservationDetailPage(
            reservation = LibraryPreviewDefaults.LiveReservation,
            isCancelling = false,
            onShowOnMap = {},
            onVerifyPresence = {},
            onCancel = {},
            now = now,
        )
    }
}

@Preview(name = "ReservationDetail Ended · Light", showBackground = true)
@Composable
private fun ReservationDetailEndedLightPreview() {
    val now = LocalDateTime.of(LibraryPreviewDefaults.Today, LibraryPreviewDefaults.NowTime)
    LibraryPreview(dark = false) {
        LibraryReservationDetailPage(
            reservation = LibraryPreviewDefaults.PastReservations.first(),
            isCancelling = false,
            onShowOnMap = {},
            onVerifyPresence = {},
            onCancel = {},
            now = now,
        )
    }
}

@Preview(
    name = "ReservationDetail Ended No Code · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun ReservationDetailEndedDarkPreview() {
    val now = LocalDateTime.of(LibraryPreviewDefaults.Today, LibraryPreviewDefaults.NowTime)
    LibraryPreview(dark = true) {
        LibraryReservationDetailPage(
            reservation = LibraryPreviewDefaults.PastReservations.last(),
            isCancelling = false,
            onShowOnMap = {},
            onVerifyPresence = {},
            onCancel = {},
            now = now,
        )
    }
}
