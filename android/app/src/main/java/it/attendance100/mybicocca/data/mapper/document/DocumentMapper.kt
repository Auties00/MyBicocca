package it.attendance100.mybicocca.data.mapper.document

import it.attendance100.mybicocca.data.mapper.common.Esse3DateFormat
import it.attendance100.mybicocca.data.mapper.common.parseEsse3Date
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BadgeData
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ForeignTitlePerson
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3HighSchoolDiplomaPerson
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ItalianTitlePerson
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PersonTitles
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.model.document.BadgeBlobId
import it.attendance100.mybicocca.domain.model.document.BadgeId
import it.attendance100.mybicocca.domain.model.document.StudentBadge
import it.attendance100.mybicocca.domain.model.document.TitleAttribute
import it.attendance100.mybicocca.domain.model.document.TitleCategory
import it.attendance100.mybicocca.domain.model.document.TitleField
import it.attendance100.mybicocca.domain.model.document.TitleStatus

/**
 * Maps the Esse3 badge metadata to the domain card. Esse3 lifecycle flags are 0/1 longs and
 * anything non-zero counts as set; their wire names are misleading, so the mapping follows the
 * Esse3BadgeData KDoc: consFlg = consegnato (delivered), restFlg = annullato (cancelled),
 * annFlg = restituito (returned). The full name is assembled from the separate name/surname
 * fields.
 */
fun Esse3BadgeData.toStudentBadge(): StudentBadge {
    val name = listOfNotNull(name?.trim(), surname?.trim())
        .filter { it.isNotEmpty() }
        .joinToString(" ")
        .takeIf { it.isNotEmpty() }
    return StudentBadge(
        id = BadgeId(badgeId ?: 0L),
        blobId = badgeBlobId?.let { BadgeBlobId(it) },
        hasFrontImage = (frontImagePresent ?: 0) == 1,
        hasRearImage = (rearImagePresent ?: 0) == 1,
        rfid = rfid.trim().takeIf { it.isNotEmpty() },
        studentNumber = matricola?.trim()?.takeIf { it.isNotEmpty() },
        fullName = name,
        courseDescription = courseOfStudyDescription?.trim()?.takeIf { it.isNotEmpty() },
        facultyDescription = facultyDescription?.trim()?.takeIf { it.isNotEmpty() },
        academicYear = academicYearAnnualEnrollment,
        delivered = (consentFlag ?: 0L) != 0L,
        cancelled = (restFlag ?: 0L) != 0L,
        returned = (yearFlag ?: 0L) != 0L,
        createdOn = startDate.parseEsse3Date(),
        printedOn = printDate.parseEsse3Date(),
        deliveredOn = deliveryDate.parseEsse3Date(),
    )
}

/** The /titoli payload nests three independent title families; flattens them into one list. */
fun Esse3PersonTitles.toAcademicTitles(): List<AcademicTitle> =
    SUP.map { it.toAcademicTitle() } +
        italianTitle.map { it.toAcademicTitle() } +
        foreignTitle.map { it.toAcademicTitle() }

private fun Esse3HighSchoolDiplomaPerson.toAcademicTitle(): AcademicTitle {
    val cumLaude = (cumLaudeFlag ?: 0) == 1
    return AcademicTitle(
        id = "sup-${id ?: personId.orEmpty()}",
        category = TitleCategory.HighSchool,
        status = italianTitleStatusCode.toTitleStatus(),
        typeDescription = higherTitleTypesDescription.clean(),
        subject = null,
        institution = (schoolName ?: schoolDescription).clean(),
        country = deliveryNationDescription.clean(),
        year = highSchoolGraduationYear?.toString(),
        grade = formatGrade(grade, maxGrade),
        cumLaude = cumLaude,
        valueDeclarationFiled = (valueDeclarationFlag ?: 0) == 1,
        attributes = buildList {
            put(TitleField.GraduationYear, highSchoolGraduationYear?.toString())
            put(TitleField.AwardDate, formatDate(highSchoolGraduationDate))
            put(TitleField.Institution, (schoolName ?: schoolDescription).clean())
            put(TitleField.City, schoolMunicipalityDescription.clean())
            put(TitleField.Province, schoolMunicipalityAbbreviation.clean())
            put(TitleField.Country, deliveryNationDescription.clean())
            put(TitleField.InstitutionCode, schoolMiurCode.clean())
            put(TitleField.Language, teachingLanguageDescription.clean())
            put(TitleField.DepositType, depositTypesDescription.clean())
            putFlag(TitleField.ValueDeclaration, valueDeclarationFlag)
            putFlag(TitleField.Evaluated, evaluatedFlag?.toInt())
        },
    )
}

private fun Esse3ItalianTitlePerson.toAcademicTitle(): AcademicTitle {
    val academicYear = p06AcademicYearDescription.clean() ?: academicYearTitleAward?.toString()
    return AcademicTitle(
        id = "titit-${italianTitleId ?: personId.orEmpty()}",
        category = TitleCategory.Italian,
        status = italianTitleStatusCode.toTitleStatus(),
        typeDescription = italianTitleTypesDescription.clean(),
        subject = (vDecodeTitleTypeCodeDescription ?: courseOfStudyDescription).clean(),
        institution = p06UniversitiesDescription.clean(),
        country = null,
        year = academicYear,
        grade = formatGrade(grade, baseGrade?.toFloat()),
        cumLaude = (cumLaude ?: 0) == 1,
        valueDeclarationFiled = false,
        attributes = buildList {
            put(TitleField.AcademicYear, academicYear)
            put(TitleField.AwardDate, formatDate(titleDeliveryDate))
            put(TitleField.Session, session.clean())
            put(TitleField.AchievementYears, achievementYearsNumber?.let { "$it" })
            putGrade(TitleField.GradeAverage, gradesAverage)
            put(TitleField.Credits, credits?.let { formatNumber(it) })
            put(TitleField.Institution, p06UniversitiesDescription.clean())
            putFlag(TitleField.SameUniversity, sameUniversityFlag?.toInt())
            put(TitleField.InstitutionCode, p06UniversitiesIstatCode.clean())
            put(TitleField.Language, languageDescription.clean())
            put(TitleField.ThesisTitle, thesisTitle.clean())
            put(TitleField.StudyPath, studyPath.clean())
            put(TitleField.DepositType, depositTypesDescription.clean())
            putFlag(TitleField.Evaluated, evaluatedFlag)
        },
    )
}

private fun Esse3ForeignTitlePerson.toAcademicTitle(): AcademicTitle {
    val academicYear = p06TeachingActivityDescription.clean() ?: academicYearAwardId?.toString()
    val institution = (p06SiteDescription ?: universityDescription ?: p01ForeignTestDescription).clean()
    return AcademicTitle(
        id = "titstra-${foreignTitleId ?: personId.orEmpty()}",
        category = TitleCategory.Foreign,
        status = foreignTitleStatusCode.toTitleStatus(),
        typeDescription = titleStatusTypesDescription.clean(),
        subject = foreignCourseOfStudy.clean(),
        institution = institution,
        country = p01NationDescription.clean(),
        year = academicYear,
        grade = formatGrade(grade, baseGrade?.toFloat()),
        cumLaude = (cumLaude ?: 0) == 1,
        valueDeclarationFiled = (valueDeclarationFlag ?: 0) == 1,
        attributes = buildList {
            put(TitleField.AcademicYear, academicYear)
            put(TitleField.AwardDate, formatDate(titleDeliveryDate))
            put(TitleField.DurationYears, durationYears?.let { "$it" })
            put(TitleField.Institution, institution)
            put(TitleField.City, deliveryForeignCity.clean())
            put(TitleField.Country, p01NationDescription.clean())
            put(TitleField.Language, teachingLanguageDescription.clean())
            put(TitleField.EquivalentPath, equivalentPath.clean())
            put(TitleField.DepositType, depositTypesDescription.clean())
            putFlag(TitleField.ValueDeclaration, valueDeclarationFlag)
            putFlag(TitleField.Evaluated, evaluatedFlag?.toInt())
            putFlag(TitleField.Recognized, equivalentTitleFlag?.toInt())
        },
    )
}

/** Appends an attribute row only when the value is meaningfully present. */
private fun MutableList<TitleAttribute>.put(field: TitleField, value: String?) {
    value?.trim()?.takeIf { it.isNotEmpty() }?.let { add(TitleAttribute(field, it)) }
}

/** Esse3 0/1 flags: a non-null flag becomes an explicit Sì/No row. */
private fun MutableList<TitleAttribute>.putFlag(field: TitleField, flag: Int?) {
    if (flag != null) add(TitleAttribute(field, if (flag != 0) "Sì" else "No"))
}

private fun MutableList<TitleAttribute>.putGrade(field: TitleField, value: Float?) {
    value?.takeIf { it > 0f }?.let { add(TitleAttribute(field, formatNumber(it))) }
}

private fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

/** Esse3 status code: C = Conseguito (awarded), I = In ipotesi (hypothesised). */
private fun String?.toTitleStatus(): TitleStatus = when (this?.trim()?.uppercase()) {
    "C" -> TitleStatus.Awarded
    "I" -> TitleStatus.Hypothesised
    else -> TitleStatus.Unknown
}

private fun formatGrade(grade: Float?, base: Float?): String? {
    val value = grade?.takeIf { it > 0f } ?: return null
    val numerator = value.formatTrimmed()
    val denominator = base?.takeIf { it > 0f }?.formatTrimmed()
    return if (denominator != null) "$numerator/$denominator" else numerator
}

private fun Float.formatTrimmed(): String =
    if (this % 1f == 0f) toInt().toString() else toString()

private fun formatNumber(value: Float): String = value.formatTrimmed()

/**
 * Re-formats to a clean dd/MM/yyyy (drops the "00:00:00" tail Esse3 attaches), keeping the raw
 * string when it doesn't parse.
 */
private fun formatDate(raw: String?): String? =
    raw.parseEsse3Date()?.format(Esse3DateFormat) ?: raw.clean()
