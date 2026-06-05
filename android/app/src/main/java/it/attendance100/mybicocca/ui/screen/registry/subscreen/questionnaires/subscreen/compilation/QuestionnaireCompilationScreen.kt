package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireOption
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnairePage
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireParagraph
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireQuestion
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireQuestionKind
import it.attendance100.mybicocca.ui.component.button.MorphKnob
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadgeTone
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.ext.toDisplayCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionAnswerState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionnaireCompilationEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionnaireCompilationStep
import it.attendance100.mybicocca.ui.screen.registry.theme.registryBadgeTone
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Questionnaire compiler in the Piano di Studi edit language: every question is a
// connected segment group (header tile + answer tiles) with the circle-to-sunny morph
// knob for selection, and the app's connected button pair at the bottom morphing
// Avanti into Conferma at the summary. Pages are server-driven (branching, unknown
// total), so there are no page dots — the bar alone steps the wizard.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuestionnaireCompilationScreen(
    viewModel: QuestionnaireCompilationViewModel,
    onFinished: () -> Unit,
) {
    val step by viewModel.step.collectAsStateWithLifecycle()
    val working by viewModel.working.collectAsStateWithLifecycle()
    val answers by viewModel.answers.collectAsStateWithLifecycle()
    val invalidQuestionIds by viewModel.invalidQuestionIds.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                QuestionnaireCompilationEvent.Confirmed -> {
                    scope.launch { snackbar.showInfo("Questionario inviato. Grazie!") }
                    onFinished()
                }

                QuestionnaireCompilationEvent.MissingAnswers -> scope.launch {
                    snackbar.showInfo("Rispondi alle domande obbligatorie per continuare")
                }

                is QuestionnaireCompilationEvent.Failed -> scope.launch {
                    snackbar.showError("Operazione non riuscita", event.cause)
                }
            }
        }
    }

    val canStepBack = when (val current = step) {
        is QuestionnaireCompilationStep.Page -> current.index > 0
        is QuestionnaireCompilationStep.Summary -> true
        else -> false
    }

    // System back walks the wizard first; leaving from the first page deserves a
    // heads-up because Esse3 never resumes drafts.
    var showDiscardDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = !working && (canStepBack || step is QuestionnaireCompilationStep.Page)) {
        if (canStepBack) viewModel.back() else showDiscardDialog = true
    }
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Uscire senza inviare?") },
            text = { Text("Le risposte date finora non sono state inviate e andranno perse.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onFinished()
                    },
                ) { Text("Esci") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Continua") }
            },
        )
    }

    // Confirmation is definitive on the server, so it stays behind a dialog.
    var showConfirmDialog by remember { mutableStateOf(false) }
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confermare il questionario?") },
            text = { Text("La conferma è definitiva: le risposte non potranno più essere modificate.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.confirm()
                    },
                ) { Text("Conferma") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Annulla") }
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CompilationHeader(
            activityName = viewModel.activityName,
            lecturerName = viewModel.lecturerName,
            partitionName = viewModel.partitionName,
            anonymous = viewModel.anonymous,
        )

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val forward = (targetState as? QuestionnaireCompilationStep.Page)?.index
                    ?.let { target ->
                        val initial = (initialState as? QuestionnaireCompilationStep.Page)?.index
                        initial == null || target >= initial
                    } ?: true
                val direction = if (forward) 1 else -1
                (slideInHorizontally { it / 4 * direction } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it / 4 * direction } + fadeOut())
            },
            contentKey = { current ->
                when (current) {
                    is QuestionnaireCompilationStep.Page -> "page-${current.page.id}"
                    else -> current::class.simpleName
                }
            },
            label = "compilation-step",
            modifier = Modifier.weight(1f),
        ) { currentStep ->
            when (currentStep) {
                QuestionnaireCompilationStep.Starting -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator(modifier = Modifier.size(72.dp))
                }

                is QuestionnaireCompilationStep.StartFailed -> EmptyState(
                    icon = Icons.Outlined.CloudOff,
                    title = "Impossibile iniziare",
                    body = "Il questionario non può essere avviato in questo momento. Riprova.",
                    action = {
                        FilledTonalButton(onClick = viewModel::retryStart) { Text("Riprova") }
                    },
                )

                is QuestionnaireCompilationStep.Page -> PageContent(
                    page = currentStep.page,
                    answers = answers,
                    invalidQuestionIds = invalidQuestionIds,
                    onSelectOption = viewModel::selectOption,
                    onFreeTextChange = viewModel::setFreeText,
                )

                is QuestionnaireCompilationStep.Summary -> SummaryContent(
                    complete = currentStep.complete,
                    anonymous = viewModel.anonymous,
                )
            }
        }

        val summary = step as? QuestionnaireCompilationStep.Summary
        AnimatedVisibility(
            visible = step is QuestionnaireCompilationStep.Page || summary != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            CompilationBottomBar(
                backVisible = canStepBack && !working,
                action = when {
                    working -> CompilationAction.Working
                    summary != null -> CompilationAction.Confirm
                    else -> CompilationAction.Next
                },
                enabled = !working && (summary == null || summary.complete),
                onBack = viewModel::back,
                onPrimary = {
                    if (summary != null) showConfirmDialog = true else viewModel.next()
                },
            )
        }
    }
}

@Composable
private fun CompilationHeader(
    activityName: String,
    lecturerName: String?,
    partitionName: String?,
    anonymous: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 8.dp)) {
        Text(
            text = activityName.toDisplayCase(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
        )
        val subtitle = listOfNotNull(lecturerName, partitionName).joinToString(" · ")
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
        if (anonymous) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Questionario anonimo",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PageContent(
    page: QuestionnairePage,
    answers: Map<Long, QuestionAnswerState>,
    invalidQuestionIds: Set<Long>,
    onSelectOption: (QuestionnaireQuestion, QuestionnaireOption) -> Unit,
    onFreeTextChange: (Long, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        page.paragraphs.forEach { paragraph ->
            paragraphItems(
                paragraph = paragraph,
                answers = answers,
                invalidQuestionIds = invalidQuestionIds,
                onSelectOption = onSelectOption,
                onFreeTextChange = onFreeTextChange,
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.paragraphItems(
    paragraph: QuestionnaireParagraph,
    answers: Map<Long, QuestionAnswerState>,
    invalidQuestionIds: Set<Long>,
    onSelectOption: (QuestionnaireQuestion, QuestionnaireOption) -> Unit,
    onFreeTextChange: (Long, String) -> Unit,
) {
    paragraph.title?.let { title ->
        item(key = "paragraph-${paragraph.id}") {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp),
            )
        }
    }
    items(
        count = paragraph.questions.size,
        key = { "question-${paragraph.questions[it].id}" },
    ) { questionIndex ->
        val question = paragraph.questions[questionIndex]
        QuestionGroup(
            question = question,
            state = answers[question.id] ?: QuestionAnswerState(),
            invalid = question.id in invalidQuestionIds,
            onSelectOption = { option -> onSelectOption(question, option) },
            onFreeTextChange = { text -> onFreeTextChange(question.id, text) },
        )
    }
}

// A question in the segment language: header tile with the mandatory pill, then one
// connected tile per answer. Scale and free-text questions close the group with a
// single input tile instead.
@Composable
private fun QuestionGroup(
    question: QuestionnaireQuestion,
    state: QuestionAnswerState,
    invalid: Boolean,
    onSelectOption: (QuestionnaireOption) -> Unit,
    onFreeTextChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        QuestionHeaderTile(
            question = question,
            answered = question.isAnswered(state),
            invalid = invalid,
        )

        when {
            // Compact numeric scale (e.g. agreement 1..10) rendered as round toggles.
            question.kind == QuestionnaireQuestionKind.Scale &&
                question.options.all { it.text.length <= 2 } -> ScaleTile(
                question = question,
                state = state,
                onSelectOption = onSelectOption,
            )

            question.kind == QuestionnaireQuestionKind.FreeText -> FreeTextTile(
                value = state.freeText,
                onValueChange = onFreeTextChange,
                placeholder = "Scrivi qui…",
                minLines = 3,
            )

            else -> {
                // A selected "Altro"-style option reveals its companion text tile,
                // which then takes over the group's closing corners.
                val companionVisible = question.options.any {
                    it.requiresFreeText && it.id in state.selectedOptionIds
                }
                question.options.forEachIndexed { index, option ->
                    OptionTile(
                        text = option.text.ifBlank { "Altro" },
                        selected = option.id in state.selectedOptionIds,
                        isLast = index == question.options.lastIndex && !companionVisible,
                        onToggle = { onSelectOption(option) },
                    )
                }
                AnimatedVisibility(visible = companionVisible) {
                    FreeTextTile(
                        value = state.freeText,
                        onValueChange = onFreeTextChange,
                        placeholder = "Specifica…",
                        minLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionHeaderTile(
    question: QuestionnaireQuestion,
    answered: Boolean,
    invalid: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
    ) {
        Column(modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = question.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (question.mandatory) {
                    Spacer(Modifier.width(8.dp))
                    MandatoryPill(answered = answered, invalid = invalid)
                }
            }
            question.note?.let { note ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

// The rule-pill language applied to a mandatory question: green with a check once it
// has an answer, warning orange with an x while it doesn't, and the alert red after a
// blocked "Avanti" attempt.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MandatoryPill(answered: Boolean, invalid: Boolean) {
    val motion = MaterialTheme.motionScheme
    val tone = registryBadgeTone(
        when {
            answered -> RegistryBadgeTone.Ok
            invalid -> RegistryBadgeTone.Alert
            else -> RegistryBadgeTone.Attention
        },
    )
    val container by animateColorAsState(
        targetValue = tone.container,
        animationSpec = motion.defaultEffectsSpec(),
        label = "pillContainer",
    )
    val content by animateColorAsState(
        targetValue = tone.onContainer,
        animationSpec = motion.defaultEffectsSpec(),
        label = "pillContent",
    )
    Surface(shape = CircleShape, color = container, contentColor = content) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = answered,
                transitionSpec = {
                    (scaleIn(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec())) togetherWith
                        (scaleOut(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()))
                },
                label = "pillMark",
            ) { ok ->
                Icon(
                    imageVector = if (ok) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Obbligatoria",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

// A connected answer tile: selection lives entirely in the trailing morph knob, the
// tile itself doesn't change.
@Composable
private fun OptionTile(
    text: String,
    selected: Boolean,
    isLast: Boolean,
    onToggle: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val large = 20.dp
    val small = 4.dp
    Surface(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(
            topStart = small,
            topEnd = small,
            bottomStart = if (isLast) large else small,
            bottomEnd = if (isLast) large else small,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            MorphKnob(checked = selected)
        }
    }
}

// The group's closing tile for compact numeric scales: round toggles that wash to the
// brand red (explicit white content) plus the agreement anchors.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScaleTile(
    question: QuestionnaireQuestion,
    state: QuestionAnswerState,
    onSelectOption: (QuestionnaireOption) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                question.options.forEach { option ->
                    val selected = option.id in state.selectedOptionIds
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (selected) scheme.primary else scheme.surfaceContainerHighest)
                            .clickable { onSelectOption(option) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = option.text,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) Color.White else scheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Per niente d'accordo",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Del tutto d'accordo",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun FreeTextTile(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            minLines = minLines,
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SummaryContent(complete: Boolean, anonymous: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(if (complete) scheme.primaryContainer else scheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.TaskAlt,
                contentDescription = null,
                tint = if (complete) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = if (complete) "Questionario completato" else "Mancano alcune risposte",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = when {
                !complete -> "Torna indietro e rispondi alle domande obbligatorie prima di confermare."
                anonymous -> "Conferma per inviare le risposte in forma anonima. Dopo la conferma non potrai più modificarle."
                else -> "Conferma per inviare le risposte. Dopo la conferma non potrai più modificarle."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private enum class CompilationAction { Next, Confirm, Working }

// The app's connected button pair (see StudyPlanEditScreen/EventDetailSheet): an
// icon-only tonal back that springs in once the wizard can step backwards, and the
// primary action morphing between Avanti, Conferma and the in-flight state.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CompilationBottomBar(
    backVisible: Boolean,
    action: CompilationAction,
    enabled: Boolean,
    onBack: () -> Unit,
    onPrimary: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val spatialOffsetSpec = motion.defaultSpatialSpec<IntOffset>()
    val effectsFloatSpec = motion.defaultEffectsSpec<Float>()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AnimatedVisibility(
            visible = backVisible,
            enter = expandHorizontally(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
            exit = shrinkHorizontally(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()),
        ) {
            FilledTonalButton(
                onClick = onBack,
                modifier = Modifier
                    .width(64.dp)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHigh,
                    contentColor = scheme.onSurface,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Pagina precedente",
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // The primary action stays a full pill alone and flattens its start corners
        // when the back button joins the pair.
        val startCorner by animateDpAsState(
            targetValue = if (backVisible) 8.dp else 28.dp,
            animationSpec = motion.defaultSpatialSpec(),
            label = "actionCorner",
        )
        Button(
            onClick = { if (action != CompilationAction.Working) onPrimary() },
            enabled = enabled || action == CompilationAction.Working,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(
                topStart = startCorner,
                bottomStart = startCorner,
                topEnd = 28.dp,
                bottomEnd = 28.dp,
            ),
            // Brand red always pairs with white content, in dark mode too.
            colors = ButtonDefaults.buttonColors(
                containerColor = scheme.primary,
                contentColor = Color.White,
            ),
        ) {
            AnimatedContent(
                targetState = action,
                transitionSpec = {
                    (slideInHorizontally(spatialOffsetSpec) { it / 3 } + fadeIn(effectsFloatSpec)) togetherWith
                        (slideOutHorizontally(spatialOffsetSpec) { -it / 3 } + fadeOut(effectsFloatSpec))
                },
                label = "compilationActionLabel",
            ) { target ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (target) {
                        CompilationAction.Next -> {
                            Text("Avanti", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        CompilationAction.Confirm -> {
                            Text("Conferma", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        CompilationAction.Working -> {
                            LoadingIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Invio in corso…", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// Mirror of the ViewModel's answer check so the pill flips as the user answers.
private fun QuestionnaireQuestion.isAnswered(state: QuestionAnswerState): Boolean = when (kind) {
    QuestionnaireQuestionKind.FreeText -> state.freeText.isNotBlank()
    else -> state.selectedOptionIds.isNotEmpty() && state.selectedOptionIds.all { optionId ->
        val option = options.firstOrNull { it.id == optionId }
        option?.requiresFreeText != true || state.freeText.isNotBlank()
    }
}
