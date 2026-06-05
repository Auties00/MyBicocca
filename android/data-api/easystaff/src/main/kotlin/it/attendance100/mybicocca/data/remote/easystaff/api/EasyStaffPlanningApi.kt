package it.attendance100.mybicocca.data.remote.easystaff.api

import io.ktor.client.HttpClient
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningPortal
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningPortalSummary
import kotlinx.serialization.json.Json

/**
 * API for appointment booking operations (Portale Planning).
 *
 * The Portale Planning backend hosts the university booking portals (see
 * [EasyStaffPlanningPortal]), each exposed here as a portal-bound sub-API:
 * - [informationDesks] - the student information desks ("Sportelli Informativi"), with the
 *   full anonymous booking lifecycle
 * - [studyRooms] - the legacy study rooms portal, limited to configuration and discovery
 *   because its public login is mandatory and currently blocked
 *
 * @see EasyStaffPlanningPortalApi for the discovery operations of a portal
 * @see EasyStaffPlanningBookingApi for the booking lifecycle of a portal
 */
class EasyStaffPlanningApi(
    client: HttpClient,
    json: Json
) : EasyStaffPlanningAbstractApi(client, json) {

    /**
     * The student information desks portal ("Sportelli Informativi"), used to book
     * appointments with the student administration offices. Anonymous booking is fully
     * supported.
     */
    val informationDesks = EasyStaffPlanningBookingApi(EasyStaffPlanningPortal.INFORMATION_DESKS, client, json)

    /**
     * The legacy study rooms portal ("Prenotazione Aule Studio"). Public login is mandatory
     * and currently blocked, so the discovery operations return empty listings for anonymous
     * users. Study room booking has since moved to the Affluences platform.
     */
    val studyRooms = EasyStaffPlanningPortalApi(EasyStaffPlanningPortal.STUDY_ROOMS, client, json)

    /**
     * Retrieves the publicly listed booking portals.
     *
     * Note that the backend only lists portals flagged as public: the information desks
     * portal is reachable through [EasyStaffPlanningPortalApi.getPortal] but absent from
     * this index.
     *
     * @return The publicly listed portals
     */
    suspend fun getPortals(): List<EasyStaffPlanningPortalSummary> =
        executeGet(
            portal = null,
            path = "/cliente/index"
        )
}
