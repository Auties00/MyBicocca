package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.Serializable

@Serializable
class ElearningGetSiteInfoRequest : ElearningRequest<ElearningGetSiteInfoResponse> {
    override val functionName: String
        get() = "core_webservice_get_site_info"
}