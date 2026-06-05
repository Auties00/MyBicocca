package it.attendance100.mybicocca.ui.screen.registry.subscreen.internships

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.internship.InternshipApplication
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationState
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.ext.detailLine
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.ext.displayName
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.ext.stateLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.state.InternshipEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.subscreen.applicationDetail.InternshipApplicationDetailSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InternshipsScreen(
    onOpenSaved: () -> Unit = {},
    viewModel: InternshipsViewModel = hiltViewModel(),
) {
    val data by viewModel.applications.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val selectedApplication by viewModel.selectedApplication.collectAsStateWithLifecycle()
    val applicationDetail by viewModel.applicationDetail.collectAsStateWithLifecycle()
    val attachments by viewModel.attachments.collectAsStateWithLifecycle()
    val detailStatus by viewModel.detailStatus.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()
    val pullState = rememberPullToRefreshState()
    var pullIndicatorVisible by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is InternshipEvent.ShowMessage -> scope.launch { snackbar.showInfo(event.message) }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    PullToRefreshBox(
        isRefreshing = pullIndicatorVisible,
        onRefresh = {
            pullIndicatorVisible = true
            viewModel.pullToRefresh()
            scope.launch {
                delay(PULL_INDICATOR_DISMISS_DELAY_MS)
                pullIndicatorVisible = false
            }
        },
        state = pullState,
        modifier = Modifier.fillMaxSize(),
    ) {
        when (val snapshot = data) {
            Loadable.NotYetLoaded -> when (val status = syncStatus) {
                is SyncStatus.Failed -> RefreshableEmpty {
                    ErrorEmptyState(cause = status.cause, onRetry = viewModel::refresh)
                }

                else -> RefreshableEmpty {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator(modifier = Modifier.size(72.dp))
                    }
                }
            }

            is Loadable.Loaded -> {
                val failure = syncStatus as? SyncStatus.Failed
                when {
                    failure != null && snapshot.value.isEmpty() -> RefreshableEmpty {
                        ErrorEmptyState(cause = failure.cause, onRetry = viewModel::refresh)
                    }

                    snapshot.value.isEmpty() -> RefreshableEmpty {
                        EmptyState(
                            icon = Icons.Outlined.Work,
                            title = "Nessun tirocinio",
                            body = "Quando presenterai una domanda di tirocinio o stage la troverai qui.",
                        )
                    }

                    else -> InternshipsContent(
                        applications = snapshot.value,
                        onOpenApplication = viewModel::openApplication,
                    )
                }
            }
        }
        }

        ExtendedFloatingActionButton(
            text = { Text("Salvati") },
            icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = null) },
            onClick = onOpenSaved,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }

    selectedApplication?.let { application ->
        InternshipApplicationDetailSheet(
            application = application,
            detail = applicationDetail,
            attachments = attachments,
            detailStatus = detailStatus,
            onRetry = viewModel::retryDetail,
            onWithdraw = { viewModel.withdrawApplication(application) },
            onRegisterHours = { viewModel.registerHours(application) },
            onSignProject = { viewModel.signTrainingProject(application) },
            onUpload = { viewModel.uploadAttachment(application) },
            onDismiss = viewModel::dismissApplication,
        )
    }
}

@Composable
private fun InternshipsContent(
    applications: List<InternshipApplication>,
    onOpenApplication: (InternshipApplication) -> Unit,
) {
    val ongoing = applications.filter { it.state == InternshipApplicationState.Started }
    val pending = applications.filter { it.state.pending }
    val closed = applications.filter { it.state == InternshipApplicationState.Closed }
    val discarded = applications.filterNot {
        it.state == InternshipApplicationState.Started || it.state.pending ||
            it.state == InternshipApplicationState.Closed
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 32.dp),
    ) {
        var firstGroup = true
        listOf(
            GroupSpec("ongoing", "In corso", ongoingCaption(ongoing.size), ongoing),
            GroupSpec("pending", "Domande presentate", pendingCaption(pending.size), pending),
            GroupSpec("closed", "Conclusi", "Tirocini e stage terminati", closed),
            GroupSpec("discarded", "Annullate e rifiutate", "Domande non andate a buon fine", discarded),
        ).forEach { group ->
            if (group.items.isEmpty()) return@forEach
            if (!firstGroup) {
                item(key = "${group.keyPrefix}-gap") { Spacer(Modifier.height(20.dp)) }
            }
            firstGroup = false
            segmentedGroup(
                keyPrefix = group.keyPrefix,
                title = group.title,
                caption = group.caption,
                items = group.items,
                onOpenApplication = onOpenApplication,
            )
        }
    }
}

private data class GroupSpec(
    val keyPrefix: String,
    val title: String,
    val caption: String,
    val items: List<InternshipApplication>,
)

private fun ongoingCaption(count: Int): String = when (count) {
    1 -> "1 tirocinio attivo"
    else -> "$count tirocini attivi"
}

private fun pendingCaption(count: Int): String = when (count) {
    1 -> "1 domanda in attesa di avvio"
    else -> "$count domande in attesa di avvio"
}

// A connected segmented card in the registry's expressive style: a header tile followed
// by one tile per application, 2.dp gaps, large radii only on the group's outer corners.
private fun LazyListScope.segmentedGroup(
    keyPrefix: String,
    title: String,
    caption: String,
    items: List<InternshipApplication>,
    onOpenApplication: (InternshipApplication) -> Unit,
) {
    item(key = "$keyPrefix-header") {
        GroupHeaderTile(
            title = title,
            caption = caption,
            shape = RoundedCornerShape(
                topStart = GroupOuterRadius,
                topEnd = GroupOuterRadius,
                bottomStart = GroupInnerRadius,
                bottomEnd = GroupInnerRadius,
            ),
        )
        Spacer(Modifier.height(GroupTileGap))
    }
    itemsIndexed(
        items = items,
        key = { _, application -> "$keyPrefix-${application.id.value}" },
    ) { index, application ->
        val last = index == items.lastIndex
        InternshipApplicationRow(
            application = application,
            shape = RoundedCornerShape(
                topStart = GroupInnerRadius,
                topEnd = GroupInnerRadius,
                bottomStart = if (last) GroupOuterRadius else GroupInnerRadius,
                bottomEnd = if (last) GroupOuterRadius else GroupInnerRadius,
            ),
            onClick = { onOpenApplication(application) },
        )
        if (!last) Spacer(Modifier.height(GroupTileGap))
    }
}

@Composable
private fun GroupHeaderTile(
    title: String,
    caption: String,
    shape: Shape,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = shape,
    ) {
        Column(modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InternshipApplicationRow(
    application: InternshipApplication,
    shape: Shape,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val accent = when (application.state) {
        InternshipApplicationState.Started -> scheme.primaryContainer to scheme.onPrimaryContainer
        InternshipApplicationState.Submitted,
        InternshipApplicationState.NotAssigned -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        InternshipApplicationState.Confirmed -> scheme.secondaryContainer to scheme.onSecondaryContainer
        InternshipApplicationState.Closed -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
        InternshipApplicationState.Cancelled,
        InternshipApplicationState.Rejected -> scheme.errorContainer to scheme.onErrorContainer
        InternshipApplicationState.Unknown -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        shape = shape,
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(application.state.glyphShape())
                    .background(accent.first),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = application.state.glyphIcon(),
                    contentDescription = null,
                    tint = accent.second,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = application.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = application.detailLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(10.dp))
            StateBadge(application = application)
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun StateBadge(application: InternshipApplication) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (application.state) {
        InternshipApplicationState.Started -> scheme.primary to scheme.onPrimary
        InternshipApplicationState.Submitted,
        InternshipApplicationState.NotAssigned -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        InternshipApplicationState.Confirmed -> scheme.secondaryContainer to scheme.onSecondaryContainer
        InternshipApplicationState.Closed -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
        InternshipApplicationState.Cancelled,
        InternshipApplicationState.Rejected -> scheme.errorContainer to scheme.onErrorContainer
        InternshipApplicationState.Unknown -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .background(container, RoundedCornerShape(percent = 50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = application.stateLabel,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = content,
            maxLines = 1,
        )
    }
}

private val InternshipApplicationState.pending: Boolean
    get() = this == InternshipApplicationState.Submitted ||
        this == InternshipApplicationState.Confirmed ||
        this == InternshipApplicationState.NotAssigned

private val GroupOuterRadius = 20.dp
private val GroupInnerRadius = 4.dp
private val GroupTileGap = 2.dp

// MaterialShapes getters are @Composable in this material3 version, so the mapper is too.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InternshipApplicationState.glyphShape(): Shape = when (this) {
    InternshipApplicationState.Started -> MaterialShapes.Cookie6Sided.toShape()
    InternshipApplicationState.Submitted,
    InternshipApplicationState.NotAssigned -> MaterialShapes.Clover4Leaf.toShape()
    InternshipApplicationState.Confirmed -> MaterialShapes.Sunny.toShape()
    InternshipApplicationState.Closed -> MaterialShapes.Circle.toShape()
    InternshipApplicationState.Cancelled,
    InternshipApplicationState.Rejected -> MaterialShapes.Square.toShape()
    InternshipApplicationState.Unknown -> MaterialShapes.Square.toShape()
}

private fun InternshipApplicationState.glyphIcon(): ImageVector = when (this) {
    InternshipApplicationState.Started -> Icons.Rounded.Work
    InternshipApplicationState.Submitted -> Icons.Rounded.HourglassBottom
    InternshipApplicationState.Confirmed -> Icons.Rounded.ThumbUp
    InternshipApplicationState.NotAssigned -> Icons.Rounded.QuestionMark
    InternshipApplicationState.Closed -> Icons.Rounded.Check
    InternshipApplicationState.Cancelled,
    InternshipApplicationState.Rejected -> Icons.Rounded.Close
    InternshipApplicationState.Unknown -> Icons.Rounded.ErrorOutline
}

// Wraps full-screen empty/loading/error content in a scrollable container so the
// pull-to-refresh gesture still works when there's nothing else to scroll.
@Composable
private fun RefreshableEmpty(content: @Composable () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(Modifier.fillParentMaxSize()) { content() }
        }
    }
}

@Composable
private fun ErrorEmptyState(cause: Throwable, onRetry: () -> Unit) {
    EmptyState(
        icon = Icons.Outlined.CloudOff,
        title = "Caricamento non riuscito",
        body = cause.friendlyMessage(),
        action = { FilledTonalButton(onClick = onRetry) { Text("Riprova") } },
    )
}

private fun Throwable.friendlyMessage(): String = when (this) {
    is UnknownHostException,
    is ConnectException -> "Rete non disponibile. Controlla la connessione e riprova."
    is SocketTimeoutException -> "Timeout di rete. Riprova tra un momento."
    is IOException -> "Errore di rete. Riprova tra un momento."
    else -> "Si è verificato un errore imprevisto. Riprova."
}

private const val PULL_INDICATOR_DISMISS_DELAY_MS = 350L
