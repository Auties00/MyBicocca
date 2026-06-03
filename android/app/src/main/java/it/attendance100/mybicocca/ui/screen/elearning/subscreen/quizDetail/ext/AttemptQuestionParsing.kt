package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.ext

import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptQuestion
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.ChoiceOption
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.ClozeSegment
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.MenuOption
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.ParsedQuestion
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.QuestionUiModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.ReviewMark
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

// Turns the raw Moodle question HTML (from mod_quiz_get_attempt_data / _review) into a
// renderable model. Detection is structural, not type-string based, so unseen qtypes that
// reuse standard controls (radio rows, inline selects) still render; only genuinely
// exotic ones (drag-drop, ...) fall back to read-only HTML.
fun AttemptQuestion.parseQuestion(): ParsedQuestion {
    val doc = Jsoup.parseBodyFragment(html)
    doc.select(".accesshide").remove()
    val formulation = doc.selectFirst("div.formulation")
    val rightAnswer = doc.selectFirst("div.rightanswer")?.html()?.takeIf { it.isNotBlank() }
    val feedback = listOfNotNull(
        doc.selectFirst("div.specificfeedback")?.html(),
        doc.selectFirst("div.generalfeedback")?.html(),
    ).filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }?.joinToString("<br>")

    if (formulation == null) {
        return ParsedQuestion(
            model = QuestionUiModel.Unsupported(slot, html, emptyMap(), type),
            rightAnswerHtml = rightAnswer,
            feedbackHtml = feedback,
        )
    }

    // Hidden inputs (sequencecheck, answerformat, ...) must round-trip with every save or
    // Moodle throws submissionoutofsequence. The flag field is deliberately excluded: it is
    // user state, not answer payload.
    val baseFields = formulation.select("input[type=hidden][name]")
        .associate { it.attr("name") to it.attr("value") }
        .filterKeys { !it.endsWith(":flagged") }

    val qtext = formulation.selectFirst("div.qtext")
    val promptHtml = qtext?.html().orEmpty()
    formulation.select("div.questionflag").remove()

    val model = parseChoiceRows(formulation)?.toModel(slot, promptHtml, baseFields)
        ?: parseEssay(formulation, slot, promptHtml, baseFields)
        ?: parseCloze(formulation, slot, baseFields)
        ?: parseTextEntry(formulation, slot, promptHtml, baseFields, type)
        ?: QuestionUiModel.Unsupported(
            slot = slot,
            promptHtml = formulation.apply { select("input[type=hidden]").remove() }.html(),
            baseFields = baseFields,
            type = type,
        )

    return ParsedQuestion(model = model, rightAnswerHtml = rightAnswer, feedbackHtml = feedback)
}

private sealed interface ChoiceRows {
    data class Radio(val fieldName: String, val options: List<ChoiceOption>) : ChoiceRows
    data class Checkbox(val options: List<ChoiceOption>) : ChoiceRows
}

private fun ChoiceRows.toModel(
    slot: Int,
    promptHtml: String,
    baseFields: Map<String, String>,
): QuestionUiModel = when (this) {
    is ChoiceRows.Radio -> QuestionUiModel.SingleChoice(slot, promptHtml, baseFields, fieldName, options)
    is ChoiceRows.Checkbox -> QuestionUiModel.MultiChoice(slot, promptHtml, baseFields, options)
}

private fun parseChoiceRows(formulation: Element): ChoiceRows? {
    val answerBlock = formulation.selectFirst("div.answer") ?: return null
    val doc = formulation.ownerDocument() ?: return null
    var radioName: String? = null
    var anyCheckbox = false
    val options = answerBlock.children().mapNotNull { row ->
        val input = row.selectFirst("input[type=radio], input[type=checkbox]") ?: return@mapNotNull null
        when (input.attr("type")) {
            "radio" -> radioName = input.attr("name")
            else -> anyCheckbox = true
        }
        ChoiceOption(
            fieldName = input.attr("name"),
            value = input.attr("value"),
            labelHtml = choiceLabelHtml(doc, row, input),
            initiallySelected = input.hasAttr("checked"),
            reviewMark = reviewMarkOf(row),
        )
    }
    if (options.isEmpty()) return null
    val name = radioName
    return when {
        anyCheckbox -> ChoiceRows.Checkbox(options)
        name != null -> ChoiceRows.Radio(name, options)
        else -> null
    }
}

private fun choiceLabelHtml(doc: Element, row: Element, input: Element): String {
    val labelled = input.attr("aria-labelledby").takeIf { it.isNotBlank() }
        ?.let { doc.getElementById(it) }
    val label = labelled ?: row.selectFirst("label") ?: row
    label.select("span.answernumber").remove()
    label.select("input").remove()
    return label.html()
}

// Cloze (and gapselect-style) questions embed their controls inline in the question body.
// Real Bicocca cloze HTML often has no div.qtext at all — the gaps sit in paragraphs
// directly under the formulation — so the scan covers the whole formulation. Each control
// is swapped for a sentinel text node, then the html is split back into text chunks
// interleaved with the gaps in document order.
private fun parseCloze(formulation: Element, slot: Int, baseFields: Map<String, String>): QuestionUiModel? {
    val controls = formulation.select("input[type=text], select")
    val hasMenus = controls.any { it.tagName() == "select" }
    // A lone text input reads better as a plain TextEntry below the prompt.
    if (controls.isEmpty() || (!hasMenus && controls.size < 2)) return null
    formulation.select("input[type=hidden]").remove()

    val gaps = controls.map { control ->
        if (control.tagName() == "select") {
            ClozeSegment.MenuGap(
                fieldName = control.attr("name"),
                options = control.select("option").map { MenuOption(it.attr("value"), it.text()) },
                initialValue = control.selectFirst("option[selected]")?.attr("value").orEmpty(),
                reviewMark = reviewMarkOf(control),
            )
        } else {
            ClozeSegment.TextGap(
                fieldName = control.attr("name"),
                initialValue = control.attr("value"),
                reviewMark = reviewMarkOf(control),
            )
        }
    }
    controls.forEachIndexed { index, control ->
        control.replaceWith(TextNode("$GAP_MARKER$index$GAP_MARKER"))
    }

    val segments = mutableListOf<ClozeSegment>()
    formulation.html().split(GAP_MARKER).forEachIndexed { index, part ->
        if (index % 2 == 1) {
            part.toIntOrNull()?.let { segments += gaps[it] }
        } else if (Jsoup.parseBodyFragment(part).text().isNotBlank() || part.contains("<img")) {
            segments += ClozeSegment.TextChunk(part)
        }
    }
    return QuestionUiModel.Cloze(slot, promptHtml = "", baseFields = baseFields, segments = segments)
}

private fun parseEssay(
    formulation: Element,
    slot: Int,
    promptHtml: String,
    baseFields: Map<String, String>,
): QuestionUiModel? {
    val textarea = formulation.selectFirst("textarea") ?: return null
    return QuestionUiModel.Essay(
        slot = slot,
        promptHtml = promptHtml,
        baseFields = baseFields,
        fieldName = textarea.attr("name"),
        initialValue = textarea.text(),
    )
}

private fun parseTextEntry(
    formulation: Element,
    slot: Int,
    promptHtml: String,
    baseFields: Map<String, String>,
    type: String,
): QuestionUiModel? {
    val input = formulation.selectFirst("input[type=text]") ?: return null
    return QuestionUiModel.TextEntry(
        slot = slot,
        promptHtml = promptHtml,
        baseFields = baseFields,
        fieldName = input.attr("name"),
        initialValue = input.attr("value"),
        numeric = type == "numerical" || type == "calculated",
    )
}

private fun reviewMarkOf(element: Element): ReviewMark? = when {
    element.hasClass("correct") -> ReviewMark.Correct
    element.hasClass("incorrect") -> ReviewMark.Incorrect
    else -> null
}

private const val GAP_MARKER = "\u0001"
