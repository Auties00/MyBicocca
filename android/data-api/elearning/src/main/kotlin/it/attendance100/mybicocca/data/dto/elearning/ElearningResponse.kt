package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable(with = ElearningResponseSerializer::class)
sealed interface ElearningResponse<out DATA : ElearningResponseData> {
    @Serializable
    data class Success<DATA : ElearningResponseData>(
        @SerialName("data")
        val data: DATA
    ) : ElearningResponse<DATA>

    @Serializable
    data class Error(
        @SerialName("error")
        val message: String
    ) : ElearningResponse<Nothing>
}

@Suppress("UNCHECKED_CAST")
class ElearningResponseSerializer<DATA : ElearningResponseData>(
    private val dataSerializer: KSerializer<DATA>
) : JsonContentPolymorphicSerializer<ElearningResponse<DATA>>(
    ElearningResponse::class as kotlin.reflect.KClass<ElearningResponse<DATA>>
) {
    override fun selectDeserializer(element: JsonElement): KSerializer<out ElearningResponse<DATA>> {
        val jsonObject = element.jsonObject
        return if(jsonObject["data"] != null) {
            ElearningResponse.Success.serializer(dataSerializer) as KSerializer<ElearningResponse<DATA>>
        } else {
            ElearningResponse.Error.serializer() as KSerializer<ElearningResponse<DATA>>
        }
    }
}