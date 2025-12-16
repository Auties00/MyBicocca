package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetCalendarMonthlyViewRequest(
    @SerializedName("year") val year: Int,
    @SerializedName("month") val month: Int,
    @SerializedName("courseid") val courseId: Int? = null,
    @SerializedName("categoryid") val categoryId: Int? = null,
    @SerializedName("includenavigation") val includeNavigation: Boolean? = null,
    @SerializedName("mini") val mini: Boolean? = null
)