package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.course.CompletionState
import it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumType
import it.attendance100.mybicocca.domain.model.elearning.grade.GradeItem
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.ObserveCourseAssignmentsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.RefreshCourseAssignmentsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveCompletionStatesUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveCourseDetailsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.RefreshCourseDetailsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.SetActivityCompletedUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ToggleCourseFavouriteUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObserveCourseForumsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObserveDiscussionsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.RefreshCourseForumsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.RefreshDiscussionsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.grade.ObserveCourseGradeItemsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.grade.RefreshCourseGradeItemsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.ObserveCourseQuizzesUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.RefreshCourseQuizzesUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.video.GetVideoThumbnailUrlUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.video.ObserveCourseVideoProgressUseCase
import it.attendance100.mybicocca.domain.model.elearning.video.VideoProgress
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.CourseDetailOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.CourseTab
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
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
@HiltViewModel(assistedFactory = CourseDetailViewModel.Factory::class)
class CourseDetailViewModel @AssistedInject constructor(
    @Assisted private val key: AppRoute.CourseDetail,
    private val savedState: SavedStateHandle,
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val observeDetails: ObserveCourseDetailsUseCase,
    private val observeAssignments: ObserveCourseAssignmentsUseCase,
    private val observeQuizzes: ObserveCourseQuizzesUseCase,
    private val observeForums: ObserveCourseForumsUseCase,
    private val observeDiscussions: ObserveDiscussionsUseCase,
    private val refreshDiscussions: RefreshDiscussionsUseCase,
    private val observeGradeItems: ObserveCourseGradeItemsUseCase,
    private val observeCompletion: ObserveCompletionStatesUseCase,
    private val refreshDetails: RefreshCourseDetailsUseCase,
    private val refreshAssignments: RefreshCourseAssignmentsUseCase,
    private val refreshQuizzes: RefreshCourseQuizzesUseCase,
    private val refreshForums: RefreshCourseForumsUseCase,
    private val refreshGrades: RefreshCourseGradeItemsUseCase,
    private val setActivityCompleted: SetActivityCompletedUseCase,
    private val toggleCourseFavourite: ToggleCourseFavouriteUseCase,
    private val observeCourseVideoProgress: ObserveCourseVideoProgressUseCase,
    private val getVideoThumbnailUrl: GetVideoThumbnailUrlUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: AppRoute.CourseDetail): CourseDetailViewModel
    }

    private val courseId: CourseId = CourseId(key.courseId)

    val selectedTab: StateFlow<CourseTab> = savedState
        .getStateFlow(KEY_TAB, CourseTab.Syllabus.name)
        .map { runCatching { CourseTab.valueOf(it) }.getOrDefault(CourseTab.Syllabus) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CourseTab.Syllabus)

    val expandedSections: StateFlow<Set<Int>> = savedState
        .getStateFlow(KEY_EXPANDED, emptyList<Int>())
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val activeAccountId: Flow<AccountId?> = observeActiveAccount()
        .map { it?.id }
        .distinctUntilChanged()

    val details: StateFlow<Loadable<CourseDetails>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.NotYetLoaded) else observeDetails(id, courseId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val assignments: StateFlow<Loadable<List<Assignment>>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.Loaded(emptyList())) else observeAssignments(id, courseId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val quizzes: StateFlow<Loadable<List<Quiz>>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.Loaded(emptyList())) else observeQuizzes(id, courseId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val forums: StateFlow<Loadable<List<Forum>>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.Loaded(emptyList())) else observeForums(id, courseId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    // The course's single read-only "Avvisi" forum, when it has one. Drives the announcements
    // hero on the Forum tab.
    private val newsForumId: Flow<ForumId?> = forums
        .map { loadable ->
            (loadable as? Loadable.Loaded)?.value?.firstOrNull { it.type == ForumType.News }?.id
        }
        .distinctUntilChanged()

    // Latest teacher announcement from the news forum. Independent stream from `forums`:
    // the card renders forum metadata immediately and fills the preview when this lands.
    val latestAnnouncement: StateFlow<Loadable<Discussion?>> = activeAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) return@flatMapLatest flowOf(Loadable.NotYetLoaded)
            newsForumId.flatMapLatest { forumId ->
                if (forumId == null) flowOf(Loadable.NotYetLoaded)
                else observeDiscussions(accountId, forumId).map { loadable ->
                    when (loadable) {
                        is Loadable.Loaded -> Loadable.Loaded(loadable.value.pickLatest())
                        Loadable.NotYetLoaded -> Loadable.NotYetLoaded
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val gradeItems: StateFlow<Loadable<List<GradeItem>>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.Loaded(emptyList())) else observeGradeItems(id, courseId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val completion: StateFlow<Map<Int, CompletionState>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyMap()) else observeCompletion(id, courseId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), emptyMap())

    val videoProgressByCmId: StateFlow<Map<Int, VideoProgress>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyMap()) else observeCourseVideoProgress(id, courseId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), emptyMap())

    val isFavourite: StateFlow<Boolean> = details
        .map { (it as? Loadable.Loaded)?.value?.enrolled?.isFavourite == true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), false)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // True only when this VM had to fetch from the network because Room had nothing useful
    // cached. Distinct from `syncStatus`, which also fires for pull-to-refresh and for
    // already-fresh refreshes that early-return inside the repo.
    private val _initialFetchInProgress = MutableStateFlow(false)
    val initialFetchInProgress: StateFlow<Boolean> = _initialFetchInProgress.asStateFlow()

    private val _continueWatchingThumbnailUrl = MutableStateFlow<String?>(null)
    val continueWatchingThumbnailUrl: StateFlow<String?> = _continueWatchingThumbnailUrl.asStateFlow()

    private var continueWatchingThumbnailCmId: Int? = null

    private val oneShotChannel = Channel<CourseDetailOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<CourseDetailOneShotEvent> = oneShotChannel.receiveAsFlow()

    // News forums refreshed during this VM's lifetime; refreshDiscussions has no stale-policy
    // gate, so guard here against re-fetching on every forums emission.
    private val announcementRefreshed = mutableSetOf<Int>()

    init {
        viewModelScope.launch {
            activeAccountId.filterNotNull().distinctUntilChanged().collect { id ->
                // Snapshot Room before kicking off refresh: if the first emission already
                // has sections we're a warm cache hit and the loading visual stays off.
                val snapshot = observeDetails(id, courseId).first()
                val hadCache = snapshot is Loadable.Loaded &&
                    snapshot.value.sections.isNotEmpty()
                _initialFetchInProgress.value = !hadCache
                runRefresh(id, force = false)
                _initialFetchInProgress.value = false
            }
        }
        viewModelScope.launch {
            newsForumId.filterNotNull().collect { forumId ->
                if (!announcementRefreshed.add(forumId.value)) return@collect
                val accountId = activeAccountId.filterNotNull().first()
                runCatching { refreshDiscussions(accountId, forumId, page = 0, perPage = 10) }
            }
        }
    }

    fun selectTab(tab: CourseTab) {
        savedState[KEY_TAB] = tab.name
    }

    fun toggleSection(sectionId: Int) {
        val current = (savedState.get<List<Int>>(KEY_EXPANDED) ?: emptyList()).toMutableSet()
        if (!current.add(sectionId)) current.remove(sectionId)
        savedState[KEY_EXPANDED] = current.toList()
    }

    fun onSetActivityCompleted(cmId: Int, completed: Boolean) {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { setActivityCompleted(accountId, courseId, cmId, completed) }
        }
    }

    fun resolveContinueWatchingThumbnail(cmId: Int?) {
        if (cmId == continueWatchingThumbnailCmId) return
        continueWatchingThumbnailCmId = cmId
        _continueWatchingThumbnailUrl.value = null
        if (cmId == null) return
        viewModelScope.launch {
            val url = runCatching { getVideoThumbnailUrl(cmId) }.getOrNull()
            if (continueWatchingThumbnailCmId == cmId) _continueWatchingThumbnailUrl.value = url
        }
    }

    fun pullToRefresh() {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runRefresh(accountId, force = true)
        }
    }

    fun toggleFavourite() {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            val next = !isFavourite.value
            runCatching { toggleCourseFavourite(accountId, courseId, next) }
        }
    }

    private suspend fun runRefresh(accountId: AccountId, force: Boolean) {
        _syncStatus.value = SyncStatus.Refreshing
        val outcome = runCatching {
            coroutineScope {
                launch { refreshDetails(accountId, courseId, force) }
                launch { runCatching { refreshAssignments(accountId, courseId, force) } }
                launch { runCatching { refreshQuizzes(accountId, courseId, force) } }
                launch { runCatching { refreshForums(accountId, courseId, force) } }
                launch { runCatching { refreshGrades(accountId, courseId, force) } }
            }
        }
        outcome
            .onSuccess { _syncStatus.value = SyncStatus.Idle }
            .onFailure {
                _syncStatus.value = SyncStatus.Failed(it)
                oneShotChannel.trySend(CourseDetailOneShotEvent.RefreshFailed(it))
            }
    }

    fun emitOpenAssignment(id: Int) {
        oneShotChannel.trySend(CourseDetailOneShotEvent.OpenAssignment(AssignmentId(id)))
    }

    fun emitOpenQuiz(id: Int) {
        oneShotChannel.trySend(CourseDetailOneShotEvent.OpenQuiz(QuizId(id)))
    }

    fun emitOpenForum(id: Int) {
        oneShotChannel.trySend(CourseDetailOneShotEvent.OpenForum(ForumId(id)))
    }

    fun emitOpenDiscussion(id: DiscussionId) {
        oneShotChannel.trySend(CourseDetailOneShotEvent.OpenDiscussion(id))
    }

    fun emitOpenResource(url: String) {
        oneShotChannel.trySend(CourseDetailOneShotEvent.OpenModuleResource(url))
    }

    fun emitOpenVideo(cmId: Int, title: String) {
        oneShotChannel.trySend(CourseDetailOneShotEvent.OpenVideo(cmId, title))
    }

    fun emitOpenFile(fileName: String, fileUrl: String, mimeType: String?, sizeBytes: Long?) {
        oneShotChannel.trySend(
            CourseDetailOneShotEvent.OpenFile(fileName, fileUrl, mimeType, sizeBytes),
        )
    }

    fun emitOpenFolder(cmId: Int) {
        oneShotChannel.trySend(CourseDetailOneShotEvent.OpenFolder(cmId))
    }

    private companion object {
        const val KEY_COURSE_ID = "courseId"
        const val KEY_TAB = "course_detail_tab"
        const val KEY_EXPANDED = "course_detail_expanded"
        const val STATE_KEEP_ALIVE_MS = 5_000L
    }
}

// Pinned announcements can be old; "latest" means most recent activity regardless of pin.
private fun List<Discussion>.pickLatest(): Discussion? =
    maxByOrNull { it.timeModified ?: it.createdAt ?: java.time.Instant.EPOCH }
