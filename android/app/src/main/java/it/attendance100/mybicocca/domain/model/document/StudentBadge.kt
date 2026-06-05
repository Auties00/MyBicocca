package it.attendance100.mybicocca.domain.model.document

import java.time.LocalDate

// The official Esse3 university card ("tessera"). Distinct from the e-learning badge shown
// on the profile page: this is the physical/RFID card minted by the registry.
data class StudentBadge(
    val id: BadgeId,
    // Present only when the card has printable artwork on the server. Null means there is no
    // downloadable front/rear image (common — most cards expose metadata only).
    val blobId: BadgeBlobId?,
    val hasFrontImage: Boolean,
    val hasRearImage: Boolean,
    val rfid: String?,
    val matricola: String?,
    val fullName: String?,
    val courseDescription: String?,
    val facultyDescription: String?,
    val academicYear: Int?,
    val delivered: Boolean,
    val cancelled: Boolean,
    val returned: Boolean,
    val createdOn: LocalDate?,
    val printedOn: LocalDate?,
    val deliveredOn: LocalDate?,
)
