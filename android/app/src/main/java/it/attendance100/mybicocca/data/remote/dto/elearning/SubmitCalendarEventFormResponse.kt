package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SubmitCalendarEventFormResponse(
    @SerializedName("event") val event: CalendarEvent? = null,
    @SerializedName("validationerror") val validationError: Boolean? = null
)