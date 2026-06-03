package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.component.AttendanceCourseCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.component.AttendanceFilterBar
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.subscreen.courseAttendanceDetail.CourseAttendanceDetailSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel = hiltViewModel(),
) {
    val data by viewModel.courses.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val pullState = rememberPullToRefreshState()
    var pullIndicatorVisible by remember { mutableStateOf(false) }
    var detailCourse by remember { mutableStateOf<CourseAttendance?>(null) }

    val years = remember(data) {
        (data as? Loadable.Loaded)?.value.orEmpty()
            .map { it.year }
            .filter { it != StudyYear.Unknown }
            .distinct()
            .sorted()
    }
    // A filter persisted from an older snapshot may point to a year with no courses.
    val effectiveYear = selectedYear?.takeIf { it in years }

    Column(modifier = Modifier.fillMaxSize()) {
        // Outside the refresh box, like the elearning home: the bar stays pinned
        // while only the list underneath scrolls.
        AttendanceFilterBar(
            selectedYear = effectiveYear,
            studyYears = years,
            onSelect = viewModel::selectYear,
        )

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
                    val courses = snapshot.value
                    val failure = syncStatus as? SyncStatus.Failed
                    when {
                        failure != null && courses.isEmpty() -> RefreshableEmpty {
                            ErrorEmptyState(cause = failure.cause, onRetry = viewModel::refresh)
                        }

                        courses.isEmpty() -> RefreshableEmpty {
                            EmptyState(
                                icon = Icons.Outlined.CoPresent,
                                title = "Nessun corso da frequentare",
                                body = "Hai superato tutti i corsi previsti dal tuo piano fino a questo semestre.",
                            )
                        }

                        else -> CourseList(
                            courses = remember(courses, effectiveYear) {
                                effectiveYear
                                    ?.let { year -> courses.filter { it.year == year } }
                                    ?: courses
                            },
                            onOpenCourse = { detailCourse = it },
                        )
                    }
                }
            }
        }
    }

    detailCourse?.let { course ->
        CourseAttendanceDetailSheet(
            course = course,
            onDismiss = { detailCourse = null },
        )
    }
}

// Connected segmented stack (M3 Expressive grouped list): wide corners on the
// group's outer edges, tight seams between segments — same language as the
// registry sections and the filter bar.
@Composable
private fun CourseList(
    courses: List<CourseAttendance>,
    onOpenCourse: (CourseAttendance) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(
            count = courses.size,
            key = { index -> courses[index].let { it.code ?: it.name } },
        ) { index ->
            val course = courses[index]
            AttendanceCourseCard(
                course = course,
                shape = segmentShape(index, courses.lastIndex),
                onClick = { onOpenCourse(course) },
            )
        }
    }
}

private fun segmentShape(index: Int, lastIndex: Int): Shape {
    val outer = 20.dp
    val inner = 4.dp
    return RoundedCornerShape(
        topStart = if (index == 0) outer else inner,
        topEnd = if (index == 0) outer else inner,
        bottomStart = if (index == lastIndex) outer else inner,
        bottomEnd = if (index == lastIndex) outer else inner,
    )
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
