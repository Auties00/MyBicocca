package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.AppointmentsTestTags

/**
 * Root page of the Appuntamenti sheet: this device's bookings, one [ReservationCard] each in
 * a scrollable column, over a pinned brand-filled "Prenota" footer that opens the desk
 * directory. With no bookings, the shared empty state renders height-bounded so it cannot
 * stretch the sheet to full height (same treatment as the ISEE sheet).
 */
@Composable
internal fun ReservationsPage(
    reservations: Loadable<List<AppointmentReservation>>,
    onOpenReservation: (AppointmentReservation) -> Unit,
    onPrenota: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val list = (reservations as? Loadable.Loaded)?.value.orEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 720.dp)
            .testTag(AppointmentsTestTags.RESERVATIONS_PAGE),
    ) {
        if (list.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(bottom = 4.dp)
                    .testTag(AppointmentsTestTags.RESERVATIONS_EMPTY),
            ) {
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.EventNote,
                    title = "Nessun appuntamento",
                    body = "Non hai ancora prenotato nessun appuntamento. Tocca Prenota per fissarne uno.",
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .testTag(AppointmentsTestTags.RESERVATIONS_LIST),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                list.forEach { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onClick = { onOpenReservation(reservation) },
                    )
                }
            }
        }

        PrenotaButton(
            onClick = onPrenota,
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp)
                .testTag(AppointmentsTestTags.PRENOTA_BUTTON),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PrenotaButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Button(
        onClick = onClick,
        shapes = ButtonDefaults.shapes(),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ButtonDefaults.MediumContainerHeight),
        contentPadding = ButtonDefaults.MediumContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (dark) scheme.primaryContainer else scheme.primary,
            contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
        ),
    ) {
        Icon(Icons.Outlined.EditCalendar, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Prenota", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}
