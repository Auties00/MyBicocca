package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus

/**
 * Title-cased activity name: Esse3 ships them in all caps ("ANALISI E PROGETTO DI
 * ALGORITMI").
 */
val QuestionnaireActivity.displayName: String
    @Composable
    @ReadOnlyComposable
    get() = activityName.toDisplayCase()

/** True for the statuses that still await (or partially await) compilation. */
val QuestionnaireActivityStatus.pending: Boolean
    get() = this == QuestionnaireActivityStatus.ToCompile ||
        this == QuestionnaireActivityStatus.PartiallyCompleted

/** Title-cases each whitespace-separated word using Italian casing rules. */
@Composable
@ReadOnlyComposable
fun String.toDisplayCase(): String {
    val locale = currentLocale()
    return trim()
        .split(Regex("\\s+"))
        .joinToString(" ") { word ->
            word.lowercase(locale).replaceFirstChar { it.titlecase(locale) }
        }
}
