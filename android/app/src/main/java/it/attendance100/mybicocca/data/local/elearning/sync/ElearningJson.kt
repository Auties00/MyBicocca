package it.attendance100.mybicocca.data.local.elearning.sync

import kotlinx.serialization.json.Json

/**
 * The Json instance shared by the e-learning entities and mappers for the JSON blobs
 * embedded in Room columns (module contents/dates, syllabus, submission status,
 * attachments, quiz answers). Lenient with unknown keys ignored, so rows written by
 * older schema revisions still decode; defaults are encoded so absent-with-default
 * fields survive the round trip.
 */
internal val ElearningJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}
