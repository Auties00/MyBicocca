package it.attendance100.mybicocca.ui.screen.elearning.subscreen.conversationDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.message.Conversation
import it.attendance100.mybicocca.domain.model.elearning.message.ConversationId
import it.attendance100.mybicocca.domain.model.elearning.message.Message
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.message.MarkConversationReadUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.message.ObserveConversationUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.message.ObserveMessagesUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.message.RefreshMessagesUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.message.SendMessageUseCase
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.conversationDetail.state.ConversationDetailOneShotEvent
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
class ConversationDetailViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val observeConversation: ObserveConversationUseCase,
    private val observeMessages: ObserveMessagesUseCase,
    private val refreshMessages: RefreshMessagesUseCase,
    private val sendMessage: SendMessageUseCase,
    private val markConversationRead: MarkConversationReadUseCase,
) : ViewModel() {

    private val conversationId: ConversationId = ConversationId(
        savedState.get<Int>(KEY_CONV_ID)
            ?: error("ConversationDetailViewModel requires $KEY_CONV_ID")
    )

    val composer: StateFlow<String> = savedState
        .getStateFlow(KEY_COMPOSER, "")
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val activeAccountId: Flow<AccountId?> = observeActiveAccount()
        .map { it?.id }
        .distinctUntilChanged()

    val conversation: StateFlow<Loadable<Conversation>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.NotYetLoaded) else observeConversation(id, conversationId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val messages: StateFlow<Loadable<List<Message>>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.Loaded(emptyList())) else observeMessages(id, conversationId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val oneShotChannel = Channel<ConversationDetailOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<ConversationDetailOneShotEvent> = oneShotChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            activeAccountId.filterNotNull().distinctUntilChanged().collect { id ->
                runRefresh(id)
                runCatching { markConversationRead(id, conversationId) }
            }
        }
    }

    fun setComposer(text: String) {
        savedState[KEY_COMPOSER] = text
    }

    fun pullToRefresh() {
        viewModelScope.launch { runRefresh(activeAccountId.filterNotNull().first()) }
    }

    fun markRead() {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { markConversationRead(accountId, conversationId) }
        }
    }

    fun onSend() {
        val text = composer.value.trim()
        if (text.isEmpty()) return
        val toUserId = otherMemberId() ?: return
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { sendMessage(accountId, toUserId, text) }
                .onSuccess {
                    setComposer("")
                    oneShotChannel.trySend(ConversationDetailOneShotEvent.MessageSent(it))
                    runRefresh(accountId)
                }
                .onFailure { oneShotChannel.trySend(ConversationDetailOneShotEvent.RefreshFailed(it)) }
        }
    }

    private fun otherMemberId(): Int? {
        val convo = conversation.value.valueOrNull() ?: return null
        return convo.members.firstOrNull()?.userId
    }

    private suspend fun runRefresh(accountId: AccountId) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching { refreshMessages(accountId, conversationId) }
            .onSuccess { _syncStatus.value = SyncStatus.Idle }
            .onFailure {
                _syncStatus.value = SyncStatus.Failed(it)
                oneShotChannel.trySend(ConversationDetailOneShotEvent.RefreshFailed(it))
            }
    }

    private companion object {
        const val KEY_CONV_ID = "conversationId"
        const val KEY_COMPOSER = "conversation_composer"
        const val STATE_KEEP_ALIVE_MS = 5_000L
    }
}
