package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallDetail
import it.attendance100.mybicocca.ui.component.exam.ExamDateBadge
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

private val LongDateFormat = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
private val DayOfMonthFormat = DateTimeFormatter.ofPattern("d")
private val MonthFormat = DateTimeFormatter.ofPattern("MMM", Locale.ITALIAN)
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
    onConfirm: (String?) -> Unit,
    modifier: Modifier = Modifier,
    maxHeight: Dp,
) {
    val isSubmitting = bookingAction is BookingActionState.InProgress
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    AnimatedContent(
        targetState = step,
        transitionSpec = {
            fadeIn(animationSpec = effectsSpec)
                .togetherWith(fadeOut(animationSpec = effectsSpec))
        },
        modifier = modifier.heightIn(max = maxHeight),
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
            .fillMaxWidth()
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

        CallSummaryCard(call = call)

        // Secondary facts grouped in one tonal list card; rows split by inset dividers.
        val rows = buildList {
            call.stateDescription?.takeIf { it.isNotBlank() }?.let {
                add(Triple(Icons.Outlined.Schedule, "Stato", it))
            }
            val window = call.enrollmentWindow
            if (window.opensAt != null || window.closesAt != null) {
                add(
                    Triple(
                        Icons.Outlined.EditNote,
                        "Iscrizioni",
                        formatWindow(window.opensAt, window.closesAt),
                    )
                )
            }
            call.enrolledNumber?.takeIf { it > 0 }?.let { n ->
                add(Triple(Icons.Outlined.Groups, "Iscritti", "$n studenti"))
            }
            (detail as? Loadable.Loaded)?.value?.let { d ->
                d.bookingTypeDescription?.takeIf { it.isNotBlank() }?.let {
                    add(Triple(Icons.Outlined.EditNote, "Modalità prenotazione", it))
                }
                d.president?.let { p ->
                    listOfNotNull(p.name, p.surname).joinToString(" ").ifBlank { null }?.let {
                        add(Triple(Icons.Outlined.Person, "Presidente", it))
                    }
                }
            }
        }
        if (rows.isNotEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = scheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    rows.forEachIndexed { index, (icon, label, value) ->
                        InfoRow(icon = icon, label = label, value = value)
                        if (index < rows.lastIndex) {
                            HorizontalDivider(
                                color = scheme.outlineVariant,
                                modifier = Modifier.padding(start = 52.dp, end = 16.dp),
                            )
                        }
                    }
                }
            }
        }

        when (detail) {
            Loadable.NotYetLoaded -> when (val s = syncStatus) {
                is SyncStatus.Failed -> ErrorRow(cause = s.cause, onRetry = onRefresh)
                else -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    LoadingIndicator(modifier = Modifier.size(40.dp))
                }
            }
            is Loadable.Loaded -> detail.value.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                NotesCard(notes = notes)
            }
        }

        Spacer(Modifier.height(4.dp))

        if (target.canBook) {
            Button(
                onClick = onBook,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = ButtonDefaults.MediumContainerHeight),
                contentPadding = ButtonDefaults.MediumContentPadding,
            ) {
                Text("Prenota", style = MaterialTheme.typography.titleMedium)
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 4.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SheetHeader(
            title = "Conferma prenotazione",
            subtitle = null,
            onBack = onBack,
            backEnabled = !isSubmitting,
        )

        CallSummaryCard(
            call = call,
            title = call.activityDescription?.takeIf { it.isNotBlank() } ?: "Esame",
        )

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
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

        Button(
            onClick = { onConfirm(note.ifBlank { null }) },
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ButtonDefaults.MediumContainerHeight),
            contentPadding = ButtonDefaults.MediumContentPadding,
            enabled = !isSubmitting,
        ) {
            if (isSubmitting) {
                LoadingIndicator(
                    modifier = Modifier.size(28.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Conferma", style = MaterialTheme.typography.titleMedium)
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

// Hero recap of the chosen call: arch date badge + long date + time.
@Composable
private fun CallSummaryCard(
    call: ExamCall,
    title: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val date = call.callDate
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = scheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (date != null) {
                ExamDateBadge(
                    dayOfMonth = date.format(DayOfMonthFormat),
                    month = date.format(MonthFormat).uppercase(Locale.ITALIAN),
                    modifier = Modifier.padding(end = 16.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface,
                    )
                }
                Text(
                    text = date?.format(LongDateFormat)
                        ?.replaceFirstChar { it.titlecase(Locale.ITALIAN) }
                        ?: "Data da definire",
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurface,
                )
                call.callTime?.let {
                    Text(
                        text = "ore ${it.format(TimeFormat)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SheetHeader(
    title: String,
    subtitle: String?,
    onBack: () -> Unit,
    backEnabled: Boolean = true,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(onClick = onBack, enabled = backEnabled) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Indietro",
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmallEmphasized,
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
private fun NotesCard(notes: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
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

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        FilledTonalButton(onClick = onRetry, shapes = ButtonDefaults.shapes()) { Text("Riprova") }
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
    else -> "Si è verificato un errore imprevisto"
}
