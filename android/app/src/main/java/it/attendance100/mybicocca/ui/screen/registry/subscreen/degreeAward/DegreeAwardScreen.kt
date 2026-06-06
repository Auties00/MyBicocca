package it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationHub
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationStage
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.component.CommitteeSessionCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.component.GraduationStatusHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.component.GraduationStepRow
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.state.DegreeAwardEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.state.GraduationStep
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.state.StepStatus
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.state.steps
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.subscreen.applicationSheet.ApplicationSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.subscreen.attachmentsSheet.AttachmentsSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.subscreen.consultationSheet.ConsultationSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.subscreen.supervisorsSheet.SupervisorsSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.subscreen.thesisSheet.ThesisSheet
import kotlinx.coroutines.flow.collectLatest
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val ALMALAUREA_URL = "https://www.almalaurea.it/lau/"
private const val ESSE3_PORTAL_URL = "https://s3w.si.unimib.it/esse3/"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DegreeAwardScreen(
    viewModel: DegreeAwardViewModel = hiltViewModel(),
) {
    val hubData by viewModel.hub.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val actionInProgress by viewModel.actionInProgress.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val searching by viewModel.searching.collectAsStateWithLifecycle()
    val discussionModes by viewModel.discussionModes.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    val context = LocalContext.current
    val openInAppBrowser: (String) -> Unit = remember(context) {
        { url -> CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, url.toUri()) }
    }

    // Which action sheet (if any) is open. Re-pulling the hub after a successful mutation
    // also dismisses the sheet via the event handler below.
    var openSheet by remember { mutableStateOf<GraduationStep?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DegreeAwardEvent.ShowMessage -> snackbar.showInfo(event.message)
                is DegreeAwardEvent.ShowError -> snackbar.showError(event.message)
                is DegreeAwardEvent.OpenUrl -> openInAppBrowser(event.url)
                DegreeAwardEvent.ApplicationSubmitted -> {
                    openSheet = null
                    snackbar.showInfo("Domanda di laurea presentata")
                }
                DegreeAwardEvent.ThesisSubmitted -> {
                    openSheet = null
                    snackbar.showInfo("Dati della tesi salvati")
                }
                DegreeAwardEvent.SupervisorsAssigned -> {
                    openSheet = null
                    snackbar.showInfo("Relatori assegnati")
                }
                DegreeAwardEvent.DiscussionModeSet -> {
                    openSheet = null
                    snackbar.showInfo("Modalità di consultazione aggiornata")
                }
                DegreeAwardEvent.ApplicationCancelled -> {
                    showCancelDialog = false
                    snackbar.showInfo("Domanda annullata")
                }
            }
        }
    }

    val hub = hubData.valueOrNull()

    when {
        hub == null -> when (val status = syncStatus) {
            is SyncStatus.Failed -> ErrorEmptyState(cause = status.cause, onRetry = viewModel::refresh)
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator(modifier = Modifier.size(72.dp))
            }
        }

        else -> GraduationHubContent(
            hub = hub,
            actionInProgress = actionInProgress,
            onOpenStep = { step -> openSheet = step },
            onCancelApplication = { showCancelDialog = true },
            onOpenAlmaLaurea = { openInAppBrowser(ALMALAUREA_URL) },
        )
    }

    // Action sheets, each gated by the corresponding hub state already enforced by the step
    // statuses. Opening one for a locked step is impossible (the row is disabled).
    when (openSheet) {
        GraduationStep.Application -> ApplicationSheet(
            calls = hub?.openCalls.orEmpty(),
            submitting = actionInProgress,
            onSubmit = viewModel::applyToCall,
            onDismiss = { openSheet = null },
        )

        GraduationStep.Thesis -> ThesisSheet(
            existing = hub?.thesis,
            thesisTypes = hub?.thesisTypes.orEmpty(),
            submitting = actionInProgress,
            onSubmit = viewModel::saveThesis,
            onDismiss = { openSheet = null },
        )

        GraduationStep.Supervisors -> SupervisorsSheet(
            results = searchResults,
            searching = searching,
            submitting = actionInProgress,
            onQueryChange = viewModel::searchSupervisors,
            onSubmit = viewModel::assignSupervisors,
            onDismiss = {
                viewModel.clearSearch()
                openSheet = null
            },
        )

        GraduationStep.Attachments -> AttachmentsSheet(
            attachments = hub?.thesis?.attachments.orEmpty(),
            onOpenPortal = { openInAppBrowser(ESSE3_PORTAL_URL) },
            onDismiss = { openSheet = null },
        )

        GraduationStep.Consultation -> {
            LaunchedEffect(Unit) { viewModel.loadDiscussionModes() }
            ConsultationSheet(
                modes = discussionModes,
                currentCode = hub?.thesis?.discussionModeCode,
                submitting = actionInProgress,
                onSubmit = viewModel::setDiscussionMode,
                onDismiss = { openSheet = null },
            )
        }

        null -> Unit
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = { Icon(Icons.Outlined.Cancel, null) },
            title = { Text("Annullare la domanda?") },
            text = { Text("La domanda di laurea verrà annullata. Questa operazione è irreversibile.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.cancelApplication() },
                    enabled = !actionInProgress,
                ) { Text("Annulla domanda") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Indietro") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GraduationHubContent(
    hub: GraduationHub,
    actionInProgress: Boolean,
    onOpenStep: (GraduationStep) -> Unit,
    onCancelApplication: () -> Unit,
    onOpenAlmaLaurea: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val steps = remember(hub) { hub.steps() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GraduationStatusHeader(
            stage = hub.stage,
            application = hub.application,
            result = hub.result,
        )

        if (hub.stage == GraduationStage.NotOpen) {
            EmptyState(
                icon = Icons.Default.School,
                title = "Nessuna domanda attiva",
                body = "Non sei ancora nella finestra di conseguimento titolo. Quando un appello di laurea sarà disponibile per il tuo corso, potrai presentare la domanda da qui.",
                modifier = Modifier.height(360.dp),
            )
            return@Column
        }

        // The step checklist: domanda → tesi → relatori → allegati → consultazione.
        Text(
            text = "Percorso",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            steps.forEachIndexed { index, stepState ->
                GraduationStepRow(
                    state = stepState,
                    isFirst = index == 0,
                    isLast = index == steps.lastIndex,
                    onClick = { onOpenStep(stepState.step) },
                )
            }
        }

        // The seduta, once the secretariat has scheduled it.
        hub.session?.let { session ->
            CommitteeSessionCard(session = session)
        }

        // AlmaLaurea hand-off: the questionnaire lives on the AlmaLaurea portal.
        AlmaLaureaCard(onClick = onOpenAlmaLaurea)

        // Cancelling is only offered while the application is still cancellable.
        if (hub.application?.isCancellable == true) {
            OutlinedButton(
                onClick = onCancelApplication,
                enabled = !actionInProgress,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.Cancel, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Annulla domanda")
            }
        }
    }
}

@Composable
private fun AlmaLaureaCard(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = scheme.tertiaryContainer,
        contentColor = scheme.onTertiaryContainer,
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Questionario AlmaLaurea",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "La compilazione del questionario AlmaLaurea è obbligatoria per laurearti. Si completa sul portale AlmaLaurea.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.width(12.dp))
            Icon(Icons.AutoMirrored.Filled.OpenInNew, null)
        }
    }
}

@Composable
private fun ErrorEmptyState(cause: Throwable, onRetry: () -> Unit) {
    EmptyState(
        icon = Icons.Default.Warning,
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
    else -> "Si è verificato un errore imprevisto"
}
