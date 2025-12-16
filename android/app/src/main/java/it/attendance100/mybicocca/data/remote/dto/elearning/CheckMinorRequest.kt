package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CheckMinorRequest(
    @SerializedName("age") val age: Int,
    @SerializedName("country") val country: String
)