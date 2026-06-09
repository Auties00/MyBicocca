package it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentHistory
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentId
import it.attendance100.mybicocca.domain.model.enrollment.RenewalState
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.component.EnrollmentRenewalButton
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.component.EnrollmentRow

// "Iscrizioni" timeline — the root page of the Iscrizioni sheet (annual-enrollment timeline).
// The sheet container, pinned morphing header and the timeline -> year-detail page transition
// are now owned by BottomSheetSceneStrategy: this is just the root entry's body. The year detail
// is a separate back-stack entry (SheetRoute.EnrollmentDetail rendering EnrollmentDetailPage).
@Composable
fun EnrollmentsTimelinePage(
    viewModel: EnrollmentsViewModel,
    onOpenDetail: (EnrollmentId) -> Unit,
) {
    val context = LocalContext.current
    val historyLoadable by viewModel.history.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    // The VM outlives the sheet (shell-scoped). The first open is loaded by the VM's own career
    // collector; a re-open shows the cached snapshot instantly while this kicks a refresh.
    LaunchedEffect(viewModel) {
        if (viewModel.history.value is Loadable.Loaded) viewModel.refresh()
    }

    LaunchedEffect(viewModel) {
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

    TimelinePage(
        history = historyLoadable.valueOrNull(),
        syncStatus = syncStatus,
        onRetry = viewModel::refresh,
        onRenew = viewModel::renew,
        onOpenDetail = { onOpenDetail(it.id) },
    )
}

// "5 anni accademici": the timeline depth, shown as the header subtitle. Public so MainShell's
// sheet entry can build the pinned header from the shell-hoisted ViewModel's history.
fun enrollmentsHeaderSubtitle(history: EnrollmentHistory): String? {
    val count = history.years.size
    if (count == 0) return null
    return if (count == 1) "1 anno accademico" else "$count anni accademici"
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TimelinePage(
    history: EnrollmentHistory?,
    syncStatus: SyncStatus,
    onRetry: () -> Unit,
    onRenew: () -> Unit,
    onOpenDetail: (AnnualEnrollment) -> Unit,
) {
    val failure = syncStatus as? SyncStatus.Failed
    // Hold the loading state for a beat so quick fetches don't flash it.
    val showLoading = rememberMinDurationLoading(loading = history == null)
    val settled = history != null && !showLoading

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    // The sheet only grows/shrinks vertically as content lands — animate the height change
    // here instead of letting the modal snap to the new size.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        when {
            failure != null && history == null -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = "Caricamento non riuscito",
                body = "Impossibile caricare le iscrizioni.",
                action = { RetryButton(onClick = onRetry) },
            )

            !settled || history == null -> SheetLoadingIndicator(label = "Caricamento iscrizioni…")

            else -> {
                // The renewal action lives below the list as the sheet's pinned footer,
                // not as a scrolling item.
                val hasFooter = history.renewal != RenewalState.NotApplicable
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 560.dp)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = if (hasFooter) 8.dp else 24.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (history.years.isEmpty()) {
                        item(key = "empty") {
                            SheetMessage(
                                icon = Icons.Outlined.School,
                                title = "Nessuna iscrizione",
                                body = "Non risultano iscrizioni registrate.",
                            )
                        }
                    } else {
                        itemsIndexed(history.years, key = { _, it -> it.id.value }) { index, enrollment ->
                            EnrollmentRow(
                                enrollment = enrollment,
                                isFirst = index == 0,
                                isLast = index == history.years.lastIndex,
                                onClick = { onOpenDetail(enrollment) },
                            )
                        }
                    }
                }
                if (hasFooter) {
                    EnrollmentRenewalButton(
                        state = history.renewal,
                        onRenew = onRenew,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 20.dp),
                    )
                }
            }
        }
    }
}
