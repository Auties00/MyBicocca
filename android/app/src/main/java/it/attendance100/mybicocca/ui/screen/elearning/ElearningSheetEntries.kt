package it.attendance100.mybicocca.ui.screen.elearning

import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.settings.FileOpenChoice
import it.attendance100.mybicocca.ui.component.feedback.AppSnackbarController
import it.attendance100.mybicocca.ui.component.file.FileKind
import it.attendance100.mybicocca.ui.navigation.FileOpenPreferenceViewModel
import it.attendance100.mybicocca.ui.navigation.rememberPopSelf
import it.attendance100.mybicocca.ui.navigation.requireAppNavigator
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.sheetHeader
import it.attendance100.mybicocca.ui.navigation.sheetHeaderInPage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.AddCoursePage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.AddCourseViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.AssignmentDetailPage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.officeOpen.OfficeOpenPage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.officeOpen.officeOpenHeader
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.openChooser.FileOpenChooserContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.openChooser.fileOpenChooserHeader
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.ForumSheetPage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.QuizDetailPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * The e-learning sheets: an activity's quiz, forum and assignment, how a file opens, and the
 * add-course catalog.
 *
 * Files are opened through [openFile] (the shell decides between the in-app viewer, an external
 * app and the chooser; `true` forces the chooser). The chooser remembers choices in
 * [fileOpenViewModel] and hands "open with another app" to [openExternally].
 *
 * The add-course enrol outcomes outlive the sheet, which closes as they arrive, so they are raised
 * on the shell's [snackbarController] from the shell's [scope] — the sheet's own snackbar surface
 * and coroutine scope go away with it. A successful enrolment also asks [elearningViewModel] to
 * reveal the new course in the list. [addCourseViewModel] is shell-scoped so an enrolment in
 * flight is not cancelled when the sheet closes, and the catalog is not refetched on every open.
 */
fun EntryProviderScope<NavKey>.elearningSheetEntries(
    elearningViewModel: ElearningViewModel,
    addCourseViewModel: AddCourseViewModel,
    fileOpenViewModel: FileOpenPreferenceViewModel,
    snackbarController: AppSnackbarController,
    scope: CoroutineScope,
    openFile: (file: AppRoute.FileViewer, forceChooser: Boolean) -> Unit,
    openExternally: (AppRoute.FileViewer) -> Unit,
) {
    entry<SheetRoute.QuizDetail>(metadata = sheetHeaderInPage()) { route ->
        QuizDetailPage(quizId = route.quizId, courseId = route.courseId)
    }
    entry<SheetRoute.Forum>(metadata = sheetHeaderInPage()) { route ->
        ForumSheetPage(
            forumId = route.forumId,
            courseId = route.courseId,
            initialDiscussionId = route.initialDiscussionId,
            onOpenFile = { fileName, fileUrl, mimeType, sizeBytes ->
                openFile(AppRoute.FileViewer(fileName, fileUrl = fileUrl, mimeType = mimeType, sizeBytes = sizeBytes), false)
            },
        )
    }
    entry<SheetRoute.AssignmentDetail>(metadata = sheetHeaderInPage()) { route ->
        AssignmentDetailPage(
            assignId = route.assignId,
            courseId = route.courseId,
            onOpenFile = { fileName, fileUrl, mimeType, sizeBytes, forceChooser ->
                openFile(
                    AppRoute.FileViewer(fileName, fileUrl = fileUrl, mimeType = mimeType, sizeBytes = sizeBytes),
                    forceChooser,
                )
            },
        )
    }
    entry<SheetRoute.FileOpenChooser>(
        metadata = sheetHeader<SheetRoute.FileOpenChooser> { route -> fileOpenChooserHeader(route.file) },
    ) { route ->
        val navigator = requireAppNavigator()
        val closeChooser = rememberPopSelf()
        val kind = remember(route) { FileKind.classify(route.file.fileName, route.file.mimeType) }
        FileOpenChooserContent(
            kind = kind,
            onChoose = { choice, rememberChoice ->
                kind.preferenceKey
                    ?.takeIf { rememberChoice }
                    ?.let { fileOpenViewModel.remember(it, choice) }
                closeChooser()
                when (choice) {
                    FileOpenChoice.InApp -> navigator.navigate(route.file)
                    FileOpenChoice.External -> openExternally(route.file)
                }
            },
        )
    }
    entry<SheetRoute.OfficeOpen>(
        metadata = sheetHeader<SheetRoute.OfficeOpen> { route -> officeOpenHeader(app = route.app, file = route.file) },
    ) { route ->
        OfficeOpenPage(app = route.app, route = route.file)
    }

    entry<SheetRoute.AddCourse>(metadata = sheetHeaderInPage()) {
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
            onRequireSignIn = {},
            viewModel = addCourseViewModel,
        )
    }
}
