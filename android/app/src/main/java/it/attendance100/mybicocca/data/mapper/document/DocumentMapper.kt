package it.attendance100.mybicocca.data.mapper.document

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BadgeData
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ForeignTitlePerson
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3HighSchoolDiplomaPerson
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ItalianTitlePerson
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PersonTitles
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.model.document.BadgeBlobId
import it.attendance100.mybicocca.domain.model.document.BadgeId
import it.attendance100.mybicocca.domain.model.document.StudentBadge
import it.attendance100.mybicocca.domain.model.document.TitleCategory
import it.attendance100.mybicocca.domain.model.document.TitleStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        matricola = matricola?.trim()?.takeIf { it.isNotEmpty() },
        fullName = name,
        courseDescription = courseOfStudyDescription?.trim()?.takeIf { it.isNotEmpty() },
        facultyDescription = facultyDescription?.trim()?.takeIf { it.isNotEmpty() },
        academicYear = academicYearAnnualEnrollment,
        // Esse3 flags are 0/1 longs; treat anything non-zero as set. annFlg = restituito,
        // restFlg = annullato, consFlg = consegnato (see Esse3BadgeData KDoc).
        delivered = (consentFlag ?: 0L) != 0L,
        cancelled = (restFlag ?: 0L) != 0L,
        returned = (yearFlag ?: 0L) != 0L,
        createdOn = startDate.toEsse3LocalDate(),
        printedOn = printDate.toEsse3LocalDate(),
        deliveredOn = deliveryDate.toEsse3LocalDate(),
    )
}

// The /titoli payload nests three independent title families; flatten them into one list.
fun Esse3PersonTitles.toAcademicTitles(): List<AcademicTitle> =
    SUP.map { it.toAcademicTitle() } +
        italianTitle.map { it.toAcademicTitle() } +
        foreignTitle.map { it.toAcademicTitle() }

private fun Esse3HighSchoolDiplomaPerson.toAcademicTitle(): AcademicTitle =
    AcademicTitle(
        category = TitleCategory.HighSchool,
        typeDescription = higherTitleTypesDescription?.trim()?.takeIf { it.isNotEmpty() },
        subject = null,
        institution = schoolName?.trim()?.takeIf { it.isNotEmpty() },
        status = italianTitleStatusCode.toTitleStatus(),
        year = highSchoolGraduationYear,
        grade = formatGrade(grade, maxGrade),
        cumLaude = (cumLaudeFlag ?: 0) == 1,
        country = deliveryNationDescription?.trim()?.takeIf { it.isNotEmpty() },
        valueDeclarationFiled = (valueDeclarationFlag ?: 0) == 1,
    )

private fun Esse3ItalianTitlePerson.toAcademicTitle(): AcademicTitle =
    AcademicTitle(
        category = TitleCategory.Italian,
        typeDescription = italianTitleTypesDescription?.trim()?.takeIf { it.isNotEmpty() },
        subject = (vDecodeTitleTypeCodeDescription ?: courseOfStudyDescription)
            ?.trim()?.takeIf { it.isNotEmpty() },
        institution = p06UniversitiesDescription?.trim()?.takeIf { it.isNotEmpty() },
        status = italianTitleStatusFrom(italianTitleStatesDescription),
        year = academicYearTitleAward,
        grade = formatGrade(grade, baseGrade?.toFloat()),
        cumLaude = (cumLaude ?: 0) == 1,
        country = null,
        valueDeclarationFiled = false,
    )

private fun Esse3ForeignTitlePerson.toAcademicTitle(): AcademicTitle =
    AcademicTitle(
        category = TitleCategory.Foreign,
        typeDescription = titleStatusTypesDescription?.trim()?.takeIf { it.isNotEmpty() },
        subject = foreignCourseOfStudy?.trim()?.takeIf { it.isNotEmpty() },
        institution = (p06SiteDescription ?: universityDescription ?: p01ForeignTestDescription)
            ?.trim()?.takeIf { it.isNotEmpty() },
        status = foreignTitleStatusCode.toTitleStatus(),
        year = academicYearAwardId?.toInt(),
        grade = formatGrade(grade, baseGrade?.toFloat()),
        cumLaude = (cumLaude ?: 0) == 1,
        country = p01NationDescription?.trim()?.takeIf { it.isNotEmpty() },
        valueDeclarationFiled = (valueDeclarationFlag ?: 0) == 1,
    )

// Esse3 status code: C = Conseguito (awarded), I = In ipotesi (hypothesised).
private fun String?.toTitleStatus(): TitleStatus = when (this?.trim()?.uppercase()) {
    "C" -> TitleStatus.Awarded
    "I" -> TitleStatus.Hypothesised
    else -> TitleStatus.Unknown
}

// Italian titles only carry the localized description (e.g. "Conseguito", "In ipotesi").
private fun italianTitleStatusFrom(description: String?): TitleStatus {
    val text = description?.trim()?.lowercase() ?: return TitleStatus.Unknown
    return when {
        text.startsWith("conseg") -> TitleStatus.Awarded
        text.contains("ipotesi") -> TitleStatus.Hypothesised
        else -> TitleStatus.Unknown
    }
}

private fun formatGrade(grade: Float?, base: Float?): String? {
    val value = grade?.takeIf { it > 0f } ?: return null
    val numerator = value.formatTrimmed()
    val denominator = base?.takeIf { it > 0f }?.formatTrimmed()
    return if (denominator != null) "$numerator/$denominator" else numerator
}

private fun Float.formatTrimmed(): String =
    if (this % 1f == 0f) toInt().toString() else toString()

// Esse3 dates are dd/MM/yyyy, sometimes with a trailing time; strip it and tolerate failure.
private val ESSE3_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun String?.toEsse3LocalDate(): LocalDate? {
    val datePart = this?.trim()?.takeIf { it.isNotEmpty() }
        ?.substringBefore('T')?.substringBefore(' ') ?: return null
    return runCatching { LocalDate.parse(datePart, ESSE3_DATE_FORMAT) }.getOrNull()
}
