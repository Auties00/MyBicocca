package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireOption
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnairePage
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireParagraph
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireQuestion
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireQuestionKind
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.ext.toDisplayCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionAnswerState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionnaireCompilationEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionnaireCompilationStep
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
            modifier = Modifier.fillMaxSize(),
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
                    index = currentStep.index,
                    answers = answers,
                    invalidQuestionIds = invalidQuestionIds,
                    working = working,
                    onSelectOption = viewModel::selectOption,
                    onFreeTextChange = viewModel::setFreeText,
                    onBack = viewModel::back,
                    onNext = viewModel::next,
                )

                is QuestionnaireCompilationStep.Summary -> SummaryContent(
                    complete = currentStep.complete,
                    anonymous = viewModel.anonymous,
                    working = working,
                    onBack = viewModel::back,
                    onConfirm = viewModel::confirm,
                )
            }
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
    index: Int,
    answers: Map<Long, QuestionAnswerState>,
    invalidQuestionIds: Set<Long>,
    working: Boolean,
    onSelectOption: (QuestionnaireQuestion, QuestionnaireOption) -> Unit,
    onFreeTextChange: (Long, String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (index > 0) {
                OutlinedButton(
                    onClick = onBack,
                    enabled = !working,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Indietro")
                }
            }
            Button(
                onClick = onNext,
                enabled = !working,
                modifier = Modifier.weight(if (index > 0) 1f else 2f),
            ) {
                if (working) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Avanti")
                }
            }
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
        QuestionCard(
            question = question,
            state = answers[question.id] ?: QuestionAnswerState(),
            invalid = question.id in invalidQuestionIds,
            onSelectOption = { option -> onSelectOption(question, option) },
            onFreeTextChange = { text -> onFreeTextChange(question.id, text) },
        )
    }
}

@Composable
private fun QuestionCard(
    question: QuestionnaireQuestion,
    state: QuestionAnswerState,
    invalid: Boolean,
    onSelectOption: (QuestionnaireOption) -> Unit,
    onFreeTextChange: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = scheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (invalid) {
                    Modifier.border(1.5.dp, scheme.error, RoundedCornerShape(20.dp))
                } else {
                    Modifier
                },
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Text(
                    text = question.text,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                if (question.mandatory) {
                    Text(
                        text = "*",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (invalid) scheme.error else scheme.primary,
                    )
                }
            }
            question.note?.let { note ->
                Spacer(Modifier.height(2.dp))
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))

            when (question.kind) {
                QuestionnaireQuestionKind.Scale -> if (question.options.all { it.text.length <= 2 }) {
                    ScaleOptions(question = question, state = state, onSelectOption = onSelectOption)
                } else {
                    ChoiceOptions(question = question, state = state, onSelectOption = onSelectOption)
                }

                QuestionnaireQuestionKind.FreeText -> OutlinedTextField(
                    value = state.freeText,
                    onValueChange = onFreeTextChange,
                    minLines = 3,
                    placeholder = { Text("Scrivi qui…") },
                    modifier = Modifier.fillMaxWidth(),
                )

                else -> ChoiceOptions(question = question, state = state, onSelectOption = onSelectOption)
            }

            // A selected "Altro"-style option reveals its companion text field.
            val freeTextOptionSelected = question.options.any {
                it.requiresFreeText && it.id in state.selectedOptionIds
            }
            AnimatedVisibility(visible = freeTextOptionSelected) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = state.freeText,
                        onValueChange = onFreeTextChange,
                        placeholder = { Text("Specifica…") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoiceOptions(
    question: QuestionnaireQuestion,
    state: QuestionAnswerState,
    onSelectOption: (QuestionnaireOption) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        question.options.forEachIndexed { index, option ->
            val selected = option.id in state.selectedOptionIds
            val first = index == 0
            val last = index == question.options.lastIndex
            Surface(
                onClick = { onSelectOption(option) },
                shape = RoundedCornerShape(
                    topStart = if (first) 14.dp else 5.dp,
                    topEnd = if (first) 14.dp else 5.dp,
                    bottomStart = if (last) 14.dp else 5.dp,
                    bottomEnd = if (last) 14.dp else 5.dp,
                ),
                color = if (selected) scheme.secondaryContainer else scheme.surfaceContainerHigh,
                contentColor = if (selected) scheme.onSecondaryContainer else scheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = option.text.ifBlank { "Altro" },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    if (selected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

// Compact numeric scale (e.g. agreement 1..10) rendered as round toggles.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScaleOptions(
    question: QuestionnaireQuestion,
    state: QuestionAnswerState,
    onSelectOption: (QuestionnaireOption) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
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
                    color = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                )
            }
        }
    }
    Spacer(Modifier.height(6.dp))
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

@Composable
private fun SummaryContent(
    complete: Boolean,
    anonymous: Boolean,
    working: Boolean,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var confirmDialog by remember { mutableStateOf(false) }

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
                .clip(summaryShape())
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
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, enabled = !working) {
                Text("Rivedi")
            }
            Button(
                onClick = { confirmDialog = true },
                enabled = complete && !working,
            ) {
                if (working) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Conferma")
                }
            }
        }
    }

    if (confirmDialog) {
        AlertDialog(
            onDismissRequest = { confirmDialog = false },
            title = { Text("Confermare il questionario?") },
            text = { Text("La conferma è definitiva: le risposte non potranno più essere modificate.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDialog = false
                        onConfirm()
                    },
                ) {
                    Text("Conferma")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDialog = false }) { Text("Annulla") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun summaryShape() = MaterialShapes.Cookie9Sided.toShape()
