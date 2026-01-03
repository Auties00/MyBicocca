package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningRemoveUserDeviceRequest(
    private val uuid: String,
    private val appId: String? = null
) : ElearningRequest<ElearningRemoveUserDeviceResponse> {
    override val functionName = "core_user_remove_user_device"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("uuid", uuid)
        appId?.let { formData.append("appid", it) }
    }
}
