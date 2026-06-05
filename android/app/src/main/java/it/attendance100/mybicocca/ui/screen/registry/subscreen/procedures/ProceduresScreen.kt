package it.attendance100.mybicocca.ui.screen.registry.subscreen.procedures

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AltRoute
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.MoreTime
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
import androidx.compose.runtime.rememberCoroutineScope
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import kotlinx.coroutines.launch

// Landing for the "Domande e procedure" tile: a directory of segreteria procedures.
// Cambio percorso and proroga are in place; appuntamenti is deferred to the EasyStaff
// integration, so it announces itself rather than opening the empty stub.
@Composable
fun ProceduresScreen(
    onOpenCourseChange: () -> Unit,
    onOpenEnrollmentExtension: () -> Unit,
) {
    val snackbar = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProcedureRow(
            icon = Icons.Outlined.AltRoute,
            title = "Cambio percorso",
            subtitle = "Passaggio a un altro corso di studi",
            onClick = onOpenCourseChange,
        )
        ProcedureRow(
            icon = Icons.Outlined.MoreTime,
            title = "Proroga iscrizione",
            subtitle = "Estendi i termini di iscrizione",
            onClick = onOpenEnrollmentExtension,
        )
        ProcedureRow(
            icon = Icons.Outlined.EventAvailable,
            title = "Appuntamenti",
            subtitle = "Prenota uno sportello",
            comingSoon = true,
            onClick = { scope.launch { snackbar.showInfo("Questa funzione sarà disponibile a breve.") } },
        )
    }
}

@Composable
private fun ProcedureRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    comingSoon: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = scheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(scheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = scheme.onSecondaryContainer, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                )
                Text(
                    text = if (comingSoon) "Presto disponibile" else subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
