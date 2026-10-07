package it.attendance100.mybicocca.ui.screen.elearning

import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.feedback.AppSnackbarController
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.AddCoursePage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.AddCourseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * The e-learning tab's sheets: the add-course catalog browser. Its enrol outcomes outlive the
 * sheet, which closes as they arrive, so they are raised on the shell's [snackbarController] from
 * the shell's [scope] — the sheet's own snackbar surface and coroutine scope go away with it. A
 * successful enrolment also asks [elearningViewModel] to reveal the new course in the list.
 * [addCourseViewModel] is shell-scoped (like the tab's own ViewModel) so an enrolment in flight is
 * not cancelled when the sheet closes, and the catalog is not refetched on every open.
 */
fun EntryProviderScope<NavKey>.elearningSheetEntries(
    elearningViewModel: ElearningViewModel,
    addCourseViewModel: AddCourseViewModel,
    snackbarController: AppSnackbarController,
    scope: CoroutineScope,
    onRequireSignIn: () -> Unit = {},
) {
    entry<SheetRoute.AddCourse> {
        val strEnrolledSuccess = stringResource(R.string.elearning_enrolled_success)
        val strEnrolFailedWithReason = stringResource(R.string.elearning_enrol_failed_with_reason)
        val strEnrolFailed = stringResource(R.string.elearning_enrol_failed)
        val strNetworkUnavailable = stringResource(R.string.elearning_error_network_unavailable)
        val strNetworkTimeout = stringResource(R.string.elearning_error_network_timeout)
        val strNetwork = stringResource(R.string.elearning_error_network)

        AddCoursePage(
            onEnrolFailed = { cause ->
                val reason = when (cause) {
                    is UnknownHostException, is ConnectException -> strNetworkUnavailable
                    is SocketTimeoutException -> strNetworkTimeout
                    is IOException -> strNetwork
                    else -> cause.message?.takeIf { it.isNotBlank() }
                }
                val message = if (reason != null) strEnrolFailedWithReason.format(reason) else strEnrolFailed
                scope.launch { snackbarController.showError(message) }
            },
            onEnrolSucceeded = { courseId, name ->
                scope.launch { snackbarController.showInfo(strEnrolledSuccess.format(name)) }
                elearningViewModel.revealEnrolledCourse(courseId)
            },
            onRequireSignIn = onRequireSignIn,
            viewModel = addCourseViewModel,
        )
    }
}
