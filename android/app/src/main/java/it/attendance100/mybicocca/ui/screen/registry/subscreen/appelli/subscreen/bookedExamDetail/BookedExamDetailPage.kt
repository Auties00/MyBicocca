package it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.subscreen.bookedExamDetail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.School
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.ui.component.card.DetailFactCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.ext.countdownLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.ext.displayLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.ext.locationLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.state.ExamDocument
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FullDateFormat = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")
private val ShortDateFormat = DateTimeFormatter.ofPattern("d MMMM", Locale.ITALIAN)
private val BookingDateFormat = DateTimeFormatter.ofPattern("d MMMM, HH:mm", Locale.ITALIAN)

/**
 * Everything about one booking, paged inside the appelli sheet. The pinned morphing header
 * above carries the exam title, so the page is headerless: fact cards — appello date and
 * time with a countdown, place, mode (led by the exam type), queue position, booking date
 * with the cancellable-until cutoff, student note — over the action footer. Cancella
 * pushes the confirmation page in the parent pager; Ricevuta downloads the booking slip.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookedExamDetailPage(
    booking: BookedExam,
    today: LocalDate,
    isCancelling: Boolean,
    downloadingDocument: ExamDocument?,
    onRequestCancel: () -> Unit,
    onDownloadSlip: (BookedExam) -> Unit,
) {
    val location = remember(booking.classroomDescription, booking.buildingDescription) {
        booking.locationLabel()
    }
    val countdown = remember(booking.examDateTime, today) { booking.countdownLabel(today) }
    val modeValue = listOfNotNull(
        booking.examType.displayLabel(),
        booking.examModeDescription?.takeIf { it.isNotBlank() },
    ).joinToString(" · ").ifBlank { null }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp)
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            booking.examDateTime?.let { dt ->
                DetailFactCard(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "APPELLO",
                    value = buildString {
                        append(
                            dt.toLocalDate().format(FullDateFormat)
                                .replaceFirstChar { it.titlecase(Locale.ITALIAN) },
                        )
                        append(" · ore ")
                        append(dt.toLocalTime().format(TimeFormat))
                        countdown?.let { append(" ($it)") }
                    },
                )
            }
            DetailFactCard(
                icon = Icons.Outlined.LocationOn,
                label = "LUOGO",
                value = location ?: "Aula da definire",
            )
            modeValue?.let {
                DetailFactCard(
                    icon = Icons.Outlined.School,
                    label = "MODALITÀ",
                    value = it,
                )
            }
            booking.position?.takeIf { it > 0 }?.let { p ->
                DetailFactCard(
                    icon = Icons.Outlined.ConfirmationNumber,
                    label = "POSIZIONE",
                    value = "${p}º a prenotarsi",
                )
            }
            booking.bookingDate?.let { booked ->
                DetailFactCard(
                    icon = Icons.Outlined.EventAvailable,
                    label = "PRENOTAZIONE",
                    value = buildString {
                        append("Effettuata il ${booked.format(BookingDateFormat)}")
                        booking.cancellableUntil?.let {
                            append(" · annullabile fino al ${it.format(ShortDateFormat)}")
                        }
                    },
                )
            }
            booking.studentNote?.let {
                DetailFactCard(
                    icon = Icons.AutoMirrored.Outlined.Notes,
                    label = "NOTA",
                    value = it,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        ActionRow(
            isDownloadingSlip = downloadingDocument == ExamDocument.BookingSlip,
            isCancelling = isCancelling,
            canCancel = booking.studentId != null,
            onDownloadSlip = { onDownloadSlip(booking) },
            onCancel = onRequestCancel,
        )
    }
}

/**
 * Connected button pair, same arrangement as the calendar event sheet: the secondary
 * destructive "Cancella" half leads on the neutral tonal; the primary "Ricevuta" half
 * (the booking slip) trails wide on the brand fill. Whichever half is in flight swaps to
 * a spinner, and both disable offline.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActionRow(
    isDownloadingSlip: Boolean,
    isCancelling: Boolean,
    canCancel: Boolean,
    onDownloadSlip: () -> Unit,
    onCancel: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val slipBg = if (dark) scheme.primaryContainer else scheme.primary
    val slipFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary
    val isOnline = LocalIsOnline.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilledTonalButton(
            onClick = onCancel,
            enabled = canCancel && !isCancelling && isOnline,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = ButtonGroupDefaults.connectedLeadingButtonShape,
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
                Text("Cancella", fontWeight = FontWeight.SemiBold)
            }
        }
        Button(
            onClick = onDownloadSlip,
            enabled = !isDownloadingSlip && !isCancelling && isOnline,
            modifier = Modifier
                .weight(1.4f)
                .height(56.dp),
            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = slipBg,
                contentColor = slipFg,
            ),
        ) {
            if (isDownloadingSlip) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = slipFg,
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Ricevuta", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

