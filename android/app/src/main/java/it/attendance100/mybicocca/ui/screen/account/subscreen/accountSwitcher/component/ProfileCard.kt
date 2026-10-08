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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.career.isOpen
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import java.io.File
import java.time.Instant

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
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val careers = account.academic.careers.sortedByDescending { it.status.isOpen }
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
        onClick = {
            haptic.tap()
            if (isActive) onOpenDetails() else onSwitchAccount()
        },
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
 * open = neutral container; ended careers sit muted but can still be picked.
 */
@Composable
private fun CareerSubCard(
    career: Career,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    val ended = !career.status.isOpen
    val container = if (selected) scheme.primaryContainer else scheme.surfaceContainerLow
    val titleColor = when {
        selected -> scheme.onPrimaryContainer
        ended -> scheme.onSurfaceVariant
        else -> scheme.onSurface
    }
    val supportColor = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant

    Surface(
        onClick = { haptic.tap(); onClick() },
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
                    text = career.description.ifEmpty {
                        stringResource(R.string.account_career_fallback, career.id.value)
                    },
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
                    if (career.status != CareerStatus.ACTIVE && career.status != CareerStatus.OTHER) {
                        StatusChip(
                            active = selected,
                            status = career.status,
                        )
                    }
                }
            }

            if (selected) Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.account_switcher_career_active),
                tint = scheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun StatusChip(
    active: Boolean,
    status: CareerStatus,
) {
    val scheme = MaterialTheme.colorScheme
    val contentColor = if (active) scheme.onPrimaryContainer else scheme.onSurfaceVariant
    Surface(
        shape = RoundedCornerShape(50),
        color = if (active) scheme.onPrimaryContainer.copy(alpha = 0.16f) else scheme.surfaceContainerHighest,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            when (status) {
                CareerStatus.SUSPENDED -> {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = stringResource(R.string.enrollment_status_suspended),
                        tint = contentColor,
                        modifier = Modifier.size(16.dp),
                    )
                }
                CareerStatus.INTERRUPTED -> {
                    Icon(
                        painter = painterResource(R.drawable.arrow_cool_down_24px),
                        contentDescription = stringResource(R.string.account_career_status_interrupted),
                        tint = contentColor,
                        modifier = Modifier.size(14.dp).padding(vertical = 0.5.dp),
                    )
                }
                CareerStatus.GRADUATED -> {
                    Icon(
                        painter = painterResource(R.drawable.award_star_ribbon_24px),
                        contentDescription = stringResource(R.string.account_career_status_graduated),
                        tint = contentColor,
                        modifier = Modifier.size(16.dp),
                    )
                }
                CareerStatus.ACTIVE, CareerStatus.OTHER -> {}
            }
        }
    }
}


private val SampleAccount = Account(
    id = AccountId("1"),
    username = "m.rossi@campus.unimib.it",
    displayName = "MARIO ROSSI",
    academic = AcademicIdentity(
        recordUserId = "1",
        personId = 1,
        fiscalCode = "RSSMRA80A01H501U",
        careers = listOf(
            Career(
                id = CareerId(1),
                enrollmentTraitId = 1,
                programId = 1,
                easyStaffProgramCode = "P01",
                academicYearEnrollmentId = 1,
                studentNumber = "123456",
                description = "Informatica",
                academicYear = 2026,
                status = CareerStatus.ACTIVE,
            ),
            Career(
                id = CareerId(3),
                enrollmentTraitId = 3,
                programId = 3,
                easyStaffProgramCode = "P03",
                academicYearEnrollmentId = 3,
                studentNumber = "789012",
                description = "Data Science",
                academicYear = 2025,
                status = CareerStatus.ACTIVE,
            ),
            Career(
                id = CareerId(4),
                enrollmentTraitId = 4,
                programId = 4,
                easyStaffProgramCode = "P04",
                academicYearEnrollmentId = 4,
                studentNumber = "345678",
                description = "Fisica",
                academicYear = 2024,
                status = CareerStatus.SUSPENDED,
            ),
            Career(
                id = CareerId(2),
                enrollmentTraitId = 2,
                programId = 2,
                easyStaffProgramCode = "P02",
                academicYearEnrollmentId = 2,
                studentNumber = "654321",
                description = "Sistemi Informatici",
                academicYear = 2023,
                status = CareerStatus.GRADUATED,
            ),
            Career(
                id = CareerId(5),
                enrollmentTraitId = 5,
                programId = 5,
                easyStaffProgramCode = "P05",
                academicYearEnrollmentId = 5,
                studentNumber = "901234",
                description = "Matematica",
                academicYear = 2021,
                status = CareerStatus.INTERRUPTED,
            ),
            Career(
                id = CareerId(6),
                enrollmentTraitId = 6,
                programId = 6,
                easyStaffProgramCode = "P06",
                academicYearEnrollmentId = 6,
                studentNumber = "567890",
                description = "Biologia",
                academicYear = 2020,
                status = CareerStatus.OTHER,
            ),
        ),
        selectedCareerId = CareerId(1),
    ),
    learning = LearningIdentity(
        lmsUserId = 1,
        lmsUsername = "m.rossi",
        locale = "it",
        isSiteAdmin = false,
        maxUploadFileSizeBytes = 100,
        storageQuotaBytes = 100,
    ),
    createdAt = Instant.now(),
    lastUsedAt = Instant.now(),
    lastSyncedAt = Instant.now(),
)

@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark
)
@Composable
private fun ProfileCardActivePreview() {
    BicoccaTheme(dark = true) {
        ProfileCard(
            account = SampleAccount,
            isActive = true,
            photo = null,
            onOpenDetails = {},
            onSwitchAccount = {},
            onSelectCareer = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun ProfileCardInactiveDarkPreview() {
    BicoccaTheme(dark = true) {
        ProfileCard(
            account = SampleAccount,
            isActive = false,
            photo = null,
            onOpenDetails = {},
            onSwitchAccount = {},
            onSelectCareer = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
