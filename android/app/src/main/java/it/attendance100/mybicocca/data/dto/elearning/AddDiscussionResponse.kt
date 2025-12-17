package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class AddDiscussionResponse(
    @SerializedName("discussionid") val discussionId: Int? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
