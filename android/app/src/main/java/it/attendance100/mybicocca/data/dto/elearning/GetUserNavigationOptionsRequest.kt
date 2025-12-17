package it.attendance100.mybicocca.data.dto.elearning

import com.google.gson.annotations.SerializedName

data class GetUserNavigationOptionsRequest(
    @SerializedName("courseids") val courseids: List<Int>
)
