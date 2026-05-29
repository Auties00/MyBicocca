package it.attendance100.mybicocca.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import it.attendance100.mybicocca.ui.component.feedback.StatusBar
import it.attendance100.mybicocca.ui.screen.profile.component.BadgeBack
import it.attendance100.mybicocca.ui.screen.profile.component.BadgeFront
import it.attendance100.mybicocca.ui.screen.profile.component.CreditCard
import it.attendance100.mybicocca.ui.screen.profile.component.GradeTrendChart
import it.attendance100.mybicocca.ui.screen.profile.component.ProgressStatCard
import it.attendance100.mybicocca.ui.screen.profile.component.SkeletonProfileContent
import it.attendance100.mybicocca.ui.screen.profile.component.StatCard
import it.attendance100.mybicocca.ui.screen.profile.subscreen.examsByYear.ExamValueMode
import it.attendance100.mybicocca.ui.screen.profile.subscreen.examsByYear.ExamsByYearSheet
import it.attendance100.mybicocca.ui.screen.profile.subscreen.hypotheticalGrade.HypotheticalGradeSheet
import it.attendance100.mybicocca.ui.theme.BadgeWhiteDrawableColor
import it.attendance100.mybicocca.ui.theme.OnBackgroundColor
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    val career by viewModel.activeCareer.collectAsStateWithLifecycle()
    val photoFile by viewModel.photoFile.collectAsStateWithLifecycle()
    val statsLoadable by viewModel.stats.collectAsStateWithLifecycle()
    val rollupLoadable by viewModel.gradeRollup.collectAsStateWithLifecycle()
    val rowsLoadable by viewModel.transcriptRows.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val error by viewModel.errorMessage.collectAsStateWithLifecycle()

    val stats = statsLoadable.valueOrNull()
    val rollup = rollupLoadable.valueOrNull()
    val rows = rowsLoadable.valueOrNull().orEmpty()

    // First-load gate is the Room snapshot itself: NotYetLoaded means Room hasn't emitted yet,
    // so we show the skeleton. A refresh over already-loaded data keeps the content on screen.
    val showSkeleton = statsLoadable is Loadable.NotYetLoaded

    // null = sheet closed; false = arithmetic mode; true = weighted mode.
    var calculatorWeighted by remember { mutableStateOf<Boolean?>(null) }
    // Which exams-by-year modal is open (passed-exam grades vs acquired credits), or null.
    var examsModal by remember { mutableStateOf<ExamValueMode?>(null) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        // Match the expanded (sub-page) app bar surface so the page reads as one continuous
        // panel. The collapsed/sub-page bar lerps to surfaceContainer (tonal elevation 0).
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        SectionTitle("Dati Personali")
                        Spacer(Modifier.height(12.dp))
                        CreditCard(
                            modifier = Modifier.fillMaxWidth(),
                            accentColor = MaterialTheme.colorScheme.primary,
                            isChromatic = true,
                            frontContent = { x, y, white, _ ->
                                BadgeFront(
                                    account = account,
                                    career = career,
                                    textColor = if (white) BadgeWhiteDrawableColor else OnBackgroundColor,
                                    touchX = x,
                                    touchY = y,
                                    whiteBadge = white,
                                )
                            },
                            backContent = { x, y, white, haze ->
                                BadgeBack(
                                    account = account,
                                    photoFile = photoFile,
                                    textColor = if (white) BadgeWhiteDrawableColor else OnBackgroundColor,
                                    touchX = x,
                                    touchY = y,
                                    whiteBadge = white,
                                    hazeState = haze,
                                )
                            },
                        )
                    }

                    item {
                        SectionTitle("Statistiche")
                        Spacer(Modifier.height(12.dp))
                        StatisticsGrid(
                            stats = stats,
                            onCalculateArithmetic = { calculatorWeighted = false },
                            onCalculateWeighted = { calculatorWeighted = true },
                            onShowExams = { examsModal = ExamValueMode.Grade },
                            onShowCredits = { examsModal = ExamValueMode.Credits },
                        )
                    }

                    item {
                        GradeTrendChart(rows = rows)
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
            onDismiss = { examsModal = null },
        )
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Media Aritmetica",
                value = stats?.arithmeticAverage?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: "—",
                textColor = textColor,
                icon = { m -> Icon(Icons.Filled.Calculate, contentDescription = "Calcola media ipotetica", tint = primaryColor, modifier = m) },
                iconOnClick = onCalculateArithmetic,
                onClick = onCalculateArithmetic,
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Media Ponderata",
                value = stats?.weightedAverage?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: "—",
                textColor = textColor,
                icon = { m -> Icon(Icons.Filled.Calculate, contentDescription = "Calcola media ipotetica", tint = primaryColor, modifier = m) },
                iconOnClick = onCalculateWeighted,
                onClick = onCalculateWeighted,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProgressStatCard(
                modifier = Modifier.weight(1f),
                title = "Esami Sostenuti",
                current = stats?.passedExamCount ?: 0,
                total = stats?.plannedExamCount ?: 0,
                textColor = textColor,
                progressbar = true,
                onClick = onShowExams,
            )
            ProgressStatCard(
                modifier = Modifier.weight(1f),
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

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    )
}
