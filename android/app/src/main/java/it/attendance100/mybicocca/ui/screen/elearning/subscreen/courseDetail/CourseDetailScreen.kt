package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.domain.model.elearning.video.VideoProgress
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.ActivityEmpty
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.AssignmentRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.CourseHero
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.CourseTabBar
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.ForumRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.SectionsList
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.SyllabusContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component.UpNextCard
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.ContinuePlayable
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.CourseDetailOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.CourseTab
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.PickedContinueWatching
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.UpNextItem
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.theme.CourseDetailTheme
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.theme.heroShapesFor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
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
    onProvideTitle: (String?) -> Unit = {},
    onProvideActions: ((@Composable () -> Unit)?) -> Unit = {},
    onOpenAssignment: (AssignmentId) -> Unit = {},
    onOpenQuiz: (QuizId) -> Unit = {},
    onOpenForum: (ForumId) -> Unit = {},
    onOpenResource: (String) -> Unit = {},
    onOpenVideo: (cmId: Int, title: String) -> Unit = { _, _ -> },
    viewModel: CourseDetailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val courseIdValue = remember(courseId) { CourseId(courseId) }
    val heroShapes = remember(courseIdValue) { heroShapesFor(courseIdValue) }
    val detailsLoadable by viewModel.details.collectAsStateWithLifecycle()
    val assignmentsLoadable by viewModel.assignments.collectAsStateWithLifecycle()
    val forumsLoadable by viewModel.forums.collectAsStateWithLifecycle()
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
    // 2dp seam between the inline copy and the overlay at the swap moment.
    val tabBarPinned by remember(listState) {
        derivedStateOf {
            val tabBar = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == "tab_bar" }
            if (tabBar != null) tabBar.offset <= 0 else listState.firstVisibleItemIndex > 1
        }
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
                contentPadding = PaddingValues(bottom = 16.dp),
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
                            selected = selectedTab,
                            onSelect = viewModel::selectTab,
                            pinned = tabBarPinned,
                            modifier = Modifier.animateItem(),
                        )
                    }

                    when (selectedTab) {
                        CourseTab.Syllabus -> syllabusItems(details)
                        CourseTab.Content -> contentItems(
                            details = details,
                            expanded = expanded,
                            completion = completion,
                            onToggleSection = viewModel::toggleSection,
                            onModuleClick = { openModule(it, viewModel) },
                        )
                        CourseTab.Assignments -> assignmentsItems(
                            assignmentsLoadable = assignmentsLoadable,
                            onClick = { viewModel.emitOpenAssignment(it.value) },
                        )
                        CourseTab.Forum -> forumItems(
                            forumsLoadable = forumsLoadable,
                            onClick = { viewModel.emitOpenForum(it.value) },
                        )
                    }
                }
            }
        }

        // Sits outside the LazyColumn so the list's overscroll doesn't deform it.
        if (tabBarPinned) {
            CourseTabBar(
                selected = selectedTab,
                onSelect = viewModel::selectTab,
                pinned = true,
                modifier = Modifier.align(Alignment.TopStart),
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

private fun LazyListScope.syllabusItems(details: CourseDetails) {
    item("syllabus") {
        SyllabusContent(details = details)
    }
}

private fun LazyListScope.contentItems(
    details: CourseDetails,
    expanded: Set<Int>,
    completion: Map<Int, CompletionState>,
    onToggleSection: (Int) -> Unit,
    onModuleClick: (CourseModule) -> Unit,
) {
    if (details.sections.isEmpty()) {
        item("content_empty") { ActivityEmpty(message = "Nessuna sezione disponibile") }
    } else {
        item("content_list") {
            Spacer(Modifier.height(12.dp))
            SectionsList(
                sections = details.sections,
                expanded = expanded,
                completion = completion,
                onToggleSection = onToggleSection,
                onModuleClick = onModuleClick,
            )
        }
    }
}

private fun LazyListScope.assignmentsItems(
    assignmentsLoadable: Loadable<List<Assignment>>,
    onClick: (AssignmentId) -> Unit,
) {
    val list = assignmentsLoadable.valueOrNull()
    val now = Instant.now()
    val next = list?.let { pickNextDueAssignment(it, now) }
    if (next != null) {
        item("assignments_up_next") {
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
    }
    when (assignmentsLoadable) {
        Loadable.NotYetLoaded -> item("assignments_loading") { ListLoadingRow() }
        is Loadable.Loaded -> {
            val values = assignmentsLoadable.value
            if (values.isEmpty()) {
                item("assignments_empty") {
                    ActivityEmpty(message = "Nessun compito assegnato")
                }
            } else {
                item("assignments_pad_top") { Spacer(Modifier.height(12.dp)) }
                items(items = values, key = { it.id.value }) { a ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        AssignmentRow(assignment = a, onClick = { onClick(a.id) })
                    }
                }
            }
        }
    }
}

private fun LazyListScope.forumItems(
    forumsLoadable: Loadable<List<Forum>>,
    onClick: (ForumId) -> Unit,
) {
    when (forumsLoadable) {
        Loadable.NotYetLoaded -> item("forum_loading") { ListLoadingRow() }
        is Loadable.Loaded -> {
            val list = forumsLoadable.value
            if (list.isEmpty()) {
                item("forum_empty") {
                    ActivityEmpty(message = "Nessun forum nel corso")
                }
            } else {
                item("forum_pad_top") { Spacer(Modifier.height(12.dp)) }
                items(items = list, key = { it.id.value }) { f ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        ForumRow(forum = f, onClick = { onClick(f.id) })
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
