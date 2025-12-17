package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName
import java.net.URI
import java.math.BigDecimal

data class RecentCourse(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("fullname") val fullName: String? = null,
    @SerializedName("shortname") val shortName: String? = null,
    @SerializedName("idnumber") val idNumber: String? = null,
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("summaryformat") val summaryFormat: Int? = null,
    @SerializedName("startdate") val startDate: Int? = null,
    @SerializedName("enddate") val endDate: Int? = null,
    @SerializedName("visible") val visible: Boolean? = null,
    @SerializedName("fullnamedisplay") val fullNameDisplay: String? = null,
    @SerializedName("viewurl") val viewUrl: URI? = null,
    @SerializedName("courseimage") val courseImage: URI? = null,
    @SerializedName("progress") val progress: BigDecimal? = null,
    @SerializedName("hasprogress") val hasProgress: Boolean? = null,
    @SerializedName("isfavourite") val isFavourite: Boolean? = null,
    @SerializedName("hidden") val hidden: Boolean? = null,
    @SerializedName("timeaccess") val timeAccess: Int? = null,
    @SerializedName("showshortname") val showShortName: Boolean? = null,
    @SerializedName("coursecategory") val courseCategory: String? = null
)