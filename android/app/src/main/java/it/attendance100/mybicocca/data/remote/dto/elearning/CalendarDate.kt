package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CalendarDate(
    @SerializedName("seconds") val seconds: Int,
    @SerializedName("minutes") val minutes: Int,
    @SerializedName("hours") val hours: Int,
    @SerializedName("mday") val mday: Int,
    @SerializedName("wday") val wday: Int,
    @SerializedName("mon") val mon: Int,
    @SerializedName("year") val year: Int,
    @SerializedName("yday") val yday: Int,
    @SerializedName("weekday") val weekday: String,
    @SerializedName("month") val month: String,
    @SerializedName("timestamp") val timestamp: Int
)