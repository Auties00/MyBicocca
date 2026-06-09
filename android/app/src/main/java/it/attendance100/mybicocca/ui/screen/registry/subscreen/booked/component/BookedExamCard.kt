package it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamType
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.displayTitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.examKindLabel
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DayOfWeekFormat = DateTimeFormatter.ofPattern("EEE", Locale.ITALIAN)
private val DayOfMonthFormat = DateTimeFormatter.ofPattern("d")
private val MonthFormat = DateTimeFormatter.ofPattern("MMM", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookedExamCard(
    booking: BookedExam,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    val dayOfMonth = remember(booking.examDateTime) {
        booking.examDateTime?.toLocalDate()?.format(DayOfMonthFormat)
    }
    val month = remember(booking.examDateTime) {
        booking.examDateTime?.toLocalDate()?.format(MonthFormat)?.uppercase(Locale.ITALIAN)
    }
    // The tile carries the date, so the footer carries the weekday: "Gio · 09:00".
    val timeLabel = remember(booking.examDateTime) {
        booking.examDateTime?.let {
            val weekday = it.toLocalDate().format(DayOfWeekFormat)
                .replaceFirstChar { c -> c.titlecase(Locale.ITALIAN) }
            "$weekday · ${it.toLocalTime().format(TimeFormat)}"
        }
    }
    val title = remember(booking.activityDescription) { booking.displayTitle() }
    val subtitle = remember(booking.examType, booking.callType) { booking.examKindLabel() }

    // Expressive press feedback: the corners tighten while the finger is down, the
    // same shape-morph language as the M3 button shapes.
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val corner by animateDpAsState(
        targetValue = if (pressed) 12.dp else 24.dp,
        label = "bookedExamCardCorner",
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth(),
        // One step above the sheet's surfaceContainerLow so the card reads on it.
        color = scheme.surfaceContainer,
        shape = RoundedCornerShape(corner),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp),
            ) {
                if (dayOfMonth != null && month != null) {
                    ExamDateTile(dayOfMonth = dayOfMonth, month = month)
                    Spacer(Modifier.width(14.dp))
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Tonal footer strip: the "when & where" at a glance.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.surfaceContainerHigh)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                timeLabel?.let {
                    FooterEntry(
                        icon = Icons.Outlined.Schedule,
                        label = it,
                    )
                }
                Box(modifier = Modifier.weight(1f))
                FooterEntry(
                    icon = Icons.Outlined.LocationOn,
                    label = booking.classroomDescription ?: "Aula da definire",
                    muted = booking.classroomDescription == null,
                )
            }
        }
    }
}

// Same leading tile as the study plan's CFU block, repurposed for the exam date.
@Composable
private fun ExamDateTile(dayOfMonth: String, month: String) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(scheme.primaryContainer, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayOfMonth,
                color = scheme.onPrimaryContainer,
                fontSize = 15.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = month,
                color = scheme.onPrimaryContainer.copy(alpha = 0.75f),
                fontSize = 8.sp,
                lineHeight = 9.sp,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun FooterEntry(
    icon: ImageVector,
    label: String,
    muted: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (muted) scheme.outline else scheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (muted) scheme.outline else scheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val PreviewBooking = BookedExam(
    key = ExamCallKey(
        courseOfStudyId = 12345,
        activityId = 67890,
        callId = 1
    ),
    applicationListId = 1L,
    studentId = 1L,
    activityChoiceId = 1L,
    activityDescription = "SICUREZZA ED AFFIDABILITA'",
    examCallDescription = "SICUREZZA ED AFFIDAB-scritto completo",
    examType = ExamType.Written,
    callType = ExamCallType.Final,
    examDateTime = LocalDateTime.of(2026, 6, 11, 9, 30),
    classroomDescription = "U9-16",
    buildingDescription = "U9 - Via dell'Innovazione, 10",
    credits = 8f,
    examModeDescription = "Esame in Presenza",
    position = 24,
    bookingDate = LocalDateTime.of(2026, 6, 2, 15, 47),
    cancellableUntil = LocalDate.of(2026, 6, 8),
    studentNote = null
)

@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark
)
@Composable
private fun BookedExamCardDarkPreview() {
    BicoccaTheme(dark = true) {
        BookedExamCard(
            booking = PreviewBooking,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BookedExamCardLightPreview() {
    BicoccaTheme(dark = false) {
        BookedExamCard(
            booking = PreviewBooking.copy(
                activityDescription = "ANALISI E PROGETTO DI ALGORITMI",
                examCallDescription = "ANALISI E PROGETTO DI ALGORITMI",
                examType = ExamType.Written,
                callType = ExamCallType.Partial,
                examDateTime = LocalDateTime.of(2026, 6, 8, 14, 0),
                classroomDescription = null,
                buildingDescription = "U9 - Via dell'Innovazione, 10",
                position = 60,
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
