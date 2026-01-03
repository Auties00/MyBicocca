package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
sealed interface ElearningRequest<RESPONSE : ElearningResponse> {
    val functionName: String

    fun writeAdditionalData(formData: ParametersBuilder) {
        // Default: no additional data
    }
}