package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class CalendarDayName(
    @SerializedName("dayno") val dayNo: Int? = null,
    @SerializedName("shortname") val shortName: String? = null,
    @SerializedName("fullname") val fullName: String? = null
)