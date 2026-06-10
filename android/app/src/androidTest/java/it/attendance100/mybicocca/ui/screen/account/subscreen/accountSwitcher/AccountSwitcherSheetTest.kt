package it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.verify
import it.attendance100.mybicocca.core.os.ProvideHapticManager
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant

/**
 * State and behaviour coverage for the account switcher's roster scene ([AccountsScene]), the
 * stateless content the modal sheet hosts. Rendering the scene directly avoids the sheet's modal
 * window and its 800ms seekable scene transition, neither of which settle deterministically under
 * Robolectric. The active card surfaces its careers, an inactive card offers a switch, and the
 * "Aggiungi un altro account" tile closes the list. Verifies the roster, the active and inactive
 * account rows and the add-account tile render, that tapping an inactive row invokes the
 * switch-account callback, and that tapping the add-account tile invokes the add-account callback.
 * Anchored on [AccountSwitcherTestTags], wrapped in the production [BicoccaTheme] with a
 * [ProvideHapticManager] the swipe-to-remove rows require, and driven by relaxed-mock callbacks.
 */
@RunWith(AndroidJUnit4::class)
class AccountSwitcherSheetTest {

    @get:Rule
    val compose = createComposeRule()

    private val activeId = AccountId("acc-1")
    private val otherId = AccountId("acc-2")
    private val careerA = CareerId(101L)

    private fun career(id: CareerId, description: String) = Career(
        id = id,
        enrollmentTraitId = 1L,
        programId = 2L,
        easyStaffProgramCode = "E32",
        academicYearEnrollmentId = 3L,
        studentNumber = "900001",
        description = description,
        academicYear = 2024,
        status = CareerStatus.ACTIVE,
    )

    private fun account(id: AccountId, careers: List<Career>, selected: CareerId) = Account(
        id = id,
        username = "user-${id.value}",
        displayName = "User ${id.value}",
        academic = AcademicIdentity(
            recordUserId = "rec-${id.value}",
            personId = 7L,
            fiscalCode = null,
            careers = careers,
            selectedCareerId = selected,
        ),
        learning = LearningIdentity(
            lmsUserId = 11,
            lmsUsername = "user-${id.value}",
            locale = "it",
            isSiteAdmin = false,
            maxUploadFileSizeBytes = 0L,
            storageQuotaBytes = 0L,
        ),
        createdAt = Instant.EPOCH,
        lastUsedAt = Instant.EPOCH,
        lastSyncedAt = Instant.EPOCH,
    )

    private val active = account(activeId, listOf(career(careerA, "Informatica")), careerA)
    private val other = account(otherId, listOf(career(careerA, "Fisica")), careerA)

    private val onSwitchAccount: (AccountId) -> Unit = mockk(relaxed = true)
    private val onSelectCareer: (AccountId, CareerId) -> Unit = mockk(relaxed = true)
    private val onRemove: (Account) -> Unit = mockk(relaxed = true)
    private val onAddAccount: () -> Unit = mockk(relaxed = true)
    private val onOpenDetails: () -> Unit = mockk(relaxed = true)
    private val onOpenSettings: () -> Unit = mockk(relaxed = true)

    private fun setRoster() {
        compose.setContent {
            BicoccaTheme(dark = false) {
                ProvideHapticManager {
                    AccountsScene(
                        modifier = Modifier,
                        ordered = listOf(active, other),
                        activeId = activeId,
                        photos = emptyMap<AccountId, File?>(),
                        maxListHeight = 600.dp,
                        motion = MaterialTheme.motionScheme,
                        onOpenDetails = onOpenDetails,
                        onOpenSettings = onOpenSettings,
                        onSwitchAccount = onSwitchAccount,
                        onSelectCareer = onSelectCareer,
                        onRemove = onRemove,
                        onAddAccount = onAddAccount,
                    )
                }
            }
        }
    }

    @Test
    fun the_roster_renders_the_active_and_inactive_account_rows_and_the_add_account_tile() {
        setRoster()

        compose.onNodeWithTag(AccountSwitcherTestTags.ROSTER).assertIsDisplayed()
        compose.onNodeWithTag(AccountSwitcherTestTags.account(activeId)).assertIsDisplayed()
        compose.onNodeWithTag(AccountSwitcherTestTags.account(otherId)).assertIsDisplayed()
        compose.onNodeWithTag(AccountSwitcherTestTags.ADD_ACCOUNT).assertIsDisplayed()
    }

    @Test
    fun tapping_an_inactive_account_row_invokes_the_switch_account_callback() {
        setRoster()

        compose.onNodeWithTag(AccountSwitcherTestTags.account(otherId)).performClick()
        compose.waitForIdle()

        verify { onSwitchAccount(otherId) }
    }

    @Test
    fun tapping_the_add_account_tile_invokes_the_add_account_callback() {
        setRoster()

        compose.onNodeWithTag(AccountSwitcherTestTags.ADD_ACCOUNT).performClick()
        compose.waitForIdle()

        verify { onAddAccount() }
    }

    @Test
    fun tapping_the_settings_shortcut_invokes_the_open_settings_callback() {
        setRoster()

        compose.onNodeWithTag(AccountSwitcherTestTags.SETTINGS_SHORTCUT).performClick()
        compose.waitForIdle()

        verify { onOpenSettings() }
    }
}
