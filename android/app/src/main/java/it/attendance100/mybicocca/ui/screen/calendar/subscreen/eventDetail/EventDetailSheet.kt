package it.attendance100.mybicocca.ui.screen.calendar.subscreen.eventDetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.ui.screen.calendar.ext.durationMinutes
import it.attendance100.mybicocca.ui.screen.calendar.ext.formatTimeRange
import it.attendance100.mybicocca.ui.screen.calendar.ext.isInProgress
import it.attendance100.mybicocca.ui.screen.calendar.ext.locationLine
import it.attendance100.mybicocca.ui.screen.calendar.ext.minutesRemaining
import it.attendance100.mybicocca.ui.screen.calendar.ext.peopleLine
import it.attendance100.mybicocca.ui.screen.calendar.ext.rememberCurrentTime

val LocalEventCourseOpener = compositionLocalOf<(CalendarEvent) -> (() -> Unit)?> { { null } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailSheet(
    event: CalendarEvent,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cancelled = event.status == EventStatus.CANCELLED
    val scheme = MaterialTheme.colorScheme

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
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (cancelled) scheme.onSurfaceVariant else scheme.onSurface,
                textDecoration = if (cancelled) TextDecoration.LineThrough else TextDecoration.None,
            )
            EventDetailContent(event = event)
        }
    }
}

@Composable
fun EventDetailContent(
    event: CalendarEvent,
    modifier: Modifier = Modifier,
) {
    val now by rememberCurrentTime()
    val cancelled = event.status == EventStatus.CANCELLED
    val inProgress = event.isInProgress(now)
    val context = LocalContext.current

    Column(modifier = modifier) {
        if (cancelled || inProgress) {
            Spacer(Modifier.height(12.dp))
            StatusChip(cancelled = cancelled, inProgress = inProgress, minutesLeft = event.minutesRemaining(now))
        }
        Spacer(Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IconRow(
                icon = if (event is CalendarEvent.Exam) Icons.Outlined.Quiz else Icons.Outlined.School,
                label = "ATTIVITÀ",
                value = activityLabel(event),
            )
            IconRow(
                icon = Icons.Outlined.Schedule,
                label = "ORARIO",
                value = orarioValue(event),
            )
            val location = event.locationLine()
            if (location.isNotBlank()) {
                IconRow(
                    icon = Icons.Outlined.LocationOn,
                    label = "LUOGO",
                    value = location,
                )
            }
            val people = event.peopleLine()
            if (people.isNotBlank()) {
                IconRow(
                    icon = Icons.Outlined.Person,
                    label = if (event is CalendarEvent.Exam) "COMMISSIONE" else "DOCENTE",
                    value = people,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        ActionRow(
            onDirections = {
                val label = event.locationLine().takeIf { it.isNotBlank() }
                val url = event.location?.mapsUrl
                    ?.let { mapsUrlToGeoUri(it, label) }
                    ?: label?.let { "geo:0,0?q=${Uri.encode(it)}" }
                if (url != null) {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                }
            },
            onNotify = {},
            onOpenCourse = LocalEventCourseOpener.current(event),
        )
    }
}

@Composable
private fun StatusChip(cancelled: Boolean, inProgress: Boolean, minutesLeft: Int) {
    val scheme = MaterialTheme.colorScheme
    val (label, fg, bg) = when {
        cancelled -> Triple("Annullato", scheme.onErrorContainer, scheme.errorContainer)
        inProgress -> Triple("In corso · $minutesLeft min", scheme.onTertiaryContainer, scheme.tertiaryContainer)
        else -> Triple("", scheme.onSurface, scheme.surfaceContainerHigh)
    }
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = bg,
            labelColor = fg,
        ),
        border = null,
    )
}

@Composable
private fun IconRow(icon: ImageVector, label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = scheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActionRow(
    onDirections: () -> Unit,
    onNotify: () -> Unit,
    onOpenCourse: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val accentBg = if (dark) scheme.primaryContainer else scheme.primary
    val accentFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    val compactPadding = PaddingValues(horizontal = 8.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilledTonalButton(
            onClick = onDirections,
            modifier = Modifier
                .weight(1.3f)
                .height(56.dp),
            shape = ButtonGroupDefaults.connectedLeadingButtonShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.surfaceContainerHigh,
                contentColor = scheme.onSurface,
            ),
            contentPadding = compactPadding,
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("Indicazioni", fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        if (onOpenCourse != null) {
            Button(
                onClick = onOpenCourse,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = ShapeDefaults.Small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentBg,
                    contentColor = accentFg,
                ),
                contentPadding = compactPadding,
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Corso", fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
        }
        FilledTonalButton(
            onClick = onNotify,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.surfaceContainerHigh,
                contentColor = scheme.onSurface,
            ),
            contentPadding = compactPadding,
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("Notifica", fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

private fun activityLabel(event: CalendarEvent): String = when (event) {
    is CalendarEvent.Lesson -> "Lezione"
    is CalendarEvent.Exam -> {
        val type = event.examTypeLabel?.takeIf { it.isNotBlank() }
        if (type != null) "Esame · $type" else "Esame"
    }
}

private fun orarioValue(event: CalendarEvent): String {
    val durMin = event.durationMinutes()
    val durLabel = when {
        durMin == 0 -> "istantaneo"
        durMin < 60 -> "${durMin}min"
        durMin % 60 == 0 -> "${durMin / 60}h"
        else -> "${durMin / 60}h ${durMin % 60}min"
    }
    return "${event.formatTimeRange()} · $durLabel"
}

private val MapsEmbedCoordRegex = Regex("!2d(-?\\d+\\.?\\d*)!3d(-?\\d+\\.?\\d*)")

private fun mapsUrlToGeoUri(rawUrl: String, label: String?): String {
    val match = MapsEmbedCoordRegex.find(rawUrl) ?: return rawUrl
    val lng = match.groupValues[1]
    val lat = match.groupValues[2]
    val labelPart = label?.takeIf { it.isNotBlank() }?.let { "(${Uri.encode(it)})" }.orEmpty()
    return "geo:$lat,$lng?q=$lat,$lng$labelPart"
}
