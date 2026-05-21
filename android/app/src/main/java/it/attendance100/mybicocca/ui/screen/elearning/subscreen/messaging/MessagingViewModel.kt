package it.attendance100.mybicocca.ui.screen.elearning.subscreen.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.message.Conversation
import it.attendance100.mybicocca.domain.model.elearning.message.ConversationId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.message.BlockUserUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.message.DeleteConversationsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.message.ObserveConversationsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.message.ObserveUnreadMessagesCountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.message.RefreshConversationsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.message.UnblockUserUseCase
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.messaging.state.MessagingOneShotEvent
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
class MessagingViewModel @Inject constructor(
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val observeConversations: ObserveConversationsUseCase,
    private val observeUnread: ObserveUnreadMessagesCountUseCase,
    private val refreshConversations: RefreshConversationsUseCase,
    private val deleteConversations: DeleteConversationsUseCase,
    private val blockUser: BlockUserUseCase,
    private val unblockUser: UnblockUserUseCase,
) : ViewModel() {

    private val activeAccountId: Flow<AccountId?> = observeActiveAccount()
        .map { it?.id }
        .distinctUntilChanged()

    val conversations: StateFlow<Loadable<List<Conversation>>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.Loaded(emptyList())) else observeConversations(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val unreadCount: StateFlow<Int> = activeAccountId
        .flatMapLatest { id -> if (id == null) flowOf(0) else observeUnread(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), 0)

    private val _selection = MutableStateFlow<Set<ConversationId>>(emptySet())
    val selection: StateFlow<Set<ConversationId>> = _selection.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val oneShotChannel = Channel<MessagingOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<MessagingOneShotEvent> = oneShotChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            activeAccountId.filterNotNull().distinctUntilChanged().collect { id ->
                runRefresh(id, force = false)
            }
        }
    }

    fun pullToRefresh() {
        viewModelScope.launch {
            runRefresh(activeAccountId.filterNotNull().first(), force = true)
        }
    }

    fun openConversation(id: ConversationId) {
        oneShotChannel.trySend(MessagingOneShotEvent.OpenConversation(id))
    }

    fun toggleSelection(id: ConversationId) {
        val updated = _selection.value.toMutableSet()
        if (!updated.add(id)) updated.remove(id)
        _selection.value = updated
    }

    fun clearSelection() {
        _selection.value = emptySet()
    }

    fun deleteSelected() {
        val ids = _selection.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { deleteConversations(accountId, ids) }
                .onSuccess { _selection.value = emptySet() }
                .onFailure { oneShotChannel.trySend(MessagingOneShotEvent.RefreshFailed(it)) }
        }
    }

    fun block(userId: Int) {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { blockUser(accountId, userId) }
        }
    }

    fun unblock(userId: Int) {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { unblockUser(accountId, userId) }
        }
    }

    private suspend fun runRefresh(accountId: AccountId, force: Boolean) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching { refreshConversations(accountId, force) }
            .onSuccess { _syncStatus.value = SyncStatus.Idle }
            .onFailure {
                _syncStatus.value = SyncStatus.Failed(it)
                oneShotChannel.trySend(MessagingOneShotEvent.RefreshFailed(it))
            }
    }

    private companion object {
        const val STATE_KEEP_ALIVE_MS = 5_000L
    }
}
