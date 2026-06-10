package it.attendance100.mybicocca.ui.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import it.attendance100.mybicocca.ui.component.text.SectionHeader
import it.attendance100.mybicocca.ui.component.feedback.ErrorBanner
import it.attendance100.mybicocca.ui.screen.profile.component.GradeTrendChart
import it.attendance100.mybicocca.ui.screen.profile.component.ProgressStatCard
import it.attendance100.mybicocca.ui.screen.profile.component.SkeletonProfileContent
import it.attendance100.mybicocca.ui.screen.profile.component.StatCard
import it.attendance100.mybicocca.ui.screen.profile.component.StudentCard
import it.attendance100.mybicocca.ui.screen.profile.subscreen.examsByYear.ExamValueMode
import it.attendance100.mybicocca.ui.screen.profile.subscreen.examsByYear.ExamsByYearSheet
import it.attendance100.mybicocca.ui.screen.profile.subscreen.hypotheticalGrade.HypotheticalGradeSheet
import java.util.Locale

/**
 * Full-screen profile page: the student's identity badge and career statistics behind a
 * pull-to-refresh container that re-syncs the transcript while cached content stays on
 * screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onOpenAppelli: (courseKey: String) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Box(modifier) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
        ) {
            ProfileContent(
                modifier = Modifier.fillMaxSize(),
                onOpenAppelli = onOpenAppelli,
                viewModel = viewModel,
            )
        }
    }
}

/**
 * Profile body shared by [ProfileScreen], which wraps it in pull-to-refresh, and
 * [ProfileSheetContent], which cannot host the refresh gesture because the drag belongs
 * to the sheet.
 *
 * Renders a scrolling column: the flippable [StudentCard] (hidden until the account
 * loads), the "Statistiche" grid of average and progress tiles, and the "Andamento" grade
 * trend chart. The average tiles open [HypotheticalGradeSheet] in arithmetic or weighted
 * mode; the progress tiles open [ExamsByYearSheet] on the grades or credits view, whose
 * course detail can bubble an appelli deep-link up through [onOpenAppelli]. Guided search
 * lands here via [ProfileViewModel.openCalculatorRequests], opening the calculator
 * directly in weighted mode.
 *
 * The first-load skeleton is gated on the cache snapshot itself: [Loadable.NotYetLoaded]
 * means the cache has not emitted yet, while a refresh over loaded data keeps the content
 * on screen.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    onOpenAppelli: (courseKey: String) -> Unit = {},
    viewModel: ProfileViewModel,
) {
    val statsLoadable by viewModel.stats.collectAsStateWithLifecycle()
    val rollupLoadable by viewModel.gradeRollup.collectAsStateWithLifecycle()
    val rowsLoadable by viewModel.transcriptRows.collectAsStateWithLifecycle()
    val error by viewModel.errorMessage.collectAsStateWithLifecycle()
    val prerequisiteStatuses by viewModel.prerequisiteStatuses.collectAsStateWithLifecycle()
    val account by viewModel.account.collectAsStateWithLifecycle()
    val activeCareer by viewModel.activeCareer.collectAsStateWithLifecycle()
    val photoFile by viewModel.photoFile.collectAsStateWithLifecycle()
    val badgeCardTheme by viewModel.badgeCardTheme.collectAsStateWithLifecycle()

    val stats = statsLoadable.valueOrNull()
    val rollup = rollupLoadable.valueOrNull()
    val rows = rowsLoadable.valueOrNull().orEmpty()

    val showSkeleton = statsLoadable is Loadable.NotYetLoaded

    var calculatorWeighted by remember { mutableStateOf<Boolean?>(null) }
    var examsModal by remember { mutableStateOf<ExamValueMode?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.openCalculatorRequests.collect { calculatorWeighted = true }
    }

    Column(modifier) {
        ErrorBanner(
            message = error,
            onDismiss = viewModel::clearError,
        )

        if (showSkeleton) {
            SkeletonProfileContent()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                    top = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                account?.let { acc ->
                    item {
                        StudentCard(
                            account = acc,
                            career = activeCareer,
                            photoFile = photoFile,
                            enabled = true,
                            modifier = Modifier.fillMaxWidth(),
                            theme = badgeCardTheme,
                        )
                    }
                }

                item {
                    SectionHeader(
                        title = "Statistiche",
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    StatisticsGrid(
                        stats = stats,
                        onCalculateArithmetic = { calculatorWeighted = false },
                        onCalculateWeighted = { calculatorWeighted = true },
                        onShowExams = { examsModal = ExamValueMode.Grade },
                        onShowCredits = { examsModal = ExamValueMode.Credits },
                    )
                }

                item {
                    SectionHeader(
                        title = "Andamento",
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    GradeTrendChart(rows = rows)
                }
            }
        }
    }

    calculatorWeighted?.let { weighted ->
        HypotheticalGradeSheet(
            rollup = rollup,
            currentArithmetic = stats?.arithmeticAverage,
            currentWeighted = stats?.weightedAverage,
            isWeighted = weighted,
            onDismiss = { calculatorWeighted = null },
        )
    }

    examsModal?.let { initialMode ->
        ExamsByYearSheet(
            rows = rows,
            initialMode = initialMode,
            prerequisiteStatuses = prerequisiteStatuses,
            onOpenAppelli = onOpenAppelli,
            onDismiss = { examsModal = null },
        )
    }
}

/**
 * Two-by-two stat grid: arithmetic and weighted average tiles, each with a calculator
 * affordance, above passed-exams and earned-credits progress tiles that open the
 * exams-by-year breakdown.
 */
@Composable
private fun StatisticsGrid(
    stats: TranscriptStats?,
    onCalculateArithmetic: () -> Unit,
    onCalculateWeighted: () -> Unit,
    onShowExams: () -> Unit,
    onShowCredits: () -> Unit,
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    val rowHeight = 110.dp

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(rowHeight),
        ) {
            StatCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                title = "Media Aritmetica",
                value = stats?.arithmeticAverage?.let {
                    String.format(
                        Locale.getDefault(),
                        "%.2f",
                        it
                    )
                } ?: "—",
                textColor = textColor,
                icon = { m ->
                    Icon(
                        Icons.Filled.Calculate,
                        contentDescription = "Calcola media ipotetica",
                        tint = primaryColor,
                        modifier = m
                    )
                },
                iconOnClick = onCalculateArithmetic,
                onClick = onCalculateArithmetic,
            )
            StatCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                title = "Media Ponderata",
                value = stats?.weightedAverage?.let {
                    String.format(
                        Locale.getDefault(),
                        "%.2f",
                        it
                    )
                } ?: "—",
                textColor = textColor,
                icon = { m ->
                    Icon(
                        Icons.Filled.Calculate,
                        contentDescription = "Calcola media ipotetica",
                        tint = primaryColor,
                        modifier = m
                    )
                },
                iconOnClick = onCalculateWeighted,
                onClick = onCalculateWeighted,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(rowHeight),
        ) {
            ProgressStatCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                title = "Esami Sostenuti",
                current = stats?.passedExamCount ?: 0,
                total = stats?.plannedExamCount ?: 0,
                textColor = textColor,
                progressbar = true,
                onClick = onShowExams,
            )
            ProgressStatCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                title = "CFU Acquisiti",
                current = stats?.passedCredits?.toInt() ?: 0,
                total = stats?.totalCreditsRequired?.toInt() ?: 0,
                textColor = textColor,
                progressbar = true,
                onClick = onShowCredits,
            )
        }
    }
}
