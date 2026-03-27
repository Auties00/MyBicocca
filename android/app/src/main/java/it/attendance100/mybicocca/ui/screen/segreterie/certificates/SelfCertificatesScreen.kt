package it.attendance100.mybicocca.ui.screen.segreterie.certificates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.AutoScrollingFilterRow

enum class SelfCertificatesLanguage {
    All,
    Italian,
    English,
    Unknown,
}

data class Document(
    val name: String,
    val url: String = "",
) {
    val language: SelfCertificatesLanguage = when {
        name.startsWith("Autodichiarazione", ignoreCase = true) -> SelfCertificatesLanguage.Italian
        name.startsWith("Self-declaration", ignoreCase = true) -> SelfCertificatesLanguage.English
        else -> SelfCertificatesLanguage.Unknown
    }
}

@Composable
fun SelfCertificatesLanguage.label(): String {
    return when (this) {
        SelfCertificatesLanguage.All -> stringResource(R.string.segreterie_self_certificates_filter_all)
        SelfCertificatesLanguage.Italian -> stringResource(R.string.language_italiano)
        SelfCertificatesLanguage.English -> stringResource(R.string.language_english)
        SelfCertificatesLanguage.Unknown -> stringResource(R.string.segreterie_self_certificates_filter_unknown)
    }
}

@Composable
fun SelfCertificatesScreen() {
    var selectedLang by remember { mutableStateOf(SelfCertificatesLanguage.All) }

    val documents = remember {
        listOf(
            Document("Autodichiarazione Iscrizione"),
            Document("Autodichiarazione Iscrizione con Esami"),
            Document("Autodichiarazione Laurea"),
            Document("Autodichiarazione Laurea con Esami"),
            Document("Self-declaration of Enrollment"),
            Document("Self-declaration of Enrollment with Exams"),
            Document("Self-declaration of Degree"),
            Document("Self-declaration of Degree with Exams"),
        )
    }

    val availableLanguages = remember(documents) {
        val hasUnknown = documents.any { it.language == SelfCertificatesLanguage.Unknown }
        if (hasUnknown) SelfCertificatesLanguage.entries
        else SelfCertificatesLanguage.entries.filter { it != SelfCertificatesLanguage.Unknown }
    }

    val filteredDocuments = remember(selectedLang) {
        if (selectedLang == SelfCertificatesLanguage.All) documents
        else documents.filter { it.language == selectedLang }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Filter Chips
        AutoScrollingFilterRow(
            startPadding = 8.dp,
            contentPadding = PaddingValues(bottom = 4.dp),
            items = availableLanguages,
            selectedItem = selectedLang,
            onSelectionChanged = { selectedLang = it },
            labelProvider = { it.label() },
            leadingIcon = { lang, isSelected, _ ->
                val vector = if (isSelected) {
                    when (lang) {
                        SelfCertificatesLanguage.Italian -> R.drawable.italy_flag
                        SelfCertificatesLanguage.English -> R.drawable.uk_flag
                        else -> null
                    }
                } else null

                if (vector != null) {
                    {
                        Icon(
                            painter = painterResource(vector),
                            contentDescription = lang.label(),
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            tint = Color.Unspecified,
                        )
                    }
                } else null
            },
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(filteredDocuments) { doc ->
                Card(
                    onClick = { /* TODO: open document */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        // Document Preview Placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp),
                            )
                        }

                        // Info Area
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            // Name
                            val displayName = doc.name
                                .replace("Autodichiarazione ", "", ignoreCase = true)
                                .replace("Self-declaration of ", "", ignoreCase = true)
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                modifier = Modifier.weight(1f),
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Language Flag
                            val flagRes = when (doc.language) {
                                SelfCertificatesLanguage.Italian -> R.drawable.italy_flag
                                SelfCertificatesLanguage.English -> R.drawable.uk_flag
                                else -> null
                            }

                            if (flagRes != null) {
                                Icon(
                                    painter = painterResource(flagRes),
                                    contentDescription = doc.language.label(),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    tint = Color.Unspecified,
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
