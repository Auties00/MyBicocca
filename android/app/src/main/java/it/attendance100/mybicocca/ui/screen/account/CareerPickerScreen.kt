package it.attendance100.mybicocca.ui.screen.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.career.isOpen
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.CareerSubCard
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import java.time.Instant

/** Stable anchors for the career picker's instrumented tests. */
object CareerPickerTestTags {
    const val CONFIRM = "career_picker_confirm"
}

/**
 * Full-screen career chooser, shown right after sign-in when the account carries more than one
 * open career. Laid out like the crash screen (icon, title, one line of instruction, content,
 * bottom action) around the account switcher's career tiles: one list, open careers first, ended
 * ones muted below.
 *
 * Tapping a tile only selects it; the bottom button reports the selection as the [CareerId] so
 * navigation can move on. The account's default career starts selected, so confirming without
 * touching the list keeps the app's own choice.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CareerPickerScreen(
    account: Account,
    onPicked: (CareerId) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val careers = remember(account.academic.careers) {
        account.academic.careers.sortedByDescending { it.status.isOpen }
    }
    var selectedId by rememberSaveable { mutableLongStateOf(account.academic.selectedCareerId.value) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(64.dp))
        Icon(
            imageVector = Icons.Outlined.School,
            contentDescription = null,
            tint = scheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.career_picker_title),
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = scheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.career_picker_instruction),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = scheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    careers.forEach { career ->
                        CareerSubCard(
                            career = career,
                            selected = career.id.value == selectedId,
                            onClick = { selectedId = career.id.value },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onPicked(CareerId(selectedId)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(CareerPickerTestTags.CONFIRM),
        ) {
            Text(stringResource(R.string.common_continue))
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun sampleCareer(id: Long, description: String, year: Int, status: CareerStatus) = Career(
    id = CareerId(id),
    enrollmentTraitId = id,
    programId = id,
    easyStaffProgramCode = "P0$id",
    academicYearEnrollmentId = id,
    studentNumber = "123456",
    description = description,
    academicYear = year,
    status = status,
)

private val SampleAccount = Account(
    id = AccountId("1"),
    username = "m.rossi@campus.unimib.it",
    displayName = "MARIO ROSSI",
    academic = AcademicIdentity(
        recordUserId = "1",
        personId = 1,
        fiscalCode = null,
        careers = listOf(
            sampleCareer(1, "Informatica", 2023, CareerStatus.ACTIVE),
            sampleCareer(2, "Informatica", 2026, CareerStatus.PROVISIONAL),
            sampleCareer(3, "Matematica", 2020, CareerStatus.INTERRUPTED),
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
    createdAt = Instant.EPOCH,
    lastUsedAt = Instant.EPOCH,
    lastSyncedAt = Instant.EPOCH,
)

@Preview(name = "Career picker · Light", showBackground = true)
@Composable
private fun CareerPickerScreenPreview() {
    BicoccaTheme(dark = false) {
        CareerPickerScreen(account = SampleAccount, onPicked = {})
    }
}

@Preview(
    name = "Career picker · Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun CareerPickerScreenDarkPreview() {
    BicoccaTheme(dark = true) {
        CareerPickerScreen(account = SampleAccount, onPicked = {})
    }
}
