package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptQuestion
import it.attendance100.mybicocca.ui.component.text.HtmlBody
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.ChoiceOption
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.ClozeSegment
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.ParsedQuestion
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.QuestionUiModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.ReviewMark

// Renders one parsed question, both while answering and in read-only review, as a
// connected segment group in the plan-compiler language (see StudyPlanEditScreen):
// a header tile carrying the prompt, one connected tile per choice with a morphing
// selection knob, free-text/cloze fields in a closing tile, and review notes
// appended as closing segments. Every interaction emits the FULL field map for the
// slot (controls + hidden base fields), which is exactly the payload
// mod_quiz_save_attempt / process_attempt expect.
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
    val noteCount = if (readOnly) listOfNotNull(parsed.rightAnswerHtml, parsed.feedbackHtml).size else 0
    // Unsupported questions have no interactive body in review, so the header may
    // need to close the group on its own.
    val hasBody = parsed.model !is QuestionUiModel.Unsupported || !readOnly
    val bodyClosing = noteCount == 0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        QuestionHeaderTile(
            question = question,
            // Cloze carries its text inside the segments, not in the prompt.
            promptHtml = if (parsed.model is QuestionUiModel.Cloze) "" else parsed.model.promptHtml,
            flagged = flagged,
            readOnly = readOnly,
            onToggleFlag = onToggleFlag,
            alone = !hasBody && noteCount == 0,
        )

        when (val model = parsed.model) {
            is QuestionUiModel.SingleChoice -> {
                val selected = answerFields[model.fieldName]
                    ?: model.options.firstOrNull { it.initiallySelected }?.value.orEmpty()
                ChoiceTiles(
                    options = model.options,
                    isSelected = { it.value == selected },
                    readOnly = readOnly,
                    closing = bodyClosing,
                    onSelect = { option ->
                        onAnswer(model.baseFields + (model.fieldName to option.value))
                    },
                )
            }

            is QuestionUiModel.MultiChoice -> {
                val checked = { option: ChoiceOption ->
                    (answerFields[option.fieldName] ?: if (option.initiallySelected) "1" else "0") == "1"
                }
                ChoiceTiles(
                    options = model.options,
                    isSelected = checked,
                    readOnly = readOnly,
                    closing = bodyClosing,
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

            is QuestionUiModel.TextEntry -> BodyTile(closing = bodyClosing) {
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

            is QuestionUiModel.Essay -> BodyTile(closing = bodyClosing) {
                OutlinedTextField(
                    value = answerFields[model.fieldName] ?: model.initialValue,
                    onValueChange = { onAnswer(model.baseFields + (model.fieldName to it)) },
                    readOnly = readOnly,
                    minLines = 5,
                    placeholder = { Text("Scrivi qui la tua risposta…") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            is QuestionUiModel.Cloze -> BodyTile(closing = bodyClosing) {
                ClozeContent(
                    model = model,
                    answerFields = answerFields,
                    readOnly = readOnly,
                    onAnswer = onAnswer,
                )
            }

            is QuestionUiModel.Unsupported -> if (!readOnly) {
                BodyTile(closing = bodyClosing) {
                    Text(
                        text = "Questo tipo di domanda non è supportato nell'app: rispondi dal sito e-learning.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontStyle = FontStyle.Italic,
                    )
                }
            }
        }

        if (readOnly) {
            val hasFeedback = parsed.feedbackHtml != null
            parsed.rightAnswerHtml?.let {
                ReviewNoteTile(title = "Risposta corretta", html = it, accent = true, closing = !hasFeedback)
            }
            parsed.feedbackHtml?.let {
                ReviewNoteTile(title = "Feedback", html = it, accent = false, closing = true)
            }
        }
    }
}

// Header tile in the rule-group style: eyebrow row with the flag/verdict, then the
// question formulation as the tile body.
@Composable
private fun QuestionHeaderTile(
    question: AttemptQuestion,
    promptHtml: String,
    flagged: Boolean,
    readOnly: Boolean,
    onToggleFlag: (() -> Unit)?,
    alone: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val bottom = if (alone) 20.dp else 4.dp
    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = bottom, bottomEnd = bottom),
        color = scheme.surfaceContainerLow,
        contentColor = scheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 14.dp, bottom = 14.dp)) {
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
            if (promptHtml.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                HtmlBody(html = promptHtml, color = scheme.onSurface)
            }
        }
    }
}

// Middle segments keep flat 4dp corners; the group's last segment closes with 20dp.
private fun segmentShape(closing: Boolean): RoundedCornerShape = RoundedCornerShape(
    topStart = 4.dp,
    topEnd = 4.dp,
    bottomStart = if (closing) 20.dp else 4.dp,
    bottomEnd = if (closing) 20.dp else 4.dp,
)

// One connected tile per option. Like the plan compiler's course tiles, the tile
// itself doesn't change on selection — the knob carries it. Review verdicts DO wash
// the tile: the verdict must be loud.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ChoiceTiles(
    options: List<ChoiceOption>,
    isSelected: (ChoiceOption) -> Boolean,
    readOnly: Boolean,
    closing: Boolean,
    onSelect: (ChoiceOption) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    options.forEachIndexed { index, option ->
        val selected = isSelected(option)
        val (container, content) = when {
            readOnly && option.reviewMark == ReviewMark.Correct ->
                scheme.primaryContainer to scheme.onPrimaryContainer
            readOnly && option.reviewMark == ReviewMark.Incorrect ->
                scheme.errorContainer to scheme.onErrorContainer
            else -> scheme.surfaceContainerLow to scheme.onSurface
        }
        Surface(
            onClick = { onSelect(option) },
            enabled = !readOnly,
            shape = segmentShape(closing = closing && index == options.lastIndex),
            color = container,
            contentColor = content,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    HtmlBody(html = option.labelHtml, color = content)
                }
                Spacer(Modifier.width(10.dp))
                when {
                    readOnly && option.reviewMark == ReviewMark.Correct -> MorphKnob(
                        checked = true,
                        container = scheme.primary,
                        content = scheme.onPrimary,
                        icon = Icons.Rounded.Check,
                    )

                    readOnly && option.reviewMark == ReviewMark.Incorrect -> MorphKnob(
                        checked = true,
                        container = scheme.error,
                        content = scheme.onError,
                        icon = Icons.Rounded.Close,
                    )

                    else -> MorphKnob(
                        checked = selected,
                        container = if (selected) scheme.primary else scheme.surfaceContainerHighest,
                        content = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                        icon = if (selected) Icons.Rounded.Check else Icons.Rounded.Add,
                    )
                }
            }
        }
    }
}

// The plan compiler's selection knob: a circle that morphs into the sunny shape
// when checked.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MorphKnob(
    checked: Boolean,
    container: Color,
    content: Color,
    icon: ImageVector,
) {
    val motion = MaterialTheme.motionScheme
    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Sunny) }
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = motion.defaultSpatialSpec(),
        label = "knobMorph",
    )
    val containerColor by animateColorAsState(
        targetValue = container,
        animationSpec = motion.defaultEffectsSpec(),
        label = "knobContainer",
    )
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(MorphPolygonShape(morph, progress))
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(16.dp),
        )
    }
}

// Scales the normalized morph path up to the knob's actual size.
private class MorphPolygonShape(
    private val morph: Morph,
    private val progress: Float,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val matrix = Matrix()
        matrix.scale(size.width, size.height)
        val path = morph.toPath(progress).asComposePath()
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

@Composable
private fun BodyTile(
    closing: Boolean,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = segmentShape(closing),
        color = scheme.surfaceContainerLow,
        contentColor = scheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            content()
        }
    }
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
private fun ReviewNoteTile(title: String, html: String, accent: Boolean, closing: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = segmentShape(closing),
        color = if (accent) scheme.tertiaryContainer else scheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)) {
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
