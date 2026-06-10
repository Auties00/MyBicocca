package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.verify
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamEnrollmentWindow
import it.attendance100.mybicocca.domain.model.exam.ExamType
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingCourseGroup
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingTarget
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * State + behaviour coverage for the booking sub-flow pages ([ExamCalendarPage], [CallPage],
 * [ConfirmPage]). These are stateless content composables, so each is rendered directly with
 * fixture data and relaxed-mock callbacks rather than through a ViewModel. Anchored on
 * [BookingTestTags] and wrapped in the production [BicoccaTheme]; covers the calendar's
 * loading / empty / error / content branches, the per-call tap routing to the booking target,
 * and the confirm step forwarding the typed note.
 */
@RunWith(AndroidJUnit4::class)
class BookingPagesTest {

    @get:Rule
    val compose = createComposeRule()

    private fun examCall(examCallId: Long, choiceId: Long? = 99L): ExamCall = ExamCall(
        key = ExamCallKey(courseOfStudyId = 1L, activityId = 2L, callId = examCallId.toInt()),
        examCallId = examCallId,
        activityChoiceId = choiceId,
        activityCode = "E3101Q123",
        activityDescription = "Algoritmi",
        courseOfStudyDescription = "Informatica",
        callDescription = "Appello I",
        callDate = null,
        callTime = null,
        enrollmentWindow = ExamEnrollmentWindow(opensAt = null, closesAt = null),
        enrolledNumber = 0,
        state = null,
        stateDescription = null,
        callType = ExamCallType.Final,
        examType = ExamType.Written,
        isReserved = false,
        matId = null,
        notes = null,
        president = null,
        bookingTypeDescription = null,
    )

    private fun group(call: ExamCall): BookingCourseGroup = BookingCourseGroup(
        courseKey = "E3101Q123",
        courseTitle = "Algoritmi",
        courseCode = "E3101Q123",
        courseOfStudy = "Informatica",
        calls = listOf(call),
    )

    @Test
    fun calendar_shows_the_loading_marker_before_the_calls_settle() {
        compose.setContent {
            BicoccaTheme(dark = false) {
                ExamCalendarPage(
                    loaded = false,
                    groups = null,
                    syncStatus = SyncStatus.Refreshing,
                    pendingFocus = null,
                    onConsumeFocus = {},
                    onRetry = {},
                    onOpenCall = {},
                )
            }
        }

        compose.onNodeWithTag(BookingTestTags.CALENDAR_LOADING).assertIsDisplayed()
    }

    @Test
    fun calendar_shows_the_empty_marker_when_there_are_no_bookable_calls() {
        compose.setContent {
            BicoccaTheme(dark = false) {
                ExamCalendarPage(
                    loaded = true,
                    groups = emptyList(),
                    syncStatus = SyncStatus.Idle,
                    pendingFocus = null,
                    onConsumeFocus = {},
                    onRetry = {},
                    onOpenCall = {},
                )
            }
        }

        compose.onNodeWithTag(BookingTestTags.CALENDAR_EMPTY).assertIsDisplayed()
    }

    @Test
    fun calendar_shows_the_error_marker_on_a_first_load_failure() {
        compose.setContent {
            BicoccaTheme(dark = false) {
                ExamCalendarPage(
                    loaded = false,
                    groups = null,
                    syncStatus = SyncStatus.Failed(IOException("offline")),
                    pendingFocus = null,
                    onConsumeFocus = {},
                    onRetry = {},
                    onOpenCall = {},
                )
            }
        }

        compose.onNodeWithTag(BookingTestTags.CALENDAR_ERROR).assertIsDisplayed()
    }

    @Test
    fun tapping_a_bookable_call_opens_it_with_the_call_as_the_booking_target() {
        val call = examCall(examCallId = 50L)
        val onOpenCall = mockk<(ExamCall) -> Unit>(relaxed = true)

        compose.setContent {
            BicoccaTheme(dark = false) {
                ExamCalendarPage(
                    loaded = true,
                    groups = listOf(group(call)),
                    syncStatus = SyncStatus.Idle,
                    pendingFocus = null,
                    onConsumeFocus = {},
                    onRetry = {},
                    onOpenCall = onOpenCall,
                )
            }
        }

        compose.onNodeWithTag(BookingTestTags.CALENDAR_CONTENT).assertIsDisplayed()
        compose.onNodeWithTag(BookingTestTags.call(50L)).performClick()
        compose.waitForIdle()

        verify { onOpenCall(call) }
    }

    @Test
    fun bookable_call_detail_invokes_onBook_when_its_action_is_tapped() {
        val onBook = mockk<() -> Unit>(relaxed = true)

        compose.setContent {
            BicoccaTheme(dark = false) {
                CallPage(
                    target = BookingTarget(call = examCall(examCallId = 50L), activityChoiceId = 99L),
                    onBook = onBook,
                )
            }
        }

        compose.onNodeWithTag(BookingTestTags.CALL_BOOK_BUTTON).assertIsDisplayed()
        compose.onNodeWithTag(BookingTestTags.CALL_BOOK_BUTTON).performClick()
        compose.waitForIdle()

        verify { onBook() }
    }

    @Test
    fun confirm_step_forwards_the_typed_note_on_confirm() {
        val onConfirm = mockk<(String?) -> Unit>(relaxed = true)

        compose.setContent {
            BicoccaTheme(dark = false) {
                ConfirmPage(submitting = false, onConfirm = onConfirm)
            }
        }

        compose.onNodeWithTag(BookingTestTags.CONFIRM_NOTE_FIELD).performTextInput("vegetariano")
        compose.onNodeWithTag(BookingTestTags.CONFIRM_BUTTON).performClick()
        compose.waitForIdle()

        verify { onConfirm("vegetariano") }
    }
}
