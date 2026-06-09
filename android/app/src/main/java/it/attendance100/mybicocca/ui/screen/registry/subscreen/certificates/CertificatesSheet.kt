package it.attendance100.mybicocca.ui.screen.registry.subscreen.certificates

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.model.document.CertificateType
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.component.modal.SheetOutcome
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.SheetResultPage
import it.attendance100.mybicocca.ui.component.modal.sheetBodyGestureBarrier
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.openPdfDocument
import kotlinx.coroutines.flow.collectLatest

// "Certificati" as a modal sheet, same modal language as the percorso sheet: a pinned
// header over the self-declaration list. Tapping a row downloads the PDF and opens it
// in an external viewer.
// Certificati: a single list page with an in-page download/open outcome. The sheet container is
// owned by BottomSheetSceneStrategy; this entry keeps its OWN header because the outcome result
// page morphs the title in place (so its metadata header is left null).
@Composable
fun CertificatesPage(
    viewModel: CertificatesViewModel,
) {
    val certificatesLoadable by viewModel.certificates.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val downloading by viewModel.downloadingCertificates.collectAsStateWithLifecycle()

    // The VM outlives the sheet (shell-scoped): re-opening shows the cached list instantly
    // while this kicks a background refresh.
    LaunchedEffect(viewModel) { viewModel.refresh() }

    val context = LocalContext.current
    // The outcome of a download/open, shown as a dedicated result page instead of a snackbar.
    var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CertificateEvent.ShowMessage -> outcome = SheetOutcome.Info(event.message)
                is CertificateEvent.OpenPdf ->
                    runCatching { openPdfDocument(context, event.bytes, event.fileName) }
                        .onFailure { outcome = SheetOutcome.Info("Nessuna app per aprire i PDF.") }
            }
        }
    }

    BackHandler(enabled = outcome != null) { outcome = null }

    Column {
        SheetPagerHeader(
            depth = if (outcome != null) 1 else 0,
            title = if (outcome != null) "" else "Certificati",
            subtitle = if (outcome != null) null else "Autocertificazioni in PDF"
                .takeIf { certificatesLoadable is Loadable.Loaded },
            onBack = null,
        )
        AnimatedContent(
            targetState = outcome,
            transitionSpec = { sheetPageTransform(forward = targetState != null) },
            contentKey = { it != null },
            label = "certificates_pages",
        ) { current ->
            if (current != null) {
                SheetResultPage(outcome = current, onDismiss = { outcome = null })
            } else {
                SheetBody(
                    loaded = certificatesLoadable is Loadable.Loaded,
                    certificates = certificatesLoadable.valueOrNull(),
                    syncStatus = syncStatus,
                    downloading = downloading,
                    onRetry = viewModel::refresh,
                    onDownload = viewModel::download,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SheetBody(
    loaded: Boolean,
    certificates: List<Certificate>?,
    syncStatus: SyncStatus,
    downloading: Set<CertificateId>,
    onRetry: () -> Unit,
    onDownload: (Certificate) -> Unit,
) {
    val failure = syncStatus as? SyncStatus.Failed
    // Hold the loading state for a beat so quick fetches don't flash it.
    val showLoading = rememberMinDurationLoading(loading = !loaded)
    val settled = loaded && !showLoading

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    // The sheet only grows/shrinks vertically as content lands — animate the height change
    // here instead of letting the modal snap to the new size. Swipes on the body scroll its
    // content, never the sheet: the header (and drag handle) is the only dismiss surface.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sheetBodyGestureBarrier()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        when {
            failure != null && certificates == null -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = "Caricamento non riuscito",
                body = "Impossibile caricare i certificati.",
                action = { RetryButton(onClick = onRetry) },
            )

            !settled -> SheetLoadingIndicator(label = "Caricamento certificati…")

            certificates.isNullOrEmpty() -> SheetMessage(
                icon = Icons.Rounded.Description,
                title = "Nessun certificato",
                body = "Non risultano certificati disponibili.",
            )

            else -> CertificateList(
                certificates = certificates,
                downloading = downloading,
                onDownload = onDownload,
            )
        }
    }
}

@Composable
private fun CertificateList(
    certificates: List<Certificate>,
    downloading: Set<CertificateId>,
    onDownload: (Certificate) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
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
                onDownload = { onDownload(cert) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CertificateTile(
    certificate: Certificate,
    isFirst: Boolean,
    isLast: Boolean,
    downloading: Boolean,
    onDownload: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val large = 20.dp
    val small = 4.dp
    Surface(
        modifier = Modifier
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
                    text = certificate.description.ifBlank { "Certificato" },
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
            if (downloading) {
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    LoadingIndicator(modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

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
