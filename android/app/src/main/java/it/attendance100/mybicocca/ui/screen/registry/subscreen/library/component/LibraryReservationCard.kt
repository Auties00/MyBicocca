package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.ui.component.card.BookingFooterEntry
import it.attendance100.mybicocca.ui.component.card.BookingSummaryCard
import it.attendance100.mybicocca.ui.component.card.relativeDayLabel
import java.time.format.DateTimeFormatter

private val DayOfMonthFormat = DateTimeFormatter.ofPattern("d")
private val MonthFormat: DateTimeFormatter
    @Composable
    @ReadOnlyComposable
    get() = DateTimeFormatter.ofPattern("MMM", currentLocale())
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/**
 * One synced seat booking, styled like the Appelli card: date tile, library + seat, tonal footer
 * (time + library location).
 */
@Composable
fun LibraryReservationCard(
    reservation: LibraryReservation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val day = relativeDayLabel(reservation.start.toLocalDate())
    val timeLabel = "$day · ${reservation.start.format(TimeFormat)}–${reservation.end.format(TimeFormat)}"
    val location = reservation.librarySecondaryName?.removeSuffix(" (Unimib)")?.trim()
    val haptic = rememberHapticManager()

    val footer = buildList {
        add(BookingFooterEntry(Icons.Outlined.Schedule, timeLabel))
        if (!location.isNullOrBlank()) {
            add(BookingFooterEntry(Icons.Outlined.LocationOn, location))
        }
    }

    BookingSummaryCard(
        title = reservation.libraryName,
        subtitle = reservation.seatName,
        dayOfMonth = reservation.start.format(DayOfMonthFormat),
        month = reservation.start.format(MonthFormat).uppercase(currentLocale()),
        footer = footer,
        onClick = { haptic.tap(); onClick() },
        modifier = modifier,
    )
}
