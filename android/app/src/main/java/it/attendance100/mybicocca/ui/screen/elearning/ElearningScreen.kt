package it.attendance100.mybicocca.ui.screen.elearning

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material.icons.outlined.School
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourseGroup
import it.attendance100.mybicocca.domain.model.elearning.course.courseCode
import it.attendance100.mybicocca.domain.model.elearning.deadline.Deadline
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.elearning.component.CardEdition
import it.attendance100.mybicocca.ui.screen.elearning.component.HomeFilterBar
import it.attendance100.mybicocca.ui.screen.elearning.component.NotebookCard
import it.attendance100.mybicocca.ui.screen.elearning.state.ElearningOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.state.InitialFetchState
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.AddCourseSheet
import it.attendance100.mybicocca.ui.screen.elearning.theme.LocalCourseAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.theme.ProvideCourseAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.theme.accentFor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Instant
import java.time.temporal.ChronoUnit


/**
 * E-learning tab: the student's Moodle course home.
 *
 * Renders one [NotebookCard] per course group in a vertical list under a [HomeFilterBar]
 * (all / favourites / per study year), all wrapped in the screen-scoped course-accent palette,
 * with an extended FAB that opens the [AddCourseSheet] catalog browser. The tab registers no
 * shell filter toggle — filtering is inline.
 *
 * Until the local cache is first populated the whole tab is a full-screen loading or error view
 * and the filter bar stays hidden; once settled, cached data keeps rendering while refreshes run
 * behind pull-to-refresh, and empty results distinguish "no courses" from "nothing matches the
 * current filter". Loading/empty/error views remain pull-refreshable, so the pull gesture
 * doubles as the retry affordance.
 *
 * Enrol outcomes from the sheet surface as snackbars. After a successful enrolment the
 * ViewModel drops any hiding filter and emits the course id; the screen waits for its group to
 * materialise in the (possibly just re-filtered) list, then animate-scrolls to it. [isActive]
 * is true only while this is the visible tab — see CalendarScreen for the pager-cache
 * rationale.
 */
@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElearningScreen(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
    onOpenCourse: (CourseId) -> Unit = {},
    onOpenAssignment: (CourseId, AssignmentId) -> Unit = { _, _ -> },
    onOpenQuiz: (CourseId, QuizId) -> Unit = { _, _ -> },
    onRequireSignIn: () -> Unit = {},
    viewModel: ElearningViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val visibleCourses by viewModel.visibleCourses.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val studyYears by viewModel.availableStudyYears.collectAsStateWithLifecycle()
    val initialFetch by viewModel.initialFetch.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    val coroutineScope = rememberCoroutineScope()
    var addSheetVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isActive) { if (isActive) onProvideFilterToggle(null) }

    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is ElearningOneShotEvent.OpenCourse -> onOpenCourse(event.courseId)
                is ElearningOneShotEvent.OpenAssignment -> onOpenAssignment(
                    event.courseId,
                    event.assignmentId
                )

                is ElearningOneShotEvent.OpenQuiz -> onOpenQuiz(event.courseId, event.quizId)
                ElearningOneShotEvent.RequireSignIn -> onRequireSignIn()
                ElearningOneShotEvent.OpenAddCourse -> addSheetVisible = true
            }
        }
    }

    val pullState = rememberPullToRefreshState()
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        viewModel.revealCourse.collect { courseId ->
            val index = withTimeoutOrNull(4_000) {
                snapshotFlow {
                    (visibleCourses as? Loadable.Loaded)?.value
                        ?.indexOfFirst { group -> group.editions.any { it.id == courseId } }
                        ?: -1
                }.first { it >= 0 }
            }
            if (index != null && index >= 0) listState.animateScrollToItem(index)
        }
    }

    ProvideCourseAccentPalette {
        Box(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (initialFetch is InitialFetchState.Settled) {
                    HomeFilterBar(
                        selected = filter,
                        studyYears = studyYears,
                        onSelect = viewModel::setFilter,
                    )
                }

                PullToRefreshBox(
                    isRefreshing = syncStatus is SyncStatus.Refreshing,
                    onRefresh = viewModel::pullToRefresh,
                    state = pullState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (val initial = initialFetch) {
                        InitialFetchState.InProgress -> RefreshableEmpty {
                            ElearningLoadingState(modifier = Modifier.fillMaxSize())
                        }

                        is InitialFetchState.Failed -> RefreshableEmpty {
                            ErrorEmptyState(
                                cause = initial.cause,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        InitialFetchState.Settled -> {
                            when (val data = visibleCourses) {
                                is Loadable.Loaded if data.value.isNotEmpty() -> CourseList(
                                    groups = data.value,
                                    listState = listState,
                                    onOpenCourse = viewModel::openCourse,
                                    onOpenDeadline = viewModel::openDeadline,
                                    onToggleGroupFavourite = viewModel::toggleGroupFavourite,
                                )

                                is Loadable.Loaded -> RefreshableEmpty {
                                    EmptyStateForCurrentFilter(
                                        filterActive = filter !is CourseFilter.All,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }

                                else -> RefreshableEmpty {
                                    ElearningLoadingState(modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }
            }

            AddCourseFab(
                onClick = { addSheetVisible = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            )
        }
    }

    if (addSheetVisible) {
        AddCourseSheet(
            onDismiss = { addSheetVisible = false },
            onEnrolFailed = { cause ->
                coroutineScope.launch {
                    snackbar.showError(enrolFailureMessage(cause))
                }
            },
            onEnrolSucceeded = { courseId, name ->
                coroutineScope.launch {
                    snackbar.showInfo("Iscritto a $name")
                }
                viewModel.revealEnrolledCourse(courseId)
            },
            onRequireSignIn = {
                addSheetVisible = false
                onRequireSignIn()
            },
        )
    }
}

/**
 * The enrol failure carries a meaningful reason — the Moodle warning (e.g. an enrolment-key
 * requirement or "self-enrolment disabled"), or a network error — so it is surfaced instead of
 * a bare title the user can't act on.
 */
private fun enrolFailureMessage(cause: Throwable): String {
    val reason = when (cause) {
        is UnknownHostException, is ConnectException -> "rete non disponibile"
        is SocketTimeoutException -> "timeout di rete"
        is IOException -> "errore di rete"
        else -> cause.message?.takeIf { it.isNotBlank() }
    }
    return if (reason != null) "Iscrizione non riuscita: $reason" else "Iscrizione non riuscita"
}

@Composable
private fun AddCourseFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
            )
        },
        text = { Text("Aggiungi corso") },
    )
}

/**
 * The course-group card list. Each group remembers its active edition tab; when the remembered
 * edition is no longer in the editions list (a year just dropped out of the filter) the card
 * falls back to the group's latest edition. Card deadlines are pruned to those due in the
 * future or within the past two weeks. Items fade and spring into place as filtering reshuffles
 * the list.
 */
@Composable
private fun CourseList(
    groups: List<EnrolledCourseGroup>,
    listState: LazyListState,
    onOpenCourse: (CourseId) -> Unit,
    onOpenDeadline: (Deadline) -> Unit,
    onToggleGroupFavourite: (EnrolledCourseGroup, Boolean) -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = groups,
                key = { it.editions.first().id.value }
            ) { group ->
                val accentPalette = LocalCourseAccentPalette.current
                val groupKey = group.sharedCode ?: group.latest.id.value
                var activeEditionId by rememberSaveable(groupKey) { mutableIntStateOf(group.latest.id.value) }

                val resolvedActiveId = group.editions
                    .firstOrNull { it.id.value == activeEditionId }
                    ?.id
                    ?: group.latest.id
                val active = group.editions.first { it.id == resolvedActiveId }
                val cardEditions = group.editions.map { e ->
                    val code = e.courseCode()
                    CardEdition(
                        id = e.id,
                        yearLabel = code.periodLabel ?: "Trasversale",
                        accent = accentPalette.accentFor(e.id),
                        academicYear = code.academicYear,
                    )
                }

                val validDeadlines =
                    active.deadlines.filter { it.dueAt > Instant.now().minus(14, ChronoUnit.DAYS) }

                NotebookCard(
                    title = active.fullName,
                    code = active.idNumber?.takeIf { it.isNotBlank() },
                    editions = cardEditions,
                    activeEditionId = active.id,
                    isFavourite = group.isFavourite,
                    deadlines = validDeadlines,
                    onClick = { onOpenCourse(active.id) },
                    onSelectEdition = { id -> activeEditionId = id.value },
                    onToggleFavourite = { fav -> onToggleGroupFavourite(group, fav) },
                    onDeadlineClick = onOpenDeadline,
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = tween(durationMillis = 260),
                            fadeOutSpec = tween(durationMillis = 220),
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
private fun EmptyStateForCurrentFilter(
    filterActive: Boolean,
    modifier: Modifier = Modifier,
) {
    when {
        filterActive -> EmptyState(
            icon = Icons.Outlined.FilterAltOff,
            title = "Nessun corso per questo filtro",
            body = "Cambia filtro per vedere altri corsi.",
            modifier = modifier,
        )

        else -> EmptyState(
            icon = Icons.Outlined.School,
            title = "Nessun corso",
            body = "Non risulti iscritto a nessun corso e-learning.",
            modifier = modifier,
        )
    }
}

@Composable
private fun ElearningLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * Wraps a non-scrolling empty/error/loading view in a LazyColumn so the surrounding
 * PullToRefreshBox still receives nested-scroll gestures and the user can pull to retry from
 * these states.
 */
@Composable
private fun RefreshableEmpty(content: @Composable () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(Modifier.fillParentMaxSize()) {
                content()
            }
        }
    }
}

/**
 * Full-screen sync-failure state. No explicit retry action: the state sits inside the
 * PullToRefreshBox, so the pull gesture is the retry.
 */
@Composable
private fun ErrorEmptyState(
    cause: Throwable,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        icon = Icons.Outlined.CloudOff,
        title = "Sincronizzazione non riuscita",
        body = cause.friendlyMessage(),
        modifier = modifier,
    )
}

private fun Throwable.friendlyMessage(): String = when (this) {
    is UnknownHostException,
    is ConnectException -> "Rete non disponibile. Controlla la connessione e riprova."

    is SocketTimeoutException -> "Timeout di rete. Riprova tra un momento."
    is IOException -> "Errore di rete. Riprova tra un momento."
    else -> "Si è verificato un errore imprevisto"
}
