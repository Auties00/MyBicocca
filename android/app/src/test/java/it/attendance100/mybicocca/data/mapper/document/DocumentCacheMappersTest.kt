package it.attendance100.mybicocca.data.mapper.document

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.document.AcademicTitleEntity
import it.attendance100.mybicocca.data.local.document.StudentBadgeEntity
import it.attendance100.mybicocca.data.local.document.TitleAttributeEntity
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.model.document.BadgeBlobId
import it.attendance100.mybicocca.domain.model.document.BadgeId
import it.attendance100.mybicocca.domain.model.document.StudentBadge
import it.attendance100.mybicocca.domain.model.document.TitleAttribute
import it.attendance100.mybicocca.domain.model.document.TitleCategory
import it.attendance100.mybicocca.domain.model.document.TitleField
import it.attendance100.mybicocca.domain.model.document.TitleStatus
import org.junit.Test
import java.time.LocalDate

/**
 * Round-trip checks for the offline document mirror: value classes unwrap to longs, enums
 * round-trip by name (status -> Unknown / category -> Italian fallback), dates round-trip as
 * ISO-8601 strings, and an attribute whose field no longer maps to a known enum is dropped.
 */
class DocumentCacheMappersTest {

    private val careerId = CareerId(321L)

    private fun badge(blobId: BadgeBlobId? = BadgeBlobId(9L), createdOn: LocalDate? = LocalDate.of(2023, 1, 1)) =
        StudentBadge(
            id = BadgeId(7L),
            blobId = blobId,
            hasFrontImage = true,
            hasRearImage = false,
            rfid = "RF",
            studentNumber = "123456",
            fullName = "Mario Rossi",
            courseDescription = "Informatica",
            facultyDescription = "Scienze",
            academicYear = 2023,
            delivered = true,
            cancelled = false,
            returned = false,
            createdOn = createdOn,
            printedOn = null,
            deliveredOn = LocalDate.of(2023, 2, 2),
        )

    @Test
    fun `badge round-trips unwrapping value classes and dates`() {
        val domain = badge()
        val entity = domain.toEntity(careerId)

        assertThat(entity.careerId).isEqualTo(321L)
        assertThat(entity.badgeId).isEqualTo(7L)
        assertThat(entity.blobId).isEqualTo(9L)
        assertThat(entity.createdOn).isEqualTo("2023-01-01")
        assertThat(entity.deliveredOn).isEqualTo("2023-02-02")

        assertThat(entity.toDomain()).isEqualTo(domain)
    }

    @Test
    fun `badge with null blob and unparseable date drops to null`() {
        val entity = badge(blobId = null).toEntity(careerId).copy(createdOn = "bad")
        val back = entity.toDomain()
        assertThat(back.blobId).isNull()
        assertThat(back.createdOn).isNull()
    }

    @Test
    fun `title round-trips scalar fields and enums`() {
        val title = AcademicTitle(
            id = "sup-1",
            category = TitleCategory.HighSchool,
            status = TitleStatus.Awarded,
            typeDescription = "Maturità",
            subject = null,
            institution = "Liceo",
            country = "Italia",
            year = "2019",
            grade = "100/100",
            cumLaude = true,
            valueDeclarationFiled = false,
            attributes = emptyList(),
        )
        val entity = title.toEntity(careerId, order = 2)
        assertThat(entity.category).isEqualTo("HighSchool")
        assertThat(entity.status).isEqualTo("Awarded")
        assertThat(entity.cacheOrder).isEqualTo(2)

        val back = entity.toDomain(attributes = emptyList())
        assertThat(back).isEqualTo(title)
    }

    @Test
    fun `title entity with unknown category falls back to Italian and unknown status to Unknown`() {
        val entity = AcademicTitleEntity(
            careerId = 1L,
            titleId = "x",
            cacheOrder = 0,
            category = "Galaxy",
            status = "Mysterious",
            typeDescription = null,
            subject = null,
            institution = null,
            country = null,
            year = null,
            grade = null,
            cumLaude = false,
            valueDeclarationFiled = false,
        )
        val back = entity.toDomain(emptyList())
        assertThat(back.category).isEqualTo(TitleCategory.Italian)
        assertThat(back.status).isEqualTo(TitleStatus.Unknown)
    }

    @Test
    fun `attribute entities preserve their order`() {
        val title = AcademicTitle(
            id = "sup-1",
            category = TitleCategory.HighSchool,
            status = TitleStatus.Awarded,
            typeDescription = null,
            subject = null,
            institution = null,
            country = null,
            year = null,
            grade = null,
            cumLaude = false,
            valueDeclarationFiled = false,
            attributes = listOf(
                TitleAttribute(TitleField.Institution, "Liceo"),
                TitleAttribute(TitleField.City, "Milano"),
            ),
        )
        val entities = title.toAttributeEntities(careerId)
        assertThat(entities).hasSize(2)
        assertThat(entities[0].attrOrder).isEqualTo(0)
        assertThat(entities[0].field).isEqualTo("Institution")
        assertThat(entities[1].attrOrder).isEqualTo(1)
        assertThat(entities[1].field).isEqualTo("City")
        assertThat(entities.all { it.titleId == "sup-1" && it.careerId == 321L }).isTrue()
    }

    @Test
    fun `attribute round-trips a known field`() {
        val attribute = TitleAttribute(TitleField.ThesisTitle, "Una tesi")
        val back = attribute.toEntity(careerId, "sup-1", 0).toDomain()
        assertThat(back).isEqualTo(attribute)
    }

    @Test
    fun `attribute with an unknown stored field is dropped`() {
        val entity = TitleAttributeEntity(
            careerId = 1L,
            titleId = "sup-1",
            attrOrder = 0,
            field = "RemovedField",
            value = "v",
        )
        assertThat(entity.toDomain()).isNull()
    }
}
