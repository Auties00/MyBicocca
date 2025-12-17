package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class CallExternalFunctionsResponse(
    @SerializedName("responses") val responses: List<ExternalFunctionResponse>? = null
)