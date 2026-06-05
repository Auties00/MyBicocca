package it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.degreeaward.CommitteeSession
import java.time.format.DateTimeFormatter

private val DayFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

// The scheduled seduta di laurea: date/time, room, department and the commission roster.
@Composable
fun CommitteeSessionCard(session: CommitteeSession, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.secondaryContainer,
        contentColor = scheme.onSecondaryContainer,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Seduta di laurea",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(12.dp))

            session.date?.let {
                InfoRow(Icons.Outlined.Schedule, it.format(DayFormat).replaceFirstChar { c -> c.uppercase() })
            }
            session.time?.let {
                InfoRow(Icons.Outlined.Schedule, "Ore ${it.format(TimeFormat)}")
            }
            val place = listOfNotNull(session.classroomDescription, session.buildingDescription)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
            if (place.isNotBlank()) InfoRow(Icons.Outlined.Place, place)
            session.departmentDescription?.let { InfoRow(Icons.Outlined.Groups, it) }

            if (session.committeeMembers.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Commissione",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                session.committeeMembers.forEach { member ->
                    Text(
                        text = listOfNotNull(
                            member.displayName.takeIf { it.isNotBlank() },
                            member.roleDescription,
                        ).joinToString(" — "),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
