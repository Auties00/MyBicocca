package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.ext

import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus
import java.util.Locale

// Esse3 ships activity names in all caps ("ANALISI E PROGETTO DI ALGORITMI").
val QuestionnaireActivity.displayName: String
    get() = activityName.toDisplayCase()

val QuestionnaireActivity.detailLine: String
    get() = buildList {
        activityCode?.takeIf { it.isNotBlank() }?.let(::add)
        add("${credits.toCompactString()} CFU")
        attendanceYear?.let { add("${it}/${it + 1}") }
    }.joinToString(" · ")

val QuestionnaireActivityStatus.label: String
    get() = when (this) {
        QuestionnaireActivityStatus.Completed -> "Compilato"
        QuestionnaireActivityStatus.PartiallyCompleted -> "Parziale"
        QuestionnaireActivityStatus.ToCompile -> "Da compilare"
        QuestionnaireActivityStatus.ConfigurationError -> "Non disponibile"
    }

val QuestionnaireActivityStatus.pending: Boolean
    get() = this == QuestionnaireActivityStatus.ToCompile ||
        this == QuestionnaireActivityStatus.PartiallyCompleted

fun String.toDisplayCase(): String = trim()
    .split(Regex("\\s+"))
    .joinToString(" ") { word ->
        word.lowercase(Locale.ITALIAN).replaceFirstChar { it.titlecase(Locale.ITALIAN) }
    }

private fun Float.toCompactString(): String =
    if (this % 1f == 0f) toInt().toString() else toString()
