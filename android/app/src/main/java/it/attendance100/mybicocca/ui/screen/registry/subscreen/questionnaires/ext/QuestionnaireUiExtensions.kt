package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.ext

import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus
import java.util.Locale

// Esse3 ships activity names in all caps ("ANALISI E PROGETTO DI ALGORITMI").
val QuestionnaireActivity.displayName: String
    get() = activityName.toDisplayCase()

val QuestionnaireActivityStatus.pending: Boolean
    get() = this == QuestionnaireActivityStatus.ToCompile ||
        this == QuestionnaireActivityStatus.PartiallyCompleted

fun String.toDisplayCase(): String = trim()
    .split(Regex("\\s+"))
    .joinToString(" ") { word ->
        word.lowercase(Locale.ITALIAN).replaceFirstChar { it.titlecase(Locale.ITALIAN) }
    }
