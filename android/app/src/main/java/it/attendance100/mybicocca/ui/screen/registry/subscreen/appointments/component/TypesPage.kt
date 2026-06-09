package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import it.attendance100.mybicocca.ui.component.directory.SegmentedIconChip
import it.attendance100.mybicocca.ui.component.directory.SegmentedTile
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.ext.directoryIcon
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.ext.displayName
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.ext.durationLabel

// The bookable desks inside one macro section: tapping one opens its booking wizard.
@Composable
internal fun TypesPage(
    services: List<AppointmentService>,
    onStartBooking: (AppointmentService) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 720.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        services.forEachIndexed { index, service ->
            SegmentedTile(
                isFirst = index == 0,
                isLast = index == services.lastIndex,
                title = service.displayName,
                subtitle = service.durationLabel,
                onClick = { onStartBooking(service) },
                leading = {
                    SegmentedIconChip(
                        icon = service.directoryIcon,
                        container = scheme.primaryContainer,
                        onContainer = scheme.onPrimaryContainer,
                    )
                },
                trailing = {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp),
                    )
                },
            )
        }
    }
}
