package it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentHistory
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.component.EnrollmentTimelineNode
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.component.RenewalBanner
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.subscreen.yearDetail.EnrollmentDetailSheet

// "Iscrizioni": the annual-enrollment timeline + renewal affordance, opened from the
// profile. One node per academic year, newest first; tap a node for the full year detail.
@Composable
fun EnrollmentsSheet(
    onDismiss: () -> Unit,
    viewModel: EnrollmentsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val historyLoadable by viewModel.history.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    var detail by remember { mutableStateOf<AnnualEnrollment?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EnrollmentEvent.OpenRenewalWeb -> runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            }
        }
    }

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sizeDuration = 500,
    ) { _, _ ->
        val history = (historyLoadable as? Loadable.Loaded)?.value
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 760.dp),
        ) {
            Header(history)

            when {
                history != null -> Body(
                    history = history,
                    onRenew = viewModel::renew,
                    onOpenYear = { detail = it },
                )

                syncStatus is SyncStatus.Failed -> ErrorBox(onRetry = viewModel::refresh)
                else -> LoadingBox()
            }
        }
    }

    detail?.let { enrollment ->
        EnrollmentDetailSheet(
            enrollment = enrollment,
            onDismiss = { detail = null },
        )
    }
}

@Composable
private fun Header(history: EnrollmentHistory?) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(start = 22.dp, top = 4.dp, end = 22.dp, bottom = 4.dp)) {
        Text(
            text = "Iscrizioni",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.9).sp,
            color = scheme.onSurface,
        )
        Text(
            text = buildAnnotatedString {
                val count = history?.years?.size ?: 0
                if (count == 0) {
                    append("Storico delle iscrizioni annuali")
                } else {
                    withStyle(SpanStyle(color = scheme.primary, fontWeight = FontWeight.Bold)) {
                        append(if (count == 1) "1 anno accademico" else "$count anni accademici")
                    }
                    append(" registrati")
                }
            },
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ColumnScope.Body(
    history: EnrollmentHistory,
    onRenew: () -> Unit,
    onOpenYear: (AnnualEnrollment) -> Unit,
) {
    val latestYear = history.latest?.academicYear
    LazyColumn(
        modifier = Modifier
            .weight(1f, fill = false)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp),
    ) {
        item(key = "renewal") {
            RenewalBanner(
                state = history.renewal,
                onRenew = onRenew,
                modifier = Modifier.padding(bottom = 18.dp),
            )
        }

        if (history.years.isEmpty()) {
            item(key = "empty") { EmptyTimeline() }
        } else {
            items(history.years, key = { it.id.value }) { enrollment ->
                EnrollmentTimelineNode(
                    enrollment = enrollment,
                    isLatest = enrollment.academicYear == latestYear,
                    isLast = enrollment == history.years.last(),
                    onClick = { onOpenYear(enrollment) },
                )
            }
        }
    }
}

@Composable
private fun EmptyTimeline() {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Nessuna iscrizione registrata.",
            fontSize = 14.sp,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorBox(onRetry: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Impossibile caricare le iscrizioni.",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Tocca per riprovare.",
            fontSize = 13.sp,
            color = scheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable(onClick = onRetry)
                .padding(8.dp),
        )
    }
}
