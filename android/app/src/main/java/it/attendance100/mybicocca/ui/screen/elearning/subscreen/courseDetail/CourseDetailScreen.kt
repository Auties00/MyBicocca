package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CompletionState
import it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseModule
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleType
import it.attendance100.mybicocca.domain.model.elearning.course.kalvidresCmIdOrNull
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumType
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.domain.model.elearning.video.VideoProgress
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.ActivityEmpty
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.AnnouncementsCard
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.AssignmentRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.CourseHero
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.CourseTabBar
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.ForumRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.QuizRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.SectionTitle
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.SectionsList
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.SyllabusContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.UpNextCard
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.ContinuePlayable
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.CourseDetailOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.CourseTab
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.PickedContinueWatching
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.UpNextItem
import it.attendance100.mybicocca.ui.screen.elearning.theme.CourseDetailTheme
import it.attendance100.mybicocca.ui.screen.elearning.theme.heroShapesFor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun CourseDetailActions(viewModel: CourseDetailViewModel = hiltViewModel()) {
    val scheme = MaterialTheme.colorScheme
    val favourite by viewModel.isFavourite.collectAsStateWithLifecycle()
    IconButton(onClick = viewModel::toggleFavourite) {
        Icon(
            imageVector = if (favourite) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = if (favourite) "Rimuovi dai preferiti" else "Aggiungi ai preferiti",
            tint = if (favourite) scheme.tertiary else scheme.onSurface,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CourseDetailScreen(
    courseId: Int,
    // Height of the global top bar. The page draws edge to edge behind the (see-through) bar
    // and re-applies this as list contentPadding, so content scrolls visibly through the bar
    // region instead of being clipped at its bottom edge.
    topBarInset: Dp = 0.dp,
    onProvideTitle: (String?) -> Unit = {},
    onProvideActions: ((@Composable () -> Unit)?) -> Unit = {},
    onOpenAssignment: (AssignmentId) -> Unit = {},
    onOpenQuiz: (QuizId) -> Unit = {},
    onOpenForum: (ForumId) -> Unit = {},
    onOpenDiscussion: (DiscussionId) -> Unit = {},
    onOpenResource: (String) -> Unit = {},
    onOpenVideo: (cmId: Int, title: String) -> Unit = { _, _ -> },
    viewModel: CourseDetailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val courseIdValue = remember(courseId) { CourseId(courseId) }
    val heroShapes = remember(courseIdValue) { heroShapesFor(courseIdValue) }
    val detailsLoadable by viewModel.details.collectAsStateWithLifecycle()
    val assignmentsLoadable by viewModel.assignments.collectAsStateWithLifecycle()
    val quizzesLoadable by viewModel.quizzes.collectAsStateWithLifecycle()
    val forumsLoadable by viewModel.forums.collectAsStateWithLifecycle()
    val latestAnnouncement by viewModel.latestAnnouncement.collectAsStateWithLifecycle()
    val completion by viewModel.completion.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val expanded by viewModel.expandedSections.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val initialFetchInProgress by viewModel.initialFetchInProgress.collectAsStateWithLifecycle()
    val videoProgress by viewModel.videoProgressByCmId.collectAsStateWithLifecycle()
    val continueWatchingThumbnailUrl by viewModel.continueWatchingThumbnailUrl.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current

    // Publish the favourite toggle up to the shell so the global top bar can morph it during the
    // back/forward transition (the local bar that normally shows it is hidden mid-morph). The
    // lambda captures this entry-scoped ViewModel, so it stays valid when the shell renders it.
    DisposableEffect(viewModel) {
        onProvideActions { CourseDetailActions(viewModel = viewModel) }
        onDispose { onProvideActions(null) }
    }

    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is CourseDetailOneShotEvent.OpenAssignment -> onOpenAssignment(event.id)
                is CourseDetailOneShotEvent.OpenQuiz -> onOpenQuiz(event.id)
                is CourseDetailOneShotEvent.OpenForum -> onOpenForum(event.id)
                is CourseDetailOneShotEvent.OpenDiscussion -> onOpenDiscussion(event.id)
                is CourseDetailOneShotEvent.OpenVideo -> onOpenVideo(event.cmId, event.title)
                is CourseDetailOneShotEvent.OpenModuleResource -> {
                    onOpenResource(event.url)
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                }
                is CourseDetailOneShotEvent.RefreshFailed ->
                    snackbar.showError("Sincronizzazione del corso non riuscita", event.cause)
            }
        }
    }

    CourseDetailTheme(courseId = courseIdValue) {
    val scheme = MaterialTheme.colorScheme
    val pullState = rememberPullToRefreshState()
    val listState = rememberLazyListState()
    val pullScope = rememberCoroutineScope()
    // Pull-to-refresh indicator stays only while the user-pull dismiss animation runs;
    // the shapes-loading indicator picks up afterwards (or on cold open).
    var pullIndicatorVisible by remember { mutableStateOf(false) }

    // Roughly the bottom of the hero's headline; the bar title fades in past this.
    val density = LocalDensity.current
    val titleHideThresholdPx = remember(density) { with(density) { 120.dp.toPx() } }
    val titleHidden by remember(listState) {
        derivedStateOf {
            val hero = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == "hero" }
            hero == null || -hero.offset >= titleHideThresholdPx
        }
    }
    // Drive off the tab bar's own offset (not hero.bottom) so spacedBy doesn't put a
    // 2dp seam between the inline copy and the overlay at the swap moment. The morph is
    // continuous over the bar's last stretch of travel, so by the time the overlay swaps
    // in at progress 1 the two copies render identically — no snap.
    val tabBarMorphPx = remember(density) { with(density) { TAB_BAR_MORPH_DISTANCE.toPx() } }
    val tabBarPinProgress by remember(listState) {
        derivedStateOf {
            val tabBar = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == "tab_bar" }
            when {
                tabBar != null -> (1f - tabBar.offset / tabBarMorphPx).coerceIn(0f, 1f)
                listState.firstVisibleItemIndex > 1 -> 1f
                else -> 0f
            }
        }
    }
    val tabBarPinned by remember { derivedStateOf { tabBarPinProgress >= 1f } }

    // One pager drives both the swipeable tab content and the tab bar's indicator; the
    // SavedStateHandle-backed selectedTab stays the source of truth so the tab survives
    // process death (tab clicks land there first, then animate the pager from here).
    val pagerState = rememberPagerState(initialPage = selectedTab.ordinal) { CourseTab.entries.size }
    LaunchedEffect(pagerState, viewModel) {
        snapshotFlow { pagerState.currentPage }
            .collect { page -> viewModel.selectTab(CourseTab.entries[page]) }
    }
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab.ordinal) {
            pagerState.animateScrollToPage(selectedTab.ordinal)
        }
    }
    // Landing on a new tab while the bar is pinned starts that tab from its top; when the
    // bar is still in-list the scroll position is left alone, matching the old behavior.
    LaunchedEffect(pagerState, listState) {
        snapshotFlow { pagerState.settledPage }
            .drop(1)
            .collect { if (tabBarPinned) listState.animateScrollToItem(TAB_BAR_ITEM_INDEX) }
    }

    val courseTitle = detailsLoadable.valueOrNull()?.enrolled?.fullName?.trim().orEmpty()
    LaunchedEffect(titleHidden, courseTitle) {
        onProvideTitle(courseTitle.takeIf { titleHidden && it.isNotBlank() })
    }
    DisposableEffect(Unit) {
        onDispose { onProvideTitle(null) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.surfaceContainer),
    ) {
        PullToRefreshBox(
            isRefreshing = pullIndicatorVisible,
            onRefresh = {
                pullIndicatorVisible = true
                viewModel.pullToRefresh()
                pullScope.launch {
                    delay(PULL_INDICATOR_DISMISS_DELAY_MS)
                    pullIndicatorVisible = false
                }
            },
            state = pullState,
            modifier = Modifier.fillMaxSize(),
            // The box is full-bleed now, so the spinner must drop below the floating bar
            // instead of emerging behind it.
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullState,
                    isRefreshing = pullIndicatorVisible,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = topBarInset),
                )
            },
        ) {
            val details = detailsLoadable.valueOrNull()
            val hasFullData = details != null && details.sections.isNotEmpty()
            val continueWatching = remember(details, completion, videoProgress, continueWatchingThumbnailUrl) {
                details?.let { pickContinueWatching(it, completion, videoProgress, continueWatchingThumbnailUrl) }
            }
            LaunchedEffect(continueWatching?.module?.cmId) {
                viewModel.resolveContinueWatchingThumbnail(continueWatching?.module?.cmId)
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = topBarInset, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                item("hero") {
                    val refreshing = syncStatus is SyncStatus.Refreshing
                    val shapesSpinning = initialFetchInProgress ||
                        (refreshing && !pullIndicatorVisible)
                    CourseHero(
                        details = details,
                        continueWatching = continueWatching?.playable,
                        shapes = heroShapes,
                        isLoading = shapesSpinning,
                        onResume = {
                            continueWatching?.module?.let { openModule(it, viewModel) }
                        },
                        onGoToLesson = {
                            viewModel.selectTab(CourseTab.Content)
                            continueWatching?.let { picked ->
                                if (picked.sectionId !in expanded) {
                                    viewModel.toggleSection(picked.sectionId)
                                }
                            }
                        },
                    )
                }
                if (hasFullData && details != null) {
                    // Not stickyHeader — that would let the list's stretchy overscroll
                    // deform the pinned bar; the overlay below handles the pinned state.
                    item(key = "tab_bar") {
                        CourseTabBar(
                            pagerState = pagerState,
                            onSelect = viewModel::selectTab,
                            pinProgress = { tabBarPinProgress },
                            modifier = Modifier.animateItem(),
                        )
                    }

                    // Pages wrap their content height, so the outer list stays the only
                    // vertical scrollable and the hero/title/pin behavior is untouched.
                    item(key = "tab_pager") {
                        HorizontalPager(
                            state = pagerState,
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth(),
                        ) { page ->
                            when (CourseTab.entries[page]) {
                                CourseTab.Syllabus -> SyllabusContent(details = details)
                                CourseTab.Content -> ContentPage(
                                    details = details,
                                    expanded = expanded,
                                    completion = completion,
                                    videoProgress = videoProgress,
                                    onToggleSection = viewModel::toggleSection,
                                    onModuleClick = { openModule(it, viewModel) },
                                )
                                CourseTab.Quiz -> QuizzesPage(
                                    quizzesLoadable = quizzesLoadable,
                                    details = details,
                                    completion = completion,
                                    onClick = { viewModel.emitOpenQuiz(it.value) },
                                )
                                CourseTab.Assignments -> AssignmentsPage(
                                    assignmentsLoadable = assignmentsLoadable,
                                    onClick = { viewModel.emitOpenAssignment(it.value) },
                                )
                                CourseTab.Forum -> ForumsPage(
                                    forumsLoadable = forumsLoadable,
                                    latestAnnouncement = latestAnnouncement,
                                    onClick = { viewModel.emitOpenForum(it.value) },
                                    onOpenDiscussion = { viewModel.emitOpenDiscussion(it) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Soft fade under the status bar so scrolling content doesn't collide with the system
        // icons while it travels through the see-through bar region. Same hue as the page
        // background, so it is invisible at rest.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(
                    Brush.verticalGradient(
                        listOf(scheme.surfaceContainer, scheme.surfaceContainer.copy(alpha = 0f)),
                    ),
                ),
        )

        // Sits outside the LazyColumn so the list's overscroll doesn't deform it; offset below
        // the global bar, which is opaque again by the time the tab bar can reach it.
        if (tabBarPinned) {
            CourseTabBar(
                pagerState = pagerState,
                onSelect = viewModel::selectTab,
                pinProgress = { 1f },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = topBarInset),
            )
        }
    }
    }
}

private fun openModule(module: CourseModule, viewModel: CourseDetailViewModel) {
    val kalvidresCmId = module.kalvidresCmIdOrNull()
    when {
        module.type == ModuleType.Quiz ->
            module.instanceId?.let { viewModel.emitOpenQuiz(it) }
        module.type == ModuleType.Assign ->
            module.instanceId?.let { viewModel.emitOpenAssignment(it) }
        module.type == ModuleType.Forum ->
            module.instanceId?.let { viewModel.emitOpenForum(it) }
        kalvidresCmId != null ->
            viewModel.emitOpenVideo(kalvidresCmId, module.name)
        else ->
            module.url?.takeIf { it.isNotBlank() }?.let { viewModel.emitOpenResource(it) }
    }
}

@Composable
private fun ContentPage(
    details: CourseDetails,
    expanded: Set<Int>,
    completion: Map<Int, CompletionState>,
    videoProgress: Map<Int, VideoProgress>,
    onToggleSection: (Int) -> Unit,
    onModuleClick: (CourseModule) -> Unit,
) {
    if (details.sections.isEmpty()) {
        ActivityEmpty(message = "Nessuna sezione disponibile")
    } else {
        Column {
            Spacer(Modifier.height(12.dp))
            SectionsList(
                sections = details.sections,
                expanded = expanded,
                completion = completion,
                videoProgress = videoProgress,
                onToggleSection = onToggleSection,
                onModuleClick = onModuleClick,
            )
        }
    }
}

@Composable
private fun QuizzesPage(
    quizzesLoadable: Loadable<List<Quiz>>,
    details: CourseDetails?,
    completion: Map<Int, CompletionState>,
    onClick: (QuizId) -> Unit,
) {
    when (quizzesLoadable) {
        Loadable.NotYetLoaded -> ListLoadingRow()
        is Loadable.Loaded -> {
            val list = quizzesLoadable.value
            if (list.isEmpty()) {
                ActivityEmpty(message = "Nessun quiz nel corso")
            } else {
                // Real courses ship up to ~100 quizzes with heavily repeated names
                // ("Esercizio 1.2", "Esercizi da svolgere" ×12); the section a quiz lives
                // in is what gives the row its identity, so the list mirrors course order
                // grouped by section instead of the cache's flat alphabetical order.
                val groups = remember(list, details) { groupQuizzesBySection(list, details) }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Spacer(Modifier.height(12.dp))
                    val tracked = list.count { completion[it.cmId]?.isTracked == true }
                    val done = list.count { completion[it.cmId]?.isCompleted == true }
                    if (list.size >= 2 && tracked >= list.size / 2) {
                        QuizProgressCard(done = done, total = list.size)
                    }
                    groups.forEachIndexed { index, group ->
                        if (group.title != null) {
                            SectionTitle(
                                title = "${group.title} · ${group.quizzes.size}",
                                mark = QuizSectionMarks[index % QuizSectionMarks.size],
                            )
                        }
                        group.quizzes.forEach { quiz ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                QuizRow(
                                    quiz = quiz,
                                    completed = completion[quiz.cmId]?.isCompleted == true,
                                    onClick = { onClick(quiz.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QuizProgressCard(done: Int, total: Int) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = scheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Avanzamento",
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "$done di $total",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearWavyProgressIndicator(
                progress = { if (total == 0) 0f else (done.toFloat() / total).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private data class QuizSectionGroup(
    val title: String?,
    val quizzes: List<Quiz>,
)

private val QuizSectionMarks = listOf("✦", "❀", "✸", "✿")

private fun groupQuizzesBySection(quizzes: List<Quiz>, details: CourseDetails?): List<QuizSectionGroup> {
    val sections = details?.sections.orEmpty()
    if (sections.isEmpty()) return listOf(QuizSectionGroup(title = null, quizzes = quizzes))
    val byCmId = quizzes.filter { it.cmId != null }.associateBy { it.cmId }
    val grouped = mutableListOf<QuizSectionGroup>()
    val seen = mutableSetOf<QuizId>()
    sections.forEach { section ->
        val inSection = section.modules.mapNotNull { module -> byCmId[module.cmId] }
        if (inSection.isNotEmpty()) {
            inSection.forEach { seen += it.id }
            grouped += QuizSectionGroup(
                title = section.name.takeIf { it.isNotBlank() } ?: "Sezione ${section.sectionNumber}",
                quizzes = inSection,
            )
        }
    }
    val leftover = quizzes.filterNot { it.id in seen }
    if (leftover.isNotEmpty()) {
        grouped += QuizSectionGroup(
            title = if (grouped.isEmpty()) null else "Altri quiz",
            quizzes = leftover,
        )
    }
    // A single group adds header noise without disambiguating anything — drop the title.
    return if (grouped.size == 1) listOf(grouped.first().copy(title = null)) else grouped
}

@Composable
private fun AssignmentsPage(
    assignmentsLoadable: Loadable<List<Assignment>>,
    onClick: (AssignmentId) -> Unit,
) {
    val list = assignmentsLoadable.valueOrNull()
    val now = Instant.now()
    val next = list?.let { pickNextDueAssignment(it, now) }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (next != null) {
            Spacer(Modifier.height(12.dp))
            UpNextCard(
                item = UpNextItem(
                    title = next.name,
                    subtitle = next.intro?.takeIf { it.isNotBlank() }?.let { stripTagsShort(it) },
                    dueAt = next.dueDate!!,
                    onClick = { onClick(next.id) },
                ),
                now = now,
                onSubmit = { onClick(next.id) },
                onViewBrief = { onClick(next.id) },
            )
        }
        when (assignmentsLoadable) {
            Loadable.NotYetLoaded -> ListLoadingRow()
            is Loadable.Loaded -> {
                val values = assignmentsLoadable.value
                if (values.isEmpty()) {
                    ActivityEmpty(message = "Nessun compito assegnato")
                } else {
                    groupAssignments(values, now).forEach { group ->
                        SectionTitle(title = "${group.title} · ${group.items.size}", mark = group.mark)
                        group.items.forEach { a ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                AssignmentRow(assignment = a, now = now, onClick = { onClick(a.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class AssignmentGroup(
    val key: String,
    val title: String,
    val mark: String,
    val items: List<Assignment>,
)

// Open work first (soonest deadline on top, deadline-less hand-ins after), then what was
// already handed in, then deadlines that slipped by — dead work goes last.
private fun groupAssignments(assignments: List<Assignment>, now: Instant): List<AssignmentGroup> {
    val open = mutableListOf<Assignment>()
    val handedIn = mutableListOf<Assignment>()
    val expired = mutableListOf<Assignment>()
    for (a in assignments) {
        val status = a.submissionStatus
        when {
            status is SubmissionStatus.Submitted || status is SubmissionStatus.Graded -> handedIn += a
            a.dueDate != null && a.dueDate.isBefore(now) -> expired += a
            else -> open += a
        }
    }
    open.sortWith(compareBy(nullsLast()) { it.dueDate })
    handedIn.sortWith(compareByDescending { it.submittedAtOrNull() ?: Instant.EPOCH })
    expired.sortWith(compareByDescending { it.dueDate })
    return buildList {
        if (open.isNotEmpty()) add(AssignmentGroup("open", "Da consegnare", "✦", open))
        if (handedIn.isNotEmpty()) add(AssignmentGroup("done", "Consegnati", "❀", handedIn))
        if (expired.isNotEmpty()) add(AssignmentGroup("expired", "Scaduti", "✸", expired))
    }
}

private fun Assignment.submittedAtOrNull(): Instant? = when (val s = submissionStatus) {
    is SubmissionStatus.Submitted -> s.submittedAt
    is SubmissionStatus.Graded -> s.submittedAt
    else -> null
}

@Composable
private fun ForumsPage(
    forumsLoadable: Loadable<List<Forum>>,
    latestAnnouncement: Loadable<Discussion?>,
    onClick: (ForumId) -> Unit,
    onOpenDiscussion: (DiscussionId) -> Unit,
) {
    when (forumsLoadable) {
        Loadable.NotYetLoaded -> ListLoadingRow()
        is Loadable.Loaded -> {
            val list = forumsLoadable.value
            if (list.isEmpty()) {
                ActivityEmpty(message = "Nessun forum nel corso")
                return
            }
            // The read-only teacher board gets the hero; everything else lists below,
            // active forums first since topic forums are routinely created and left empty.
            val news = list.firstOrNull { it.type == ForumType.News }
            val others = list
                .filterNot { it.id == news?.id }
                .sortedByDescending { it.discussionCount }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (news != null) {
                    Spacer(Modifier.height(12.dp))
                    AnnouncementsCard(
                        forum = news,
                        latestAnnouncement = latestAnnouncement,
                        onOpenForum = { onClick(news.id) },
                        onOpenDiscussion = { onOpenDiscussion(it.id) },
                    )
                }
                if (others.isNotEmpty()) {
                    if (news != null) {
                        SectionTitle(title = "Spazi di discussione")
                    } else {
                        Spacer(Modifier.height(12.dp))
                    }
                    others.forEach { f ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            ForumRow(forum = f, onClick = { onClick(f.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListLoadingRow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

// The kalvidres cmId is what keys progress and what we hand to the player; it may differ
// from `module.cmId` when the host is e.g. a `mod_url` redirector linking to a kalvidres
// activity, so we carry it alongside.
private data class VideoCandidate(
    val section: it.attendance100.mybicocca.domain.model.elearning.course.CourseSection,
    val module: CourseModule,
    val videoCmId: Int,
)

private fun pickContinueWatching(
    details: CourseDetails,
    completion: Map<Int, CompletionState>,
    videoProgress: Map<Int, VideoProgress>,
    thumbnailUrl: String?,
): PickedContinueWatching? {
    val videos = details.sections
        .filter { it.visible }
        .flatMap { section ->
            section.modules.mapNotNull { module ->
                if (!module.visible) return@mapNotNull null
                val videoCmId = module.kalvidresCmIdOrNull() ?: return@mapNotNull null
                VideoCandidate(section, module, videoCmId)
            }
        }
    if (videos.isEmpty()) return null

    // Prefer the most-recently-watched-but-not-finished video, so reopening the screen
    // surfaces what the student actually paused on.
    val pick = videos
        .filter { videoProgress[it.videoCmId]?.let { p -> !p.completed } == true }
        .maxByOrNull { videoProgress.getValue(it.videoCmId).lastUpdatedAt }
        ?: videos.firstOrNull { completion[it.videoCmId]?.isCompleted != true }
        ?: videos.first()

    val subtitle = pick.section.name.takeIf { it.isNotBlank() }
        ?: "Sezione ${pick.section.sectionNumber}"
    val p = videoProgress[pick.videoCmId]
    return PickedContinueWatching(
        playable = ContinuePlayable(
            title = pick.module.name,
            subtitle = subtitle,
            progress = p?.progressFraction ?: 0f,
            elapsedLabel = p?.let { formatDurationLabel(it.positionMs) },
            totalLabel = p?.takeIf { it.durationMs > 0 }?.let { formatDurationLabel(it.durationMs) },
            thumbnailUrl = thumbnailUrl,
        ),
        module = pick.module,
        sectionId = pick.section.id,
    )
}

private fun formatDurationLabel(ms: Long): String? {
    if (ms <= 0L) return null
    val totalSec = ms / 1_000L
    val h = totalSec / 3_600L
    val m = (totalSec % 3_600L) / 60L
    val s = totalSec % 60L
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

private fun pickNextDueAssignment(
    assignments: List<Assignment>,
    now: Instant,
): Assignment? = assignments
    .filter {
        it.dueDate != null &&
            it.dueDate.isAfter(now) &&
            it.submissionStatus !is SubmissionStatus.Submitted &&
            it.submissionStatus !is SubmissionStatus.Graded
    }
    .minByOrNull { it.dueDate!! }

private fun stripTagsShort(html: String): String {
    val plain = android.text.Html
        .fromHtml(html, android.text.Html.FROM_HTML_MODE_COMPACT)
        .toString()
    val collapsed = plain
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" ")
    return if (collapsed.length <= 120) collapsed else collapsed.take(117) + "…"
}

private const val PULL_INDICATOR_DISMISS_DELAY_MS = 350L
// List index of the "tab_bar" item (hero is 0); scrolled to when a swipe lands while pinned.
private const val TAB_BAR_ITEM_INDEX = 1
// How much of the bar's final travel is spent morphing buttons into tab indicators.
private val TAB_BAR_MORPH_DISTANCE = 80.dp
