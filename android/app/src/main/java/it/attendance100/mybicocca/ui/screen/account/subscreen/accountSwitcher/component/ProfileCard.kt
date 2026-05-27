package it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.career.isSelectable
import java.io.File

private val CardShape = RoundedCornerShape(28.dp)
private val CareerShape = RoundedCornerShape(18.dp)

@Composable
fun ProfileCard(
    account: Account,
    isActive: Boolean,
    photo: File?,
    onOpenDetails: () -> Unit,
    onSelectCareer: (CareerId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val careers = account.academic.careers.sortedByDescending { it.status.isSelectable }
    val selectedCareerId = account.academic.selectedCareerId

    // onClick on the Surface itself so the whole card is tappable and the ripple is clipped
    // to CardShape. The career sub-cards are nested clickables, so tapping one selects that
    // career instead of opening details (the inner click consumes the gesture).
    Surface(
        onClick = onOpenDetails,
        shape = CardShape,
        color = if (isActive) scheme.surfaceContainerHighest else scheme.surfaceContainerHigh,
        tonalElevation = if (isActive) 3.dp else 0.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            ProfileHeader(account = account, isActive = isActive, photo = photo)

            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                careers.forEach { career ->
                    CareerSubCard(
                        career = career,
                        selected = career.id == selectedCareerId,
                        onClick = { onSelectCareer(career.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    account: Account,
    isActive: Boolean,
    photo: File?,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AccountAvatar(photo = photo, size = if (isActive) 56.dp else 48.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = account.username,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CareerSubCard(
    career: Career,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val selectable = career.status.isSelectable
    val container = when {
        selected -> scheme.primaryContainer
        selectable -> scheme.surfaceContainer
        else -> scheme.surfaceContainerLow
    }
    val titleColor = when {
        selected -> scheme.onPrimaryContainer
        selectable -> scheme.onSurface
        else -> scheme.onSurfaceVariant
    }
    val supportColor = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        enabled = selectable,
        shape = CareerShape,
        color = container,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = career.description.ifEmpty { "Carriera #${career.id.value}" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Matricola ${career.matricola} · A.A. ${career.academicYear}",
                        style = MaterialTheme.typography.bodySmall,
                        color = supportColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    statusLabel(career.status)?.let { label ->
                        StatusChip(label = label, active = selected)
                    }
                }
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Carriera attiva",
                    tint = scheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, active: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(50),
        color = if (active) scheme.onPrimaryContainer.copy(alpha = 0.16f) else scheme.surfaceContainerHighest,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

private fun statusLabel(status: CareerStatus): String? = when (status) {
    CareerStatus.ACTIVE -> null
    CareerStatus.SUSPENDED -> "Sospesa"
    CareerStatus.GRADUATED -> "Conclusa"
    CareerStatus.INTERRUPTED -> "Interrotta"
    CareerStatus.OTHER -> null
}
