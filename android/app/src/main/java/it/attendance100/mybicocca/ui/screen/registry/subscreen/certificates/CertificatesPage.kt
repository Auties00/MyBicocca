package it.attendance100.mybicocca.ui.screen.registry.subscreen.certificates

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.model.document.CertificateType
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.component.modal.SheetOutcome
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.SheetResultPage
import it.attendance100.mybicocca.ui.component.modal.sheetBodyGestureBarrier
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import kotlinx.coroutines.flow.collectLatest
import java.io.File

/**
 * "Certificati" sheet body: the self-declaration PDF list as a single page with an
 * in-page download/open outcome. Tapping a row downloads the certificate PDF (or reuses
 * the disk cache) and hands it to an external viewer; download failures and a missing
 * PDF viewer surface as a dedicated in-sheet result page that the title morphs into. The
 * sheet container is owned by BottomSheetSceneStrategy, but this entry keeps its OWN
 * pinned header because of that in-place title morph (its metadata header is left null).
 *
 * The ViewModel outlives the sheet (shell-scoped): re-opening shows the cached list
 * instantly while a background refresh is kicked.
 */
@Composable
fun CertificatesPage(
    viewModel: CertificatesViewModel,
) {
    val strCertsNoPdfApp = stringResource(R.string.certs_no_pdf_app)

    val certificatesLoadable by viewModel.certificates.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val downloading by viewModel.downloadingCertificates.collectAsStateWithLifecycle()
    val downloaded by viewModel.downloadedCertificates.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.refresh() }

    val context = LocalContext.current
    var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CertificateEvent.ShowMessage -> outcome =
                    SheetOutcome.Info(event.message.asString(context))
                is CertificateEvent.OpenFile ->
                    if (!openCertificate(context, File(event.path))) {
                        outcome = SheetOutcome.Info(strCertsNoPdfApp)
                    }
            }
        }
    }

    val seekableState =
        remember { androidx.compose.animation.core.SeekableTransitionState(outcome) }
    val transition = androidx.compose.animation.core.rememberTransition(
        seekableState,
        label = "certificates_pages"
    )

    LaunchedEffect(outcome) {
        if (seekableState.targetState != outcome) {
            seekableState.animateTo(outcome)
        }
    }

    androidx.activity.compose.PredictiveBackHandler(enabled = outcome != null) { progress ->
        try {
            progress.collect { event ->
                seekableState.seekTo(event.progress, targetState = null)
            }
            seekableState.animateTo(null)
            outcome = null
        } catch (_: kotlinx.coroutines.CancellationException) {
            seekableState.animateTo(outcome)
        }
    }

    Column(modifier = Modifier.testTag(CertificatesTestTags.ROOT)) {
        SheetPagerHeader(
            depth = if (outcome != null) 1 else 0,
            title = if (outcome != null) "" else stringResource(R.string.certs_title),
            subtitle = if (outcome != null) null else stringResource(R.string.certs_subtitle)
                .takeIf { certificatesLoadable is Loadable.Loaded },
            onBack = null,
        )
        transition.AnimatedContent(
            transitionSpec = { sheetPageTransform(forward = targetState != null) },
            contentKey = { it != null },
        ) { current ->
            if (current != null) {
                Box(modifier = Modifier.testTag(CertificatesTestTags.RESULT_PAGE)) {
                    SheetResultPage(outcome = current, onDismiss = { outcome = null })
                }
            } else {
                SheetBody(
                    loaded = certificatesLoadable is Loadable.Loaded,
                    certificates = certificatesLoadable.valueOrNull(),
                    syncStatus = syncStatus,
                    downloading = downloading,
                    downloaded = downloaded,
                    onRetry = viewModel::refresh,
                    onDownload = viewModel::download,
                )
            }
        }
    }
}

/**
 * List body states: an error message with retry when the first load failed, a loading
 * indicator (held for a minimum beat so quick fetches don't flash it) until data lands,
 * an empty message when no certificates exist, and otherwise the certificate list.
 * Height changes are animated so the modal never snaps to a new size as content lands,
 * and swipes on the body scroll its content, never the sheet: the header (and drag
 * handle) is the only dismiss surface.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SheetBody(
    loaded: Boolean,
    certificates: List<Certificate>?,
    syncStatus: SyncStatus,
    downloading: Set<CertificateId>,
    downloaded: Set<CertificateId>,
    onRetry: () -> Unit,
    onDownload: (Certificate) -> Unit,
) {
    val failure = syncStatus as? SyncStatus.Failed
    val showLoading = rememberMinDurationLoading(loading = !loaded)
    val settled = loaded && !showLoading

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sheetBodyGestureBarrier()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        when {
            failure != null && certificates == null -> Box(modifier = Modifier.testTag(CertificatesTestTags.STATE_ERROR)) {
                SheetMessage(
                    icon = Icons.Outlined.CloudOff,
                    title = stringResource(R.string.certs_load_failed),
                    body = stringResource(R.string.certs_load_failed_body),
                    action = { RetryButton(onClick = onRetry) },
                )
            }

            !settled -> Box(modifier = Modifier.testTag(CertificatesTestTags.STATE_LOADING)) {
                SheetLoadingIndicator(label = stringResource(R.string.certs_loading))
            }

            certificates.isNullOrEmpty() -> Box(modifier = Modifier.testTag(CertificatesTestTags.STATE_EMPTY)) {
                SheetMessage(
                    icon = Icons.Rounded.Description,
                    title = stringResource(R.string.certs_none),
                    body = stringResource(R.string.certs_none_body),
                )
            }

            else -> CertificateList(
                certificates = certificates,
                downloading = downloading,
                downloaded = downloaded,
                onDownload = onDownload,
            )
        }
    }
}

@Composable
private fun CertificateList(
    certificates: List<Certificate>,
    downloading: Set<CertificateId>,
    downloaded: Set<CertificateId>,
    onDownload: (Certificate) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .testTag(CertificatesTestTags.STATE_CONTENT)
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        itemsIndexed(certificates) { index, cert ->
            CertificateTile(
                certificate = cert,
                isFirst = index == 0,
                isLast = index == certificates.lastIndex,
                downloading = cert.id in downloading,
                downloaded = cert.id in downloaded,
                onDownload = { onDownload(cert) },
                modifier = Modifier.testTag(CertificatesTestTags.tile(cert.id.value)),
            )
        }
    }
}

/**
 * One certificate row: a leading icon chip keyed on the certificate type, the
 * description with a supporting line beneath, and a trailing status that cross-fades
 * with a pop between a spinner while fetching, a brand check once cached on disk, and a
 * download affordance otherwise — the spinner-to-downloaded flip reads as expressive.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CertificateTile(
    certificate: Certificate,
    isFirst: Boolean,
    isLast: Boolean,
    downloading: Boolean,
    downloaded: Boolean,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val large = 20.dp
    val small = 4.dp
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !downloading, onClick = onDownload),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(
            topStart = if (isFirst) large else small,
            topEnd = if (isFirst) large else small,
            bottomStart = if (isLast) large else small,
            bottomEnd = if (isLast) large else small,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(scheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = certificate.type.icon(),
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = certificate.description.ifBlank { stringResource(R.string.certs_certificate) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                certificate.supportingLine()?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            val status = when {
                downloading -> CertificateStatus.Downloading
                downloaded -> CertificateStatus.Downloaded
                else -> CertificateStatus.Available
            }
            AnimatedContent(
                targetState = status,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.7f)) togetherWith fadeOut() },
                label = "certificate_status",
            ) { current ->
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    when (current) {
                        CertificateStatus.Downloading -> LoadingIndicator(modifier = Modifier.size(36.dp))
                        CertificateStatus.Downloaded -> Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = stringResource(R.string.certs_downloaded),
                            tint = scheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        CertificateStatus.Available -> Icon(
                            imageVector = Icons.Rounded.FileDownload,
                            contentDescription = stringResource(R.string.certs_download),
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

private enum class CertificateStatus { Downloading, Downloaded, Available }

private fun CertificateType.icon() = when (this) {
    CertificateType.Enrolment -> Icons.Rounded.School
    CertificateType.DegreeAward -> Icons.Rounded.WorkspacePremium
    CertificateType.TuitionFees -> Icons.Rounded.Receipt
    CertificateType.Other -> Icons.Rounded.Description
}

private fun Certificate.supportingLine(): String? {
    val parts = buildList {
        solarYear?.let { add("Anno $it") }
        if (digitallySigned) add("Firma digitale")
    }
    return parts.joinToString(" · ").takeIf { it.isNotBlank() }
}
