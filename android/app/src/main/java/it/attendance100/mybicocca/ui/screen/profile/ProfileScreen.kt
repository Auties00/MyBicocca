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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import it.attendance100.mybicocca.ui.component.SectionHeader
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.component.feedback.StatusBar
import it.attendance100.mybicocca.ui.screen.profile.component.CertificatesSection
import it.attendance100.mybicocca.ui.screen.profile.component.EnrollmentsEntryCard
import it.attendance100.mybicocca.ui.screen.profile.component.GradeTrendChart
import it.attendance100.mybicocca.ui.screen.profile.component.ProgressStatCard
import it.attendance100.mybicocca.ui.screen.profile.component.SkeletonProfileContent
import it.attendance100.mybicocca.ui.screen.profile.component.StatCard
import it.attendance100.mybicocca.ui.screen.profile.component.StudentCard
import it.attendance100.mybicocca.ui.screen.profile.component.TitlesSection
import it.attendance100.mybicocca.ui.screen.profile.state.DocumentEvent
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.EnrollmentsSheet
import it.attendance100.mybicocca.ui.screen.profile.subscreen.examsByYear.ExamValueMode
import it.attendance100.mybicocca.ui.screen.profile.subscreen.examsByYear.ExamsByYearSheet
import it.attendance100.mybicocca.ui.screen.profile.subscreen.hypotheticalGrade.HypotheticalGradeSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.openPdfDocument
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    val statsLoadable by viewModel.stats.collectAsStateWithLifecycle()
    val rollupLoadable by viewModel.gradeRollup.collectAsStateWithLifecycle()
    val rowsLoadable by viewModel.transcriptRows.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val error by viewModel.errorMessage.collectAsStateWithLifecycle()
    val prerequisiteStatuses by viewModel.prerequisiteStatuses.collectAsStateWithLifecycle()
    val account by viewModel.account.collectAsStateWithLifecycle()
    val activeCareer by viewModel.activeCareer.collectAsStateWithLifecycle()
    val photoFile by viewModel.photoFile.collectAsStateWithLifecycle()
    val titlesLoadable by viewModel.titles.collectAsStateWithLifecycle()
    val certificatesLoadable by viewModel.certificates.collectAsStateWithLifecycle()
    val downloadingCertificates by viewModel.downloadingCertificates.collectAsStateWithLifecycle()
    val badgeCardTheme by viewModel.badgeCardTheme.collectAsStateWithLifecycle()

    val stats = statsLoadable.valueOrNull()
    val rollup = rollupLoadable.valueOrNull()
    val rows = rowsLoadable.valueOrNull().orEmpty()
    val titles = titlesLoadable.valueOrNull().orEmpty()
    val certificates = certificatesLoadable.valueOrNull().orEmpty()

    val snackbar = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DocumentEvent.ShowMessage -> scope.launch { snackbar.showInfo(event.message) }
                is DocumentEvent.OpenPdf -> scope.launch {
                    runCatching { openPdfDocument(context, event.bytes, event.fileName) }
                        .onFailure { snackbar.showInfo("Nessuna app per aprire i PDF.") }
                }
            }
        }
    }

    // First-load gate is the Room snapshot itself: NotYetLoaded means Room hasn't emitted yet,
    // so we show the skeleton. A refresh over already-loaded data keeps the content on screen.
    val showSkeleton = statsLoadable is Loadable.NotYetLoaded

    // null = sheet closed; false = arithmetic mode; true = weighted mode.
    var calculatorWeighted by remember { mutableStateOf<Boolean?>(null) }
    // Which exams-by-year modal is open (passed-exam grades vs acquired credits), or null.
    var examsModal by remember { mutableStateOf<ExamValueMode?>(null) }
    // Whether the annual-enrollment ("Iscrizioni") timeline sheet is open.
    var showEnrollments by remember { mutableStateOf(false) }

    Box(modifier) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                StatusBar(
                    isOnline = isOnline,
                    errorMessage = error,
                    onDismissError = viewModel::clearError,
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
                        // The student card now scrolls in the body as the first item (it was a
                        // shell-level floating overlay before). Hidden until the account loads.
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
                                title = "Carriera",
                                modifier = Modifier.padding(bottom = 12.dp),
                            )
                            EnrollmentsEntryCard(onClick = { showEnrollments = true })
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

                        if (titles.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "Titoli",
                                    modifier = Modifier.padding(bottom = 12.dp),
                                )
                                TitlesSection(titles = titles)
                            }
                        }

                        if (certificates.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "Certificati",
                                    modifier = Modifier.padding(bottom = 12.dp),
                                )
                                CertificatesSection(
                                    certificates = certificates,
                                    downloading = downloadingCertificates,
                                    onDownload = viewModel::download,
                                )
                            }
                        }
                    }
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

    if (showEnrollments) {
        EnrollmentsSheet(onDismiss = { showEnrollments = false })
    }
}

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
