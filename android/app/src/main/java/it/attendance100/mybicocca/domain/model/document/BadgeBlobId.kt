package it.attendance100.mybicocca.domain.model.document

/**
 * Identifier of the Esse3 blob that stores a student card's printable artwork. Obtained from
 * the badge metadata and required to download the front/rear card images.
 *
 * @property value The numeric blob id assigned by Esse3.
 */
@JvmInline
value class BadgeBlobId(val value: Long)
