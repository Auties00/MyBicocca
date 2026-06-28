package it.attendance100.mybicocca.ui.screen.profile.subscreen.courseDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import it.attendance100.mybicocca.core.os.rememberHapticManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.transcript.AttemptOutcome
import it.attendance100.mybicocca.domain.model.transcript.CourseAttempt
import it.attendance100.mybicocca.domain.model.transcript.CourseDetail
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadgeTone
import it.attendance100.mybicocca.ui.screen.registry.theme.registryBadgeTone
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PassedGreen = Color(0xFF1FA84B)
private val FullDateFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

/**
 * Body page for a single libretto course, hosted as the depth-1 page of the exams sheet's
 * in-sheet pager. The course name and code ride the sheet's morphing header, so this page
 * renders only the outcome card, the propedeuticità warning, the attempt cards (or an
 * empty state), and the bottom appelli CTA. The detail loads lazily per course and is not
 * cached: a live fetch whose failure surfaces as [SyncStatus.Failed] with an in-page
 * retry.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CourseDetailPage(
    row: TranscriptRow,
    onOpenAppelli: (courseKey: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CourseDetailViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    LaunchedEffect(row.id) {
        viewModel.load(activityChoiceId = row.id, alreadyPassed = row.passed)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 480.dp),
        ) {
            when (val data = detail) {
                Loadable.NotYetLoaded -> when (syncStatus) {
                    is SyncStatus.Failed -> DetailError(
                        onRetry = { viewModel.retry(row.id, row.passed) },
                    )

                    else -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(52.dp))
                    }
                }

                is Loadable.Loaded -> CourseBody(row = row, detail = data.value)
            }
        }

        Spacer(Modifier.height(12.dp))
        AppelliButton(
            row = row,
            detail = (detail as? Loadable.Loaded)?.value,
            onOpenAppelli = onOpenAppelli
        )
        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Scrollable course body. The propedeuticità warning surfaces only when the check resolved
 * to [PrerequisiteStatus.NotSatisfied]; unknown or satisfied prerequisites show nothing.
 */
@Composable
private fun CourseBody(
    row: TranscriptRow,
    detail: CourseDetail,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (row.passed) OutcomeCard(row = row)

        if (!row.passed && detail.prerequisites == PrerequisiteStatus.NotSatisfied) {
            PrerequisiteWarning()
        }

        if (detail.attempts.isNotEmpty()) {
            SectionLabel("Prove sostenute")
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                detail.attempts.forEachIndexed { index, attempt ->
                    AttemptCard(
                        attempt = attempt,
                        isFirst = index == 0,
                        isLast = index == detail.attempts.lastIndex,
                    )
                }
            }
        } else if (!row.passed) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
            ) {
                EmptyState(
                    icon = Icons.Outlined.HistoryEdu,
                    title = stringResource(R.string.profile_course_no_exams_title),
                    body = stringResource(R.string.profile_course_no_exams_body),
                )
            }
        }
    }
}

@Composable
private fun OutcomeCard(row: TranscriptRow) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = PassedGreen,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_course_passed),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurfaceVariant,
                )
                row.examDate?.let { date ->
                    Text(
                        text = date.format(FullDateFormat).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurface,
                    )
                }
            }
            Text(
                text = gradeLabel(row),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
        }
    }
}

@Composable
private fun PrerequisiteWarning() {
    val tone = registryBadgeTone(RegistryBadgeTone.Attention)
    Surface(
        color = tone.container,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = tone.onContainer,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = stringResource(R.string.profile_course_prereq_unmet),
                style = MaterialTheme.typography.bodyMedium,
                color = tone.onContainer,
            )
        }
    }
}

/**
 * One prova as a card in a segmented group — 28dp corners cap the group's ends, 6dp where
 * cards touch, the same group language as the exam list. A green dot marks a passed
 * outcome; the trailing label shows the grade, judgment, or idoneità.
 */
@Composable
private fun AttemptCard(
    attempt: CourseAttempt,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val outcome = attempt.bestOutcome
    val passed = outcome?.passed == true
    val shape = RoundedCornerShape(
        topStart = if (isFirst) 28.dp else 6.dp,
        topEnd = if (isFirst) 28.dp else 6.dp,
        bottomStart = if (isLast) 28.dp else 6.dp,
        bottomEnd = if (isLast) 28.dp else 6.dp,
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = scheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (passed) PassedGreen else scheme.outlineVariant),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attempt.callDate?.format(FullDateFormat)
                        ?.replaceFirstChar { it.uppercase() }
                        ?: attempt.sessionDescription
                        ?: "Prova",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurface,
                )
                val subtitle = attempt.statusDescription ?: attempt.sessionDescription
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            attemptOutcomeLabel(outcome)?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (passed) scheme.onSurface else scheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Bottom CTA: a filled "Vai agli appelli" button when bookable calls exist, a disabled
 * tonal placeholder otherwise. The bookable count prefers the freshly fetched detail and
 * falls back to the cached row value. The emitted course key must match the appelli
 * grouping key, which is the activity code when present and the activity name otherwise.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppelliButton(
    row: TranscriptRow,
    detail: CourseDetail?,
    onOpenAppelli: (courseKey: String) -> Unit,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    val bookableCount = detail?.bookableCallsCount ?: row.bookableCallsCount
    val hasAppelli = bookableCount > 0
    val courseKey = row.activityCode?.takeIf { it.isNotBlank() } ?: row.activityName

    if (hasAppelli) {
        Button(
            onClick = { haptic.tap(); onOpenAppelli(courseKey) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(100),
            colors = ButtonDefaults.buttonColors(
                containerColor = scheme.primary,
                contentColor = Color.White,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.profile_course_go_to_appelli),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp),
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    } else {
        FilledTonalButton(
            onClick = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(100),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.surfaceContainerHighest,
                contentColor = scheme.onSurfaceVariant,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.profile_course_no_appelli),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun DetailError(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.profile_course_load_failed),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        RetryButton(onClick = onRetry)
    }
}

private fun gradeLabel(row: TranscriptRow): String = when {
    row.cumLaude -> "30L"
    row.grade != null -> row.grade.toString()
    else -> "Idoneo"
}

private fun attemptOutcomeLabel(outcome: AttemptOutcome?): String? {
    if (outcome == null) return null
    return when {
        outcome.cumLaude -> "30L"
        outcome.grade != null -> outcome.grade.toInt().toString()
        outcome.judgment != null -> outcome.judgment
        outcome.passed -> "Idoneo"
        else -> null
    }
}
