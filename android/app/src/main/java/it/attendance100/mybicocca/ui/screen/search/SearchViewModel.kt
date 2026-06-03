package it.attendance100.mybicocca.ui.screen.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.search.DictationEvent
import it.attendance100.mybicocca.domain.model.search.SearchAssistantStatus
import it.attendance100.mybicocca.domain.model.search.SearchHistoryEntry
import it.attendance100.mybicocca.domain.model.search.SearchResult
import it.attendance100.mybicocca.domain.model.search.SearchSuggestion
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.search.AddSearchHistoryEntryUseCase
import it.attendance100.mybicocca.domain.usecase.search.ClearSearchHistoryUseCase
import it.attendance100.mybicocca.domain.usecase.search.DictateUseCase
import it.attendance100.mybicocca.domain.usecase.search.EnsureSearchAssistantReadyUseCase
import it.attendance100.mybicocca.domain.usecase.search.GlobalSearchUseCase
import it.attendance100.mybicocca.domain.usecase.search.InterpretQueryUseCase
import it.attendance100.mybicocca.domain.usecase.search.ObserveSearchAssistantStatusUseCase
import it.attendance100.mybicocca.domain.usecase.search.ObserveSearchHistoryUseCase
import it.attendance100.mybicocca.domain.usecase.search.RemoveSearchHistoryEntryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    observeActiveAccount: ObserveActiveAccountUseCase,
    globalSearch: GlobalSearchUseCase,
    observeSearchHistory: ObserveSearchHistoryUseCase,
    observeAssistantStatus: ObserveSearchAssistantStatusUseCase,
    private val addHistoryEntry: AddSearchHistoryEntryUseCase,
    private val removeHistoryEntry: RemoveSearchHistoryEntryUseCase,
    private val clearHistoryEntries: ClearSearchHistoryUseCase,
    private val ensureAssistantReady: EnsureSearchAssistantReadyUseCase,
    private val interpretQuery: InterpretQueryUseCase,
    private val dictate: DictateUseCase,
) : ViewModel() {

    // Search text is user input — SavedStateHandle so it survives process death.
    val query: StateFlow<String> = savedState.getStateFlow(KEY_QUERY, "")

    private val activeAccount = observeActiveAccount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val accountAndCareer = activeAccount
        .map { it?.id to it?.academic?.selectedCareerId }
        .distinctUntilChanged()

    val results: StateFlow<List<SearchResult>> = accountAndCareer
        .flatMapLatest { (accountId, careerId) ->
            if (accountId == null || careerId == null) flowOf(emptyList())
            else globalSearch(
                query = query.debounce(QUERY_DEBOUNCE_MS).map { it.trim() }.distinctUntilChanged(),
                accountId = accountId,
                careerId = careerId,
            )
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), emptyList())

    val history: StateFlow<List<SearchHistoryEntry>> = activeAccount
        .map { it?.id }
        .distinctUntilChanged()
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(emptyList()) else observeSearchHistory(accountId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), emptyList())

    val assistantStatus: StateFlow<SearchAssistantStatus> = observeAssistantStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), SearchAssistantStatus.Unavailable)

    private val _aiSuggestion = MutableStateFlow<SearchSuggestion?>(null)
    val aiSuggestion: StateFlow<SearchSuggestion?> = _aiSuggestion.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _dictating = MutableStateFlow(false)
    val dictating: StateFlow<Boolean> = _dictating.asStateFlow()

    // Mic input level (0..1) while dictating, for the dialog's audio-reactive visuals.
    // Engines that can't measure it never emit, so this just stays at rest.
    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    private var aiJob: Job? = null
    private var dictationJob: Job? = null

    fun setQuery(value: String) {
        savedState[KEY_QUERY] = value
        // Typing invalidates the previous submit's AI card.
        aiJob?.cancel()
        _aiLoading.value = false
        _aiSuggestion.value = null
    }

    // IME search action: persist the query and, when on-device AI is around, interpret it.
    fun submit() {
        val q = query.value.trim()
        if (q.isEmpty()) return
        commitToHistory()
        if (assistantStatus.value != SearchAssistantStatus.Available) return
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _aiLoading.value = true
            runCatching { _aiSuggestion.value = interpretQuery(q) }
            _aiLoading.value = false
        }
    }

    // Result taps save the query without re-triggering the AI pass.
    fun commitToHistory() {
        val q = query.value.trim()
        if (q.isEmpty()) return
        val accountId = activeAccount.value?.id ?: return
        viewModelScope.launch { addHistoryEntry(accountId, q) }
    }

    fun removeFromHistory(historyQuery: String) {
        val accountId = activeAccount.value?.id ?: return
        viewModelScope.launch { removeHistoryEntry(accountId, historyQuery) }
    }

    fun clearHistory() {
        val accountId = activeAccount.value?.id ?: return
        viewModelScope.launch { clearHistoryEntries(accountId) }
    }

    fun downloadAssistant() {
        viewModelScope.launch { runCatching { ensureAssistantReady() } }
    }

    // Caller must hold RECORD_AUDIO. Partials stream straight into the query so results
    // update live while the user is still speaking.
    fun startDictation() {
        if (_dictating.value) return
        dictationJob = viewModelScope.launch {
            _dictating.value = true
            runCatching {
                dictate()
                    .onCompletion {
                        _dictating.value = false
                        _soundLevel.value = 0f
                    }
                    .collect { event ->
                        when (event) {
                            is DictationEvent.Transcript ->
                                savedState[KEY_QUERY] = event.transcript.text

                            is DictationEvent.SoundLevel -> _soundLevel.value = event.level
                        }
                    }
            }
            _dictating.value = false
            _soundLevel.value = 0f
        }
    }

    // Ends the session keeping whatever was heard so far.
    fun stopDictation() {
        dictationJob?.cancel()
        dictationJob = null
        _dictating.value = false
        _soundLevel.value = 0f
    }

    // Called when the search overlay closes: reset transient state but keep history.
    fun reset() {
        stopDictation()
        aiJob?.cancel()
        _aiLoading.value = false
        _aiSuggestion.value = null
        savedState[KEY_QUERY] = ""
    }

    private companion object {
        const val KEY_QUERY = "search_query"
        const val QUERY_DEBOUNCE_MS = 120L
        const val STATE_KEEP_ALIVE_MS = 5_000L
    }
}
