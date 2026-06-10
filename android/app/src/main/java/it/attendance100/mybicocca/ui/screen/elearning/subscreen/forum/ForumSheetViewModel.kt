package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumAttachmentUpload
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumGroup
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumHtml
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.forum.Post
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.CreateDiscussionUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.DeletePostUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.EditPostUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.GetCourseGroupsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.MarkDiscussionReadUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObserveDiscussionsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObserveForumUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObservePostsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.PrepareEditAttachmentsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.RefreshDiscussionsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.RefreshPostsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ReplyToPostUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.SetDiscussionFavouriteUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.SetDiscussionSubscriptionUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.UploadForumAttachmentsUseCase
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.ComposerTarget
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.ForumSheetEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.PendingAttachment
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.ThreadNode
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.buildThread
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.PageState
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.withContext

/**
 * Backs the multi-page forum sheet: the paged discussions list, the open discussion's post
 * thread, and the shared composer (new discussion / reply / edit) with its attachments.
 *
 * Data streams: [forum] and [discussions] from the Room cache, [pageState] (server paging over
 * the list), [openDiscussionId] with the derived [posts] and [thread], plus the composer state
 * — [composerTarget], [pendingAttachments], [composerGroups], [selectedGroupId] and
 * [submitting]. Sync status: [syncStatus] for the list, [threadSyncStatus] for the open thread,
 * and [initialFetchInProgress] for the cold-cache first load. One-shot: [eventFlow].
 *
 * Actions: [pullToRefresh], [loadMore], [openThread], [closeThread], [refreshOpenThread],
 * [toggleFavourite], [setSubscribed], [deletePost], [startNewDiscussion], [startReply],
 * [startEdit], [selectGroup], [addAttachment], [removeAttachment], [cancelComposer] and
 * [submitComposer]. The first page refreshes automatically when the active account becomes
 * available, marking the discussion read and loading its posts when the sheet was opened
 * directly on a discussion.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = ForumSheetViewModel.Factory::class)
class ForumSheetViewModel @AssistedInject constructor(
    @Assisted private val key: SheetRoute.Forum,
    @ApplicationContext private val appContext: Context,
    observeActiveAccount: ObserveActiveAccountUseCase,
    observeForum: ObserveForumUseCase,
    private val observeDiscussions: ObserveDiscussionsUseCase,
    private val refreshDiscussions: RefreshDiscussionsUseCase,
    private val observePosts: ObservePostsUseCase,
    private val refreshPosts: RefreshPostsUseCase,
    private val createDiscussion: CreateDiscussionUseCase,
    private val replyToPost: ReplyToPostUseCase,
    private val editPost: EditPostUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val setFavourite: SetDiscussionFavouriteUseCase,
    private val setSubscription: SetDiscussionSubscriptionUseCase,
    private val markRead: MarkDiscussionReadUseCase,
    private val uploadAttachments: UploadForumAttachmentsUseCase,
    private val getCourseGroups: GetCourseGroupsUseCase,
    private val prepareEditAttachments: PrepareEditAttachmentsUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: SheetRoute.Forum): ForumSheetViewModel
    }

    private val forumId = ForumId(key.forumId)
    private val courseId = CourseId(key.courseId)

    private val activeAccountId: Flow<AccountId?> = observeActiveAccount()
        .map { it?.id }
        .distinctUntilChanged()

    val forum: StateFlow<Loadable<Forum>> = activeAccountId
        .flatMapLatest { id -> if (id == null) flowOf(Loadable.NotYetLoaded) else observeForum(id, forumId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val discussions: StateFlow<Loadable<List<Discussion>>> = activeAccountId
        .flatMapLatest { id -> if (id == null) flowOf(Loadable.Loaded(emptyList())) else observeDiscussions(id, forumId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    private val _pageState = MutableStateFlow(PageState())
    val pageState: StateFlow<PageState> = _pageState.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _initialFetchInProgress = MutableStateFlow(false)
    val initialFetchInProgress: StateFlow<Boolean> = _initialFetchInProgress.asStateFlow()

    private val _openDiscussionId = MutableStateFlow<DiscussionId?>(null)

    /** Which discussion's thread is open; null shows the discussions list. */
    val openDiscussionId: StateFlow<DiscussionId?> = _openDiscussionId.asStateFlow()

    val posts: StateFlow<Loadable<List<Post>>> = _openDiscussionId
        .flatMapLatest { discussionId ->
            val id = discussionId ?: return@flatMapLatest flowOf(Loadable.NotYetLoaded)
            activeAccountId.flatMapLatest { account ->
                if (account == null) flowOf(Loadable.NotYetLoaded) else observePosts(account, id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val thread: StateFlow<List<ThreadNode>> = posts
        .map { loadable -> loadable.valueOrNull()?.let(::buildThread).orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), emptyList())

    private val _threadSyncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val threadSyncStatus: StateFlow<SyncStatus> = _threadSyncStatus.asStateFlow()

    private val _composerTarget = MutableStateFlow<ComposerTarget?>(null)
    val composerTarget: StateFlow<ComposerTarget?> = _composerTarget.asStateFlow()

    private val _pendingAttachments = MutableStateFlow<List<PendingAttachment>>(emptyList())
    val pendingAttachments: StateFlow<List<PendingAttachment>> = _pendingAttachments.asStateFlow()

    private val _composerGroups = MutableStateFlow<List<ForumGroup>>(emptyList())

    /**
     * The user's groups for the course; non-empty only in group-mode forums where the new
     * discussion must target a specific group.
     */
    val composerGroups: StateFlow<List<ForumGroup>> = _composerGroups.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<Int?>(null)
    val selectedGroupId: StateFlow<Int?> = _selectedGroupId.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    private val events = Channel<ForumSheetEvent>(Channel.BUFFERED)
    val eventFlow: Flow<ForumSheetEvent> = events.receiveAsFlow()

    init {
        viewModelScope.launch {
            activeAccountId.filterNotNull().distinctUntilChanged().collect { id ->
                val cached = observeDiscussions(id, forumId).first()
                _initialFetchInProgress.value = cached.valueOrNull().isNullOrEmpty()
                runFirstPage(id)
                _initialFetchInProgress.value = false
            }
        }
        key.initialDiscussionId?.let { openThread(DiscussionId(it)) }
    }

    private suspend fun runFirstPage(accountId: AccountId) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching { refreshDiscussions(accountId, forumId, page = 0, perPage = PAGE_SIZE) }
            .onSuccess { count ->
                _syncStatus.value = SyncStatus.Idle
                _pageState.value = PageState(loadedPages = 1, hasMore = count >= PAGE_SIZE, isLoadingMore = false)
            }
            .onFailure {
                _syncStatus.value = SyncStatus.Failed(it)
                events.trySend(ForumSheetEvent.Failed("Aggiornamento non riuscito", it))
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
                .onFailure { _pageState.value = state.copy(isLoadingMore = false) }
        }
    }

    fun openThread(discussionId: DiscussionId) {
        _openDiscussionId.value = discussionId
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { markRead(accountId, discussionId) }
            refreshThread(accountId, discussionId)
        }
    }

    fun closeThread() {
        _openDiscussionId.value = null
        _threadSyncStatus.value = SyncStatus.Idle
    }

    fun refreshOpenThread() {
        val discussionId = _openDiscussionId.value ?: return
        viewModelScope.launch { refreshThread(activeAccountId.filterNotNull().first(), discussionId) }
    }

    private suspend fun refreshThread(accountId: AccountId, discussionId: DiscussionId) {
        _threadSyncStatus.value = SyncStatus.Refreshing
        runCatching { refreshPosts(accountId, discussionId) }
            .onSuccess { _threadSyncStatus.value = SyncStatus.Idle }
            .onFailure {
                _threadSyncStatus.value = SyncStatus.Failed(it)
                events.trySend(ForumSheetEvent.Failed("Caricamento non riuscito", it))
            }
    }

    fun toggleFavourite(discussion: Discussion) {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { setFavourite(accountId, forumId, discussion.id, !discussion.isFavourite) }
                .onFailure { events.trySend(ForumSheetEvent.Failed("Operazione non riuscita", it)) }
        }
    }

    fun setSubscribed(discussionId: DiscussionId, subscribed: Boolean) {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { setSubscription(accountId, forumId, discussionId, subscribed) }
                .onSuccess {
                    events.trySend(
                        ForumSheetEvent.Info(if (subscribed) "Ora segui questa discussione" else "Non segui più questa discussione")
                    )
                }
                .onFailure { events.trySend(ForumSheetEvent.Failed("Operazione non riuscita", it)) }
        }
    }

    fun deletePost(post: Post) {
        val discussionId = _openDiscussionId.value ?: return
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { deletePostUseCase(accountId, discussionId, post.id) }
                .onFailure { events.trySend(ForumSheetEvent.Failed("Eliminazione non riuscita", it)) }
        }
    }

    /**
     * Opens the composer on a new top-level discussion and fetches the course groups in the
     * background; the fetched groups are applied only while the new-discussion composer is still
     * the active target, so an abandoned composer doesn't resurrect group state.
     */
    fun startNewDiscussion() {
        val canAttach = forum.value.valueOrNull()?.canAttachFiles == true
        _composerTarget.value = ComposerTarget.NewDiscussion(forumId, canAttach)
        _composerGroups.value = emptyList()
        _selectedGroupId.value = null
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { getCourseGroups(accountId, courseId) }
                .onSuccess { groups ->
                    if (_composerTarget.value is ComposerTarget.NewDiscussion) {
                        _composerGroups.value = groups
                        _selectedGroupId.value = groups.firstOrNull()?.id
                    }
                }
        }
    }

    fun selectGroup(groupId: Int?) {
        _selectedGroupId.value = groupId
    }

    fun startReply(post: Post) {
        val discussionId = _openDiscussionId.value ?: return
        val canAttach = forum.value.valueOrNull()?.canAttachFiles == true
        val subject = post.subject.ifBlank { "Discussione" }
        val replySubject = if (subject.startsWith("Re:", ignoreCase = true)) subject else "Re: $subject"
        _composerTarget.value = ComposerTarget.Reply(discussionId, post.id, replySubject, canAttach)
    }

    fun startEdit(post: Post) {
        val discussionId = _openDiscussionId.value ?: return
        _composerTarget.value = ComposerTarget.Edit(
            discussionId = discussionId,
            postId = post.id,
            subject = post.subject,
            initialMessage = ForumHtml.htmlToPlain(post.message),
            canAttach = forum.value.valueOrNull()?.canAttachFiles == true,
            existingAttachments = post.attachments,
        )
    }

    fun cancelComposer() {
        _composerTarget.value = null
        _pendingAttachments.value = emptyList()
        _composerGroups.value = emptyList()
        _selectedGroupId.value = null
    }

    fun addAttachment(attachment: PendingAttachment) {
        _pendingAttachments.value = _pendingAttachments.value + attachment
    }

    fun removeAttachment(attachment: PendingAttachment) {
        _pendingAttachments.value = _pendingAttachments.value - attachment
    }

    /**
     * Runs the call matching the composer target, uploading pending attachments into a draft
     * area first. An edit that adds attachments seeds its draft from the post's current
     * attachments before the new ones, so the originals survive the edit. Success closes the
     * composer — and, for a new discussion, opens its freshly created thread.
     */
    fun submitComposer(subject: String, message: String) {
        val target = _composerTarget.value ?: return
        if (message.isBlank()) return
        if (target is ComposerTarget.NewDiscussion && subject.isBlank()) return
        if (_submitting.value) return
        viewModelScope.launch {
            _submitting.value = true
            val accountId = activeAccountId.filterNotNull().first()
            runCatching {
                when (target) {
                    is ComposerTarget.NewDiscussion ->
                        createDiscussion(
                            accountId, target.forumId, subject.trim(), message,
                            groupId = _selectedGroupId.value,
                            attachmentDraftItemId = uploadNewAttachments(accountId),
                        )
                    is ComposerTarget.Reply -> {
                        replyToPost(
                            accountId, target.discussionId, target.parentPostId,
                            subject.trim().ifBlank { target.replySubject }, message,
                            uploadNewAttachments(accountId),
                        )
                        null
                    }
                    is ComposerTarget.Edit -> {
                        val draftId = if (_pendingAttachments.value.isNotEmpty()) {
                            val base = prepareEditAttachments(accountId, target.postId)
                            uploadAttachments(accountId, readUploads(), baseDraftItemId = base)
                        } else {
                            null
                        }
                        editPost(accountId, target.discussionId, target.postId, subject.trim(), message, draftId)
                        null
                    }
                }
            }.onSuccess { newDiscussionId ->
                cancelComposer()
                newDiscussionId?.let { openThread(it) }
            }.onFailure {
                events.trySend(ForumSheetEvent.Failed("Pubblicazione non riuscita", it))
            }
            _submitting.value = false
        }
    }

    private suspend fun uploadNewAttachments(accountId: AccountId): Int? {
        if (_pendingAttachments.value.isEmpty()) return null
        return uploadAttachments(accountId, readUploads())
    }

    private suspend fun readUploads(): List<ForumAttachmentUpload> = withContext(Dispatchers.IO) {
        _pendingAttachments.value.map { item ->
            val bytes = appContext.contentResolver.openInputStream(item.uri)?.use { it.readBytes() }
                ?: error("Impossibile leggere ${item.fileName}")
            ForumAttachmentUpload(item.fileName, item.mimeType, bytes)
        }
    }

    private companion object {
        const val KEEP_ALIVE_MS = 5_000L
        const val PAGE_SIZE = 25
    }
}
