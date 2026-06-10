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
import it.attendance100.mybicocca.ui.navigation.route.AppRoute

/**
 * Backs [CourseDetailScreen] for one course, bound to its navigation entry through assisted
 * injection of the route key.
 *
 * Cached data streams (Room as source of truth, re-scoped when the active account changes):
 * [details], [assignments], [quizzes], [forums], [latestAnnouncement], [gradeItems],
 * [completion], [videoProgressByCmId], plus the derived [isFavourite].
 *
 * Sync state: [syncStatus] tracks the fan-out refresh of every course resource (details are
 * the only refresh whose failure surfaces; the others fail silently), while
 * [initialFetchInProgress] separately reports a fetch running with no useful cache.
 * [continueWatchingThumbnailUrl] tracks the on-demand thumbnail resolution for the hero card.
 *
 * UI selection persisted in [SavedStateHandle] across process death: [selectedTab],
 * [expandedSections], [expandedQuizGroups].
 *
 * One-shot events: [oneShotEvents] carries module-open requests (fed by the `emitOpen*`
 * actions) and refresh failures; they fire exactly once and never replay.
 *
 * Public actions: [selectTab], [toggleSection], [toggleQuizGroup], [onSetActivityCompleted],
 * [resolveContinueWatchingThumbnail], [pullToRefresh], [toggleFavourite], and the `emitOpen*`
 * family used by the screen's module router.
 */
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

    /**
     * The Quiz tab reuses the Contenuti expandable-card pattern but keys groups by section id
     * independently, so expanding a quiz card never leaks into the Contenuti tab.
     */
    val expandedQuizGroups: StateFlow<Set<Int>> = savedState
        .getStateFlow(KEY_QUIZ_EXPANDED, emptyList<Int>())
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

    /**
     * The course's single read-only "Avvisi" forum, when it has one. Drives the announcements
     * hero on the Forum tab.
     */
    private val newsForumId: Flow<ForumId?> = forums
        .map { loadable ->
            (loadable as? Loadable.Loaded)?.value?.firstOrNull { it.type == ForumType.News }?.id
        }
        .distinctUntilChanged()

    /**
     * Latest teacher announcement from the news forum. Independent stream from [forums]: the
     * card renders forum metadata immediately and fills the preview when this lands.
     */
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

    private val _initialFetchInProgress = MutableStateFlow(false)

    /**
     * True only while a network fetch runs because Room had nothing useful cached: Room is
     * snapshotted before the refresh kicks off, and a first emission that already has sections
     * counts as a warm cache hit that keeps the loading visual off. Distinct from [syncStatus],
     * which also fires for pull-to-refresh and for already-fresh refreshes that early-return
     * inside the repository.
     */
    val initialFetchInProgress: StateFlow<Boolean> = _initialFetchInProgress.asStateFlow()

    private val _continueWatchingThumbnailUrl =
        MutableStateFlow<Loadable<String?>>(Loadable.NotYetLoaded)

    /**
     * NotYetLoaded means resolution is in flight; Loaded carries the resolved url, which may be
     * null when the video genuinely has no thumbnail.
     */
    val continueWatchingThumbnailUrl: StateFlow<Loadable<String?>> =
        _continueWatchingThumbnailUrl.asStateFlow()

    private var continueWatchingThumbnailCmId: Int? = null

    private val oneShotChannel = Channel<CourseDetailOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<CourseDetailOneShotEvent> = oneShotChannel.receiveAsFlow()

    /**
     * News forums refreshed during this ViewModel's lifetime; refreshDiscussions has no
     * stale-policy gate, so this guards against re-fetching on every forums emission.
     */
    private val announcementRefreshed = mutableSetOf<Int>()

    init {
        viewModelScope.launch {
            activeAccountId.filterNotNull().distinctUntilChanged().collect { id ->
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

    fun toggleSection(sectionId: Int) = toggleExpansion(KEY_EXPANDED, sectionId)

    fun toggleQuizGroup(groupKey: Int) = toggleExpansion(KEY_QUIZ_EXPANDED, groupKey)

    private fun toggleExpansion(stateKey: String, id: Int) {
        val current = (savedState.get<List<Int>>(stateKey) ?: emptyList()).toMutableSet()
        if (!current.add(id)) current.remove(id)
        savedState[stateKey] = current.toList()
    }

    fun onSetActivityCompleted(cmId: Int, completed: Boolean) {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { setActivityCompleted(accountId, courseId, cmId, completed) }
        }
    }

    /**
     * Resolves the continue-watching thumbnail for the given video cmId, deduplicating repeat
     * requests for the same id and ignoring stale results after the target changes. A null
     * cmId settles on "no thumbnail" rather than a perpetual loading state.
     */
    fun resolveContinueWatchingThumbnail(cmId: Int?) {
        if (cmId == continueWatchingThumbnailCmId) return
        continueWatchingThumbnailCmId = cmId
        if (cmId == null) {
            _continueWatchingThumbnailUrl.value = Loadable.Loaded(null)
            return
        }
        _continueWatchingThumbnailUrl.value = Loadable.NotYetLoaded
        viewModelScope.launch {
            val url = runCatching { getVideoThumbnailUrl(cmId) }.getOrNull()
            if (continueWatchingThumbnailCmId == cmId) {
                _continueWatchingThumbnailUrl.value = Loadable.Loaded(url)
            }
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

    fun emitOpenDiscussion(forumId: ForumId, id: DiscussionId) {
        oneShotChannel.trySend(CourseDetailOneShotEvent.OpenDiscussion(forumId, id))
    }

    fun emitOpenResource(url: String) {
        oneShotChannel.trySend(CourseDetailOneShotEvent.OpenModuleResource(url))
    }

    fun emitOpenLink(title: String, url: String) {
        oneShotChannel.trySend(CourseDetailOneShotEvent.OpenLink(title, url))
    }

    fun emitOpenVideo(cmId: Int, title: String) {
        oneShotChannel.trySend(CourseDetailOneShotEvent.OpenVideo(cmId, title))
    }

    fun emitOpenFile(
        fileName: String,
        fileUrl: String,
        mimeType: String?,
        sizeBytes: Long?,
        forceChooser: Boolean = false,
    ) {
        oneShotChannel.trySend(
            CourseDetailOneShotEvent.OpenFile(fileName, fileUrl, mimeType, sizeBytes, forceChooser),
        )
    }

    fun emitOpenFolder(cmId: Int) {
        oneShotChannel.trySend(CourseDetailOneShotEvent.OpenFolder(cmId))
    }

    private companion object {
        const val KEY_COURSE_ID = "courseId"
        const val KEY_TAB = "course_detail_tab"
        const val KEY_EXPANDED = "course_detail_expanded"
        const val KEY_QUIZ_EXPANDED = "course_detail_quiz_expanded"
        const val STATE_KEEP_ALIVE_MS = 5_000L
    }
}

/** Pinned announcements can be old; "latest" means most recent activity regardless of pin. */
private fun List<Discussion>.pickLatest(): Discussion? =
    maxByOrNull { it.timeModified ?: it.createdAt ?: java.time.Instant.EPOCH }
