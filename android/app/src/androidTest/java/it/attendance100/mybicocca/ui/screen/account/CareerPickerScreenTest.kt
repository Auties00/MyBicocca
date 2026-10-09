package it.attendance100.mybicocca.ui.screen.account

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.verify
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.testing.setBicoccaContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * Behaviour coverage for the post-sign-in [CareerPickerScreen]: tapping a career, open or ended,
 * only selects it, and the confirm button reports the selection (the account's default career
 * when nothing was tapped) through the pick callback.
 */
@RunWith(AndroidJUnit4::class)
class CareerPickerScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val openCareer = CareerId(101L)
    private val endedCareer = CareerId(102L)

    private fun career(id: CareerId, description: String, status: CareerStatus) = Career(
        id = id,
        enrollmentTraitId = 1L,
        programId = 2L,
        easyStaffProgramCode = "E32",
        academicYearEnrollmentId = 3L,
        studentNumber = "900001",
        description = description,
        academicYear = 2024,
        status = status,
    )

    private val account = Account(
        id = AccountId("acc-1"),
        username = "user-acc-1",
        displayName = "User acc-1",
        academic = AcademicIdentity(
            recordUserId = "rec-acc-1",
            personId = 7L,
            fiscalCode = null,
            careers = listOf(
                career(openCareer, "Informatica", CareerStatus.ACTIVE),
                career(endedCareer, "Matematica", CareerStatus.GRADUATED),
            ),
            selectedCareerId = openCareer,
        ),
        learning = LearningIdentity(
            lmsUserId = 11,
            lmsUsername = "user-acc-1",
            locale = "it",
            isSiteAdmin = false,
            maxUploadFileSizeBytes = 0L,
            storageQuotaBytes = 0L,
        ),
        createdAt = Instant.EPOCH,
        lastUsedAt = Instant.EPOCH,
        lastSyncedAt = Instant.EPOCH,
    )

    private val onPicked: (CareerId) -> Unit = mockk(relaxed = true)

    private fun setPicker() {
        compose.setBicoccaContent {
            CareerPickerScreen(account = account, onPicked = onPicked)
        }
    }

    @Test
    fun confirming_without_touching_the_list_picks_the_default_career() {
        setPicker()

        compose.onNodeWithTag(CareerPickerTestTags.CONFIRM).performClick()
        compose.waitForIdle()

        verify { onPicked(openCareer) }
    }

    @Test
    fun tapping_a_career_only_selects_it() {
        setPicker()

        compose.onNodeWithText("Matematica").performClick()
        compose.waitForIdle()

        verify(exactly = 0) { onPicked(any()) }
    }

    @Test
    fun selecting_an_ended_career_then_confirming_picks_it() {
        setPicker()

        compose.onNodeWithText("Matematica").performClick()
        compose.onNodeWithTag(CareerPickerTestTags.CONFIRM).performClick()
        compose.waitForIdle()

        verify { onPicked(endedCareer) }
    }
}
