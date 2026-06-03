package it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Grading
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.exam.ExamGrade
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.model.exam.requiresStudentDecision
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.state.ExamResultActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.state.ExamResultEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// The page lives behind the "Esiti" sub-page top bar, so it renders the list directly
// with no in-page header.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExamResultsScreen(
    viewModel: ExamResultsViewModel = hiltViewModel(),
) {
    val data by viewModel.results.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                ExamResultEvent.AcceptSucceeded -> scope.launch { snackbar.showInfo("Esito accettato") }
                ExamResultEvent.RejectSucceeded -> scope.launch { snackbar.showInfo("Esito rifiutato") }
                is ExamResultEvent.Failed -> scope.launch { snackbar.showError("Operazione non riuscita", event.cause) }
            }
        }
    }

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
                val eligible = snapshot.value.filter { it.requiresStudentDecision(today) }
                val failure = syncStatus as? SyncStatus.Failed
                when {
                    failure != null && eligible.isEmpty() -> RefreshableEmpty {
                        ErrorEmptyState(cause = failure.cause, onRetry = viewModel::refresh)
                    }

                    eligible.isEmpty() -> RefreshableEmpty {
                        EmptyState(
                            icon = Icons.Outlined.Grading,
                            title = "Nessun esito da gestire",
                            body = "Quando un docente pubblica un voto da accettare o rifiutare lo troverai qui.",
                        )
                    }

                    else -> ExamResultsList(
                        results = eligible,
                        actionState = actionState,
                        today = today,
                        onAccept = viewModel::accept,
                        onReject = viewModel::reject,
                    )
                }
            }
        }
    }
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

@Composable
private fun ExamResultsList(
    results: List<ExamResult>,
    actionState: ExamResultActionState,
    today: LocalDate,
    onAccept: (ExamResult) -> Unit,
    onReject: (ExamResult) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(
            items = results,
            // applicationListId is guaranteed by requiresStudentDecision; fall back defensively.
            key = { _, result -> result.applicationListId ?: result.key.hashCode().toLong() },
        ) { index, result ->
            val inProgress = (actionState as? ExamResultActionState.InProgress)
                ?.applicationListId == result.applicationListId
            ExamResultCard(
                result = result,
                badgeShape = badgeShapeFor(index),
                today = today,
                inProgress = inProgress,
                actionEnabled = actionState is ExamResultActionState.Idle,
                onAccept = { onAccept(result) },
                onReject = { onReject(result) },
            )
        }
    }
}

// Expressive badge polygons rotate per position, same trick as the booking cards.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun badgeShapeFor(index: Int): Shape = when (index % 4) {
    0 -> MaterialShapes.Sunny
    1 -> MaterialShapes.Cookie9Sided
    2 -> MaterialShapes.Clover4Leaf
    else -> MaterialShapes.Flower
}.toShape()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExamResultCard(
    result: ExamResult,
    badgeShape: Shape,
    today: LocalDate,
    inProgress: Boolean,
    actionEnabled: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.surfaceContainerLow,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradeBadge(grade = result.grade, shape = badgeShape)
                Spacer(Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = result.activityDescription ?: "Esame",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = scheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    result.examDateTime?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "Sostenuto il ${it.toLocalDate().format(DateFormat)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            result.publishedNote?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            result.acknowledgmentDeadline?.let {
                Spacer(Modifier.height(16.dp))
                DeadlineBanner(deadline = it, today = today)
            }

            Spacer(Modifier.height(12.dp))

            Box {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FilledTonalButton(
                        onClick = onReject,
                        enabled = actionEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = scheme.surfaceContainerHighest,
                            contentColor = scheme.onSurface,
                        ),
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Rifiuta", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onAccept,
                        enabled = actionEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Accetta", fontWeight = FontWeight.SemiBold)
                    }
                }
                if (inProgress) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(scheme.surfaceContainerLow.copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GradeBadge(grade: ExamGrade, shape: Shape) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (grade) {
        is ExamGrade.Numeric ->
            if (grade.value >= 31) scheme.tertiaryContainer to scheme.onTertiaryContainer
            else scheme.primaryContainer to scheme.onPrimaryContainer

        ExamGrade.Passed -> scheme.tertiaryContainer to scheme.onTertiaryContainer

        ExamGrade.NotPassed,
        ExamGrade.Withdrew,
        ExamGrade.Absent -> scheme.errorContainer to scheme.onErrorContainer

        ExamGrade.Unknown -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(shape)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = grade.shortLabel(),
            style = MaterialTheme.typography.titleLargeEmphasized,
            color = content,
        )
    }
}

@Composable
private fun DeadlineBanner(deadline: LocalDate, today: LocalDate) {
    val scheme = MaterialTheme.colorScheme
    val daysLeft = ChronoUnit.DAYS.between(today, deadline)
    val urgent = daysLeft <= 2
    val container = if (urgent) scheme.errorContainer else scheme.secondaryContainer
    val content = if (urgent) scheme.onErrorContainer else scheme.onSecondaryContainer
    val countdown = when (daysLeft) {
        0L -> "Oggi"
        1L -> "Domani"
        else -> "$daysLeft giorni"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.HourglassTop,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Decidi entro il ${deadline.format(DateFormat)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = countdown,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private val DateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)

private fun ExamGrade.shortLabel(): String = when (this) {
    is ExamGrade.Numeric -> if (value >= 31) "30L" else value.toString()
    ExamGrade.Passed -> "Id."
    ExamGrade.NotPassed -> "Resp."
    ExamGrade.Withdrew -> "Rit."
    ExamGrade.Absent -> "Ass."
    ExamGrade.Unknown -> "—"
}
