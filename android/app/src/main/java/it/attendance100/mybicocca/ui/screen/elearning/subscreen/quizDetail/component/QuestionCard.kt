package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptQuestion
import it.attendance100.mybicocca.ui.component.text.HtmlBody
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.ChoiceOption
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.ClozeSegment
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.ParsedQuestion
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.QuestionUiModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.ReviewMark

// Renders one parsed question, both while answering and in read-only review. Every
// interaction emits the FULL field map for the slot (controls + hidden base fields),
// which is exactly the payload mod_quiz_save_attempt / process_attempt expect.
@Composable
fun QuestionCard(
    question: AttemptQuestion,
    parsed: ParsedQuestion,
    answerFields: Map<String, String>,
    flagged: Boolean,
    readOnly: Boolean,
    onAnswer: (Map<String, String>) -> Unit,
    onToggleFlag: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = scheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DOMANDA ${question.slot}",
                    color = scheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.4.sp,
                    modifier = Modifier.weight(1f),
                )
                if (readOnly) {
                    VerdictBadge(question)
                } else if (onToggleFlag != null) {
                    IconButton(onClick = onToggleFlag, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (flagged) Icons.Rounded.Flag else Icons.Outlined.Flag,
                            contentDescription = if (flagged) "Rimuovi contrassegno" else "Contrassegna domanda",
                            tint = if (flagged) scheme.tertiary else scheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            if (readOnly && question.mark != null && question.maxMark != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Punteggio ${formatGradeValue(question.mark)} su ${formatGradeValue(question.maxMark)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                )
            }
            Spacer(Modifier.height(10.dp))

            when (val model = parsed.model) {
                is QuestionUiModel.SingleChoice -> {
                    Prompt(model.promptHtml)
                    Spacer(Modifier.height(12.dp))
                    val selected = answerFields[model.fieldName]
                        ?: model.options.firstOrNull { it.initiallySelected }?.value.orEmpty()
                    ChoiceList(
                        options = model.options,
                        isSelected = { it.value == selected },
                        readOnly = readOnly,
                        onSelect = { option ->
                            onAnswer(model.baseFields + (model.fieldName to option.value))
                        },
                    )
                }

                is QuestionUiModel.MultiChoice -> {
                    Prompt(model.promptHtml)
                    Spacer(Modifier.height(12.dp))
                    val checked = { option: ChoiceOption ->
                        (answerFields[option.fieldName] ?: if (option.initiallySelected) "1" else "0") == "1"
                    }
                    ChoiceList(
                        options = model.options,
                        isSelected = checked,
                        readOnly = readOnly,
                        onSelect = { option ->
                            val updated = model.options.associate { o ->
                                val value = if (o.fieldName == option.fieldName) {
                                    if (checked(o)) "0" else "1"
                                } else {
                                    if (checked(o)) "1" else "0"
                                }
                                o.fieldName to value
                            }
                            onAnswer(model.baseFields + updated)
                        },
                    )
                }

                is QuestionUiModel.TextEntry -> {
                    Prompt(model.promptHtml)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = answerFields[model.fieldName] ?: model.initialValue,
                        onValueChange = { onAnswer(model.baseFields + (model.fieldName to it)) },
                        readOnly = readOnly,
                        singleLine = true,
                        placeholder = { Text("Risposta…") },
                        keyboardOptions = if (model.numeric) {
                            KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        } else {
                            KeyboardOptions.Default
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is QuestionUiModel.Essay -> {
                    Prompt(model.promptHtml)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = answerFields[model.fieldName] ?: model.initialValue,
                        onValueChange = { onAnswer(model.baseFields + (model.fieldName to it)) },
                        readOnly = readOnly,
                        minLines = 5,
                        placeholder = { Text("Scrivi qui la tua risposta…") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is QuestionUiModel.Cloze -> ClozeContent(
                    model = model,
                    answerFields = answerFields,
                    readOnly = readOnly,
                    onAnswer = onAnswer,
                )

                is QuestionUiModel.Unsupported -> {
                    Prompt(model.promptHtml)
                    if (!readOnly) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Questo tipo di domanda non è supportato nell'app: rispondi dal sito e-learning.",
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.error,
                            fontStyle = FontStyle.Italic,
                        )
                    }
                }
            }

            if (readOnly) {
                parsed.rightAnswerHtml?.let { ReviewNote(title = "Risposta corretta", html = it, accent = true) }
                parsed.feedbackHtml?.let { ReviewNote(title = "Feedback", html = it, accent = false) }
            }
        }
    }
}

@Composable
private fun Prompt(html: String) {
    if (html.isNotBlank()) {
        HtmlBody(html = html, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ChoiceList(
    options: List<ChoiceOption>,
    isSelected: (ChoiceOption) -> Boolean,
    readOnly: Boolean,
    onSelect: (ChoiceOption) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        options.forEachIndexed { index, option ->
            val selected = isSelected(option)
            val first = index == 0
            val last = index == options.lastIndex
            val (container, content) = when {
                readOnly && option.reviewMark == ReviewMark.Correct ->
                    scheme.primaryContainer to scheme.onPrimaryContainer
                readOnly && option.reviewMark == ReviewMark.Incorrect ->
                    scheme.errorContainer to scheme.onErrorContainer
                selected -> scheme.secondaryContainer to scheme.onSecondaryContainer
                else -> scheme.surfaceContainerHigh to scheme.onSurface
            }
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (first) 14.dp else 5.dp,
                    topEnd = if (first) 14.dp else 5.dp,
                    bottomStart = if (last) 14.dp else 5.dp,
                    bottomEnd = if (last) 14.dp else 5.dp,
                ),
                color = container,
                contentColor = content,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (readOnly) Modifier else Modifier.clickable { onSelect(option) }),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        HtmlBody(html = option.labelHtml, color = content)
                    }
                    when {
                        readOnly && option.reviewMark == ReviewMark.Correct -> ChoiceMark(Icons.Rounded.Check, scheme.primary)
                        readOnly && option.reviewMark == ReviewMark.Incorrect -> ChoiceMark(Icons.Rounded.Close, scheme.error)
                        selected -> ChoiceMark(Icons.Rounded.Check, scheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceMark(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Spacer(Modifier.width(8.dp))
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(18.dp),
    )
}

// Cloze: text chunks flow vertically with their gaps inline-ish below each chunk.
// Real Bicocca cloze questions are line-oriented (one gap per formula line), so the
// linearized layout reads naturally.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClozeContent(
    model: QuestionUiModel.Cloze,
    answerFields: Map<String, String>,
    readOnly: Boolean,
    onAnswer: (Map<String, String>) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    fun currentValue(fieldName: String, initial: String): String =
        answerFields[fieldName] ?: initial

    // Every gap field ships on each change, so partially-filled cloze rows persist whole.
    fun emit(changedField: String, newValue: String) {
        val gapFields = model.segments
            .filterIsInstance<ClozeSegment.TextGap>()
            .associate { it.fieldName to currentValue(it.fieldName, it.initialValue) } +
            model.segments
                .filterIsInstance<ClozeSegment.MenuGap>()
                .associate { it.fieldName to currentValue(it.fieldName, it.initialValue) }
        onAnswer(model.baseFields + gapFields + (changedField to newValue))
    }

    var gapNumber = 0
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        model.segments.forEach { segment ->
            when (segment) {
                is ClozeSegment.TextChunk -> HtmlBody(html = segment.html, color = scheme.onSurface)

                is ClozeSegment.TextGap -> {
                    gapNumber++
                    OutlinedTextField(
                        value = currentValue(segment.fieldName, segment.initialValue),
                        onValueChange = { emit(segment.fieldName, it) },
                        readOnly = readOnly,
                        singleLine = true,
                        label = { Text("Risposta $gapNumber") },
                        trailingIcon = when (segment.reviewMark) {
                            ReviewMark.Correct -> {
                                { Icon(Icons.Rounded.Check, contentDescription = "Corretta", tint = scheme.primary) }
                            }
                            ReviewMark.Incorrect -> {
                                { Icon(Icons.Rounded.Close, contentDescription = "Sbagliata", tint = scheme.error) }
                            }
                            null -> null
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is ClozeSegment.MenuGap -> {
                    gapNumber++
                    val current = currentValue(segment.fieldName, segment.initialValue)
                    Column {
                        Text(
                            text = "Risposta $gapNumber",
                            style = MaterialTheme.typography.labelSmall,
                            color = when (segment.reviewMark) {
                                ReviewMark.Correct -> scheme.primary
                                ReviewMark.Incorrect -> scheme.error
                                null -> scheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            segment.options
                                .filter { it.value.isNotBlank() || it.label.isNotBlank() }
                                .forEach { option ->
                                    val selected = option.value == current && option.value.isNotBlank()
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(if (selected) scheme.secondary else scheme.surfaceContainerHigh)
                                            .then(
                                                if (readOnly) Modifier
                                                else Modifier.clickable { emit(segment.fieldName, option.value) },
                                            )
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                    ) {
                                        Text(
                                            text = option.label.ifBlank { "—" },
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) scheme.onSecondary else scheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerdictBadge(question: AttemptQuestion) {
    val scheme = MaterialTheme.colorScheme
    val (label, bg, fg) = when (question.state) {
        "gradedright" -> Triple("CORRETTA", scheme.primaryContainer, scheme.onPrimaryContainer)
        "gradedpartial" -> Triple("PARZIALE", scheme.tertiaryContainer, scheme.onTertiaryContainer)
        "gradedwrong" -> Triple("SBAGLIATA", scheme.errorContainer, scheme.onErrorContainer)
        "gaveup" -> Triple("SENZA RISPOSTA", scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
        "needsgrading", "complete" -> Triple("DA VALUTARE", scheme.secondaryContainer, scheme.onSecondaryContainer)
        else -> return
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 1.2.sp,
        )
    }
}

@Composable
private fun ReviewNote(title: String, html: String, accent: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Spacer(Modifier.height(12.dp))
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (accent) scheme.tertiaryContainer else scheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title.uppercase(),
                color = if (accent) scheme.onTertiaryContainer else scheme.tertiary,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
            )
            Spacer(Modifier.height(6.dp))
            HtmlBody(
                html = html,
                color = if (accent) scheme.onTertiaryContainer else scheme.onSurfaceVariant,
            )
        }
    }
}
