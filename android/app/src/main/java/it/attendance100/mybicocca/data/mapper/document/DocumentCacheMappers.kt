package it.attendance100.mybicocca.data.mapper.document

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
import java.time.LocalDate

// Entity <-> domain mapping for the offline document mirrors. Value classes unwrap to their
// underlying longs; enums round-trip by name with a fallback (Unknown for status, dropped for
// unrecognised category/field); dates round-trip through ISO-8601 strings, with unparseable
// values dropped to null. A title's attribute list is mapped to/from the sibling child table.

fun StudentBadge.toEntity(careerId: CareerId): StudentBadgeEntity = StudentBadgeEntity(
    careerId = careerId.value,
    badgeId = id.value,
    blobId = blobId?.value,
    hasFrontImage = hasFrontImage,
    hasRearImage = hasRearImage,
    rfid = rfid,
    studentNumber = studentNumber,
    fullName = fullName,
    courseDescription = courseDescription,
    facultyDescription = facultyDescription,
    academicYear = academicYear,
    delivered = delivered,
    cancelled = cancelled,
    returned = returned,
    createdOn = createdOn?.toString(),
    printedOn = printedOn?.toString(),
    deliveredOn = deliveredOn?.toString(),
)

fun StudentBadgeEntity.toDomain(): StudentBadge = StudentBadge(
    id = BadgeId(badgeId),
    blobId = blobId?.let { BadgeBlobId(it) },
    hasFrontImage = hasFrontImage,
    hasRearImage = hasRearImage,
    rfid = rfid,
    studentNumber = studentNumber,
    fullName = fullName,
    courseDescription = courseDescription,
    facultyDescription = facultyDescription,
    academicYear = academicYear,
    delivered = delivered,
    cancelled = cancelled,
    returned = returned,
    createdOn = createdOn.toLocalDateOrNull(),
    printedOn = printedOn.toLocalDateOrNull(),
    deliveredOn = deliveredOn.toLocalDateOrNull(),
)

fun AcademicTitle.toEntity(careerId: CareerId, order: Int): AcademicTitleEntity = AcademicTitleEntity(
    careerId = careerId.value,
    titleId = id,
    cacheOrder = order,
    category = category.name,
    status = status.name,
    typeDescription = typeDescription,
    subject = subject,
    institution = institution,
    country = country,
    year = year,
    grade = grade,
    cumLaude = cumLaude,
    valueDeclarationFiled = valueDeclarationFiled,
)

fun AcademicTitle.toAttributeEntities(careerId: CareerId): List<TitleAttributeEntity> =
    attributes.mapIndexed { index, attribute -> attribute.toEntity(careerId, id, index) }

fun AcademicTitleEntity.toDomain(attributes: List<TitleAttribute>): AcademicTitle = AcademicTitle(
    id = titleId,
    category = category.toTitleCategory(),
    status = status.toTitleStatus(),
    typeDescription = typeDescription,
    subject = subject,
    institution = institution,
    country = country,
    year = year,
    grade = grade,
    cumLaude = cumLaude,
    valueDeclarationFiled = valueDeclarationFiled,
    attributes = attributes,
)

fun TitleAttribute.toEntity(careerId: CareerId, titleId: String, order: Int): TitleAttributeEntity =
    TitleAttributeEntity(
        careerId = careerId.value,
        titleId = titleId,
        attrOrder = order,
        field = field.name,
        value = value,
    )

// Drops attributes whose field no longer maps to a known enum (e.g. after a schema change),
// keeping the round-trip lossless for everything still recognised.
fun TitleAttributeEntity.toDomain(): TitleAttribute? =
    field.toTitleFieldOrNull()?.let { TitleAttribute(it, value) }

private fun String.toTitleCategory(): TitleCategory =
    runCatching { TitleCategory.valueOf(this) }.getOrDefault(TitleCategory.Italian)

private fun String.toTitleStatus(): TitleStatus =
    runCatching { TitleStatus.valueOf(this) }.getOrDefault(TitleStatus.Unknown)

private fun String.toTitleFieldOrNull(): TitleField? =
    runCatching { TitleField.valueOf(this) }.getOrNull()

private fun String?.toLocalDateOrNull(): LocalDate? =
    this?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
