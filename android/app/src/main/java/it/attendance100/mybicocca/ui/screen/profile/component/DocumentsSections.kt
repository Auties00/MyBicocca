package it.attendance100.mybicocca.ui.screen.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.model.document.CertificateType
import it.attendance100.mybicocca.domain.model.document.TitleCategory
import it.attendance100.mybicocca.domain.model.document.TitleStatus

// Grouped academic titles (Maturità -> Italian -> Foreign), rendered as connected,
// segmented stacks. Plain data composable — no ViewModel.
@Composable
fun TitlesSection(titles: List<AcademicTitle>) {
    // Titles ordered HighSchool -> Italian -> Foreign, matching life sequence.
    val grouped = remember(titles) {
        titles.groupBy { it.category }
            .toSortedMap(compareBy { it.ordinal })
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        grouped.forEach { (category, items) ->
            TitleGroup(title = category.label(), items = items)
        }
    }
}

// Self-declaration certificates rendered as a connected, segmented stack. Plain data
// composable — no ViewModel.
@Composable
fun CertificatesSection(
    certificates: List<Certificate>,
    downloading: Set<CertificateId>,
    onDownload: (Certificate) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        certificates.forEachIndexed { index, cert ->
            CertificateTile(
                certificate = cert,
                isFirst = index == 0,
                isLast = index == certificates.lastIndex,
                downloading = cert.id in downloading,
                onDownload = { onDownload(cert) },
            )
        }
    }
}

@Composable
private fun CertificateTile(
    certificate: Certificate,
    isFirst: Boolean,
    isLast: Boolean,
    downloading: Boolean,
    onDownload: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val large = 20.dp
    val small = 4.dp
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (isFirst) 0.dp else 2.dp)
            .clickable(enabled = !downloading, onClick = onDownload),
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
            modifier = Modifier.padding(start = 14.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(scheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = certificate.type.icon(),
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = certificate.description.ifBlank { "Certificato" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                certificate.supportingLine()?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                if (downloading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Rounded.FileDownload,
                        contentDescription = "Scarica certificato",
                        tint = scheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

// One title family rendered as a connected, segmented stack (header tile + sub-tiles with
// 2.dp seams), matching the registry directory / study-plan aesthetic.
@Composable
private fun TitleGroup(title: String, items: List<AcademicTitle>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        TitleGroupHeader(title = title, count = items.size)
        items.forEachIndexed { index, item ->
            TitleTile(item = item, isLast = index == items.lastIndex)
        }
    }
}

@Composable
private fun TitleGroupHeader(title: String, count: Int) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (count == 1) "1 titolo" else "$count titoli",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TitleTile(item: AcademicTitle, isLast: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val large = 20.dp
    val small = 4.dp
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(
            topStart = small,
            topEnd = small,
            bottomStart = if (isLast) large else small,
            bottomEnd = if (isLast) large else small,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(scheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.category.icon(),
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.headline(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                item.supportingLine()?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusChip(item.status)
                    item.grade?.let { GradeChip(it, item.cumLaude) }
                    if (item.valueDeclarationFiled) {
                        InfoChip("Dichiarazione di valore")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: TitleStatus) {
    val scheme = MaterialTheme.colorScheme
    val text = when (status) {
        TitleStatus.Awarded -> "Conseguito"
        TitleStatus.Hypothesised -> "In ipotesi"
        TitleStatus.Unknown -> return
    }
    val container = when (status) {
        TitleStatus.Awarded -> scheme.tertiaryContainer
        else -> scheme.secondaryContainer
    }
    val content = when (status) {
        TitleStatus.Awarded -> scheme.onTertiaryContainer
        else -> scheme.onSecondaryContainer
    }
    Chip(text = text, container = container, content = content)
}

@Composable
private fun GradeChip(grade: String, cumLaude: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val text = if (cumLaude) "$grade e lode" else grade
    Chip(text = text, container = scheme.primaryContainer, content = scheme.onPrimaryContainer)
}

@Composable
private fun InfoChip(text: String) {
    val scheme = MaterialTheme.colorScheme
    Chip(text = text, container = scheme.surfaceContainerHighest, content = scheme.onSurfaceVariant)
}

@Composable
private fun Chip(text: String, container: Color, content: Color) {
    Surface(shape = CircleShape, color = container, contentColor = content) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

private fun TitleCategory.label(): String = when (this) {
    TitleCategory.HighSchool -> "Maturità"
    TitleCategory.Italian -> "Titoli italiani"
    TitleCategory.Foreign -> "Titoli esteri"
}

private fun TitleCategory.icon() = when (this) {
    TitleCategory.HighSchool -> Icons.Outlined.School
    TitleCategory.Italian -> Icons.Rounded.WorkspacePremium
    TitleCategory.Foreign -> Icons.Rounded.Cached
}

private fun AcademicTitle.headline(): String =
    subject?.takeIf { it.isNotBlank() }
        ?: typeDescription?.takeIf { it.isNotBlank() }
        ?: institution
        ?: "Titolo"

private fun AcademicTitle.supportingLine(): String? {
    val parts = buildList {
        // Avoid repeating the headline as the supporting line.
        typeDescription?.takeIf { it.isNotBlank() && it != headline() }?.let { add(it) }
        institution?.takeIf { it.isNotBlank() && it != headline() }?.let { add(it) }
        country?.takeIf { it.isNotBlank() }?.let { add(it) }
        year?.let { add(it.toString()) }
    }
    return parts.joinToString(" · ").takeIf { it.isNotBlank() }
}

private fun CertificateType.icon() = when (this) {
    CertificateType.Enrolment -> Icons.Rounded.School
    CertificateType.DegreeAward -> Icons.Rounded.WorkspacePremium
    CertificateType.TuitionFees -> Icons.Rounded.Receipt
    CertificateType.Other -> Icons.Rounded.Description
}

private fun Certificate.supportingLine(): String? {
    val parts = buildList {
        solarYear?.let { add("Anno $it") }
        if (digitallySigned) add("Firma digitale")
    }
    return parts.joinToString(" · ").takeIf { it.isNotBlank() }
}
