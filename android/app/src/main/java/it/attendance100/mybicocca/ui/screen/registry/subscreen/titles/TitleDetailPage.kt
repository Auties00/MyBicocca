package it.attendance100.mybicocca.ui.screen.registry.subscreen.titles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.model.document.TitleStatus
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.ext.TitleSection
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.ext.icon
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.ext.label
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.ext.section

// Headerless title detail page hosted inside the sheet pager (the hosting sheet provides the
// morphing header): the populated fields grouped into icon-chip-headed cards with divider-
// separated key/value rows — the same detail language as the Iscrizioni year detail. The grade
// and status (promoted fields the mapper keeps off `attributes`) fold in as rows of the
// Valutazione / Conseguimento sections so no data is dropped.
@Composable
internal fun TitleDetailPage(
    title: AcademicTitle,
    modifier: Modifier = Modifier,
) {
    val sections = remember(title) { title.detailSections() }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(sections) { section ->
            DetailGroup(icon = section.icon, title = section.title, rows = section.rows)
        }
    }
}

private data class DetailSectionData(
    val title: String,
    val icon: ImageVector,
    val rows: List<Pair<String, String>>,
)

// Buckets the title's attribute rows into the fixed section order, then folds the promoted
// grade (Valutazione) and status (Conseguimento) in at the head of their sections. Sections with
// nothing to show are dropped.
private fun AcademicTitle.detailSections(): List<DetailSectionData> {
    val gradeRow = grade?.let { "Voto" to (if (cumLaude) "$it e lode" else it) }
    val statusRow = when (status) {
        TitleStatus.Awarded -> "Stato" to "Conseguito"
        TitleStatus.Hypothesised -> "Stato" to "In ipotesi"
        TitleStatus.Unknown -> null
    }

    return TitleSection.entries.mapNotNull { section ->
        val attributeRows = attributes
            .filter { it.field.section == section }
            .map { it.field.label(category) to it.value }
        val promoted = when (section) {
            TitleSection.Achievement -> listOfNotNull(statusRow)
            TitleSection.Evaluation -> listOfNotNull(gradeRow)
            else -> emptyList()
        }
        (promoted + attributeRows).takeIf { it.isNotEmpty() }
            ?.let { DetailSectionData(section.label, section.icon, it) }
    }
}

// One topic: an icon-chip header above a single tonal container holding the fields as
// divider-separated key/value rows — identical to the Iscrizioni year detail.
@Composable
private fun DetailGroup(
    icon: ImageVector,
    title: String,
    rows: List<Pair<String, String>>,
) {
    if (rows.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        GroupHeader(icon = icon, title = title)
        Spacer(Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                rows.forEachIndexed { index, (label, value) ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                    }
                    KeyValueRow(label = label, value = value)
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(icon: ImageVector, title: String) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(scheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.onSecondaryContainer,
                modifier = Modifier.size(17.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
        )
    }
}

@Composable
private fun KeyValueRow(label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f),
        )
    }
}
