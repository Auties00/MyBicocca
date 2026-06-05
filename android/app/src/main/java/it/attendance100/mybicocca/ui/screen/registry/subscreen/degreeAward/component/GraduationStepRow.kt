package it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadgeTone
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.state.GraduationStepState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.state.StepStatus
import it.attendance100.mybicocca.ui.screen.registry.theme.registryBadgeTone

// One connected tile in the graduation checklist. Done steps wear the success tone with a
// check; the current step is primary and tappable; locked steps dim with a lock.
@Composable
fun GraduationStepRow(
    state: GraduationStepState,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val okTone = registryBadgeTone(RegistryBadgeTone.Ok)
    val enabled = state.status == StepStatus.Current || state.status == StepStatus.Done

    val large = 20.dp
    val small = 4.dp
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (state.status == StepStatus.Locked) 0.5f else 1f),
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
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val (markColor, markContent) = when (state.status) {
                StepStatus.Done -> okTone.container to okTone.onContainer
                StepStatus.Current -> scheme.primary to Color.White
                StepStatus.Locked -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(markColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                when (state.status) {
                    StepStatus.Done -> Icon(Icons.Default.Check, null, tint = markContent, modifier = Modifier.size(20.dp))
                    StepStatus.Locked -> Icon(Icons.Outlined.Lock, null, tint = markContent, modifier = Modifier.size(18.dp))
                    StepStatus.Current -> Text(
                        text = (state.step.ordinal + 1).toString(),
                        color = markContent,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = state.step.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = state.summary?.takeIf { it.isNotBlank() } ?: state.step.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (enabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                )
            }
        }
    }
}
