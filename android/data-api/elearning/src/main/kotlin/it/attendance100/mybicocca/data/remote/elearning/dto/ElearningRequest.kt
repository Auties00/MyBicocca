package it.attendance100.mybicocca.data.remote.elearning.dto

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
sealed interface ElearningRequest<RESPONSE : ElearningResponse> {
    val functionName: String

    /** Writes request-specific form parameters; the default implementation writes none. */
    fun writeAdditionalData(formData: ParametersBuilder) {
    }
}