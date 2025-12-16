package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CalendarEventListItem(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("format") val format: Int? = null,
    @SerializedName("courseid") val courseId: Int? = null,
    @SerializedName("categoryid") val categoryId: Int? = null,
    @SerializedName("groupid") val groupId: Int? = null,
    @SerializedName("userid") val userId: Int? = null,
    @SerializedName("repeatid") val repeatId: Int? = null,
    @SerializedName("modulename") val moduleName: String? = null,
    @SerializedName("instance") val instance: Int? = null,
    @SerializedName("eventtype") val eventType: String? = null,
    @SerializedName("timestart") val timeStart: Int? = null,
    @SerializedName("timeduration") val timeDuration: Int? = null,
    @SerializedName("visible") val visible: Int? = null,
    @SerializedName("uuid") val uuid: String? = null,
    @SerializedName("sequence") val sequence: Int? = null,
    @SerializedName("timemodified") val timeModified: Int? = null,
    @SerializedName("subscriptionid") val subscriptionId: Int? = null
)