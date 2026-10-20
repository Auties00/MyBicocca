package it.attendance100.mybicocca.data.remote.elearning.dto

import it.attendance100.mybicocca.data.remote.elearning.exception.ElearningException

/**
 * Throws the first warning as an [ElearningException]. Functions such as
 * `mod_assign_save_submission` report a failure as warnings, with HTTP 200, rather than as an
 * error, so callers that need it to fail check their warnings with this.
 */
fun List<MoodleWarning>.throwIfPresent() {
    val warning = firstOrNull() ?: return
    val code = warning.warningCode.ifEmpty { null }
    throw ElearningException(code, warning.message.ifEmpty { code ?: "Moodle reported a warning" })
}
