package it.attendance100.mybicocca.data.remote.elearning.dto

/**
 * Marker interface for responses that come as JSON object from Moodle.
 */
sealed interface ElearningResponse

/**
 * Marker interface for responses that come as JSON arrays from Moodle.
 * The API will wrap the array in an object with the specified key.
 */
interface ElearningListResponse<T> : ElearningResponse {
    val items: List<T>
}