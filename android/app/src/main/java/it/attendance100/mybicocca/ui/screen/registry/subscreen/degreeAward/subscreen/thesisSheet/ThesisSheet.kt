package it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.subscreen.thesisSheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
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
import it.attendance100.mybicocca.domain.model.degreeaward.Thesis
import it.attendance100.mybicocca.domain.model.degreeaward.ThesisDraft
import it.attendance100.mybicocca.domain.model.degreeaward.ThesisType
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet

// Tesi: title (ita/eng), abstract, thesis type, keywords. Saving posts the thesis into the
// application — an irreversible secretariat action gated behind an existing domanda.
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ThesisSheet(
    existing: Thesis?,
    thesisTypes: List<ThesisType>,
    submitting: Boolean,
    onSubmit: (ThesisDraft) -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var titleIta by remember { mutableStateOf(existing?.titleItalian.orEmpty()) }
    var titleEng by remember { mutableStateOf(existing?.titleEnglish.orEmpty()) }
    var abstractIta by remember { mutableStateOf(existing?.abstractItalian.orEmpty()) }
    var typeCode by remember {
        mutableStateOf(thesisTypes.firstOrNull()?.code.orEmpty())
    }
    val keywords = remember { mutableStateListOf<String>().apply { addAll(existing?.keywords.orEmpty()) } }
    var keywordInput by remember { mutableStateOf("") }

    val canSubmit = titleIta.isNotBlank() && typeCode.isNotBlank() && !submitting

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) { _, _ ->
        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 760.dp)) {
            Text(
                text = "Tesi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 8.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (thesisTypes.isNotEmpty()) {
                    Column {
                        Text("Tipologia", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            thesisTypes.forEach { type ->
                                FilterChip(
                                    selected = type.code == typeCode,
                                    onClick = { typeCode = type.code },
                                    label = { Text(type.description) },
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = titleIta,
                    onValueChange = { titleIta = it },
                    label = { Text("Titolo (italiano)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                OutlinedTextField(
                    value = titleEng,
                    onValueChange = { titleEng = it },
                    label = { Text("Titolo (inglese)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                OutlinedTextField(
                    value = abstractIta,
                    onValueChange = { abstractIta = it },
                    label = { Text("Abstract") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )

                Column {
                    Text(
                        "Parole chiave (max 5)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(6.dp))
                    if (keywords.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            keywords.forEach { kw ->
                                InputChip(
                                    selected = false,
                                    onClick = { keywords.remove(kw) },
                                    label = { Text(kw) },
                                    trailingIcon = {
                                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                    },
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = keywordInput,
                            onValueChange = { keywordInput = it },
                            label = { Text("Aggiungi parola chiave") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = {
                                val kw = keywordInput.trim()
                                if (kw.isNotEmpty() && keywords.size < 5 && kw !in keywords) {
                                    keywords.add(kw)
                                    keywordInput = ""
                                }
                            },
                            enabled = keywordInput.isNotBlank() && keywords.size < 5,
                            label = { Text("Aggiungi") },
                        )
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
                            ThesisDraft(
                                titleItalian = titleIta,
                                titleEnglish = titleEng,
                                abstractItalian = abstractIta,
                                abstractEnglish = "",
                                thesisTypeCode = typeCode,
                                languageId = null,
                                keywords = keywords.toList(),
                            )
                        )
                    },
                    enabled = canSubmit,
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
                        Text("Salva tesi", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
