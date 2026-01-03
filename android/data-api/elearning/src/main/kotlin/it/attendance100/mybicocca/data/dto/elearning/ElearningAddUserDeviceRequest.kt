package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningAddUserDeviceRequest(
    private val appId: String,
    private val name: String,
    private val model: String,
    private val platform: String,
    private val version: String,
    private val pushId: String,
    private val uuid: String
) : ElearningRequest<ElearningAddUserDeviceResponse> {
    override val functionName = "core_user_add_user_device"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("appid", appId)
        formData.append("name", name)
        formData.append("model", model)
        formData.append("platform", platform)
        formData.append("version", version)
        formData.append("pushid", pushId)
        formData.append("uuid", uuid)
    }
}
