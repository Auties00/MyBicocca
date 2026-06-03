package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.CreateDiscussionUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObserveDiscussionsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObserveForumUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.RefreshDiscussionsUseCase
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail.state.ForumDetailOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail.state.PageState
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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import it.attendance100.mybicocca.ui.navigation.AppRoute

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = ForumDetailViewModel.Factory::class)
class ForumDetailViewModel @AssistedInject constructor(
    @Assisted private val key: AppRoute.ForumDetail,
    savedState: SavedStateHandle,
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val observeForum: ObserveForumUseCase,
    private val observeDiscussions: ObserveDiscussionsUseCase,
    private val refreshDiscussions: RefreshDiscussionsUseCase,
    private val createDiscussion: CreateDiscussionUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: AppRoute.ForumDetail): ForumDetailViewModel
    }

    private val forumId: ForumId = ForumId(key.forumId)

    private val activeAccountId: Flow<AccountId?> = observeActiveAccount()
        .map { it?.id }
        .distinctUntilChanged()

    val forum: StateFlow<Loadable<Forum>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.NotYetLoaded) else observeForum(id, forumId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val discussions: StateFlow<Loadable<List<Discussion>>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.Loaded(emptyList())) else observeDiscussions(id, forumId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    private val _pageState = MutableStateFlow(PageState())
    val pageState: StateFlow<PageState> = _pageState.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // True only while fetching with no cached discussions to show — gates the full-screen
    // loading visual; pull-to-refresh and warm-cache refreshes stay on syncStatus alone.
    private val _initialFetchInProgress = MutableStateFlow(false)
    val initialFetchInProgress: StateFlow<Boolean> = _initialFetchInProgress.asStateFlow()

    private val _creatingDiscussion = MutableStateFlow(false)
    val creatingDiscussion: StateFlow<Boolean> = _creatingDiscussion.asStateFlow()

    private val oneShotChannel = Channel<ForumDetailOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<ForumDetailOneShotEvent> = oneShotChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            activeAccountId.filterNotNull().distinctUntilChanged().collect { id ->
                val snapshot = observeDiscussions(id, forumId).first()
                val hadCache = snapshot is Loadable.Loaded && snapshot.value.isNotEmpty()
                _initialFetchInProgress.value = !hadCache
                runFirstPage(id)
                _initialFetchInProgress.value = false
            }
        }
    }

    private suspend fun runFirstPage(accountId: AccountId) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching { refreshDiscussions(accountId, forumId, page = 0, perPage = PAGE_SIZE) }
            .onSuccess { count ->
                _syncStatus.value = SyncStatus.Idle
                _pageState.value = PageState(
                    loadedPages = 1,
                    hasMore = count >= PAGE_SIZE,
                    isLoadingMore = false,
                )
            }
            .onFailure {
                _syncStatus.value = SyncStatus.Failed(it)
                oneShotChannel.trySend(ForumDetailOneShotEvent.RefreshFailed(it))
            }
    }

    fun pullToRefresh() {
        viewModelScope.launch { runFirstPage(activeAccountId.filterNotNull().first()) }
    }

    fun loadMore() {
        val state = _pageState.value
        if (state.isLoadingMore || !state.hasMore || state.loadedPages == 0) return
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            _pageState.value = state.copy(isLoadingMore = true)
            runCatching { refreshDiscussions(accountId, forumId, page = state.loadedPages, perPage = PAGE_SIZE) }
                .onSuccess { count ->
                    _pageState.value = state.copy(
                        loadedPages = state.loadedPages + 1,
                        hasMore = count >= PAGE_SIZE,
                        isLoadingMore = false,
                    )
                }
                .onFailure {
                    _pageState.value = state.copy(isLoadingMore = false)
                    oneShotChannel.trySend(ForumDetailOneShotEvent.RefreshFailed(it))
                }
        }
    }

    fun openDiscussion(discussion: Discussion) {
        oneShotChannel.trySend(ForumDetailOneShotEvent.OpenDiscussion(discussion.id))
    }

    fun onCreateDiscussion(subject: String, message: String) {
        if (subject.isBlank() || message.isBlank() || _creatingDiscussion.value) return
        viewModelScope.launch {
            _creatingDiscussion.value = true
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { createDiscussion(accountId, forumId, subject, message) }
                .onSuccess {
                    oneShotChannel.trySend(ForumDetailOneShotEvent.DiscussionCreated(it))
                    runFirstPage(accountId)
                }
                .onFailure { oneShotChannel.trySend(ForumDetailOneShotEvent.RefreshFailed(it)) }
            _creatingDiscussion.value = false
        }
    }

    private companion object {
        const val KEY_FORUM_ID = "forumId"
        const val STATE_KEEP_ALIVE_MS = 5_000L
        const val PAGE_SIZE = 25
    }
}
