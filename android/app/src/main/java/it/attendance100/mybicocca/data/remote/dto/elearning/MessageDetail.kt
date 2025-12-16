package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class MessageDetail(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("useridfrom") val userIdFrom: Int? = null,
    @SerializedName("useridto") val userIdTo: Int? = null,
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("fullmessage") val fullMessage: String? = null,
    @SerializedName("fullmessageformat") val fullMessageFormat: Int? = null,
    @SerializedName("fullmessagehtml") val fullMessageHtml: String? = null,
    @SerializedName("smallmessage") val smallMessage: String? = null,
    @SerializedName("notification") val notification: Int? = null,
    @SerializedName("contexturl") val contextUrl: String? = null,
    @SerializedName("contexturlname") val contextUrlName: String? = null,
    @SerializedName("timecreated") val timeCreated: Int? = null,
    @SerializedName("timeread") val timeRead: Int? = null,
    @SerializedName("usertofullname") val userToFullName: String? = null,
    @SerializedName("userfromfullname") val userFromFullName: String? = null,
    @SerializedName("component") val component: String? = null,
    @SerializedName("eventtype") val eventType: String? = null,
    @SerializedName("customdata") val customData: String? = null
)