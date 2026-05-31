package it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.ui.navigation.transitions.BookedExamElementKey
import it.attendance100.mybicocca.ui.navigation.transitions.BookedExamSharedElementType
import it.attendance100.mybicocca.ui.navigation.transitions.bicoccaSharedBounds
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DayOfWeekFormat = DateTimeFormatter.ofPattern("EEE", Locale.ITALIAN)
private val DayOfMonthFormat = DateTimeFormatter.ofPattern("d")
private val MonthFormat = DateTimeFormatter.ofPattern("MMM", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BookedExamCard(
    booking: BookedExam,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    val dayOfWeek = remember(booking.examDateTime) {
        booking.examDateTime?.toLocalDate()?.format(DayOfWeekFormat)?.uppercase(Locale.ITALIAN)
    }
    val dayOfMonth = remember(booking.examDateTime) {
        booking.examDateTime?.toLocalDate()?.format(DayOfMonthFormat)
    }
    val month = remember(booking.examDateTime) {
        booking.examDateTime?.toLocalDate()?.format(MonthFormat)?.uppercase(Locale.ITALIAN)
    }
    val timeLabel =
        remember(booking.examDateTime) { booking.examDateTime?.toLocalTime()?.format(TimeFormat) }

    val location = listOfNotNull(
        booking.classroomDescription,
        booking.buildingDescription,
    ).joinToString(" · ").ifBlank { null }

    val keyString = "${booking.key.courseOfStudyId}_${booking.key.activityId}_${booking.key.callId}"

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .bicoccaSharedBounds(
                key = BookedExamElementKey(
                    infoPath = keyString,
                    type = BookedExamSharedElementType.Body
                ),
                clipShape = RoundedCornerShape(20.dp)
            ),
        color = scheme.surfaceContainerLow,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Row(modifier = Modifier.fillMaxWidth()) {
                // Date Badge
                if (dayOfWeek != null && dayOfMonth != null && month != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = dayOfWeek,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = dayOfMonth,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = month,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Titles
                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = booking.activityDescription?.takeIf { it.isNotBlank() }
                            ?.uppercase(Locale.ITALIAN) ?: "ESAME",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        modifier = Modifier.bicoccaSharedBounds(
                            key = BookedExamElementKey(
                                infoPath = keyString,
                                type = BookedExamSharedElementType.Title
                            )
                        )
                    )

                    if (!booking.examCallDescription.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = scheme.surfaceContainerHighest,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = booking.examCallDescription,
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .bicoccaSharedBounds(
                                        key = BookedExamElementKey(
                                            infoPath = keyString,
                                            type = BookedExamSharedElementType.Description
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Metadata Rows
            timeLabel?.let { MetaRow(icon = Icons.Outlined.Schedule, label = "Ore $it") }
            location?.let { MetaRow(icon = Icons.Outlined.LocationOn, label = it) }
            booking.position?.takeIf { it > 0 }?.let { p ->
                MetaRow(icon = Icons.Outlined.ConfirmationNumber, label = "${p}º a prenotarsi")
            }
        }
    }
}

@Composable
private fun MetaRow(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}


@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF0B0808
)
@Composable
private fun BookedExamCardPreview() {
    BicoccaTheme(dark = true) {
        BookedExamCard(
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
                studentNote = null
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun BookedExamCardLightPreview() {
    BicoccaTheme(dark = false) {
        BookedExamCard(
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
                studentNote = null
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
