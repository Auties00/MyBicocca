package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires

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
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import it.attendance100.mybicocca.domain.model.questionnaire.ActivityQuestionnaires
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireUnit
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.ext.detailLine
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.ext.displayName
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.ext.label
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.ext.pending
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.units.QuestionnaireUnitsSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuestionnairesScreen(
    viewModel: QuestionnairesViewModel = hiltViewModel(),
    onStartCompilation: (QuestionnaireActivity, ActivityQuestionnaires, QuestionnaireUnit) -> Unit = { _, _, _ -> },
) {
    val data by viewModel.activities.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val selectedActivity by viewModel.selectedActivity.collectAsStateWithLifecycle()
    val activityDetail by viewModel.activityDetail.collectAsStateWithLifecycle()
    val detailStatus by viewModel.detailStatus.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val pullState = rememberPullToRefreshState()
    var pullIndicatorVisible by remember { mutableStateOf(false) }

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
                            icon = Icons.AutoMirrored.Outlined.FactCheck,
                            title = "Nessun questionario",
                            body = "Quando un insegnamento aprirà la valutazione della didattica lo troverai qui.",
                        )
                    }

                    else -> QuestionnairesContent(
                        activities = snapshot.value,
                        onOpenActivity = viewModel::openActivity,
                    )
                }
            }
        }
    }

    selectedActivity?.let { activity ->
        QuestionnaireUnitsSheet(
            activity = activity,
            detail = activityDetail,
            detailStatus = detailStatus,
            onCompileUnit = { detail, unit ->
                viewModel.dismissActivity()
                onStartCompilation(activity, detail, unit)
            },
            onRetry = viewModel::retryDetail,
            onDismiss = viewModel::dismissActivity,
        )
    }
}

@Composable
private fun QuestionnairesContent(
    activities: List<QuestionnaireActivity>,
    onOpenActivity: (QuestionnaireActivity) -> Unit,
) {
    val pending = activities.filter { it.status != QuestionnaireActivityStatus.Completed }
    val completed = activities.filter { it.status == QuestionnaireActivityStatus.Completed }
    val pendingCount = pending.count { it.status.pending }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 32.dp),
    ) {
        if (pending.isNotEmpty()) {
            segmentedGroup(
                keyPrefix = "pending",
                title = "Da compilare",
                caption = when (pendingCount) {
                    0 -> "Nessuna valutazione in attesa"
                    1 -> "1 insegnamento in attesa di valutazione"
                    else -> "$pendingCount insegnamenti in attesa di valutazione"
                },
                items = pending,
                onOpenActivity = onOpenActivity,
            )
        }

        if (completed.isNotEmpty()) {
            if (pending.isNotEmpty()) {
                item(key = "group-gap") { Spacer(Modifier.height(20.dp)) }
            }
            segmentedGroup(
                keyPrefix = "completed",
                title = "Compilati",
                caption = "Valutazioni già inviate",
                items = completed,
                onOpenActivity = onOpenActivity,
            )
        }
    }
}

// A connected segmented card in the registry's expressive style: a header tile followed
// by one tile per activity, 2.dp gaps, large radii only on the group's outer corners.
private fun LazyListScope.segmentedGroup(
    keyPrefix: String,
    title: String,
    caption: String,
    items: List<QuestionnaireActivity>,
    onOpenActivity: (QuestionnaireActivity) -> Unit,
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
        key = { _, activity -> "$keyPrefix-${activity.activityChoiceId}" },
    ) { index, activity ->
        val last = index == items.lastIndex
        QuestionnaireActivityRow(
            activity = activity,
            shape = RoundedCornerShape(
                topStart = GroupInnerRadius,
                topEnd = GroupInnerRadius,
                bottomStart = if (last) GroupOuterRadius else GroupInnerRadius,
                bottomEnd = if (last) GroupOuterRadius else GroupInnerRadius,
            ),
            onClick = { onOpenActivity(activity) },
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
private fun QuestionnaireActivityRow(
    activity: QuestionnaireActivity,
    shape: Shape,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val accent = when (activity.status) {
        QuestionnaireActivityStatus.ToCompile -> scheme.primaryContainer to scheme.onPrimaryContainer
        QuestionnaireActivityStatus.PartiallyCompleted -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        QuestionnaireActivityStatus.Completed -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
        QuestionnaireActivityStatus.ConfigurationError -> scheme.errorContainer to scheme.onErrorContainer
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
                    .clip(activity.status.glyphShape())
                    .background(accent.first),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = activity.status.glyphIcon(),
                    contentDescription = null,
                    tint = accent.second,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = activity.detailLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(10.dp))
            StatusBadge(status = activity.status)
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
private fun StatusBadge(status: QuestionnaireActivityStatus) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (status) {
        QuestionnaireActivityStatus.ToCompile -> scheme.primary to scheme.onPrimary
        QuestionnaireActivityStatus.PartiallyCompleted -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        QuestionnaireActivityStatus.Completed -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
        QuestionnaireActivityStatus.ConfigurationError -> scheme.errorContainer to scheme.onErrorContainer
    }
    Box(
        modifier = Modifier
            .background(container, RoundedCornerShape(percent = 50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = content,
            maxLines = 1,
        )
    }
}

private val GroupOuterRadius = 20.dp
private val GroupInnerRadius = 4.dp
private val GroupTileGap = 2.dp

// MaterialShapes getters are @Composable in this material3 version, so the mapper is too.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QuestionnaireActivityStatus.glyphShape(): Shape = when (this) {
    QuestionnaireActivityStatus.ToCompile -> MaterialShapes.Cookie6Sided.toShape()
    QuestionnaireActivityStatus.PartiallyCompleted -> MaterialShapes.Clover4Leaf.toShape()
    QuestionnaireActivityStatus.Completed -> MaterialShapes.Circle.toShape()
    QuestionnaireActivityStatus.ConfigurationError -> MaterialShapes.Square.toShape()
}

private fun QuestionnaireActivityStatus.glyphIcon(): ImageVector = when (this) {
    QuestionnaireActivityStatus.ToCompile -> Icons.Rounded.EditNote
    QuestionnaireActivityStatus.PartiallyCompleted -> Icons.Rounded.HourglassBottom
    QuestionnaireActivityStatus.Completed -> Icons.Rounded.Check
    QuestionnaireActivityStatus.ConfigurationError -> Icons.Rounded.ErrorOutline
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
    else -> "Si è verificato un errore imprevisto"
}

private const val PULL_INDICATOR_DISMISS_DELAY_MS = 350L
