package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.demo.DemoCareer
import it.attendance100.mybicocca.demo.DemoStudent
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.model.document.BadgeBlobId
import it.attendance100.mybicocca.domain.model.document.BadgeId
import it.attendance100.mybicocca.domain.model.document.StudentBadge
import it.attendance100.mybicocca.domain.model.document.TitleAttribute
import it.attendance100.mybicocca.domain.model.document.TitleCategory
import it.attendance100.mybicocca.domain.model.document.TitleField
import it.attendance100.mybicocca.domain.model.document.TitleStatus
import it.attendance100.mybicocca.domain.repository.DocumentRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** A delivered badge without scanned images, and the high-school diploma the student enrolled with. */
@Singleton
class DemoDocumentRepository @Inject constructor() : DocumentRepository {

    override suspend fun getBadge(careerId: CareerId): StudentBadge = StudentBadge(
        id = BadgeId(880001),
        blobId = null,
        hasFrontImage = false,
        hasRearImage = false,
        rfid = "04A1B2C3D4E5F6",
        studentNumber = DemoStudent.STUDENT_NUMBER,
        fullName = DemoStudent.FULL_NAME.uppercase(),
        courseDescription = DemoCareer.DEGREE,
        facultyDescription = "Dipartimento di Informatica, Sistemistica e Comunicazione",
        academicYear = 2024,
        delivered = true,
        cancelled = false,
        returned = false,
        createdOn = LocalDate.of(2024, 9, 20),
        printedOn = LocalDate.of(2024, 9, 27),
        deliveredOn = LocalDate.of(2024, 10, 7),
    )

    override suspend fun getTitles(careerId: CareerId): List<AcademicTitle> = listOf(
        AcademicTitle(
            id = "TSS-1",
            category = TitleCategory.HighSchool,
            status = TitleStatus.Awarded,
            typeDescription = "Diploma di maturità scientifica",
            subject = "Liceo scientifico, opzione scienze applicate",
            institution = "Liceo Scientifico Statale di Milano",
            country = "Italia",
            year = "2024",
            grade = "92/100",
            cumLaude = false,
            valueDeclarationFiled = false,
            attributes = listOf(
                TitleAttribute(TitleField.GraduationYear, "2024"),
                TitleAttribute(TitleField.City, "Milano"),
                TitleAttribute(TitleField.Province, "MI"),
                TitleAttribute(TitleField.DurationYears, "5"),
            ),
        ),
    )

    override suspend fun getBadgeImage(blobId: BadgeBlobId, rear: Boolean): ByteArray = ByteArray(0)
}
