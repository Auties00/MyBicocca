package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.ui.component.card.DetailFactCard
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FullDateFormat = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Management page for one synced booking: sectioned fact cards (reservation code, library, seat,
 * slot, note) over a connected action pair — "Verifica presenza", gated to the check-in window
 * with an explanatory hint, and "Cancella", which routes through the host's in-sheet confirmation
 * page.
 *
 * @param presenceWindowMinutes Minutes before the start when on-site validation opens
 * (server-driven; 10 is the observed default).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LibraryReservationDetailPage(
    reservation: LibraryReservation,
    isCancelling: Boolean,
    onVerifyPresence: () -> Unit,
    onCancel: (LibraryReservation) -> Unit,
    modifier: Modifier = Modifier,
    presenceWindowMinutes: Int = 10,
) {
    val scheme = MaterialTheme.colorScheme
    val canCancel = reservation.cancellationToken != null

    val context = LocalContext.current
    val now = remember(reservation) { LocalDateTime.now() }
    val windowStart = reservation.start.minusMinutes(presenceWindowMinutes.toLong())
    val tooEarly = now.isBefore(windowStart)
    val past = now.isAfter(reservation.end)
    val canVerify = !tooEarly && !past
    val presenceHint = when {
        tooEarly -> context.getString(
            R.string.library_verification_opens,
            windowStart.format(TimeFormat),
            presenceWindowMinutes
        )

        past -> context.getString(R.string.library_booking_complete)
        else -> context.getString(R.string.library_qr_or_code)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            reservation.reservationCode?.let { code ->
                DetailFactCard(Icons.Outlined.ConfirmationNumber, "CODICE PRENOTAZIONE", code, mono = true)
            }
            DetailFactCard(
                Icons.Outlined.LocalLibrary,
                "BIBLIOTECA",
                listOfNotNull(reservation.libraryName, reservation.librarySecondaryName).joinToString(" · "),
            )
            DetailFactCard(Icons.Outlined.Chair, "POSTO", reservation.seatName)
            DetailFactCard(
                Icons.Outlined.CalendarMonth,
                "QUANDO",
                reservation.start.format(FullDateFormat).replaceFirstChar { it.titlecase(Locale.ITALIAN) } +
                    " · ${reservation.start.format(TimeFormat)}–${reservation.end.format(TimeFormat)}",
            )
            reservation.note?.let { note ->
                DetailFactCard(Icons.AutoMirrored.Outlined.Notes, "NOTA", note)
            }

            Spacer(Modifier.height(4.dp))
        }

        Text(
            text = presenceHint,
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
        )
        ActionRow(
            canVerify = canVerify,
            canCancel = canCancel,
            isCancelling = isCancelling,
            onVerifyPresence = onVerifyPresence,
            onCancel = { onCancel(reservation) },
        )
    }
}

/**
 * Connected pair matching the map/buildings action style: the secondary "Cancella" leads on the
 * tonal container; the primary "Verifica presenza" trails wide on the brand fill.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActionRow(
    canVerify: Boolean,
    canCancel: Boolean,
    isCancelling: Boolean,
    onVerifyPresence: () -> Unit,
    onCancel: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary
    val haptic = rememberHapticManager()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilledTonalButton(
            onClick = { haptic.tap(); onCancel() },
            enabled = canCancel && !isCancelling && LocalIsOnline.current,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = ButtonGroupDefaults.connectedLeadingButtonShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.surfaceContainerHighest,
                contentColor = scheme.onSurface,
            ),
        ) {
            if (isCancelling) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = scheme.onSurface)
            } else {
                Text(stringResource(R.string.library_cancel), fontWeight = FontWeight.SemiBold)
            }
        }
        Button(
            onClick = { haptic.tap(); onVerifyPresence() },
            enabled = canVerify && !isCancelling,
            modifier = Modifier
                .weight(1.6f)
                .height(52.dp),
            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
            colors = ButtonDefaults.buttonColors(containerColor = brandBg, contentColor = brandFg),
        ) {
            Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.library_verify_presence), fontWeight = FontWeight.SemiBold)
        }
    }
}

