package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptAnswer
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.model.elearning.quiz.BestGrade
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.LoadAttemptPageUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.LoadAttemptReviewUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.ObserveQuizAttemptsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.ObserveQuizBestGradeUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.ObserveQuizUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.RefreshQuizAttemptsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.SaveQuizDraftUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.StartQuizAttemptUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.SubmitQuizAttemptUseCase
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.AttemptUiState
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.QuizDetailOneShotEvent
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class QuizDetailViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val observeQuiz: ObserveQuizUseCase,
    private val observeAttempts: ObserveQuizAttemptsUseCase,
    private val observeBestGrade: ObserveQuizBestGradeUseCase,
    private val refreshAttempts: RefreshQuizAttemptsUseCase,
    private val startAttempt: StartQuizAttemptUseCase,
    private val loadAttemptPage: LoadAttemptPageUseCase,
    private val saveDraft: SaveQuizDraftUseCase,
    private val submitAttempt: SubmitQuizAttemptUseCase,
    private val loadReview: LoadAttemptReviewUseCase,
) : ViewModel() {

    private val quizId: QuizId = QuizId(
        savedState.get<Int>(KEY_QUIZ_ID) ?: error("QuizDetailViewModel requires $KEY_QUIZ_ID")
    )

    private val activeAccountId: Flow<AccountId?> = observeActiveAccount()
        .map { it?.id }
        .distinctUntilChanged()

    val quiz: StateFlow<Loadable<Quiz>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.NotYetLoaded) else observeQuiz(id, quizId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val attempts: StateFlow<Loadable<List<QuizAttempt>>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.Loaded(emptyList())) else observeAttempts(id, quizId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val bestGrade: StateFlow<Loadable<BestGrade?>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.Loaded<BestGrade?>(null)) else observeBestGrade(id, quizId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    private val _attemptUi = MutableStateFlow<AttemptUiState>(AttemptUiState.Idle)
    val attemptUi: StateFlow<AttemptUiState> = _attemptUi.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val oneShotChannel = Channel<QuizDetailOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<QuizDetailOneShotEvent> = oneShotChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            activeAccountId.filterNotNull().distinctUntilChanged().collect { id ->
                runRefresh(id)
            }
        }
    }

    private suspend fun runRefresh(accountId: AccountId) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching { refreshAttempts(accountId, quizId) }
            .onSuccess { _syncStatus.value = SyncStatus.Idle }
            .onFailure {
                _syncStatus.value = SyncStatus.Failed(it)
                oneShotChannel.trySend(QuizDetailOneShotEvent.RefreshFailed(it))
            }
    }

    fun pullToRefresh() {
        viewModelScope.launch { runRefresh(activeAccountId.filterNotNull().first()) }
    }

    fun onStartAttempt() {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            _attemptUi.value = AttemptUiState.Loading
            runCatching { startAttempt(accountId, quizId) }
                .onSuccess { id ->
                    oneShotChannel.trySend(QuizDetailOneShotEvent.AttemptStarted(id))
                    loadPageInternal(accountId, id, page = 0)
                }
                .onFailure {
                    _attemptUi.value = AttemptUiState.Idle
                    oneShotChannel.trySend(QuizDetailOneShotEvent.RefreshFailed(it))
                }
        }
    }

    fun resumeAttempt(id: AttemptId) {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            loadPageInternal(accountId, id, page = 0)
        }
    }

    fun loadPage(page: Int) {
        val current = _attemptUi.value as? AttemptUiState.InProgress ?: return
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            loadPageInternal(accountId, current.attemptId, page)
        }
    }

    private suspend fun loadPageInternal(accountId: AccountId, attemptId: AttemptId, page: Int) {
        runCatching { loadAttemptPage(accountId, attemptId, page) }
            .onSuccess { attemptPage ->
                _attemptUi.value = AttemptUiState.InProgress(
                    attemptId = attemptId,
                    page = attemptPage,
                    answers = (_attemptUi.value as? AttemptUiState.InProgress)?.answers ?: emptyList(),
                    flagged = (_attemptUi.value as? AttemptUiState.InProgress)?.flagged ?: emptySet(),
                )
            }
            .onFailure {
                _attemptUi.value = AttemptUiState.Idle
                oneShotChannel.trySend(QuizDetailOneShotEvent.RefreshFailed(it))
            }
    }

    fun setAnswer(slot: Int, fields: Map<String, String>) {
        val current = _attemptUi.value as? AttemptUiState.InProgress ?: return
        val updated = current.answers.toMutableList()
        val index = updated.indexOfFirst { it.slot == slot }
        val newAnswer = AttemptAnswer(slot = slot, fields = fields)
        if (index >= 0) updated[index] = newAnswer else updated.add(newAnswer)
        _attemptUi.value = current.copy(answers = updated)
    }

    fun toggleFlag(slot: Int) {
        val current = _attemptUi.value as? AttemptUiState.InProgress ?: return
        val flagged = current.flagged.toMutableSet()
        if (!flagged.add(slot)) flagged.remove(slot)
        _attemptUi.value = current.copy(flagged = flagged)
    }

    fun onSaveDraft() {
        val current = _attemptUi.value as? AttemptUiState.InProgress ?: return
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { saveDraft(accountId, current.attemptId, current.answers) }
        }
    }

    fun onSubmit(timeUp: Boolean = false) {
        val current = _attemptUi.value as? AttemptUiState.InProgress ?: return
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            _attemptUi.value = AttemptUiState.Submitting(current.attemptId)
            runCatching { submitAttempt(accountId, current.attemptId, current.answers, timeUp) }
                .onSuccess {
                    oneShotChannel.trySend(QuizDetailOneShotEvent.AttemptSubmitted(current.attemptId))
                    runCatching { refreshAttempts(accountId, quizId) }
                    runCatching { loadReview(accountId, current.attemptId) }
                        .onSuccess { _attemptUi.value = AttemptUiState.Reviewing(it) }
                        .onFailure { _attemptUi.value = AttemptUiState.Idle }
                }
                .onFailure {
                    _attemptUi.value = current
                    oneShotChannel.trySend(QuizDetailOneShotEvent.RefreshFailed(it))
                }
        }
    }

    fun viewReview(attemptId: AttemptId) {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            _attemptUi.value = AttemptUiState.Loading
            runCatching { loadReview(accountId, attemptId) }
                .onSuccess { _attemptUi.value = AttemptUiState.Reviewing(it) }
                .onFailure {
                    _attemptUi.value = AttemptUiState.Idle
                    oneShotChannel.trySend(QuizDetailOneShotEvent.RefreshFailed(it))
                }
        }
    }

    fun closeAttempt() {
        _attemptUi.value = AttemptUiState.Idle
    }

    private companion object {
        const val KEY_QUIZ_ID = "quizId"
        const val STATE_KEEP_ALIVE_MS = 5_000L
    }
}
