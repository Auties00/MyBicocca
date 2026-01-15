package it.attendance100.mybicocca.data.exception

/**
 * Exception thrown when an error occurs during Elearning (Moodle) API operations.
 *
 * @property errorCode The Moodle error code, if available.
 * @property errorMessage The error message describing what went wrong.
 */
class ElearningException(
    val errorCode: String?,
    val errorMessage: String
) : Exception(errorMessage) {

}
