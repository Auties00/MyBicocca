package it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.isSelectable
import it.attendance100.mybicocca.ui.screen.account.AccountViewModel

@Composable
fun AccountSwitcherPopup(
    onDismiss: () -> Unit,
    onAddAccount: (returnTo: Account) -> Unit,
    onManageAccount: (account: Account) -> Unit,
    onOpenProfile: () -> Unit = {},
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val active by viewModel.activeAccount.collectAsStateWithLifecycle()
    val photo by viewModel.userPhoto.collectAsStateWithLifecycle()
    val activeAccount = active ?: return

    val activeCareer = activeAccount.academic.careers
        .firstOrNull { it.id == activeAccount.academic.selectedCareerId }
    val careers = activeAccount.academic.careers
        .sortedByDescending { it.status.isSelectable }
    val others = accounts.filter { it.id != activeAccount.id }

    val state = remember { MutableTransitionState(false).apply { targetState = true } }
    val tx = rememberTransition(state, label = "account-switcher")
    val motion = MaterialTheme.motionScheme

    val scale by tx.animateFloat(
        transitionSpec = { motion.slowSpatialSpec() },
        label = "scale",
    ) { if (it) 1f else 0.92f }
    val contentAlpha by tx.animateFloat(
        transitionSpec = { motion.slowEffectsSpec() },
        label = "content-alpha",
    ) { if (it) 1f else 0f }
    val backdropAlpha by tx.animateFloat(
        transitionSpec = { motion.slowEffectsSpec() },
        label = "backdrop-alpha",
    ) { if (it) 0.4f else 0f }

    LaunchedEffect(state.currentState, state.isIdle) {
        if (!state.currentState && state.isIdle) onDismiss()
    }

    val requestDismiss: () -> Unit = remember { { state.targetState = false } }

    Dialog(
        onDismissRequest = requestDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = backdropAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = requestDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .padding(horizontal = 24.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = contentAlpha
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(top = 8.dp, bottom = 12.dp),
                ) {
                    HeaderRow(onClose = requestDismiss)
                    Spacer(Modifier.height(8.dp))
                    ActiveAccountHero(
                        account = activeAccount,
                        activeCareer = activeCareer,
                        photo = photo?.absolutePath,
                        onClick = {
                            onManageAccount(activeAccount)
                            requestDismiss()
                        },
                    )

                    if (careers.size > 1) {
                        Spacer(Modifier.height(20.dp))
                        SectionLabel("Carriere")
                        CareersSegmented(
                            careers = careers,
                            selectedId = activeAccount.academic.selectedCareerId,
                            onPick = { careerId ->
                                if (careerId != activeAccount.academic.selectedCareerId) {
                                    viewModel.switchCareer(activeAccount.id, careerId)
                                }
                                requestDismiss()
                            },
                        )
                    }

                    if (others.isNotEmpty()) {
                        Spacer(Modifier.height(20.dp))
                        SectionLabel("Altri account")
                        others.forEach { other ->
                            OtherAccountRow(
                                account = other,
                                onClick = {
                                    viewModel.switchAccount(other.id)
                                    requestDismiss()
                                },
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    ActionMenuItem(
                        icon = Icons.Outlined.Person,
                        label = "Profilo",
                        onClick = {
                            onOpenProfile()
                            requestDismiss()
                        },
                    )
                    ActionMenuItem(
                        icon = Icons.Filled.PersonAdd,
                        label = "Aggiungi un altro account",
                        onClick = {
                            onAddAccount(activeAccount)
                            requestDismiss()
                        },
                    )
                    ActionMenuItem(
                        icon = Icons.Outlined.ManageAccounts,
                        label = "Gestisci gli account",
                        onClick = {
                            onManageAccount(activeAccount)
                            requestDismiss()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Chiudi")
        }
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun ActiveAccountHero(
    account: Account,
    activeCareer: Career?,
    photo: String?,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Avatar(photo = photo, size = 72)
            Spacer(Modifier.height(12.dp))
            Text(
                text = account.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = account.username,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (activeCareer != null) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = "${activeCareer.matricola} · ${activeCareer.description}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CareersSegmented(
    careers: List<Career>,
    selectedId: CareerId,
    onPick: (CareerId) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        careers.forEachIndexed { index, career ->
            val shape = segmentedShape(index, careers.size)
            CareerSegment(
                career = career,
                selected = career.id == selectedId,
                shape = shape,
                onClick = if (career.status.isSelectable) {
                    { onPick(career.id) }
                } else null,
            )
            if (index != careers.lastIndex) Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun CareerSegment(
    career: Career,
    selected: Boolean,
    shape: RoundedCornerShape,
    onClick: (() -> Unit)?,
) {
    val scheme = MaterialTheme.colorScheme
    val container = when {
        selected -> scheme.primaryContainer
        onClick == null -> scheme.surfaceContainerLow
        else -> scheme.surfaceContainer
    }
    val contentColor = when {
        selected -> scheme.onPrimaryContainer
        onClick == null -> scheme.onSurfaceVariant
        else -> scheme.onSurface
    }
    Surface(
        shape = shape,
        color = container,
        contentColor = contentColor,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = career.description.ifEmpty { "Carriera #${career.id.value}" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Matricola ${career.matricola} · A.A. ${career.academicYear}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selezionata",
                    tint = scheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun OtherAccountRow(
    account: Account,
    onClick: () -> Unit,
) {
    ListItem(
        leadingContent = { Avatar(photo = null, size = 36) },
        headlineContent = {
            Text(
                text = account.displayName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = account.username,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
    )
}

@Composable
private fun ActionMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    ListItem(
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        headlineContent = { Text(label) },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun Avatar(photo: String?, size: Int) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(scheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size((size * 0.6f).dp),
            )
        }
    }
}

// Top item rounds the top corners; bottom rounds the bottom; middles stay tight.
// This mirrors the M3 Expressive segmented-list cue without depending on alpha-only APIs.
private fun segmentedShape(index: Int, count: Int): RoundedCornerShape {
    val large = 20.dp
    val small = 4.dp
    return when {
        count == 1 -> RoundedCornerShape(large)
        index == 0 -> RoundedCornerShape(topStart = large, topEnd = large, bottomStart = small, bottomEnd = small)
        index == count - 1 -> RoundedCornerShape(topStart = small, topEnd = small, bottomStart = large, bottomEnd = large)
        else -> RoundedCornerShape(small)
    }
}
