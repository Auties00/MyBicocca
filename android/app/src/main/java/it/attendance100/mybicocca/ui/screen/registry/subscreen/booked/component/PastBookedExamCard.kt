package it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.ui.component.exam.ExamGradeBadge
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.displayTitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.sittingLabel

// A sat appello in the Passate register: the grade polygon leads (a plain "—" circle while
// the outcome is unpublished), title and the date it was sat alongside. No date tile or
// location footer — for a past exam the grade is the headline, not the logistics.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PastBookedExamCard(
    booking: BookedExam,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val title = remember(booking.activityDescription) { booking.displayTitle() }
    val subtitle = remember(booking.examDateTime, booking.outcomePublished, booking.grade) {
        booking.sittingLabel()
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ExamGradeBadge(
                grade = booking.grade,
                size = 52.dp,
                style = MaterialTheme.typography.titleMediumEmphasized,
            )
            Column(modifier = Modifier.weight(1f)) {
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
    }
}
