package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.Serializable

@Serializable
class ElearningGetPublicConfigRequestArgs : ElearningRequestArgs<ElearningGetPublicConfigResponseData> {
    override val methodName: String
        get() = "tool_mobile_get_public_config"
}