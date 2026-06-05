package it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.subscreen.supervisorsSheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.degreeaward.SupervisorAssignment
import it.attendance100.mybicocca.domain.model.degreeaward.SupervisorCandidate
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet

// Relatori: search lecturers (and optionally external subjects) by surname, then assign the
// relatore (first pick) and correlatori. The PUT is irreversible and gated behind an
// existing thesis.
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SupervisorsSheet(
    results: List<SupervisorCandidate>,
    searching: Boolean,
    submitting: Boolean,
    onQueryChange: (query: String, includeExternal: Boolean) -> Unit,
    onSubmit: (List<SupervisorAssignment>) -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var query by remember { mutableStateOf("") }
    var includeExternal by remember { mutableStateOf(false) }
    val picked = remember { mutableStateListOf<SupervisorCandidate>() }

    LaunchedEffect(query, includeExternal) {
        onQueryChange(query, includeExternal)
    }

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) { _, _ ->
        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 760.dp)) {
            Text(
                text = "Relatori",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp),
            )
            Text(
                text = "Il primo selezionato è il relatore, gli altri correlatori.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 10.dp),
            )

            Column(Modifier.padding(horizontal = 22.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Cognome del docente") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                FilterChip(
                    selected = includeExternal,
                    onClick = { includeExternal = !includeExternal },
                    label = { Text("Includi soggetti esterni") },
                )
            }

            if (picked.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Column(Modifier.padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    picked.forEachIndexed { index, candidate ->
                        PickedRow(
                            candidate = candidate,
                            roleLabel = if (index == 0) "Relatore" else "Correlatore",
                            onRemove = { picked.remove(candidate) },
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f, fill = false)) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (searching) {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            LoadingIndicator(modifier = Modifier.size(40.dp))
                        }
                    }
                    results.filter { it !in picked }.forEach { candidate ->
                        ResultRow(candidate = candidate, onAdd = { picked.add(candidate) })
                    }
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(22.dp),
            ) {
                Button(
                    onClick = {
                        onSubmit(
                            picked.mapIndexed { index, c ->
                                SupervisorAssignment(
                                    // First pick is the relatore (REL), the rest correlatori (CORREL).
                                    relationTypeCode = if (index == 0) "REL" else "CORREL",
                                    lecturerId = c.lecturerId,
                                    externalSubjectId = c.externalSubjectId,
                                )
                            }
                        )
                    },
                    enabled = picked.isNotEmpty() && !submitting,
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
                        Text("Assegna relatori", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PickedRow(candidate: SupervisorCandidate, roleLabel: String, onRemove: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.primaryContainer,
        contentColor = scheme.onPrimaryContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(candidate.displayName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Text(roleLabel, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            }
            IconButton(onClick = onRemove) { Icon(Icons.Default.Close, "Rimuovi") }
        }
    }
}

@Composable
private fun ResultRow(candidate: SupervisorCandidate, onAdd: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onAdd,
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(candidate.displayName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                val sub = listOfNotNull(
                    candidate.roleDescription,
                    candidate.departmentDescription,
                ).joinToString(" · ")
                if (sub.isNotBlank()) {
                    Text(sub, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.Add, "Aggiungi", tint = scheme.primary)
        }
    }
}
