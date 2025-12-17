package it.attendance100.mybicocca.data.dto.elearning

import com.google.gson.annotations.SerializedName

data class GetUserAdministrationOptionsRequest(
    @SerializedName("courseids") val courseids: List<Int>
)
