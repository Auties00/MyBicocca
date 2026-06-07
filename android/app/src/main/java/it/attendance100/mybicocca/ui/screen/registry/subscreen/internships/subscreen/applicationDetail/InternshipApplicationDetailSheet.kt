package it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.subscreen.applicationDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.internship.InternshipApplication
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationDetail
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationState
import it.attendance100.mybicocca.domain.model.internship.InternshipAttachment
import it.attendance100.mybicocca.domain.model.internship.InternshipEvaluationWindow
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.ext.displayName
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.ext.displayTitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.ext.durationLine
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.ext.formatDateRange
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.ext.sizeLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.ext.stateLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.ext.toShortLabel
import java.time.LocalDate

// Bottom sheet with the full training project of one domanda di tirocinio: period,
// progetto formativo, tutors, convention, evaluation windows and attachments.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InternshipApplicationDetailSheet(
    application: InternshipApplication,
    detail: Loadable<InternshipApplicationDetail>,
    attachments: Loadable<List<InternshipAttachment>>,
    detailStatus: SyncStatus,
    onRetry: () -> Unit,
    onWithdraw: () -> Unit,
    onRegisterHours: () -> Unit,
    onSignProject: () -> Unit,
    onUpload: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet(onDismiss = onDismiss) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
        ) {
            Text(
                text = application.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = listOfNotNull(application.typeDescription, application.stateLabel)
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))

            when (detail) {
                Loadable.NotYetLoaded -> when (detailStatus) {
                    is SyncStatus.Failed -> SheetError(onRetry = onRetry)

                    else -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(56.dp))
                    }
                }

                is Loadable.Loaded -> SheetDetail(
                    detail = detail.value,
                    attachments = attachments,
                )
            }

            ActionsSection(
                application = application,
                trainingProject = (detail as? Loadable.Loaded)?.value?.trainingProject,
                onWithdraw = onWithdraw,
                onRegisterHours = onRegisterHours,
                onSignProject = onSignProject,
                onUpload = onUpload,
            )
        }
    }
}

// Tirocini management actions. Shown by application state; the flows themselves are not yet
// wired (no student REST endpoint — pending Esse3LegacyApi), so tapping reports "a breve".
@Composable
private fun ActionsSection(
    application: InternshipApplication,
    trainingProject: it.attendance100.mybicocca.domain.model.internship.InternshipTrainingProject?,
    onWithdraw: () -> Unit,
    onRegisterHours: () -> Unit,
    onSignProject: () -> Unit,
    onUpload: () -> Unit,
) {
    val state = application.state
    val terminal = state in setOf(
        InternshipApplicationState.Closed,
        InternshipApplicationState.Cancelled,
        InternshipApplicationState.Rejected,
        InternshipApplicationState.Unknown,
    )
    val canWithdraw = state in setOf(
        InternshipApplicationState.Submitted,
        InternshipApplicationState.Confirmed,
        InternshipApplicationState.NotAssigned,
    )
    val canRegisterHours = state == InternshipApplicationState.Started
    val canUpload = state in setOf(
        InternshipApplicationState.Submitted,
        InternshipApplicationState.Confirmed,
        InternshipApplicationState.Started,
    )
    val canSign = !terminal && trainingProject != null && !trainingProject.acceptedByStudent

    if (!(canWithdraw || canRegisterHours || canUpload || canSign)) return

    Spacer(Modifier.height(20.dp))
    SheetSection(title = "Azioni") {
        if (canSign) ActionButton("Firma progetto formativo", onSignProject)
        if (canRegisterHours) ActionButton("Registra ore", onRegisterHours)
        if (canUpload) ActionButton("Carica allegato", onUpload)
        if (canWithdraw) ActionButton("Ritira candidatura", onWithdraw, destructive = true)
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit, destructive: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (destructive) {
            androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.errorContainer,
                contentColor = scheme.onErrorContainer,
            )
        } else {
            androidx.compose.material3.ButtonDefaults.filledTonalButtonColors()
        },
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SheetDetail(
    detail: InternshipApplicationDetail,
    attachments: Loadable<List<InternshipAttachment>>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        PeriodSection(detail)
        TrainingProjectSection(detail)
        TutorsSection(detail)
        ConventionSection(detail)
        EvaluationsSection(detail)
        AttachmentsSection(attachments)
    }
}

@Composable
private fun PeriodSection(detail: InternshipApplicationDetail) {
    val rows = buildList {
        formatDateRange(detail.startDate, detail.endDate)?.let { add("Periodo" to it) }
        detail.periodDescription?.let { add("Tipo di periodo" to it) }
        detail.durationLine?.let { add("Durata" to it) }
        detail.expectedSchedule?.let { add("Orario previsto" to it) }
        detail.opportunitySite?.let { add("Sede" to it) }
        detail.stageTypeDescription?.let { add("Tipologia" to it) }
        detail.areaDescription?.let { add("Area" to it) }
        detail.sectorDescription?.let { add("Settore" to it) }
        detail.workInsertionDescription?.let { add("Inserimento lavorativo" to it) }
        detail.facilitations?.let { add("Facilitazioni" to it) }
        if (detail.cfuRecognitionEnabled) add("Riconoscimento CFU" to "Previsto")
    }
    if (rows.isEmpty()) return
    SheetSection(title = "Tirocinio") {
        rows.forEach { (label, value) -> LabeledValue(label, value) }
    }
}

@Composable
private fun TrainingProjectSection(detail: InternshipApplicationDetail) {
    val project = detail.trainingProject
    val acceptances = listOf(
        Triple("Accettazione studente", project.acceptedByStudent, project.studentAcceptanceDate),
        Triple("Accettazione azienda", project.acceptedByCompany, project.companyAcceptanceDate),
        Triple("Manleva", project.liabilityWaiverAccepted, project.liabilityWaiverDate),
    )
    val texts = buildList {
        project.objectives?.let { add("Obiettivi formativi" to it) }
        project.activities?.let { add("Attività" to it) }
        project.expectedSkills?.let { add("Competenze attese" to it) }
        project.acquiredSkills?.let { add("Competenze acquisite" to it) }
        project.trainingContent?.let { add("Contenuti della formazione" to it) }
        project.learningVerificationMode?.let { add("Verifica dell'apprendimento" to it) }
    }
    SheetSection(title = "Progetto formativo") {
        project.stateDescription?.let { LabeledValue("Stato", it) }
        acceptances.forEach { (label, accepted, date) ->
            AcceptanceRow(label = label, accepted = accepted, date = date)
        }
        texts.forEach { (label, value) ->
            Spacer(Modifier.height(4.dp))
            LabeledParagraph(label, value)
        }
    }
}

@Composable
private fun TutorsSection(detail: InternshipApplicationDetail) {
    val rows = buildList {
        detail.academicTutor?.let { add("Tutor accademico" to it.fullName) }
        detail.companyTutor?.let { add("Tutor aziendale" to it.fullName) }
    }
    if (rows.isEmpty()) return
    SheetSection(title = "Tutor") {
        rows.forEach { (label, value) -> LabeledValue(label, value) }
    }
}

@Composable
private fun ConventionSection(detail: InternshipApplicationDetail) {
    val rows = buildList {
        detail.conventionDescription?.let { add("Convenzione" to it) }
        formatDateRange(detail.conventionStartDate, detail.conventionEndDate)?.let { add("Validità" to it) }
        detail.protocolNumber?.let { add("Protocollo" to it) }
        detail.opportunityTitle?.let { add("Opportunità" to it) }
        detail.opportunityDescription?.let { add("Descrizione opportunità" to it) }
    }
    if (rows.isEmpty()) return
    SheetSection(title = "Convenzione") {
        rows.forEach { (label, value) -> LabeledValue(label, value) }
    }
}

@Composable
private fun EvaluationsSection(detail: InternshipApplicationDetail) {
    val windows = listOf(
        "Valutazione finale" to detail.finalEvaluation,
        "Valutazione intermedia" to detail.midtermEvaluation,
        "Relazione finale" to detail.finalReport,
    ).filter { (_, window) -> window.enabled }
    if (windows.isEmpty()) return
    SheetSection(title = "Valutazioni") {
        windows.forEach { (label, window) ->
            LabeledValue(label, window.rangeLabel())
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "La compilazione delle valutazioni è disponibile sul portale Esse3.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun InternshipEvaluationWindow.rangeLabel(): String =
    formatDateRange(start, end) ?: "Prevista"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AttachmentsSection(attachments: Loadable<List<InternshipAttachment>>) {
    SheetSection(title = "Allegati") {
        when (attachments) {
            Loadable.NotYetLoaded -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center,
            ) {
                LoadingIndicator(modifier = Modifier.size(32.dp))
            }

            is Loadable.Loaded -> {
                val items = attachments.value
                if (items.isEmpty()) {
                    Text(
                        text = "Nessun allegato presente sulla domanda.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items.forEach { attachment -> AttachmentRow(attachment) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentRow(attachment: InternshipAttachment) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(scheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = scheme.onSecondaryContainer,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = attachment.displayTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = listOfNotNull(
                attachment.fileName.takeIf { it != attachment.displayTitle },
                attachment.sizeLabel(),
                attachment.modifiedOn?.toShortLabel(),
            ).joinToString(" · ")
            if (meta.isNotEmpty()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AcceptanceRow(label: String, accepted: Boolean, date: LocalDate?) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (accepted) Icons.Rounded.Check else Icons.Rounded.HourglassBottom,
            contentDescription = null,
            tint = if (accepted) scheme.primary else scheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = when {
                accepted && date != null -> date.toShortLabel()
                accepted -> "Accettato"
                else -> "In attesa"
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (accepted) scheme.primary else scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SheetSection(
    title: String,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = scheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Row {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.width(140.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = scheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LabeledParagraph(label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurface,
        )
    }
}

@Composable
private fun SheetError(onRetry: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Impossibile caricare il dettaglio di questo tirocinio.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        FilledTonalButton(onClick = onRetry) { Text("Riprova") }
    }
}
