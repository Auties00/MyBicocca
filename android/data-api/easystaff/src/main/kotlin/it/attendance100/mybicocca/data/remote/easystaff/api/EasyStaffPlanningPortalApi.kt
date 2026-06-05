package it.attendance100.mybicocca.data.remote.easystaff.api

import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningArea
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningAreaGroup
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningAreaGroupsEnvelope
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningAreasEnvelope
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFormField
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningPortal
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningPortalConfig
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningService
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningServiceGroupsEnvelope
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningServicesEnvelope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.URLEncoder

/**
 * API surface bound to a single Portale Planning portal, with the operations every portal
 * exposes to anonymous users: configuration and service/area discovery.
 *
 * Portals whose public login is optional (see
 * [EasyStaffPlanningPortalConfig.settings]) additionally support the anonymous booking
 * lifecycle through [EasyStaffPlanningBookingApi]. Portals with mandatory login return empty
 * listings from the discovery operations.
 *
 * @property portal The portal every request of this instance targets.
 */
open class EasyStaffPlanningPortalApi(
    val portal: EasyStaffPlanningPortal,
    client: HttpClient,
    json: Json
) : EasyStaffPlanningAbstractApi(client, json) {

    /**
     * Retrieves the full configuration of the portal.
     *
     * @return The configuration of the portal
     */
    suspend fun getPortal(): EasyStaffPlanningPortalConfig =
        executeGet(
            portal = portal,
            path = "/cliente/show/${portal.code}"
        )

    /**
     * Retrieves the service group names of the portal.
     *
     * @return The names of the service groups
     */
    suspend fun getServiceGroups(): List<String> =
        executeGet<EasyStaffPlanningServiceGroupsEnvelope>(
            portal = portal,
            path = "/servizi/raggruppamenti_servizi/${portal.id}"
        ).groups.map { it.name }

    /**
     * Retrieves the bookable services of the portal.
     *
     * @param serviceGroup When provided, only services of this group (see [getServiceGroups])
     * are returned
     * @return The services of the portal
     */
    suspend fun getServices(serviceGroup: String? = null): List<EasyStaffPlanningService> =
        executeGet<EasyStaffPlanningServicesEnvelope>(
            portal = portal,
            path = "/servizi/${portal.id}",
            headers = entryFilters {
                if (serviceGroup != null) {
                    put("raggruppamento_servizi", buildJsonObject {
                        put("nome", serviceGroup)
                    })
                }
            }
        ).services

    /**
     * Retrieves the area groups of the portal.
     *
     * @return The area groups of the portal
     */
    suspend fun getAreaGroups(): List<EasyStaffPlanningAreaGroup> =
        executeGet<EasyStaffPlanningAreaGroupsEnvelope>(
            portal = portal,
            path = "/servizi/raggruppamenti_area/${portal.id}"
        ).groups

    /**
     * Retrieves the areas of the portal.
     *
     * When [serviceId] is provided, only the areas offering that service are returned, and
     * each area embeds the services bookable there with their full booking constraints (see
     * [EasyStaffPlanningArea.services]). Without it the backend returns every area but omits
     * the embedded services.
     *
     * @param serviceId When provided, only areas offering this service are returned
     * @return The areas of the portal
     */
    suspend fun getAreas(serviceId: Int? = null): List<EasyStaffPlanningArea> =
        executeGet<EasyStaffPlanningAreasEnvelope>(
            portal = portal,
            path = "/aree/${portal.id}",
            headers = entryFilters {
                if (serviceId != null) {
                    put("servizio", buildJsonObject {
                        put("id", serviceId)
                    })
                }
            }
        ).areas

    /**
     * Retrieves the primary form field of the portal, whose value (the email address) keys
     * every reservation.
     *
     * @return The primary form field
     */
    suspend fun getPrimaryField(): EasyStaffPlanningFormField =
        executeGet(
            portal = portal,
            path = "/cliente/${portal.id}/primaria"
        )

    // The list endpoints read their filters from the entry-filters cookie the web front-end
    // persists; the value is the URL-encoded JSON of the selected filters
    private fun entryFilters(builder: JsonObjectBuilder.() -> Unit): Map<String, String> {
        val filters = buildJsonObject(builder)
        if (filters.isEmpty()) {
            return emptyMap()
        }
        val encoded = URLEncoder.encode(filters.toString(), Charsets.UTF_8).replace("+", "%20")
        return mapOf(HttpHeaders.Cookie to "entry-filters=$encoded")
    }
}
