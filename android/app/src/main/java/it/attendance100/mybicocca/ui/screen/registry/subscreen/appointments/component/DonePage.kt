package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.ext.decodeQrDataUrl
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FullDateFormat = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Booking wizard step 3, the confirmation: a cookie-shaped check hero over the booking recap,
 * the reservation code in a tonal monospace chip and the check-in QR on a white surface.
 * "Fatto" rewinds the sheet to the reservations list, where the new booking is already
 * present.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DonePage(
    serviceName: String,
    reservation: AppointmentReservation?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 720.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(MaterialShapes.Cookie9Sided.toShape())
                    .background(scheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(56.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.appointments_confirmed_text),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = scheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = reservation?.let { booked ->
                    "$serviceName · ${booked.start.format(FullDateFormat)} alle ${booked.start.format(TimeFormat)}"
                } ?: serviceName,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            reservation?.code?.let { code ->
                Spacer(Modifier.height(16.dp))
                Surface(shape = MaterialTheme.shapes.large, color = scheme.surfaceContainerHigh) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.appointments_booking_code),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = code,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                        )
                    }
                }
            }
            val qr = remember(reservation?.qrCodeDataUrl) {
                reservation?.qrCodeDataUrl?.let(::decodeQrDataUrl)
            }
            if (qr != null) {
                Spacer(Modifier.height(16.dp))
                Surface(shape = MaterialTheme.shapes.extraLarge, color = Color.White) {
                    Image(
                        bitmap = qr,
                        contentDescription = "QR di check-in",
                        modifier = Modifier
                            .padding(12.dp)
                            .size(160.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.appointments_reservation_info),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = onDone,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ButtonDefaults.MediumContainerHeight)
                .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 24.dp),
            contentPadding = ButtonDefaults.MediumContentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (dark) scheme.primaryContainer else scheme.primary,
                contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
            ),
        ) {
            Text(
                stringResource(R.string.appointments_done),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
