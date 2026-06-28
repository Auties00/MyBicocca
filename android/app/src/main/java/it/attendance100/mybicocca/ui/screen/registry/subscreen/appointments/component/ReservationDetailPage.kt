package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.ui.component.card.DetailFactCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.ext.decodeQrDataUrl
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FullDateFormat = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.getDefault())
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Management page for one desk booking, hosted inside the Appuntamenti sheet pager (the sheet
 * header carries the title): check-in QR, reservation code, recap fact cards, then the action
 * row pinned at the bottom. The QR stays on a white surface in both themes so scanners read
 * it reliably. [onCancel] only requests the cancellation — the hosting sheet shows the
 * in-sheet confirm page.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ReservationDetailPage(
    reservation: AppointmentReservation,
    isCancelling: Boolean,
    onCancel: (AppointmentReservation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 720.dp)
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
        ) {
            val qr = remember(reservation.qrCodeDataUrl) {
                reservation.qrCodeDataUrl?.let(::decodeQrDataUrl)
            }
            if (qr != null) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Surface(shape = MaterialTheme.shapes.extraLarge, color = Color.White) {
                        Image(
                            bitmap = qr,
                            contentDescription = stringResource(R.string.appointments_qr_checkin),
                            modifier = Modifier
                                .padding(16.dp)
                                .size(200.dp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Text(
                text = stringResource(R.string.appointments_reservation_code, reservation.code),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailFactCard(
                    icon = Icons.Outlined.CalendarMonth,
                    label = stringResource(R.string.appointments_appointment),
                    value = buildString {
                        append(
                            reservation.start.format(FullDateFormat)
                                .replaceFirstChar { it.titlecase(Locale.getDefault()) },
                        )
                        append(" · ore ")
                        append(reservation.start.format(TimeFormat))
                    },
                )
                if (reservation.webConferenceUrl != null) {
                    DetailFactCard(
                        icon = Icons.Outlined.Videocam,
                        label = stringResource(R.string.appointments_modality),
                        value = stringResource(R.string.appointments_video_call),
                    )
                } else {
                    DetailFactCard(
                        icon = Icons.Outlined.LocationOn,
                        label = stringResource(R.string.appointments_location),
                        value = listOfNotNull(reservation.areaName, reservation.areaAddress)
                            .joinToString(" · ")
                            .ifBlank { stringResource(R.string.appointments_location_unassigned) },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        ActionRow(
            webConferenceUrl = reservation.webConferenceUrl,
            isCancelling = isCancelling,
            onJoinCall = {
                reservation.webConferenceUrl?.let { url ->
                    CustomTabsIntent.Builder().setShowTitle(true).build()
                        .launchUrl(context, url.toUri())
                }
            },
            onCancel = { onCancel(reservation) },
        )
    }
}

/**
 * Bottom action row. For video-call appointments, a connected pair where the neutral tonal
 * "Annulla" leads and the wider brand-filled "Partecipa" trails; without a call, the cancel
 * button stands alone at full width.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActionRow(
    webConferenceUrl: String?,
    isCancelling: Boolean,
    onJoinCall: () -> Unit,
    onCancel: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val joinBg = if (dark) scheme.primaryContainer else scheme.primary
    val joinFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary
    val hasCall = webConferenceUrl != null
    val haptic = rememberHapticManager()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilledTonalButton(
            onClick = { haptic.tap(); onCancel() },
            enabled = !isCancelling && LocalIsOnline.current,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = if (hasCall) {
                ButtonGroupDefaults.connectedLeadingButtonShape
            } else {
                ButtonDefaults.filledTonalShape
            },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.surfaceContainerHighest,
                contentColor = scheme.onSurface,
                disabledContainerColor = scheme.surfaceContainerHighest.copy(alpha = 0.55f),
                disabledContentColor = scheme.onSurface.copy(alpha = 0.55f),
            ),
        ) {
            if (isCancelling) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = scheme.onSurface,
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.EventBusy,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.appointments_cancel), fontWeight = FontWeight.SemiBold)
            }
        }
        if (hasCall) {
            Button(
                onClick = { haptic.tap(); onJoinCall() },
                enabled = !isCancelling,
                modifier = Modifier
                    .weight(1.4f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = joinBg,
                    contentColor = joinFg,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Videocam,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.appointments_join), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
