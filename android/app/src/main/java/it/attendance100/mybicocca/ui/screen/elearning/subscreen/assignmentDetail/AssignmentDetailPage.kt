package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionForm
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.ui.component.modal.SheetConfirmPage
import it.attendance100.mybicocca.ui.component.modal.SheetOutcome
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.SheetResultPage
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component.AssignmentOverviewPage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component.SubmissionComposePage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state.AssignmentDetailOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state.AssignmentPage
import it.attendance100.mybicocca.ui.screen.elearning.theme.CourseDetailTheme
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The compito (assignment) detail as a multi-state bottom-sheet modal, hosted as a single
 * BottomSheetSceneStrategy entry.
 *
 * An in-sheet pager — overview, submission editor, finalize-for-grading confirmation — is driven
 * by the ViewModel's back stack under one morphing [SheetPagerHeader], in the same mold as the
 * Biblioteca sheet; the remove-confirmation and result pages are overlays held by the composable
 * and drawn on top of it. System back walks the stack up one level and is ignored while an
 * action is in flight.
 *
 * Attachment opens are wired directly through [onOpenFile] and refresh failures keep the cached
 * content visible, so neither surfaces an in-sheet result page.
 */
@Composable
fun AssignmentDetailPage(
    assignId: Int,
    courseId: Int,
    onOpenFile: (fileName: String, fileUrl: String, mimeType: String?, sizeBytes: Long?, forceChooser: Boolean) -> Unit,
    @Suppress("DEPRECATION") viewModel: AssignmentDetailViewModel = hiltViewModel<AssignmentDetailViewModel, AssignmentDetailViewModel.Factory>(
        key = "assign-sheet-$assignId",
        creationCallback = { it.create(AppRoute.AssignmentDetail(assignId = assignId, courseId = courseId)) },
    ),
) {
    val assignmentLoadable by viewModel.assignment.collectAsStateWithLifecycle()
    val backStack by viewModel.backStack.collectAsStateWithLifecycle()
    val submissionForm by viewModel.submissionForm.collectAsStateWithLifecycle()
    val draftText by viewModel.draftText.collectAsStateWithLifecycle()
    val pickedFiles by viewModel.pickedFiles.collectAsStateWithLifecycle()
    val keptExistingFiles by viewModel.keptExistingFiles.collectAsStateWithLifecycle()
    val statementAccepted by viewModel.statementAccepted.collectAsStateWithLifecycle()
    val submitting by viewModel.submitting.collectAsStateWithLifecycle()

    var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
    var pendingRemove by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetNavigation() }
    }

    val draftSavedMessage = stringResource(R.string.elearning_assign_draft_saved)
    val submissionSentMessage = stringResource(R.string.elearning_assign_submission_sent)
    val submissionRemovedMessage = stringResource(R.string.elearning_assign_submission_removed)
    LaunchedEffect(Unit) {
        viewModel.oneShotEvents.collect { event ->
            when (event) {
                AssignmentDetailOneShotEvent.DraftSaved ->
                    outcome = SheetOutcome.Success(draftSavedMessage)
                AssignmentDetailOneShotEvent.SubmissionSent ->
                    outcome = SheetOutcome.Success(submissionSentMessage)
                AssignmentDetailOneShotEvent.SubmissionRemoved ->
                    outcome = SheetOutcome.Success(submissionRemovedMessage)
                is AssignmentDetailOneShotEvent.ActionFailed ->
                    outcome = SheetOutcome.Error(event.title, event.cause)
                is AssignmentDetailOneShotEvent.OpenFile -> Unit
                is AssignmentDetailOneShotEvent.RefreshFailed -> Unit
            }
        }
    }

    val current = backStack.last()
    val display: Display = when {
        outcome != null -> Display.Outcome
        pendingRemove -> Display.ConfirmRemove
        else -> Display.Page(current)
    }

    val seekableState =
        remember { androidx.compose.animation.core.SeekableTransitionState(display) }
    val transition = androidx.compose.animation.core.rememberTransition(
        seekableState,
        label = "assignment_sheet_pages"
    )

    LaunchedEffect(display) {
        if (seekableState.targetState != display) {
            seekableState.animateTo(display)
        }
    }

    androidx.activity.compose.PredictiveBackHandler(enabled = display !is Display.Page || (backStack.size > 1 && !submitting)) { progress ->
        try {
            val fallback = when (display) {
                Display.Outcome -> if (pendingRemove) Display.ConfirmRemove else Display.Page(
                    current
                )

                Display.ConfirmRemove -> Display.Page(current)
                is Display.Page -> if (backStack.size > 1) Display.Page(backStack[backStack.size - 2]) else display
            }
            progress.collect { event ->
                seekableState.seekTo(event.progress, targetState = fallback)
            }
            seekableState.animateTo(fallback)
            when (display) {
                Display.Outcome -> outcome = null
                Display.ConfirmRemove -> pendingRemove = false
                is Display.Page -> if (!submitting) viewModel.back()
            }
        } catch (_: kotlinx.coroutines.CancellationException) {
            seekableState.animateTo(display)
        }
    }

    CourseDetailTheme(courseId = remember(courseId) { CourseId(courseId) }) {
        Column(
            modifier = Modifier
                .testTag(AssignmentDetailTestTags.ROOT)
                .fillMaxWidth()
                .heightIn(max = 760.dp)
                .padding(top = 8.dp),
        ) {
            SheetPagerHeader(
                depth = displayDepth(display),
                title = when (display) {
                    Display.Outcome -> ""
                    Display.ConfirmRemove -> stringResource(R.string.elearning_assign_confirm_remove_title)
                    is Display.Page -> when (display.page) {
                        AssignmentPage.Detail -> (assignmentLoadable as? Loadable.Loaded)?.value?.name
                            ?: stringResource(R.string.elearning_assign_overview_title)
                        AssignmentPage.Compose -> stringResource(R.string.elearning_assign_submission_title)
                        AssignmentPage.ConfirmSubmit -> stringResource(R.string.elearning_assign_confirm_title)
                    }
                },
                subtitle = when (display) {
                    is Display.Page -> when (display.page) {
                        AssignmentPage.Detail ->
                            (assignmentLoadable as? Loadable.Loaded)?.value?.let { deadlineSubtitle(it) }
                        AssignmentPage.Compose, AssignmentPage.ConfirmSubmit ->
                            (assignmentLoadable as? Loadable.Loaded)?.value?.name
                    }
                    else -> null
                },
                onBack = when (display) {
                    Display.Outcome -> null
                    Display.ConfirmRemove -> ({ pendingRemove = false })
                    is Display.Page -> if (backStack.size > 1 && !submitting) viewModel::back else null
                },
            )

            transition.AnimatedContent(
                transitionSpec = {
                    sheetPageTransform(forward = displayDepth(targetState) >= displayDepth(initialState))
                },
                contentKey = { displayKey(it) },
            ) { shown ->
                when (shown) {
                    Display.Outcome -> outcome?.let { current ->
                        SheetResultPage(outcome = current, onDismiss = { outcome = null })
                    }

                    Display.ConfirmRemove -> SheetConfirmPage(
                        body = stringResource(R.string.elearning_assign_confirm_remove_body),
                        onConfirm = {
                            pendingRemove = false
                            viewModel.removeSubmission()
                        },
                        onKeep = { pendingRemove = false },
                        confirmLabel = stringResource(R.string.elearning_assign_remove_button),
                        keepLabel = stringResource(R.string.elearning_assign_cancel),
                    )

                    is Display.Page -> when (shown.page) {
                        AssignmentPage.Detail -> when (val loadable = assignmentLoadable) {
                            Loadable.NotYetLoaded -> LoadingBox(
                                modifier = Modifier.testTag(AssignmentDetailTestTags.STATE_LOADING),
                            )
                            is Loadable.Loaded -> AssignmentOverviewPage(
                                assignment = loadable.value,
                                onOpenFile = onOpenFile,
                                onCompose = viewModel::openCompose,
                                onRemove = { pendingRemove = true },
                                modifier = Modifier.testTag(AssignmentDetailTestTags.OVERVIEW),
                            )
                        }

                        AssignmentPage.Compose -> when (val form = submissionForm) {
                            Loadable.NotYetLoaded -> LoadingBox()
                            is Loadable.Loaded -> SubmissionComposePage(
                                form = form.value,
                                draftText = draftText,
                                pickedFiles = pickedFiles,
                                keptExistingFiles = keptExistingFiles,
                                submitting = submitting,
                                onTextChange = viewModel::setText,
                                onAddFiles = viewModel::addFiles,
                                onRemoveFile = viewModel::removeFile,
                                onRemoveExistingFile = viewModel::removeExistingFile,
                                onSaveDraft = viewModel::saveDraft,
                                onContinue = {
                                    if (form.value.submissionDraftsEnabled || form.value.requiresSubmissionStatement) {
                                        viewModel.goToConfirm()
                                    } else {
                                        viewModel.confirmSubmit()
                                    }
                                },
                            )
                        }

                        AssignmentPage.ConfirmSubmit -> (submissionForm as? Loadable.Loaded)?.value?.let { form ->
                            ConfirmSubmitPage(
                                form = form,
                                fileCount = pickedFiles.size + keptExistingFiles.size,
                                hasText = form.onlineTextEnabled && draftText.isNotBlank(),
                                statementAccepted = statementAccepted,
                                submitting = submitting,
                                onStatementChange = viewModel::setStatementAccepted,
                                onConfirm = viewModel::confirmSubmit,
                                onCancel = viewModel::back,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center,
    ) { CircularProgressIndicator() }
}

/**
 * The finalize-for-grading confirmation: a recap of what is being submitted plus, when the
 * assignment requires it, the submission-statement checkbox that gates the send.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ConfirmSubmitPage(
    form: SubmissionForm,
    fileCount: Int,
    hasText: Boolean,
    statementAccepted: Boolean,
    submitting: Boolean,
    onStatementChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val recap = buildList {
        if (fileCount > 0) {
            add(pluralStringResource(R.plurals.elearning_assign_submission_files, fileCount, fileCount))
        }
        if (hasText) add(stringResource(R.string.elearning_assign_submission_text))
    }.joinToString(stringResource(R.string.elearning_assign_submission_recap_join))
        .ifBlank { stringResource(R.string.elearning_assign_submission_recap) }
    val canSend = !form.requiresSubmissionStatement || statementAccepted

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.elearning_assign_confirm_body, recap),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        if (form.requiresSubmissionStatement && form.submissionStatement != null) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Checkbox(checked = statementAccepted, onCheckedChange = onStatementChange, enabled = !submitting)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = form.submissionStatement,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FilledTonalButton(
                onClick = onCancel,
                enabled = !submitting,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHighest,
                    contentColor = scheme.onSurface,
                ),
            ) {
                Text(stringResource(R.string.elearning_assign_cancel), fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onConfirm,
                enabled = canSend && !submitting && LocalIsOnline.current,
                modifier = Modifier
                    .weight(1.4f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = Color.White,
                ),
            ) {
                if (submitting) {
                    LoadingIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(stringResource(R.string.elearning_assign_submit_text), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * What the sheet is currently showing. The ViewModel owns the overview/editor/confirm back
 * stack; the remove-confirmation and result overlays are held by the composable and drawn on
 * top of it.
 */
private sealed interface Display {
    data class Page(val page: AssignmentPage) : Display
    data object ConfirmRemove : Display
    data object Outcome : Display
}

private fun displayDepth(display: Display): Int = when (display) {
    is Display.Page -> when (display.page) {
        AssignmentPage.Detail -> 0
        AssignmentPage.Compose -> 1
        AssignmentPage.ConfirmSubmit -> 2
    }
    Display.ConfirmRemove -> 2
    Display.Outcome -> 8
}

private fun displayKey(display: Display): String = when (display) {
    is Display.Page -> display.page.name
    Display.ConfirmRemove -> "confirm_remove"
    Display.Outcome -> "outcome"
}

private val DeadlineFmt: DateTimeFormatter = DateTimeFormatter
    .ofPattern("EEE d MMM, HH:mm", Locale.getDefault())
    .withZone(ZoneId.systemDefault())

/** Deadline line shown as the modal subtitle on the overview page. */
@Composable
private fun deadlineSubtitle(assignment: Assignment): String {
    val due = assignment.dueDate ?: return stringResource(R.string.elearning_assign_no_deadline)
    return if (due.isBefore(Instant.now())) {
        stringResource(R.string.elearning_assign_expired_date, DeadlineFmt.format(due))
    } else {
        stringResource(R.string.elearning_assign_expires_date, DeadlineFmt.format(due))
    }
}
