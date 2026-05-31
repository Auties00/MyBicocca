package it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExams

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.ui.navigation.transitions.BookedExamElementKey
import it.attendance100.mybicocca.ui.navigation.transitions.BookedExamSharedElementType
import it.attendance100.mybicocca.ui.navigation.transitions.bicoccaSharedBounds
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.BookedEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.CancelActionState
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DayOfWeekFormat = DateTimeFormatter.ofPattern("EEE", Locale.ITALIAN)
private val DayOfMonthFormat = DateTimeFormatter.ofPattern("d")
private val MonthFormat = DateTimeFormatter.ofPattern("MMM", Locale.ITALIAN)
private val FullDateFormat = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")
private val BookingDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ITALIAN)

// "Large" expressive sizing for the bottom split button.
private val LargeButtonHeight = 80.dp

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookedExamDetailScreen(
    courseOfStudyId: Long,
    activityId: Long,
    callId: Int,
    viewModel: BookedExamsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyString = "${courseOfStudyId}_${activityId}_${callId}"

    val bookingsState by viewModel.bookings.collectAsStateWithLifecycle()
    val cancelAction by viewModel.cancelAction.collectAsStateWithLifecycle()

    val booking = remember(bookingsState, keyString) {
        (bookingsState as? Loadable.Loaded)?.value?.firstOrNull {
            it.key.courseOfStudyId == courseOfStudyId &&
                    it.key.activityId == activityId &&
                    it.key.callId == callId
        }
    }

    val isCancelling = (cancelAction as? CancelActionState.InProgress)?.key == keyString

    // Cancellation feedback (snackbar) is shown by the list screen, which stays composed inside
    // the tab pager; here we only need to leave this now-stale detail once the booking is gone.
    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is BookedEvent.CancellationSucceeded) onBack()
        }
    }

    BookedExamDetailContent(
        keyString = keyString,
        booking = booking,
        isCancelling = isCancelling,
        onCancel = { viewModel.cancel(it) },
        modifier = modifier
    )
}

@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BookedExamDetailContent(
    keyString: String,
    booking: BookedExam?,
    isCancelling: Boolean,
    onCancel: (BookedExam) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    var confirming by remember { mutableStateOf(false) }

    val title = booking?.activityDescription?.takeIf { it.isNotBlank() }
        ?.uppercase(Locale.ITALIAN) ?: "ESAME"
    val location = booking?.let {
        listOfNotNull(it.classroomDescription, it.buildingDescription)
            .joinToString(" · ").ifBlank { null }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeroCard(
                keyString = keyString,
                title = title,
                examCallDescription = booking?.examCallDescription,
                dayOfWeek = booking?.examDateTime?.toLocalDate()
                    ?.format(DayOfWeekFormat)?.uppercase(Locale.ITALIAN),
                dayOfMonth = booking?.examDateTime?.toLocalDate()?.format(DayOfMonthFormat),
                month = booking?.examDateTime?.toLocalDate()
                    ?.format(MonthFormat)?.uppercase(Locale.ITALIAN),
            )

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = scheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (booking == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        booking.examDateTime?.let { dt ->
                            MetaRow(
                                icon = Icons.Outlined.CalendarMonth,
                                label = "Data appello",
                                value = dt.toLocalDate().format(FullDateFormat)
                                    .replaceFirstChar { it.uppercase() },
                            )
                            MetaRow(
                                icon = Icons.Outlined.Schedule,
                                label = "Orario",
                                value = "Ore ${dt.toLocalTime().format(TimeFormat)}",
                            )
                        }
                        location?.let {
                            MetaRow(
                                icon = Icons.Outlined.PinDrop,
                                label = "Luogo",
                                value = it,
                            )
                        }
                        booking.position?.takeIf { it > 0 }?.let { p ->
                            MetaRow(
                                icon = Icons.Outlined.ConfirmationNumber,
                                label = "Posizione",
                                value = "${p}º a prenotarsi",
                            )
                        }
                        booking.bookingDate?.let {
                            MetaRow(
                                icon = Icons.Outlined.EventAvailable,
                                label = "Prenotato il",
                                value = it.format(BookingDateFormat),
                            )
                        }
                        booking.studentNote?.takeIf { it.isNotBlank() }?.let {
                            MetaRow(
                                icon = Icons.AutoMirrored.Outlined.Notes,
                                label = "Nota",
                                value = it,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Split button: the leading half is the primary (destructive) action; the trailing half
        // toggles a menu with the secondary actions (print, map). The default expressive shapes
        // give each half a pill outer edge and a tight inner edge, so the two read as one pill.
        var menuExpanded by remember { mutableStateOf(false) }
        val chevronRotation by animateFloatAsState(
            targetValue = if (menuExpanded) 180f else 0f,
            label = "splitButtonChevron",
        )
        val splitColors = ButtonDefaults.buttonColors(
            containerColor = scheme.errorContainer,
            contentColor = scheme.onErrorContainer,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            SplitButtonLayout(
                leadingButton = {
                    SplitButtonDefaults.LeadingButton(
                        onClick = { confirming = true },
                        enabled = !isCancelling && booking?.studentId != null,
                        colors = splitColors,
                        modifier = Modifier.height(LargeButtonHeight),
                    ) {
                        if (isCancelling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp,
                                color = scheme.onErrorContainer,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.EventBusy,
                                contentDescription = null,
                                modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize),
                            )
                            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                            Text(
                                text = "Cancella Prenotazione",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                trailingButton = {
                    SplitButtonDefaults.TrailingButton(
                        checked = menuExpanded,
                        onCheckedChange = { menuExpanded = it },
                        enabled = booking != null,
                        colors = splitColors,
                        modifier = Modifier
                            .height(LargeButtonHeight)
                            .width(58.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Altre azioni",
                            modifier = Modifier
                                .size(SplitButtonDefaults.TrailingIconSize)
                                .rotate(chevronRotation),
                        )
                    }
                },
            )

            val screenWidth =
                LocalResources.current.displayMetrics.widthPixels / LocalResources.current.displayMetrics.density
            val midPoint = screenWidth / 2

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                offset = DpOffset((midPoint - 28).dp, 0.dp),
            ) {
                DropdownMenuItem(
                    text = { Text("Stampa") },
                    leadingIcon = { Icon(Icons.Outlined.Print, contentDescription = null) },
                    onClick = { menuExpanded = false /* TODO: hook up booking receipt printing */ },
                )
                DropdownMenuItem(
                    text = { Text("Apri posizione") },
                    enabled = location != null,
                    leadingIcon = { Icon(Icons.Outlined.PinDrop, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        val locQuery = Uri.encode(location ?: "")
                        val mapIntent = Intent(Intent.ACTION_VIEW, "geo:0,0?q=$locQuery".toUri())
                        context.startActivity(mapIntent)
                    },
                )
            }
        }
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("Annullare la prenotazione?") },
            text = {
                Text(
                    "Stai per annullare la prenotazione a " +
                            (booking?.activityDescription?.takeIf { it.isNotBlank() }
                                ?: "questo appello") +
                            ". L'operazione non può essere ripristinata."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirming = false
                        booking?.let { onCancel(it) }
                    },
                ) { Text("Annulla prenotazione") }
            },
            dismissButton = {
                TextButton(onClick = { confirming = false }) { Text("Indietro") }
            },
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun HeroCard(
    keyString: String,
    title: String,
    examCallDescription: String?,
    dayOfWeek: String?,
    dayOfMonth: String?,
    month: String?,
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .bicoccaSharedBounds(
                key = BookedExamElementKey(
                    infoPath = keyString,
                    type = BookedExamSharedElementType.Body
                ),
                clipShape = RoundedCornerShape(20.dp)
            ),
        color = scheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(modifier = Modifier.padding(18.dp)) {
            if (dayOfWeek != null && dayOfMonth != null && month != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = scheme.primaryContainer,
                    modifier = Modifier.padding(end = 16.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Text(
                            dayOfWeek,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            dayOfMonth,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            month,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    modifier = Modifier.bicoccaSharedBounds(
                        key = BookedExamElementKey(
                            infoPath = keyString,
                            type = BookedExamSharedElementType.Title
                        )
                    ),
                )
                if (!examCallDescription.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = scheme.surfaceContainerHighest,
                        modifier = Modifier.padding(top = 6.dp),
                    ) {
                        Text(
                            text = examCallDescription,
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .bicoccaSharedBounds(
                                    key = BookedExamElementKey(
                                        infoPath = keyString,
                                        type = BookedExamSharedElementType.Description
                                    )
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
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

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF0F0606
)
@Composable
private fun BookedExamDetailPreview() {
    BicoccaTheme(dark = true) {
        BookedExamDetailContent(
            keyString = "12345_67890_1",
            booking = BookedExam(
                key = ExamCallKey(
                    courseOfStudyId = 12345,
                    activityId = 67890,
                    callId = 1
                ),
                applicationListId = 1L,
                studentId = 1L,
                activityChoiceId = 1L,
                activityDescription = "Sistemi Operativi",
                examCallDescription = "Appello Ordinario",
                examDateTime = LocalDateTime.of(2024, 6, 20, 14, 30),
                classroomDescription = "Aula G24",
                buildingDescription = "Edificio U6",
                position = 42,
                bookingDate = LocalDateTime.of(2024, 5, 1, 10, 0),
                studentNote = "Portare calcolatrice"
            ),
            isCancelling = false,
            onCancel = {}
        )
    }
}
