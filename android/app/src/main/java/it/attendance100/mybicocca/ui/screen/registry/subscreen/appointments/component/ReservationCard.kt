package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.ui.component.card.BookingFooterEntry
import it.attendance100.mybicocca.ui.component.card.BookingSummaryCard
import it.attendance100.mybicocca.ui.component.card.relativeDayLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.ext.sectionLabelOf
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DayOfMonthFormat = DateTimeFormatter.ofPattern("d")
private val MonthFormat = DateTimeFormatter.ofPattern("MMM", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/** One desk booking, styled like the Appelli card: date tile, service and section, tonal when/where footer. */
@Composable
fun ReservationCard(
    reservation: AppointmentReservation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeLabel = "${relativeDayLabel(reservation.start.toLocalDate())} · ${reservation.start.format(TimeFormat)}"
    val section =
        stringResource(sectionLabelOf(reservation.serviceGroup ?: reservation.serviceName))

    val placeEntry = if (reservation.webConferenceUrl != null) {
        BookingFooterEntry(
            Icons.Outlined.Videocam,
            stringResource(R.string.appointments_video_call)
        )
    } else {
        BookingFooterEntry(
            icon = Icons.Outlined.LocationOn,
            label = reservation.areaName
                ?: stringResource(R.string.appointments_location_unassigned),
            muted = reservation.areaName == null,
        )
    }

    BookingSummaryCard(
        title = reservation.serviceName,
        subtitle = section,
        dayOfMonth = reservation.start.format(DayOfMonthFormat),
        month = reservation.start.format(MonthFormat).uppercase(Locale.ITALIAN),
        footer = listOf(BookingFooterEntry(Icons.Outlined.Schedule, timeLabel), placeEntry),
        onClick = onClick,
        modifier = modifier,
    )
}
