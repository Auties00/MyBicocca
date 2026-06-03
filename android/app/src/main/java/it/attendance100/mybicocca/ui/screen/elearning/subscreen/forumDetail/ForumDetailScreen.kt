package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.component.shimmer.ShimmerBox
import it.attendance100.mybicocca.ui.component.shimmer.ShimmerCircle
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail.component.DiscussionRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail.component.ForumHeader
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail.state.ForumDetailOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail.subscreen.newDiscussion.NewDiscussionSheet
import it.attendance100.mybicocca.ui.screen.elearning.theme.CourseDetailTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumDetailScreen(
    forumId: Int,
    courseId: Int,
    onOpenDiscussion: (DiscussionId) -> Unit = {},
    viewModel: ForumDetailViewModel = hiltViewModel(),
) {
    val forumLoadable by viewModel.forum.collectAsStateWithLifecycle()
    val discussionsLoadable by viewModel.discussions.collectAsStateWithLifecycle()
    val pageState by viewModel.pageState.collectAsStateWithLifecycle()
    val initialFetchInProgress by viewModel.initialFetchInProgress.collectAsStateWithLifecycle()
    val creatingDiscussion by viewModel.creatingDiscussion.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    var showNewDiscussion by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is ForumDetailOneShotEvent.OpenDiscussion -> onOpenDiscussion(event.id)
                is ForumDetailOneShotEvent.DiscussionCreated -> {
                    showNewDiscussion = false
                    onOpenDiscussion(event.id)
                }
                is ForumDetailOneShotEvent.RefreshFailed ->
                    snackbar.showError("Sincronizzazione del forum non riuscita", event.cause)
            }
        }
    }

    CourseDetailTheme(courseId = remember(courseId) { CourseId(courseId) }) {
        val scheme = MaterialTheme.colorScheme
        val pullState = rememberPullToRefreshState()
        val listState = rememberLazyListState()
        val pullScope = rememberCoroutineScope()
        var pullIndicatorVisible by remember { mutableStateOf(false) }

        // Ask for the next page a few rows before the end so the spinner rarely shows.
        LaunchedEffect(listState) {
            snapshotFlow {
                val info = listState.layoutInfo
                val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
                last >= info.totalItemsCount - LOAD_MORE_LOOKAHEAD
            }
                .distinctUntilChanged()
                .collect { nearEnd -> if (nearEnd) viewModel.loadMore() }
        }

        val forum = forumLoadable.valueOrNull()
        val discussions = discussionsLoadable.valueOrNull()
        val showSkeleton = initialFetchInProgress && discussions.isNullOrEmpty()
        val showEmpty = !showSkeleton && discussions != null && discussions.isEmpty() &&
            discussionsLoadable is Loadable.Loaded

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
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (forum != null) {
                        item("header") { ForumHeader(forum = forum) }
                    }
                    when {
                        showSkeleton -> skeletonItems()
                        showEmpty -> item("empty") {
                            Box(modifier = Modifier.fillParentMaxHeight(0.65f)) {
                                EmptyState(
                                    icon = Icons.Outlined.Forum,
                                    title = "Nessuna discussione",
                                    body = if (forum?.canCreateDiscussions == true) {
                                        "Apri tu la prima discussione di questo forum"
                                    } else {
                                        "Quando i docenti pubblicheranno qualcosa lo troverai qui"
                                    },
                                )
                            }
                        }
                        discussions != null -> discussionItems(
                            discussions = discussions,
                            onOpen = viewModel::openDiscussion,
                        )
                    }
                    if (pageState.isLoadingMore) {
                        item("loading_more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            }
                        }
                    } else if (!pageState.hasMore && !discussions.isNullOrEmpty()) {
                        item("end_mark") {
                            Text(
                                text = "✦",
                                color = scheme.outlineVariant,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        }
                    }
                }
            }

            if (forum?.canCreateDiscussions == true) {
                // Collapse to icon-only once the user is reading down the list.
                val fabExpanded by remember(listState) {
                    derivedStateOf {
                        listState.firstVisibleItemIndex == 0 &&
                            listState.firstVisibleItemScrollOffset < 50
                    }
                }
                ExtendedFloatingActionButton(
                    onClick = { showNewDiscussion = true },
                    expanded = fabExpanded,
                    icon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    text = { Text("Nuova discussione", fontWeight = FontWeight.Bold) },
                    containerColor = scheme.primaryContainer,
                    contentColor = scheme.onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(16.dp),
                )
            }
        }

        if (showNewDiscussion && forum != null) {
            NewDiscussionSheet(
                forumName = forum.name,
                creating = creatingDiscussion,
                onSubmit = viewModel::onCreateDiscussion,
                onDismiss = { if (!creatingDiscussion) showNewDiscussion = false },
            )
        }
    }
}

private fun LazyListScope.discussionItems(
    discussions: List<Discussion>,
    onOpen: (Discussion) -> Unit,
) {
    // Room returns them in refresh order; pin a stable presentation regardless.
    val pinned = discussions.filter { it.isPinned }
        .sortedByDescending { it.timeModified ?: it.createdAt }
    val others = discussions.filterNot { it.isPinned }
        .sortedByDescending { it.timeModified ?: it.createdAt }

    if (pinned.isNotEmpty()) {
        item("pinned_section") { SectionLabel(title = "In evidenza") }
        items(items = pinned, key = { "p${it.id.value}" }) { d ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                DiscussionRow(discussion = d, onClick = { onOpen(d) })
            }
        }
    }
    if (others.isNotEmpty()) {
        if (pinned.isNotEmpty()) {
            item("discussions_section") { SectionLabel(title = "Discussioni") }
        } else {
            item("discussions_pad") { Spacer(Modifier.height(8.dp)) }
        }
        items(items = others, key = { it.id.value }) { d ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                DiscussionRow(discussion = d, onClick = { onOpen(d) })
            }
        }
    }
}

private fun LazyListScope.skeletonItems() {
    item("skeleton") {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            repeat(5) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ShimmerCircle(size = 42.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp))
                        Spacer(Modifier.height(8.dp))
                        ShimmerBox(modifier = Modifier.fillMaxWidth(0.95f).height(12.dp))
                        Spacer(Modifier.height(6.dp))
                        ShimmerBox(modifier = Modifier.width(110.dp).height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = "✿ ${title.uppercase()}",
        color = MaterialTheme.colorScheme.tertiary,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.4.sp,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 18.dp, bottom = 10.dp),
    )
}

private const val LOAD_MORE_LOOKAHEAD = 4
private const val PULL_INDICATOR_DISMISS_DELAY_MS = 350L
