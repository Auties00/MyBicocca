package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappCalendarResponse(
    @SerializedName("calendar")
    val calendar: List<BicoccappCalendar> = emptyList()
)

