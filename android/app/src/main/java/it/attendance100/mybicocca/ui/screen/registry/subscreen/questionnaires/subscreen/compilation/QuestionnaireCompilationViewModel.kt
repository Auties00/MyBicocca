package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireAnswer
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireOption
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnairePage
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireQuestion
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireQuestionKind
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSession
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireTarget
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.ConfirmQuestionnaireUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.GetNextQuestionnairePageUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.GetPreviousQuestionnairePageUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.GetQuestionnaireSummaryUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.SaveQuestionnairePageUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.StartQuestionnaireUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionAnswerState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionnaireCompilationEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionnaireCompilationRequest
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionnaireCompilationStep
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// One instance per compilation: Esse3 never resumes drafts, so the whole server-side
// session (start -> pages -> summary -> confirm) lives and dies with this ViewModel.
@HiltViewModel(assistedFactory = QuestionnaireCompilationViewModel.Factory::class)
class QuestionnaireCompilationViewModel @AssistedInject constructor(
    @Assisted private val key: QuestionnaireCompilationRequest,
    private val observeActiveAccount: ObserveActiveAccountUseCase,
    private val startQuestionnaire: StartQuestionnaireUseCase,
    private val savePage: SaveQuestionnairePageUseCase,
    private val getNextPage: GetNextQuestionnairePageUseCase,
    private val getPreviousPage: GetPreviousQuestionnairePageUseCase,
    private val getSummary: GetQuestionnaireSummaryUseCase,
    private val confirmQuestionnaire: ConfirmQuestionnaireUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: QuestionnaireCompilationRequest): QuestionnaireCompilationViewModel
    }

    val anonymous: Boolean get() = key.anonymous

    // Display facets for the hosting sheet's pinned header.
    val activityName: String get() = key.activityName
    val lecturerName: String? get() = key.lecturerName
    val partitionName: String? get() = key.partitionName

    private val _step =
        MutableStateFlow<QuestionnaireCompilationStep>(QuestionnaireCompilationStep.Starting)
    val step: StateFlow<QuestionnaireCompilationStep> = _step.asStateFlow()

    // True while a server round-trip (save / page move / confirm) is in flight.
    private val _working = MutableStateFlow(false)
    val working: StateFlow<Boolean> = _working.asStateFlow()

    private val _answers = MutableStateFlow<Map<Long, QuestionAnswerState>>(emptyMap())
    val answers: StateFlow<Map<Long, QuestionAnswerState>> = _answers.asStateFlow()

    // Mandatory questions left unanswered by the last "Avanti" attempt — highlighted in red.
    private val _invalidQuestionIds = MutableStateFlow<Set<Long>>(emptySet())
    val invalidQuestionIds: StateFlow<Set<Long>> = _invalidQuestionIds.asStateFlow()

    private val _events = Channel<QuestionnaireCompilationEvent>(Channel.BUFFERED)
    val events: Flow<QuestionnaireCompilationEvent> = _events.receiveAsFlow()

    private var session: QuestionnaireSession? = null

    // Cached so back-from-summary can restore the last page without a server fetch.
    private var lastPage: QuestionnairePage? = null
    private var lastPageIndex: Int = 0

    init {
        start()
    }

    fun retryStart() {
        if (_step.value is QuestionnaireCompilationStep.StartFailed) start()
    }

    // Discard an in-progress compilation. The VM is sheet-hosted and keyed per unit, so it
    // outlives the compiler page: leaving without confirming must not replay the stale
    // session or answers on the next open. Esse3 never resumes drafts, so the abandoned
    // server session is simply dropped and a fresh one is started.
    fun reset() {
        session = null
        lastPage = null
        lastPageIndex = 0
        _answers.value = emptyMap()
        _invalidQuestionIds.value = emptySet()
        _working.value = false
        start()
    }

    fun selectOption(question: QuestionnaireQuestion, option: QuestionnaireOption) {
        val state = _answers.value[question.id] ?: QuestionAnswerState()
        val selected = when (val kind = question.kind) {
            is QuestionnaireQuestionKind.MultiChoice -> when {
                option.id in state.selectedOptionIds -> state.selectedOptionIds - option.id
                kind.maxSelections != null && state.selectedOptionIds.size >= kind.maxSelections ->
                    state.selectedOptionIds

                else -> state.selectedOptionIds + option.id
            }

            // Re-tapping the selected option clears it, so single-choice answers can be undone.
            else -> if (option.id in state.selectedOptionIds) emptySet() else setOf(option.id)
        }
        _answers.value = _answers.value + (question.id to state.copy(selectedOptionIds = selected))
        _invalidQuestionIds.value = _invalidQuestionIds.value - question.id
    }

    fun setFreeText(questionId: Long, text: String) {
        val state = _answers.value[questionId] ?: QuestionAnswerState()
        _answers.value = _answers.value + (questionId to state.copy(freeText = text))
        _invalidQuestionIds.value = _invalidQuestionIds.value - questionId
    }

    fun next() {
        val current = _step.value as? QuestionnaireCompilationStep.Page ?: return
        val session = session ?: return
        if (_working.value) return

        val missing = current.page.questions().filter { !it.isAnswered() }.filter { it.mandatory }
        if (missing.isNotEmpty()) {
            _invalidQuestionIds.value = missing.map { it.id }.toSet()
            _events.trySend(QuestionnaireCompilationEvent.MissingAnswers)
            return
        }

        viewModelScope.launch {
            _working.value = true
            runCatching {
                saveCurrentAnswers(session, current.page)
                val next = getNextPage(session, current.page.id)
                if (next.isEnd) {
                    lastPage = current.page
                    lastPageIndex = current.index
                    val summary = getSummary(session)
                    _step.value = QuestionnaireCompilationStep.Summary(complete = summary.complete)
                } else {
                    enterPage(next, current.index + 1)
                }
            }.onFailure { cause ->
                _events.trySend(QuestionnaireCompilationEvent.Failed(cause))
            }
            _working.value = false
        }
    }

    fun back() {
        if (_working.value) return
        when (val current = _step.value) {
            is QuestionnaireCompilationStep.Summary -> {
                // The page was saved before reaching the summary; restore it from cache and
                // keep the in-memory answers (the cached page's saved* fields are stale).
                val page = lastPage ?: return
                _step.value = QuestionnaireCompilationStep.Page(page = page, index = lastPageIndex)
            }

            is QuestionnaireCompilationStep.Page -> {
                if (current.index == 0) return
                val session = session ?: return
                viewModelScope.launch {
                    _working.value = true
                    runCatching {
                        // Best-effort: keep edits when stepping back, but an incomplete page
                        // must not block backwards navigation.
                        runCatching { saveCurrentAnswers(session, current.page) }
                        val previous = getPreviousPage(session, current.page.id)
                        enterPage(previous, current.index - 1)
                    }.onFailure { cause ->
                        _events.trySend(QuestionnaireCompilationEvent.Failed(cause))
                    }
                    _working.value = false
                }
            }

            else -> Unit
        }
    }

    fun confirm() {
        val current = _step.value as? QuestionnaireCompilationStep.Summary ?: return
        val session = session ?: return
        if (!current.complete || _working.value) return
        viewModelScope.launch {
            _working.value = true
            runCatching { confirmQuestionnaire(session) }.fold(
                onSuccess = { _events.trySend(QuestionnaireCompilationEvent.Confirmed) },
                onFailure = { cause ->
                    _events.trySend(QuestionnaireCompilationEvent.Failed(cause))
                },
            )
            _working.value = false
        }
    }

    private fun start() {
        _step.value = QuestionnaireCompilationStep.Starting
        viewModelScope.launch {
            runCatching {
                val careerId = observeActiveAccount()
                    .mapNotNull { it?.academic?.selectedCareerId }
                    .first()
                startQuestionnaire(
                    careerId = careerId,
                    target = QuestionnaireTarget(
                        activityChoiceId = key.activityChoiceId,
                        questionnaireId = key.questionnaireId,
                        questionnaireConfigId = key.questionnaireConfigId,
                        tags = key.tags,
                    ),
                )
            }.fold(
                onSuccess = { startResult ->
                    session = startResult.session
                    enterPage(startResult.firstPage, index = 0)
                },
                onFailure = { cause ->
                    _step.value = QuestionnaireCompilationStep.StartFailed(cause)
                },
            )
        }
    }

    private fun enterPage(page: QuestionnairePage, index: Int) {
        // Seed edits from what the server already has, so revisited pages show their answers.
        _answers.value = page.questions().associate { question ->
            question.id to QuestionAnswerState(
                selectedOptionIds = question.savedOptionIds,
                freeText = question.savedFreeText.orEmpty(),
            )
        }
        _invalidQuestionIds.value = emptySet()
        _step.value = QuestionnaireCompilationStep.Page(page = page, index = index)
    }

    private suspend fun saveCurrentAnswers(session: QuestionnaireSession, page: QuestionnairePage) {
        val payload = page.questions().flatMap { it.toAnswers() }
        if (payload.isEmpty()) return
        savePage(session, page.id, payload)
    }

    private fun QuestionnairePage.questions(): List<QuestionnaireQuestion> =
        paragraphs.flatMap { it.questions }

    private fun QuestionnaireQuestion.isAnswered(): Boolean {
        val state = _answers.value[id] ?: return false
        return when (kind) {
            QuestionnaireQuestionKind.FreeText -> state.freeText.isNotBlank()
            else -> state.selectedOptionIds.isNotEmpty() && state.selectedOptionIds.all { optionId ->
                val option = options.firstOrNull { it.id == optionId }
                option?.requiresFreeText != true || state.freeText.isNotBlank()
            }
        }
    }

    private fun QuestionnaireQuestion.toAnswers(): List<QuestionnaireAnswer> {
        val state = _answers.value[id] ?: return emptyList()
        return when (kind) {
            QuestionnaireQuestionKind.FreeText -> {
                val option = options.firstOrNull() ?: return emptyList()
                if (state.freeText.isBlank()) {
                    emptyList()
                } else {
                    listOf(QuestionnaireAnswer(questionId = id, optionId = option.id, freeText = state.freeText.trim()))
                }
            }

            else -> state.selectedOptionIds.mapNotNull { optionId ->
                val option = options.firstOrNull { it.id == optionId } ?: return@mapNotNull null
                QuestionnaireAnswer(
                    questionId = id,
                    optionId = optionId,
                    freeText = if (option.requiresFreeText) state.freeText.trim() else "",
                )
            }
        }
    }
}
