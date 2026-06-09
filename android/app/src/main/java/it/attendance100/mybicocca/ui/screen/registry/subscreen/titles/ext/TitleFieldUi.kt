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
import androidx.compose.ui.graphics.vector.ImageVector
import it.attendance100.mybicocca.domain.model.document.TitleCategory
import it.attendance100.mybicocca.domain.model.document.TitleField

// The detail page groups the title's attribute rows into these sections, rendered in this order.
enum class TitleSection(val label: String) {
    Achievement("Conseguimento"),
    Evaluation("Valutazione"),
    Institution("Istituzione"),
    Path("Percorso"),
    Documentation("Documentazione"),
}

// Section header icon for the detail page's grouped cards (same icon-chip language as Iscrizioni).
val TitleSection.icon: ImageVector
    get() = when (this) {
        TitleSection.Achievement -> Icons.Outlined.WorkspacePremium
        TitleSection.Evaluation -> Icons.Outlined.Grade
        TitleSection.Institution -> Icons.Outlined.AccountBalance
        TitleSection.Path -> Icons.Outlined.Route
        TitleSection.Documentation -> Icons.Outlined.Description
    }

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

// A couple of labels read differently for a school diploma vs a university title (the same
// semantic field is an "Istituto" for the former and an "Ateneo" for the latter).
fun TitleField.label(category: TitleCategory): String = when (this) {
    TitleField.AcademicYear -> "Anno accademico"
    TitleField.GraduationYear -> "Anno di maturità"
    TitleField.AwardDate -> "Data di conseguimento"
    TitleField.Session -> "Sessione"
    TitleField.AchievementYears -> "Anni impiegati"
    TitleField.DurationYears -> "Durata legale"
    TitleField.GradeAverage -> "Media voti"
    TitleField.Credits -> "CFU"
    TitleField.Institution -> if (category == TitleCategory.HighSchool) "Istituto" else "Ateneo"
    TitleField.City -> "Città"
    TitleField.Province -> "Provincia"
    TitleField.Country -> "Nazione"
    TitleField.SameUniversity -> "Stesso ateneo"
    TitleField.InstitutionCode -> if (category == TitleCategory.HighSchool) "Codice MIUR" else "Codice ISTAT"
    TitleField.Language -> "Lingua"
    TitleField.ThesisTitle -> "Titolo tesi"
    TitleField.StudyPath -> "Percorso di studio"
    TitleField.EquivalentPath -> "Equipollenza"
    TitleField.DepositType -> "Deposito titolo"
    TitleField.ValueDeclaration -> "Dichiarazione di valore"
    TitleField.Evaluated -> "Valutato"
    TitleField.Recognized -> "Riconosciuto"
}

fun TitleCategory.label(): String = when (this) {
    TitleCategory.HighSchool -> "Maturità"
    TitleCategory.Italian -> "Titoli italiani"
    TitleCategory.Foreign -> "Titoli esteri"
}

val TitleCategory.icon: ImageVector
    get() = when (this) {
        TitleCategory.HighSchool -> Icons.Outlined.School
        TitleCategory.Italian -> Icons.Outlined.WorkspacePremium
        TitleCategory.Foreign -> Icons.Outlined.Public
    }

