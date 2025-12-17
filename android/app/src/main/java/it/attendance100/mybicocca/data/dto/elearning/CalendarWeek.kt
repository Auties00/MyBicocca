package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class CalendarWeek(
    @SerializedName("prepadding") val prePadding: List<Int>? = null,
    @SerializedName("postpadding") val postPadding: List<Int>? = null,
    @SerializedName("days") val days: List<CalendarMonthDay>? = null
)