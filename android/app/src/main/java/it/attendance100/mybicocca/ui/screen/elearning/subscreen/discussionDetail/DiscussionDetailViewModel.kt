package it.attendance100.mybicocca.ui.screen.elearning.subscreen.discussionDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.Post
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObservePostsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.RefreshPostsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ReplyToPostUseCase
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.discussionDetail.state.DiscussionDetailOneShotEvent
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
@HiltViewModel(assistedFactory = DiscussionDetailViewModel.Factory::class)
class DiscussionDetailViewModel @AssistedInject constructor(
    @Assisted private val key: AppRoute.DiscussionDetail,
    private val savedState: SavedStateHandle,
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val observePosts: ObservePostsUseCase,
    private val refreshPosts: RefreshPostsUseCase,
    private val replyToPost: ReplyToPostUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: AppRoute.DiscussionDetail): DiscussionDetailViewModel
    }

    private val discussionId: DiscussionId = DiscussionId(key.discussionId)

    val replyTarget: StateFlow<PostId?> = savedState
        .getStateFlow<Int?>(KEY_REPLY_TARGET, null)
        .map { it?.let(::PostId) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val activeAccountId: Flow<AccountId?> = observeActiveAccount()
        .map { it?.id }
        .distinctUntilChanged()

    val posts: StateFlow<Loadable<List<Post>>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.Loaded(emptyList())) else observePosts(id, discussionId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val oneShotChannel = Channel<DiscussionDetailOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<DiscussionDetailOneShotEvent> = oneShotChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            activeAccountId.filterNotNull().distinctUntilChanged().collect { id ->
                runRefresh(id)
            }
        }
    }

    fun setReplyTarget(target: PostId?) {
        savedState[KEY_REPLY_TARGET] = target?.value
    }

    fun pullToRefresh() {
        viewModelScope.launch { runRefresh(activeAccountId.filterNotNull().first()) }
    }

    private suspend fun runRefresh(accountId: AccountId) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching { refreshPosts(accountId, discussionId) }
            .onSuccess { _syncStatus.value = SyncStatus.Idle }
            .onFailure {
                _syncStatus.value = SyncStatus.Failed(it)
                oneShotChannel.trySend(DiscussionDetailOneShotEvent.RefreshFailed(it))
            }
    }

    fun submitReply(subject: String, message: String) {
        val parent = replyTarget.value ?: return
        if (subject.isBlank() || message.isBlank()) return
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { replyToPost(accountId, parent, subject, message) }
                .onSuccess {
                    oneShotChannel.trySend(DiscussionDetailOneShotEvent.ReplyPosted(it))
                    setReplyTarget(null)
                    runRefresh(accountId)
                }
                .onFailure { oneShotChannel.trySend(DiscussionDetailOneShotEvent.RefreshFailed(it)) }
        }
    }

    private companion object {
        const val KEY_DISCUSSION_ID = "discussionId"
        const val KEY_REPLY_TARGET = "discussion_reply_target"
        const val STATE_KEEP_ALIVE_MS = 5_000L
    }
}
