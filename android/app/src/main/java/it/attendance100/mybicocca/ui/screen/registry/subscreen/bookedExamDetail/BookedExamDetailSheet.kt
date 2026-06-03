package it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExamDetail

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.ui.component.exam.ExamDateBadge
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.component.ExamCallChips
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.countdownLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.creditsLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.displayTitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.examVariantLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.locationLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.partialLabel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DayOfMonthFormat = DateTimeFormatter.ofPattern("d")
private val MonthFormat = DateTimeFormatter.ofPattern("MMM", Locale.ITALIAN)
private val FullDateFormat = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")
private val ShortDateFormat = DateTimeFormatter.ofPattern("d MMMM", Locale.ITALIAN)
private val BookingDateFormat = DateTimeFormatter.ofPattern("d MMMM, HH:mm", Locale.ITALIAN)

// Everything about one booking, opened by tapping its card. Replaces the old
// full-screen detail destination.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookedExamDetailSheet(
    booking: BookedExam,
    today: LocalDate,
    isCancelling: Boolean,
    onCancel: (BookedExam) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    var confirming by remember { mutableStateOf(false) }

    val title = remember(booking.activityDescription) { booking.displayTitle() }
    val location = remember(booking.classroomDescription, booking.buildingDescription) {
        booking.locationLabel()
    }
    val countdown = remember(booking.examDateTime, today) { booking.countdownLabel(today) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = scheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp)
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val dayOfMonth = booking.examDateTime?.toLocalDate()?.format(DayOfMonthFormat)
                val month = booking.examDateTime?.toLocalDate()
                    ?.format(MonthFormat)?.uppercase(Locale.ITALIAN)
                if (dayOfMonth != null && month != null) {
                    ExamDateBadge(
                        dayOfMonth = dayOfMonth,
                        month = month,
                        modifier = Modifier.padding(end = 16.dp),
                        size = DpSize(width = 64.dp, height = 78.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                    )
                    ExamCallChips(
                        examType = booking.examType,
                        variantLabel = booking.examVariantLabel(),
                        partialLabel = booking.partialLabel(),
                        creditsLabel = booking.creditsLabel(),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                booking.examDateTime?.let { dt ->
                    IconRow(
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
                IconRow(
                    icon = Icons.Outlined.LocationOn,
                    label = "LUOGO",
                    value = location ?: "Aula da definire",
                )
                booking.examModeDescription?.let {
                    IconRow(
                        icon = Icons.Outlined.School,
                        label = "MODALITÀ",
                        value = it,
                    )
                }
                booking.position?.takeIf { it > 0 }?.let { p ->
                    IconRow(
                        icon = Icons.Outlined.ConfirmationNumber,
                        label = "POSIZIONE",
                        value = "${p}º a prenotarsi",
                    )
                }
                booking.bookingDate?.let { booked ->
                    IconRow(
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
                    IconRow(
                        icon = Icons.AutoMirrored.Outlined.Notes,
                        label = "NOTA",
                        value = it,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            ActionRow(
                canNavigate = booking.buildingDescription != null,
                isCancelling = isCancelling,
                canCancel = booking.studentId != null,
                onDirections = {
                    val query = directionsQuery(booking.buildingDescription)
                    if (query != null) {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query"))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        }
                    }
                },
                onCancel = { confirming = true },
            )
        }
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("Annullare la prenotazione?") },
            text = {
                Text(
                    "Stai per annullare la prenotazione a $title. " +
                            "Potrai prenotarti di nuovo finché le iscrizioni restano aperte.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirming = false
                        onCancel(booking)
                    },
                ) { Text("Annulla prenotazione") }
            },
            dismissButton = {
                TextButton(onClick = { confirming = false }) { Text("Indietro") }
            },
        )
    }
}

@Composable
private fun IconRow(icon: ImageVector, label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = scheme.surfaceContainerHigh,
            modifier = Modifier.size(44.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurface,
            )
        }
    }
}

// Connected button pair, same arrangement as the calendar event sheet: the primary
// "Indicazioni" half is wider, the destructive "Cancella" half smaller and error-tinted.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActionRow(
    canNavigate: Boolean,
    isCancelling: Boolean,
    canCancel: Boolean,
    onDirections: () -> Unit,
    onCancel: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val directionsBg = if (dark) scheme.primaryContainer else scheme.primary
    val directionsFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Button(
            onClick = onDirections,
            enabled = canNavigate,
            modifier = Modifier
                .weight(1.4f)
                .height(56.dp),
            shape = ButtonGroupDefaults.connectedLeadingButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = directionsBg,
                contentColor = directionsFg,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Indicazioni", fontWeight = FontWeight.SemiBold)
        }
        FilledTonalButton(
            onClick = onCancel,
            enabled = canCancel && !isCancelling,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.errorContainer,
                contentColor = scheme.onErrorContainer,
                disabledContainerColor = scheme.errorContainer.copy(alpha = 0.55f),
                disabledContentColor = scheme.onErrorContainer.copy(alpha = 0.55f),
            ),
        ) {
            if (isCancelling) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = scheme.onErrorContainer,
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
    }
}

// Esse3 buildings carry their street address ("U9 - Via dell'Innovazione, 10",
// "Edificio U24 - Viale Sarca 336, Milano "): keep the address part, make sure the
// city is there, and let Maps geocode it.
private fun directionsQuery(buildingDescription: String?): String? {
    val building = buildingDescription?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val withoutPrefix = building.removePrefix("Edificio ").removePrefix("edificio ").trim()
    val address = withoutPrefix.substringAfter(" - ", withoutPrefix).trim()
    val withCity = if (address.contains("milano", ignoreCase = true)) address else "$address, Milano"
    return Uri.encode(withCity)
}
