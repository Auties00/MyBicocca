package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment.AttachmentRef
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionForm
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.LoadSubmissionFormUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.ObserveAssignmentUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.ProbeSubmissionFileUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.ReadSubmissionFileUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.RefreshCourseAssignmentsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.RefreshSubmissionStatusUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.RemoveSubmissionUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.SaveSubmissionUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.SubmitAssignmentForGradingUseCase
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state.AssignmentDetailOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state.AssignmentPage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state.PickedFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the assignment (compito) detail sheet: the cached assignment overview, the in-sheet
 * pager, and the whole submission lifecycle (draft, finalize for grading, remove).
 *
 * Streams by role — data: [assignment] observes the Room cache for the active account, and
 * [submissionForm] is the editor model loaded on demand when the editor opens; sync:
 * [syncStatus] tracks the refresh run on account activation and on [pullToRefresh]; one-shot:
 * [oneShotEvents] carries submission outcomes, attachment-open requests, and refresh failures.
 * [backStack], [draftText], [pickedFiles], [keptExistingFiles], [statementAccepted], and
 * [submitting] hold the pager and editor state, with [submitting] gating every mutating action.
 *
 * Actions: [openCompose] enters the editor; [setText], [addFiles], [removeFile],
 * [removeExistingFile], and [setStatementAccepted] edit it; [goToConfirm], [back], and
 * [resetNavigation] move the pager; [saveDraft], [confirmSubmit], and [removeSubmission] commit.
 *
 * Picked files carry display metadata only: metadata is probed when the user picks them, and the
 * bytes are read from the content Uri just before upload when a save or submit runs.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = AssignmentDetailViewModel.Factory::class)
class AssignmentDetailViewModel @AssistedInject constructor(
    @Assisted private val key: AppRoute.AssignmentDetail,
    @ApplicationContext private val appContext: Context,
    savedState: SavedStateHandle,
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val observeAssignment: ObserveAssignmentUseCase,
    private val refreshSubmissionStatus: RefreshSubmissionStatusUseCase,
    private val refreshCourseAssignments: RefreshCourseAssignmentsUseCase,
    private val loadSubmissionForm: LoadSubmissionFormUseCase,
    private val saveSubmission: SaveSubmissionUseCase,
    private val submitAssignmentForGrading: SubmitAssignmentForGradingUseCase,
    private val removeSubmissionUseCase: RemoveSubmissionUseCase,
    private val probeSubmissionFile: ProbeSubmissionFileUseCase,
    private val readSubmissionFile: ReadSubmissionFileUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: AppRoute.AssignmentDetail): AssignmentDetailViewModel
    }

    private val assignmentId: AssignmentId = AssignmentId(key.assignId)
    private val courseId: CourseId = CourseId(key.courseId)

    private val activeAccountId: Flow<AccountId?> = observeActiveAccount()
        .map { it?.id }
        .distinctUntilChanged()

    val assignment: StateFlow<Loadable<Assignment>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.NotYetLoaded) else observeAssignment(id, assignmentId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val oneShotChannel = Channel<AssignmentDetailOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<AssignmentDetailOneShotEvent> = oneShotChannel.receiveAsFlow()

    private val _backStack = MutableStateFlow(listOf(AssignmentPage.Detail))
    val backStack: StateFlow<List<AssignmentPage>> = _backStack.asStateFlow()

    private val _submissionForm = MutableStateFlow<Loadable<SubmissionForm>>(Loadable.NotYetLoaded)
    val submissionForm: StateFlow<Loadable<SubmissionForm>> = _submissionForm.asStateFlow()

    private val _draftText = MutableStateFlow("")
    val draftText: StateFlow<String> = _draftText.asStateFlow()

    private val _pickedFiles = MutableStateFlow<List<PickedFile>>(emptyList())
    val pickedFiles: StateFlow<List<PickedFile>> = _pickedFiles.asStateFlow()

    /**
     * Already-submitted files the user keeps while editing, seeded from the loaded form and
     * re-uploaded on save so they survive Moodle's replace-the-whole-area semantics.
     */
    private val _keptExistingFiles = MutableStateFlow<List<AttachmentRef>>(emptyList())
    val keptExistingFiles: StateFlow<List<AttachmentRef>> = _keptExistingFiles.asStateFlow()

    private val _statementAccepted = MutableStateFlow(false)
    val statementAccepted: StateFlow<Boolean> = _statementAccepted.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    init {
        viewModelScope.launch {
            activeAccountId.filterNotNull().distinctUntilChanged().collect { id ->
                runRefresh(id)
            }
        }
    }

    fun pullToRefresh() {
        viewModelScope.launch { runRefresh(activeAccountId.filterNotNull().first()) }
    }

    fun openFile(url: String, fileName: String?) {
        oneShotChannel.trySend(AssignmentDetailOneShotEvent.OpenFile(url, fileName))
    }

    /** Opens the submission editor, loading the fresh editor model and pre-filling any draft. */
    fun openCompose() {
        if (_backStack.value.last() == AssignmentPage.Compose) return
        _pickedFiles.value = emptyList()
        _statementAccepted.value = false
        _submissionForm.value = Loadable.NotYetLoaded
        _backStack.update { it + AssignmentPage.Compose }
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { loadSubmissionForm(accountId, courseId, assignmentId) }
                .onSuccess { form ->
                    _draftText.value = form.existingOnlineText.orEmpty()
                    _keptExistingFiles.value = form.existingFiles
                    _submissionForm.value = Loadable.Loaded(form)
                }
                .onFailure {
                    back()
                    oneShotChannel.trySend(
                        AssignmentDetailOneShotEvent.ActionFailed(
                            appContext.getString(R.string.elearning_assign_open_failed),
                            it,
                        ),
                    )
                }
        }
    }

    /**
     * Probes and appends newly picked files, deduplicated by Uri. New picks share the file-count
     * budget with the kept existing files; anything beyond the budget is dropped.
     */
    fun addFiles(uris: List<String>) {
        val maxFiles = (_submissionForm.value as? Loadable.Loaded)?.value?.maxFiles ?: Int.MAX_VALUE
        viewModelScope.launch {
            val existing = _pickedFiles.value
            val available = (maxFiles - _keptExistingFiles.value.size - existing.size).coerceAtLeast(0)
            val additions = uris
                .filter { uri -> existing.none { it.uri == uri } }
                .mapNotNull { uri ->
                    runCatching { probeSubmissionFile(uri) }.getOrNull()?.let { meta ->
                        PickedFile(uri = uri, fileName = meta.fileName, mimeType = meta.mimeType, sizeBytes = meta.sizeBytes)
                    }
                }
                .take(available)
            _pickedFiles.value = existing + additions
        }
    }

    fun removeFile(uri: String) {
        _pickedFiles.update { list -> list.filterNot { it.uri == uri } }
    }

    fun removeExistingFile(fileUrl: String?) {
        _keptExistingFiles.update { list -> list.filterNot { it.fileUrl == fileUrl } }
    }

    fun setText(text: String) {
        _draftText.value = text
    }

    fun setStatementAccepted(accepted: Boolean) {
        _statementAccepted.value = accepted
    }

    fun goToConfirm() {
        if (_backStack.value.last() == AssignmentPage.ConfirmSubmit) return
        _backStack.update { it + AssignmentPage.ConfirmSubmit }
    }

    fun back() {
        _backStack.update { if (it.size > 1) it.dropLast(1) else it }
    }

    /** Saves the current editor state as a draft and returns to the overview. */
    fun saveDraft() {
        runEditorAction(AssignmentDetailOneShotEvent.DraftSaved) { accountId, form ->
            saveSubmission(accountId, assignmentId, onlineTextOrNull(form), readPickedFiles(form), keepFilesFor(form))
        }
    }

    /**
     * Saves then finalizes for grading: when drafts are enabled the save and submit run as a
     * single committed action, while with drafts disabled the save itself is the submission.
     */
    fun confirmSubmit() {
        runEditorAction(AssignmentDetailOneShotEvent.SubmissionSent) { accountId, form ->
            saveSubmission(accountId, assignmentId, onlineTextOrNull(form), readPickedFiles(form), keepFilesFor(form))
            if (form.submissionDraftsEnabled) {
                val accept = !form.requiresSubmissionStatement || _statementAccepted.value
                submitAssignmentForGrading(accountId, assignmentId, accept)
            }
        }
    }

    fun removeSubmission() {
        if (_submitting.value) return
        _submitting.value = true
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { removeSubmissionUseCase(accountId, assignmentId) }
                .onSuccess {
                    resetNavigation()
                    oneShotChannel.trySend(AssignmentDetailOneShotEvent.SubmissionRemoved)
                }
                .onFailure {
                    oneShotChannel.trySend(
                        AssignmentDetailOneShotEvent.ActionFailed(
                            appContext.getString(R.string.elearning_assign_remove_failed),
                            it,
                        ),
                    )
                }
            _submitting.value = false
        }
    }

    fun resetNavigation() {
        _backStack.value = listOf(AssignmentPage.Detail)
        _pickedFiles.value = emptyList()
        _keptExistingFiles.value = emptyList()
        _draftText.value = ""
        _statementAccepted.value = false
        _submissionForm.value = Loadable.NotYetLoaded
    }

    private fun runEditorAction(
        success: AssignmentDetailOneShotEvent,
        block: suspend (AccountId, SubmissionForm) -> Unit,
    ) {
        if (_submitting.value) return
        val form = (_submissionForm.value as? Loadable.Loaded)?.value ?: return
        _submitting.value = true
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { block(accountId, form) }
                .onSuccess {
                    resetNavigation()
                    oneShotChannel.trySend(success)
                }
                .onFailure {
                    oneShotChannel.trySend(
                        AssignmentDetailOneShotEvent.ActionFailed(
                            appContext.getString(R.string.elearning_assign_submit_failed),
                            it,
                        ),
                    )
                }
            _submitting.value = false
        }
    }

    private fun onlineTextOrNull(form: SubmissionForm): String? =
        _draftText.value.takeIf { form.onlineTextEnabled && it.isNotBlank() }?.let(::toHtml)

    private suspend fun readPickedFiles(form: SubmissionForm) =
        if (!form.fileEnabled) emptyList()
        else _pickedFiles.value.map { readSubmissionFile(it.uri) }

    private fun keepFilesFor(form: SubmissionForm): List<AttachmentRef> =
        if (form.fileEnabled) _keptExistingFiles.value.filter { it.fileUrl != null } else emptyList()

    /** Plain editor text rendered as minimal HTML for Moodle's onlinetext (format = HTML). */
    private fun toHtml(text: String): String =
        "<p>" + text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\n", "<br>") + "</p>"

    /**
     * Refreshes the submission status, first materializing the course's assignment rows when the
     * assignment is not yet cached: deep-opens (e.g. from the home deadlines rail) can land here
     * before the course's assignments were ever loaded, and the status-only refresh no-ops on a
     * missing row.
     */
    private suspend fun runRefresh(accountId: AccountId) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching {
            if (assignment.value !is Loadable.Loaded) {
                refreshCourseAssignments(accountId, courseId)
            }
            refreshSubmissionStatus(accountId, assignmentId)
        }
            .onSuccess { _syncStatus.value = SyncStatus.Idle }
            .onFailure {
                _syncStatus.value = SyncStatus.Failed(it)
                oneShotChannel.trySend(AssignmentDetailOneShotEvent.RefreshFailed(it))
            }
    }

    private companion object {
        const val KEY_ASSIGNMENT_ID = "assignmentId"
        const val STATE_KEEP_ALIVE_MS = 5_000L
    }
}
