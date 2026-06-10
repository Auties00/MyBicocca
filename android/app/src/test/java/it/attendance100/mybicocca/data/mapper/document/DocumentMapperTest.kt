package it.attendance100.mybicocca.data.mapper.document

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BadgeData
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3HighSchoolDiplomaPerson
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PersonTitles
import it.attendance100.mybicocca.domain.model.document.BadgeBlobId
import it.attendance100.mybicocca.domain.model.document.BadgeId
import it.attendance100.mybicocca.domain.model.document.TitleCategory
import it.attendance100.mybicocca.domain.model.document.TitleField
import it.attendance100.mybicocca.domain.model.document.TitleStatus
import org.junit.Test
import java.time.LocalDate

/**
 * Covers the Esse3 badge + titles mapping. The badge flags are intentionally swapped on the
 * wire (consFlg=delivered, restFlg=cancelled, annFlg=returned) and any non-zero long counts as
 * set; the high-school title mapping exercises status-code decoding, grade formatting, cum
 * laude, and the Sì/No flag attribute rows.
 */
class DocumentMapperTest {

    @Test
    fun `badge maps the swapped lifecycle flags by their domain meaning`() {
        val badge = Esse3BadgeData(
            consentFlag = 1L,
            restFlag = 0L,
            yearFlag = 1L,
        ).toStudentBadge()
        assertThat(badge.delivered).isTrue()
        assertThat(badge.cancelled).isFalse()
        assertThat(badge.returned).isTrue()
    }

    @Test
    fun `badge treats any non-zero long flag as set`() {
        val badge = Esse3BadgeData(restFlag = 5L).toStudentBadge()
        assertThat(badge.cancelled).isTrue()
    }

    @Test
    fun `badge treats null flags as not set`() {
        val badge = Esse3BadgeData().toStudentBadge()
        assertThat(badge.delivered).isFalse()
        assertThat(badge.cancelled).isFalse()
        assertThat(badge.returned).isFalse()
    }

    @Test
    fun `badge assembles the full name from name and surname`() {
        val badge = Esse3BadgeData(name = " Mario ", surname = " Rossi ").toStudentBadge()
        assertThat(badge.fullName).isEqualTo("Mario Rossi")
    }

    @Test
    fun `badge full name is null when both parts blank`() {
        val badge = Esse3BadgeData(name = "  ", surname = null).toStudentBadge()
        assertThat(badge.fullName).isNull()
    }

    @Test
    fun `badge defaults missing id to zero and image flags off`() {
        val badge = Esse3BadgeData(badgeId = null, frontImagePresent = null, rearImagePresent = 1).toStudentBadge()
        assertThat(badge.id).isEqualTo(BadgeId(0L))
        assertThat(badge.hasFrontImage).isFalse()
        assertThat(badge.hasRearImage).isTrue()
    }

    @Test
    fun `badge maps blob id when present`() {
        val badge = Esse3BadgeData(badgeBlobId = 42L).toStudentBadge()
        assertThat(badge.blobId).isEqualTo(BadgeBlobId(42L))
    }

    @Test
    fun `badge erases blank rfid and student number`() {
        val badge = Esse3BadgeData(rfid = "  ", matricola = "  ").toStudentBadge()
        assertThat(badge.rfid).isNull()
        assertThat(badge.studentNumber).isNull()
    }

    @Test
    fun `badge keeps a non-blank rfid trimmed`() {
        val badge = Esse3BadgeData(rfid = " ABC123 ").toStudentBadge()
        assertThat(badge.rfid).isEqualTo("ABC123")
    }

    @Test
    fun `badge parses the esse3 dd MM yyyy dates`() {
        val badge = Esse3BadgeData(
            startDate = "01/09/2023 00:00:00",
            printDate = "02/09/2023",
            deliveryDate = "garbage",
        ).toStudentBadge()
        assertThat(badge.createdOn).isEqualTo(LocalDate.of(2023, 9, 1))
        assertThat(badge.printedOn).isEqualTo(LocalDate.of(2023, 9, 2))
        assertThat(badge.deliveredOn).isNull()
    }

    @Test
    fun `titles flattens the three families into one ordered list`() {
        val titles = Esse3PersonTitles(
            SUP = listOf(highSchool(id = 1), highSchool(id = 2)),
        ).toAcademicTitles()
        assertThat(titles).hasSize(2)
        assertThat(titles.map { it.id }).containsExactly("sup-1", "sup-2").inOrder()
        assertThat(titles.all { it.category == TitleCategory.HighSchool }).isTrue()
    }

    @Test
    fun `high-school title maps status code C to Awarded`() {
        val title = Esse3PersonTitles(SUP = listOf(highSchool(statusCode = "C"))).toAcademicTitles().first()
        assertThat(title.status).isEqualTo(TitleStatus.Awarded)
    }

    @Test
    fun `high-school title maps status code I to Hypothesised`() {
        val title = Esse3PersonTitles(SUP = listOf(highSchool(statusCode = "i"))).toAcademicTitles().first()
        assertThat(title.status).isEqualTo(TitleStatus.Hypothesised)
    }

    @Test
    fun `high-school title maps unknown status code to Unknown`() {
        val title = Esse3PersonTitles(SUP = listOf(highSchool(statusCode = "X"))).toAcademicTitles().first()
        assertThat(title.status).isEqualTo(TitleStatus.Unknown)
    }

    @Test
    fun `high-school grade formats as grade over base trimming whole numbers`() {
        val title = Esse3PersonTitles(
            SUP = listOf(highSchool(grade = 84f, maxGrade = 100f)),
        ).toAcademicTitles().first()
        assertThat(title.grade).isEqualTo("84/100")
    }

    @Test
    fun `high-school grade with no base renders just the numerator`() {
        val title = Esse3PersonTitles(
            SUP = listOf(highSchool(grade = 90f, maxGrade = 0f)),
        ).toAcademicTitles().first()
        assertThat(title.grade).isEqualTo("90")
    }

    @Test
    fun `high-school grade is null when not positive`() {
        val title = Esse3PersonTitles(
            SUP = listOf(highSchool(grade = 0f, maxGrade = 100f)),
        ).toAcademicTitles().first()
        assertThat(title.grade).isNull()
    }

    @Test
    fun `high-school cum laude reflects the lode flag`() {
        val awarded = Esse3PersonTitles(SUP = listOf(highSchool(cumLaudeFlag = 1))).toAcademicTitles().first()
        val not = Esse3PersonTitles(SUP = listOf(highSchool(cumLaudeFlag = 0))).toAcademicTitles().first()
        assertThat(awarded.cumLaude).isTrue()
        assertThat(not.cumLaude).isFalse()
    }

    @Test
    fun `high-school value-declaration flag renders a Si attribute row`() {
        val title = Esse3PersonTitles(
            SUP = listOf(highSchool(valueDeclarationFlag = 1)),
        ).toAcademicTitles().first()
        val row = title.attributes.firstOrNull { it.field == TitleField.ValueDeclaration }
        assertThat(row?.value).isEqualTo("Sì")
        assertThat(title.valueDeclarationFiled).isTrue()
    }

    @Test
    fun `high-school evaluated flag of zero renders a No attribute row`() {
        val title = Esse3PersonTitles(
            SUP = listOf(highSchool(evaluatedFlag = 0L)),
        ).toAcademicTitles().first()
        val row = title.attributes.firstOrNull { it.field == TitleField.Evaluated }
        assertThat(row?.value).isEqualTo("No")
    }

    @Test
    fun `high-school institution falls back from schoolName to schoolDescription`() {
        val title = Esse3PersonTitles(
            SUP = listOf(highSchool(schoolName = null, schoolDescription = "Liceo Tasso")),
        ).toAcademicTitles().first()
        assertThat(title.institution).isEqualTo("Liceo Tasso")
    }

    @Test
    fun `high-school graduation year promotes to the year field and attribute`() {
        val title = Esse3PersonTitles(
            SUP = listOf(highSchool(graduationYear = 2019)),
        ).toAcademicTitles().first()
        assertThat(title.year).isEqualTo("2019")
        assertThat(title.attributes.firstOrNull { it.field == TitleField.GraduationYear }?.value)
            .isEqualTo("2019")
    }

    private fun highSchool(
        id: Int? = 1,
        statusCode: String? = "C",
        grade: Float? = null,
        maxGrade: Float? = null,
        cumLaudeFlag: Int? = null,
        valueDeclarationFlag: Int? = null,
        evaluatedFlag: Long? = null,
        schoolName: String? = "Liceo",
        schoolDescription: String? = null,
        graduationYear: Int? = null,
    ) = Esse3HighSchoolDiplomaPerson(
        id = id,
        italianTitleStatusCode = statusCode,
        higherTitleTypesDescription = "Maturità classica",
        schoolName = schoolName,
        schoolDescription = schoolDescription,
        deliveryNationDescription = "Italia",
        highSchoolGraduationYear = graduationYear,
        grade = grade,
        maxGrade = maxGrade,
        cumLaudeFlag = cumLaudeFlag,
        valueDeclarationFlag = valueDeclarationFlag,
        evaluatedFlag = evaluatedFlag,
    )
}
