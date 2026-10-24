package it.attendance100.mybicocca.data.remote.easystaff.api

import io.ktor.client.HttpClientConfig
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningPortal

/**
 * The Portale Planning API of [portal].
 *
 * @param httpClientConfig Additional configuration of the HTTP client.
 */
fun EasyStaffPlanningApi(
    portal: EasyStaffPlanningPortal,
    httpClientConfig: HttpClientConfig<*>.() -> Unit = {},
): EasyStaffPlanningApi = EasyStaffPlanningApi(portal.id, portal.code, httpClientConfig)
