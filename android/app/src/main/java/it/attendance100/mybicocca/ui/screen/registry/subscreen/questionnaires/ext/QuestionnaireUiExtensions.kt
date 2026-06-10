package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.ext

import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus
import java.util.Locale

/**
 * Title-cased activity name: Esse3 ships them in all caps ("ANALISI E PROGETTO DI
 * ALGORITMI").
 */
val QuestionnaireActivity.displayName: String
    get() = activityName.toDisplayCase()

/** True for the statuses that still await (or partially await) compilation. */
val QuestionnaireActivityStatus.pending: Boolean
    get() = this == QuestionnaireActivityStatus.ToCompile ||
        this == QuestionnaireActivityStatus.PartiallyCompleted

/** Title-cases each whitespace-separated word using Italian casing rules. */
fun String.toDisplayCase(): String = trim()
    .split(Regex("\\s+"))
    .joinToString(" ") { word ->
        word.lowercase(Locale.ITALIAN).replaceFirstChar { it.titlecase(Locale.ITALIAN) }
    }
