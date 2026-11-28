package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.compactLibraryName
import it.attendance100.mybicocca.domain.model.library.isInProgressAt
import it.attendance100.mybicocca.domain.model.library.zoneColor
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.bookingDayLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.formattedSeatLabel
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Canteen-style highlighted box for a booking that is in progress or inside its check-in window:
 * `primaryContainer`, zone drawing rising over the top-right edge, status label ("In corso" or
 * "Tra poco"), day/time/seat summary, and a quick "Verifica presenza" pill.
 */
@Composable
internal fun LiveReservationHeroCard(
    reservation: LibraryReservation,
    onClick: () -> Unit,
    onVerifyPresence: () -> Unit,
    modifier: Modifier = Modifier,
    now: LocalDateTime = LocalDateTime.now(AppClock.clock),
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()
    val pillBg = if (dark) scheme.onPrimaryContainer else scheme.surfaceContainerLowest
    val pillFg = if (dark) scheme.primaryContainer else scheme.onPrimaryContainer

    val statusLabel = if (reservation.isInProgressAt(now)) {
        stringResource(R.string.library_booking_in_progress)
    } else {
        stringResource(R.string.library_booking_starting_soon)
    }

    val chair = reservation.zoneColor().isChair

    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            onClick = { haptic.tap(); onClick() },
            shape = RoundedCornerShape(28.dp),
            color = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = if (chair) 112.dp else 128.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onPrimaryContainer.copy(alpha = 0.82f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = bookingDayLabel(reservation.start.toLocalDate(), today = now.toLocalDate()),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${reservation.start.format(TimeFormat)}–${reservation.end.format(TimeFormat)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = scheme.onPrimaryContainer.copy(alpha = 0.9f),
                        maxLines = 1,
                    )
                    Text(
                        text = "${reservation.formattedSeatLabel()} · ${reservation.compactLibraryName()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onPrimaryContainer.copy(alpha = 0.84f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(14.dp))

                Surface(
                    onClick = { haptic.tap(); onVerifyPresence() },
                    shape = CircleShape,
                    color = pillBg,
                    contentColor = pillFg,
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .testTag(LibraryTestTags.BOOKINGS_LIVE_VERIFY),
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.library_verify_presence),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        Image(
            painter = painterResource(reservation.zoneColor().artDrawable()),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = if (chair) 16.dp else (-8).dp)
                .librarySharedElement(key = libraryBookingArtSharedKey(reservation.reservationId))
                .size(
                    width = if (chair) 200.dp else 160.dp,
                    height = if (chair) 150.dp else 120.dp,
                ),
        )
    }
}

/**
 * Tonal card for a scheduled or past booking: leading zone drawing participating in the detail
 * shared-element transition, day/time/seat-and-library stack, and trailing chevron.
 */
@Composable
internal fun LibraryReservationCard(
    reservation: LibraryReservation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    now: LocalDateTime = LocalDateTime.now(AppClock.clock),
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()

    Surface(
        onClick = { haptic.tap(); onClick() },
        shape = RoundedCornerShape(22.dp),
        color = scheme.surfaceContainerHigh,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(
                painter = painterResource(reservation.zoneColor().artDrawable()),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .librarySharedElement(key = libraryBookingArtSharedKey(reservation.reservationId))
                    .size(width = 80.dp, height = 60.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = bookingDayLabel(reservation.start.toLocalDate(), today = now.toLocalDate()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${reservation.start.format(TimeFormat)}–${reservation.end.format(TimeFormat)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Text(
                    text = "${reservation.formattedSeatLabel()} · ${reservation.compactLibraryName()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Preview(name = "Reservation Cards · Light", showBackground = true)
@Composable
private fun ReservationCardsLightPreview() {
    val now = LocalDateTime.of(LibraryPreviewDefaults.Today, LibraryPreviewDefaults.NowTime)
    LibraryPreview(dark = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LiveReservationHeroCard(
                reservation = LibraryPreviewDefaults.LiveReservation,
                onClick = {},
                onVerifyPresence = {},
                now = now,
            )
            LibraryReservationCard(
                reservation = LibraryPreviewDefaults.Reservations[1],
                onClick = {},
                now = now,
            )
            LibraryReservationCard(
                reservation = LibraryPreviewDefaults.Reservations[2],
                onClick = {},
                now = now,
            )
        }
    }
}

@Preview(
    name = "Reservation Cards · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun ReservationCardsDarkPreview() {
    val now = LocalDateTime.of(LibraryPreviewDefaults.Today, LibraryPreviewDefaults.NowTime)
    LibraryPreview(dark = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LiveReservationHeroCard(
                reservation = LibraryPreviewDefaults.LiveReservation,
                onClick = {},
                onVerifyPresence = {},
                now = now,
            )
            LibraryReservationCard(
                reservation = LibraryPreviewDefaults.Reservations[1],
                onClick = {},
                now = now,
            )
            LibraryReservationCard(
                reservation = LibraryPreviewDefaults.Reservations[2],
                onClick = {},
                now = now,
            )
        }
    }
}
