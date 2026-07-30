package it.attendance100.mybicocca.domain.usecase.search

import androidx.annotation.StringRes
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.search.MatchInput
import it.attendance100.mybicocca.core.search.SearchMatcher
import it.attendance100.mybicocca.core.text.LocaleCachedCatalog
import it.attendance100.mybicocca.core.text.StringResolver
import it.attendance100.mybicocca.domain.model.search.SearchDestination
import it.attendance100.mybicocca.domain.model.search.SearchResult
import javax.inject.Inject

/**
 * Scores the catalog of app sub-pages against the query — the typical page navigation list.
 */
class SearchDestinationsUseCase @Inject constructor(
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

    operator fun invoke(query: String): List<SearchResult.Destination> =
        catalog.get(stringResolver).mapNotNull { item ->
            val score = SearchMatcher.score(query, MatchInput(item.title, item.aliases))
                ?: return@mapNotNull null
            SearchResult.Destination(
                destination = item.entry.destination,
                titleRes = item.entry.titleRes,
                subtitleRes = item.entry.subtitleRes,
                score = score,
            )
        }

    private data class Entry(
        val destination: SearchDestination,
        @StringRes val titleRes: Int,
        @StringRes val subtitleRes: Int?,
        val aliasResList: List<Int>,
    )

    private companion object {
        val Entries = listOf(
            Entry(
                SearchDestination.TabCalendar,
                R.string.search_dest_calendar_title, null,
                listOf(
                    R.string.search_dest_calendar_alias_1,
                    R.string.search_dest_calendar_alias_2,
                    R.string.search_dest_calendar_alias_3,
                    R.string.search_dest_calendar_alias_4,
                ),
            ),
            Entry(
                SearchDestination.TabElearning,
                R.string.search_dest_elearning_title, null,
                listOf(
                    R.string.search_dest_elearning_alias_1,
                    R.string.search_dest_elearning_alias_2,
                    R.string.search_dest_elearning_alias_3,
                    R.string.search_dest_elearning_alias_4,
                ),
            ),
            Entry(
                SearchDestination.TabMap,
                R.string.search_dest_map_title, null,
                listOf(
                    R.string.search_dest_map_alias_1,
                    R.string.search_dest_map_alias_2,
                    R.string.search_dest_map_alias_3,
                    R.string.search_dest_map_alias_4,
                    R.string.search_dest_map_alias_5,
                ),
            ),
            Entry(
                SearchDestination.TabRegistry,
                R.string.search_dest_registry_title, null,
                listOf(
                    R.string.search_dest_registry_alias_1,
                    R.string.search_dest_registry_alias_2,
                    R.string.search_dest_registry_alias_3,
                ),
            ),
            Entry(
                SearchDestination.Profile,
                R.string.search_dest_profile_title, null,
                listOf(
                    R.string.search_dest_profile_alias_1,
                    R.string.search_dest_profile_alias_2,
                    R.string.search_dest_profile_alias_3,
                    R.string.search_dest_profile_alias_4,
                    R.string.search_dest_profile_alias_5,
                    R.string.search_dest_profile_alias_6,
                    R.string.search_dest_profile_alias_7,
                    R.string.search_dest_profile_alias_8,
                    R.string.search_dest_profile_alias_9,
                ),
            ),
            Entry(
                SearchDestination.Taxes,
                R.string.search_dest_taxes_title, R.string.search_dest_taxes_subtitle,
                listOf(
                    R.string.search_dest_taxes_alias_1,
                    R.string.search_dest_taxes_alias_2,
                    R.string.search_dest_taxes_alias_3,
                    R.string.search_dest_taxes_alias_4,
                    R.string.search_dest_taxes_alias_5,
                ),
            ),
            Entry(
                SearchDestination.ExamResults,
                R.string.search_dest_exam_results_title,
                R.string.search_dest_exam_results_subtitle,
                listOf(
                    R.string.search_dest_exam_results_alias_1,
                    R.string.search_dest_exam_results_alias_2,
                    R.string.search_dest_exam_results_alias_3,
                    R.string.search_dest_exam_results_alias_4,
                ),
            ),
            Entry(
                SearchDestination.BookedExams,
                R.string.search_dest_booked_exams_title,
                R.string.search_dest_booked_exams_subtitle,
                listOf(
                    R.string.search_dest_booked_exams_alias_1,
                    R.string.search_dest_booked_exams_alias_2,
                    R.string.search_dest_booked_exams_alias_3,
                    R.string.search_dest_booked_exams_alias_4,
                ),
            ),
            Entry(
                SearchDestination.StudyPlan,
                R.string.search_dest_study_plan_title,
                R.string.search_dest_study_plan_subtitle,
                listOf(
                    R.string.search_dest_study_plan_alias_1,
                    R.string.search_dest_study_plan_alias_2,
                    R.string.search_dest_study_plan_alias_3,
                ),
            ),
            Entry(
                SearchDestination.Attendance,
                R.string.search_dest_attendance_title,
                R.string.search_dest_attendance_subtitle,
                listOf(
                    R.string.search_dest_attendance_alias_1,
                    R.string.search_dest_attendance_alias_2,
                    R.string.search_dest_attendance_alias_3,
                ),
            ),
            Entry(
                SearchDestination.Questionnaires,
                R.string.search_dest_questionnaires_title,
                R.string.search_dest_questionnaires_subtitle,
                listOf(
                    R.string.search_dest_questionnaires_alias_1,
                    R.string.search_dest_questionnaires_alias_2,
                ),
            ),
            Entry(
                SearchDestination.Appointments,
                R.string.search_dest_appointments_title,
                R.string.search_dest_appointments_subtitle,
                listOf(
                    R.string.search_dest_appointments_alias_1,
                    R.string.search_dest_appointments_alias_2,
                    R.string.search_dest_appointments_alias_3,
                    R.string.search_dest_appointments_alias_4,
                    R.string.search_dest_appointments_alias_5,
                ),
            ),
            Entry(
                SearchDestination.Enrollments,
                R.string.search_dest_enrollments_title,
                R.string.search_dest_enrollments_subtitle,
                listOf(
                    R.string.search_dest_enrollments_alias_1,
                    R.string.search_dest_enrollments_alias_2,
                    R.string.search_dest_enrollments_alias_3,
                    R.string.search_dest_enrollments_alias_4,
                ),
            ),
            Entry(
                SearchDestination.Titles,
                R.string.search_dest_titles_title, R.string.search_dest_titles_subtitle,
                listOf(
                    R.string.search_dest_titles_alias_1,
                    R.string.search_dest_titles_alias_2,
                    R.string.search_dest_titles_alias_3,
                ),
            ),
            Entry(
                SearchDestination.Certificates,
                R.string.search_dest_certificates_title,
                R.string.search_dest_certificates_subtitle,
                listOf(
                    R.string.search_dest_certificates_alias_1,
                    R.string.search_dest_certificates_alias_2,
                    R.string.search_dest_certificates_alias_3,
                ),
            ),
            Entry(
                SearchDestination.Library,
                R.string.search_dest_library_title, R.string.search_dest_library_subtitle,
                listOf(
                    R.string.search_dest_library_alias_1,
                    R.string.search_dest_library_alias_2,
                    R.string.search_dest_library_alias_3,
                    R.string.search_dest_library_alias_4,
                    R.string.search_dest_library_alias_5,
                ),
            ),
            Entry(
                SearchDestination.Refunds,
                R.string.search_dest_refunds_title, R.string.search_dest_refunds_subtitle,
                listOf(
                    R.string.search_dest_refunds_alias_1,
                    R.string.search_dest_refunds_alias_2,
                ),
            ),
            Entry(
                SearchDestination.Isee,
                R.string.search_dest_isee_title, R.string.search_dest_isee_subtitle,
                listOf(
                    R.string.search_dest_isee_alias_1,
                    R.string.search_dest_isee_alias_2,
                    R.string.search_dest_isee_alias_3,
                    R.string.search_dest_isee_alias_4,
                ),
            ),
            Entry(
                SearchDestination.Settings,
                R.string.search_dest_settings_title, null,
                listOf(
                    R.string.search_dest_settings_alias_1,
                    R.string.search_dest_settings_alias_2,
                    R.string.search_dest_settings_alias_3,
                    R.string.search_dest_settings_alias_4,
                ),
            ),
            Entry(
                SearchDestination.SettingsAppearance,
                R.string.search_dest_settings_appearance_title,
                R.string.search_dest_settings_appearance_subtitle,
                listOf(
                    R.string.search_dest_settings_appearance_alias_1,
                    R.string.search_dest_settings_appearance_alias_2,
                    R.string.search_dest_settings_appearance_alias_3,
                    R.string.search_dest_settings_appearance_alias_4,
                    R.string.search_dest_settings_appearance_alias_5,
                    R.string.search_dest_settings_appearance_alias_6,
                ),
            ),
            Entry(
                SearchDestination.SettingsSecurity,
                R.string.search_dest_settings_security_title,
                R.string.search_dest_settings_security_subtitle,
                listOf(
                    R.string.search_dest_settings_security_alias_1,
                    R.string.search_dest_settings_security_alias_2,
                    R.string.search_dest_settings_security_alias_3,
                    R.string.search_dest_settings_security_alias_4,
                    R.string.search_dest_settings_security_alias_5,
                ),
            ),
            Entry(
                SearchDestination.SettingsLanguage,
                R.string.search_dest_settings_language_title,
                R.string.search_dest_settings_language_subtitle,
                listOf(
                    R.string.search_dest_settings_language_alias_1,
                    R.string.search_dest_settings_language_alias_2,
                    R.string.search_dest_settings_language_alias_3,
                    R.string.search_dest_settings_language_alias_4,
                ),
            ),
            Entry(
                SearchDestination.SettingsFileAssociations,
                R.string.search_dest_settings_file_associations_title,
                R.string.search_dest_settings_file_associations_subtitle,
                listOf(
                    R.string.search_dest_settings_file_associations_alias_1,
                    R.string.search_dest_settings_file_associations_alias_2,
                    R.string.search_dest_settings_file_associations_alias_3,
                    R.string.search_dest_settings_file_associations_alias_4,
                ),
            ),
            Entry(
                SearchDestination.SettingsLicenses,
                R.string.search_dest_settings_licenses_title,
                R.string.search_dest_settings_licenses_subtitle,
                listOf(
                    R.string.search_dest_settings_licenses_alias_1,
                    R.string.search_dest_settings_licenses_alias_2,
                    R.string.search_dest_settings_licenses_alias_3,
                ),
            ),
            Entry(
                SearchDestination.SettingsAppInfo,
                R.string.search_dest_settings_app_info_title,
                R.string.search_dest_settings_app_info_subtitle,
                listOf(
                    R.string.search_dest_settings_app_info_alias_1,
                    R.string.search_dest_settings_app_info_alias_2,
                    R.string.search_dest_settings_app_info_alias_3,
                    R.string.search_dest_settings_app_info_alias_4,
                ),
            ),
        )
    }
}
