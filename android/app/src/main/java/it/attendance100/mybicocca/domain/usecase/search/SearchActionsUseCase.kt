package it.attendance100.mybicocca.domain.usecase.search

import androidx.annotation.StringRes
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.search.MatchInput
import it.attendance100.mybicocca.core.search.SearchMatcher
import it.attendance100.mybicocca.core.text.LocaleCachedCatalog
import it.attendance100.mybicocca.core.text.StringResolver
import it.attendance100.mybicocca.domain.model.search.SearchAction
import it.attendance100.mybicocca.domain.model.search.SearchResult
import javax.inject.Inject

/**
 * Scores the catalog of things a user can DO against the query — the command-palette half
 * of the unified search. Titles are imperative ("Prenota un esame") and aliases lean on
 * the verbs students actually type; that's what makes search feel like a command palette
 * rather than a page list.
 */
class SearchActionsUseCase @Inject constructor(
    private val stringResolver: StringResolver
) {

    private data class ResolvedEntry(
        val entry: Entry,
        val title: String,
        val aliases: List<String>,
    )

    private val catalog = LocaleCachedCatalog(Entries) { entry, resolver ->
        ResolvedEntry(
            entry = entry,
            title = resolver.getString(entry.titleRes),
            aliases = entry.aliasResList.map { resolver.getString(it) },
        )
    }

    operator fun invoke(query: String): List<SearchResult.Action> =
        catalog.get(stringResolver).mapNotNull { item ->
            val score = SearchMatcher.score(query, MatchInput(item.title, item.aliases))
                ?: return@mapNotNull null
            SearchResult.Action(
                action = item.entry.action,
                titleRes = item.entry.titleRes,
                subtitleRes = item.entry.subtitleRes,
                score = score,
            )
        }

    private data class Entry(
        val action: SearchAction,
        @StringRes val titleRes: Int,
        @StringRes val subtitleRes: Int?,
        val aliasResList: List<Int>,
    )

    private companion object {
        val Entries = listOf(
            Entry(
                SearchAction.BookExam,
                R.string.search_action_book_exam_title,
                R.string.search_action_book_exam_subtitle,
                listOf(
                    R.string.search_action_book_exam_alias_1,
                    R.string.search_action_book_exam_alias_2,
                    R.string.search_action_book_exam_alias_3,
                    R.string.search_action_book_exam_alias_4,
                    R.string.search_action_book_exam_alias_5,
                ),
            ),
            Entry(
                SearchAction.MarkPresence,
                R.string.search_action_mark_presence_title,
                R.string.search_action_mark_presence_subtitle,
                listOf(
                    R.string.search_action_mark_presence_alias_1,
                    R.string.search_action_mark_presence_alias_2,
                    R.string.search_action_mark_presence_alias_3,
                    R.string.search_action_mark_presence_alias_4,
                    R.string.search_action_mark_presence_alias_5,
                    R.string.search_action_mark_presence_alias_6,
                ),
            ),
            Entry(
                SearchAction.ReserveLibrarySeat,
                R.string.search_action_reserve_library_seat_title,
                R.string.search_action_reserve_library_seat_subtitle,
                listOf(
                    R.string.search_action_reserve_library_seat_alias_1,
                    R.string.search_action_reserve_library_seat_alias_2,
                    R.string.search_action_reserve_library_seat_alias_3,
                    R.string.search_action_reserve_library_seat_alias_4,
                ),
            ),
            Entry(
                SearchAction.BookAppointment,
                R.string.search_action_book_appointment_title,
                R.string.search_action_book_appointment_subtitle,
                listOf(
                    R.string.search_action_book_appointment_alias_1,
                    R.string.search_action_book_appointment_alias_2,
                    R.string.search_action_book_appointment_alias_3,
                ),
            ),
            Entry(
                SearchAction.AddCourse,
                R.string.search_action_add_course_title,
                R.string.search_action_add_course_subtitle,
                listOf(
                    R.string.search_action_add_course_alias_1,
                    R.string.search_action_add_course_alias_2,
                    R.string.search_action_add_course_alias_3,
                    R.string.search_action_add_course_alias_4,
                    R.string.search_action_add_course_alias_5,
                ),
            ),
            Entry(
                SearchAction.PayTaxes,
                R.string.search_action_pay_taxes_title,
                R.string.search_action_pay_taxes_subtitle,
                listOf(
                    R.string.search_action_pay_taxes_alias_1,
                    R.string.search_action_pay_taxes_alias_2,
                    R.string.search_action_pay_taxes_alias_3,
                    R.string.search_action_pay_taxes_alias_4,
                    R.string.search_action_pay_taxes_alias_5,
                ),
            ),
            Entry(
                SearchAction.CompileQuestionnaire,
                R.string.search_action_compile_questionnaire_title,
                R.string.search_action_compile_questionnaire_subtitle,
                listOf(
                    R.string.search_action_compile_questionnaire_alias_1,
                    R.string.search_action_compile_questionnaire_alias_2,
                    R.string.search_action_compile_questionnaire_alias_3,
                ),
            ),
            Entry(
                SearchAction.EditStudyPlan,
                R.string.search_action_edit_study_plan_title,
                R.string.search_action_edit_study_plan_subtitle,
                listOf(
                    R.string.search_action_edit_study_plan_alias_1,
                    R.string.search_action_edit_study_plan_alias_2,
                    R.string.search_action_edit_study_plan_alias_3,
                    R.string.search_action_edit_study_plan_alias_4,
                ),
            ),
            Entry(
                SearchAction.HypotheticalAverage,
                R.string.search_action_hypothetical_average_title,
                R.string.search_action_hypothetical_average_subtitle,
                listOf(
                    R.string.search_action_hypothetical_average_alias_1,
                    R.string.search_action_hypothetical_average_alias_2,
                    R.string.search_action_hypothetical_average_alias_3,
                    R.string.search_action_hypothetical_average_alias_4,
                    R.string.search_action_hypothetical_average_alias_5,
                    R.string.search_action_hypothetical_average_alias_6,
                ),
            ),
            Entry(
                SearchAction.ChangeTheme,
                R.string.search_action_change_theme_title,
                R.string.search_action_change_theme_subtitle,
                listOf(
                    R.string.search_action_change_theme_alias_1,
                    R.string.search_action_change_theme_alias_2,
                    R.string.search_action_change_theme_alias_3,
                    R.string.search_action_change_theme_alias_4,
                    R.string.search_action_change_theme_alias_5,
                ),
            ),
            Entry(
                SearchAction.ChangeLanguage,
                R.string.search_action_change_language_title,
                R.string.search_action_change_language_subtitle,
                listOf(
                    R.string.search_action_change_language_alias_1,
                    R.string.search_action_change_language_alias_2,
                    R.string.search_action_change_language_alias_3,
                ),
            ),
            Entry(
                SearchAction.AddAccount,
                R.string.search_action_add_account_title,
                R.string.search_action_add_account_subtitle,
                listOf(
                    R.string.search_action_add_account_alias_1,
                    R.string.search_action_add_account_alias_2,
                    R.string.search_action_add_account_alias_3,
                    R.string.search_action_add_account_alias_4,
                ),
            ),
        )
    }
}
