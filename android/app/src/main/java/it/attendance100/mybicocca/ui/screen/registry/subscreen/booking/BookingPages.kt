package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamType
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingCourseGroup
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingTarget
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Root page of the booking sub-flow hosted by AppelliPage — the flow runs as a stack of
 * sheet pages ([ExamCalendarPage] -> [CallPage] -> [ConfirmPage]), each a self-contained body
 * the host drives via the BookingSheetViewModel while owning the modal chrome, header and
 * pager transitions itself.
 *
 * Shows every bookable exam as a card with a mini calendar of its appelli, swapping between
 * the error, loading (held briefly so quick fetches don't flash it), empty and list states
 * while animating its height as content lands. A pending deep-link focus scrolls the list to
 * the matching exam section.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ExamCalendarPage(
    loaded: Boolean,
    groups: List<BookingCourseGroup>?,
    syncStatus: SyncStatus,
    pendingFocus: String?,
    onConsumeFocus: () -> Unit,
    onRetry: () -> Unit,
    onOpenCall: (ExamCall) -> Unit,
) {
    val failure = syncStatus as? SyncStatus.Failed
    val showLoading = rememberMinDurationLoading(loading = !loaded)
    val settled = loaded && !showLoading

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    Column(
        modifier = Modifier
            .testTag(BookingTestTags.CALENDAR_ROOT)
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        when {
            failure != null && groups == null -> SheetError(
                cause = failure.cause,
                onRetry = onRetry,
                modifier = Modifier.testTag(BookingTestTags.CALENDAR_ERROR),
            )

            !settled -> SheetLoadingIndicator(
                label = stringResource(R.string.booking_loading_calls),
                modifier = Modifier.testTag(BookingTestTags.CALENDAR_LOADING),
            )

            groups.isNullOrEmpty() -> SheetMessage(
                icon = Icons.Outlined.EventAvailable,
                title = stringResource(R.string.booking_no_calls_available),
                body = stringResource(R.string.booking_no_calls_available_body),
                modifier = Modifier.testTag(BookingTestTags.CALENDAR_EMPTY),
            )

            else -> ExamCalendarList(
                groups = groups,
                pendingFocus = pendingFocus,
                onConsumeFocus = onConsumeFocus,
                onOpenCall = onOpenCall,
            )
        }
    }
}

/**
 * Scrollable exam sections; a pending deep-link focus animates the list to the matching
 * section and is then consumed — found or not — so it cannot keep re-scrolling.
 */
@Composable
private fun ExamCalendarList(
    groups: List<BookingCourseGroup>,
    pendingFocus: String?,
    onConsumeFocus: () -> Unit,
    onOpenCall: (ExamCall) -> Unit,
) {
    val listState = rememberLazyListState()
    val today = remember { LocalDate.now() }

    LaunchedEffect(pendingFocus, groups) {
        val key = pendingFocus ?: return@LaunchedEffect
        val index = groups.indexOfFirst { it.courseKey == key }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
        onConsumeFocus()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .testTag(BookingTestTags.CALENDAR_CONTENT)
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(groups, key = { _, group -> group.courseKey }) { _, group ->
            ExamSection(group = group, today = today, onOpenCall = onOpenCall)
        }
    }
}

/**
 * One exam: its title over the calendar of its appelli. Date cells lead with the day numeral
 * and carry the call type as a tag; the container tone tells the enrollment state at a glance
 * (brand container = open, neutral = not yet, faded = closed).
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExamSection(
    group: BookingCourseGroup,
    today: LocalDate,
    onOpenCall: (ExamCall) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = group.courseTitle,
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOfNotNull(group.courseCode, countLabel(group.calls.size))
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                group.calls.forEach { call ->
                    CallDateCell(
                        call = call,
                        today = today,
                        onClick = { onOpenCall(call) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CallDateCell(
    call: ExamCall,
    today: LocalDate,
    onClick: () -> Unit,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    val status = call.windowStatus(today)
    val container = when (status) {
        WindowStatus.Open -> scheme.primaryContainer
        WindowStatus.NotYetOpen, WindowStatus.Closed -> scheme.surfaceContainerHigh
    }
    val content = when (status) {
        WindowStatus.Open -> scheme.onPrimaryContainer
        WindowStatus.NotYetOpen -> scheme.onSurface
        WindowStatus.Closed -> scheme.onSurfaceVariant.copy(alpha = 0.55f)
    }

    Surface(
        onClick = { haptic.tap(); onClick() },
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag(BookingTestTags.call(call.examCallId)),
    ) {
        Column(
            modifier = Modifier
                .width(76.dp)
                .padding(top = 12.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = call.callDate?.dayOfMonth?.toString() ?: "—",
                style = MaterialTheme.typography.headlineSmallEmphasized,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = call.callDate?.format(MonthFormat)?.uppercase(Locale.getDefault()) ?: "N.D.",
                style = MaterialTheme.typography.labelSmall,
                color = content.copy(alpha = content.alpha * 0.8f),
                maxLines = 1,
            )
            Spacer(Modifier.height(8.dp))
            ExamTypeTag(type = call.examType, status = status)
        }
    }
}

/**
 * Tiny exam-mode tag in the date cell; brand-filled on open cells, tonal otherwise. Content
 * on the brand red fill is explicit white — onPrimary flips dark in dark mode.
 */
@Composable
private fun ExamTypeTag(type: ExamType, status: WindowStatus) {
    val scheme = MaterialTheme.colorScheme
    val open = status == WindowStatus.Open
    Surface(
        color = if (open) scheme.primary else scheme.surfaceContainerHighest,
        contentColor = when {
            open -> Color.White
            status == WindowStatus.Closed -> scheme.onSurfaceVariant.copy(alpha = 0.55f)
            else -> scheme.onSurfaceVariant
        },
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = type.tagLabel(),
            fontSize = 9.sp,
            lineHeight = 11.sp,
            letterSpacing = 0.6.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

/**
 * Detail page for one appello: a connected stack of fact rows (state, enrollment window,
 * enrolled count, booking mode, president) over the call notes when present, with the Prenota
 * action pinned at the bottom — or a short notice when the call cannot be booked. There is no
 * hero recap: the pager header's subtitle already carries the exam mode, date and time of the
 * tapped cell.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CallPage(
    target: BookingTarget,
    onBook: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val call = target.call

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val facts = buildList {
                call.stateDescription?.takeIf { it.isNotBlank() }?.let {
                    add(
                        Triple(
                            Icons.Outlined.Schedule,
                            stringResource(R.string.booking_status),
                            it
                        )
                    )
                }
                val window = call.enrollmentWindow
                if (window.opensAt != null || window.closesAt != null) {
                    add(
                        Triple(
                            Icons.Outlined.EventAvailable,
                            stringResource(R.string.booking_enrollments),
                            formatWindow(window.opensAt, window.closesAt),
                        )
                    )
                }
                call.enrolledNumber?.takeIf { it > 0 }?.let { n ->
                    add(
                        Triple(
                            Icons.Outlined.Groups,
                            stringResource(R.string.booking_enrolled),
                            "$n studenti"
                        )
                    )
                }
                call.bookingTypeDescription?.takeIf { it.isNotBlank() }?.let {
                    add(Triple(Icons.Outlined.EditNote, stringResource(R.string.booking_mode), it))
                }
                call.president?.let { p ->
                    listOfNotNull(p.name, p.surname).joinToString(" ").ifBlank { null }?.let {
                        add(
                            Triple(
                                Icons.Outlined.Person,
                                stringResource(R.string.booking_president),
                                it
                            )
                        )
                    }
                }
            }
            if (facts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    facts.forEachIndexed { index, (icon, label, value) ->
                        FactRow(
                            icon = icon,
                            label = label,
                            value = value,
                            isFirst = index == 0,
                            isLast = index == facts.lastIndex,
                        )
                    }
                }
            }

            call.notes?.takeIf { it.isNotBlank() }?.let { NotesCard(notes = it) }
        }

        if (target.canBook) {
            BrandActionButton(
                text = stringResource(R.string.booking_book),
                onClick = onBook,
                enabled = LocalIsOnline.current,
                modifier = Modifier
                    .testTag(BookingTestTags.CALL_BOOK_BUTTON)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            )
        } else {
            Text(
                text = stringResource(R.string.booking_not_available),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 16.dp,
                    bottom = 24.dp
                ),
            )
        }
    }
}

/**
 * One row of the connected segmented stack, the registry idiom: 20dp corner caps at the
 * group's ends, 6dp seams where rows touch.
 */
@Composable
private fun FactRow(
    icon: ImageVector,
    label: String,
    value: String,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val large = 20.dp
    val small = 6.dp
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(
            topStart = if (isFirst) large else small,
            topEnd = if (isFirst) large else small,
            bottomStart = if (isLast) large else small,
            bottomEnd = if (isLast) large else small,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainerHigh,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.booking_call_notes),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant,
            )
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/**
 * Final step of the booking flow: an optional note field above the Conferma action, with a
 * reminder that the booking stays cancellable until enrollment closes.
 */
@Composable
internal fun ConfirmPage(
    submitting: Boolean,
    onConfirm: (String?) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var note by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(BookingTestTags.CONFIRM_NOTE_FIELD),
                shape = MaterialTheme.shapes.large,
                label = { Text(stringResource(R.string.booking_optional_note)) },
                minLines = 2,
                maxLines = 4,
                enabled = !submitting,
            )

            Text(
                text = stringResource(R.string.booking_confirm_message),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }

        BrandActionButton(
            text = stringResource(R.string.common_confirm),
            onClick = { onConfirm(note.ifBlank { null }) },
            enabled = !submitting && LocalIsOnline.current,
            loading = submitting,
            modifier = Modifier
                .testTag(BookingTestTags.CONFIRM_BUTTON)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        )
    }
}

/**
 * Brand action pinned at the page bottom — primary in light, primaryContainer in dark (the
 * percorso footer scheme); swaps its label for a loading indicator while submitting.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BrandActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Button(
        onClick = { haptic.tap(); onClick() },
        enabled = enabled,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = brandBg,
            contentColor = brandFg,
        ),
    ) {
        if (loading) {
            LoadingIndicator(
                modifier = Modifier.size(24.dp),
                color = brandFg,
            )
        } else {
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SheetError(cause: Throwable, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    SheetMessage(
        icon = Icons.Outlined.CloudOff,
        title = stringResource(R.string.booking_load_failed),
        body = stringResource(cause.friendlyMessage()),
        action = {
            val haptic = rememberHapticManager()
            RetryButton(onClick = { haptic.tap(); onRetry() })
        },
        modifier = modifier,
    )
}

private enum class WindowStatus { Open, NotYetOpen, Closed }

private fun ExamCall.windowStatus(today: LocalDate): WindowStatus {
    val window = enrollmentWindow
    return when {
        window.closesAt != null && window.closesAt.isBefore(today) -> WindowStatus.Closed
        window.opensAt != null && window.opensAt.isAfter(today) -> WindowStatus.NotYetOpen
        else -> WindowStatus.Open
    }
}

internal fun ExamCall.title(): String = activityDescription?.takeIf { it.isNotBlank() } ?: "Esame"

/**
 * "Esame scritto · 22 giu 2026, ore 14:00" — the appello sub-modal has no hero card, so the
 * header subtitle carries the whole when/how of the tapped cell.
 */
@Composable
internal fun ExamCall.headerSubtitle(): String {
    val date = callDate?.format(ShortDateFormat)
    val time = callTime?.let { "ore ${it.format(TimeFormat)}" }
    val moment = when {
        date != null && time != null -> "$date, $time"
        else -> date ?: time
    }
    return listOfNotNull(examType.longLabel(), moment).joinToString(" · ")
}

/** Scritto & orale (joint or separate parts) reads as a single "unico" exam. */
private fun ExamType.tagLabel(): String = when (this) {
    ExamType.Written -> "SCRITTO"
    ExamType.Oral -> "ORALE"
    ExamType.WrittenAndOralJoint, ExamType.WrittenAndOralSeparate -> "UNICO"
    ExamType.Unknown -> "APPELLO"
}

private fun ExamType.longLabel(): String = when (this) {
    ExamType.Written -> "Esame scritto"
    ExamType.Oral -> "Esame orale"
    ExamType.WrittenAndOralJoint, ExamType.WrittenAndOralSeparate -> "Esame unico"
    ExamType.Unknown -> "Appello d'esame"
}

internal fun rootSubtitle(groups: List<BookingCourseGroup>): String {
    val calls = groups.sumOf { it.calls.size }
    val exams = groups.size
    return "${countLabel(calls)} · ${if (exams == 1) "1 esame" else "$exams esami"}"
}

private fun countLabel(calls: Int): String = if (calls == 1) "1 appello" else "$calls appelli"

@Composable
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

@get:Composable
private val ShortDateFormat
    get() = DateTimeFormatter.ofPattern("d MMM yyyy", currentLocale())

@get:Composable
private val MonthFormat
    get() = DateTimeFormatter.ofPattern("MMM", currentLocale())
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

@get:Composable
private val WindowFormat
    get() = DateTimeFormatter.ofPattern("d MMM yyyy", currentLocale())
