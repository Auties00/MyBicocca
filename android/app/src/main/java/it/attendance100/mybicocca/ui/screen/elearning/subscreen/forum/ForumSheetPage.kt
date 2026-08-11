package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.Post
import it.attendance100.mybicocca.domain.model.elearning.forum.PostAttachment
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.modal.SheetConfirmPage
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetOutcome
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.SheetResultPage
import it.attendance100.mybicocca.ui.component.modal.sheetBodyGestureBarrier
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.scene.LocalSheetDismissControl
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.component.DiscussionRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.component.ForumComposer
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.component.ThreadPostItem
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.ComposerTarget
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.ForumSheetEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.PendingAttachment
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.ThreadNode
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.visibleThread
import it.attendance100.mybicocca.ui.screen.elearning.theme.CourseDetailTheme
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.time.Duration.Companion.milliseconds

/**
 * The whole course forum in one modal sheet entry, driving an internal page machine over a single
 * morphing header: discussions list (0) -> thread (1) -> composer (2) -> confirm/result. The list
 * and thread are observed from Room; the composer (new discussion / reply / edit) handles its own
 * text + attachments and submits via the ViewModel. A failed action morphs onto an in-sheet
 * result page, and system back walks the internal pages up one level.
 *
 * The result and confirmation pages are composable-local overlays layered on top of the
 * ViewModel's list/thread/composer state, as is the per-discussion set of collapsed sub-threads.
 * The last opened discussion and its thread are retained so the outgoing thread page keeps its
 * content during the back transition (closing clears the live state immediately, which would
 * otherwise blank the header and jump the sheet height mid-animation). Composer text lives at
 * page scope so both submit and the discard veto can read it, resetting whenever the composer
 * target changes; while the composer holds a draft (or a submit is running) swipe and scrim
 * dismissal are vetoed and morph into the discard confirmation instead of dropping the draft.
 */
@Composable
fun ForumSheetPage(
    forumId: Int,
    courseId: Int,
    initialDiscussionId: Int?,
    onOpenFile: (fileName: String, fileUrl: String?, mimeType: String?, sizeBytes: Long?) -> Unit,
    viewModel: ForumSheetViewModel = hiltViewModel<ForumSheetViewModel, ForumSheetViewModel.Factory>(
        key = "forum-sheet-$forumId-${initialDiscussionId ?: 0}",
        creationCallback = {
            it.create(SheetRoute.Forum(forumId = forumId, courseId = courseId, initialDiscussionId = initialDiscussionId))
        },
    ),
) {
    val context = LocalContext.current

    val forumLoadable by viewModel.forum.collectAsStateWithLifecycle()
    val discussionsLoadable by viewModel.discussions.collectAsStateWithLifecycle()
    val pageState by viewModel.pageState.collectAsStateWithLifecycle()
    val initialFetchInProgress by viewModel.initialFetchInProgress.collectAsStateWithLifecycle()
    val openDiscussionId by viewModel.openDiscussionId.collectAsStateWithLifecycle()
    val postsLoadable by viewModel.posts.collectAsStateWithLifecycle()
    val thread by viewModel.thread.collectAsStateWithLifecycle()
    val composerTarget by viewModel.composerTarget.collectAsStateWithLifecycle()
    val pendingAttachments by viewModel.pendingAttachments.collectAsStateWithLifecycle()
    val composerGroups by viewModel.composerGroups.collectAsStateWithLifecycle()
    val selectedGroupId by viewModel.selectedGroupId.collectAsStateWithLifecycle()
    val submitting by viewModel.submitting.collectAsStateWithLifecycle()

    var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
    var pendingDeletePostId by remember { mutableStateOf<Int?>(null) }
    var confirmDiscard by remember { mutableStateOf(false) }
    var collapsedIds by remember(openDiscussionId) { mutableStateOf<Set<Int>>(emptySet()) }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is ForumSheetEvent.Failed -> outcome =
                    SheetOutcome.Error(event.title.asString(context), event.cause)

                is ForumSheetEvent.Info -> outcome =
                    SheetOutcome.Info(event.title.asString(context))
            }
        }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) viewModel.addAttachment(queryAttachment(context, uri))
    }

    CourseDetailTheme(courseId = remember(courseId) { CourseId(courseId) }) {
        val forum = forumLoadable.valueOrNull()
        val discussions = discussionsLoadable.valueOrNull()
        val openDiscussion = remember(discussions, openDiscussionId) {
            discussions?.firstOrNull { it.id == openDiscussionId }
        }
        var lastDiscussion by remember { mutableStateOf(openDiscussion) }
        if (openDiscussion != null) lastDiscussion = openDiscussion
        var subject by rememberSaveable(composerTarget) {
            mutableStateOf((composerTarget as? ComposerTarget.Edit)?.subject
                ?: (composerTarget as? ComposerTarget.Reply)?.replySubject
                ?: "")
        }
        var message by rememberSaveable(composerTarget) {
            mutableStateOf((composerTarget as? ComposerTarget.Edit)?.initialMessage ?: "")
        }

        val display = when {
            outcome != null -> Display.Result
            pendingDeletePostId != null -> Display.ConfirmDelete
            confirmDiscard -> Display.ConfirmDiscard
            composerTarget != null -> Display.Composer
            openDiscussionId != null -> Display.Thread
            else -> Display.List
        }

        val control = LocalSheetDismissControl.current
        val composerOpen = rememberUpdatedState(composerTarget != null)
        val busy = rememberUpdatedState(submitting)
        val onConfirmDismiss = remember {
            {
                when {
                    busy.value -> false
                    composerOpen.value -> { confirmDiscard = true; false }
                    else -> true
                }
            }
        }
        SideEffect {
            control?.gesturesEnabled = !busy.value
            control?.confirmDismiss = onConfirmDismiss
        }

        val seekableState =
            remember { androidx.compose.animation.core.SeekableTransitionState(display) }
        val transition = androidx.compose.animation.core.rememberTransition(
            seekableState,
            label = "forum_sheet_pages"
        )

        LaunchedEffect(display) {
            if (seekableState.targetState != display) {
                seekableState.animateTo(display)
            }
        }

        androidx.activity.compose.PredictiveBackHandler(enabled = display != Display.List) { progress ->
            try {
                val fallback = when (display) {
                    Display.Result -> if (pendingDeletePostId != null) Display.ConfirmDelete else if (confirmDiscard) Display.ConfirmDiscard else if (composerTarget != null) Display.Composer else if (openDiscussionId != null) Display.Thread else Display.List
                    Display.ConfirmDelete -> if (confirmDiscard) Display.ConfirmDiscard else if (composerTarget != null) Display.Composer else if (openDiscussionId != null) Display.Thread else Display.List
                    Display.ConfirmDiscard -> if (composerTarget != null) Display.Composer else if (openDiscussionId != null) Display.Thread else Display.List
                    Display.Composer -> if (subject.isNotBlank() || message.isNotBlank()) Display.ConfirmDiscard else if (openDiscussionId != null) Display.Thread else Display.List
                    Display.Thread -> Display.List
                    Display.List -> display
                }
                progress.collect { event ->
                    seekableState.seekTo(event.progress, targetState = fallback)
                }
                seekableState.animateTo(fallback)
                when (display) {
                    Display.Result -> outcome = null
                    Display.ConfirmDelete -> pendingDeletePostId = null
                    Display.ConfirmDiscard -> confirmDiscard = false
                    Display.Composer -> if (subject.isNotBlank() || message.isNotBlank()) confirmDiscard =
                        true else viewModel.cancelComposer()

                    Display.Thread -> viewModel.closeThread()
                    Display.List -> Unit
                }
            } catch (_: kotlinx.coroutines.CancellationException) {
                seekableState.animateTo(display)
            }
        }

        val composerBack = {
            if (subject.isNotBlank() || message.isNotBlank()) confirmDiscard = true else viewModel.cancelComposer()
        }

        Column(modifier = Modifier.testTag(ForumSheetTestTags.ROOT)) {
            SheetPagerHeader(
                depth = display.depth,
                title = when (display) {
                    Display.List -> forum?.name ?: stringResource(R.string.elearning_course_generic_forum)
                    Display.Thread -> openDiscussion?.subject ?: stringResource(R.string.elearning_forum_discussion_fallback)
                    Display.Composer -> when (composerTarget) {
                        is ComposerTarget.NewDiscussion -> stringResource(R.string.elearning_forum_new_discussion)
                        is ComposerTarget.Edit -> stringResource(R.string.elearning_forum_edit_message_title)
                        else -> stringResource(R.string.elearning_forum_reply)
                    }
                    Display.ConfirmDelete -> stringResource(R.string.elearning_forum_delete_confirm_title)
                    Display.ConfirmDiscard -> stringResource(R.string.elearning_forum_discard_confirm_title)
                    Display.Result -> ""
                },
                subtitle = when (display) {
                    Display.List -> forum?.let { discussionsSubtitle(it, discussions) }
                    Display.Thread -> openDiscussion?.let { threadSubtitle(it) }
                    else -> null
                },
                onBack = when (display) {
                    Display.Thread -> viewModel::closeThread
                    Display.Composer -> composerBack
                    Display.ConfirmDelete -> ({ pendingDeletePostId = null })
                    Display.ConfirmDiscard -> ({ confirmDiscard = false })
                    Display.Result -> ({ outcome = null })
                    Display.List -> null
                },
            )

            transition.AnimatedContent(
                modifier = Modifier.sheetBodyGestureBarrier(),
                transitionSpec = { sheetPageTransform(forward = targetState.depth >= initialState.depth) },
                contentKey = { it.key },
            ) { target ->
                when (target) {
                    Display.List -> DiscussionsListBody(
                        forum = forum,
                        discussionsLoadable = discussionsLoadable,
                        initialFetchInProgress = initialFetchInProgress,
                        isLoadingMore = pageState.isLoadingMore,
                        hasMore = pageState.hasMore,
                        onLoadMore = viewModel::loadMore,
                        onOpenDiscussion = { viewModel.openThread(it.id) },
                        onNewDiscussion = viewModel::startNewDiscussion,
                    )

                    Display.Thread -> ThreadBody(
                        discussion = openDiscussion ?: lastDiscussion,
                        thread = thread,
                        collapsedIds = collapsedIds,
                        onToggleCollapse = { id ->
                            collapsedIds = if (id in collapsedIds) collapsedIds - id else collapsedIds + id
                        },
                        onReplyToDiscussion = {
                            postsLoadable.valueOrNull()?.firstOrNull { it.parentId == null }?.let(viewModel::startReply)
                        },
                        onReplyToPost = viewModel::startReply,
                        onEditPost = viewModel::startEdit,
                        onDeletePost = { pendingDeletePostId = it.id.value },
                        onSubscribe = { openDiscussionId?.let { viewModel.setSubscribed(it, true) } },
                        onOpenAttachment = { att -> onOpenFile(att.fileName, att.fileUrl, att.mimeType, att.sizeBytes) },
                    )

                    Display.Composer -> composerTarget?.let { target ->
                        ForumComposer(
                            modifier = Modifier.testTag(ForumSheetTestTags.COMPOSER),
                            target = target,
                            subject = subject,
                            onSubjectChange = { subject = it },
                            message = message,
                            onMessageChange = { message = it },
                            attachments = pendingAttachments,
                            onAddAttachment = { filePicker.launch(arrayOf("*/*")) },
                            onRemoveAttachment = viewModel::removeAttachment,
                            groups = composerGroups,
                            selectedGroupId = selectedGroupId,
                            onSelectGroup = viewModel::selectGroup,
                            submitting = submitting,
                            onSubmit = { viewModel.submitComposer(subject, message) },
                        )
                    }

                    Display.ConfirmDelete -> SheetConfirmPage(
                        body = stringResource(R.string.elearning_forum_delete_confirm_body),
                        onConfirm = {
                            val id = pendingDeletePostId
                            pendingDeletePostId = null
                            postsLoadable.valueOrNull()?.firstOrNull { it.id.value == id }?.let(viewModel::deletePost)
                        },
                        onKeep = { pendingDeletePostId = null },
                        confirmLabel = stringResource(R.string.elearning_forum_delete),
                    )

                    Display.ConfirmDiscard -> SheetConfirmPage(
                        body = stringResource(R.string.elearning_forum_discard_confirm_body),
                        onConfirm = {
                            confirmDiscard = false
                            viewModel.cancelComposer()
                        },
                        onKeep = { confirmDiscard = false },
                        confirmIsPrimary = true,
                    )

                    Display.Result -> outcome?.let { current ->
                        SheetResultPage(outcome = current, onDismiss = { outcome = null })
                    }
                }
            }
        }
    }
}

/**
 * The sheet's internal pages; [depth] orders them so the page transition knows whether it is
 * moving forward (deeper) or back, with both confirmations sharing a level and the result page
 * always entering forward.
 */
private enum class Display(val depth: Int, val key: String) {
    List(0, "list"),
    Thread(1, "thread"),
    Composer(2, "composer"),
    ConfirmDelete(3, "confirm_delete"),
    ConfirmDiscard(3, "confirm_discard"),
    Result(9, "result"),
}

/**
 * The discussions list page: pinned threads first, then the rest by latest activity, with
 * infinite scroll that requests the next page as the list nears its end. A cold cache shows a
 * centred spinner, an empty forum a friendly empty state, and a brand "Nuova discussione" button
 * sits under the list when the forum allows starting threads.
 */
@Composable
private fun DiscussionsListBody(
    forum: Forum?,
    discussionsLoadable: Loadable<List<Discussion>>,
    initialFetchInProgress: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onOpenDiscussion: (Discussion) -> Unit,
    onNewDiscussion: () -> Unit,
) {
    val listState = rememberLazyListState()
    val discussions = discussionsLoadable.valueOrNull()
    val showSkeleton = initialFetchInProgress && discussions.isNullOrEmpty()
    val showEmpty = !showSkeleton && discussions != null && discussions.isEmpty() &&
        discussionsLoadable is Loadable.Loaded

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= info.totalItemsCount - 3
        }.distinctUntilChanged().collect { nearEnd -> if (nearEnd && hasMore) onLoadMore() }
    }

    val pinned = remember(discussions) {
        discussions.orEmpty().filter { it.isPinned }.sortedByDescending { it.timeModified ?: it.createdAt }
    }
    val others = remember(discussions) {
        discussions.orEmpty().filterNot { it.isPinned }.sortedByDescending { it.timeModified ?: it.createdAt }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .heightIn(max = 600.dp)
                .testTag(ForumSheetTestTags.DISCUSSIONS_LIST),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            when {
                showSkeleton -> item("loading") {
                    ListLoading(modifier = Modifier.testTag(ForumSheetTestTags.STATE_LOADING))
                }
                showEmpty -> item("empty") {
                    Box(Modifier
                        .fillParentMaxHeight(0.55f)
                        .testTag(ForumSheetTestTags.STATE_EMPTY)) {
                        EmptyState(
                            icon = Icons.Outlined.Forum,
                            title = stringResource(R.string.elearning_forum_empty_title),
                            body = if (forum?.canCreateDiscussions == true) stringResource(R.string.elearning_forum_empty_can_create)
                            else stringResource(R.string.elearning_forum_empty_readonly),
                        )
                    }
                }
                else -> {
                    items(pinned, key = { "p${it.id.value}" }) { d ->
                        Box(
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .testTag(ForumSheetTestTags.discussion(d.id.value)),
                        ) {
                            val haptic = rememberHapticManager()
                            DiscussionRow(
                                discussion = d,
                                onClick = { haptic.tap(); onOpenDiscussion(d) })
                        }
                    }
                    items(others, key = { it.id.value }) { d ->
                        Box(
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .testTag(ForumSheetTestTags.discussion(d.id.value)),
                        ) {
                            val haptic = rememberHapticManager()
                            DiscussionRow(
                                discussion = d,
                                onClick = { haptic.tap(); onOpenDiscussion(d) })
                        }
                    }
                    if (isLoadingMore) {
                        item("loading_more") {
                            Box(Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(26.dp))
                            }
                        }
                    }
                }
            }
        }
        if (forum?.canCreateDiscussions == true) {
            val haptic = rememberHapticManager()
            NewDiscussionButton(
                onClick = { haptic.tap(); onNewDiscussion() },
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
                    .testTag(ForumSheetTestTags.NEW_DISCUSSION_ACTION),
            )
        }
    }
}

/**
 * The thread page: the discussion's posts as a collapsible indented tree, with the reply /
 * subscribe footer pinned underneath. Closing the thread empties the live post list at once, so
 * the last loaded thread is kept (per discussion) and shown on the outgoing page — otherwise the
 * back transition would momentarily collapse to the loading indicator, jumping the sheet height
 * and snapping the header.
 */
@Composable
private fun ThreadBody(
    discussion: Discussion?,
    thread: List<ThreadNode>,
    collapsedIds: Set<Int>,
    onToggleCollapse: (Int) -> Unit,
    onReplyToDiscussion: () -> Unit,
    onReplyToPost: (Post) -> Unit,
    onEditPost: (Post) -> Unit,
    onDeletePost: (Post) -> Unit,
    onSubscribe: () -> Unit,
    onOpenAttachment: (PostAttachment) -> Unit,
) {
    val canReply = discussion?.canReply == true
    val discussionKey = discussion?.id?.value
    var lastThread by remember(discussionKey) { mutableStateOf(thread) }
    if (thread.isNotEmpty()) lastThread = thread
    val shown = if (thread.isNotEmpty()) thread else lastThread

    val loading = rememberThreadLoading(hasContent = shown.isNotEmpty(), key = discussionKey)
    val visible = remember(shown, collapsedIds) { visibleThread(shown, collapsedIds) }
    val haptic = rememberHapticManager()
    Column(modifier = Modifier.fillMaxWidth()) {
        if (loading) {
            SheetLoadingIndicator(label = stringResource(R.string.elearning_forum_thread_loading))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 560.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(visible, key = { it.post.id.value }) { node ->
                    ThreadPostItem(
                        node = node,
                        collapsed = node.post.id.value in collapsedIds,
                        onToggleCollapse = { haptic.tap(); onToggleCollapse(node.post.id.value) },
                        onReply = { haptic.tap(); onReplyToPost(node.post) },
                        onEdit = { haptic.tap(); onEditPost(node.post) },
                        onDelete = { haptic.tap(); onDeletePost(node.post) },
                        onOpenAttachment = { haptic.tap(); onOpenAttachment(it) },
                    )
                }
            }
        }
        ThreadFooter(
            canReply = canReply,
            onReply = { haptic.tap(); onReplyToDiscussion() },
            onSubscribe = { haptic.tap(); onSubscribe() })
    }
}

/**
 * Bottom actions: replying is the brand primary, following the discussion the neutral secondary,
 * joined as one connected button group. When the discussion is read-only only Segui shows, full
 * width. Both actions disable while offline.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThreadFooter(canReply: Boolean, onReply: () -> Unit, onSubscribe: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else Color.White
    val isOnline = LocalIsOnline.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (canReply) {
            FilledTonalButton(
                onClick = onSubscribe,
                enabled = isOnline,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHighest,
                    contentColor = scheme.onSurface,
                ),
            ) {
                Icon(Icons.Outlined.NotificationsActive, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.elearning_forum_subscribe_short), fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onReply,
                enabled = isOnline,
                modifier = Modifier
                    .weight(1.4f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = brandBg, contentColor = brandFg),
            ) {
                Icon(Icons.AutoMirrored.Outlined.Reply, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.elearning_forum_reply), fontWeight = FontWeight.SemiBold)
            }
        } else {
            FilledTonalButton(
                onClick = onSubscribe,
                enabled = isOnline,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHighest,
                    contentColor = scheme.onSurface,
                ),
            ) {
                Icon(Icons.Outlined.NotificationsActive, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.elearning_forum_subscribe), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun NewDiscussionButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else Color.White
    Button(
        onClick = onClick,
        enabled = LocalIsOnline.current,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = brandBg, contentColor = brandFg),
    ) {
        Icon(Icons.AutoMirrored.Outlined.Reply, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.elearning_forum_new_discussion), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ListLoading(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Whether to show the thread loading indicator. A short grace lets a cached thread render with no
 * spinner at all (instant from memory); on a cache miss the spinner is held for a minimum so it
 * never flashes in and straight back out.
 */
@Composable
private fun rememberThreadLoading(hasContent: Boolean, key: Any?): Boolean {
    val latestHasContent = rememberUpdatedState(hasContent)
    var visible by remember(key) { mutableStateOf(false) }
    var minDone by remember(key) { mutableStateOf(true) }
    LaunchedEffect(key) {
        visible = false
        minDone = true
        delay(LOADING_GRACE_MS.milliseconds)
        if (!latestHasContent.value) {
            visible = true
            minDone = false
            delay(LOADING_MIN_MS.milliseconds)
            minDone = true
        }
    }
    return visible && (!minDone || !latestHasContent.value)
}

private const val LOADING_GRACE_MS = 120L
private const val LOADING_MIN_MS = 600L

@Composable
private fun discussionsSubtitle(forum: Forum, discussions: List<Discussion>?): String {
    val count = discussions?.size ?: forum.discussionCount
    return if (count == 0) stringResource(R.string.elearning_forum_empty_title)
    else pluralStringResource(R.plurals.elearning_forum_discussion_count, count, count)
}

@Composable
private fun threadSubtitle(discussion: Discussion): String {
    val replies = discussion.replyCount
    return if (replies == 0) discussion.authorName
    else pluralStringResource(R.plurals.elearning_forum_thread_replies, replies, discussion.authorName, replies)
}

private fun queryAttachment(context: android.content.Context, uri: Uri): PendingAttachment {
    var name = context.getString(R.string.elearning_forum_attachment_fallback_name)
    var size: Long? = null
    runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex >= 0) cursor.getString(nameIndex)?.let { name = it }
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
            }
        }
    }
    val mime = context.contentResolver.getType(uri)
    return PendingAttachment(uri = uri, fileName = name, mimeType = mime, sizeBytes = size)
}
