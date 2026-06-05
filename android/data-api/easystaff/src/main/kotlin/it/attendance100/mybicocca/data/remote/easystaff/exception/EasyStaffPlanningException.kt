package it.attendance100.mybicocca.data.remote.easystaff.exception

import it.attendance100.mybicocca.data.remote.common.exception.ApiRequestException

/**
 * Exception thrown when the Portale Planning backend rejects a request with a structured error
 * response.
 *
 * The Portale Planning API reports errors as `{"message": "<snake_case_key>", "data": [...]}`
 * with a non-success status code. Known keys include:
 * - `not_found` - the reservation code passed to the manage endpoint does not exist
 * - `reservation_not_found` - the reservation code passed to the info endpoint does not exist
 *
 * Responses with a non-success status code but no parsable error body are reported as
 * [ApiRequestException] instead.
 *
 * @property errorKey The machine-readable error key returned by the backend
 * (e.g. `reservation_not_found`).
 */
class EasyStaffPlanningException(
    val errorKey: String,
    statusCode: Int,
    message: String = errorKey
) : ApiRequestException(statusCode, message)
