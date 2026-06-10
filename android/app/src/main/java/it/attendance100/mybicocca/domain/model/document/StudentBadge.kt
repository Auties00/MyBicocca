package it.attendance100.mybicocca.domain.model.document

import java.time.LocalDate

/**
 * The official Esse3 university card ("tessera"): the physical/RFID card minted by the
 * registry. Distinct from the e-learning badge shown on the profile page.
 *
 * @property id Esse3 badge identifier.
 * @property blobId Identifier of the card's printable artwork; present only when the server
 *   holds one. Null means there is no downloadable front/rear image, which is the common case —
 *   most cards expose metadata only.
 * @property hasFrontImage Whether a front-side image can be downloaded from [blobId].
 * @property hasRearImage Whether a rear-side image can be downloaded from [blobId].
 * @property rfid RFID code printed on the card.
 * @property studentNumber Student registration number (matricola) the card is issued for.
 * @property fullName Card holder's full name.
 * @property courseDescription Course of study printed on the card.
 * @property facultyDescription Faculty/department printed on the card.
 * @property academicYear Starting solar year of the enrollment the card refers to.
 * @property delivered Whether the card has been handed to the student.
 * @property cancelled Whether the card has been voided ("annullato").
 * @property returned Whether the card has been given back ("restituito").
 * @property createdOn Date the card record was created.
 * @property printedOn Date the card was printed.
 * @property deliveredOn Date the card was handed to the student.
 */
data class StudentBadge(
    val id: BadgeId,
    val blobId: BadgeBlobId?,
    val hasFrontImage: Boolean,
    val hasRearImage: Boolean,
    val rfid: String?,
    val studentNumber: String?,
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
