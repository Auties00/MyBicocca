package it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.ext

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AltRoute
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.document.TitleCategory
import it.attendance100.mybicocca.domain.model.document.TitleField

/**
 * Sections the detail page groups a title's attribute rows into, rendered in declaration
 * order.
 */
enum class TitleSection {
    Achievement,
    Evaluation,
    Institution,
    Path,
    Documentation,
}

/** Section header copy resource for the detail page's grouped cards. */
@get:StringRes
val TitleSection.labelRes: Int
    get() = when (this) {
        TitleSection.Achievement -> R.string.titles_section_achievement
        TitleSection.Evaluation -> R.string.titles_section_evaluation
        TitleSection.Institution -> R.string.titles_section_institution
        TitleSection.Path -> R.string.titles_section_path
        TitleSection.Documentation -> R.string.titles_section_documentation
    }

/** Section header icon for the detail page's grouped cards (same icon-chip language as Iscrizioni). */
val TitleSection.icon: ImageVector
    get() = when (this) {
        TitleSection.Achievement -> Icons.Outlined.WorkspacePremium
        TitleSection.Evaluation -> Icons.Outlined.Grade
        TitleSection.Institution -> Icons.Outlined.AccountBalance
        TitleSection.Path -> Icons.Outlined.Route
        TitleSection.Documentation -> Icons.Outlined.Description
    }

/** The detail-page section a field's row renders under. */
val TitleField.section: TitleSection
    get() = when (this) {
        TitleField.AcademicYear,
        TitleField.GraduationYear,
        TitleField.AwardDate,
        TitleField.Session,
        TitleField.AchievementYears,
        TitleField.DurationYears -> TitleSection.Achievement

        TitleField.GradeAverage,
        TitleField.Credits -> TitleSection.Evaluation

        TitleField.Institution,
        TitleField.City,
        TitleField.Province,
        TitleField.Country,
        TitleField.SameUniversity,
        TitleField.InstitutionCode,
        TitleField.Language -> TitleSection.Institution

        TitleField.ThesisTitle,
        TitleField.StudyPath,
        TitleField.EquivalentPath -> TitleSection.Path

        TitleField.DepositType,
        TitleField.ValueDeclaration,
        TitleField.Evaluated,
        TitleField.Recognized -> TitleSection.Documentation
    }

val TitleField.icon: ImageVector
    get() = when (this) {
        TitleField.AcademicYear -> Icons.Outlined.CalendarMonth
        TitleField.GraduationYear -> Icons.Outlined.CalendarToday
        TitleField.AwardDate -> Icons.Outlined.Event
        TitleField.Session -> Icons.Outlined.Schedule
        TitleField.AchievementYears -> Icons.Outlined.Timeline
        TitleField.DurationYears -> Icons.Outlined.Schedule
        TitleField.GradeAverage -> Icons.Outlined.Calculate
        TitleField.Credits -> Icons.Outlined.Numbers
        TitleField.Institution -> Icons.Outlined.AccountBalance
        TitleField.City -> Icons.Outlined.LocationCity
        TitleField.Province -> Icons.Outlined.Place
        TitleField.Country -> Icons.Outlined.Public
        TitleField.SameUniversity -> Icons.Outlined.Verified
        TitleField.InstitutionCode -> Icons.Outlined.Tag
        TitleField.Language -> Icons.Outlined.Translate
        TitleField.ThesisTitle -> Icons.AutoMirrored.Outlined.MenuBook
        TitleField.StudyPath -> Icons.Outlined.Route
        TitleField.EquivalentPath -> Icons.AutoMirrored.Outlined.AltRoute
        TitleField.DepositType -> Icons.Outlined.Description
        TitleField.ValueDeclaration -> Icons.AutoMirrored.Outlined.FactCheck
        TitleField.Evaluated -> Icons.Outlined.Grade
        TitleField.Recognized -> Icons.Outlined.Verified
    }

/**
 * Field label copy resource. A couple of labels read differently for a school diploma vs a
 * university title: the same semantic field is an "Istituto" for the former and an
 * "Ateneo" for the latter, so it takes the title's [category].
 */
@StringRes
fun TitleField.labelRes(category: TitleCategory): Int = when (this) {
    TitleField.AcademicYear -> R.string.titles_field_academic_year
    TitleField.GraduationYear -> R.string.titles_field_graduation_year
    TitleField.AwardDate -> R.string.titles_field_award_date
    TitleField.Session -> R.string.titles_field_session
    TitleField.AchievementYears -> R.string.titles_field_achievement_years
    TitleField.DurationYears -> R.string.titles_field_duration_years
    TitleField.GradeAverage -> R.string.titles_field_grade_average
    TitleField.Credits -> R.string.common_cfu
    TitleField.Institution ->
        if (category == TitleCategory.HighSchool) R.string.titles_field_institution_school
        else R.string.titles_field_institution_university
    TitleField.City -> R.string.titles_field_city
    TitleField.Province -> R.string.titles_field_province
    TitleField.Country -> R.string.titles_field_country
    TitleField.SameUniversity -> R.string.titles_field_same_university
    TitleField.InstitutionCode ->
        if (category == TitleCategory.HighSchool) R.string.titles_field_code_miur
        else R.string.titles_field_code_istat
    TitleField.Language -> R.string.titles_field_language
    TitleField.ThesisTitle -> R.string.titles_field_thesis_title
    TitleField.StudyPath -> R.string.titles_field_study_path
    TitleField.EquivalentPath -> R.string.titles_field_equivalent_path
    TitleField.DepositType -> R.string.titles_field_deposit_type
    TitleField.ValueDeclaration -> R.string.titles_field_value_declaration
    TitleField.Evaluated -> R.string.titles_field_evaluated
    TitleField.Recognized -> R.string.titles_field_recognized
}

/** Category section header copy resource for the titles directory. */
@get:StringRes
val TitleCategory.labelRes: Int
    get() = when (this) {
        TitleCategory.HighSchool -> R.string.titles_category_high_school
        TitleCategory.Italian -> R.string.titles_category_italian
        TitleCategory.Foreign -> R.string.titles_category_foreign
    }

val TitleCategory.icon: ImageVector
    get() = when (this) {
        TitleCategory.HighSchool -> Icons.Outlined.School
        TitleCategory.Italian -> Icons.Outlined.WorkspacePremium
        TitleCategory.Foreign -> Icons.Outlined.Public
    }

