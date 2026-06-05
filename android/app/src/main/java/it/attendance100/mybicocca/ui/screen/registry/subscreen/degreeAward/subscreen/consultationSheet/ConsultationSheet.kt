package it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.subscreen.consultationSheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.degreeaward.DiscussionMode
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet

// Consultazione: public access vs embargo. The PUT updates the deposited thesis and is
// gated behind an existing thesis with attachments.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConsultationSheet(
    modes: List<DiscussionMode>,
    currentCode: String?,
    submitting: Boolean,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var selected by remember { mutableStateOf(currentCode ?: modes.firstOrNull()?.code) }

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) { _, _ ->
        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 640.dp)) {
            Text(
                text = "Consultazione tesi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp),
            )
            Text(
                text = "Scegli come la tua tesi potrà essere consultata dopo la discussione.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 12.dp),
            )

            if (modes.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Lock,
                    title = "Modalità non disponibili",
                    body = "Le modalità di consultazione non sono al momento recuperabili.",
                    modifier = Modifier.height(280.dp),
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    modes.forEach { mode ->
                        ModeOption(
                            mode = mode,
                            selected = mode.code == selected,
                            onSelect = { selected = mode.code },
                        )
                    }
                }
                Column(
                    Modifier.fillMaxWidth().navigationBarsPadding().padding(22.dp),
                ) {
                    Button(
                        onClick = { selected?.let(onSubmit) },
                        enabled = selected != null && !submitting,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = scheme.primary,
                            contentColor = Color.White,
                        ),
                    ) {
                        if (submitting) {
                            LoadingIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Conferma modalità", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeOption(mode: DiscussionMode, selected: Boolean, onSelect: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().selectable(selected = selected, onClick = onSelect),
        color = if (selected) scheme.primaryContainer else scheme.surfaceContainer,
        contentColor = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onSelect)
            Column(Modifier.weight(1f)) {
                Text(mode.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                mode.embargoDays?.takeIf { it > 0 }?.let {
                    Text(
                        "Embargo di $it giorni",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
