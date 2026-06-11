package it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.career.isSelectable
import java.io.File

private val CardShape = RoundedCornerShape(28.dp)
private val CareerShape = RoundedCornerShape(18.dp)

/**
 * One account in the switcher roster: avatar, display name and username, plus — for the
 * active account only — its careers as nested sub-cards, with AnimatedVisibility smoothing
 * the expand/collapse as the active flag moves between cards.
 *
 * Active = filled chip on `surfaceContainerHigh`; inactive = the modal's own background
 * color (`surfaceContainerLow`, which is what ModalBottomSheet uses by default) with a
 * hairline `outlineVariant` border, matching the "Aggiungi un altro account" tile so the
 * two outlined slots feel like one family. Both halves animate together when the active
 * selection swaps so the colors keep up with the list's placement slide.
 *
 * Tapping the card opens the profile page when active and switches the active account in
 * place when inactive; the career sub-cards are nested clickables that consume their own
 * gesture, so the card-level tap only fires on the header. Tapping the already-selected
 * career is a deliberate no-op — only a different career triggers a switch.
 */
@Composable
fun ProfileCard(
    account: Account,
    isActive: Boolean,
    photo: File?,
    onOpenDetails: () -> Unit,
    onSwitchAccount: () -> Unit,
    onSelectCareer: (CareerId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val careers = account.academic.careers.sortedByDescending { it.status.isSelectable }
    val selectedCareerId = account.academic.selectedCareerId

    val containerColor by animateColorAsState(
        targetValue = if (isActive) scheme.surfaceContainerHigh else scheme.surfaceContainerLow,
        animationSpec = motion.defaultEffectsSpec(),
        label = "ProfileCardContainer",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) Color.Transparent else scheme.outlineVariant,
        animationSpec = motion.defaultEffectsSpec(),
        label = "ProfileCardBorder",
    )
    val elevation by animateDpAsState(
        targetValue = if (isActive) 3.dp else 0.dp,
        animationSpec = motion.defaultEffectsSpec(),
        label = "ProfileCardElevation",
    )

    Surface(
        onClick = if (isActive) onOpenDetails else onSwitchAccount,
        shape = CardShape,
        color = containerColor,
        border = BorderStroke(width = 1.dp, color = borderColor),
        tonalElevation = elevation,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            ProfileHeader(account = account, isActive = isActive, photo = photo)

            AnimatedVisibility(
                visible = isActive,
                enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()),
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        careers.forEach { career ->
                            CareerSubCard(
                                career = career,
                                selected = career.id == selectedCareerId,
                                onClick = {
                                    if (career.id != selectedCareerId) onSelectCareer(career.id)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Avatar (slightly larger when active), display name and username. Inactive accounts get a
 * non-interactive unfilled radio dot as the "switch to me" affordance — the whole card
 * handles the click — while the active card needs no trailing glyph because the filled
 * background and career slot already read as the selected state.
 */
@Composable
private fun ProfileHeader(
    account: Account,
    isActive: Boolean,
    photo: File?,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val avatarSize by animateDpAsState(
        targetValue = if (isActive) 56.dp else 48.dp,
        animationSpec = motion.defaultSpatialSpec(),
        label = "ProfileAvatarSize",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AccountAvatar(photo = photo, size = avatarSize)
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
        if (!isActive) {
            RadioButton(
                selected = false,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    unselectedColor = scheme.outline,
                ),
            )
        }
    }
}

/**
 * One career under the active account: description, matricola and academic year, with a
 * status chip for noteworthy statuses. Selected = primary container plus a trailing check;
 * selectable = neutral container; ended careers sit muted and disabled.
 */
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
                        text = stringResource(
                            R.string.account_matricola_year,
                            career.studentNumber,
                            career.academicYear,
                        ),
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
                    contentDescription = stringResource(R.string.account_switcher_career_active),
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

/** Chip copy for statuses worth flagging; null (regular active or unknown) renders no chip. */
private fun statusLabel(status: CareerStatus): String? = when (status) {
    CareerStatus.ACTIVE -> null
    CareerStatus.SUSPENDED -> "Sospesa" // TODO: externalize to resources
    CareerStatus.GRADUATED -> "Conclusa" // TODO: externalize to resources
    CareerStatus.INTERRUPTED -> "Interrotta" // TODO: externalize to resources
    CareerStatus.OTHER -> null
}
