package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.exam.ExamCallDetail
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingSheetStep
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingTarget
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FullDateFormat = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")
private val WindowFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)

// Booking detail/confirm content, without a ModalBottomSheet wrapper — the host embeds
// it (e.g. the bookable modal swaps between its list and this). `onBackToList` returns
// from the Info step to whatever the host showed before.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSheetContent(
    target: BookingTarget,
    detail: Loadable<ExamCallDetail>,
    syncStatus: SyncStatus,
    step: BookingSheetStep,
    bookingAction: BookingActionState,
    onBackToList: () -> Unit,
    onRefresh: () -> Unit,
    onGoToConfirm: () -> Unit,
    onGoBackToInfo: () -> Unit,
    onConfirm: (note: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSubmitting = bookingAction is BookingActionState.InProgress
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    AnimatedContent(
        targetState = step,
        transitionSpec = {
            fadeIn(animationSpec = effectsSpec)
                .togetherWith(fadeOut(animationSpec = effectsSpec))
        },
        modifier = modifier,
        label = "bookingSheetStep",
    ) { stage ->
        when (stage) {
            BookingSheetStep.Info -> InfoStep(
                target = target,
                detail = detail,
                syncStatus = syncStatus,
                onBack = onBackToList,
                onRefresh = onRefresh,
                onBook = onGoToConfirm,
            )
            BookingSheetStep.Confirm -> ConfirmStep(
                target = target,
                detail = (detail as? Loadable.Loaded)?.value,
                isSubmitting = isSubmitting,
                onBack = onGoBackToInfo,
                onConfirm = onConfirm,
            )
        }
    }
}

@Composable
private fun InfoStep(
    target: BookingTarget,
    detail: Loadable<ExamCallDetail>,
    syncStatus: SyncStatus,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onBook: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val call = target.call

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 4.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SheetHeader(
            title = call.activityDescription?.takeIf { it.isNotBlank() } ?: "Esame",
            subtitle = listOfNotNull(call.activityCode, call.courseOfStudyDescription)
                .joinToString(" · ")
                .takeIf { it.isNotBlank() },
            onBack = onBack,
        )

        val dateLabel = call.callDate?.format(FullDateFormat)
            ?.replaceFirstChar { it.titlecase(Locale.ITALIAN) }
        val timeLabel = call.callTime?.format(TimeFormat)
        InfoRow(
            icon = Icons.Outlined.CalendarToday,
            label = "Quando",
            value = listOfNotNull(dateLabel, timeLabel?.let { "ore $it" })
                .joinToString(" · ")
                .ifBlank { "Data da definire" },
        )

        call.stateDescription?.takeIf { it.isNotBlank() }?.let {
            InfoRow(icon = Icons.Outlined.Schedule, label = "Stato", value = it)
        }

        val window = call.enrollmentWindow
        if (window.opensAt != null || window.closesAt != null) {
            InfoRow(
                icon = Icons.Outlined.EditNote,
                label = "Iscrizioni",
                value = formatWindow(window.opensAt, window.closesAt),
            )
        }

        call.enrolledNumber?.takeIf { it > 0 }?.let { n ->
            InfoRow(icon = Icons.Outlined.Groups, label = "Iscritti", value = "$n studenti")
        }

        when (detail) {
            Loadable.NotYetLoaded -> when (val s = syncStatus) {
                is SyncStatus.Failed -> ErrorRow(cause = s.cause, onRetry = onRefresh)
                else -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                }
            }
            is Loadable.Loaded -> DetailExtras(detail.value)
        }

        Spacer(Modifier.height(4.dp))

        if (target.canBook) {
            Button(
                onClick = onBook,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Prenota")
            }
        } else {
            Text(
                text = "Prenotazione non disponibile da questa schermata.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ConfirmStep(
    target: BookingTarget,
    detail: ExamCallDetail?,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onConfirm: (note: String?) -> Unit,
) {
    val call = target.call
    var note by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 4.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, enabled = !isSubmitting) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Indietro",
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Conferma prenotazione",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            text = call.activityDescription?.takeIf { it.isNotBlank() } ?: "Esame",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        val dateLabel = call.callDate?.format(FullDateFormat)
            ?.replaceFirstChar { it.titlecase(Locale.ITALIAN) }
        val timeLabel = call.callTime?.format(TimeFormat)
        val whenLabel = listOfNotNull(dateLabel, timeLabel?.let { "alle $it" }).joinToString(" ")
        if (whenLabel.isNotBlank()) {
            Text(
                text = whenLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            label = { Text("Nota (facoltativa)") },
            singleLine = false,
            minLines = 2,
            maxLines = 4,
            enabled = !isSubmitting,
        )

        Text(
            text = "Confermando, sarai iscritto a questo appello. Potrai annullare la prenotazione fino alla data di chiusura iscrizioni.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = { onConfirm(note.ifBlank { null }) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting,
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Conferma")
            }
        }
        if (detail == null) {
            Text(
                text = "Alcuni dati di dettaglio non sono stati caricati. La prenotazione userà comunque le info essenziali.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SheetHeader(
    title: String,
    subtitle: String?,
    onBack: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Indietro")
        }
        Spacer(Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DetailExtras(detail: ExamCallDetail) {
    detail.bookingTypeDescription?.takeIf { it.isNotBlank() }?.let {
        InfoRow(icon = Icons.Outlined.EditNote, label = "Modalità prenotazione", value = it)
    }
    detail.president?.let { p ->
        val full = listOfNotNull(p.name, p.surname).joinToString(" ").ifBlank { null }
        if (full != null) {
            InfoRow(icon = Icons.Outlined.Person, label = "Presidente", value = full)
        }
    }
    detail.notes?.takeIf { it.isNotBlank() }?.let { notes ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Note",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ErrorRow(cause: Throwable, onRetry: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = cause.friendlyMessage(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        FilledTonalButton(onClick = onRetry) { Text("Riprova") }
    }
}

private fun formatWindow(opensAt: LocalDate?, closesAt: LocalDate?): String {
    val opens = opensAt?.format(WindowFormat)
    val closes = closesAt?.format(WindowFormat)
    return when {
        opens != null && closes != null -> "Dal $opens al $closes"
        opens != null -> "Apertura $opens"
        closes != null -> "Chiusura $closes"
        else -> "—"
    }
}

private fun Throwable.friendlyMessage(): String = when (this) {
    is UnknownHostException,
    is ConnectException -> "Rete non disponibile. Controlla la connessione e riprova."
    is SocketTimeoutException -> "Timeout di rete. Riprova tra un momento."
    is IOException -> "Errore di rete. Riprova tra un momento."
    else -> "Si è verificato un errore imprevisto. Riprova."
}
