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
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ForumDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val observeForum: ObserveForumUseCase,
    private val observeDiscussions: ObserveDiscussionsUseCase,
    private val refreshDiscussions: RefreshDiscussionsUseCase,
    private val createDiscussion: CreateDiscussionUseCase,
) : ViewModel() {

    private val forumId: ForumId = ForumId(
        savedState.get<Int>(KEY_FORUM_ID) ?: error("ForumDetailViewModel requires $KEY_FORUM_ID")
    )

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

    private val oneShotChannel = Channel<ForumDetailOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<ForumDetailOneShotEvent> = oneShotChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            activeAccountId.filterNotNull().distinctUntilChanged().collect { id ->
                runFirstPage(id)
            }
        }
    }

    private suspend fun runFirstPage(accountId: AccountId) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching { refreshDiscussions(accountId, forumId, page = 0) }
            .onSuccess {
                _syncStatus.value = SyncStatus.Idle
                _pageState.value = PageState(loadedPages = 1, hasMore = true, isLoadingMore = false)
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
        if (state.isLoadingMore || !state.hasMore) return
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            _pageState.value = state.copy(isLoadingMore = true)
            runCatching { refreshDiscussions(accountId, forumId, page = state.loadedPages) }
                .onSuccess {
                    _pageState.value = state.copy(
                        loadedPages = state.loadedPages + 1,
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
        if (subject.isBlank() || message.isBlank()) return
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { createDiscussion(accountId, forumId, subject, message) }
                .onSuccess {
                    oneShotChannel.trySend(ForumDetailOneShotEvent.DiscussionCreated(it))
                    runFirstPage(accountId)
                }
                .onFailure { oneShotChannel.trySend(ForumDetailOneShotEvent.RefreshFailed(it)) }
        }
    }

    private companion object {
        const val KEY_FORUM_ID = "forumId"
        const val STATE_KEEP_ALIVE_MS = 5_000L
    }
}
